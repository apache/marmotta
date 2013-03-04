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
package org.apache.marmotta.ldcache.backend.kiwi;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.api.LDCachingConnection;
import org.apache.marmotta.ldcache.backend.kiwi.persistence.LDCachingKiWiPersistence;
import org.apache.marmotta.ldcache.backend.kiwi.repository.LDCachingSailRepositoryConnection;
import org.apache.marmotta.ldcache.backend.kiwi.sail.LDCachingKiWiSail;
import org.apache.marmotta.ldcache.backend.kiwi.sail.LDCachingKiWiSailConnection;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCachingKiWiBackend implements LDCachingBackend {

    private static Logger log = LoggerFactory.getLogger(LDCachingKiWiBackend.class);



    /**
     * URI used as cache context in the central triple store
     */
    private String cacheContext;


    /**
     * Direct access to the caching SAIL with its caching maintenance functionality.
     */
    private LDCachingKiWiSail sail;


    private LDCachingKiWiPersistence persistence;

    /**
     * Repository API access to the cache data
     */
    private SailRepository repository;

    /**
     * Create a new LDCache KiWi backend using the given store and context for caching triples and storing cache
     * metadata via JDBC in the database.
     *
     * @param store
     * @param cacheContext
     */
    public LDCachingKiWiBackend(KiWiStore store, String cacheContext) {
        this.cacheContext = cacheContext;
        this.sail         = new LDCachingKiWiSail(store);
        this.repository   = new SailRepository(sail);
        this.persistence  = new LDCachingKiWiPersistence(store.getPersistence());
    }

    /**
     * Return a repository connection that can be used for caching. The LDCache will first remove all statements for
     * the newly cached resources and then add retrieved statements as-is to this connection and properly commit and
     * close it after use.
     * <p/>
     * Note that in case the statements should be rewritten this method must take care of providing the proper
     * connection, e.g. by using a ContextAwareRepositoryConnection to add a context to all statements when adding them.
     *
     *
     * @param resource the resource that will be cached
     * @return a repository connection that can be used for storing retrieved triples for caching
     */
    @Override
    public LDCachingConnection getCacheConnection(String resource) throws RepositoryException {
        try {
            LDCachingKiWiSailConnection sailConnection = sail.getConnection();

            return new LDCachingSailRepositoryConnection(repository,sailConnection,cacheContext);
        } catch (SailException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Return an iterator over all expired cache entries (can e.g. be used for refreshing).
     *
     * @return
     */
    @Override
    public CloseableIteration<CacheEntry, RepositoryException> listExpiredEntries()  throws RepositoryException {
        try {
            final LDCachingKiWiSailConnection sailConnection = sail.getConnection();
            sailConnection.begin();

            return new ExceptionConvertingIteration<CacheEntry, RepositoryException>(sailConnection.listExpired()) {
                @Override
                protected RepositoryException convert(Exception e) {
                    return new RepositoryException(e);
                }

                /**
                 * Closes this Iteration as well as the wrapped Iteration if it happens to be
                 * a {@link info.aduna.iteration.CloseableIteration}.
                 */
                @Override
                protected void handleClose() throws RepositoryException {
                    super.handleClose();
                    try {
                        sailConnection.commit();
                        sailConnection.close();
                    } catch (SailException ex) {
                        throw new RepositoryException(ex);
                    }
                }
            };
        } catch (SailException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Return an iterator over all cache entries (can e.g. be used for refreshing or expiring).
     *
     * @return
     */
    @Override
    public CloseableIteration<CacheEntry, RepositoryException> listCacheEntries()  throws RepositoryException {
        try {
            final LDCachingKiWiSailConnection sailConnection = sail.getConnection();
            sailConnection.begin();

            return new ExceptionConvertingIteration<CacheEntry, RepositoryException>(sailConnection.listAll()) {
                @Override
                protected RepositoryException convert(Exception e) {
                    return new RepositoryException(e);
                }

                /**
                 * Closes this Iteration as well as the wrapped Iteration if it happens to be
                 * a {@link info.aduna.iteration.CloseableIteration}.
                 */
                @Override
                protected void handleClose() throws RepositoryException {
                    super.handleClose();
                    try {
                        sailConnection.commit();
                        sailConnection.close();
                    } catch (SailException ex) {
                        throw new RepositoryException(ex);
                    }
                }
            };
        } catch (SailException e) {
            throw new RepositoryException(e);
        }
    }


    public LDCachingKiWiPersistence getPersistence() {
        return persistence;
    }

    /**
     * Carry out any initialization tasks that might be necessary
     */
    @Override
    public void initialize() {
        try {
            repository.initialize();
        } catch (RepositoryException e) {
            log.error("error initializing secondary repository",e);
        }

        try {
            persistence.initDatabase();
        } catch (SQLException e) {
            log.error("error initializing LDCache database tables",e);
        }

        // register cache context in database
        repository.getValueFactory().createURI(cacheContext);

    }

    /**
     * Shutdown the backend and free all runtime resources.
     */
    @Override
    public void shutdown() {
        try {
            repository.shutDown();
        } catch (RepositoryException e) {
            log.error("error shutting down secondary repository",e);
        }
    }


}
