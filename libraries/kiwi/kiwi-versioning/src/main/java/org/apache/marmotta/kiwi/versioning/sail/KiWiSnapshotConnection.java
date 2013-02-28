/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.kiwi.versioning.sail;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.DelayedIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.UnionIteration;
import org.apache.marmotta.kiwi.model.rdf.KiWiNamespace;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.apache.marmotta.kiwi.versioning.persistence.KiWiVersioningConnection;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.*;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailReadOnlyException;
import org.openrdf.sail.UnknownSailTransactionStateException;
import org.openrdf.sail.UpdateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiSnapshotConnection implements SailConnection {


    private KiWiVersioningConnection databaseConnection;

    private Date snapshotDate;

    private String defaultContext;

    private KiWiValueFactory valueFactory;

    private KiWiVersioningSail parent;

    public KiWiSnapshotConnection(KiWiVersioningSail sailBase, Date snapshotDate) throws SailException {
        this.snapshotDate = snapshotDate;
        try {
            this.databaseConnection = sailBase.getPersistence().getConnection();
            this.defaultContext     = sailBase.getBaseStore().getDefaultContext();
            this.valueFactory       = new KiWiValueFactory(sailBase.getBaseStore(),databaseConnection,defaultContext);
            this.parent             = sailBase;
        } catch (SQLException e) {
            throw new SailException("error establishing database connection",e);
        }
    }

    /**
     * Return the date for which this snapshot is valid.
     * @return
     */
    public Date getSnapshotDate() {
        return snapshotDate;
    }

    @Override
    public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        throw new SailReadOnlyException("snapshot sails are read-only");
    }

    @Override
    public void close() throws SailException {
        try {
            databaseConnection.close();

            parent.closeSnapshotConnection(this);
        } catch (SQLException e) {
            throw new SailException("database error while closing connection",e);
        }
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
            KiWiTripleSource tripleSource = new KiWiTripleSource(this,includeInferred);
            EvaluationStrategy strategy = new EvaluationStrategyImpl(tripleSource, dataset);

            new BindingAssigner().optimize(tupleExpr, dataset, bindings);
            new ConstantOptimizer(strategy).optimize(tupleExpr, dataset, bindings);
            new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
            new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);
            new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
            new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
            new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
            new QueryJoinOptimizer(new KiWiEvaluationStatistics()).optimize(tupleExpr, dataset, bindings);
            new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
            new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
            new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

            return strategy.evaluate(tupleExpr, bindings);

        } catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }

    @Override
    public CloseableIteration<? extends Resource, SailException> getContextIDs() throws SailException {
        try {
            return new ExceptionConvertingIteration<Resource, SailException>(databaseConnection.listContexts()) {
                @Override
                protected SailException convert(Exception e) {
                    return new SailException("database error while iterating over result set",e);
                }
            };
        } catch (SQLException e) {
            throw new SailException("database error while listing contexts",e);
        }
    }

    @Override
    public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred, Value obj, final boolean includeInferred, Resource... contexts) throws SailException {
        final KiWiResource rsubj    = valueFactory.convert(subj);
        final KiWiUriResource rpred = valueFactory.convert(pred);
        final KiWiNode robj         = valueFactory.convert(obj);

        Set<KiWiResource> contextSet = new HashSet<KiWiResource>();
        contextSet.addAll(Lists.transform(Arrays.asList(contexts), new Function<Resource, KiWiResource>() {
            @Override
            public KiWiResource apply(Resource input) {
                return valueFactory.convert(input);
            }
        }));

        Set<DelayedIteration<Statement,RepositoryException>> iterations = new HashSet<DelayedIteration<Statement, RepositoryException>>();
        if(contextSet.size() > 0) {
            for(final KiWiResource context : contextSet) {
                iterations.add(new DelayedIteration<Statement, RepositoryException>() {
                    @Override
                    protected Iteration<? extends Statement, ? extends RepositoryException> createIteration() throws RepositoryException {
                        try {
                            return databaseConnection.listTriplesSnapshot(rsubj, rpred, robj, context, includeInferred, snapshotDate);
                        } catch (SQLException e) {
                            throw new RepositoryException("database error while listing triples",e);
                        }
                    }
                });
            }
        } else {
            iterations.add(new DelayedIteration<Statement, RepositoryException>() {
                @Override
                protected Iteration<? extends Statement, ? extends RepositoryException> createIteration() throws RepositoryException {
                    try {
                        return databaseConnection.listTriplesSnapshot(rsubj, rpred, robj, null, includeInferred, snapshotDate);
                    } catch (SQLException e) {
                        throw new RepositoryException("database error while listing triples",e);
                    }
                }
            });
        }


        return new UnionIteration<Statement, SailException>(
                Iterables.transform(iterations, new Function<DelayedIteration<Statement, RepositoryException>, Iteration<? extends Statement, SailException>>() {
                    @Override
                    public Iteration<? extends Statement, SailException> apply(DelayedIteration<Statement, RepositoryException> input) {
                        return new ExceptionConvertingIteration<Statement, SailException>(input) {
                            /**
                             * Converts an exception from the underlying iteration to an exception of
                             * type <tt>X</tt>.
                             */
                            @Override
                            protected SailException convert(Exception e) {
                                return new SailException("database error while iterating over result set", e);
                            }
                        };
                    }
                })
        );
    }

    @Override
    public long size(Resource... contexts) throws SailException {
        try {
            if(contexts.length == 0) {
                return databaseConnection.getSnapshotSize(snapshotDate);
            } else {
                long sum = 0;
                for(Resource context : contexts) {
                    sum += databaseConnection.getSnapshotSize(valueFactory.convert(context),snapshotDate);
                }
                return sum;
            }
        } catch(SQLException ex) {
            throw new SailException("database error while listing triples",ex);
        }
    }

    @Override
    public void begin() throws SailException {
    }

    @Override
    public void commit() throws SailException {
        try {
            databaseConnection.commit();
        } catch (SQLException e) {
            throw new SailException("database error while committing transaction",e);
        }
    }

    @Override
    public void rollback() throws SailException {
        try {
            databaseConnection.rollback();
        } catch (SQLException e) {
            throw new SailException("database error while committing transaction",e);
        }
    }

    @Override
    public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        throw new SailReadOnlyException("snapshot sails are read-only");
    }

    @Override
    public void clear(Resource... contexts) throws SailException {
        throw new SailReadOnlyException("snapshot sails are read-only");
    }

    @Override
    public CloseableIteration<? extends Namespace, SailException> getNamespaces() throws SailException {
        try {
            return new ExceptionConvertingIteration<Namespace, SailException>(databaseConnection.listNamespaces()) {
                /**
                 * Converts an exception from the underlying iteration to an exception of
                 * type <tt>X</tt>.
                 */
                @Override
                protected SailException convert(Exception e) {
                    return new SailException("database error while iterating over namespaces",e);
                }
            };
        } catch (SQLException e) {
            throw new SailException("database error while querying namespaces",e);
        }
    }

    @Override
    public String getNamespace(String prefix) throws SailException {
        try {
            KiWiNamespace result = databaseConnection.loadNamespaceByPrefix(prefix);
            if(result != null) {
                return result.getUri();
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new SailException("database error while querying namespaces",e);
        }
    }

    @Override
    public void setNamespace(String prefix, String name) throws SailException {
        throw new SailReadOnlyException("snapshot sails are read-only");
    }

    @Override
    public void removeNamespace(String prefix) throws SailException {
        throw new SailReadOnlyException("snapshot sails are read-only");
    }

    @Override
    public void clearNamespaces() throws SailException {
        throw new SailReadOnlyException("snapshot sails are read-only");
    }


    /**
     * Adds a statement to the store. Called when adding statements through a
     * {@link org.openrdf.query.algebra.UpdateExpr} operation.
     *
     * @param op       operation properties of the {@link org.openrdf.query.algebra.UpdateExpr} operation producing
     *                 these statements.
     * @param subj     The subject of the statement to add.
     * @param pred     The predicate of the statement to add.
     * @param obj      The object of the statement to add.
     * @param contexts The context(s) to add the statement to. Note that this parameter is
     *                 a vararg and as such is optional. If no contexts are specified, a
     *                 context-less statement will be added.
     * @throws org.openrdf.sail.SailException If the statement could not be added, for example because no
     *                                        transaction is active.
     * @throws IllegalStateException          If the connection has been closed.
     */
    @Override
    public void addStatement(UpdateContext op, Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        throw new SailReadOnlyException("snapshot sails are read-only");
    }

    /**
     * Checks for an error state in the active transaction that would force the
     * transaction to be rolled back. This is an optional call; calling or not
     * calling this method should have no effect on the outcome of
     * {@link #commit()} or {@link #rollback()}. A call to this method must be
     * followed by (in the same thread) with a call to {@link #prepare()} ,
     * {@link #commit()}, {@link #rollback()}, or {@link #close()}. This method
     * may be called multiple times within the same transaction by the same
     * thread. If this method returns normally, the caller can reasonably expect
     * that a subsequent call to {@link #commit()} will also return normally. If
     * this method returns with an exception the caller should treat the
     * exception as if it came from a call to {@link #commit()}.
     *
     * @throws org.openrdf.sail.UnknownSailTransactionStateException
     *                                        If the transaction state can not be determined (this can happen
     *                                        for instance when communication between client and server fails or
     *                                        times-out). It does not indicate a problem with the integrity of
     *                                        the store.
     * @throws org.openrdf.sail.SailException If there is an active transaction and it cannot be committed.
     * @throws IllegalStateException          If the connection has been closed or prepare was already called by
     *                                        another thread.
     * @since 2.7.0
     */
    @Override
    public void prepare() throws SailException {
        throw new SailReadOnlyException("snapshot sails are read-only");
    }

    /**
     * Signals the start of an update operation. The given <code>op</code> maybe
     * passed to subsequent
     * {@link #addStatement(org.openrdf.sail.UpdateContext, org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource...)} or
     * {@link #removeStatement(org.openrdf.sail.UpdateContext, org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource...)}
     * calls before {@link #endUpdate(org.openrdf.sail.UpdateContext)} is called.
     *
     * @throws org.openrdf.sail.SailException
     */
    @Override
    public void startUpdate(UpdateContext op) throws SailException {
        throw new SailReadOnlyException("snapshot sails are read-only");
    }

    /**
     * Removes all statements matching the specified subject, predicate and
     * object from the repository. All three parameters may be null to indicate
     * wildcards. Called when removing statements through a {@link org.openrdf.query.algebra.UpdateExpr}
     * operation.
     *
     * @param op       operation properties of the {@link org.openrdf.query.algebra.UpdateExpr} operation removing these
     *                 statements.
     * @param subj     The subject of the statement that should be removed.
     * @param pred     The predicate of the statement that should be removed.
     * @param obj      The object of the statement that should be removed.
     * @param contexts The context(s) from which to remove the statement. Note that this
     *                 parameter is a vararg and as such is optional. If no contexts are
     *                 specified the method operates on the entire repository. A
     *                 <tt>null</tt> value can be used to match context-less statements.
     * @throws org.openrdf.sail.SailException If the statement could not be removed, for example because no
     *                                        transaction is active.
     * @throws IllegalStateException          If the connection has been closed.
     */
    @Override
    public void removeStatement(UpdateContext op, Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        throw new SailReadOnlyException("snapshot sails are read-only");
    }

    /**
     * Indicates that the given <code>op</code> will not be used in any call
     * again. Implementations should use this to flush of any temporary operation
     * states that may have occurred.
     *
     * @param op
     * @throws org.openrdf.sail.SailException
     */
    @Override
    public void endUpdate(UpdateContext op) throws SailException {
        throw new SailReadOnlyException("snapshot sails are read-only");
    }

    @Override
    public boolean isOpen() throws SailException {
        try {
            return !databaseConnection.isClosed();
        } catch (SQLException e) {
            throw new SailException("database error while accessing connection", e);
        }
    }

    @Override
    public boolean isActive() throws UnknownSailTransactionStateException {
        try {
            return isOpen();
        } catch (SailException e) {
            throw new UnknownSailTransactionStateException("unknown sail transaction state, error accessing database");
        }
    }

    private static class KiWiTripleSource implements TripleSource {

        private boolean inferred;
        private KiWiSnapshotConnection connection;

        private KiWiTripleSource(KiWiSnapshotConnection connection, boolean inferred) {
            this.inferred   = inferred;
            this.connection = connection;
        }

        /**
         * Gets all statements that have a specific subject, predicate and/or object.
         * All three parameters may be null to indicate wildcards. Optionally a (set
         * of) context(s) may be specified in which case the result will be
         * restricted to statements matching one or more of the specified contexts.
         *
         * @param subj     A Resource specifying the subject, or <tt>null</tt> for a
         *                 wildcard.
         * @param pred     A URI specifying the predicate, or <tt>null</tt> for a wildcard.
         * @param obj      A Value specifying the object, or <tt>null</tt> for a wildcard.
         * @param contexts The context(s) to get the statements from. Note that this parameter
         *                 is a vararg and as such is optional. If no contexts are supplied
         *                 the method operates on the entire repository.
         * @return An iterator over the relevant statements.
         * @throws org.openrdf.query.QueryEvaluationException
         *          If the triple source failed to get the statements.
         */
        @Override
        public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj, URI pred, Value obj, Resource... contexts) throws QueryEvaluationException {
            try {
                return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(
                        connection.getStatements(subj, pred, obj, inferred, contexts)
                ) {
                    @Override
                    protected QueryEvaluationException convert(Exception e) {
                        if (e instanceof ClosedByInterruptException) {
                            return new QueryInterruptedException(e);
                        }
                        else if (e instanceof IOException) {
                            return new QueryEvaluationException(e);
                        }
                        else if (e instanceof RuntimeException) {
                            throw (RuntimeException)e;
                        }
                        else if (e == null) {
                            throw new IllegalArgumentException("e must not be null");
                        }
                        else {
                            throw new IllegalArgumentException("Unexpected exception type: " + e.getClass());
                        }
                    }
                };
            } catch (SailException ex) {
                throw new QueryEvaluationException(ex);
            }
        }

        /**
         * Gets a ValueFactory object that can be used to create URI-, blank node-
         * and literal objects.
         *
         * @return a ValueFactory object for this TripleSource.
         */
        @Override
        public ValueFactory getValueFactory() {
            return connection.valueFactory;
        }
    }


    private static class KiWiEvaluationStatistics extends EvaluationStatistics {

        private final Logger log = LoggerFactory.getLogger(this.getClass());

        public KiWiEvaluationStatistics() {
        }

        @Override
        protected CardinalityCalculator createCardinalityCalculator() {
            return new KiWiCardinalityCalculator();
        }

        protected class KiWiCardinalityCalculator extends CardinalityCalculator {

            @Override
            protected double getCardinality(StatementPattern sp) {
                return super.getCardinality(sp);
            }

            protected Value getConstantValue(Var var) {
                return (var != null) ? var.getValue() : null;
            }
        }
    }

}
