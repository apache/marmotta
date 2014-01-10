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

package org.apache.marmotta.ldcache.backend.kiwi;

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.ldcache.api.LDCachingBackendNG;
import org.apache.marmotta.ldcache.backend.kiwi.model.KiWiCacheEntry;
import org.apache.marmotta.ldcache.backend.kiwi.persistence.LDCachingKiWiPersistence;
import org.apache.marmotta.ldcache.backend.kiwi.persistence.LDCachingKiWiPersistenceConnection;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryWrapper;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.helpers.SailWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCachingKiWiBackendNG implements LDCachingBackendNG {

    private static Logger log = LoggerFactory.getLogger(LDCachingKiWiBackendNG.class);



    /**
     * URI used as cache context in the central triple store
     */
    private String cacheContext;



    protected LDCachingKiWiPersistence persistence;

    /**
     * Repository API access to the cache data
     */
    protected Repository repository;


    /**
     * Create a new LDCache KiWi backend using the given store and context for caching triples and storing cache
     * metadata via JDBC in the database.
     *
     * @param cacheContext
     */
    public LDCachingKiWiBackendNG(Repository repository, String cacheContext) {
        this.cacheContext = cacheContext;
        this.repository   = repository;
        this.persistence  = new LDCachingKiWiPersistence(getStore(repository).getPersistence());
    }


    protected KiWiStore getStore(Repository repository) {
        if(repository instanceof SailRepository) {
            return getStore(((SailRepository) repository).getSail());
        } else if(repository instanceof RepositoryWrapper) {
            return getStore(((RepositoryWrapper) repository).getDelegate());
        } else {
            throw new IllegalArgumentException("the repository is not backed by a KiWiStore");
        }
    }


    /**
     * Get the root sail in the wrapped sail stack
     * @param sail
     * @return
     */
    protected KiWiStore getStore(Sail sail) {
        if(sail instanceof KiWiStore) {
            return (KiWiStore) sail;
        } else if(sail instanceof SailWrapper) {
            return getStore(((SailWrapper) sail).getBaseSail());
        } else {
            throw new IllegalArgumentException("root sail is not a KiWiStore or could not be found");
        }
    }


    /**
     * Return the cache entry for the given resource, or null if this entry does not exist.
     *
     *
     * @param resource the resource to retrieve the cache entry for
     * @return
     */
    @Override
    public CacheEntry getEntry(URI resource) {
        try {
            try(LDCachingKiWiPersistenceConnection dbcon = persistence.getConnection()) {

                // load cache entry from database
                CacheEntry ce = dbcon.getCacheEntry(resource.stringValue());

                // if entry exists, load triples for the resource from the cache context of the repository
                if(ce != null) {
                    RepositoryConnection con = repository.getConnection();
                    try {
                        con.begin();

                        Model triples = new TreeModel();
                        ModelCommons.add(triples,con.getStatements(resource,null,null,true,con.getValueFactory().createURI(cacheContext)));
                        ce.setTriples(triples);

                        con.commit();
                    } catch(RepositoryException ex) {
                        con.rollback();
                    } finally {
                        con.close();
                    }
                }
                return ce;

            }

        } catch (RepositoryException | SQLException e) {
            log.error("could not retrieve cached triples from repository",e);
        }

        return null;
    }

    /**
     * Update the cache entry for the given resource with the given entry.
     *
     * @param resource the resource to update
     * @param entry    the entry for the resource
     */
    @Override
    public void putEntry(URI resource, CacheEntry entry) {
        try {
            try(LDCachingKiWiPersistenceConnection dbcon = persistence.getConnection()) {

                // store cache entry in database
                dbcon.removeCacheEntry(resource.stringValue());

                // update triples in cache
                RepositoryConnection con = repository.getConnection();
                try {
                    con.begin();

                    con.remove(resource,null,null,con.getValueFactory().createURI(cacheContext));
                    con.add(entry.getTriples(),con.getValueFactory().createURI(cacheContext));

                    con.commit();

                    entry.setResource(con.getValueFactory().createURI(resource.stringValue()));

                    dbcon.storeCacheEntry(entry);
                } catch(RepositoryException ex) {
                    con.rollback();
                } finally {
                    con.close();
                }

            }

        } catch (RepositoryException | SQLException e) {
            log.error("could not retrieve cached triples from repository",e);
        }

    }

    /**
     * Remove the cache entry for the given resource if it exists. Does nothing otherwise.
     *
     * @param resource the resource to remove the entry for
     */
    @Override
    public void removeEntry(URI resource) {
        try {
            try(LDCachingKiWiPersistenceConnection dbcon = persistence.getConnection()) {

                // store cache entry in database
                dbcon.removeCacheEntry(resource.stringValue());

                // update triples in cache
                RepositoryConnection con = repository.getConnection();
                try {
                    con.begin();

                    con.remove(resource, null, null, con.getValueFactory().createURI(cacheContext));

                    con.commit();
                } catch(RepositoryException ex) {
                    con.rollback();
                } finally {
                    con.close();
                }

            }

        } catch (RepositoryException | SQLException e) {
            log.error("could not retrieve cached triples from repository",e);
        }
    }

    /**
     * Clear all entries in the cache backend.
     */
    @Override
    public void clear() {
        try {
            try(LDCachingKiWiPersistenceConnection dbcon = persistence.getConnection()) {

                // list all entries and remove them
                CloseableIteration<KiWiCacheEntry, SQLException> entries = dbcon.listAll();
                while (entries.hasNext()) {
                    dbcon.removeCacheEntry(entries.next());
                }

                // update triples in cache
                RepositoryConnection con = repository.getConnection();
                try {
                    con.begin();

                    con.remove((Resource)null,null,null,con.getValueFactory().createURI(cacheContext));

                    con.commit();
                } catch(RepositoryException ex) {
                    con.rollback();
                } finally {
                    con.close();
                }

            }

        } catch (RepositoryException | SQLException e) {
            log.error("could not retrieve cached triples from repository",e);
        }

    }

    /**
     * Carry out any initialization tasks that might be necessary
     */
    @Override
    public void initialize() {
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
    }
}
