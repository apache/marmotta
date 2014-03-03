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
import org.apache.marmotta.kiwi.caching.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A triple registry implementation based on the Infinispan cache. Registry entries are stored in a replicated,
 * synchronized Infinispan cache. Transaction information is kept locally.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class CacheTripleRegistry implements KiWiTripleRegistry {

    private static Logger log = LoggerFactory.getLogger(CacheTripleRegistry.class);

    private Map<Long,Long> cache;


    private Map<Long,List<Long>>  transactions;


    public CacheTripleRegistry(CacheManager cacheManager) {
        cache        = cacheManager.getRegistryCache();
        transactions = new HashMap<>();

    }


    /**
     * Register a key/triple id pair in the triple registry for the given transaction ID.
     *
     * @param key           the key identifying the triple arguments (subject, object, predicate, context)
     * @param transactionId the identifier of the transaction registering the triple id
     * @param tripleId      the new triple identifier
     */
    @Override
    public void registerKey(IntArray key, long transactionId, long tripleId) {
        List<Long> transaction = transactions.get(transactionId);
        if(transaction == null) {
            transaction = new ArrayList<>();
            transactions.put(transactionId, transaction);
        }
        cache.put(key.longHashCode(), tripleId);
        transaction.add(key.longHashCode());
    }

    /**
     * Check if another (or the same) transaction has already registered an ID for the triple with the
     * given key. Returns -1 in case no other ID has been registered, or a value >0 otherwise.
     *
     * @param key the key identifying the triple arguments (subject, object, predicate, context)
     * @return id of the triple or -1
     */
    @Override
    public long lookupKey(IntArray key) {
        Long value = cache.get(key.longHashCode());
        if(value != null) {
            return value;
        } else {
            return -1;
        }
    }

    /**
     * Free all registry entries claimed by the transaction with the given identifier. Should remove or
     * expire all registry entries to avoid unnecessary storage consumption.
     *
     * @param transactionId the identifier of the transaction registering the triple id
     */
    @Override
    public void releaseTransaction(long transactionId) {
        if(transactions.containsKey(transactionId)) {
            for(long key : transactions.remove(transactionId)) {
                cache.remove(key);
            }
        }
    }

    /**
     * Remove the key with the given key, e.g. when a statement is again deleted during a transaction.
     *
     * @param key the key identifying the triple arguments (subject, object, predicate, context)
     */
    @Override
    public void deleteKey(IntArray key) {
        cache.remove(key.longHashCode());
    }
}
