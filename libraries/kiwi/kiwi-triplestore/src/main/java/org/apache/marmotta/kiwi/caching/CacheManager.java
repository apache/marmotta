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

import org.apache.marmotta.kiwi.model.rdf.*;

import java.util.Map;

/**
 * A generic cache manager API implemented by different caching backends. Each cache should be made accessible
 * using the Java Map interface.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface CacheManager {

    // cache name constants
    String NODE_CACHE =      "node-cache";
    String TRIPLE_CACHE =    "triple-cache";
    String URI_CACHE =       "uri-cache";
    String BNODE_CACHE =     "bnode-cache";
    String LITERAL_CACHE =   "literal-cache";
    String NS_URI_CACHE =    "namespace-uri-cache";
    String NS_PREFIX_CACHE = "namespace-prefix-cache";
    String REGISTRY_CACHE =  "registry-cache";


    /**
     * Return the node id -> node cache from the cache manager. This cache is heavily used to lookup
     * nodes when querying or loading triples and should therefore have a decent size (default 500.000 elements).
     *
     * @return an EHCache Cache instance containing the node id -> node mappings
     */
    Map<Long, KiWiNode> getNodeCache();


    /**
     * Return the triple id -> triple cache from the cache manager. This cache is used for speeding up the
     * construction of query results.
     *
     * @return
     */
    Map<Long, KiWiTriple> getTripleCache();


    /**
     * Return the uri -> KiWiUriResource cache from the cache manager. This cache is used when constructing new
     * KiWiUriResources to avoid a database lookup.
     *
     * @return
     */
    Map<String, KiWiUriResource> getUriCache();


    /**
     * Return the anonId -> KiWiAnonResource cache from the cache manager. This cache is used when constructing new
     * KiWiAnonResources to avoid a database lookup.
     *
     * @return
     */
    Map<String, KiWiAnonResource> getBNodeCache();



    /**
     * Return the literal cache key -> KiWiLiteral cache from the cache manager. This cache is used when constructing new
     * KiWiLiterals to avoid a database lookup.
     *
     * @see org.apache.marmotta.commons.sesame.model.LiteralCommons#createCacheKey(String, java.util.Locale, String)
     * @return
     */
    Map<String, KiWiLiteral> getLiteralCache();


    /**
     * Return the URI -> namespace cache from the cache manager. Used for looking up namespaces
     * @return
     */
    Map<String, KiWiNamespace> getNamespaceUriCache();


    /**
     * Return the prefix -> namespace cache from the cache manager. Used for looking up namespaces
     * @return
     */
    Map<String, KiWiNamespace> getNamespacePrefixCache();


    /**
     * Create and return the cache used by the CacheTripleRegistry. This is an unlimited synchronous replicated
     * cache and should be used with care.
     * @return
     */
    Map<Long,Long> getRegistryCache();



    /**
     * Get the cache with the given name from the cache manager. Can be used to request additional
     * caches from the cache manager that are not covered by explicit methods.
     *
     * @param name
     * @return
     */
    Map getCacheByName(String name);


    /**
     * Clear all caches managed by this cache manager.
     */
    void clear();

    /**
     * Shutdown this cache manager instance. Will shutdown the underlying EHCache cache manager.
     */
    void shutdown();
}
