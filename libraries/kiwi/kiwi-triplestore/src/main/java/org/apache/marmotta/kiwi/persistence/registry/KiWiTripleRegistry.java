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

package org.apache.marmotta.kiwi.persistence.registry;

import org.apache.marmotta.commons.sesame.tripletable.IntArray;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface KiWiTripleRegistry {

    /**
     * Register a key/triple id pair in the triple registry for the given transaction ID.
     *
     * @param key            the key identifying the triple arguments (subject, object, predicate, context)
     * @param transactionId  the identifier of the transaction registering the triple id
     * @param tripleId       the new triple identifier
     */
    void registerKey(IntArray key, long transactionId, long tripleId);

    /**
     * Check if another (or the same) transaction has already registered an ID for the triple with the
     * given key. Returns -1 in case no other ID has been registered, or a value >0 otherwise.
     *
     * @param key            the key identifying the triple arguments (subject, object, predicate, context)
     * @return               id of the triple or -1
     */
    long lookupKey(IntArray key);

    /**
     * Free all registry entries claimed by the transaction with the given identifier. Should remove or
     * expire all registry entries to avoid unnecessary storage consumption.
     *
     * @param transactionId  the identifier of the transaction registering the triple id
     */
    void releaseTransaction(long transactionId);

    /**
     * Remove the key with the given key, e.g. when a statement is again deleted during a transaction.
     *
     * @param key            the key identifying the triple arguments (subject, object, predicate, context)
     */
    void deleteKey(IntArray key);
}
