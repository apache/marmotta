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
package org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.service;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.api.LDCacheProvider;
import org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.exception.LDClientException;
import org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.model.CacheEntry;
import org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.model.ClientResponse;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LDCache {

    private static final String CTX_CACHE = "http://www.newmedialab.at/ldclient/cache";

    private Logger log = LoggerFactory.getLogger(LDCache.class);

    private LDClient clientService;

    private LDEndpoints endpointService;


    private ThreadLocal<Boolean> inProgress = new ThreadLocal<Boolean>();


    private LDCacheProvider cacheProvider;

    // lock a resource while refreshing it so that not several threads trigger a refresh at the same time
    private HashMap<URI,Lock> resourceLocks;

    private Configuration config;


    public LDCache(LDCacheProvider ldCacheProvider) {
        log.info("Linked Data Caching Service initialising ...");
        try {
            config = new PropertiesConfiguration("ldclient.properties");
        } catch (ConfigurationException e) {
            log.warn("could not load configuration file ldclient.properties from current directory, home directory, or classpath");
        }

        resourceLocks = new HashMap<URI, Lock>();

        cacheProvider   = ldCacheProvider;
        clientService   = new LDClient();
        endpointService = new LDEndpoints();
    }


    private void lockResource(URI resource) {
        Lock lock;
        synchronized (resourceLocks) {
            lock = resourceLocks.get(resource);
            if(lock == null) {
                lock = new ReentrantLock();
                resourceLocks.put(resource,lock);
            }
        }
        lock.lock();
    }

    private void unlockResource(URI resource) {
        Lock lock;
        synchronized (resourceLocks) {
            lock = resourceLocks.remove(resource);
        }
        if(lock != null) {
            lock.unlock();
        }
    }


    /**
     * Refresh the cached resource passed as argument. The method will do nothing for local resources.
     * Calling the method will carry out the following tasks:
     * 1. check whether the resource is a remote resource; if no, returns immediately
     * 2. check whether the resource has a cache entry; if no, goto 4
     * 3. check whether the expiry time of the cache entry has passed; if no, returns immediately
     * 4. retrieve the triples for the resource from the Linked Data Cloud using the methods offered by the
     * LinkedDataClientService (registered endpoints etc); returns immediately if the result is null or
     * an exception is thrown
     * 5. remove all old triples for the resource and add all new triples for the resource
     * 6. create new expiry information of the cache entry and persist it in the transaction
     *
     * @param resource
     */
    public void refreshResource(URI resource) {

        lockResource(resource);
        try {

            // 2. check whether the resource has a cache entry; if no, goto 4
            CacheEntry entry = getCacheEntry(resource);

            // 3. check whether the expiry time of the cache entry has passed; if no, returns immediately
            if(entry != null && entry.getExpiryDate().after(new Date())) {
                log.info("not refreshing resource {}, as the cached entry is not yet expired",resource);
                return;
            }

            // 4.
            log.debug("refreshing resource {}",resource);
            try {
                ClientResponse response = clientService.retrieveResource(resource);

                if(response != null) {
                    log.info("refreshed resource {}",resource);

                    URI context = cacheProvider.getTripleRepository().getValueFactory().createURI(CTX_CACHE);


                    RepositoryConnection lmfConnection = cacheProvider.getTripleRepository().getConnection();
                    RepositoryConnection respConnection = response.getTriples().getConnection();

                    lmfConnection.remove(resource,null,null,context);


                    RepositoryResult<Statement> triples = respConnection.getStatements(null,null,null,true);
                    while(triples.hasNext()) {
                        Statement triple = triples.next();
                        try {
                            lmfConnection.add(triple,context);
                        } catch (RuntimeException ex) {
                            log.warn("not adding triple {}: an exception occurred ({})",triple,ex.getMessage());
                        }
                    }
                    lmfConnection.commit();

                    lmfConnection.close();
                    respConnection.close();

                    CacheEntry newEntry = new CacheEntry();
                    newEntry.setResource(resource);
                    newEntry.setExpiryDate(response.getExpires());
                    newEntry.setLastRetrieved(new Date());
                    if(entry != null) {
                        newEntry.setUpdateCount(entry.getUpdateCount()+1);
                    } else {
                        newEntry.setUpdateCount((long)1);
                    }

                    cacheProvider.getMetadataRepository().put(resource.stringValue(),newEntry);

                }

            } catch (LDClientException e) {
                // on exception, save an expiry information and retry in one day
                CacheEntry newEntry = new CacheEntry();
                newEntry.setResource(resource);
                newEntry.setExpiryDate(new Date(System.currentTimeMillis() + config.getInt("expiry", 86400)*1000));
                newEntry.setLastRetrieved(new Date());
                if(entry != null) {
                    newEntry.setUpdateCount(entry.getUpdateCount()+1);
                } else {
                    newEntry.setUpdateCount((long)1);
                }

                cacheProvider.getMetadataRepository().put(resource.stringValue(), newEntry);

                log.error("refreshing the remote resource {} from the Linked Data Cloud failed ({})",resource,e.getMessage());
                return;
            } catch (RepositoryException e) {
                log.error("repository error while refreshing the remote resource {} from the Linked Data Cloud", resource, e);
                return;
            }
        } finally {
            unlockResource(resource);
        }

    }


    private CacheEntry getCacheEntry(URI resource) {
        return cacheProvider.getMetadataRepository().get(resource.stringValue());
    }

    public LDEndpoints getEndpointService() {
        return endpointService;
    }

}
