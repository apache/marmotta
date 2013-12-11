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
package org.apache.marmotta.kiwi.sail;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import info.aduna.iteration.*;
import org.apache.marmotta.commons.sesame.repository.ResourceConnection;
import org.apache.marmotta.kiwi.exception.ResultInterruptedException;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.openrdf.model.*;
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
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedEvent;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiSailConnection extends NotifyingSailConnectionBase implements InferencerConnection, ResourceConnection {

    private static final Logger log = LoggerFactory.getLogger(KiWiSailConnection.class);

    /**
     * Internal JDBC connection to the database. Will be committed when the SailConnection commits.
     */
    protected KiWiConnection databaseConnection;

    private String defaultContext;

    private String inferredContext;

    private KiWiValueFactory valueFactory;

    private KiWiStore store;

    private boolean triplesAdded, triplesRemoved;


    public KiWiSailConnection(KiWiStore sailBase) throws SailException {
        super(sailBase);
        this.store = sailBase;
        try {
            this.databaseConnection = sailBase.getPersistence().getConnection();
            this.defaultContext     = sailBase.getDefaultContext();
            this.inferredContext    = sailBase.getInferredContext();
            //this.valueFactory       = new KiWiValueFactory(sailBase,databaseConnection,defaultContext);
            this.valueFactory       = (KiWiValueFactory) sailBase.getValueFactory();
        } catch (SQLException e) {
            throw new SailException("error establishing database connection",e);
        }
    }

    public KiWiConnection getDatabaseConnection() {
        return databaseConnection;
    }

    @Override
    protected void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        addStatementInternal(subj,pred,obj,false,contexts);
    }

    /**
     * Adds an inferred statement to a specific context.
     *
     * @param subj     The subject of the statement to add.
     * @param pred     The predicate of the statement to add.
     * @param obj      The object of the statement to add.
     * @param contexts The context(s) to add the statement to. Note that this parameter is
     *                 a vararg and as such is optional. If no contexts are supplied the
     *                 method operates on the entire repository.
     * @throws org.openrdf.sail.SailException If the statement could not be added.
     * @throws IllegalStateException          If the connection has been closed.
     */
    @Override
    public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        return addStatementInternal(subj,pred,obj,true,valueFactory.createURI(inferredContext)).size() > 0;
    }

    /**
     * Used by the KiWi reasoner, returns the created inferred statement directly.
     * @throws SailException
     */
    public KiWiTriple addInferredStatement(Resource subj, URI pred, Value obj) throws SailException {
        return addStatementInternal(subj,pred,obj,true,valueFactory.createURI(inferredContext)).iterator().next();
    }


    public Set<KiWiTriple> addStatementInternal(Resource subj, URI pred, Value obj, boolean inferred, Resource... contexts) throws SailException {
        try {
            Set<Resource> contextSet = new HashSet<Resource>();
            for(Resource ctx : contexts) {
                if(ctx != null) {
                    contextSet.add(ctx);
                }
            }
            if(contextSet.size() == 0) {
                if(defaultContext != null) {
                    contextSet.add(valueFactory.createURI(defaultContext));
                } else {
                    contextSet.add(null);
                }
            }
            if(inferred && inferredContext != null) {
                contextSet.add(valueFactory.createURI(inferredContext));
            }

            KiWiResource    ksubj = valueFactory.convert(subj);
            KiWiUriResource kpred = valueFactory.convert(pred);
            KiWiNode        kobj  = valueFactory.convert(obj);


            Set<KiWiTriple> added = new HashSet<KiWiTriple>();
            for(Resource context : contextSet) {
                KiWiResource kcontext = valueFactory.convert(context);

                KiWiTriple triple = (KiWiTriple)valueFactory.createStatement(ksubj,kpred,kobj,kcontext, databaseConnection);
                triple.setInferred(inferred);

                if(databaseConnection.storeTriple(triple)) {
                    triplesAdded = true;
                    notifyStatementAdded(triple);
                }

                added.add(triple);
            }

            return added;
        } catch(SQLException ex) {
            log.error(String.format("Could not persist rdf-statement (%s %s %s)", subj, pred, obj ), ex);
            throw new SailException("database error while storing statement",ex);
        }
    }



    @Override
    protected void closeInternal() throws SailException {
        try {
            databaseConnection.close();
        } catch (SQLException e) {
            throw new SailException("database error while closing connection",e);
        }
    }

    /**
     * Implementation of SPARQL/SeRQL querying using an abstract query expression. This very basic implementation relies
     * on the getStatements method to list triples and is not very efficient for complex queries. You might want to
     * use the kiwi-sparql module to improve the query performance.
     *
     * @param tupleExpr
     * @param dataset
     * @param bindings
     * @param includeInferred
     * @return
     * @throws SailException
     */
    @Override
    protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred) throws SailException {
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

            return strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());

        } catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }


    @Override
    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal() throws SailException {
        try {
            return  new FilterIteration<Resource, SailException>(new ExceptionConvertingIteration<Resource, SailException>(databaseConnection.listContexts()) {
                @Override
                protected SailException convert(Exception e) {
                    return new SailException("database error while iterating over result set",e);
                }
            }) {
                @Override
                protected boolean accept(Resource object) throws SailException {
                    return !object.stringValue().equals(defaultContext);
                }
            };
        } catch (SQLException e) {
            throw new SailException("database error while listing contexts",e);
        }
    }

    @Override
    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource subj, URI pred, Value obj, final boolean includeInferred, Resource... contexts) throws SailException {
        final KiWiResource rsubj    = valueFactory.convert(subj);
        final KiWiUriResource rpred = valueFactory.convert(pred);
        final KiWiNode robj         = valueFactory.convert(obj);

        Set<KiWiResource> contextSet = new HashSet<KiWiResource>();
        contextSet.addAll(Lists.transform(Arrays.asList(contexts), new Function<Resource, KiWiResource>() {
            @Override
            public KiWiResource apply(Resource input) {
                if(input == null) {
                    if(defaultContext != null) {
                        // null value for context means statements without context; in KiWi, this means "default context"
                        return (KiWiUriResource)valueFactory.createURI(defaultContext);
                    } else {
                        return null;
                    }
                } else {
                    return valueFactory.convert(input);
                }
            }
        }));

        Set<DelayedIteration<Statement,RepositoryException>> iterations = new HashSet<DelayedIteration<Statement, RepositoryException>>();
        if(contextSet.size() > 0) {
            for(final KiWiResource context : contextSet) {
                iterations.add(new DelayedIteration<Statement, RepositoryException>() {
                    @Override
                    protected Iteration<? extends Statement, ? extends RepositoryException> createIteration() throws RepositoryException {
                        try {
                            return databaseConnection.listTriples(rsubj, rpred, robj, context, includeInferred, false);
                        } catch (ResultInterruptedException e) {
                            throw new RepositoryException("listing triples interrupted",e);
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
                        return databaseConnection.listTriples(rsubj, rpred, robj, null, includeInferred, true);
                    } catch (ResultInterruptedException e) {
                        throw new RepositoryException("listing triples interrupted",e);
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
                                return new SailException("database error while iterating over result set",e.getCause());
                            }
                        };
                    }
                })
        );
    }

    @Override
    protected long sizeInternal(Resource... contexts) throws SailException {
        try {
            if(contexts.length == 0) {
                return databaseConnection.getSize();
            } else {
                long sum = 0;
                for(Resource context : contexts) {
                    sum += databaseConnection.getSize(valueFactory.convert(context));
                }
                return sum;
            }
        } catch(SQLException ex) {
            throw new SailException("database error while listing triples",ex);
        }
    }

    @Override
    protected void startTransactionInternal() throws SailException {
        // nothing to do, the database transaction is started automatically
        triplesAdded = false;
        triplesRemoved = false;
    }

    @Override
    protected void commitInternal() throws SailException {
        try {
            valueFactory.releaseRegistry(databaseConnection);
            databaseConnection.commit();
        } catch (SQLException e) {
            throw new SailException("database error while committing transaction",e);
        }
        if(triplesAdded || triplesRemoved) {

            store.notifySailChanged(new SailChangedEvent() {
                @Override
                public Sail getSail() {
                    return store;
                }

                @Override
                public boolean statementsAdded() {
                    return triplesAdded;
                }

                @Override
                public boolean statementsRemoved() {
                    return triplesRemoved;
                }
            });
        }
    }

    @Override
    protected void rollbackInternal() throws SailException {
        try {
            valueFactory.releaseRegistry(databaseConnection);
            databaseConnection.rollback();
        } catch (SQLException e) {
            throw new SailException("database error while rolling back transaction",e);
        }
    }

    @Override
    protected void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        try {
            CloseableIteration<? extends Statement, SailException> triples = getStatementsInternal(subj,pred,obj,true,contexts);
            while(triples.hasNext()) {
                KiWiTriple triple = (KiWiTriple)triples.next();
                if(triple.getId() >= 0) {
                    databaseConnection.deleteTriple(triple);
                    triplesRemoved = true;
                    notifyStatementRemoved(triple);
                }
                valueFactory.removeStatement(triple);
            }
            triples.close();
        } catch(SQLException ex) {
            throw new SailException("database error while deleting statement",ex);
        }
    }

    /**
     * Removes an inferred statement from a specific context.
     *
     * @param subj     The subject of the statement that should be removed.
     * @param pred     The predicate of the statement that should be removed.
     * @param obj      The object of the statement that should be removed.
     * @param contexts The context(s) from which to remove the statements. Note that this
     *                 parameter is a vararg and as such is optional. If no contexts are
     *                 supplied the method operates on the entire repository.
     * @throws org.openrdf.sail.SailException If the statement could not be removed.
     * @throws IllegalStateException          If the connection has been closed.
     */
    @Override
    public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        try {
            CloseableIteration<? extends Statement, SailException> triples = getStatementsInternal(subj,pred,obj,true,valueFactory.createURI(inferredContext));
            while(triples.hasNext()) {
                KiWiTriple triple = (KiWiTriple)triples.next();
                if(triple.getId() >= 0 && triple.isInferred()) {
                    databaseConnection.deleteTriple(triple);
                    triplesRemoved = true;
                    notifyStatementRemoved(triple);
                }
                valueFactory.removeStatement(triple);
            }
            triples.close();
        } catch(SQLException ex) {
            throw new SailException("database error while deleting statement",ex);
        }
        return true;
    }

    /**
     * Removes an inferred statement from a specific context.
     *
     * @throws org.openrdf.sail.SailException If the statement could not be removed.
     * @throws IllegalStateException          If the connection has been closed.
     */
    public boolean removeInferredStatement(KiWiTriple triple) throws SailException {
        try {
            if(triple.getId() >= 0 && triple.isInferred()) {
                databaseConnection.deleteTriple(triple);
                triplesRemoved = true;
                notifyStatementRemoved(triple);
            }
        } catch(SQLException ex) {
            throw new SailException("database error while deleting statement",ex);
        }
        return true;
    }

    @Override
    protected void clearInternal(Resource... contexts) throws SailException {
        removeStatementsInternal(null, null, null, contexts);
    }

    /**
     * Removes all inferred statements from the specified/all contexts. If no
     * contexts are specified the method operates on the entire repository.
     *
     * @param contexts The context(s) from which to remove the statements. Note that this
     *                 parameter is a vararg and as such is optional. If no contexts are
     *                 supplied the method operates on the entire repository.
     * @throws org.openrdf.sail.SailException If the statements could not be removed.
     * @throws IllegalStateException          If the connection has been closed.
     */
    @Override
    public void clearInferred(Resource... contexts) throws SailException {
        removeInferredStatement(null, null, null, valueFactory.createURI(inferredContext));
    }

    public void flushUpdates() {
        // no-op; changes are reported as soon as they come in
    }


    @Override
    protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal() throws SailException {
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
    protected String getNamespaceInternal(String prefix) throws SailException {
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
    protected void setNamespaceInternal(String prefix, String name) throws SailException {
        try {
            // check if the namespace prefix already exists; if yes and unequal, then delete first
            KiWiNamespace result = databaseConnection.loadNamespaceByPrefix(prefix);
            if(result != null) {
                if(!result.getUri().equals(name)) {
                    databaseConnection.deleteNamespace(result);
                    databaseConnection.storeNamespace(new KiWiNamespace(prefix,name));
                }
            } else {
                databaseConnection.storeNamespace(new KiWiNamespace(prefix,name));
            }
        } catch (SQLException e) {
            throw new SailException("database error while updating namespace",e);
        }
    }

    @Override
    protected void removeNamespaceInternal(String prefix) throws SailException {
        try {
            // check if the namespace prefix already exists; if yes and unequal, then delete first
            KiWiNamespace result = databaseConnection.loadNamespaceByPrefix(prefix);
            if(result != null) {
                databaseConnection.deleteNamespace(result);
            }
        } catch (SQLException e) {
            throw new SailException("database error while deleting namespace",e);
        }
    }

    @Override
    protected void clearNamespacesInternal() throws SailException {
        for(Namespace ns : Iterations.asList(getNamespacesInternal())) {
            removeNamespaceInternal(ns.getPrefix());
        }
    }


    public KiWiValueFactory getValueFactory() {
        return valueFactory;
    }

    private static class KiWiTripleSource implements TripleSource {

        private boolean inferred;
        private KiWiSailConnection connection;

        private KiWiTripleSource(KiWiSailConnection connection, boolean inferred) {
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
                        else if (e instanceof SailException) {
                            if(e.getCause() instanceof ResultInterruptedException) {
                                return new QueryInterruptedException(e);
                            } else {
                                return new QueryEvaluationException(e);
                            }
                        }
                        else if (e instanceof RuntimeException) {
                            throw (RuntimeException)e;
                        }
                        else if (e == null) {
                            throw new IllegalArgumentException("e must not be null");
                        }
                        else {
                            throw new IllegalArgumentException("Unexpected exception type: " + e.getClass(),e);
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

    /**
     * Return an iterator over the resources contained in this repository.
     *
     * @return
     */
    @Override
    public RepositoryResult<Resource> getResources() throws RepositoryException {
        try {
            return new RepositoryResult<Resource>(new ExceptionConvertingIteration<Resource,RepositoryException>(databaseConnection.listResources()) {
                @Override
                protected RepositoryException convert(Exception e) {
                    return new RepositoryException(e);
                }
            });
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Return an iterator over the resources contained in this repository matching the given prefix.
     *
     * @return
     */
    @Override
    public RepositoryResult<URI> getResources(String prefix) throws RepositoryException {
        try {
            return new RepositoryResult<URI>(new ExceptionConvertingIteration<URI,RepositoryException>(databaseConnection.listResources(prefix)) {
                @Override
                protected RepositoryException convert(Exception e) {
                    return new RepositoryException(e);
                }
            });
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Return the Sesame URI with the given uri identifier if it exists, or null if it does not exist.
     *
     * @param uri
     * @return
     */
    @Override
    public URI getURI(String uri) {
        try {
            return databaseConnection.loadUriResource(uri);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Return the Sesame BNode with the given anonymous ID if it exists, or null if it does not exist.
     *
     * @param id
     * @return
     */
    @Override
    public BNode getBNode(String id) {
        try {
            return databaseConnection.loadAnonResource(id);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Remove the resource given as argument from the triple store and the resource repository.
     *
     * @param resource
     */
    @Override
    public void removeResource(Resource resource) {
        // handled by garbage collection
    }

    protected static class KiWiEvaluationStatistics extends EvaluationStatistics {

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
