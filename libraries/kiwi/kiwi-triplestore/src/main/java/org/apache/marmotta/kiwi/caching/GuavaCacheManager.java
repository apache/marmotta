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

package org.apache.marmotta.kiwi.caching;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A simple implementation of a cache manager using the Guava caching functionality. Does not support clustered
 * operation.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class GuavaCacheManager implements CacheManager {

    private static Logger log = LoggerFactory.getLogger(GuavaCacheManager.class);

    private KiWiConfiguration configuration;

    private Cache<Long,KiWiNode> nodeCache;
    private Cache<Long,KiWiTriple> tripleCache;
    private Cache<String,KiWiUriResource> uriCache;
    private Cache<String,KiWiAnonResource> bnodeCache;
    private Cache<String,KiWiLiteral> literalCache;
    private Cache<String,KiWiNamespace> namespaceUriCache, namespacePrefixCache;
    private ConcurrentHashMap<Long,Long> registryCache;

    private Map<String,Cache> dynamicCaches;


    public GuavaCacheManager(KiWiConfiguration configuration) {
        this.configuration = configuration;

        log.info("initialising Guava in-memory caching backend ...");

        if(configuration.isClustered()) {
            log.warn("clustering not supported by Guava in-memory caching backend; please use Infinispan or Hazelcast instead!");
        }

        nodeCache = CacheBuilder.newBuilder()
                .maximumSize(configuration.getNodeCacheSize())
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();

        tripleCache = CacheBuilder.newBuilder()
                .maximumSize(configuration.getTripleCacheSize())
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();

        uriCache = CacheBuilder.newBuilder()
                .maximumSize(configuration.getUriCacheSize())
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();

        bnodeCache = CacheBuilder.newBuilder()
                .maximumSize(configuration.getBNodeCacheSize())
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();

        literalCache = CacheBuilder.newBuilder()
                .maximumSize(configuration.getLiteralCacheSize())
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();

        namespaceUriCache = CacheBuilder.newBuilder()
                .maximumSize(configuration.getNamespaceCacheSize())
                .expireAfterAccess(1, TimeUnit.DAYS)
                .build();

        namespacePrefixCache = CacheBuilder.newBuilder()
                .maximumSize(configuration.getNamespaceCacheSize())
                .expireAfterAccess(1, TimeUnit.DAYS)
                .build();


        registryCache = new ConcurrentHashMap<>();

        dynamicCaches = new HashMap<>();

    }

    /**
     * Return the node id -> node cache from the cache manager. This cache is heavily used to lookup
     * nodes when querying or loading triples and should therefore have a decent size (default 500.000 elements).
     *
     * @return an EHCache Cache instance containing the node id -> node mappings
     */
    @Override
    public Map<Long, KiWiNode> getNodeCache() {
        return nodeCache.asMap();
    }

    /**
     * Return the triple id -> triple cache from the cache manager. This cache is used for speeding up the
     * construction of query results.
     *
     * @return
     */
    @Override
    public Map<Long, KiWiTriple> getTripleCache() {
        return tripleCache.asMap();
    }

    /**
     * Return the uri -> KiWiUriResource cache from the cache manager. This cache is used when constructing new
     * KiWiUriResources to avoid a database lookup.
     *
     * @return
     */
    @Override
    public Map<String, KiWiUriResource> getUriCache() {
        return uriCache.asMap();
    }

    /**
     * Return the anonId -> KiWiAnonResource cache from the cache manager. This cache is used when constructing new
     * KiWiAnonResources to avoid a database lookup.
     *
     * @return
     */
    @Override
    public Map<String, KiWiAnonResource> getBNodeCache() {
        return bnodeCache.asMap();
    }

    /**
     * Return the literal cache key -> KiWiLiteral cache from the cache manager. This cache is used when constructing new
     * KiWiLiterals to avoid a database lookup.
     *
     * @return
     * @see org.apache.marmotta.commons.sesame.model.LiteralCommons#createCacheKey(String, java.util.Locale, String)
     */
    @Override
    public Map<String, KiWiLiteral> getLiteralCache() {
        return literalCache.asMap();
    }

    /**
     * Return the URI -> namespace cache from the cache manager. Used for looking up namespaces
     *
     * @return
     */
    @Override
    public Map<String, KiWiNamespace> getNamespaceUriCache() {
        return namespaceUriCache.asMap();
    }

    /**
     * Return the prefix -> namespace cache from the cache manager. Used for looking up namespaces
     *
     * @return
     */
    @Override
    public Map<String, KiWiNamespace> getNamespacePrefixCache() {
        return namespacePrefixCache.asMap();
    }

    /**
     * Create and return the cache used by the CacheTripleRegistry. This is an unlimited synchronous replicated
     * cache and should be used with care.
     *
     * @return
     */
    @Override
    public Map<Long, Long> getRegistryCache() {
        return registryCache;
    }

    /**
     * Get the cache with the given name from the cache manager. Can be used to request additional
     * caches from the cache manager that are not covered by explicit methods.
     *
     * @param name
     * @return
     */
    @Override
    public Map getCacheByName(String name) {
        synchronized (dynamicCaches) {
            if(!dynamicCaches.containsKey(name)) {
                dynamicCaches.put(name, CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).maximumSize(100000).build());
            }
            return dynamicCaches.get(name).asMap();
        }
    }

    /**
     * Clear all caches managed by this cache manager.
     */
    @Override
    public void clear() {
        for(Cache c : dynamicCaches.values()) {
            c.invalidateAll();
        }

        for(Cache c : new Cache[] { nodeCache, uriCache, bnodeCache, literalCache, tripleCache, namespacePrefixCache, namespaceUriCache}) {
            c.invalidateAll();
        }

        registryCache.clear();
    }

    /**
     * Shutdown this cache manager instance. Will shutdown the underlying EHCache cache manager.
     */
    @Override
    public void shutdown() {
        dynamicCaches.clear();
    }
}
