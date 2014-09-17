/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.kiwi.sparql.builder;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.sparql.exception.UnsatisfiableQueryException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a SPARQL UNION in SQL. Essentially, we translate a SPARQL UNION into a SQL subquery using UNION to
 * merge the different parts. Mostly, we can use a new SQLBuilder to construct the subquery. However, what needs to be
 * taken care of is that SQL UNIONS only work when all subqueries have the same columns, but in SPARQL this is not
 * always the case. Furthermore, we need to map variables from the subquery to variables in the enclosing query
 * (using the expressions mapping of our SQLVariable).
 *
 * TODO: proper variable mapping and conditions (in SQLBuilder)
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLUnion extends SQLAbstractSubquery {

    private static Logger log = LoggerFactory.getLogger(SQLUnion.class);

    private SQLBuilder left, right;

    private Set<SQLVariable> variables = new HashSet<>();

    public SQLUnion(String alias, Union query, BindingSet bindings, Dataset dataset, ValueConverter converter, KiWiDialect dialect) throws UnsatisfiableQueryException {
        super(alias);

        // we build a full subquery for each of the UNION's arguments
        left  = new SQLBuilder(query.getLeftArg(), bindings, dataset, converter, dialect);
        right = new SQLBuilder(query.getRightArg(), bindings, dataset, converter, dialect);

        // next we make sure that both subqueries share the same SQL variables so the SQL UNION succeeds by
        // adding NULL aliases for all variables present in one but not the other
        int c = 0;
        Map<String,SQLVariable> leftVars = new HashMap<>();
        for(SQLVariable svl : left.getVariables().values()) {
            leftVars.put(svl.getName(), svl);
        }

        Map<String,SQLVariable> rightVars = new HashMap<>();
        for(SQLVariable svr : right.getVariables().values()) {
            rightVars.put(svr.getName(), svr);
        }

        for(SQLVariable svl : leftVars.values()) {
            if(!rightVars.containsKey(svl.getName())) {
                String vname = "_u_"+alias+"_" + (++c);
                SQLVariable svr = new SQLVariable(svl.getName(), vname);
                svr.getExpressions().add("NULL");
                svr.setProjectionType(ProjectionType.NODE);
                right.getVariables().put(vname,svr);
            }
            variables.add(svl);
        }

        for(SQLVariable svr : rightVars.values()) {
            if(!leftVars.containsKey(svr.getName())) {
                String vname = "_u_"+alias+"_" + (++c);
                SQLVariable svl = new SQLVariable(svr.getName(), vname);
                svl.getExpressions().add("NULL");
                svl.setProjectionType(ProjectionType.NODE);
                left.getVariables().put(vname,svl);
            }
            variables.add(svr);
        }

        log.debug("UNION variables: {}", variables);
    }


    /**
     * Return the SQL variables used by the subquery; we need this to do proper mapping in the parent query.
     *
     * @return
     */
    @Override
    public Set<SQLVariable> getQueryVariables() {
        return variables;
    }

    /**
     * Build the query fragment that can be used in the FROM clause of a SQL query for representing this SPARQL construct.
     * The fragment will be joined appropriately by the enclosing construct using CROSS JOIN, LEFT JOIN or normal JOIN.
     *
     * @return
     */
    @Override
    public String buildFromClause() {
        StringBuilder fromClause = new StringBuilder();
        fromClause
                .append("((")
                .append(left.build())
                .append(") UNION (")
                .append(right.build())
                .append(")) AS ")
                .append(alias);
        return fromClause.toString();
    }


    /**
     * Return the projection type of an expression in this subquery. Needed for propagation up to the parent.
     *
     * @param expr
     * @return
     */
    @Override
    protected ProjectionType getProjectionType(ValueExpr expr) {
        ProjectionType r = left.getProjectionType(expr);
        if(r == ProjectionType.STRING) {
            r = right.getProjectionType(expr);
        }
        return r;
    }
}


/*
Example:

PREFIX foaf: <http://xmlns.com/foaf/0.1/>

SELECT ?p ?name ?likes WHERE {
    ?p foaf:name ?name .
    { ?p foaf:knows ?likes }
    UNION
    { ?p foaf:interest ?likes }

}

translated to:

SELECT SUB1.V3 AS V3, P1.object AS V2, P1.subject AS V1
 FROM triples P1
 JOIN
 (
  (SELECT P2.subject AS V1, P2.object AS V3, P2.predicate AS V2 FROM triples P2 WHERE P2.deleted=false AND P2.predicate = 512217739590426624)
 UNION
  (SELECT P3.subject AS V1, P3.object AS V3, NULL AS V2 FROM triples P3 WHERE P3.deleted=false AND P3.predicate = 512217739326185472)
 ) AS SUB1 ON (P1.subject = SUB1.V1)
 WHERE P1.deleted = false
      AND P1.predicate = 512217739124858880


 */
