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

package org.apache.marmotta.kiwi.ehcache.caching;

import org.apache.marmotta.kiwi.caching.CacheManager;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.ehcache.util.CacheMap;
import org.apache.marmotta.kiwi.model.rdf.*;

import java.util.Map;

/**
 * KiWi Cache Manager implementation based on EHCache. Best used for single machine production environments.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class EHCacheManager implements CacheManager {


    private net.sf.ehcache.CacheManager cacheManager;

    private CacheMap<Long,KiWiNode> nodeCache;
    private CacheMap<Long,KiWiTriple> tripleCache;
    private CacheMap<String,KiWiUriResource> uriCache;
    private CacheMap<String,KiWiAnonResource> bnodeCache;
    private CacheMap<String,KiWiLiteral> literalCache;
    private CacheMap<String,KiWiNamespace> nsPrefixCache;
    private CacheMap<String,KiWiNamespace> nsUriCache;

    private CacheMap<Long,Long> registryCache;

    public EHCacheManager(KiWiConfiguration configuration) {
        cacheManager = net.sf.ehcache.CacheManager.newInstance(EHCacheManager.class.getResource("/ehcache-kiwi.xml"));
        if(configuration.getClusterName() != null) {
            cacheManager.setName(configuration.getClusterName());
        }
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
            nodeCache = new CacheMap<>(cacheManager.getCache((NODE_CACHE)));
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
            tripleCache = new CacheMap<>(cacheManager.getCache((TRIPLE_CACHE)));
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
            uriCache = new CacheMap<>(cacheManager.getCache((URI_CACHE)));
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
            bnodeCache = new CacheMap<>(cacheManager.getCache((BNODE_CACHE)));
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
            literalCache = new CacheMap<>(cacheManager.getCache((LITERAL_CACHE)));
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
            nsUriCache = new CacheMap<>(cacheManager.getCache((NS_URI_CACHE)));
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
            nsPrefixCache = new CacheMap<>(cacheManager.getCache((NS_PREFIX_CACHE)));
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
            registryCache = new CacheMap<>(cacheManager.getCache((REGISTRY_CACHE)));
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
        return new CacheMap(cacheManager.getCache((name)));
    }

    /**
     * Clear all caches managed by this cache manager.
     */
    @Override
    public void clear() {
        cacheManager.clearAll();
    }

    /**
     * Shutdown this cache manager instance. Will shutdown the underlying EHCache cache manager.
     */
    @Override
    public void shutdown() {
        cacheManager.shutdown();
    }
}
