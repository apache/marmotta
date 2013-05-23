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
package org.apache.marmotta.ldcache.services;

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.api.LDCachingConnection;
import org.apache.marmotta.ldcache.api.LDCachingService;
import org.apache.marmotta.ldcache.model.CacheConfiguration;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Main class for accessing the Linked Data Cache. A new LDCache can be instantiated with
 * <code>new LDCache(CacheConfiguration, LDCachingBackend)</code> and passing an appropriate
 * configuration and caching backend.
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCache implements LDCachingService {

    private static Logger log = LoggerFactory.getLogger(LDCache.class);


    // lock a resource while refreshing it so that not several threads trigger a refresh at the same time
    private HashMap<Resource,ReentrantLock> resourceLocks;

    private LDClientService  ldclient;

    private LDCachingBackend backend;

    private CacheConfiguration config;

    private ReentrantReadWriteLock lock;

    /**
     * Instantiate a new LDCache service by passing a configuration and a backend for storing cache data.
     *
     * @param config
     * @param backend
     */
    public LDCache(CacheConfiguration config, LDCachingBackend backend) {
        log.info("Linked Data Caching Service initialising ...");

        this.resourceLocks = new HashMap<Resource, ReentrantLock>();
        this.backend  = backend;
        this.ldclient = new LDClient(config.getClientConfiguration());
        this.config   = config;
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Reload configuration and initialise LDClient.
     */
    public void reload() {
        lock.writeLock().lock();
        try {
            if(this.ldclient != null) {
                log.info("Reloading LDClient configuration ...");
                this.ldclient.shutdown();
                this.ldclient = new LDClient(config.getClientConfiguration());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Return a repository connection that can be used for accessing cached resources.
     *
     * @param  resource the resource that will be cached
     * @return a repository connection that can be used for storing retrieved triples for caching
     */
    @Override
    public LDCachingConnection getCacheConnection(String resource) throws RepositoryException {
        return backend.getCacheConnection(resource);
    }

    /**
     * Return an iterator over all cache entries (can e.g. be used for refreshing or expiring).
     *
     * @return
     */
    @Override
    public CloseableIteration<CacheEntry, RepositoryException> listCacheEntries() throws RepositoryException {
        return backend.listCacheEntries();
    }

    /**
     * Return an iterator over all expired cache entries (can e.g. be used for refreshing).
     *
     * @return
     */
    @Override
    public CloseableIteration<CacheEntry, RepositoryException> listExpiredEntries() throws RepositoryException {
        return backend.listExpiredEntries();
    }


    /**
     * Return true if the resource is a cached resource.
     *
     * @param resourceUri
     * @return
     * @throws RepositoryException
     */
    public boolean isCached(String resourceUri) throws RepositoryException {
        // if there is no cache entry, then return false in any case
        if(!backend.isCached(resourceUri)) {
            return false;
        } else {
            // else list all cached triples - if there are none, the resource is not cached (e.g. blacklist or no LD resource)
            RepositoryConnection con = backend.getCacheConnection(resourceUri);
            try {
                con.begin();
                return con.hasStatement(con.getValueFactory().createURI(resourceUri), null, null, false);
            } finally {
                con.commit();
                con.close();
            }
        }
    }

    /**
     * Manually expire the caching information for the given resource. The resource will be
     * re-retrieved upon the next access.
     *
     * @param resource the Resource to expire.
     */
    @Override
    public void expire(URI resource) {
        Date now = new Date();

        try {
            LDCachingConnection con = backend.getCacheConnection(resource.stringValue());
            try {
                con.begin();

                CacheEntry entry = con.getCacheEntry(resource);
                if(entry.getExpiryDate().getTime() > now.getTime()) {
                    entry.setExpiryDate(now);

                    con.removeCacheEntry(entry.getResource());
                    con.addCacheEntry(entry.getResource(),entry);
                }

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } finally {
                con.close();
            }
        } catch(RepositoryException ex) {
            ex.printStackTrace(); // TODO: handle error
        }

    }

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
     * @param resource
     * @param forceRefresh if <code>true</code> the resource will be refreshed despite the
     */
    @Override
    public void refreshResource(URI resource, boolean forceRefresh) {
        final ReentrantLock lock = lockResource(resource);
        try {
            LDCachingConnection cacheConnection = backend.getCacheConnection(resource.stringValue());
            CacheEntry entry = null;
            try {
                cacheConnection.begin();

                // 2. check whether the resource has a cache entry; if no, goto 4
                entry = cacheConnection.getCacheEntry(resource);

                // commit/close the connection, the retrieveResource method takes too long to hold the DB connection open
                cacheConnection.commit();

                // 3. check whether the expiry time of the cache entry has passed; if no, returns immediately
                if(!forceRefresh && entry != null && entry.getExpiryDate().after(new Date())) {
                    log.debug("not refreshing resource {}, as the cached entry is not yet expired",resource);
                    return;
                }
            } catch(RepositoryException ex) {
                cacheConnection.rollback();
            } finally {
                cacheConnection.close();
            }

            // 4.
            log.debug("refreshing resource {}",resource);
            this.lock.readLock().lock();
            try {
                ClientResponse response = ldclient.retrieveResource(resource.stringValue());

                if(response != null) {
                    log.info("refreshed resource {}",resource);

                    // obtain a new cache connection, since we closed the original connection above
                    LDCachingConnection cacheConnection1 = backend.getCacheConnection(resource.stringValue());
                    cacheConnection1.begin();
                    try {
                        URI subject = cacheConnection1.getValueFactory().createURI(resource.stringValue());

                        RepositoryConnection respConnection = response.getTriples().getConnection();

                        cacheConnection1.remove(subject, null, null);

                        int count = 0;
                        RepositoryResult<Statement> triples = respConnection.getStatements(null,null,null,true);
                        while(triples.hasNext()) {
                            Statement triple = triples.next();
                            try {
                                cacheConnection1.add(triple);
                            } catch (RuntimeException ex) {
                                log.warn("not adding triple {}: an exception occurred ({})",triple,ex.getMessage());
                            }
                            count++;
                        }
                        triples.close();
                        respConnection.close();

                        CacheEntry newEntry = new CacheEntry();
                        newEntry.setResource(subject);
                        newEntry.setExpiryDate(response.getExpires());
                        newEntry.setLastRetrieved(new Date());
                        if(entry != null) {
                            newEntry.setUpdateCount(entry.getUpdateCount()+1);
                        } else {
                            newEntry.setUpdateCount(1);
                        }
                        newEntry.setTripleCount(count);

                        cacheConnection1.removeCacheEntry(resource);
                        cacheConnection1.addCacheEntry(resource, newEntry);
                        cacheConnection1.commit();
                    } catch (RepositoryException e) {
                        log.error("repository error while refreshing the remote resource {} from the Linked Data Cloud", resource, e);
                        cacheConnection1.rollback();
                    } finally {
                        cacheConnection1.close();
                    }
                }

            } catch (DataRetrievalException e) {
                // on exception, save an expiry information and retry in one day
                CacheEntry newEntry = new CacheEntry();
                newEntry.setResource(cacheConnection.getValueFactory().createURI(resource.stringValue()));
                newEntry.setExpiryDate(new Date(System.currentTimeMillis() + config.getDefaultExpiry()*1000));
                newEntry.setLastRetrieved(new Date());
                if(entry != null) {
                    newEntry.setUpdateCount(entry.getUpdateCount()+1);
                } else {
                    newEntry.setUpdateCount(1);
                }
                newEntry.setTripleCount(0);

                LDCachingConnection cacheConnection2 = backend.getCacheConnection(resource.stringValue());
                cacheConnection2.begin();
                try {
                    cacheConnection2.removeCacheEntry(resource);
                    cacheConnection2.addCacheEntry(resource, newEntry);

                    cacheConnection2.commit();
                    log.error("refreshing the remote resource {} from the Linked Data Cloud failed ({})",resource,e.getMessage());
                    //log.info("exception was:",e);
                    return;
                } catch (RepositoryException ex) {
                    log.error("repository error while refreshing the remote resource {} from the Linked Data Cloud", resource, ex);
                    cacheConnection2.rollback();
                } finally {
                    cacheConnection2.close();
                }
            } finally {
                this.lock.readLock().unlock();
            }
        } catch (RepositoryException e) {
            log.error("repository exception while obtaining cache connection",e);
        } finally {
            unlockResource(resource, lock);
        }

    }


    /**
     * Refresh all expired resources by listing the cache entries that have expired and calling refreshResource on
     * them. This method can e.g. be called by a scheduled task to regularly update cache entries to always have
     * the latest version available in the Search Index and elsewhere.
     */
    @Override
    public void refreshExpired() {
        Date now = new Date();

        try {
            CloseableIteration<CacheEntry,RepositoryException> it = backend.listExpiredEntries();
            try {
                while(it.hasNext()) {
                    CacheEntry next =  it.next();

                    if(next.getExpiryDate().getTime() < now.getTime()) {
                        refreshResource(next.getResource(),false);
                    }
                }
            } finally {
                it.close();
            }
        } catch(RepositoryException ex) {
            log.error("exception while refreshing cache entries", ex);
        }

    }

    /**
     * Manually expire all cached resources.
     *
     * @see #expire(org.openrdf.model.URI)
     */
    @Override
    public void expireAll() {
        Date now = new Date();

        try {
            CloseableIteration<CacheEntry,RepositoryException> it = backend.listCacheEntries();
            try {
                while(it.hasNext()) {
                    CacheEntry next =  it.next();

                    if(next.getExpiryDate().getTime() > now.getTime()) {
                        next.setExpiryDate(now);

                        try {
                            LDCachingConnection con = backend.getCacheConnection(next.getResource().stringValue());
                            try {
                                con.begin();

                                con.removeCacheEntry(next.getResource());
                                con.addCacheEntry(next.getResource(), next);

                                con.commit();
                            } catch(RepositoryException ex) {
                                con.rollback();
                            } finally {
                                con.close();
                            }
                        } catch(RepositoryException ex) {
                        }
                    }
                }
            } finally {
                it.close();
            }
        } catch(RepositoryException ex) {
            log.error("exception while expiring cache entries",ex);
        }

    }

    /**
     * Shutdown the caching service and free all occupied runtime resources.
     */
    @Override
    public void shutdown() {
        lock.writeLock().lock();
        try {
            backend.shutdown();
            ldclient.shutdown();
        } finally {
            lock.writeLock().unlock();
        }
    }


    public LDClientService getLDClient() {
        return ldclient;
    }

    private ReentrantLock lockResource(final URI resource) {
        ReentrantLock lock;
        synchronized (resourceLocks) {
            lock = resourceLocks.get(resource);
            if(lock == null) {
                lock = new ReentrantLock();
                resourceLocks.put(resource,lock);
            }
        }
        lock.lock();
        return lock;
    }

    private void unlockResource(final URI resource, final ReentrantLock lock) {
        synchronized (resourceLocks) {
            // lock = resourceLocks.get(resource);
            if (lock != null) {
                if (!lock.hasQueuedThreads()) {
                    resourceLocks.remove(resource);
                }
            }
        }
        if (lock != null) {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.error("Could not release lock for {} (Thread: {}, Lock: {})", resource, Thread.currentThread().getName(), lock.toString() );
                throw e;
            }

        }
    }

}
