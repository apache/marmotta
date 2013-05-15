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
import org.openrdf.repository.RepositoryException;

/**
 * This interface defines the API for different backends for storing the caching results.
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public interface LDCachingBackend {

    /**
     * Return a repository connection that can be used for caching. The LDCache will first remove all statements for
     * the newly cached resources and then add retrieved statements as-is to this connection and properly commit and
     * close it after use.
     * <p/>
     * Note that in case the statements should be rewritten this method must take care of providing the proper
     * connection, e.g. by using a ContextAwareRepositoryConnection to add a context to all statements when adding them.
     *
     *
     * @param  resource the resource that will be cached
     * @return a repository connection that can be used for storing retrieved triples for caching
     */
    public LDCachingConnection getCacheConnection(String resource) throws RepositoryException;


    /**
     * Return an iterator over all expired cache entries (can e.g. be used for refreshing).
     *
     * @return
     */
    public CloseableIteration<CacheEntry,RepositoryException> listExpiredEntries() throws RepositoryException;


    /**
     * Return an iterator over all cache entries (can e.g. be used for refreshing or expiring).
     *
     * @return
     */
    public CloseableIteration<CacheEntry,RepositoryException> listCacheEntries() throws RepositoryException;


    /**
     * Return true in case the resource is a cached resource.
     *
     * @param resource the URI of the resource to check
     * @return true in case the resource is a cached resource
     */
    public boolean isCached(String resource)  throws RepositoryException;

    /**
     * Carry out any initialization tasks that might be necessary
     */
    public void initialize();

    /**
     * Shutdown the backend and free all runtime resources.
     */
    public void shutdown();
}
