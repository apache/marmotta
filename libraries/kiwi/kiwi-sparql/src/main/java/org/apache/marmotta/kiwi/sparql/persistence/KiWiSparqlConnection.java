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
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.Iterations;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.util.DateUtils;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.util.ResultSetIteration;
import org.apache.marmotta.kiwi.persistence.util.ResultTransformerFunction;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.openrdf.model.*;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.MapBindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

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

    private static DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    private KiWiConnection parent;
    private KiWiValueFactory valueFactory;

    private ExecutorService executorService;

    public KiWiSparqlConnection(KiWiConnection parent, KiWiValueFactory valueFactory) throws SQLException {
        this.parent = parent;
        this.valueFactory = valueFactory;

        // interruptible queries run in a separate thread
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Evaluate a statement pattern join or filter on the database by translating it into an appropriate SQL statement.
     * Copied and adapted from KiWiReasoningConnection.query()
     *
     * @param join
     * @param dataset
     * @return
     */
    public CloseableIteration<BindingSet, SQLException> evaluateJoin(TupleExpr join, final BindingSet bindings, final Dataset dataset) throws SQLException, InterruptedException {
        Preconditions.checkArgument(join instanceof Join || join instanceof Filter || join instanceof StatementPattern || join instanceof Distinct || join instanceof Slice || join instanceof Reduced);

        // some definitions
        String[] positions = new String[] {"subject","predicate","object","context"};

        // collect all patterns in a list, using depth-first search over the join
        List<StatementPattern> patterns = new PatternCollector(join).patterns;

        long offset = new LimitFinder(join).offset;
        long limit  = new LimitFinder(join).limit;

        boolean distinct = new DistinctFinder(join).distinct;

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
        final Map<Var,String> variableNames = new HashMap<>();

        // a map for mapping variables to field names; each variable might have one or more field names,
        // depending on the number of patterns it occurs in; will look like
        // { ?x -> ["P1_V1", "P2_V1"], ?y -> ["P2_V2"], ... }
        Map<Var,List<String>> queryVariables = new HashMap<>();
        Map<Var,List<String>> queryVariableIds = new HashMap<>();

        // a map for defining alternative context values for each variable used in the context part of a pattern
        Map<StatementPattern,List<Resource>> variableContexts = new HashMap<>();

        for(StatementPattern p : patterns) {
            // check graph restrictions in datasets (MARMOTTA-340)
            Resource[] contexts;
            Value contextValue = p.getContextVar() != null ? p.getContextVar().getValue() : null;

            Set<URI> graphs = null;
            boolean emptyGraph = false;

            if (dataset != null) {
                if (p.getScope() == StatementPattern.Scope.DEFAULT_CONTEXTS) {
                    graphs = dataset.getDefaultGraphs();
                    emptyGraph = graphs.isEmpty() && !dataset.getNamedGraphs().isEmpty();
                }
                else {
                    graphs = dataset.getNamedGraphs();
                    emptyGraph = graphs.isEmpty() && !dataset.getDefaultGraphs().isEmpty();
                }
            }

            if (emptyGraph) {
                // Search zero contexts
                return new EmptyIteration<BindingSet, SQLException>();
            } else if (graphs == null || graphs.isEmpty()) {
                if (contextValue != null) {
                    contexts = new Resource[] { (Resource)contextValue };
                } else {
                    contexts = new Resource[0];
                }
            } else if (contextValue != null) {
                if (graphs.contains(contextValue)) {
                    contexts = new Resource[] { (Resource)contextValue };
                } else {
                    // Statement pattern specifies a context that is not part of
                    // the dataset
                    return new EmptyIteration<BindingSet, SQLException>();
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


            // build pattern
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
                        queryVariableIds.put(v, new LinkedList<String>());
                    }
                    String pName = patternNames.get(p);
                    String vName = variableNames.get(v);
                    if(hasNodeCondition(fields[i], join)) {
                        queryVariables.get(v).add(pName + "_" + positions[i] + "_" + vName);
                    }
                    queryVariableIds.get(v).add(pName + "." + positions[i]);
                }
            }

            // build an OR query for the value of the context variable
            if(contexts.length > 0) {
                variableContexts.put(p, Arrays.asList(contexts));
            }
        }

        // build the select clause by projecting for each query variable the first name
        StringBuilder selectClause = new StringBuilder();

        if(distinct) {
            selectClause.append("DISTINCT ");
        }

        final List<Var> selectVariables = new LinkedList<Var>();
        for(Iterator<Var> it = queryVariableIds.keySet().iterator(); it.hasNext(); ) {
            Var v = it.next();
            String projectedName = variableNames.get(v);
            String fromName = queryVariableIds.get(v).get(0);
            selectClause.append(fromName);
            selectClause.append(" as ");
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
                if(fields[i] != null && !fields[i].hasValue() && hasNodeCondition(fields[i], join)) {
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
                long nodeId = -1;
                if(fields[i] != null && fields[i].hasValue()) {
                    Value v = valueFactory.convert(fields[i].getValue());
                    if(v instanceof KiWiNode) {
                        nodeId = ((KiWiNode) v).getId();
                    } else {
                        throw new IllegalArgumentException("the values in this query have not been created by the KiWi value factory");
                    }

                    if(nodeId >= 0) {
                        String condition = pName+"."+positions[i]+" = " + nodeId;
                        whereConditions.add(condition);
                    }
                }
            }
        }

        // 2. for each variable that has more than one occurrences, add a join condition
        for(Var v : queryVariableIds.keySet()) {
            List<String> vNames = queryVariableIds.get(v);
            for(int i = 1; i < vNames.size(); i++) {
                String vName1 = vNames.get(i-1);
                String vName2 = vNames.get(i);
                whereConditions.add(vName1 + " = " + vName2);
            }
        }

        // 3. for each variable in the initialBindings, add a condition to the where clause setting it
        //    to the node given as binding
        if(bindings != null) {
            for(String v : bindings.getBindingNames()) {
                for(Map.Entry<Var,List<String>> entry : queryVariableIds.entrySet()) {
                    if(entry.getKey().getName() != null && entry.getKey().getName().equals(v) &&
                            entry.getValue() != null && entry.getValue().size() > 0) {
                        List<String> vNames = entry.getValue();
                        String vName = vNames.get(0);
                        Value binding = valueFactory.convert(bindings.getValue(v));
                        if(binding instanceof KiWiNode) {
                            whereConditions.add(vName+" = "+((KiWiNode)binding).getId());
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


        // 5. for each filter condition, add a statement to the where clause
        List<ValueExpr> filters = new FilterCollector(join).filters;
        for(ValueExpr expr : filters) {
            whereConditions.add(evaluateExpression(expr,queryVariables, null));
        }


        // 6. for each context variable with a restricted list of contexts, we add a condition to the where clause
        //    of the form (V.id = R1.id OR V.id = R2.id ...)
        for(Map.Entry<StatementPattern,List<Resource>> vctx : variableContexts.entrySet()) {
            // the variable
            String varName = patternNames.get(vctx.getKey());

            // the string we are building
            StringBuilder cCond = new StringBuilder();
            cCond.append("(");
            for(Iterator<Resource> it = vctx.getValue().iterator(); it.hasNext(); ) {
                Value v = valueFactory.convert(it.next());
                if(v instanceof KiWiNode) {
                    long nodeId = ((KiWiNode) v).getId();

                    cCond.append(varName);
                    cCond.append(".context = ");
                    cCond.append(nodeId);

                    if(it.hasNext()) {
                        cCond.append(" OR ");
                    }
                } else {
                    throw new IllegalArgumentException("the values in this query have not been created by the KiWi value factory");
                }

            }
            cCond.append(")");
            whereConditions.add(cCond.toString());
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


        // build the query string
        String queryString =
                "SELECT " + selectClause + "\n " +
                        "FROM " + fromClause + "\n " +
                        "WHERE " + whereClause + "\n " +
                        limitClause;

        log.debug("original SPARQL syntax tree:\n {}", join);
        log.debug("constructed SQL query string:\n {}",queryString);
        log.debug("SPARQL -> SQL node variable mappings:\n {}", queryVariables);
        log.debug("SPARQL -> SQL ID variable mappings:\n {}", queryVariableIds);

        final PreparedStatement queryStatement = parent.getJDBCConnection().prepareStatement(queryString);
        if(parent.getDialect().isCursorSupported()) {
            queryStatement.setFetchSize(parent.getConfiguration().getCursorSize());
        }

        Future<ResultSet> queryFuture =
                executorService.submit(new Callable<ResultSet>() {
                    @Override
                    public ResultSet call() throws Exception {
                        try {
                            return queryStatement.executeQuery();
                        } catch (SQLException ex) {
                            if(Thread.interrupted()) {
                                log.info("SQL query execution cancelled; not returning result (Thread={})", Thread.currentThread());
                                throw new InterruptedException("SPARQL query execution cancelled");
                            } else {
                                throw ex;
                            }
                        }
                    }
                }
                );

        try {
            ResultSet result = queryFuture.get();

            ResultSetIteration<BindingSet> it = new ResultSetIteration<BindingSet>(result, true, new ResultTransformerFunction<BindingSet>() {
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

            // materialize result to avoid having more than one result set open at the same time
            return new CloseableIteratorIteration<BindingSet, SQLException>(Iterations.asList(it).iterator());
        } catch (InterruptedException | CancellationException e) {
            log.info("SPARQL query execution cancelled");
            queryFuture.cancel(true);
            queryStatement.cancel();
            queryStatement.close();

            throw new InterruptedException("SPARQL query execution cancelled");
        } catch (ExecutionException e) {
            log.error("error executing SPARQL query",e.getCause());
            if(e.getCause() instanceof SQLException) {
                throw (SQLException)e.getCause();
            } else if(e.getCause() instanceof InterruptedException) {
                throw (InterruptedException)e.getCause();
            } else {
                throw new SQLException("error executing SPARQL query",e);
            }
        }
    }

    private String evaluateExpression(ValueExpr expr, Map<Var, List<String>> queryVariables, OPTypes optype) {
        if(expr instanceof And) {
            return "(" + evaluateExpression(((And) expr).getLeftArg(), queryVariables, optype) + " AND " + evaluateExpression(((And) expr).getRightArg(),queryVariables, optype) + ")";
        } else if(expr instanceof Or) {
            return "(" + evaluateExpression(((Or) expr).getLeftArg(), queryVariables, optype) + " OR " + evaluateExpression(((Or) expr).getRightArg(),queryVariables, optype) + ")";
        } else if(expr instanceof Not) {
            return "NOT (" + evaluateExpression(((Not) expr).getArg(), queryVariables, optype)  + ")";
        } else if(expr instanceof Str) {
            Str str = (Str)expr;

            // get value of argument and express it as string
            return evaluateExpression(str.getArg(), queryVariables, OPTypes.STRING);
        } else if(expr instanceof Label) {
            Label str = (Label)expr;

            // get value of argument and express it as string
            return evaluateExpression(str.getArg(), queryVariables, OPTypes.STRING);
        } else if(expr instanceof Lang) {
            Lang lang = (Lang)expr;

            if(lang.getArg() instanceof Var) {
                return queryVariables.get(lang.getArg()).get(0) + ".lang";
            }
        } else if(expr instanceof Compare) {
            Compare cmp = (Compare)expr;

            OPTypes ot = determineOpType(cmp.getLeftArg(), cmp.getRightArg());

            return evaluateExpression(cmp.getLeftArg(),queryVariables, ot) + getSQLOperator(cmp.getOperator()) + evaluateExpression(cmp.getRightArg(),queryVariables, ot);
        } else if(expr instanceof MathExpr) {
            MathExpr cmp = (MathExpr)expr;

            OPTypes ot = determineOpType(cmp.getLeftArg(), cmp.getRightArg());

            if(ot == OPTypes.STRING) {
                if(cmp.getOperator() == MathExpr.MathOp.PLUS) {
                    return parent.getDialect().getConcat(evaluateExpression(cmp.getLeftArg(),queryVariables, ot), evaluateExpression(cmp.getRightArg(),queryVariables, ot));
                } else {
                    throw new IllegalArgumentException("operation "+cmp.getOperator()+" is not supported on strings");
                }
            } else {
                return evaluateExpression(cmp.getLeftArg(),queryVariables, ot) + getSQLOperator(cmp.getOperator()) + evaluateExpression(cmp.getRightArg(),queryVariables, ot);
            }
        } else if(expr instanceof Regex) {
            Regex re = (Regex)expr;

            return optimizeRegexp(evaluateExpression(re.getArg(),queryVariables, optype), evaluateExpression(re.getPatternArg(), queryVariables, OPTypes.STRING), re.getFlagsArg());
        } else if(expr instanceof LangMatches) {
            LangMatches lm = (LangMatches)expr;
            String value = evaluateExpression(lm.getLeftArg(), queryVariables, optype);
            ValueConstant pattern = (ValueConstant) lm.getRightArg();

            if(pattern.getValue().stringValue().equals("*")) {
                return value + " LIKE '%'";
            } else if(pattern.getValue().stringValue().equals("")) {
                return value + " IS NULL";
            } else {
                return "(" + value + " = '"+pattern.getValue().stringValue()+"' OR " + parent.getDialect().getILike(value, "'" + pattern.getValue().stringValue() + "-%' )");
            }
        } else if(expr instanceof IsResource) {
            ValueExpr arg = ((UnaryValueOperator)expr).getArg();

            // operator must be a variable or a constant
            if(arg instanceof ValueConstant) {
                return Boolean.toString(((ValueConstant) arg).getValue() instanceof URI || ((ValueConstant) arg).getValue() instanceof BNode);
            } else if(arg instanceof Var) {
                String var = queryVariables.get(arg).get(0);

                return "(" + var + ".ntype = 'uri' OR " + var + ".ntype = 'bnode')";
            }
        } else if(expr instanceof IsURI) {
            ValueExpr arg = ((UnaryValueOperator)expr).getArg();

            // operator must be a variable or a constant
            if(arg instanceof ValueConstant) {
                return Boolean.toString(((ValueConstant) arg).getValue() instanceof URI);
            } else if(arg instanceof Var) {
                String var = queryVariables.get(arg).get(0);

                return var + ".ntype = 'uri'";
            }
        } else if(expr instanceof IsBNode) {
            ValueExpr arg = ((UnaryValueOperator)expr).getArg();

            // operator must be a variable or a constant
            if(arg instanceof ValueConstant) {
                return Boolean.toString(((ValueConstant) arg).getValue() instanceof BNode);
            } else if(arg instanceof Var) {
                String var = queryVariables.get(arg).get(0);

                return var + ".ntype = 'bnode'";
            }
        } else if(expr instanceof IsLiteral) {
            ValueExpr arg = ((UnaryValueOperator)expr).getArg();

            // operator must be a variable or a constant
            if(arg instanceof ValueConstant) {
                return Boolean.toString(((ValueConstant) arg).getValue() instanceof Literal);
            } else if(arg instanceof Var) {
                String var = queryVariables.get(arg).get(0);

                return "(" + var + ".ntype = 'string' OR " + var + ".ntype = 'int' OR " + var + ".ntype = 'double'  OR " + var + ".ntype = 'date'  OR " + var + ".ntype = 'boolean')";
            }
        } else if(expr instanceof Var) {
            String var = queryVariables.get(expr).get(0);

            if(optype == null) {
                return var + ".svalue";
            } else {
                switch (optype) {
                    case STRING: return var + ".svalue";
                    case INT:    return var + ".ivalue";
                    case DOUBLE: return var + ".dvalue";
                    case DATE:   return var + ".tvalue";
                    case ANY:    return var + ".id";
                }
            }
        } else if(expr instanceof ValueConstant) {
            String val = ((ValueConstant) expr).getValue().stringValue();

            if(optype == null) {
                return "'" + val + "'";
            } else {
                switch (optype) {
                    case STRING: return "'" + val + "'";
                    case INT:    return ""  + Integer.parseInt(val);
                    case DOUBLE: return ""  + Double.parseDouble(val);
                    case DATE:   return "'" + sqlDateFormat.format(DateUtils.parseDate(val)) + "'";
                    default: throw new IllegalArgumentException("unsupported value type: " + optype);
                }
            }
        } else if(expr instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall)expr;

            // special optimizations for frequent cases with variables
            if((XMLSchema.DOUBLE.toString().equals(fc.getURI()) || XMLSchema.FLOAT.toString().equals(fc.getURI()) ) &&
               fc.getArgs().size() == 1) {
                return evaluateExpression(fc.getArgs().get(0), queryVariables, OPTypes.DOUBLE);
            } else if(XMLSchema.INTEGER.toString().equals(fc.getURI()) && fc.getArgs().size() == 1) {
                return evaluateExpression(fc.getArgs().get(0), queryVariables, OPTypes.INT);
            } else if(XMLSchema.BOOLEAN.toString().equals(fc.getURI()) && fc.getArgs().size() == 1) {
                return evaluateExpression(fc.getArgs().get(0), queryVariables, OPTypes.BOOL);
            } else if(XMLSchema.DATE.toString().equals(fc.getURI()) && fc.getArgs().size() == 1) {
                return evaluateExpression(fc.getArgs().get(0), queryVariables, OPTypes.DATE);
            }

            URI fnUri = new URIImpl(fc.getURI());

            String[] args = new String[fc.getArgs().size()];

            OPTypes fOpType = functionParameterTypes.get(fnUri);
            if(fOpType == null) {
                fOpType = OPTypes.STRING;
            }

            for(int i=0; i<args.length;i++) {
                args[i] = evaluateExpression(fc.getArgs().get(i),queryVariables,fOpType);
            }

            if(optype != null && optype != functionReturnTypes.get(fnUri)) {
                return castExpression(parent.getDialect().getFunction(fnUri,args), optype);
            } else {
                return parent.getDialect().getFunction(fnUri,args);
            }
        }


        throw new IllegalArgumentException("unsupported value expression: "+expr);
    }

    private String castExpression(String arg, OPTypes type) {
        if(type == null) {
            return arg;
        }

        switch (type) {
            case DOUBLE:
                return parent.getDialect().getFunction(XMLSchema.DOUBLE, arg);
            case INT:
                return parent.getDialect().getFunction(XMLSchema.INTEGER, arg);
            case BOOL:
                return parent.getDialect().getFunction(XMLSchema.BOOLEAN, arg);
            case DATE:
                return parent.getDialect().getFunction(XMLSchema.DATETIME, arg);
            case ANY:
                return arg;
            default:
                return arg;
        }
    }

    /**
     * Check if a variable selecting a node actually has any attached condition; if not return false. This is used to
     * decide whether joining with the node itself is necessary.
     * @param v
     * @param expr
     * @return
     */
    private boolean hasNodeCondition(Var v, TupleExpr expr) {
        if(expr instanceof Filter) {
            return hasNodeCondition(v, ((UnaryTupleOperator) expr).getArg()) || hasNodeCondition(v,  ((Filter) expr).getCondition());
        } else if(expr instanceof UnaryTupleOperator) {
            return hasNodeCondition(v, ((UnaryTupleOperator) expr).getArg());
        } else if(expr instanceof BinaryTupleOperator) {
            return hasNodeCondition(v, ((BinaryTupleOperator) expr).getLeftArg()) || hasNodeCondition(v, ((BinaryTupleOperator) expr).getRightArg());
        } else {
            return false;
        }

    }

    private boolean hasNodeCondition(Var v, ValueExpr expr) {
        if(expr instanceof Var) {
            return v.equals(expr);
        } else if(expr instanceof UnaryValueOperator) {
            return hasNodeCondition(v, ((UnaryValueOperator) expr).getArg());
        } else if(expr instanceof BinaryValueOperator) {
            return hasNodeCondition(v, ((BinaryValueOperator) expr).getLeftArg()) || hasNodeCondition(v, ((BinaryValueOperator) expr).getRightArg());
        } else if(expr instanceof NAryValueOperator) {
            for(ValueExpr e : ((NAryValueOperator) expr).getArguments()) {
                if(hasNodeCondition(v,e)) {
                    return true;
                }
            }
        } else if(expr instanceof FunctionCall) {
            for(ValueExpr e : ((FunctionCall) expr).getArgs()) {
                if(hasNodeCondition(v,e)) {
                    return true;
                }
            }
        }
        return false;
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



    private OPTypes determineOpType(ValueExpr expr) {
        if(expr instanceof ValueConstant) {
            if(((ValueConstant) expr).getValue() instanceof Literal) {
                Literal l = (Literal)((ValueConstant) expr).getValue();
                String type = l.getDatatype() != null ? l.getDatatype().stringValue() : null;

                if(StringUtils.equals(Namespaces.NS_XSD + "double", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "float", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "decimal", type)) {
                    return OPTypes.DOUBLE;
                } else if(StringUtils.equals(Namespaces.NS_XSD + "integer", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "long", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "int", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "short", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "nonNegativeInteger", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "nonPositiveInteger", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "negativeInteger", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "positiveInteger", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "unsignedLong", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "unsignedShort", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "byte", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "unsignedByte", type)) {
                    return OPTypes.INT;
                } else if(StringUtils.equals(Namespaces.NS_XSD + "dateTime", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "date", type)
                        || StringUtils.equals(Namespaces.NS_XSD + "time", type)) {
                    return OPTypes.DATE;
                } else {
                    return OPTypes.STRING;
                }
            } else {
                return OPTypes.STRING;
            }
        } else if(expr instanceof Str) {
            return OPTypes.STRING;
        } else if(expr instanceof Lang) {
            return OPTypes.STRING;
        } else if(expr instanceof LocalName) {
            return OPTypes.STRING;
        } else if(expr instanceof Label) {
            return OPTypes.STRING;
        } else if(expr instanceof MathExpr) {
            return determineOpType(((MathExpr) expr).getLeftArg(), ((MathExpr) expr).getRightArg());
        } else if(expr instanceof Var) {
            return OPTypes.ANY;
        } else if(expr instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall)expr;
            URI fnUri = new URIImpl(fc.getURI());

            String[] args = new String[fc.getArgs().size()];

            OPTypes fOpType = functionReturnTypes.get(fnUri);
            if(fOpType == null) {
                fOpType = OPTypes.ANY;
            }
            return fOpType;
        } else {
            throw new IllegalArgumentException("unsupported expression: "+expr);
        }
    }

    private OPTypes determineOpType(ValueExpr expr1, ValueExpr expr2) {
        OPTypes left  = determineOpType(expr1);
        OPTypes right = determineOpType(expr2);

        if(left == OPTypes.ANY) {
            return right;
        } else if(right == OPTypes.ANY) {
            return left;
        } else if(left == right) {
            return left;
        } else if( (left == OPTypes.INT && right == OPTypes.DOUBLE) || (left == OPTypes.DOUBLE && right == OPTypes.INT)) {
            return OPTypes.DOUBLE;
        } else if( (left == OPTypes.STRING) || (right == OPTypes.STRING)) {
            return OPTypes.STRING;
        } else {
            throw new IllegalArgumentException("unsupported type coercion: " + left + " and " + right);
        }
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
            return parent.getDialect().getRegexp(value, pattern, _flags);
        } else {
            if(!simplified.startsWith("%") && !simplified.endsWith("%")) {
                if(StringUtils.containsIgnoreCase(_flags,"i")) {
                    return String.format("lower(%s) = lower('%s')", value, simplified);
                } else {
                    return String.format("%s = '%s'", value, simplified);
                }
            } else {
                if(StringUtils.containsIgnoreCase(_flags,"i")) {
                    return parent.getDialect().getILike(value, "'" + simplified + "'");
                } else {
                    return value + " LIKE '"+simplified+"'";
                }
            }
        }

    }

    protected Value getVarValue(Var var, BindingSet bindings) {
        if (var == null) {
            return null;
        }
        else if (var.hasValue()) {
            return var.getValue();
        }
        else {
            return bindings.getValue(var.getName());
        }
    }


    private static enum OPTypes {
        STRING, DOUBLE, INT, DATE, BOOL, ANY
    }

    public KiWiDialect getDialect() {
        return parent.getDialect();
    }


    private static Map<URI,OPTypes> functionParameterTypes = new HashMap<>();
    static {
        functionParameterTypes.put(FN.CONCAT, OPTypes.STRING);
        functionParameterTypes.put(FN.CONTAINS, OPTypes.STRING);
        functionParameterTypes.put(FN.LOWER_CASE, OPTypes.STRING);
        functionParameterTypes.put(FN.UPPER_CASE, OPTypes.STRING);
        functionParameterTypes.put(FN.REPLACE, OPTypes.STRING);
        functionParameterTypes.put(FN.SUBSTRING_AFTER, OPTypes.STRING);
        functionParameterTypes.put(FN.SUBSTRING_BEFORE, OPTypes.STRING);
        functionParameterTypes.put(FN.STARTS_WITH, OPTypes.STRING);
        functionParameterTypes.put(FN.ENDS_WITH, OPTypes.STRING);
        functionParameterTypes.put(FN.STRING_LENGTH, OPTypes.STRING);
        functionParameterTypes.put(FN.SUBSTRING, OPTypes.STRING);

        functionParameterTypes.put(FN.NUMERIC_ABS, OPTypes.DOUBLE);
        functionParameterTypes.put(FN.NUMERIC_CEIL, OPTypes.DOUBLE);
        functionParameterTypes.put(FN.NUMERIC_FLOOR, OPTypes.DOUBLE);
        functionParameterTypes.put(FN.NUMERIC_ROUND, OPTypes.DOUBLE);

    }

    private static Map<URI,OPTypes> functionReturnTypes = new HashMap<>();
    static {
        functionReturnTypes.put(FN.CONCAT, OPTypes.STRING);
        functionReturnTypes.put(FN.CONTAINS, OPTypes.BOOL);
        functionReturnTypes.put(FN.LOWER_CASE, OPTypes.STRING);
        functionReturnTypes.put(FN.UPPER_CASE, OPTypes.STRING);
        functionReturnTypes.put(FN.REPLACE, OPTypes.STRING);
        functionReturnTypes.put(FN.SUBSTRING_AFTER, OPTypes.STRING);
        functionReturnTypes.put(FN.SUBSTRING_BEFORE, OPTypes.STRING);
        functionReturnTypes.put(FN.STARTS_WITH, OPTypes.BOOL);
        functionReturnTypes.put(FN.ENDS_WITH, OPTypes.BOOL);
        functionReturnTypes.put(FN.STRING_LENGTH, OPTypes.INT);
        functionReturnTypes.put(FN.SUBSTRING, OPTypes.STRING);

        functionReturnTypes.put(FN.NUMERIC_ABS, OPTypes.DOUBLE);
        functionReturnTypes.put(FN.NUMERIC_CEIL, OPTypes.INT);
        functionReturnTypes.put(FN.NUMERIC_FLOOR, OPTypes.INT);
        functionReturnTypes.put(FN.NUMERIC_ROUND, OPTypes.INT);

    }


    private static class LimitFinder extends QueryModelVisitorBase<RuntimeException> {

        long limit = -1, offset = -1;

        private LimitFinder(TupleExpr expr) {
            expr.visit(this);
        }

        @Override
        public void meet(Slice node) throws RuntimeException {
            if(node.hasLimit())
                limit = node.getLimit();
            if(node.hasOffset())
                offset = node.getOffset();
        }
    }

    private static class DistinctFinder extends QueryModelVisitorBase<RuntimeException> {

        boolean distinct = false;

        private DistinctFinder(TupleExpr expr) {
            expr.visit(this);
        }

        @Override
        public void meet(Distinct node) throws RuntimeException {
            distinct = true;
        }

        @Override
        public void meet(Reduced node) throws RuntimeException {
            distinct = true;
        }
    }


    private static class PatternCollector extends QueryModelVisitorBase<RuntimeException> {

        List<StatementPattern> patterns = new ArrayList<>();

        private PatternCollector(TupleExpr expr) {
            expr.visit(this);
        }

        @Override
        public void meet(StatementPattern node) throws RuntimeException {
            patterns.add(node);

            super.meet(node);
        }
    }


    private static class FilterCollector extends QueryModelVisitorBase<RuntimeException> {

        List<ValueExpr> filters = new ArrayList<>();

        private FilterCollector(TupleExpr expr) {
            expr.visit(this);
        }

        @Override
        public void meet(Filter node) throws RuntimeException {
            filters.add(node.getCondition());

            super.meet(node);
        }
    }
}
