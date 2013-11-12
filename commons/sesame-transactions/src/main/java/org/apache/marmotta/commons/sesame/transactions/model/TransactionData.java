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
package org.apache.marmotta.commons.sesame.transactions.model;

import org.apache.marmotta.commons.sesame.tripletable.TripleTable;
import org.openrdf.model.Statement;

import java.util.Date;
import java.util.UUID;

/**
 * Hold the data recorded in a transaction, i.e. the list of added and removed triples.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class TransactionData {

    private String transactionId;


    /*
    * Triples that have been added to the triple store
    */
    protected TripleTable<Statement> addedTriples;

    /**
     * Triples that have been removed from the triple store
     */
    protected TripleTable<Statement> removedTriples;

    /**
     * Set by transaction service to indicate the time when this transaction is committed. Used e.g. to ensure
     * consistent creation and deletion time for triples and nodes.
     */
    protected Date commitTime;




    public TransactionData() {
        transactionId  = "TX-" + UUID.randomUUID().toString();
        removedTriples = new TripleTable<Statement>();
        addedTriples   = new TripleTable<Statement>();
    }


    public void addTriple(Statement triple) {
        addedTriples.add(triple);
        removedTriples.remove(triple);
    }

    public void removeTriple(Statement triple) {
        addedTriples.remove(triple);
        removedTriples.add(triple);
    }

    public Date getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(Date commitTime) {
        this.commitTime = commitTime;
    }

    public TripleTable<Statement> getAddedTriples() {
        return addedTriples;
    }

    public TripleTable<Statement> getRemovedTriples() {
        return removedTriples;
    }


    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionData that = (TransactionData) o;

        if (!addedTriples.equals(that.addedTriples)) return false;
        if (!removedTriples.equals(that.removedTriples)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = addedTriples.hashCode();
        result = 31 * result + removedTriples.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TransactionData{" +
                "transactionId='" + transactionId + '\'' +
                ", addedTriples=" + addedTriples +
                ", removedTriples=" + removedTriples +
                ", commitTime=" + commitTime +
                '}';
    }
}
