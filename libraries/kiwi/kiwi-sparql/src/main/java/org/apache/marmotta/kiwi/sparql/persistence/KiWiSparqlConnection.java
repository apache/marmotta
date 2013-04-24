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

package org.apache.marmotta.kiwi.sparql.persistence;

import com.google.common.base.Preconditions;
import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.kiwi.caching.KiWiCacheManager;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.persistence.util.ResultSetIteration;
import org.apache.marmotta.kiwi.persistence.util.ResultTransformerFunction;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.impl.MapBindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Provide improved SPARQL support by evaluating certain common compley SPARQL constructs directly on the
 * database (e.g. JOIN over pattern queries).
 * <p/>
 * Implemented using a decorator pattern (i.e. wrapping the KiWiConnection).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiSparqlConnection {

    private static Logger log = LoggerFactory.getLogger(KiWiSparqlConnection.class);

    private KiWiConnection parent;

    public KiWiSparqlConnection(KiWiConnection parent) throws SQLException {
        this.parent = parent;
    }

    /**
     * Evaluate a statement pattern join on the database by translating it into an appropriate SQL statement.
     * Copied and adapted from KiWiReasoningConnection.query()
     * @param join
     * @return
     */
    public CloseableIteration<BindingSet, SQLException> evaluateJoin(Join join, final BindingSet bindings) throws SQLException {
        Preconditions.checkArgument(join.getLeftArg() instanceof StatementPattern || join.getLeftArg() instanceof Join);
        Preconditions.checkArgument(join.getRightArg() instanceof StatementPattern || join.getRightArg() instanceof Join);

        // some definitions
        String[] positions = new String[] {"subject","predicate","object","context"};

        // collect all patterns in a list, using depth-first search over the join
        List<StatementPattern> patterns = new ArrayList<StatementPattern>();
        collectPatterns(join, patterns);

        // associate a name with each pattern; the names are used in the database query to refer to the triple
        // that matched this pattern and in the construction of variable names for the HQL query
        int patternCount = 0;
        final Map<StatementPattern,String> patternNames = new HashMap<StatementPattern, String>();
        for(StatementPattern p : patterns) {
            patternNames.put(p,"P"+ (++patternCount));
        }

        // find all variables occurring in the patterns and create a map to map them to
        // field names in the database query; each variable will have one or several field names,
        // one for each pattern it occurs in; field names are constructed automatically by a counter
        // and the pattern name to ensure the name is a valid HQL identifier
        int variableCount = 0;

        // a map for the variable names; will look like { ?x -> "V1", ?y -> "V2", ... }
        final Map<Var,String> variableNames = new HashMap<Var, String>();

        // a map for mapping variables to field names; each variable might have one or more field names,
        // depending on the number of patterns it occurs in; will look like
        // { ?x -> ["P1_V1", "P2_V1"], ?y -> ["P2_V2"], ... }
        Map<Var,List<String>> queryVariables = new HashMap<Var, List<String>>();
        for(StatementPattern p : patterns) {
            Var[] fields = new Var[] {
                    p.getSubjectVar(),
                    p.getPredicateVar(),
                    p.getObjectVar(),
                    p.getContextVar()
            };
            for(int i = 0; i<fields.length; i++) {
                if(fields[i] != null && !fields[i].hasValue()) {
                    Var v = fields[i];
                    if(variableNames.get(v) == null) {
                        variableNames.put(v,"V"+ (++variableCount));
                        queryVariables.put(v,new LinkedList<String>());
                    }
                    String pName = patternNames.get(p);
                    String vName = variableNames.get(v);
                    queryVariables.get(v).add(pName + "_" + positions[i] + "_" + vName);
                }
            }
        }

        // build the select clause by projecting for each query variable the first name
        StringBuilder selectClause = new StringBuilder();
        final List<Var> selectVariables = new LinkedList<Var>();
        for(Iterator<Var> it = queryVariables.keySet().iterator(); it.hasNext(); ) {
            Var v = it.next();
            String projectedName = variableNames.get(v);
            String fromName = queryVariables.get(v).get(0);
            selectClause.append(fromName);
            selectClause.append(".id as ");
            selectClause.append(projectedName);
            if(it.hasNext()) {
                selectClause.append(", ");
            }
            selectVariables.add(v);
        }


        // build the from-clause of the query; the from clause is constructed as follows:
        // 1. for each pattern P, there will be a "KiWiTriple P" in the from clause
        // 2. for each variable V in P occurring in
        //    - subject, there will be a "inner join P.subject as P_S_V" or "left outer join P.subject as P_S_V",
        //      depending on whether the "optional" parameter is false or true
        //    - property, there will be a "inner join P.property as P_P_V" or "left outer join p.property as P_P_V"
        //    - object, there will be a "inner join P.object as P_O_V" or "left outer join p.object as P_O_V"
        //    - context, there will be a "inner join P.context as P_C_V" or "left outer join p.context as P_C_V"
        StringBuilder fromClause = new StringBuilder();
        for(Iterator<StatementPattern> it = patterns.iterator(); it.hasNext(); ) {
            StatementPattern p = it.next();
            String pName = patternNames.get(p);
            fromClause.append("triples "+pName);

            Var[] fields = new Var[] {
                    p.getSubjectVar(),
                    p.getPredicateVar(),
                    p.getObjectVar(),
                    p.getContextVar()
            };
            for(int i = 0; i<fields.length; i++) {
                if(fields[i] != null && !fields[i].hasValue()) {
                    String vName = variableNames.get(fields[i]);
                    fromClause.append(" INNER JOIN nodes AS ");
                    fromClause.append(pName + "_"+positions[i]+"_" + vName);
                    fromClause.append(" ON " + pName + "." + positions[i] + " = ");
                    fromClause.append(pName + "_"+positions[i]+"_" + vName + ".id ");
                }
            }

            if(it.hasNext()) {
                fromClause.append(",\n ");
            }
        }


        // build the where clause as follows:
        // 1. iterate over all patterns and for each resource and literal field in subject,
        //    property, object, or context, and set a query condition according to the
        //    nodes given in the pattern
        // 2. for each variable that has more than one occurrences, add a join condition
        // 3. for each variable in the initialBindings, add a condition to the where clause

        // list of where conditions that will later be connected by AND
        List<String> whereConditions = new LinkedList<String>();


        // 1. iterate over all patterns and for each resource and literal field in subject,
        //    property, object, or context, and set a query condition according to the
        //    nodes given in the pattern
        for(StatementPattern p : patterns) {
            String pName = patternNames.get(p);
            Var[] fields = new Var[] {
                    p.getSubjectVar(),
                    p.getPredicateVar(),
                    p.getObjectVar(),
                    p.getContextVar()
            };
            for(int i = 0; i<fields.length; i++) {
                // find node id of the resource or literal field and use it in the where clause
                // in this way we can avoid setting too many query parameters
                Long nodeId = null;
                if(fields[i] != null && fields[i].hasValue()) {
                    Value v = fields[i].getValue();
                    if(v instanceof KiWiNode) {
                        nodeId = ((KiWiNode) v).getId();
                    } else {
                        throw new IllegalArgumentException("the values in this query have not been created by the KiWi value factory");
                    }

                    if(nodeId != null) {
                        String condition = pName+"."+positions[i]+" = " + nodeId;
                        whereConditions.add(condition);
                    }
                }
            }
        }

        // 2. for each variable that has more than one occurrences, add a join condition
        for(Var v : queryVariables.keySet()) {
            List<String> vNames = queryVariables.get(v);
            for(int i = 1; i < vNames.size(); i++) {
                String vName1 = vNames.get(i-1);
                String vName2 = vNames.get(i);
                whereConditions.add(vName1 + ".id = " + vName2 + ".id");
            }
        }

        // 3. for each variable in the initialBindings, add a condition to the where clause setting it
        //    to the node given as binding
        if(bindings != null) {
            for(String v : bindings.getBindingNames()) {
                for(Map.Entry<Var,List<String>> entry : queryVariables.entrySet()) {
                    if(entry.getKey().getName() != null && entry.getKey().getName().equals(v) &&
                            entry.getValue() != null && entry.getValue().size() > 0) {
                        List<String> vNames = queryVariables.get(v);
                        String vName = vNames.get(0);
                        Value binding = bindings.getValue(v);
                        if(binding instanceof KiWiNode) {
                            whereConditions.add(vName+".id = "+((KiWiNode)binding).getId());
                        } else {
                            throw new IllegalArgumentException("the values in this binding have not been created by the KiWi value factory");
                        }
                    }
                }
            }
        }

        // 4. for each pattern, ensure that the matched triple is not marked as deleted
        for(StatementPattern p : patterns) {
            String pName = patternNames.get(p);
            whereConditions.add(pName+".deleted = false");
        }

        // construct the where clause
        StringBuilder whereClause = new StringBuilder();
        for(Iterator<String> it = whereConditions.iterator(); it.hasNext(); ) {
            whereClause.append(it.next());
            whereClause.append("\n ");
            if(it.hasNext()) {
                whereClause.append("AND ");
            }
        }


        // build the query string
        String queryString =
                "SELECT " + selectClause + "\n " +
                        "FROM " + fromClause + "\n " +
                        "WHERE " + whereClause;

        log.debug("constructed SQL query string {}",queryString);

        PreparedStatement queryStatement = parent.getJDBCConnection().prepareStatement(queryString);
        ResultSet result = queryStatement.executeQuery();

        return new ResultSetIteration<BindingSet>(result, true, new ResultTransformerFunction<BindingSet>() {
            @Override
            public BindingSet apply(ResultSet row) throws SQLException {
                MapBindingSet resultRow = new MapBindingSet();

                for(Var v : selectVariables) {
                    resultRow.addBinding(v.getName(), parent.loadNodeById(row.getLong(variableNames.get(v))));
                }


                if(bindings != null) {
                    for(Binding binding : bindings) {
                        resultRow.addBinding(binding);
                    }
                }
                return resultRow;
            }
        });

    }

    /**
     * Collect all statement patterns in a tuple expression in depth-first order. The tuple expression may only
     * contain Join or StatementPattern expressions, otherwise an IllegalArgumentException is thrown.
     * @param expr
     * @param patterns
     */
    private void collectPatterns(TupleExpr expr, List<StatementPattern> patterns) {
        if(expr instanceof Join) {
            collectPatterns(((Join) expr).getLeftArg(), patterns);
            collectPatterns(((Join) expr).getRightArg(), patterns);
        } else if(expr instanceof StatementPattern) {
            patterns.add((StatementPattern)expr);
        } else {
            throw new IllegalArgumentException("the tuple expression was neither a join nor a statement pattern: "+expr);
        }

    }
}
