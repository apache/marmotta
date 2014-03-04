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
import org.apache.marmotta.kiwi.model.rdf.*;

import java.util.Map;

/**
 * Implementation of an Infinispan cache manager with a remote (client-server) cache.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class InfinispanRemoteCacheManager implements CacheManager {

    private KiWiConfiguration configuration;

    public InfinispanRemoteCacheManager(KiWiConfiguration configuration) {
        this.configuration = configuration;
    }


    /**
     * Return the node id -> node cache from the cache manager. This cache is heavily used to lookup
     * nodes when querying or loading triples and should therefore have a decent size (default 500.000 elements).
     *
     * @return an EHCache Cache instance containing the node id -> node mappings
     */
    @Override
    public Map<Long, KiWiNode> getNodeCache() {
        return null;
    }

    /**
     * Return the triple id -> triple cache from the cache manager. This cache is used for speeding up the
     * construction of query results.
     *
     * @return
     */
    @Override
    public Map<Long, KiWiTriple> getTripleCache() {
        return null;
    }

    /**
     * Return the uri -> KiWiUriResource cache from the cache manager. This cache is used when constructing new
     * KiWiUriResources to avoid a database lookup.
     *
     * @return
     */
    @Override
    public Map<String, KiWiUriResource> getUriCache() {
        return null;
    }

    /**
     * Return the anonId -> KiWiAnonResource cache from the cache manager. This cache is used when constructing new
     * KiWiAnonResources to avoid a database lookup.
     *
     * @return
     */
    @Override
    public Map<String, KiWiAnonResource> getBNodeCache() {
        return null;
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
        return null;
    }

    /**
     * Return the URI -> namespace cache from the cache manager. Used for looking up namespaces
     *
     * @return
     */
    @Override
    public Map<String, KiWiNamespace> getNamespaceUriCache() {
        return null;
    }

    /**
     * Return the prefix -> namespace cache from the cache manager. Used for looking up namespaces
     *
     * @return
     */
    @Override
    public Map<String, KiWiNamespace> getNamespacePrefixCache() {
        return null;
    }

    /**
     * Create and return the cache used by the CacheTripleRegistry. This is an unlimited synchronous replicated
     * cache and should be used with care.
     *
     * @return
     */
    @Override
    public Map<Long, Long> getRegistryCache() {
        return null;
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
        return null;
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

    }
}
