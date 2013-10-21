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

package org.apache.marmotta.platform.backend.bigdata;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.store.AbstractTripleStore;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.helpers.SailBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class BigDataSesame27Sail extends NotifyingSailBase implements NotifyingSail {

    private static Logger log = LoggerFactory.getLogger(BigDataSesame27Sail.class);

    /**
     * Objects that should be notified of changes to the data in this Sail.
     */
    private Set<SailChangedListener> sailChangedListeners = new HashSet<SailChangedListener>(0);


    private BigdataSail wrapped;

    /**
     * Create or re-open a database instance configured using defaults.
     */
    public BigDataSesame27Sail() {
        wrapped = new BigdataSail();
    }

    /**
     * Constructor used to wrap an existing {@link com.bigdata.rdf.store.AbstractTripleStore}
     * instance.
     *
     * @param database The instance.
     */
    public BigDataSesame27Sail(AbstractTripleStore database) {
        wrapped = new BigdataSail(database);
    }

    /**
     * Core ctor. You must use this variant for a scale-out triple store.
     * <p/>
     * To create a {@link com.bigdata.rdf.sail.BigdataSail} backed by an {@link com.bigdata.service.IBigdataFederation}
     * use the {@link com.bigdata.rdf.store.ScaleOutTripleStore} ctor and then
     * {@link com.bigdata.rdf.store.AbstractTripleStore#create()} the triple store if it does not
     * exist.
     *
     * @param database     An existing {@link com.bigdata.rdf.store.AbstractTripleStore}.
     * @param mainDatabase When <i>database</i> is a {@link com.bigdata.rdf.store.TempTripleStore}, this is the
     *                     {@link com.bigdata.rdf.store.AbstractTripleStore} used to resolve the
     *                     {@link com.bigdata.bop.engine.QueryEngine}. Otherwise it must be the same object as
     *                     the <i>database</i>.
     */
    public BigDataSesame27Sail(AbstractTripleStore database, AbstractTripleStore mainDatabase) {
        wrapped = new BigdataSail(database,mainDatabase);
    }

    /**
     * Create or open a database instance configured using the specified
     * properties.
     *
     * @see com.bigdata.rdf.sail.BigdataSail.Options
     */
    public BigDataSesame27Sail(Properties properties) {
        wrapped = new BigdataSail(properties);
    }



    /**
     * Adds the specified SailChangedListener to receive events when the data in
     * this Sail object changes.
     */
    @Override
    public void addSailChangedListener(SailChangedListener listener) {
        synchronized (sailChangedListeners) {
            sailChangedListeners.add(listener);
        }
    }

    /**
     * Removes the specified SailChangedListener so that it no longer receives
     * events from this Sail object.
     */
    @Override
    public void removeSailChangedListener(SailChangedListener listener) {
        synchronized (sailChangedListeners) {
            sailChangedListeners.remove(listener);
        }
    }

    /*
    * Notifies all registered SailChangedListener's of changes to the contents
    * of this Sail.
    */
    public void notifySailChanged(SailChangedEvent event) {
        synchronized (sailChangedListeners) {
            for (SailChangedListener l : sailChangedListeners) {
                l.sailChanged(event);
            }
        }
    }


    @Override
    public void initialize() throws SailException {
        wrapped.initialize();

        super.initialize();
    }

    @Override
    protected NotifyingSailConnection getConnectionInternal() throws SailException {
        return new BigDataSesame27SailConnection(this,wrapped.getConnection());
    }

    /**
     * Do store-specific operations to ensure proper shutdown of the store.
     */
    @Override
    protected void shutDownInternal() throws SailException {
        wrapped.shutDown();
    }

    /**
     * Checks whether this Sail object is writable, i.e. if the data contained in
     * this Sail object can be changed.
     */
    @Override
    public boolean isWritable() throws SailException {
        return wrapped.isWritable();
    }

    /**
     * Gets a ValueFactory object that can be used to create URI-, blank node-,
     * literal- and statement objects.
     *
     * @return a ValueFactory object for this Sail object.
     */
    @Override
    public ValueFactory getValueFactory() {
        return wrapped.getValueFactory();
    }

    /**
     * Get the wrapped BigdataSail
     *
     * @return
     */
    protected BigdataSail getWrapped() {
        return wrapped;
    }

    /**
     * A wrapper around the (Sesame 2.6) BigdataSailConnection, adding the new API methods of Sesame 2.7
     */
    protected static class BigDataSesame27SailConnection extends NotifyingSailConnectionBase {
        BigdataSail.BigdataSailConnection con;

        public BigDataSesame27SailConnection(SailBase sailBase, BigdataSail.BigdataSailConnection con) {
            super(sailBase);
            this.con = con;
        }

        protected BigdataSail.BigdataSailConnection getWrapped() {
            return con;
        }

        @Override
        protected void closeInternal() throws SailException {
            con.close();
        }

        @Override
        protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred) throws SailException {
            return con.evaluate(tupleExpr,dataset,bindings,includeInferred);
        }

        @Override
        protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal() throws SailException {
            return con.getContextIDs();
        }

        @Override
        protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts) throws SailException {
            return con.getStatements(subj,pred,obj,includeInferred,contexts);
        }

        @Override
        protected long sizeInternal(Resource... contexts) throws SailException {
            return con.size(contexts);
        }

        @Override
        protected void startTransactionInternal() throws SailException {
            // wrapped connection is Sesame 2.6, no begin method
        }

        @Override
        protected void commitInternal() throws SailException {
            con.commit();
        }

        @Override
        protected void rollbackInternal() throws SailException {
            con.rollback();
        }

        @Override
        protected void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
            con.addStatement(subj,pred,obj,contexts);
        }

        @Override
        protected void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
            con.removeStatements(subj,pred,obj,contexts);
        }

        @Override
        protected void clearInternal(Resource... contexts) throws SailException {
            con.clear(contexts);
        }

        @Override
        protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal() throws SailException {
            return con.getNamespaces();
        }

        @Override
        protected String getNamespaceInternal(String prefix) throws SailException {
            return con.getNamespace(prefix);
        }

        @Override
        protected void setNamespaceInternal(String prefix, String name) throws SailException {
            con.setNamespace(prefix,name);
        }

        @Override
        protected void removeNamespaceInternal(String prefix) throws SailException {
            con.removeNamespace(prefix);
        }

        @Override
        protected void clearNamespacesInternal() throws SailException {
            con.clearNamespaces();
        }

    }
}
