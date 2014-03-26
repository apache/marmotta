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

package org.apache.marmotta.kiwi.infinispan.remote;

import org.apache.marmotta.kiwi.caching.CacheManager;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.infinispan.util.AsyncMap;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Implementation of an Infinispan cache manager with a remote (client-server) cache.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class InfinispanRemoteCacheManager implements CacheManager {

    private static Logger log = LoggerFactory.getLogger(InfinispanRemoteCacheManager.class);

    private KiWiConfiguration configuration;

    private RemoteCacheManager cacheManager;

    private Map nodeCache, tripleCache, uriCache, literalCache, bnodeCache, nsPrefixCache, nsUriCache, registryCache;


    public InfinispanRemoteCacheManager(KiWiConfiguration configuration) {
        this.configuration = configuration;

        Configuration remoteCfg = new ConfigurationBuilder()
                .addServer()
                    .host(configuration.getClusterAddress())
                    .port(configuration.getClusterPort())
                .marshaller(new CustomJBossMarshaller())
                .socketTimeout(configuration.getClusterTimeout())
                .connectionTimeout(configuration.getClusterTimeout())
                .build();

        cacheManager = new RemoteCacheManager(remoteCfg);

        log.info("initialised Infinispan remote cache manager (servers: {})",  configuration.getClusterAddress());
    }


    /**
     * Return the node id -> node cache from the cache manager. This cache is heavily used to lookup
     * nodes when querying or loading triples and should therefore have a decent size (default 500.000 elements).
     *
     * @return an EHCache Cache instance containing the node id -> node mappings
     */
    @Override
    public Map<Long, KiWiNode> getNodeCache() {
        if(nodeCache == null) {
            nodeCache = new AsyncMap(cacheManager.getCache(NODE_CACHE));
        }
        return nodeCache;
    }

    /**
     * Return the triple id -> triple cache from the cache manager. This cache is used for speeding up the
     * construction of query results.
     *
     * @return
     */
    @Override
    public Map<Long, KiWiTriple> getTripleCache() {
        if(tripleCache == null) {
            tripleCache = new AsyncMap(cacheManager.getCache(TRIPLE_CACHE));
        }
        return tripleCache;
    }

    /**
     * Return the uri -> KiWiUriResource cache from the cache manager. This cache is used when constructing new
     * KiWiUriResources to avoid a database lookup.
     *
     * @return
     */
    @Override
    public Map<String, KiWiUriResource> getUriCache() {
        if(uriCache == null) {
            uriCache = new AsyncMap(cacheManager.getCache(URI_CACHE));
        }
        return uriCache;
    }

    /**
     * Return the anonId -> KiWiAnonResource cache from the cache manager. This cache is used when constructing new
     * KiWiAnonResources to avoid a database lookup.
     *
     * @return
     */
    @Override
    public Map<String, KiWiAnonResource> getBNodeCache() {
        if(bnodeCache == null) {
            bnodeCache = new AsyncMap(cacheManager.getCache(BNODE_CACHE));
        }
        return bnodeCache;
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
        if(literalCache == null) {
            literalCache = new AsyncMap(cacheManager.getCache(LITERAL_CACHE));
        }
        return literalCache;
    }

    /**
     * Return the URI -> namespace cache from the cache manager. Used for looking up namespaces
     *
     * @return
     */
    @Override
    public Map<String, KiWiNamespace> getNamespaceUriCache() {
        if(nsUriCache == null) {
            nsUriCache = new AsyncMap(cacheManager.getCache(NS_URI_CACHE));
        }
        return nsUriCache;
    }

    /**
     * Return the prefix -> namespace cache from the cache manager. Used for looking up namespaces
     *
     * @return
     */
    @Override
    public Map<String, KiWiNamespace> getNamespacePrefixCache() {
        if(nsPrefixCache == null) {
            nsPrefixCache = new AsyncMap(cacheManager.getCache(NS_PREFIX_CACHE));
        }
        return nsPrefixCache;
    }

    /**
     * Create and return the cache used by the CacheTripleRegistry. This is an unlimited synchronous replicated
     * cache and should be used with care.
     *
     * @return
     */
    @Override
    public Map<Long, Long> getRegistryCache() {
        if(registryCache == null) {
            registryCache = cacheManager.getCache(REGISTRY_CACHE);
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
    @Override
    public Map getCacheByName(String name) {
        return cacheManager.getCache(name);
    }

    /**
     * Clear all caches managed by this cache manager.
     */
    @Override
    public void clear() {

    }

    /**
     * Shutdown this cache manager instance. Will shutdown the underlying EHCache cache manager.
     */
    @Override
    public void shutdown() {
        try {
            log.info("shutting down Infinispan remote cache manager ...");
            cacheManager.stop();

            while(cacheManager.isStarted()) {
                log.info("waiting 100ms for cache manager to come down ...");
                Thread.sleep(100);
            }
        } catch (CacheException | InterruptedException ex) {
            log.warn("error shutting down cache: {}", ex.getMessage());
        }
    }
}
