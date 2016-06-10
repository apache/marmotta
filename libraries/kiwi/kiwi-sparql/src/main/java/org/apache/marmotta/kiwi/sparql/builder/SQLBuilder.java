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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.apache.marmotta.kiwi.sparql.builder.collect.*;
import org.apache.marmotta.kiwi.sparql.builder.eval.ValueExpressionEvaluator;
import org.apache.marmotta.kiwi.sparql.builder.model.SQLAbstractSubquery;
import org.apache.marmotta.kiwi.sparql.builder.model.SQLFragment;
import org.apache.marmotta.kiwi.sparql.builder.model.SQLPattern;
import org.apache.marmotta.kiwi.sparql.builder.model.SQLVariable;
import org.apache.marmotta.kiwi.sparql.exception.UnsatisfiableQueryException;
import org.apache.marmotta.kiwi.sparql.function.NativeFunctionRegistry;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A builder for translating SPARQL queries into SQL.
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class SQLBuilder {

    private static Logger log = LoggerFactory.getLogger(SQLBuilder.class);

    /**
     * Simplify access to different node ids
     */
    private static final String[] positions = new String[] {"subject","predicate","object","context"};

    /**
     * Reference to the registry of natively supported functions with parameter and return types as well as SQL translation
     */
    private NativeFunctionRegistry functionRegistry = NativeFunctionRegistry.getInstance();

    /**
     * Query results should be DISTINCT.
     */
    private boolean distinct = false;

    /**
     * Result offset, translated into an OFFSET expression; ignored if <0
     */
    private long offset = -1;

    /**
     * Result limit, translated into a LIMIT expression; ignored if <= 0
     */
    private long limit  = -1;

    private List<OrderElem> orderby;
    private List<ExtensionElem> extensions;

    private Set<String>     groupLabels;

    // list of SPARQL variable names that are actually contained in the projection
    private Set<String>     projectedVars;

    /**
     * Maintains a mapping between SPARQL variable names and internal variable descriptions. The internal description
     * contains information whether the variable needs to be projected, what SQL expressions represent this variable,
     * and what internal aliases to use.
     */
    private Map<String,SQLVariable> variables;
    protected void addVariable(SQLVariable v) {
        variables.put(v.getSparqlName(),v);
    }


    /**
     * The SQL fragments and triple patterns collected from the query. SQLFragments are LEFT JOINed with each other,
     * so for each OPTIONAL found in the query, a new fragment is appended here. Inside a fragment, the patterns
     * are CROSS JOINed with each other.
     *
     * @see org.apache.marmotta.kiwi.sparql.builder.collect.PatternCollector
     */
    private List<SQLFragment> fragments;

    /**
     * Contains the names  those variables for which the value is needed instead of the ID, because the value is used
     * somewhere in a condition, function or other kind of value expression.
     *
     * @see org.apache.marmotta.kiwi.sparql.builder.collect.ConditionFinder
     */
    private Set<String> resolveVariables;

    private TupleExpr query;

    private BindingSet bindings;

    private Dataset dataset;

    private ValueConverter converter;

    private KiWiDialect dialect;

    // a prefix for naming table aliases (needed in case this is a subquery)
    private String prefix;

    /**
     * Create a new SQLBuilder for the given query, initial bindings, dataset, and
     * @param query
     * @param bindings
     * @param dataset
     */
    public SQLBuilder(TupleExpr query, BindingSet bindings, Dataset dataset, final KiWiValueFactory valueFactory, KiWiDialect dialect, Set<String> projectedVars) throws UnsatisfiableQueryException {
        this(query, bindings, dataset, new ValueConverter() {
            @Override
            public KiWiNode convert(Value value) {
                return valueFactory.convert(value);
            }
        }, dialect, projectedVars);
    }


    public SQLBuilder(TupleExpr query, BindingSet bindings, Dataset dataset, ValueConverter converter, KiWiDialect dialect, Set<String> projectedVars) throws UnsatisfiableQueryException {
        this(query,bindings, dataset, converter, dialect, "", projectedVars, new HashMap<String, SQLVariable>());
    }

    /**
     * Create a new SQLBuilder for the given query, initial bindings, dataset, and
     * @param query
     * @param bindings
     * @param dataset
     */
    public SQLBuilder(TupleExpr query, BindingSet bindings, Dataset dataset, ValueConverter converter, KiWiDialect dialect, String prefix, Set<String> projectedVars, Map<String,SQLVariable> variables) throws UnsatisfiableQueryException {
        this.query = query;
        this.bindings = bindings;
        this.dataset = dataset;
        this.converter = converter;
        this.dialect = dialect;
        this.projectedVars = projectedVars;
        this.prefix = prefix;
        this.variables = variables;

        prepareBuilder();
    }

    public Map<String, SQLVariable> getVariables() {
        return variables;
    }

    public ValueConverter getConverter() {
        return converter;
    }

    public KiWiDialect getDialect() {
        return dialect;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public BindingSet getBindings() {
        return bindings;
    }

    public Set<String> getProjectedVars() {
        return projectedVars;
    }

    private void prepareBuilder()  throws UnsatisfiableQueryException {
        Preconditions.checkArgument(query instanceof Projection || query instanceof Union || query instanceof Extension || query instanceof Order || query instanceof Group || query instanceof LeftJoin ||query instanceof Join || query instanceof Filter || query instanceof StatementPattern || query instanceof Distinct || query instanceof Slice || query instanceof Reduced);


        // collect all patterns in a list, using depth-first search over the join
        PatternCollector pc = new PatternCollector(query, bindings, dataset, converter, dialect, projectedVars, prefix);

        fragments = pc.parts;

        // collect offset and limit from the query if given
        offset   = new LimitFinder(query).offset;
        limit    = new LimitFinder(query).limit;

        // check if query is distinct
        distinct = new DistinctFinder(query).distinct;

        // find the ordering
        orderby  = new OrderFinder(query).elements;

        // find the grouping
        GroupFinder gf  = new GroupFinder(query);
        groupLabels      = gf.bindings;

        // find extensions (BIND)
        extensions = new ExtensionFinder(query).elements;

        // find variables that need to be resolved
        resolveVariables = new ConditionFinder(query).neededVariables;

        int variableCount = 0;

        // find all variables that have been bound already, even if they do not appear in a pattern
        for(Var v : new VariableFinder(query).variables) {
            if(v.hasValue() && !v.getName().startsWith("-const")) {
                SQLVariable sv = variables.get(v.getName());
                if(sv == null) {
                    sv = new SQLVariable("V" + (++variableCount), v.getName());

                    // select those variables that are really projected and not only needed in a grouping construct
                    if(projectedVars.contains(sv.getSparqlName()) || new SQLProjectionFinder(query,v.getName()).found) {
                        sv.setProjectionType(ValueType.NODE);
                    }

                    sv.getExpressions().add(""+ converter.convert(v.getValue()).getId());

                    addVariable(sv);
                }
            }
        }

        // find all variables occurring in the patterns and create a map to map them to
        // field names in the database query; each variable will have one or several field names,
        // one for each pattern it occurs in; field names are constructed automatically by a counter
        // and the pattern name to ensure the name is a valid HQL identifier
        for(SQLFragment f : fragments) {
            for (SQLPattern p : f.getPatterns()) {
                // build pattern
                Var[] fields = p.getFields();
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i] != null && (!fields[i].hasValue() || !fields[i].getName().startsWith("-const"))) {
                        Var v = fields[i];

                        SQLVariable sv = variables.get(v.getName());
                        if(sv == null) {
                            sv = new SQLVariable("V" + (++variableCount), v.getName());

                            // select those variables that are really projected and not only needed in a grouping construct
                            if(projectedVars.contains(sv.getSparqlName()) || new SQLProjectionFinder(query,v.getName()).found) {
                                sv.setProjectionType(ValueType.NODE);
                            }

                            String pName = p.getName();
                            String vName = sv.getName();

                            if (sv.getAlias() == null && resolveVariables.contains(v.getName())) {
                                sv.setAlias(pName + "_" + positions[i] + "_" + vName);
                            }

                            addVariable(sv);
                        }
                    }
                }
            }

            // subqueries: look up which variables are bound in the subqueries and add proper aliases
            for(SQLAbstractSubquery sq : f.getSubqueries()) {
                for(SQLVariable sq_v : sq.getQueryVariables()) {
                    SQLVariable sv = variables.get(sq_v.getSparqlName());

                    if(sv == null) {
                        sv = new SQLVariable("V" + (++variableCount), sq_v.getSparqlName());

                        // select those variables that are really projected and not only needed in a grouping construct
                        if(projectedVars.contains(sv.getSparqlName()) || new SQLProjectionFinder(query,sq_v.getSparqlName()).found) {
                            sv.setProjectionType(sq_v.getProjectionType());
                        }

                        String sqName = sq.getAlias();
                        String vName = sv.getName();

                        if (sv.getAlias() == null && resolveVariables.contains(sq_v.getSparqlName())) {
                            sv.setAlias(sqName + "_" + vName);
                        }

                        addVariable(sv);
                    }

                }
            }
        }


        // add all extensions to the variable list so they are properly considered in projections and clauses
        // TODO: order by variable dependency, or otherwise the evaluateExpression might fail
        List<ExtensionElem> deferredExtensions = new ArrayList<>(); // some extension might need to be evaluated later because their expressions cannot be computed yet
        for(ExtensionElem ext : extensions) {
            Var v = new Var(ext.getName());

            SQLVariable sv = variables.get(v.getName());
            if(!variables.containsKey(v.getName())) {
                sv = new SQLVariable("V" + (++variableCount), v.getName());

                // select those variables that are really projected and not only needed in a grouping construct
                if(projectedVars.contains(sv.getSparqlName()) || new SQLProjectionFinder(query,v.getName()).found) {
                    sv.setProjectionType(getProjectionType(ext.getExpr()));
                }

                // Functions that return a string literal do so with the string literal of the same kind as the first
                // argument (simple literal, plain literal with same language tag, xsd:string).
                ValueType type = getProjectionType(ext.getExpr());
                if(type == ValueType.STRING) {
                    sv.setLiteralTypeExpression(getLiteralTypeExpression(ext.getExpr()));
                    sv.setLiteralLangExpression(getLiteralLangExpression(ext.getExpr()));
                    // TODO: the following will produce invalid results for aggregation functions
                /*
                } else if(type == ProjectionType.INT || type == ProjectionType.DOUBLE || type == ProjectionType.BOOL) {
                    sv.setLiteralTypeExpression(getLiteralTypeExpression(ext.getExpr()));
                */
                }

                addVariable(sv);
            }

            // TODO: ANY as OPType here is dangerous, because the OPType should depends on projection and actual use
            //       of variables in conditions etc
            if (resolveVariables.contains(v.getName())) {
                //sv.getAliases().add(evaluateExpression(ext.getExpr(), OPTypes.VALUE));
                sv.getBindings().add(ext.getExpr());
            }

            try {
                sv.getExpressions().add(evaluateExpression(ext.getExpr(), ValueType.NODE));
                if(sv.getProjectionType() == ValueType.NODE && getProjectionType(ext.getExpr()) != ValueType.NODE) {
                    sv.setProjectionType(getProjectionType(ext.getExpr()));
                }
            } catch(IllegalStateException ex) {
                deferredExtensions.add(ext);
            }

        }

        // calculate for each variable the SQL expressions representing them and any necessary JOIN conditions
        for (SQLFragment f : fragments) {
            for (SQLPattern p : f.getPatterns()) {
                // build pattern
                Var[] fields = p.getFields();
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i] != null && (!fields[i].hasValue() || !fields[i].getName().startsWith("-const"))) {
                        Var v = fields[i];

                        SQLVariable sv = variables.get(v.getName());

                        String pName = p.getName();

                        // if the variable has been used before, add a join condition to the first occurrence
                        if(sv.getExpressions().size() > 0) {
                            // case distinction: is this variable projected as node or as another value in an extension?
                            // if it is a value, we need to refer to the corresponding typed column of the node, otherwise
                            // to the node ID (field ID is sufficient)
                            switch (sv.getProjectionType()) {
                                case INT:
                                    p.getConditions().add(sv.getExpressions().get(0) + " = " + sv.getAlias() + ".ivalue");
                                    break;
                                case DECIMAL:
                                case DOUBLE:
                                    p.getConditions().add(sv.getExpressions().get(0) + " = " + sv.getAlias() + ".dvalue");
                                    break;
                                case DATE:
                                case TZDATE:
                                    p.getConditions().add(sv.getExpressions().get(0) + " = " + sv.getAlias() + ".tvalue");
                                    break;
                                case BOOL:
                                    p.getConditions().add(sv.getExpressions().get(0) + " = " + sv.getAlias() + ".bvalue");
                                    break;
                                case URI:
                                case STRING:
                                    p.getConditions().add(sv.getExpressions().get(0) + " = " + sv.getAlias() + ".svalue");
                                    break;

                                default:
                                    p.getConditions().add(sv.getExpressions().get(0) + " = " + pName + "." + positions[i]);
                                    break;
                            }
                        }

                        sv.getExpressions().add(pName + "." + positions[i]);
                    }
                }
            }

            // subqueries: look up which variables are bound in the subqueries and add proper aliases
            for (SQLAbstractSubquery sq : f.getSubqueries()) {
                for (SQLVariable sq_v : sq.getQueryVariables()) {
                    SQLVariable sv = variables.get(sq_v.getSparqlName());

                    String sqName = sq.getAlias();

                    // if the variable has been used before, add a join condition to the first occurrence
                    if(sv.getExpressions().size() > 0) {
                        sq.getConditions().add(sv.getExpressions().get(0) + " = " + sqName + "." + sq_v.getName());
                    }

                    sv.getExpressions().add(sqName + "." + sq_v.getName());

                }
            }
        }

        for(ExtensionElem ext : deferredExtensions) {
            Var v = new Var(ext.getName());

            SQLVariable sv = variables.get(v.getName());
            sv.getExpressions().add(evaluateExpression(ext.getExpr(), ValueType.NODE));
        }

        // find context restrictions of patterns and match them with potential restrictions given in the
        // dataset (MARMOTTA-340)
        for(SQLFragment f : fragments) {
            for (SQLPattern p : f.getPatterns()) {
                Resource[] contexts;
                Value contextValue = p.getSparqlPattern().getContextVar() != null ? p.getSparqlPattern().getContextVar().getValue() : null;

                Set<URI> graphs = null;
                boolean emptyGraph = false;

                if (dataset != null) {
                    if (p.getSparqlPattern().getScope() == StatementPattern.Scope.DEFAULT_CONTEXTS) {
                        graphs = dataset.getDefaultGraphs();
                        emptyGraph = graphs.isEmpty() && !dataset.getNamedGraphs().isEmpty();
                    } else {
                        graphs = dataset.getNamedGraphs();
                        emptyGraph = graphs.isEmpty() && !dataset.getDefaultGraphs().isEmpty();
                    }
                }

                // set the contexts to query according to the following rules:
                // 1. if the context defined in the dataset does not exist, there will be no result, so set "empty" to true
                // 2. if no context graphs have been given, use the context from the statement
                // 3. if context graphs have been given and the statement has a context, check if the statement context is
                //    contained in the context graphs; if no, set "empty" to true as there can be no result
                // 4. if context graphs have been given and the statement has no context, use the contexts from the
                //    dataset

                if (emptyGraph) {
                    // Search zero contexts
                    throw new UnsatisfiableQueryException("dataset does not contain any default graphs");
                } else if (graphs == null || graphs.isEmpty()) {
                    if (contextValue != null) {
                        contexts = new Resource[]{(Resource) contextValue};
                    } else {
                        contexts = new Resource[0];
                    }
                } else if (contextValue != null) {
                    if (graphs.contains(contextValue)) {
                        contexts = new Resource[]{(Resource) contextValue};
                    } else {
                        // Statement pattern specifies a context that is not part of
                        // the dataset
                        throw new UnsatisfiableQueryException("default graph does not contain statement context '" + contextValue.stringValue() + "'");
                    }
                } else {
                    contexts = new Resource[graphs.size()];
                    int i = 0;
                    for (URI graph : graphs) {
                        URI context = null;
                        if (!SESAME.NIL.equals(graph)) {
                            context = graph;
                        }
                        contexts[i++] = context;
                    }
                }


                // build an OR query for the value of the context variable
                if (contexts.length > 0) {
                    p.setVariableContexts(Arrays.asList(contexts));
                }
            }
        }

        prepareConditions();
    }

    private void prepareConditions() throws UnsatisfiableQueryException {
        // build the where clause as follows:
        // 1. iterate over all patterns and for each resource and literal field in subject,
        //    property, object, or context, and set a query condition according to the
        //    nodes given in the pattern
        // 2. for each variable that has more than one occurrences, add a join condition
        // 3. for each variable in the initialBindings, add a condition to the where clause


        // iterate over all fragments and add translate the filter conditions into SQL
        for(SQLFragment f : fragments) {
            for(ValueExpr e : f.getFilters()) {
                f.getConditions().add(evaluateExpression(e, ValueType.NODE));
            }
        }


        // 1. iterate over all patterns and for each resource and literal field in subject,
        //    property, object, or context, and set a query condition according to the
        //    nodes given in the pattern
        for(SQLFragment f : fragments) {
            for (SQLPattern p : f.getPatterns()) {
                String pName = p.getName();
                Var[] fields = p.getFields();
                for (int i = 0; i < fields.length; i++) {
                    // find node id of the resource or literal field and use it in the where clause
                    // in this way we can avoid setting too many query parameters
                    long nodeId = -1;
                    if (fields[i] != null && fields[i].hasValue()) {
                        Value v = converter.convert(fields[i].getValue());
                        if (v instanceof KiWiNode) {
                            nodeId = ((KiWiNode) v).getId();
                        } else {
                            throw new UnsatisfiableQueryException("the values in this query have not been created by the KiWi value factory");
                        }

                        if (nodeId >= 0) {
                            String condition = pName + "." + positions[i] + " = " + nodeId;
                            p.getConditions().add(condition);
                        }
                    }
                }
            }
        }


        // 6. for each context variable with a restricted list of contexts, we add a condition to the where clause
        //    of the form (V.id = R1.id OR V.id = R2.id ...)
        for(SQLFragment f : fragments) {
            for (SQLPattern p : f.getPatterns()) {
                // the variable
                String varName = p.getName();

                if (p.getVariableContexts() != null) {
                    // the string we are building
                    StringBuilder cCond = new StringBuilder();
                    cCond.append("(");
                    for (Iterator<Resource> it = p.getVariableContexts().iterator(); it.hasNext(); ) {
                        Value v = converter.convert(it.next());
                        if (v instanceof KiWiNode) {
                            long nodeId = ((KiWiNode) v).getId();

                            cCond.append(varName).append(".context = ").append(nodeId);

                            if (it.hasNext()) {
                                cCond.append(" OR ");
                            }
                        } else {
                            throw new UnsatisfiableQueryException("the values in this query have not been created by the KiWi value factory");
                        }

                    }
                    cCond.append(")");
                    p.getConditions().add(cCond.toString());
                }
            }
        }

        // for each pattern, update the joinFields by checking if subject, predicate, object or context are involved
        // in any filters or functions; in this case, the corresponding field needs to be joined with the NODES table
        // and we need to mark the pattern accordingly.
        boolean first = true;
        Set<String> joined = new HashSet<>();
        for(SQLFragment f : fragments) {
            if(first && f.getConditionPosition() == SQLFragment.ConditionPosition.JOIN) {
                // the conditions of the first fragment need to be placed in the WHERE part of the query, because
                // there is not necessarily a JOIN ... ON where we can put it
                f.setConditionPosition(SQLFragment.ConditionPosition.WHERE);
            }
            first = false;

            for (SQLPattern p : f.getPatterns()) {
                for(Map.Entry<SQLPattern.TripleColumns, Var> fieldEntry : p.getTripleFields().entrySet()) {
                    if(fieldEntry.getValue() != null && !fieldEntry.getValue().hasValue() && !joined.contains(fieldEntry.getValue().getName())
                        && resolveVariables.contains(fieldEntry.getValue().getName())) {
                        p.setJoinField(fieldEntry.getKey(), variables.get(fieldEntry.getValue().getName()).getName());
                        joined.add(fieldEntry.getValue().getName());
                    }
                }
            }

            for(SQLAbstractSubquery sq : f.getSubqueries()) {
                for(SQLVariable sq_v : sq.getQueryVariables()) {
                    if(!joined.contains(sq_v.getSparqlName()) && resolveVariables.contains(sq_v.getSparqlName()) && sq_v.getProjectionType() == ValueType.NODE) {
                        // this is needed in case we need to JOIN with the NODES table to retrieve values
                        SQLVariable sv = variables.get(sq_v.getSparqlName());  // fetch the name of the variable in the enclosing query
                        sq.getJoinFields().add(new SQLAbstractSubquery.VariableMapping(sv.getName(), sq_v.getName()));
                        joined.add(sq_v.getSparqlName());
                    }
                }
            }
        }
    }

    private StringBuilder buildSelectClause() {
        List<String> projections = new ArrayList<>();

        // enforce order in SELECT part, we need this for merging UNION subqueries
        List<SQLVariable> vars = new ArrayList<>(variables.values());
        Collections.sort(vars, SQLVariable.sparqlNameComparator);

        for(SQLVariable v : vars) {
            if(v.getProjectionType() != ValueType.NONE && (projectedVars.isEmpty() || projectedVars.contains(v.getSparqlName()))) {
                String projectedName = v.getName();

                if (v.getExpressions() != null && v.getExpressions().size() > 0) {
                    String fromName = v.getExpressions().get(0);
                    projections.add(fromName + " AS " + projectedName);
                }

                if(v.getLiteralTypeExpression() != null) {
                    projections.add(v.getLiteralTypeExpression() + " AS " + projectedName + "_TYPE");
                }

                if(v.getLiteralLangExpression() != null) {
                    projections.add(v.getLiteralLangExpression() + " AS " + projectedName + "_LANG");
                }
            } else {
                projections.add("NULL"); //fix for MARMOTTA-460
            }
        }

        // SQL enforces ORDER BY variables to occur in the select part in case distinct is set
        int counter = 0;
        if(distinct) {
            for(OrderElem e : orderby) {
                projections.add(evaluateExpression(e.getExpr(), ValueType.STRING) + " AS _OB" + (++counter));
            }
        }

        StringBuilder selectClause = new StringBuilder();

        if(distinct) {
            selectClause.append("DISTINCT ");
        }

        Joiner.on(", ").appendTo(selectClause, projections);

        return selectClause;
    }


    private StringBuilder buildFromClause() {
        // build the from-clause of the query; the from clause is constructed as follows:
        // 1. for each pattern P, there will be a "KiWiTriple P" in the from clause
        // 2. for each variable V in P occurring in
        //    - subject, there will be a "inner join P.subject as P_S_V" or "left outer join P.subject as P_S_V",
        //      depending on whether the "optional" parameter is false or true
        //    - property, there will be a "inner join P.property as P_P_V" or "left outer join p.property as P_P_V"
        //    - object, there will be a "inner join P.object as P_O_V" or "left outer join p.object as P_O_V"
        //    - context, there will be a "inner join P.context as P_C_V" or "left outer join p.context as P_C_V"
        StringBuilder fromClause = new StringBuilder();
        for(Iterator<SQLFragment> fit = fragments.iterator(); fit.hasNext(); ) {
            fromClause.append(fit.next().buildFromClause());
            if(fit.hasNext()) {
                fromClause.append("\n LEFT JOIN \n  ");
            }
        }

        return fromClause;
    }


    private StringBuilder buildWhereClause()  {
        // build the where clause as follows:
        // 1. iterate over all patterns and for each resource and literal field in subject,
        //    property, object, or context, and set a query condition according to the
        //    nodes given in the pattern
        // 2. for each variable that has more than one occurrences, add a join condition
        // 3. for each variable in the initialBindings, add a condition to the where clause

        // list of where conditions that will later be connected by AND
        List<String> whereConditions = new LinkedList<String>();

        // 1. for the first pattern of the first fragment, we add the conditions to the WHERE clause

        for(SQLFragment fragment : fragments) {
            if(fragment.getConditionPosition() != SQLFragment.ConditionPosition.JOIN) {
                whereConditions.add(fragment.buildConditionClause());
            }
        }

        // 3. for each variable in the initialBindings, add a condition to the where clause setting it
        //    to the node given as binding
        if(bindings != null) {
            for(String v : bindings.getBindingNames()) {
                SQLVariable sv = variables.get(v);

                if(sv != null && !sv.getExpressions().isEmpty()) {
                    List<String> vNames = sv.getExpressions();
                    String vName = vNames.get(0);
                    Value binding = converter.convert(bindings.getValue(v));
                    if(binding instanceof KiWiNode) {
                        whereConditions.add(vName+" = "+((KiWiNode)binding).getId());
                    } else {
                        throw new IllegalStateException("the values in this binding have not been created by the KiWi value factory");
                    }

                }
            }
        }

        // construct the where clause
        StringBuilder whereClause = new StringBuilder();
        for(Iterator<String> it = whereConditions.iterator(); it.hasNext(); ) {
            String condition = it.next();
            if(condition.length() > 0) {
                whereClause.append(condition);
                whereClause.append("\n ");
                if (it.hasNext()) {
                    whereClause.append("AND ");
                }
            }
        }
        return whereClause;
    }

    private StringBuilder buildHavingClause()  {

        // list of where conditions that will later be connected by AND
        List<CharSequence> havingConditions = new LinkedList<CharSequence>();

        // 1. for the first pattern of the first fragment, we add the conditions to the WHERE clause

        for(SQLFragment fragment : fragments) {
            if(fragment.getConditionPosition() == SQLFragment.ConditionPosition.HAVING) {
                StringBuilder conditionClause = new StringBuilder();
                for(Iterator<String> cit = fragment.getConditions().iterator(); cit.hasNext(); ) {
                    if(conditionClause.length() > 0) {
                        conditionClause.append("\n       AND ");
                    }
                    conditionClause.append(cit.next());
                }

                havingConditions.add(conditionClause);
            }
        }

        // construct the having clause
        StringBuilder havingClause = new StringBuilder();
        for(Iterator<CharSequence> it = havingConditions.iterator(); it.hasNext(); ) {
            CharSequence condition = it.next();
            if(condition.length() > 0) {
                havingClause.append(condition);
                havingClause.append("\n ");
                if (it.hasNext()) {
                    havingClause.append("AND ");
                }
            }
        }
        return havingClause;
    }


    private StringBuilder buildOrderClause() {
        StringBuilder orderClause = new StringBuilder();
        if(orderby.size() > 0) {
            for(Iterator<OrderElem> it = orderby.iterator(); it.hasNext(); ) {
                OrderElem elem = it.next();
                orderClause.append(evaluateExpression(elem.getExpr(), ValueType.STRING));
                if(elem.isAscending()) {
                    orderClause.append(" ASC");
                } else {
                    orderClause.append(" DESC");
                }
                if(it.hasNext()) {
                    orderClause.append(", ");
                }
            }
            orderClause.append(" \n");
        }

        return orderClause;
    }

    private StringBuilder buildGroupClause() {
        StringBuilder groupClause = new StringBuilder();

        if(groupLabels.size() > 0) {
            for(Iterator<String> it = groupLabels.iterator(); it.hasNext(); ) {
                SQLVariable sv = variables.get(it.next());

                if(sv != null) {
                    Iterator<String> lit = sv.getExpressions().iterator();
                    while (lit.hasNext()) {
                        groupClause.append(lit.next());
                        if(lit.hasNext()) {
                            groupClause.append(", ");
                        }
                    }
                }

                if(it.hasNext()) {
                    groupClause.append(", ");
                }
            }

            if (orderby.size() > 0) {
                groupClause.append(", ");
                for(Iterator<OrderElem> it = orderby.iterator(); it.hasNext(); ) {
                    OrderElem elem = it.next();
                    groupClause.append(evaluateExpression(elem.getExpr(), ValueType.STRING));
                    if (it.hasNext()) {
                        groupClause.append(", ");
                    }
                }
            }

            groupClause.append(" \n");
        }

        return groupClause;
    }

    private StringBuilder buildLimitClause() {
        // construct limit and offset
        StringBuilder limitClause = new StringBuilder();
        if(limit > 0) {
            limitClause
                    .append("LIMIT ")
                    .append(limit)
                    .append(" ");
        }
        if(offset >= 0) {
            limitClause
                    .append("OFFSET ")
                    .append(offset)
                    .append(" ");
        }
        return limitClause;
    }

    private String evaluateExpression(ValueExpr expr, final ValueType optype) {
        return new ValueExpressionEvaluator(expr, this, optype).build();
    }

    protected ValueType getProjectionType(ValueExpr expr) {
        if(expr instanceof BNodeGenerator) {
            return ValueType.BNODE;
        } else if(expr instanceof IRIFunction) {
            return ValueType.URI;
        } else if(expr instanceof FunctionCall) {
            return functionRegistry.get(((FunctionCall) expr).getURI()).getReturnType();
        } else if(expr instanceof NAryValueOperator) {
            return getProjectionType(((NAryValueOperator) expr).getArguments().get(0));
        } else if(expr instanceof ValueConstant) {
            return ValueType.NODE;
            /*
            if (((ValueConstant) expr).getValue() instanceof URI) {
                return ProjectionType.URI;
            } else if (((ValueConstant) expr).getValue() instanceof Literal) {
                Literal l = (Literal) ((ValueConstant) expr).getValue();
                if (XSD.Integer.equals(l.getDatatype()) || XSD.Int.equals(l.getDatatype())) {
                    return ProjectionType.INT;
                } else if (XSD.Double.equals(l.getDatatype()) || XSD.Float.equals(l.getDatatype())) {
                    return ProjectionType.DOUBLE;
                } else {
                    return ProjectionType.STRING;
                }

            } else {
                return ProjectionType.STRING;
            }
            */
        } else if(expr instanceof Var) {
            return ValueType.NODE;
        } else if(expr instanceof MathExpr) {
            MathExpr cmp = (MathExpr) expr;

            return new OPTypeFinder(cmp).coerce();
        } else if(expr instanceof Count) {
            return ValueType.INT;
        } else if(expr instanceof Sum) {
            return ValueType.DOUBLE;
        } else if(expr instanceof Avg) {
            return ValueType.DOUBLE;
        } else if(expr instanceof Compare) {
            return ValueType.BOOL;
        } else if(expr instanceof If) {
            return getProjectionType(((If) expr).getResult());
        } else if(expr instanceof Exists) {
            return ValueType.BOOL;
        } else {
            return ValueType.STRING;
        }

    }

    private String getLiteralLangExpression(ValueExpr expr) {
        Var langVar = new LiteralTypeExpressionFinder(expr).expr;

        if(langVar != null) {
            SQLVariable sqlVar = variables.get(langVar.getName());
            if(sqlVar != null) {
                return sqlVar.getAlias() + ".lang";
            }
        }
        return null;

    }

    private String getLiteralTypeExpression(ValueExpr expr) {
        Var typeVar = new LiteralTypeExpressionFinder(expr).expr;

        if(typeVar != null) {
            SQLVariable sqlVar = variables.get(typeVar.getName());
            if(sqlVar != null) {
                return sqlVar.getAlias() + ".ltype";
            }
        }
        return null;
    }

    /**
     * Construct the SQL query for the given SPARQL query part.
     *
     * @return
     */
    public StringBuilder build()  {
        StringBuilder selectClause = buildSelectClause();
        StringBuilder fromClause   = buildFromClause();
        StringBuilder whereClause  = buildWhereClause();
        StringBuilder orderClause  = buildOrderClause();
        StringBuilder groupClause  = buildGroupClause();
        StringBuilder havingClause = buildHavingClause();
        StringBuilder limitClause  = buildLimitClause();

        final StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT ");

        if(selectClause.length() > 0) {
            queryString.append(selectClause).append("\n ");
        } else {
            queryString.append("* \n");
        }

        if(fromClause.length() > 0) {
            queryString.append("FROM ").append(fromClause).append("\n ");
        }

        if(whereClause.length() > 0) {
            queryString.append("WHERE ").append(whereClause).append("\n ");
        }

        if(groupClause.length() > 0) {
            queryString.append("GROUP BY ").append(groupClause).append("\n ");
        }

        if(havingClause.length() > 9) {
            queryString.append("HAVING ").append(havingClause).append("\n ");
        }

        if(orderClause.length() > 0) {
            queryString.append("ORDER BY ").append(orderClause).append("\n ");
        }

        queryString.append(limitClause);

        log.debug("original SPARQL syntax tree:\n {}", query);
        log.debug("constructed SQL query string:\n {}",queryString);
        log.debug("SPARQL -> SQL node variable mappings:\n {}", variables);
        log.debug("projected variables:\n {}", projectedVars);

        return queryString;
    }

}
