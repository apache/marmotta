/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ldcache.backend.kiwi.sail;

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.kiwi.sail.KiWiSailConnection;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.apache.marmotta.ldcache.backend.kiwi.model.KiWiCacheEntry;
import org.apache.marmotta.ldcache.backend.kiwi.persistence.LDCachingKiWiPersistenceConnection;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.apache.marmotta.ldcache.sail.LDCachingSailConnection;
import org.openrdf.model.URI;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

import java.sql.SQLException;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCachingKiWiSailConnection extends SailConnectionWrapper implements LDCachingSailConnection {

    private LDCachingKiWiPersistenceConnection persistence;

    private KiWiSailConnection wrapped;

    public LDCachingKiWiSailConnection(KiWiSailConnection wrappedCon) throws SailException {
        super(wrappedCon);

        this.wrapped = wrappedCon;
        try {
            this.persistence = new LDCachingKiWiPersistenceConnection(wrappedCon.getDatabaseConnection());
        } catch (SQLException e) {
            throw new SailException(e);
        }
    }

    public KiWiValueFactory getValueFactory() {
        return wrapped.getValueFactory();
    }

    /**
     * Store a cache entry for the passed resource in the backend. Depending on the backend, this can be a
     * persistent storage or an in-memory storage.
     *
     * @param resource
     * @param entry
     */
    @Override
    public void addCacheEntry(URI resource, CacheEntry entry) throws SailException {
        try {
            persistence.storeCacheEntry(entry);
        } catch (SQLException e) {
            throw new SailException(e);
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
    public CacheEntry getCacheEntry(URI resource) throws SailException {
        try {
            return persistence.getCacheEntry(resource.stringValue());
        } catch (SQLException e) {
            throw new SailException(e);
        }
    }

    /**
     * Remove the currently stored cache entry for the passed resource from the backend.
     *
     * @param resource
     */
    @Override
    public void removeCacheEntry(URI resource) throws SailException {
        try {
            persistence.removeCacheEntry(resource.stringValue());
        } catch (SQLException e) {
            throw new SailException(e);
        }
    }

    /**
     * List all cache entries with an expiry date older than the current time.
     *
     * @return a closeable iteration with KiWiCacheEntries; needs to be released by the caller
     * @throws SQLException
     */
    public CloseableIteration<KiWiCacheEntry,SQLException> listExpired() throws SailException {
        try {
            return persistence.listExpired();
        } catch (SQLException e) {
            throw new SailException(e);
        }
    }


    /**
     * List all cache entries in the database, regardless of expiry date.
     *
     * @return a closeable iteration with KiWiCacheEntries; needs to be released by the caller
     * @throws SQLException
     */
    public CloseableIteration<KiWiCacheEntry,SQLException> listAll() throws SailException {
        try {
            return persistence.listAll();
        } catch (SQLException e) {
            throw new SailException(e);
        }
    }

}
