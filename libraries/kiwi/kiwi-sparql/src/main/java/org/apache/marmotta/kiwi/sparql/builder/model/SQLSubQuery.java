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

package org.apache.marmotta.kiwi.sparql.builder.model;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.sparql.builder.SQLBuilder;
import org.apache.marmotta.kiwi.sparql.builder.ValueConverter;
import org.apache.marmotta.kiwi.sparql.exception.UnsatisfiableQueryException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;

import java.util.HashSet;
import java.util.Set;

/**
 * Representation of a simple SPARQL->SQL subquery (no union). We can use a new SQLBuilder to construct the subquery
 * and add appropriate variable mappings.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLSubQuery extends SQLAbstractSubquery {

    private SQLBuilder builder;

    private Set<SQLVariable> variables = new HashSet<>();

    public SQLSubQuery(String alias, Projection query, BindingSet bindings, Dataset dataset, ValueConverter converter, KiWiDialect dialect, Set<String> parentProjectedVars) throws UnsatisfiableQueryException {
        super(alias);

        Set<String> projectedVars = new HashSet<>(parentProjectedVars);
        // count projected variables
        for(ProjectionElem elem : query.getProjectionElemList().getElements()) {
            projectedVars.add(elem.getSourceName());
        }



        // we build a full subquery for each of the UNION's arguments
        builder = new SQLBuilder(query.getArg(), bindings, dataset, converter, dialect, projectedVars);

        for(SQLVariable svl : builder.getVariables().values()) {
            if(projectedVars.contains(svl.getSparqlName())) {
                variables.add(svl);
            }
        }
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
                .append("(")
                .append(builder.build())
                .append(") AS ")
                .append(alias);

        for(VariableMapping var : getJoinFields()) {
            fromClause.append(" LEFT JOIN nodes AS ");  // outer join because binding might be NULL
            fromClause.append(alias).append("_").append(var.getParentName());

            fromClause
                    .append(" ON ").append(alias).append(".").append(var.getSubqueryName())
                    .append(" = ").append(alias).append("_").append(var.getParentName()).append(".id ");
        }

        return fromClause.toString();
    }

}
