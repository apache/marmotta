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

package org.apache.marmotta.kiwi.sparql.sail;

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.apache.marmotta.kiwi.sparql.evaluation.KiWiEvaluationStatistics;
import org.apache.marmotta.kiwi.sparql.evaluation.KiWiEvaluationStrategy;
import org.apache.marmotta.kiwi.sparql.evaluation.KiWiTripleSource;
import org.apache.marmotta.kiwi.sparql.optimizer.DifferenceOptimizer;
import org.apache.marmotta.kiwi.sparql.optimizer.DistinctLimitOptimizer;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.impl.*;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiSparqlSailConnection extends NotifyingSailConnectionWrapper {

    private static Logger log = LoggerFactory.getLogger(KiWiSparqlSailConnection.class);

    private KiWiConnection connection;
    private KiWiValueFactory valueFactory;

    public KiWiSparqlSailConnection(NotifyingSailConnection parent, KiWiConnection connection, KiWiValueFactory valueFactory) {
        super(parent);
        this.connection = connection;
        this.valueFactory = valueFactory;
    }

    @Override
    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred) throws SailException {
        // Clone the tuple expression to allow for more aggressive optimizations
        tupleExpr = tupleExpr.clone();

        if (!(tupleExpr instanceof QueryRoot)) {
            // Add a dummy root node to the tuple expressions to allow the
            // optimizers to modify the actual root node
            tupleExpr = new QueryRoot(tupleExpr);
        }

        try {
            KiWiTripleSource tripleSource = new KiWiTripleSource(this,valueFactory,includeInferred);
            EvaluationStrategy strategy = new KiWiEvaluationStrategy(tripleSource, dataset, connection, valueFactory);

            new BindingAssigner().optimize(tupleExpr, dataset, bindings);
            //new ConstantOptimizer(strategy).optimize(tupleExpr, dataset, bindings);
            new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
            new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);

            // these are better handled by SQL directly
            //new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
            //new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);


            new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
            new QueryJoinOptimizer(new KiWiEvaluationStatistics()).optimize(tupleExpr, dataset, bindings);
            new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
            new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
            //new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);
            new DistinctLimitOptimizer().optimize(tupleExpr, dataset, bindings);

            // replace Difference with NOT EXISTS
            new DifferenceOptimizer().optimize(tupleExpr,dataset,bindings);

            log.debug("evaluating SPARQL query:\n {}", tupleExpr);

            return strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());

        } catch (QueryEvaluationException e) {
            throw new SailException(e.getMessage(),e);
        }
    }

}
