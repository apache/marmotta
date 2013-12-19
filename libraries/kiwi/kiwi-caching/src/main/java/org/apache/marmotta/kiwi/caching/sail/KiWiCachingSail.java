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

package org.apache.marmotta.kiwi.caching.sail;

import org.apache.marmotta.kiwi.caching.config.KiWiQueryCacheConfiguration;
import org.apache.marmotta.kiwi.caching.transaction.GeronimoTransactionManagerLookup;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailWrapper;
import org.openrdf.sail.helpers.SailWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A sail wrapper for KiWi stores that introduces transparent query caching using Infinispan distributed caches.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiCachingSail extends NotifyingSailWrapper {

    public static final String QUERY_CACHE = "query-cache";

    private static Logger log = LoggerFactory.getLogger(KiWiCachingSail.class);

    private KiWiStore parent;


    private EmbeddedCacheManager cacheManager;

    private KiWiQueryCacheConfiguration configuration;

    /**
     * Creates a new SailWrapper that wraps the supplied Sail.
     *
     * @param baseSail
     */
    public KiWiCachingSail(NotifyingSail baseSail, KiWiQueryCacheConfiguration configuration) {
        super(baseSail);

        this.parent = getRootSail(baseSail);
        this.configuration = configuration;
    }


    @Override
    public void initialize() throws SailException {
        super.initialize();

        this.cacheManager = parent.getPersistence().getCacheManager().getCacheManager();
    }

    @Override
    public NotifyingSailConnection getConnection() throws SailException {
        return new KiWiCachingSailConnection(super.getConnection(), getQueryCache(), configuration.getMaxEntrySize());
    }



    /**
     * Return the query key -> query result cache from the cache manager. This cache is used for speeding up the
     * listing of query results.
     *
     * @return
     */
    private Cache getQueryCache() {
        if(!cacheManager.cacheExists(QUERY_CACHE)) {
            Configuration tripleConfiguration = new ConfigurationBuilder().read(cacheManager.getDefaultCacheConfiguration())
                    .transaction()
                        .transactionMode(TransactionMode.TRANSACTIONAL)
                        .transactionManagerLookup(new GeronimoTransactionManagerLookup())
                        .cacheStopTimeout(1, TimeUnit.SECONDS)
                    .eviction()
                    .   maxEntries(configuration.getMaxCacheSize())
                    .expiration()
                        .lifespan(60, TimeUnit.MINUTES)
                        .maxIdle(30, TimeUnit.MINUTES)
                    .build();
            cacheManager.defineConfiguration(QUERY_CACHE, tripleConfiguration);
        }
        return cacheManager.getCache(QUERY_CACHE);
    }


    /**
     * Get the root sail in the wrapped sail stack
     * @param sail
     * @return
     */
    private KiWiStore getRootSail(Sail sail) {
        if(sail instanceof KiWiStore) {
            return (KiWiStore) sail;
        } else if(sail instanceof SailWrapper) {
            return getRootSail(((SailWrapper) sail).getBaseSail());
        } else {
            throw new IllegalArgumentException("root sail is not a KiWiStore or could not be found");
        }
    }

}
