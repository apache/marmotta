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
package org.apache.marmotta.ldcache.api;

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public interface LDCachingService {


    /**
     * Refresh the cached resource passed as argument. The method will do nothing for local
     * resources.
     * Calling the method will carry out the following tasks:
     * 1. check whether the resource is a remote resource; if no, returns immediately
     * 2. check whether the resource has a cache entry; if no, goto 4
     * 3. check whether the expiry time of the cache entry has passed; if no, returns immediately
     * 4. retrieve the triples for the resource from the Linked Data Cloud using the methods offered
     * by the
     * LinkedDataClientService (registered endpoints etc); returns immediately if the result is null
     * or
     * an exception is thrown
     * 5. remove all old triples for the resource and add all new triples for the resource
     * 6. create new expiry information of the cache entry and persist it in the transaction
     * 
     *
     * @param resource
     * @param forceRefresh if <code>true</code> the resource will be refreshed despite the
     */
    public void refreshResource(URI resource, boolean forceRefresh);

    /**
     * Refresh all expired resources by listing the cache entries that have expired and calling refreshResource on
     * them. This method can e.g. be called by a scheduled task to regularly update cache entries to always have
     * the latest version available in the Search Index and elsewhere.
     */
    public void refreshExpired();

    /**
     * Manually expire the caching information for the given resource. The resource will be
     * re-retrieved upon the next access.
     *
     * @param resource the Resource to expire.
     */
    public void expire(URI resource);

    /**
     * Manually expire all cached resources.
     * 
     * @see #expire(org.openrdf.model.URI)
     */
    public void expireAll();


    /**
     * Shutdown the caching service and free all occupied runtime resources.
     */
    public void shutdown();

    /**
     * Return a repository connection that can be used for accessing cached resources.
     *
     * @param  resource the resource that will be cached
     * @return a repository connection that can be used for storing retrieved triples for caching
     */
    LDCachingConnection getCacheConnection(String resource) throws RepositoryException;

    /**
     * Return an iterator over all cache entries (can e.g. be used for refreshing or expiring).
     *
     * @return
     */
    CloseableIteration<CacheEntry, RepositoryException> listCacheEntries() throws RepositoryException;

    /**
     * Return an iterator over all expired cache entries (can e.g. be used for refreshing).
     *
     * @return
     */
    CloseableIteration<CacheEntry, RepositoryException> listExpiredEntries() throws RepositoryException;
}
