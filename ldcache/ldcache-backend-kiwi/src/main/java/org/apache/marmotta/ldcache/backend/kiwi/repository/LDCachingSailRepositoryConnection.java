/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ldcache.backend.kiwi.repository;

import org.apache.marmotta.kiwi.contextaware.ContextAwareSailConnection;
import org.apache.marmotta.ldcache.api.LDCachingConnection;
import org.apache.marmotta.ldcache.backend.kiwi.sail.LDCachingKiWiSailConnection;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.apache.marmotta.ldcache.sail.LDCachingSailConnection;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;

/**
 * This is an extension wrapper around sail repository connections that allows delegating the additional cache entry
 * methods to the underlying SAIL repository. Otherwise it behaves like any SailRepositoryConnection.
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCachingSailRepositoryConnection extends SailRepositoryConnection implements LDCachingConnection {

    private LDCachingSailConnection cacheConnection;

    public LDCachingSailRepositoryConnection(SailRepository repository, LDCachingKiWiSailConnection sailConnection, String cacheContext) {
        super(repository, new ContextAwareSailConnection(sailConnection, sailConnection.getValueFactory().createURI(cacheContext)));
        cacheConnection = sailConnection;
    }

    /**
     * Store a cache entry for the passed resource in the backend. Depending on the backend, this can be a
     * persistent storage or an in-memory storage.
     *
     * @param resource
     * @param entry
     */
    @Override
    public void addCacheEntry(URI resource, CacheEntry entry) throws RepositoryException {
        try {
            cacheConnection.addCacheEntry(resource,entry);
        } catch (org.openrdf.sail.SailException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Get the cache entry for the passed resource, if any. Returns null in case there is no cache entry.
     *
     *
     * @param resource the resource to look for
     * @return the cache entry for the resource, or null if the resource has never been cached or is expired
     */
    @Override
    public CacheEntry getCacheEntry(URI resource) throws RepositoryException {
        try {
            return cacheConnection.getCacheEntry(resource);
        } catch (org.openrdf.sail.SailException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Remove the currently stored cache entry for the passed resource from the backend.
     *
     * @param resource
     */
    @Override
    public void removeCacheEntry(URI resource) throws RepositoryException {
        try {
            cacheConnection.removeCacheEntry(resource);
        } catch (org.openrdf.sail.SailException e) {
            throw new RepositoryException(e);
        }
    }

}
