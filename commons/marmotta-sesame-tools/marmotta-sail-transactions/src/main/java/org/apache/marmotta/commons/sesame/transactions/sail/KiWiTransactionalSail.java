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
import org.apache.marmotta.commons.sesame.transactions.api.TransactionalSail;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an extended version of the KiWi triple store with support for transaction tracking. It offers the
 * possibility to register transaction listeners that are triggered whenever a connection commits or rolls back.
 * If the transaction commits, they are also passed over the transaction data, i.e. the added and removed triples.
 * <p/>
 * Note that even the basic KiWiStore offers transaction support by directly wrapping database transactions. The
 * extended transactions provided by the KiWiTransactionalStore are only necessary for getting access to the
 * transaction data and triggering actions on commit or rollback.s
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiTransactionalSail extends NotifyingSailWrapper implements TransactionalSail {

    private List<TransactionListener> listeners;

    private boolean transactionsEnabled;

    public KiWiTransactionalSail(NotifyingSail base) {
        super(base);

        this.listeners           = new ArrayList<TransactionListener>();
        this.transactionsEnabled = true;
    }

    /**
     * Add a transaction listener to the KiWiTransactionalStore. The listener will be notified whenever a connection
     * commits or rolls back. The listeners are collected in a list, i.e. a listener that is added first is also executed
     * first.
     *
     * @param listener the listener to add to the list
     */
    public void addTransactionListener(TransactionListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a transaction listener from the list.
     *
     * @param listener the listener to remove
     */
    public void removeTransactionListener(TransactionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Check if extended transaction support is enabled
     * @return true if extended transactions are enabled
     */
    public boolean isTransactionsEnabled() {
        return transactionsEnabled;
    }

    /**
     * Temporarily enable/disable extended transactions. Disabling transactions might be useful when bulk loading large
     * amounts of data.
     *
     * @param transactionsEnabled
     */
    public void setTransactionsEnabled(boolean transactionsEnabled) {
        this.transactionsEnabled = transactionsEnabled;
    }

    /**
     * Returns a store-specific SailConnection object.
     *
     * @return A connection to the store.
     */
    @Override
    public NotifyingSailConnection getConnection() throws SailException {
        if(transactionsEnabled)
            return new KiWiTransactionalConnection(super.getConnection(),listeners);
        else
            return super.getConnection();
    }
}
