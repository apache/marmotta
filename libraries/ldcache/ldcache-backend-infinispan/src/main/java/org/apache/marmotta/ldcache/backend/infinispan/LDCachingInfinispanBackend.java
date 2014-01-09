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
package org.apache.marmotta.ldcache.backend.infinispan;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.EmptyIteration;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.api.LDCachingConnection;
import org.apache.marmotta.ldcache.backend.infinispan.repository.LDCachingInfinispanRepositoryConnection;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.distribution.ch.SyncConsistentHashFactory;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.openrdf.model.Model;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCachingInfinispanBackend implements LDCachingBackend {

    public static final String LDCACHE_ENTRY_CACHE = "ldcache-entry-cache";
    public static final String LDCACHE_TRIPLE_CACHE = "ldcache-triple-cache";
    private static Logger log = LoggerFactory.getLogger(LDCachingInfinispanBackend.class);

    private EmbeddedCacheManager cacheManager;

    private GlobalConfiguration globalConfiguration;

    private Configuration defaultConfiguration;

    private boolean clustered;

    private Cache<String,CacheEntry> entryCache;
    private Cache<String,Model>      tripleCache;

    /**
     * Create a non-clustered instance of the infinispan cache.
     */
    public LDCachingInfinispanBackend() {
        globalConfiguration = new GlobalConfigurationBuilder()
                .classLoader(LDCachingInfinispanBackend.class.getClassLoader())
                .globalJmxStatistics()
                    .jmxDomain("org.apache.marmotta.ldcache")
                    .allowDuplicateDomains(true)
                .build();

        defaultConfiguration = new ConfigurationBuilder()
                .clustering()
                    .cacheMode(CacheMode.LOCAL)
                .eviction()
                    .strategy(EvictionStrategy.LIRS)
                    .maxEntries(100000)
                .expiration()
                    .lifespan(7, TimeUnit.DAYS)
                    .maxIdle(1, TimeUnit.DAYS)
                .build();

        clustered = false;

    }

    /**
     * Create a clustered instane of the infinispan cache backend using the provided cluster and machine name
     * @param clusterName
     * @param machineName
     */
    public LDCachingInfinispanBackend(String clusterName, String machineName) {
        globalConfiguration = new GlobalConfigurationBuilder()
                .classLoader(LDCachingInfinispanBackend.class.getClassLoader())
                .transport()
                    .defaultTransport()
                    .clusterName(clusterName)
                    .machineId(machineName)
                    .addProperty("configurationFile", "jgroups-kiwi.xml")
                .globalJmxStatistics()
                    .jmxDomain("org.apache.marmotta.ldcache")
                    .allowDuplicateDomains(true)
                .build();



        defaultConfiguration = new ConfigurationBuilder()
                .clustering()
                    .cacheMode(CacheMode.DIST_ASYNC)
                        .async()
                        .asyncMarshalling()
                    .l1()
                        .lifespan(5, TimeUnit.MINUTES)
                    .hash()
                        .numOwners(2)
                        .numSegments(40)
                        .consistentHashFactory(new SyncConsistentHashFactory())
                .stateTransfer()
                    .fetchInMemoryState(false)
                .eviction()
                    .strategy(EvictionStrategy.LIRS)
                    .maxEntries(100000)
                .expiration()
                    .lifespan(7, TimeUnit.DAYS)
                    .maxIdle(1, TimeUnit.DAYS)
                .build();


        clustered = true;


    }


    public Cache<String,CacheEntry> getEntryCache() {
        if(entryCache == null) {
            cacheManager.defineConfiguration(LDCACHE_ENTRY_CACHE, defaultConfiguration);

            entryCache = cacheManager.<String,CacheEntry>getCache(LDCACHE_ENTRY_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }

        return entryCache;

    }

    public Cache<String,Model> getTripleCache() {
        if(tripleCache == null) {
            cacheManager.defineConfiguration(LDCACHE_TRIPLE_CACHE, defaultConfiguration);

            tripleCache = cacheManager.<String,Model>getCache(LDCACHE_TRIPLE_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }

        return tripleCache;

    }

    /**
     * Return a repository connection that can be used for caching. The LDCache will first remove all statements for
     * the newly cached resources and then add retrieved statements as-is to this connection and properly commit and
     * close it after use.
     * <p/>
     * Note that in case the statements should be rewritten this method must take care of providing the proper
     * connection, e.g. by using a ContextAwareRepositoryConnection to add a context to all statements when adding them.
     *
     * @param resource the resource that will be cached
     * @return a repository connection that can be used for storing retrieved triples for caching
     */
    @Override
    public LDCachingConnection getCacheConnection(String resource) throws RepositoryException {
        return new LDCachingInfinispanRepositoryConnection(this, resource);
    }

    /**
     * Return an iterator over all expired cache entries (can e.g. be used for refreshing).
     *
     * @return
     */
    @Override
    public CloseableIteration<CacheEntry, RepositoryException> listExpiredEntries() throws RepositoryException {
        return new EmptyIteration<>();  // Infinispan does not allow listing expired entries
    }

    /**
     * Return an iterator over all cache entries (can e.g. be used for refreshing or expiring).
     *
     * @return
     */
    @Override
    public CloseableIteration<CacheEntry, RepositoryException> listCacheEntries() throws RepositoryException {
        return new CloseableIteratorIteration<>(getEntryCache().values().iterator());
    }

    /**
     * Return true in case the resource is a cached resource.
     *
     * @param resource the URI of the resource to check
     * @return true in case the resource is a cached resource
     */
    @Override
    public boolean isCached(String resource) throws RepositoryException {
        return getEntryCache().containsKey(resource);
    }

    /**
     * Carry out any initialization tasks that might be necessary
     */
    @Override
    public void initialize() {
        cacheManager = new DefaultCacheManager(globalConfiguration, defaultConfiguration, true);

        getEntryCache();
        getTripleCache();

        log.info("initialised cache manager ({})", globalConfiguration.isClustered() ? "cluster name: "+globalConfiguration.transport().clusterName() : "single host");

    }

    /**
     * Shutdown the backend and free all runtime resources.
     */
    @Override
    public void shutdown() {
        log.warn("shutting down cache manager ...");
        if(cacheManager.getTransport() != null) {
            log.info("... shutting down transport ...");
            cacheManager.getTransport().stop();
        }
        log.info("... shutting down main component ...");
        cacheManager.stop();
        log.info("... done!");
    }
}
