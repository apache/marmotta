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

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import org.apache.marmotta.kiwi.sparql.function.FunctionUtil;
import org.apache.marmotta.kiwi.sparql.persistence.KiWiSparqlConnection;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

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
public class KiWiEvaluationStrategyImpl extends EvaluationStrategyImpl{

    private static Logger log = LoggerFactory.getLogger(KiWiEvaluationStrategyImpl.class);

    /**
     * The database connection offering specific SPARQL-SQL optimizations.
     */
    private KiWiSparqlConnection connection;

    public KiWiEvaluationStrategyImpl(TripleSource tripleSource, KiWiSparqlConnection connection) {
        super(tripleSource);
        this.connection = connection;
    }

    public KiWiEvaluationStrategyImpl(TripleSource tripleSource, Dataset dataset, KiWiSparqlConnection connection) {
        super(tripleSource, dataset);
        this.connection = connection;
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Union union, BindingSet bindings) throws QueryEvaluationException {
        if(Thread.currentThread().isInterrupted()) {
            throw new QueryEvaluationException("SPARQL evaluation has already been cancelled");
        }

        if(isSupported(union)) {
            log.debug("applying KiWi UNION optimizations on SPARQL query ...");

            try {
                return new ExceptionConvertingIteration<BindingSet, QueryEvaluationException>(connection.evaluateNative(union, bindings, dataset)) {
                    @Override
                    protected QueryEvaluationException convert(Exception e) {
                        return new QueryEvaluationException(e);
                    }
                };
            } catch (SQLException e) {
                throw new QueryEvaluationException(e.getMessage(),e);
            } catch (IllegalArgumentException e) {
                throw new QueryEvaluationException(e.getMessage(),e);
            } catch (InterruptedException e) {
                throw new QueryInterruptedException(e.getMessage());
            }
        } else {
            return super.evaluate(union, bindings);
        }
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Extension order, BindingSet bindings) throws QueryEvaluationException {
        if(Thread.currentThread().isInterrupted()) {
            throw new QueryEvaluationException("SPARQL evaluation has already been cancelled");
        }

        if(isSupported(order)) {
            log.debug("applying KiWi EXTENSION optimizations on SPARQL query ...");

            try {
                return new ExceptionConvertingIteration<BindingSet, QueryEvaluationException>(connection.evaluateNative(order, bindings, dataset)) {
                    @Override
                    protected QueryEvaluationException convert(Exception e) {
                        return new QueryEvaluationException(e);
                    }
                };
            } catch (SQLException e) {
                throw new QueryEvaluationException(e.getMessage(),e);
            } catch (IllegalArgumentException e) {
                throw new QueryEvaluationException(e.getMessage(),e);
            } catch (InterruptedException e) {
                throw new QueryInterruptedException(e.getMessage());
            }
        } else {
            return super.evaluate(order, bindings);
        }
    }


    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Order order, BindingSet bindings) throws QueryEvaluationException {
        if(Thread.currentThread().isInterrupted()) {
            throw new QueryEvaluationException("SPARQL evaluation has already been cancelled");
        }

        if(isSupported(order)) {
            log.debug("applying KiWi ORDER optimizations on SPARQL query ...");

            try {
                return new ExceptionConvertingIteration<BindingSet, QueryEvaluationException>(connection.evaluateNative(order, bindings, dataset)) {
                    @Override
                    protected QueryEvaluationException convert(Exception e) {
                        return new QueryEvaluationException(e);
                    }
                };
            } catch (SQLException e) {
                throw new QueryEvaluationException(e.getMessage(),e);
            } catch (IllegalArgumentException e) {
                throw new QueryEvaluationException(e.getMessage(),e);
            } catch (InterruptedException e) {
                throw new QueryInterruptedException(e.getMessage());
            }
        } else {
            return super.evaluate(order, bindings);
        }
    }


    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(LeftJoin join, BindingSet bindings) throws QueryEvaluationException {
        if(Thread.currentThread().isInterrupted()) {
            throw new QueryEvaluationException("SPARQL evaluation has already been cancelled");
        }

        if(isSupported(join)) {
            log.debug("applying KiWi LEFTJOIN optimizations on SPARQL query ...");

            try {
                return new ExceptionConvertingIteration<BindingSet, QueryEvaluationException>(connection.evaluateNative(join, bindings, dataset)) {
                    @Override
                    protected QueryEvaluationException convert(Exception e) {
                        return new QueryEvaluationException(e);
                    }
                };
            } catch (SQLException e) {
                throw new QueryEvaluationException(e.getMessage(),e);
            } catch (IllegalArgumentException e) {
                throw new QueryEvaluationException(e.getMessage(),e);
            } catch (InterruptedException e) {
                throw new QueryInterruptedException(e.getMessage());
            }
        } else {
            return super.evaluate(join, bindings);
        }
    }


    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Join join, BindingSet bindings) throws QueryEvaluationException {
        if(Thread.currentThread().isInterrupted()) {
            throw new QueryEvaluationException("SPARQL evaluation has already been cancelled");
        }

        if(isSupported(join)) {
            log.debug("applying KiWi JOIN optimizations on SPARQL query ...");

            try {
                return new ExceptionConvertingIteration<BindingSet, QueryEvaluationException>(connection.evaluateNative(join, bindings, dataset)) {
                    @Override
                    protected QueryEvaluationException convert(Exception e) {
                        return new QueryEvaluationException(e);
                    }
                };
            } catch (SQLException e) {
                throw new QueryEvaluationException(e.getMessage(),e);
            } catch (IllegalArgumentException e) {
                throw new QueryEvaluationException(e.getMessage(),e);
            } catch (InterruptedException e) {
                throw new QueryInterruptedException(e.getMessage());
            }
        } else {
            return super.evaluate(join, bindings);
        }
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Filter join, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(join)) {
            log.debug("applying KiWi FILTER optimizations on SPARQL query ...");

            try {
                return new ExceptionConvertingIteration<BindingSet, QueryEvaluationException>(connection.evaluateNative(join, bindings, dataset)) {
                    @Override
                    protected QueryEvaluationException convert(Exception e) {
                        return new QueryEvaluationException(e);
                    }
                };
            } catch (SQLException e) {
                throw new QueryEvaluationException(e);
            } catch (IllegalArgumentException e) {
                throw new QueryEvaluationException(e);
            } catch (InterruptedException e) {
                throw new QueryInterruptedException(e);
            }
        } else {
            return super.evaluate(join, bindings);
        }
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Slice slice, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(slice)) {
            log.debug("applying KiWi SLICE optimizations on SPARQL query ...");

            try {
                return new ExceptionConvertingIteration<BindingSet, QueryEvaluationException>(connection.evaluateNative(slice, bindings, dataset)) {
                    @Override
                    protected QueryEvaluationException convert(Exception e) {
                        return new QueryEvaluationException(e);
                    }
                };
            } catch (SQLException e) {
                throw new QueryEvaluationException(e);
            } catch (IllegalArgumentException e) {
                throw new QueryEvaluationException(e);
            } catch (InterruptedException e) {
                throw new QueryInterruptedException(e);
            }
        } else {
            return super.evaluate(slice, bindings);
        }
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Reduced reduced, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(reduced)) {
            log.debug("applying KiWi REDUCED optimizations on SPARQL query ...");

            try {
                return new ExceptionConvertingIteration<BindingSet, QueryEvaluationException>(connection.evaluateNative(reduced, bindings, dataset)) {
                    @Override
                    protected QueryEvaluationException convert(Exception e) {
                        return new QueryEvaluationException(e);
                    }
                };
            } catch (SQLException e) {
                throw new QueryEvaluationException(e);
            } catch (IllegalArgumentException e) {
                throw new QueryEvaluationException(e);
            } catch (InterruptedException e) {
                throw new QueryInterruptedException(e);
            }
        } else {
            return super.evaluate(reduced, bindings);
        }
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Distinct distinct, BindingSet bindings) throws QueryEvaluationException {
        if(isSupported(distinct)) {
            log.debug("applying KiWi DISTINCT optimizations on SPARQL query ...");

            try {
                return new ExceptionConvertingIteration<BindingSet, QueryEvaluationException>(connection.evaluateNative(distinct, bindings, dataset)) {
                    @Override
                    protected QueryEvaluationException convert(Exception e) {
                        return new QueryEvaluationException(e);
                    }
                };
            } catch (SQLException e) {
                throw new QueryEvaluationException(e);
            } catch (IllegalArgumentException e) {
                throw new QueryEvaluationException(e);
            } catch (InterruptedException e) {
                throw new QueryInterruptedException(e);
            }
        } else {
            return super.evaluate(distinct, bindings);
        }
    }


    /**
     * Test if a tuple expression is supported nby the optimized evaluation; in this case we can apply a specific optimization.
     * @param expr
     * @return
     */
    private boolean isSupported(TupleExpr expr) {
        if(expr instanceof Join) {
            return isSupported(((Join) expr).getLeftArg()) && isSupported(((Join) expr).getRightArg());
        } else if(expr instanceof LeftJoin) {
                return isSupported(((LeftJoin) expr).getLeftArg()) && isSupported(((LeftJoin) expr).getRightArg()) && isSupported(((LeftJoin)expr).getCondition());
        } else if(expr instanceof Filter) {
            return isSupported(((Filter) expr).getArg()) && isSupported(((Filter) expr).getCondition());
        } else if(expr instanceof Extension) {
            for(ExtensionElem elem : ((Extension) expr).getElements()) {
                if(!isSupported(elem.getExpr())) {
                    return false;
                }
            }
            return isSupported(((Extension) expr).getArg());
        } else if(expr instanceof StatementPattern) {
            return true;
        } else if(expr instanceof Slice) {
            return isSupported(((Slice) expr).getArg());
        } else if(expr instanceof Reduced) {
            return isSupported(((Reduced) expr).getArg());
        } else if(expr instanceof Distinct) {
            return isSupported(((Distinct) expr).getArg());
        } else if(expr instanceof Union) {
            return isSupported(((Union) expr).getLeftArg()) && isSupported(((Union)expr).getRightArg());
        } else if(expr instanceof Order) {
            for(OrderElem elem : ((Order) expr).getElements()) {
                if(!isSupported(elem.getExpr())) {
                    return false;
                }
            }
            return isSupported(((Order) expr).getArg());
        } else if(expr instanceof Group) {
            for(GroupElem elem : ((Group) expr).getGroupElements()) {
                if(!isSupported(elem.getOperator())) {
                    return false;
                }
            }
            return isSupported(((Group) expr).getArg());
        } else {
            return false;
        }
    }

    /**
     * Test if the value expression construct and all its subexpressions are supported by the optimized evaluation
     * strategy. Returns true if yes, false otherwise.
     *
     * @param expr
     * @return
     */
    private boolean isSupported(ValueExpr expr) {
        if(expr == null) {
            return true;
        } else if(expr instanceof Coalesce) {
            for(ValueExpr e : ((Coalesce) expr).getArguments()) {
                if(!isSupported(e)) {
                    return false;
                }
            }
            return true;
        } else if(expr instanceof Count) {
            if(((Count) expr).getArg() == null) {
                return connection.getDialect().isArraySupported();
            } else {
                return isSupported(((Count) expr).getArg());
            }
        } else if(expr instanceof Avg) {
            return isSupported(((Avg) expr).getArg());
        } else if(expr instanceof Min) {
            return isSupported(((Min) expr).getArg());
        } else if(expr instanceof Max) {
            return isSupported(((Max) expr).getArg());
        } else if(expr instanceof Sum) {
            return isSupported(((Sum) expr).getArg());
        } else if(expr instanceof Compare) {
            return isSupported(((Compare) expr).getLeftArg()) && isSupported(((Compare) expr).getRightArg());
        } else if(expr instanceof MathExpr) {
            return isSupported(((MathExpr) expr).getLeftArg()) && isSupported(((MathExpr) expr).getRightArg());
        } else if(expr instanceof And) {
            return isSupported(((And) expr).getLeftArg()) && isSupported(((And) expr).getRightArg());
        } else if(expr instanceof Or) {
            return isSupported(((Or) expr).getLeftArg()) && isSupported(((Or) expr).getRightArg());
        } else if(expr instanceof Not) {
            return isSupported(((Not) expr).getArg());
        } else if(expr instanceof ValueConstant) {
            return true;
        } else if(expr instanceof Var) {
            return true;
        } else if(expr instanceof Str) {
            return isAtomic(((Str) expr).getArg());
        } else if(expr instanceof Label) {
            return isAtomic(((UnaryValueOperator) expr).getArg());
        } else if(expr instanceof IsResource) {
            return isAtomic(((UnaryValueOperator) expr).getArg());
        } else if(expr instanceof IsURI) {
            return isAtomic(((UnaryValueOperator) expr).getArg());
        } else if(expr instanceof IsBNode) {
            return isAtomic(((UnaryValueOperator) expr).getArg());
        } else if(expr instanceof IsLiteral) {
            return isAtomic(((UnaryValueOperator) expr).getArg());
        } else if(expr instanceof Lang) {
            return isAtomic(((Lang) expr).getArg());
        } else if(expr instanceof LangMatches) {
            return isSupported(((LangMatches) expr).getLeftArg()) && isConstant(((LangMatches) expr).getRightArg());
        } else if(expr instanceof Regex) {
            ValueExpr flags = ((Regex) expr).getFlagsArg();
            String _flags = flags != null && flags instanceof ValueConstant ? ((ValueConstant)flags).getValue().stringValue() : null;
            return isSupported(((Regex) expr).getArg()) && isAtomic(((Regex) expr).getPatternArg()) && connection.getDialect().isRegexpSupported(_flags);
        } else if(expr instanceof FunctionCall) {
            return isFunctionSupported((FunctionCall)expr);
        } else {
            return false;
        }
    }

    private boolean isFunctionSupported(FunctionCall fc) {
        URI fnUri = FunctionUtil.getFunctionUri(fc.getURI());
        return connection.getDialect().isFunctionSupported(fnUri);
    }


    private static boolean isAtomic(ValueExpr expr) {
        return expr instanceof Var || expr instanceof ValueConstant;
    }

    private static boolean isConstant(ValueExpr expr) {
        return expr instanceof ValueConstant;
    }

}
