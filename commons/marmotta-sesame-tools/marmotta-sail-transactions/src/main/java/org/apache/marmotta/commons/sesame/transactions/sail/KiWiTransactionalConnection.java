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
package org.apache.marmotta.commons.sesame.transactions.sail;

import org.apache.marmotta.commons.sesame.transactions.api.TransactionListener;
import org.apache.marmotta.commons.sesame.transactions.api.TransactionalSailConnection;
import org.apache.marmotta.commons.sesame.transactions.model.TransactionData;
import org.openrdf.model.Statement;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;

/**
 * This is an extended version of the KiWi triple store connection with support for transaction tracking. It offers the
 * possibility to register transaction listeners that are triggered whenever a connection commits or rolls back.
 * If the transaction commits, they are also passed over the transaction data, i.e. the added and removed triples.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiTransactionalConnection extends NotifyingSailConnectionWrapper implements SailConnectionListener, TransactionalSailConnection {

    private static Logger log = LoggerFactory.getLogger(KiWiTransactionalConnection.class);

    private Collection<TransactionListener> listeners;

    private TransactionData data;

    public KiWiTransactionalConnection(NotifyingSailConnection wrapped, Collection<TransactionListener> listeners) throws SailException {
        super(wrapped);
        wrapped.addConnectionListener(this);

        this.listeners = listeners;
    }

    /**
     * Add a transaction listener to the transactional connection.
     *
     * @param listener
     */
    @Override
    public void addTransactionListener(TransactionListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a transaction listener from the transactional connection
     *
     * @param listener
     */
    @Override
    public void removeTransactionListener(TransactionListener listener) {
        listeners.remove(listener);
    }


    /**
     * This method is called when a triple has been added to the repository. It can be overridden by subclasses to
     * add additional functionality.
     *
     * @param triple
     */
    @Override
    public void statementAdded(Statement triple) {
        ensureTransactionStarted();
        data.addTriple(triple);
    }

    /**
     * This method is called when a triple has been removed from the repository. It can be overridden by subclasses to
     * add additional functionality.
     *
     * @param triple
     */
    @Override
    public void statementRemoved(Statement triple) {
        ensureTransactionStarted();
        data.removeTriple(triple);
    }

    @Override
    public void begin() throws SailException {
        super.begin();

        // start new transaction
        data = new TransactionData();

    }

    /**
     * Notify the listeners of a commit before and after calling the super method
     * @throws SailException
     */
    @Override
    public void commit() throws SailException {
        // notify only if there is actually any data
        if(data != null && data.getAddedTriples().size() + data.getRemovedTriples().size() > 0) {
            data.setCommitTime(new Date());

            // notify beforeCommit listeners
            for(TransactionListener l : listeners) {
                l.beforeCommit(data);
            }

            // perform commit
            super.commit();

            // notify afterCommit listeners
            for(TransactionListener l : listeners) {
                l.afterCommit(data);
            }
        } else {
            super.commit();
        }

        // empty transaction data
        data = new TransactionData();
    }

    /**
     * Notify the listeners after rolling back.
     * @throws SailException
     */
    @Override
    public void rollback() throws SailException {
        // perform rollback
        super.rollback();

        // notify rollback listeners
        for(TransactionListener l : listeners) {
            l.rollback(data);
        }

        // empty transaction data
        data = new TransactionData();
    }


    private void ensureTransactionStarted() {
        if(data == null) {
            log.warn("transaction was not properly started, autostarting; please consider using connection.begin() explicitly!");
            data = new TransactionData();
        }
    }
}
