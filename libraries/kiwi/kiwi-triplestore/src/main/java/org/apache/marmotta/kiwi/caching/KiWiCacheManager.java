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
package org.apache.marmotta.kiwi.caching;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.infinispan.Cache;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.distribution.ch.SyncConsistentHashFactory;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A class for managing the different caches that are used by the triple store.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiCacheManager {

    private static Logger log = LoggerFactory.getLogger(KiWiCacheManager.class);

    public static final String NODE_CACHE = "node-cache";
    public static final String TRIPLE_CACHE = "triple-cache";
    public static final String URI_CACHE = "uri-cache";
    public static final String BNODE_CACHE = "bnode-cache";
    public static final String LITERAL_CACHE = "literal-cache";
    public static final String NAMESPACE_URI_CACHE = "namespace-uri-cache";
    public static final String NAMESPACE_PREFIX_CACHE = "namespace-prefix-cache";
    public static final String LOADER_CACHE = "loader-cache";
    public static final String REGISTRY_CACHE = "registry-cache";

    private EmbeddedCacheManager cacheManager;

    private GlobalConfiguration globalConfiguration;

    // default configuration: distributed cache, 100000 entries, 300 seconds expiration, 60 seconds idle
    private Configuration defaultConfiguration;

    private boolean clustered, embedded;

    private KiWiConfiguration kiWiConfiguration;


    private Cache nodeCache, tripleCache, uriCache, literalCache, bnodeCache, nsPrefixCache, nsUriCache, loaderCache, registryCache;


    /**
     * Create a new cache manager with its own automatically created Infinispan instance.
     *
     * @param config
     */
    public KiWiCacheManager(KiWiConfiguration config, AdvancedExternalizer...externalizers) {

        this.clustered = config.isClustered();
        this.kiWiConfiguration = config;

        if(clustered) {
            globalConfiguration = new GlobalConfigurationBuilder()
                    .classLoader(KiWiCacheManager.class.getClassLoader())
                    .transport()
                        .defaultTransport()
                        .clusterName(config.getClusterName())
                        .machineId("instance-" + config.getDatacenterId())
                        .addProperty("configurationFile", "jgroups-kiwi.xml")
                    .globalJmxStatistics()
                        .jmxDomain("org.apache.marmotta.kiwi")
                        .allowDuplicateDomains(true)
                    .serialization()
                        .addAdvancedExternalizer(externalizers)
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
                        .lifespan(30, TimeUnit.MINUTES)
                        .maxIdle(10, TimeUnit.MINUTES)
                    .build();
        } else {
            globalConfiguration = new GlobalConfigurationBuilder()
                    .classLoader(KiWiCacheManager.class.getClassLoader())
                    .globalJmxStatistics()
                        .jmxDomain("org.apache.marmotta.kiwi")
                        .allowDuplicateDomains(true)
                    .build();

            defaultConfiguration = new ConfigurationBuilder()
                    .clustering()
                        .cacheMode(CacheMode.LOCAL)
                    .eviction()
                        .strategy(EvictionStrategy.LIRS)
                        .maxEntries(100000)
                    .expiration()
                        .lifespan(5, TimeUnit.MINUTES)
                        .maxIdle(1, TimeUnit.MINUTES)
                    .build();

        }


        cacheManager = new DefaultCacheManager(globalConfiguration, defaultConfiguration, true);

        log.info("initialised cache manager ({})", globalConfiguration.isClustered() ? "cluster name: "+globalConfiguration.transport().clusterName() : "single host");

        this.embedded = true;
    }

    /**
     * Create a cache manager from an existing Infinispan cache manager.
     *
     * @param cacheManager
     * @param kiWiConfiguration
     */
    public KiWiCacheManager(EmbeddedCacheManager cacheManager, KiWiConfiguration kiWiConfiguration, AdvancedExternalizer...externalizers) {
        this.cacheManager = cacheManager;
        this.globalConfiguration = cacheManager.getCacheManagerConfiguration();
        this.defaultConfiguration = cacheManager.getDefaultCacheConfiguration();
        this.kiWiConfiguration = kiWiConfiguration;

        this.clustered = kiWiConfiguration.isClustered();

        for(AdvancedExternalizer e : externalizers) {
            this.globalConfiguration.serialization().advancedExternalizers().put(e.getId(), e);
        }

        log.info("initialised cache manager ({})", globalConfiguration.isClustered() ? "cluster name: "+globalConfiguration.transport().clusterName() : "single host");

        this.embedded = false;
    }

    /**
     * Return the node id -> node cache from the cache manager. This cache is heavily used to lookup
     * nodes when querying or loading triples and should therefore have a decent size (default 500.000 elements).
     *
     * @return an EHCache Cache instance containing the node id -> node mappings
     */
    public Cache getNodeCache() {
        if(nodeCache == null) {
            Configuration nodeConfiguration = new ConfigurationBuilder().read(defaultConfiguration)
                    .eviction()
                        .maxEntries(500000)
                    .build();
            cacheManager.defineConfiguration(NODE_CACHE, nodeConfiguration);

            nodeCache = cacheManager.getCache(NODE_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }

        return nodeCache;
    }

    /**
     * Return the triple id -> triple cache from the cache manager. This cache is used for speeding up the
     * construction of query results.
     *
     * @return
     */
    public Cache getTripleCache() {
        if(tripleCache == null) {
            Configuration tripleConfiguration = new ConfigurationBuilder().read(defaultConfiguration)
                    .clustering()
                        .cacheMode(CacheMode.LOCAL)
                    .eviction()
                        .maxEntries(kiWiConfiguration.getTripleCacheSize())
                    .expiration()
                        .lifespan(60, TimeUnit.MINUTES)
                        .maxIdle(30, TimeUnit.MINUTES)
                    .build();
            cacheManager.defineConfiguration(TRIPLE_CACHE, tripleConfiguration);

            tripleCache = cacheManager.getCache(TRIPLE_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }
        return tripleCache;
    }


    /**
     * Return the uri -> KiWiUriResource cache from the cache manager. This cache is used when constructing new
     * KiWiUriResources to avoid a database lookup.
     *
     * @return
     */
    public Cache getUriCache() {
        if(uriCache == null) {
            Configuration uriConfiguration = new ConfigurationBuilder().read(defaultConfiguration)
                    .eviction()
                        .maxEntries(kiWiConfiguration.getUriCacheSize())
                    .build();
            cacheManager.defineConfiguration(URI_CACHE, uriConfiguration);

            uriCache = cacheManager.getCache(URI_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }
        return uriCache;
    }


    /**
     * Return the anonId -> KiWiAnonResource cache from the cache manager. This cache is used when constructing new
     * KiWiAnonResources to avoid a database lookup.
     *
     * @return
     */
    public Cache getBNodeCache() {
        if(bnodeCache == null) {
            Configuration bnodeConfiguration = new ConfigurationBuilder().read(defaultConfiguration)
                    .eviction()
                        .maxEntries(kiWiConfiguration.getBNodeCacheSize())
                    .build();
            cacheManager.defineConfiguration(BNODE_CACHE, bnodeConfiguration);

            bnodeCache = cacheManager.getCache(BNODE_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }
        return bnodeCache;
    }

    /**
     * Return the literal cache key -> KiWiLiteral cache from the cache manager. This cache is used when constructing new
     * KiWiLiterals to avoid a database lookup.
     *
     * @see org.apache.marmotta.commons.sesame.model.LiteralCommons#createCacheKey(String, java.util.Locale, String)
     * @return
     */
    public Cache getLiteralCache() {
        if(literalCache == null) {
            Configuration literalConfiguration = new ConfigurationBuilder().read(defaultConfiguration)
                    .eviction()
                        .maxEntries(kiWiConfiguration.getLiteralCacheSize())
                    .build();
            cacheManager.defineConfiguration(LITERAL_CACHE, literalConfiguration);

            literalCache = cacheManager.getCache(LITERAL_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }
        return literalCache;
    }


    /**
     * Return the URI -> namespace cache from the cache manager. Used for looking up namespaces
     * @return
     */
    public Cache getNamespaceUriCache() {
        if(nsUriCache == null) {
            if(clustered) {
                Configuration nsuriConfiguration = new ConfigurationBuilder().read(defaultConfiguration)
                        .clustering()
                            .cacheMode(CacheMode.REPL_ASYNC)
                        .eviction()
                            .maxEntries(kiWiConfiguration.getNamespaceCacheSize())
                        .expiration()
                            .lifespan(1, TimeUnit.DAYS)
                        .build();
                cacheManager.defineConfiguration(NAMESPACE_URI_CACHE, nsuriConfiguration);
            } else {
                Configuration nsuriConfiguration = new ConfigurationBuilder().read(defaultConfiguration)
                        .eviction()
                            .maxEntries(kiWiConfiguration.getNamespaceCacheSize())
                        .expiration()
                            .lifespan(1, TimeUnit.HOURS)
                        .build();
                cacheManager.defineConfiguration(NAMESPACE_URI_CACHE, nsuriConfiguration);
            }

            nsUriCache = cacheManager.getCache(NAMESPACE_URI_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }
        return nsUriCache;
    }

    /**
     * Return the prefix -> namespace cache from the cache manager. Used for looking up namespaces
     * @return
     */
    public Cache getNamespacePrefixCache() {
        if(nsPrefixCache == null) {
            if(clustered) {
                Configuration nsprefixConfiguration = new ConfigurationBuilder().read(defaultConfiguration)
                        .clustering()
                            .cacheMode(CacheMode.REPL_ASYNC)
                        .eviction()
                            .maxEntries(kiWiConfiguration.getNamespaceCacheSize())
                        .expiration()
                            .lifespan(1, TimeUnit.DAYS)
                        .build();
                cacheManager.defineConfiguration(NAMESPACE_PREFIX_CACHE, nsprefixConfiguration);

            } else {
                Configuration nsprefixConfiguration = new ConfigurationBuilder().read(defaultConfiguration)
                        .eviction()
                            .maxEntries(kiWiConfiguration.getNamespaceCacheSize())
                        .expiration()
                            .lifespan(1, TimeUnit.HOURS)
                        .build();
                cacheManager.defineConfiguration(NAMESPACE_PREFIX_CACHE, nsprefixConfiguration);

            }
            nsPrefixCache = cacheManager.getCache(NAMESPACE_PREFIX_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }
        return nsPrefixCache;
    }


    /**
     * Return the cache used by the KiWiLoader. Used for mapping from Sesame nodes to KiWi nodes.
     * @return
     */
    public Cache getLoaderCache() {
        if(loaderCache == null) {
            Configuration loaderConfiguration = new ConfigurationBuilder().read(defaultConfiguration)
                    .eviction()
                        .maxEntries(100000)
                    .expiration()
                        .lifespan(10, TimeUnit.MINUTES)
                        .maxIdle(30, TimeUnit.SECONDS)
                    .build();
            cacheManager.defineConfiguration(LOADER_CACHE, loaderConfiguration);

            loaderCache = cacheManager.getCache(LOADER_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }
        return loaderCache;
    }


    /**
     * Create and return the cache used by the CacheTripleRegistry. This is an unlimited synchronous replicated
     * cache and should be used with care.
     * @return
     */
    public Cache getRegistryCache() {
        if(registryCache == null) {
            if(clustered) {
                Configuration registryConfiguration = new ConfigurationBuilder()
                    .clustering()
                        .cacheMode(CacheMode.REPL_SYNC)
                        .sync()
                            .replTimeout(15, TimeUnit.SECONDS)
                    .eviction()
                        .strategy(EvictionStrategy.NONE)
                    .build();
                cacheManager.defineConfiguration(REGISTRY_CACHE, registryConfiguration);
            } else {
                Configuration registryConfiguration = new ConfigurationBuilder()
                    .clustering()
                        .cacheMode(CacheMode.LOCAL)
                    .eviction()
                        .strategy(EvictionStrategy.NONE)
                    .build();
                cacheManager.defineConfiguration(REGISTRY_CACHE, registryConfiguration);
            }

            registryCache = cacheManager.getCache(REGISTRY_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }
        return registryCache;
    }

    /**
     * Get the cache with the given name from the cache manager. Can be used to request additional
     * caches from the cache manager that are not covered by explicit methods.
     *
     * @param name
     * @return
     */
    public synchronized Cache getCacheByName(String name) {
        if(!cacheManager.cacheExists(name)) {
            cacheManager.defineConfiguration(name, new ConfigurationBuilder().read(defaultConfiguration).build());
        }
        return cacheManager.getCache(name).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);

    }

    /**
     * Return the Infinispan cache manager used by the caching infrastructure.
     *
     * @return
     */
    public EmbeddedCacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * Return the global cache manager configuration used by the caching infrastructure.
     * @return
     */
    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    /**
     * Return the default cache configuration used by the caching infrastructure.
     * @return
     */
    public Configuration getDefaultConfiguration() {
        return defaultConfiguration;
    }

    /**
     * Clear all caches managed by this cache manager.
     */
    public void clear() {
        Set<String> set =  cacheManager.getCacheNames();
        Iterator<String> iterator =  set.iterator();
        while(iterator.hasNext()){
            String cacheName = iterator.next();
            Cache<String,Object> cache = cacheManager.getCache(cacheName);
            cache.clear();
        }

        nodeCache     = null;
        tripleCache   = null;
        uriCache      = null;
        literalCache  = null;
        bnodeCache    = null;
        nsPrefixCache = null;
        nsUriCache    = null;
        loaderCache   = null;
        registryCache = null;
    }

    /**
     * Shutdown this cache manager instance. Will shutdown the underlying EHCache cache manager.
     */
    public void shutdown() {
        if(embedded && cacheManager.getStatus() == ComponentStatus.RUNNING) {
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
}
