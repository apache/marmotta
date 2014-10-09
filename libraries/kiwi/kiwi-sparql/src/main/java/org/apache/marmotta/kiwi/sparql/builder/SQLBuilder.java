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

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.collections.CollectionUtils;
import org.apache.marmotta.commons.util.DateUtils;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.apache.marmotta.kiwi.sparql.exception.UnsatisfiableQueryException;
import org.apache.marmotta.kiwi.sparql.function.NativeFunction;
import org.apache.marmotta.kiwi.sparql.function.NativeFunctionRegistry;
import org.openrdf.model.*;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A builder for translating SPARQL queries into SQL.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLBuilder {

    private static Logger log = LoggerFactory.getLogger(SQLBuilder.class);

    /**
     * Simplify access to different node ids
     */
    private static final String[] positions = new String[] {"subject","predicate","object","context"};

    /**
     * Date format used for SQL timestamps.
     */
    private static final DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");


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
     * The triple patterns collected from the query.
     */
    private List<SQLFragment> fragments;


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


    public Set<String> getProjectedVars() {
        return projectedVars;
    }

    private void prepareBuilder()  throws UnsatisfiableQueryException {
        Preconditions.checkArgument(query instanceof Union || query instanceof Extension || query instanceof Order || query instanceof Group || query instanceof LeftJoin ||query instanceof Join || query instanceof Filter || query instanceof StatementPattern || query instanceof Distinct || query instanceof Slice || query instanceof Reduced);


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

        // find all variables occurring in the patterns and create a map to map them to
        // field names in the database query; each variable will have one or several field names,
        // one for each pattern it occurs in; field names are constructed automatically by a counter
        // and the pattern name to ensure the name is a valid HQL identifier
        int variableCount = 0;
        for(SQLFragment f : fragments) {
            for (SQLPattern p : f.getPatterns()) {
                // build pattern
                Var[] fields = p.getFields();
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i] != null && !fields[i].hasValue()) {
                        Var v = fields[i];

                        SQLVariable sv = variables.get(v.getName());
                        if(sv == null) {
                            sv = new SQLVariable("V" + (++variableCount), v.getName());

                            // select those variables that are really projected and not only needed in a grouping construct
                            if(projectedVars.contains(sv.getSparqlName()) || new SQLProjectionFinder(query,v.getName()).found) {
                                sv.setProjectionType(ProjectionType.NODE);
                            }

                            String pName = p.getName();
                            String vName = sv.getName();

                            if (new ConditionFinder(v.getName(), query).found) {
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

                        if (new ConditionFinder(sq_v.getSparqlName(), query).found) {
                            sv.setAlias(sqName + "_" + vName);
                        }

                        addVariable(sv);
                    }

                }
            }
        }



        // add all extensions to the variable list so they are properly considered in projections and clauses
        // TODO: order by variable dependency, or otherwise the evaluateExpression might fail
        for(ExtensionElem ext : extensions) {
            Var v = new Var(ext.getName());

            SQLVariable sv = variables.get(v.getName());
            if(!variables.containsKey(v.getName())) {
                sv = new SQLVariable("V" + (++variableCount), v.getName());

                // select those variables that are really projected and not only needed in a grouping construct
                if(projectedVars.contains(sv.getSparqlName()) || new SQLProjectionFinder(query,v.getName()).found) {
                    sv.setProjectionType(getProjectionType(ext.getExpr()));
                }

                addVariable(sv);
            }

            // TODO: ANY as OPType here is dangerous, because the OPType should depends on projection and actual use
            //       of variables in conditions etc
            if (new ConditionFinder(v.getName(), query).found) {
                //sv.getAliases().add(evaluateExpression(ext.getExpr(), OPTypes.VALUE));
                sv.getBindings().add(ext.getExpr());
            }

            sv.getExpressions().add(evaluateExpression(ext.getExpr(), OPTypes.ANY));

        }

        // calculate for each variable the SQL expressions representing them and any necessary JOIN conditions

        for(SQLFragment f : fragments) {
            for (SQLPattern p : f.getPatterns()) {
                // build pattern
                Var[] fields = p.getFields();
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i] != null && !fields[i].hasValue()) {
                        Var v = fields[i];

                        SQLVariable sv = variables.get(v.getName());

                        String pName = p.getName();

                        // if the variable has been used before, add a join condition to the first occurrence
                        if(sv.getExpressions().size() > 0) {
                            p.getConditions().add(sv.getExpressions().get(0) + " = " + pName + "." + positions[i]);
                        }

                        sv.getExpressions().add(pName + "." + positions[i]);
                    }
                }
            }

            // subqueries: look up which variables are bound in the subqueries and add proper aliases
            for(SQLAbstractSubquery sq : f.getSubqueries()) {
                for(SQLVariable sq_v : sq.getQueryVariables()) {
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
                f.getConditions().add(evaluateExpression(e, OPTypes.ANY));
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
        for(SQLFragment f : fragments) {
            if(first) {
                // the conditions of the first fragment need to be placed in the WHERE part of the query, because
                // there is not necessarily a JOIN ... ON where we can put it
                f.setConditionPosition(SQLFragment.ConditionPosition.WHERE);
                first = false;
            }

            for (SQLPattern p : f.getPatterns()) {
                for(Map.Entry<SQLPattern.TripleColumns, Var> fieldEntry : p.getTripleFields().entrySet()) {
                    if(fieldEntry.getValue() != null && !fieldEntry.getValue().hasValue() && new ConditionFinder(fieldEntry.getValue().getName(),query).found) {
                        p.setJoinField(fieldEntry.getKey(), variables.get(fieldEntry.getValue().getName()).getName());
                    }
                }
            }

            for(SQLAbstractSubquery sq : f.getSubqueries()) {
                for(SQLVariable sq_v : sq.getQueryVariables()) {
                    if(new ConditionFinder(sq_v.getSparqlName(),query).found && sq_v.getProjectionType() == ProjectionType.NODE) {
                        // this is needed in case we need to JOIN with the NODES table to retrieve values
                        SQLVariable sv = variables.get(sq_v.getSparqlName());  // fetch the name of the variable in the enclosing query
                        sq.getJoinFields().add(new VariableMapping(sv.getName(), sq_v.getName()));
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
            if(v.getProjectionType() != ProjectionType.NONE && (projectedVars.isEmpty() || projectedVars.contains(v.getSparqlName()))) {
                String projectedName = v.getName();
                String fromName = v.getExpressions().get(0);

                projections.add(fromName + " AS " + projectedName);
            }
        }

        // SQL enforces ORDER BY variables to occur in the select part in case distinct is set
        int counter = 0;
        if(distinct) {
            for(OrderElem e : orderby) {
                projections.add(evaluateExpression(e.getExpr(), OPTypes.VALUE) + " AS _OB" + (++counter));
            }
        }


        StringBuilder selectClause = new StringBuilder();

        if(distinct) {
            selectClause.append("DISTINCT ");
        }

        selectClause.append(CollectionUtils.fold(projections,", "));

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
            if(fragment.getConditionPosition() == SQLFragment.ConditionPosition.WHERE) {
                whereConditions.add(fragment.buildConditionClause());
            }
        }

        // 3. for each variable in the initialBindings, add a condition to the where clause setting it
        //    to the node given as binding
        if(bindings != null) {
            for(String v : bindings.getBindingNames()) {
                SQLVariable sv = variables.get(v);

                if(!sv.getExpressions().isEmpty()) {
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

    private StringBuilder buildOrderClause() {
        StringBuilder orderClause = new StringBuilder();
        if(orderby.size() > 0) {
            for(Iterator<OrderElem> it = orderby.iterator(); it.hasNext(); ) {
                OrderElem elem = it.next();
                orderClause.append(evaluateExpression(elem.getExpr(), OPTypes.VALUE));
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
            groupClause.append(" \n");
        }

        return groupClause;
    }


    private StringBuilder buildLimitClause() {
        // construct limit and offset
        StringBuilder limitClause = new StringBuilder();
        if(limit > 0) {
            limitClause.append("LIMIT ");
            limitClause.append(limit);
            limitClause.append(" ");
        }
        if(offset >= 0) {
            limitClause.append("OFFSET ");
            limitClause.append(offset);
            limitClause.append(" ");
        }
        return limitClause;
    }



    private String evaluateExpression(ValueExpr expr, final OPTypes optype) {
        if(expr instanceof And) {
            return "(" + evaluateExpression(((And) expr).getLeftArg(), optype) + " AND " + evaluateExpression(((And) expr).getRightArg(), optype) + ")";
        } else if(expr instanceof Or) {
            return "(" + evaluateExpression(((Or) expr).getLeftArg(), optype) + " OR " + evaluateExpression(((Or) expr).getRightArg(), optype) + ")";
        } else if(expr instanceof Not) {
            return "NOT (" + evaluateExpression(((Not) expr).getArg(), optype) + ")";
        } else if(expr instanceof Exists) {

            // TODO: need to make sure that variables of the parent are visible in the subquery
            //       - pattern names need to be unique even in subqueries
            //       - variable lookup for expressions in the subquery need to refer to the parent
            SQLBuilder sq_builder = new SQLBuilder(((Exists) expr).getSubQuery(), bindings, dataset, converter, dialect, "_", Collections.EMPTY_SET, copyVariables(variables));

            return "EXISTS (" + sq_builder.build() + ")";
        } else if(expr instanceof Str) {
            Str str = (Str)expr;

            // get value of argument and express it as string
            return evaluateExpression(str.getArg(), OPTypes.STRING);
        } else if(expr instanceof Label) {
            Label str = (Label)expr;

            // get value of argument and express it as string
            return evaluateExpression(str.getArg(), OPTypes.STRING);
        } else if(expr instanceof Lang) {
            Lang lang = (Lang)expr;

            if(lang.getArg() instanceof Var) {
                return variables.get(((Var) lang.getArg()).getName()).getAlias() + ".lang";
            }
        } else if(expr instanceof Compare) {
            Compare cmp = (Compare)expr;

            OPTypes ot = new OPTypeFinder(cmp).coerce();

            return evaluateExpression(cmp.getLeftArg(), ot) + getSQLOperator(cmp.getOperator()) + evaluateExpression(cmp.getRightArg(), ot);
        } else if(expr instanceof MathExpr) {
            MathExpr cmp = (MathExpr)expr;

            OPTypes ot = new OPTypeFinder(cmp).coerce();

            if(ot == OPTypes.STRING) {
                if(cmp.getOperator() == MathExpr.MathOp.PLUS) {
                    return NativeFunctionRegistry.getInstance().get(FN.CONCAT.stringValue()).getNative(dialect,evaluateExpression(cmp.getLeftArg(), ot), evaluateExpression(cmp.getRightArg(), ot));
                } else {
                    throw new IllegalArgumentException("operation "+cmp.getOperator()+" is not supported on strings");
                }
            } else {
                return evaluateExpression(cmp.getLeftArg(), ot) + getSQLOperator(cmp.getOperator()) + evaluateExpression(cmp.getRightArg(), ot);
            }
        } else if(expr instanceof Regex) {
            Regex re = (Regex)expr;

            return optimizeRegexp(evaluateExpression(re.getArg(), OPTypes.STRING), evaluateExpression(re.getPatternArg(), OPTypes.STRING), re.getFlagsArg());
        } else if(expr instanceof LangMatches) {
            LangMatches lm = (LangMatches)expr;
            String value = evaluateExpression(lm.getLeftArg(), optype);
            ValueConstant pattern = (ValueConstant) lm.getRightArg();

            if(pattern.getValue().stringValue().equals("*")) {
                return value + " LIKE '%'";
            } else if(pattern.getValue().stringValue().equals("")) {
                return value + " IS NULL";
            } else {
                return "(" + value + " = '"+pattern.getValue().stringValue().toLowerCase()+"' OR " + dialect.getILike(value, "'" + pattern.getValue().stringValue().toLowerCase() + "-%' )");
            }
        } else if(expr instanceof Bound) {
            ValueExpr arg = ((Bound)expr).getArg();

            if(arg instanceof ValueConstant) {
                return Boolean.toString(true);
            } else if(arg instanceof Var) {

                return "(" + evaluateExpression(arg, optype) + " IS NOT NULL)";
            }

        } else if(expr instanceof IsResource) {
            ValueExpr arg = ((UnaryValueOperator)expr).getArg();

            // operator must be a variable or a constant
            if(arg instanceof ValueConstant) {
                return Boolean.toString(((ValueConstant) arg).getValue() instanceof URI || ((ValueConstant) arg).getValue() instanceof BNode);
            } else if(arg instanceof Var) {
                String var = variables.get(((Var) arg).getName()).getAlias();

                return "(" + var + ".ntype = 'uri' OR " + var + ".ntype = 'bnode')";
            }
        } else if(expr instanceof IsURI) {
            ValueExpr arg = ((UnaryValueOperator)expr).getArg();

            // operator must be a variable or a constant
            if(arg instanceof ValueConstant) {
                return Boolean.toString(((ValueConstant) arg).getValue() instanceof URI);
            } else if(arg instanceof Var) {
                String var = variables.get(((Var) arg).getName()).getAlias();

                return var + ".ntype = 'uri'";
            }
        } else if(expr instanceof IsBNode) {
            ValueExpr arg = ((UnaryValueOperator)expr).getArg();

            // operator must be a variable or a constant
            if(arg instanceof ValueConstant) {
                return Boolean.toString(((ValueConstant) arg).getValue() instanceof BNode);
            } else if(arg instanceof Var) {
                String var = variables.get(((Var) arg).getName()).getAlias();

                return var + ".ntype = 'bnode'";
            }
        } else if(expr instanceof IsLiteral) {
            ValueExpr arg = ((UnaryValueOperator)expr).getArg();

            // operator must be a variable or a constant
            if(arg instanceof ValueConstant) {
                return Boolean.toString(((ValueConstant) arg).getValue() instanceof Literal);
            } else if(arg instanceof Var) {
                String var = variables.get(((Var) arg).getName()).getAlias();

                return "(" + var + ".ntype = 'string' OR " + var + ".ntype = 'int' OR " + var + ".ntype = 'double'  OR " + var + ".ntype = 'date'  OR " + var + ".ntype = 'boolean')";
            }
        } else if(expr instanceof Var) {
            // distinguish between the case where the variable is plain and the variable is bound
            SQLVariable sv = variables.get(((Var) expr).getName());

            if(sv.getBindings().size() > 0) {
                // in case the variable is actually an alias for an expression, we evaluate that expression instead, effectively replacing the
                // variable occurrence with its value
                return evaluateExpression(sv.getBindings().get(0),optype);
            } else {
                String var = sv.getAlias();

                if(sv.getProjectionType() != ProjectionType.NODE && sv.getProjectionType() != ProjectionType.NONE) {
                    // in case the variable represents a constructed or bound value instead of a node, we need to
                    // use the SQL expression as value; SQL should take care of proper casting...
                    // TODO: explicit casting needed?
                    return sv.getExpressions().get(0);
                } else {
                    // in case the variable represents an entry from the NODES table (i.e. has been bound to a node
                    // in the database, we take the NODES alias and resolve to the correct column according to the
                    // operator type
                    if (optype == null) {
                        return var + ".svalue";
                    } else {
                        switch (optype) {
                            case STRING:
                                return var + ".svalue";
                            case INT:
                                return var + ".ivalue";
                            case DOUBLE:
                                return var + ".dvalue";
                            case DATE:
                                return var + ".tvalue";
                            case VALUE:
                                return var + ".svalue";
                            case URI:
                                return var + ".svalue";
                            case ANY:
                                return var + ".id";
                        }
                    }
                }
            }
        } else if(expr instanceof ValueConstant) {
            String val = ((ValueConstant) expr).getValue().stringValue();

            if(optype == null) {
                return "'" + val + "'";
            } else {
                switch (optype) {
                    case STRING: return "'" + val + "'";
                    case VALUE:  return "'" + val + "'";
                    case URI:    return "'" + val + "'";
                    case INT:    return ""  + Integer.parseInt(val);
                    case DOUBLE: return ""  + Double.parseDouble(val);
                    case DATE:   return "'" + sqlDateFormat.format(DateUtils.parseDate(val)) + "'";

                    // in this case we should return a node ID and also need to make sure it actually exists
                    case ANY:
                        KiWiNode n = converter.convert(((ValueConstant) expr).getValue());
                        return "" + n.getId();

                    default: throw new IllegalArgumentException("unsupported value type: " + optype);
                }
            }
        } else if(expr instanceof Coalesce) {
            return "COALESCE(" + CollectionUtils.fold(((Coalesce) expr).getArguments(), new CollectionUtils.StringSerializer<ValueExpr>() {
                @Override
                public String serialize(ValueExpr valueExpr) {
                    return evaluateExpression(valueExpr, optype);
                }
            },", ") + ")";
        } else if(expr instanceof Count) {
            StringBuilder countExp = new StringBuilder();
            countExp.append("COUNT(");

            if(((Count) expr).isDistinct()) {
                countExp.append("DISTINCT ");
            }

            if(((Count) expr).getArg() == null) {
                // this is a weird special case where we need to expand to all variables selected in the query wrapped
                // by the group; we cannot simply use "*" because the concept of variables is a different one in SQL,
                // so instead we construct an ARRAY of the bindings of all variables

                List<String> countVariables = new ArrayList<>();
                for(SQLVariable v : variables.values()) {
                    if(v.getProjectionType() == ProjectionType.NONE) {
                        //countVariables.add(v.getExpressions().get(0));
                        countVariables.add(v.getAlias());
                    }
                }
                countExp.append("ARRAY[");
                countExp.append(CollectionUtils.fold(countVariables,","));
                countExp.append("]");

            } else {
                countExp.append(evaluateExpression(((Count) expr).getArg(), OPTypes.ANY));
            }
            countExp.append(")");

            return countExp.toString();
        } else if(expr instanceof Avg) {
            return "AVG(" + evaluateExpression(((Avg) expr).getArg(), OPTypes.DOUBLE) + ")";
        } else if(expr instanceof Min) {
            return "MIN(" + evaluateExpression(((Min) expr).getArg(), OPTypes.DOUBLE) + ")";
        } else if(expr instanceof Max) {
            return "MAX(" + evaluateExpression(((Max) expr).getArg(), OPTypes.DOUBLE) + ")";
        } else if(expr instanceof Sum) {
            return "SUM(" + evaluateExpression(((Sum) expr).getArg(), OPTypes.DOUBLE) + ")";
        } else if(expr instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall)expr;

            // special optimizations for frequent cases with variables
            if((XMLSchema.DOUBLE.toString().equals(fc.getURI()) || XMLSchema.FLOAT.toString().equals(fc.getURI()) ) &&
                    fc.getArgs().size() == 1) {
                return evaluateExpression(fc.getArgs().get(0), OPTypes.DOUBLE);
            } else if(XMLSchema.INTEGER.toString().equals(fc.getURI()) && fc.getArgs().size() == 1) {
                return evaluateExpression(fc.getArgs().get(0), OPTypes.INT);
            } else if(XMLSchema.BOOLEAN.toString().equals(fc.getURI()) && fc.getArgs().size() == 1) {
                return evaluateExpression(fc.getArgs().get(0), OPTypes.BOOL);
            } else if(XMLSchema.DATE.toString().equals(fc.getURI()) && fc.getArgs().size() == 1) {
                return evaluateExpression(fc.getArgs().get(0), OPTypes.DATE);
            }

            String fnUri = fc.getURI();

            String[] args = new String[fc.getArgs().size()];

            NativeFunction nf = functionRegistry.get(fnUri);

            if(nf != null && nf.isSupported(dialect)) {
                OPTypes fOpType = nf.getArgumentType(0);

                for (int i = 0; i < args.length; i++) {
                    args[i] = evaluateExpression(fc.getArgs().get(i), fOpType);
                }

                if (optype != null && optype != nf.getReturnType()) {
                    return castExpression(nf.getNative(dialect, args), optype);
                } else {
                    return nf.getNative(dialect, args);
                }
            } else {
                throw new IllegalArgumentException("the function "+fnUri+" is not supported by the SQL translation");
            }
        }


        throw new IllegalArgumentException("unsupported value expression: "+expr);
    }

    /**
     * Copy variables from the set to a new set suitable for a subquery; this allows passing over variable expressions
     * from parent queries to subqueries without the subquery adding expressions that are then not visible outside
     * @param variables
     * @return
     */
    private Map<String, SQLVariable> copyVariables(Map<String, SQLVariable> variables) {
        Map<String,SQLVariable> copy = new HashMap<>();
        try {
            for(Map.Entry<String,SQLVariable> entry : variables.entrySet()) {
                    copy.put(entry.getKey(), (SQLVariable) entry.getValue().clone());
            }
        } catch (CloneNotSupportedException e) {
            log.error("could not clone SQL variable:",e);
        }

        return copy;
    }

    private String castExpression(String arg, OPTypes type) {
        if(type == null) {
            return arg;
        }

        switch (type) {
            case DOUBLE:
                return functionRegistry.get(XMLSchema.DOUBLE).getNative(dialect, arg);
            case INT:
                return functionRegistry.get(XMLSchema.INTEGER).getNative(dialect, arg);
            case BOOL:
                return functionRegistry.get(XMLSchema.BOOLEAN).getNative(dialect, arg);
            case DATE:
                return functionRegistry.get(XMLSchema.DATETIME).getNative(dialect, arg);
            case VALUE:
                return arg;
            case ANY:
                return arg;
            default:
                return arg;
        }
    }

    private String getSQLOperator(Compare.CompareOp op) {
        switch (op) {
            case EQ: return " = ";
            case GE: return " >= ";
            case GT: return " > ";
            case LE: return " <= ";
            case LT: return " < ";
            case NE: return " <> ";
        }
        throw new IllegalArgumentException("unsupported operator type for comparison: "+op);
    }


    private String getSQLOperator(MathExpr.MathOp op) {
        switch (op) {
            case PLUS: return " + ";
            case MINUS: return " - ";
            case DIVIDE: return " / ";
            case MULTIPLY: return " / ";
        }
        throw new IllegalArgumentException("unsupported operator type for math expression: "+op);
    }


    /**
     * Test if the regular expression given in the pattern can be simplified to a LIKE SQL statement; these are
     * considerably more efficient to evaluate in most databases, so in case we can simplify, we return a LIKE.
     *
     * @param value
     * @param pattern
     * @return
     */
    private String optimizeRegexp(String value, String pattern, ValueExpr flags) {
        String _flags = flags != null && flags instanceof ValueConstant ? ((ValueConstant)flags).getValue().stringValue() : null;

        String simplified = pattern;

        // apply simplifications

        // remove SQL quotes at beginning and end
        simplified = simplified.replaceFirst("^'","");
        simplified = simplified.replaceFirst("'$","");


        // remove .* at beginning and end, they are the default anyways
        simplified = simplified.replaceFirst("^\\.\\*","");
        simplified = simplified.replaceFirst("\\.\\*$","");

        // replace all occurrences of % with \% and _ with \_, as they are special characters in SQL
        simplified = simplified.replaceAll("%","\\%");
        simplified = simplified.replaceAll("_","\\_");

        // if pattern now does not start with a ^, we put a "%" in front
        if(!simplified.startsWith("^")) {
            simplified = "%" + simplified;
        } else {
            simplified = simplified.substring(1);
        }

        // if pattern does not end with a "$", we put a "%" at the end
        if(!simplified.endsWith("$")) {
            simplified = simplified + "%";
        } else {
            simplified = simplified.substring(0,simplified.length()-1);
        }

        // replace all non-escaped occurrences of .* with %
        simplified = simplified.replaceAll("(?<!\\\\)\\.\\*","%");

        // replace all non-escaped occurrences of .+ with _%
        simplified = simplified.replaceAll("(?<!\\\\)\\.\\+","_%");

        // the pattern is not simplifiable if the simplification still contains unescaped regular expression constructs
        Pattern notSimplifiable = Pattern.compile("(?<!\\\\)[\\.\\*\\+\\{\\}\\[\\]\\|]");

        if(notSimplifiable.matcher(simplified).find()) {
            return dialect.getRegexp(value, pattern, _flags);
        } else {
            if(!simplified.startsWith("%") && !simplified.endsWith("%")) {
                if(StringUtils.containsIgnoreCase(_flags, "i")) {
                    return String.format("lower(%s) = lower('%s')", value, simplified);
                } else {
                    return String.format("%s = '%s'", value, simplified);
                }
            } else {
                if(StringUtils.containsIgnoreCase(_flags,"i")) {
                    return dialect.getILike(value, "'" + simplified + "'");
                } else {
                    return value + " LIKE '"+simplified+"'";
                }
            }
        }

    }


    protected ProjectionType getProjectionType(ValueExpr expr) {
        if(expr instanceof FunctionCall) {
            return opTypeToProjection(functionRegistry.get(((FunctionCall) expr).getURI()).getReturnType());
        } else if(expr instanceof NAryValueOperator) {
            return getProjectionType(((NAryValueOperator) expr).getArguments().get(0));
        } else if(expr instanceof ValueConstant) {
            return ProjectionType.NODE;
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
            return ProjectionType.NODE;
        } else if(expr instanceof MathExpr) {
            MathExpr cmp = (MathExpr) expr;

            return opTypeToProjection(new OPTypeFinder(cmp).coerce());
        } else if(expr instanceof Count) {
            return ProjectionType.INT;
        } else if(expr instanceof Sum) {
            return ProjectionType.DOUBLE;
        } else if(expr instanceof Avg) {
            return ProjectionType.DOUBLE;
        } else {
            return ProjectionType.STRING;
        }

    }

    private ProjectionType opTypeToProjection(OPTypes t) {
        switch (t) {
            case ANY:
                return ProjectionType.NODE;
            case URI:
                return ProjectionType.URI;
            case DOUBLE:
                return ProjectionType.DOUBLE;
            case INT:
                return ProjectionType.INT;
            case DATE:
                return ProjectionType.DATE;
            case STRING:
                return ProjectionType.STRING;
            default:
                log.warn("optype {} cannot be projected!",t);
                return ProjectionType.STRING;
        }
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
        StringBuilder limitClause  = buildLimitClause();


        StringBuilder queryString = new StringBuilder();
        queryString
                .append("SELECT ").append(selectClause).append("\n ")
                .append("FROM ").append(fromClause).append("\n ");

        if(whereClause.length() > 0) {
            queryString.append("WHERE ").append(whereClause).append("\n ");
        }

        if(groupClause.length() > 0) {
            queryString.append("GROUP BY ").append(groupClause).append("\n ");
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
