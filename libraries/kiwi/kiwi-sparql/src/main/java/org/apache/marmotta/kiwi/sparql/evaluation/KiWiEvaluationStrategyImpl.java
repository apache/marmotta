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
import org.apache.marmotta.kiwi.sparql.builder.collect.SupportedFinder;
import org.apache.marmotta.kiwi.sparql.persistence.KiWiSparqlConnection;
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
import java.util.HashSet;
import java.util.Set;

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

    private Set<String> projectedVars = new HashSet<>();

    public KiWiEvaluationStrategyImpl(TripleSource tripleSource, KiWiSparqlConnection connection) {
        super(tripleSource);
        this.connection = connection;
    }

    public KiWiEvaluationStrategyImpl(TripleSource tripleSource, Dataset dataset, KiWiSparqlConnection connection) {
        super(tripleSource, dataset);
        this.connection = connection;
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

    public CloseableIteration<BindingSet, QueryEvaluationException> evaluateNative(TupleExpr expr, BindingSet bindings) throws QueryEvaluationException {
        log.debug("applying KiWi native optimizations on SPARQL query ...");

        try {
            return new ExceptionConvertingIteration<BindingSet, QueryEvaluationException>(connection.evaluateNative(expr, bindings, dataset, projectedVars)) {
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
