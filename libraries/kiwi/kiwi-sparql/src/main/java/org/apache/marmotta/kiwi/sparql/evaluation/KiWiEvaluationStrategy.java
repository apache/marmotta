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

package org.apache.marmotta.kiwi.sparql.evaluation;

import info.aduna.iteration.*;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.util.ResultSetIteration;
import org.apache.marmotta.kiwi.persistence.util.ResultTransformerFunction;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.apache.marmotta.kiwi.sparql.builder.SQLBuilder;
import org.apache.marmotta.kiwi.sparql.builder.ValueType;
import org.apache.marmotta.kiwi.sparql.builder.collect.SupportedFinder;
import org.apache.marmotta.kiwi.sparql.builder.model.SQLVariable;
import org.apache.marmotta.kiwi.sparql.exception.UnsatisfiableQueryException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.impl.MapBindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * An implementation of the SPARQL query evaluation strategy with specific extensions and optimizations. The KiWi
 * evaluation strategy is able to apply optimizations to certain frequently found query patterns by directly translating
 * them into SQL queries. Currently, the following constructs are supported:
 * <ul>
 *     <li>JOINs of statement patterns are translated into SQL joins (no OPTIONAL and no path expressions supporterd)</li>
 *     <li>FILTERs are translated to SQL where conditions, in case the FILTER conditions are supported (no aggregation constructs are supported)</li>
 * </ul>
 * In case a query is not completely supported by the optimizer, the optimizer might still improve performance by
 * evaluating the optimizable components of the query and then letting the in-memory implementation take over
 * (e.g. for aggregation constructs, distinct, path expressions, optional).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiEvaluationStrategy extends EvaluationStrategyImpl{

    private static Logger log = LoggerFactory.getLogger(KiWiEvaluationStrategy.class);


    /**
     * The database connection offering specific SPARQL-SQL optimizations.
     */
    private KiWiConnection connection;
    private KiWiValueFactory valueFactory;
    private ExecutorService executorService;


    private Set<String> projectedVars = new HashSet<>();

    public KiWiEvaluationStrategy(TripleSource tripleSource, KiWiConnection connection, KiWiValueFactory valueFactory) {
        super(tripleSource);
        this.connection = connection;
        this.valueFactory = valueFactory;

        // interruptible queries run in a separate thread
        this.executorService = Executors.newCachedThreadPool();
    }

    public KiWiEvaluationStrategy(TripleSource tripleSource, Dataset dataset, KiWiConnection connection, KiWiValueFactory valueFactory) {
        super(tripleSource, dataset);
        this.connection = connection;
        this.valueFactory = valueFactory;

        // interruptible queries run in a separate thread
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Projection projection, BindingSet bindings) throws QueryEvaluationException {
        // count projected variables
        if(isSupported(projection.getArg())) {
            for (ProjectionElem elem : projection.getProjectionElemList().getElements()) {
                projectedVars.add(elem.getSourceName());
            }
        }

        return super.evaluate(projection, bindings);
    }


    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Union union, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(union)) {
            return evaluateNative(union, bindings);
        } else {
            return super.evaluate(union, bindings);
        }
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Extension order, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(order)) {
            return evaluateNative(order, bindings);
        } else {
            return super.evaluate(order, bindings);
        }
    }


    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Order order, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(order)) {
            return evaluateNative(order, bindings);
        } else {
            return super.evaluate(order, bindings);
        }
    }


    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(LeftJoin join, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(join)) {
            return evaluateNative(join, bindings);
        } else {
            return super.evaluate(join, bindings);
        }
    }


    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Join join, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(join)) {
            return evaluateNative(join, bindings);
        } else {
            return super.evaluate(join, bindings);
        }
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Filter join, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(join)) {
            return evaluateNative(join, bindings);
        } else {
            return super.evaluate(join, bindings);
        }
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Slice slice, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(slice)) {
            return evaluateNative(slice, bindings);
        } else {
            return super.evaluate(slice, bindings);
        }
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Reduced reduced, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(reduced)) {
            return evaluateNative(reduced, bindings);
        } else {
            return super.evaluate(reduced, bindings);
        }
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Distinct distinct, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(distinct)) {
            return evaluateNative(distinct, bindings);
        } else {
            return super.evaluate(distinct, bindings);
        }
    }

    /**
     * Evaluate a statement pattern join or filter on the database by translating it into an appropriate SQL statement.
     * Copied and adapted from KiWiReasoningConnection.query()
     *
     * @param join
     * @return
     */
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluateNative(TupleExpr join, final BindingSet bindings) throws QueryEvaluationException {
        log.debug("applying KiWi native optimizations on SPARQL query ...");

        try {
            final SQLBuilder builder = new SQLBuilder(join, bindings, dataset, valueFactory, connection.getDialect(), projectedVars);

            final PreparedStatement queryStatement = connection.getJDBCConnection().prepareStatement(builder.build().toString());
            if (connection.getDialect().isCursorSupported()) {
                queryStatement.setFetchSize(connection.getConfiguration().getCursorSize());
            }

            Future<ResultSet> queryFuture =
                    executorService.submit(new Callable<ResultSet>() {
                                               @Override
                                               public ResultSet call() throws Exception {
                                                   try {
                                                       return queryStatement.executeQuery();
                                                   } catch (SQLException ex) {
                                                       if (Thread.interrupted()) {
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

                        List<SQLVariable> vars = new ArrayList<>(builder.getVariables().values());

                        long[] nodeIds = new long[vars.size()];
                        for(int i=0; i<vars.size(); i++) {
                            SQLVariable sv = vars.get(i);
                            if(sv.getProjectionType() == ValueType.NODE && (builder.getProjectedVars().isEmpty() || builder.getProjectedVars().contains(sv.getSparqlName()))) {
                                nodeIds[i] = row.getLong(sv.getName());
                            }
                        }
                        KiWiNode[] nodes = connection.loadNodesByIds(nodeIds);

                        for (int i = 0; i < vars.size(); i++) {
                            SQLVariable sv = vars.get(i);
                            if(nodes[i] != null) {
                                // resolved node
                                resultRow.addBinding(sv.getSparqlName(), nodes[i]);
                            } else if(sv.getProjectionType() != ValueType.NONE && (builder.getProjectedVars().isEmpty() || builder.getProjectedVars().contains(sv.getSparqlName()))) {
                                // literal value
                                String svalue;
                                switch (sv.getProjectionType()) {
                                    case URI:
                                        svalue = row.getString(sv.getName());
                                        if(svalue != null)
                                            resultRow.addBinding(sv.getSparqlName(), new URIImpl(svalue));
                                        break;
                                    case BNODE:
                                        svalue = row.getString(sv.getName());
                                        if(svalue != null)
                                            resultRow.addBinding(sv.getSparqlName(), new BNodeImpl(svalue));
                                        break;
                                    case INT:
                                        if(row.getObject(sv.getName()) != null) {
                                            svalue = Integer.toString(row.getInt(sv.getName()));
                                            URI type = XSD.Integer;
                                            try {
                                                long typeId = row.getLong(sv.getName() + "_TYPE");
                                                if (typeId > 0)
                                                    type = (URI) connection.loadNodeById(typeId);
                                            } catch (SQLException ex) {
                                            }

                                            resultRow.addBinding(sv.getSparqlName(), new LiteralImpl(svalue, type));
                                        }
                                        break;
                                    case DOUBLE:
                                        if(row.getObject(sv.getName()) != null) {
                                            svalue = Double.toString(row.getDouble(sv.getName()));
                                            URI type = XSD.Double;
                                            try {
                                                long typeId = row.getLong(sv.getName() + "_TYPE");
                                                if (typeId > 0)
                                                    type = (URI) connection.loadNodeById(typeId);
                                            } catch (SQLException ex) {
                                            }

                                            resultRow.addBinding(sv.getSparqlName(), new LiteralImpl(svalue, type));
                                        }
                                        break;
                                    case DECIMAL:
                                        if(row.getObject(sv.getName()) != null) {
                                            svalue = row.getBigDecimal(sv.getName()).toString();
                                            URI type = XSD.Decimal;
                                            try {
                                                long typeId = row.getLong(sv.getName() + "_TYPE");
                                                if (typeId > 0)
                                                    type = (URI) connection.loadNodeById(typeId);
                                            } catch (SQLException ex) {
                                            }

                                            resultRow.addBinding(sv.getSparqlName(), new LiteralImpl(svalue, type));
                                        }
                                        break;
                                    case BOOL:
                                        if(row.getObject(sv.getName()) != null) {
                                            svalue = Boolean.toString(row.getBoolean(sv.getName()));
                                            resultRow.addBinding(sv.getSparqlName(), new LiteralImpl(svalue.toLowerCase(), XSD.Boolean));
                                        }
                                        break;
                                    case STRING:
                                    default:
                                        svalue = row.getString(sv.getName());

                                        if(svalue != null) {

                                            // retrieve optional type and language information, because string functions
                                            // need to preserve this in certain cases, even when constructing new literals
                                            String lang = null;
                                            try {
                                                lang = row.getString(sv.getName() + "_LANG");
                                            } catch (SQLException ex) {
                                            }

                                            URI type = null;
                                            try {
                                                long typeId = row.getLong(sv.getName() + "_TYPE");
                                                if (typeId > 0)
                                                    type = (URI) connection.loadNodeById(typeId);
                                            } catch (SQLException ex) {
                                            }

                                            if (lang != null) {
                                                if (svalue.length() > 0) {
                                                    resultRow.addBinding(sv.getSparqlName(), new LiteralImpl(svalue, lang));
                                                } else {
                                                    // string functions that return empty literal should yield no type or language
                                                    resultRow.addBinding(sv.getSparqlName(), new LiteralImpl(""));
                                                }
                                            } else if (type != null) {
                                                if(type.stringValue().equals(XSD.String.stringValue())) {
                                                    // string functions on other datatypes than string should yield no binding
                                                    if (svalue.length() > 0) {
                                                        resultRow.addBinding(sv.getSparqlName(), new LiteralImpl(svalue, type));
                                                    } else {
                                                        // string functions that return empty literal should yield no type or language
                                                        resultRow.addBinding(sv.getSparqlName(), new LiteralImpl(""));
                                                    }
                                                }
                                            } else {
                                                resultRow.addBinding(sv.getSparqlName(), new LiteralImpl(svalue));
                                            }

                                        }
                                        break;
                                }
                            }
                        }


                        if (bindings != null) {
                            for (Binding binding : bindings) {
                                resultRow.addBinding(binding);
                            }
                        }
                        return resultRow;
                    }
                });


                return new ExceptionConvertingIteration<BindingSet, QueryEvaluationException>(new CloseableIteratorIteration<BindingSet, SQLException>(Iterations.asList(it).iterator())) {
                    @Override
                    protected QueryEvaluationException convert(Exception e) {
                        return new QueryEvaluationException(e);
                    }
                };

            } catch (InterruptedException | CancellationException e) {
                log.info("SPARQL query execution cancelled");
                queryFuture.cancel(true);
                queryStatement.cancel();
                queryStatement.close();

                throw new QueryInterruptedException("SPARQL query execution cancelled");
            } catch (ExecutionException e) {
                log.error("error executing SPARQL query", e.getCause());
                if (e.getCause() instanceof SQLException) {
                    throw new QueryEvaluationException(e.getCause());
                } else if (e.getCause() instanceof InterruptedException) {
                    throw new QueryInterruptedException(e.getCause());
                } else {
                    throw new QueryEvaluationException("error executing SPARQL query", e);
                }
            }
        } catch (SQLException e) {
            throw new QueryEvaluationException(e);
        } catch (IllegalArgumentException e) {
            throw new QueryEvaluationException(e);
        } catch (UnsatisfiableQueryException ex) {
            return new EmptyIteration<>();
        }
    }


    /**
     * Test if a tuple expression is supported nby the optimized evaluation; in this case we can apply a specific optimization.
     *
     * @param expr
     * @return
     */
    private boolean isSupported(TupleExpr expr) {
        return new SupportedFinder(expr, connection.getDialect()).isSupported();
    }

}
