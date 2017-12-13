/*
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
package org.apache.marmotta.ldcache.api;

import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public interface LDCachingConnection extends RepositoryConnection {


    /**
     * Get the cache entry for the passed resource, if any. Returns null in case there is no cache entry.
     *
     *
     * @param resource the resource to look for
     * @return the cache entry for the resource, or null if the resource has never been cached or is expired
     */
    CacheEntry getCacheEntry(URI resource) throws RepositoryException;

    /**
     * Store a cache entry for the passed resource in the backend. Depending on the backend, this can be a
     * persistent storage or an in-memory storage.
     *
     * @param resource
     * @param entry
     */
    void addCacheEntry(URI resource, CacheEntry entry) throws RepositoryException;


    /**
     * Remove the currently stored cache entry for the passed resource from the backend.
     * @param resource
     */
    void removeCacheEntry(URI resource) throws RepositoryException;


}
