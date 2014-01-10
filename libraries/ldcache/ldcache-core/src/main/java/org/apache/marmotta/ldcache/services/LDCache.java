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

package org.apache.marmotta.ldcache.services;

import org.apache.marmotta.commons.locking.ObjectLocks;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.api.LDCachingService;
import org.apache.marmotta.ldcache.model.CacheConfiguration;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.TreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Next generation LDCache API. Will eventually replace the old LDCache API.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCache implements LDCachingService {

    private static Logger log = LoggerFactory.getLogger(LDCache.class);

    // lock a resource while refreshing it so that not several threads trigger a refresh at the same time
    private ObjectLocks resourceLocks;

    private CacheConfiguration config;

    private LDClientService ldclient;

    private LDCachingBackend backend;

    private ReentrantReadWriteLock lock;

    /**
     * Create a new instance of LDCache using the provided LDCache configuration and backend. The backend needs to
     * be initialized already. The cache configuration will be used to create an instance of LDClient.
     *
     * @param config
     * @param backend
     */
    public LDCache(CacheConfiguration config, LDCachingBackend backend) {
        this.resourceLocks = new ObjectLocks();
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
     * Refresh the resource passed as argument. If the resource is not yet cached or the cache entry is
     * expired or refreshing is forced, the remote resource is retrieved using LDClient and the result stored
     * in the cache. Otherwise the method does nothing.
     *
     * @param resource the resource to refresh
     * @param options  options for refreshing
     */
    @Override
    public void refresh(URI resource, RefreshOpts... options) {
        Set<RefreshOpts> optionSet = new HashSet<>(Arrays.asList(options));

        resourceLocks.lock(resource.stringValue());
        try {
            // check if the resource is already cached; if yes, and refresh is not forced, return immediately
            CacheEntry entry = backend.getEntry(resource);
            if(!optionSet.contains(RefreshOpts.FORCE) && entry != null && entry.getExpiryDate().after(new Date())) {
                log.debug("not refreshing resource {}, as the cached entry is not yet expired",resource);
                return;
            }

            // refresh the resource by calling LDClient
            log.debug("refreshing resource {}",resource);
            this.lock.readLock().lock();
            try {
                ClientResponse response = ldclient.retrieveResource(resource.stringValue());

                if(response != null) {
                    log.info("refreshed resource {}",resource);

                    CacheEntry newEntry = new CacheEntry();
                    newEntry.setResource(resource);
                    newEntry.setExpiryDate(response.getExpires());
                    newEntry.setLastRetrieved(new Date());
                    if(entry != null) {
                        newEntry.setUpdateCount(entry.getUpdateCount()+1);
                    } else {
                        newEntry.setUpdateCount(1);
                    }
                    newEntry.setTripleCount(response.getData().size());
                    newEntry.setTriples(response.getData());

                    backend.putEntry(resource, newEntry);

                }

            } catch (DataRetrievalException e) {

                // on exception, save an expiry information and retry in one day
                CacheEntry newEntry = new CacheEntry();
                newEntry.setResource(resource);
                newEntry.setExpiryDate(new Date(System.currentTimeMillis() + config.getDefaultExpiry()*1000));
                newEntry.setLastRetrieved(new Date());
                if(entry != null) {
                    newEntry.setUpdateCount(entry.getUpdateCount()+1);
                } else {
                    newEntry.setUpdateCount(1);
                }
                newEntry.setTripleCount(0);
                newEntry.setTriples(new TreeModel());

                backend.putEntry(resource, newEntry);

            } finally {
                this.lock.readLock().unlock();
            }
        } finally {
            resourceLocks.unlock(resource.stringValue());
        }

    }

    /**
     * Refresh and return the resource passed as argument. If the resource is not yet cached or the cache entry is
     * expired or refreshing is forced, the remote resource is retrieved using LDClient and the result stored
     * in the cache. Otherwise the method returns the cached entry. In case a cached entry does not exist, the method
     * returns an empty Model.
     *
     * @param resource the resource to retrieve
     * @param options  options for refreshing
     * @return a Sesame Model holding the triples representing the resource
     */
    @Override
    public Model get(URI resource, RefreshOpts... options) {
        refresh(resource, options);

        CacheEntry entry =  backend.getEntry(resource);

        if(entry != null) {
            return entry.getTriples();
        } else {
            return new TreeModel();
        }
    }

    /**
     * Manually expire the caching information for the given resource. The resource will be
     * re-retrieved upon the next access.
     *
     * @param resource the resource to expire.
     */
    @Override
    public void expire(URI resource) {
        backend.removeEntry(resource);
    }

    /**
     * Return true in case the cache contains an entry for the resource given as argument.
     *
     * @param resource the resource to check
     * @return true in case the resource is contained in the cache
     */
    @Override
    public boolean contains(URI resource) {
        return backend.getEntry(resource) != null;
    }

    /**
     * Manually expire all cached resources.
     */
    @Override
    public void clear() {
        backend.clear();
    }

    /**
     * Shutdown the caching service and free all occupied runtime resources.
     */
    @Override
    public void shutdown() {
        backend.shutdown();
    }


    public LDClientService getClient() {
        return ldclient;
    }
}
