/*
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

import org.apache.marmotta.commons.sesame.transactions.api.TransactionListener;
import org.apache.marmotta.commons.sesame.transactions.model.TransactionData;
import org.apache.marmotta.kiwi.versioning.persistence.KiWiVersioningPersistence;

/**
 * A version listener that can be added to a KiWiTransactionalStore. Will create versions when a transaction commits.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiVersionListener implements TransactionListener {

    /**
     * Called after a transaction has committed. The transaction data will contain all changes done in the transaction since
     * the last commit. This method should be used in case the transaction listener aims to perform additional activities
     * in a new transaction or outside the transaction management, e.g. notifying a server on the network, adding
     * data to a cache, or similar.
     *
     * @param data
     */
    @Override
    public void afterCommit(TransactionData data) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called before a transaction commits. The transaction data will contain all changes done in the transaction since
     * the last commit. This method should be used in case the transaction listener aims to perform additional activities
     * in the same transaction, like inserting or updating database tables.
     *
     * @param data
     */
    @Override
    public void beforeCommit(TransactionData data) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Called when a transaction rolls back.
     */
    @Override
    public void rollback(TransactionData data) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * Return the backend used for versioning. Used to provide advanced access to versions, e.g.
     * for listing.
     *
     * @return
     */
    public KiWiVersioningPersistence getVersioningBackend() {
        return null;
    }
}
