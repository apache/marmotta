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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * A class for managing the different caches that are used by the triple store.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiCacheManager {


    private CacheManager cacheManager;

    private KiWiQueryCache queryCache;

    public KiWiCacheManager(String name) {
        cacheManager = CacheManager.newInstance(KiWiCacheManager.class.getResource("/ehcache-kiwi.xml"));
        if(name != null) {
            cacheManager.setName(name);
        }
        queryCache = new KiWiQueryCache(cacheManager.getCache("query-cache"));
    }

    /**
     * Return the node id -> node cache from the cache manager. This cache is heavily used to lookup
     * nodes when querying or loading triples and should therefore have a decent size (default 500.000 elements).
     *
     * @return an EHCache Cache instance containing the node id -> node mappings
     */
    public Cache getNodeCache() {
        return cacheManager.getCache("node-cache");
    }

    /**
     * Return the triple id -> triple cache from the cache manager. This cache is used for speeding up the
     * construction of query results.
     *
     * @return
     */
    public Cache getTripleCache() {
        return cacheManager.getCache("triple-cache");
    }


    /**
     * Return the uri -> KiWiUriResource cache from the cache manager. This cache is used when constructing new
     * KiWiUriResources to avoid a database lookup.
     *
     * @return
     */
    public Cache getUriCache() {
        return cacheManager.getCache("uri-cache");
    }


    /**
     * Return the anonId -> KiWiAnonResource cache from the cache manager. This cache is used when constructing new
     * KiWiAnonResources to avoid a database lookup.
     *
     * @return
     */
    public Cache getBNodeCache() {
        return cacheManager.getCache("bnode-cache");
    }

    /**
     * Return the literal cache key -> KiWiLiteral cache from the cache manager. This cache is used when constructing new
     * KiWiLiterals to avoid a database lookup.
     *
     * @see org.apache.marmotta.commons.sesame.model.LiteralCommons#createCacheKey(String, java.util.Locale, String)
     * @return
     */
    public Cache getLiteralCache() {
        return cacheManager.getCache("literal-cache");
    }


    /**
     * Return the URI -> namespace cache from the cache manager. Used for looking up namespaces
     * @return
     */
    public Cache getNamespaceUriCache() {
        return cacheManager.getCache("namespace-uri-cache");
    }

    /**
     * Return the prefix -> namespace cache from the cache manager. Used for looking up namespaces
     * @return
     */
    public Cache getNamespacePrefixCache() {
        return cacheManager.getCache("namespace-prefix-cache");
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
             cacheManager.addCache(name);
        }
        return cacheManager.getCache(name);

    }


    /**
     * Return the triple query cache, which caches (smaller) listTriples results. This cache is used fairly
     * often, but since the cache elements will often be bigger collections of results, it should not be too
     * big (default 10.000 elements). Since the underlying database might change, cache entries should expire
     * after a certain time (default 1 hour).
     *
     * @return  an EHCache Cache instance containing the query pattern -> query result mappings
     */
    public KiWiQueryCache getQueryCache() {
        return queryCache;
    }


    /**
     * Clear all caches managed by this cache manager.
     */
    public void clear() {
        cacheManager.clearAll();
    }

    /**
     * Shutdown this cache manager instance. Will shutdown the underlying EHCache cache manager.
     */
    public void shutdown() {
        cacheManager.shutdown();
    }
}
