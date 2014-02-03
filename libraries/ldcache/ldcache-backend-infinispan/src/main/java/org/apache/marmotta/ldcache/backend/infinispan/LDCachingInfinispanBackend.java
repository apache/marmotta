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

package org.apache.marmotta.ldcache.backend.infinispan;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.backend.infinispan.io.ModelExternalizer;
import org.apache.marmotta.ldcache.backend.infinispan.io.ValueExternalizer;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.distribution.ch.SyncConsistentHashFactory;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCachingInfinispanBackend implements LDCachingBackend {

    private static Logger log = LoggerFactory.getLogger(LDCachingInfinispanBackend.class);

    public static final String LDCACHE_ENTRY_CACHE = "ldcache-entry-cache";
    public static final String LDCACHE_TRIPLE_CACHE = "ldcache-triple-cache";

    private EmbeddedCacheManager cacheManager;

    private GlobalConfiguration globalConfiguration;

    private Configuration defaultConfiguration;

    private boolean clustered;

    private Cache<String,CacheEntry> entryCache;

    /**
     * Create a non-clustered instance of the infinispan cache.
     */
    public LDCachingInfinispanBackend() {
        globalConfiguration = new GlobalConfigurationBuilder()
                .classLoader(LDCachingInfinispanBackend.class.getClassLoader())
                .globalJmxStatistics()
                    .jmxDomain("org.apache.marmotta.ldcache")
                    .allowDuplicateDomains(true)
                .build();

        defaultConfiguration = new ConfigurationBuilder()
                .clustering()
                    .cacheMode(CacheMode.LOCAL)
                .eviction()
                    .strategy(EvictionStrategy.LIRS)
                    .maxEntries(100000)
                .expiration()
                    .lifespan(7, TimeUnit.DAYS)
                    .maxIdle(1, TimeUnit.DAYS)
                .build();

        clustered = false;

    }


    public LDCachingInfinispanBackend(String clusterName, String machineName) {
        this(clusterName, machineName, 62333);
    }

    /**
     * Create a clustered instance of the infinispan cache backend using the provided cluster and machine name
     * @param clusterName
     * @param machineName
     * @param clusterPort port to use for multicast messages
     */
    public LDCachingInfinispanBackend(String clusterName, String machineName, int clusterPort) {
        try {
            String jgroupsXml = IOUtils.toString(LDCachingInfinispanBackend.class.getResourceAsStream("/jgroups-ldcache.xml"));

            jgroupsXml = jgroupsXml.replaceAll("mcast_port=\"[0-9]+\"", String.format("mcast_port=\"%d\"", clusterPort));

            globalConfiguration = new GlobalConfigurationBuilder()
                    .classLoader(LDCachingInfinispanBackend.class.getClassLoader())
                    .transport()
                        .defaultTransport()
                        .clusterName(clusterName)
                        .machineId(machineName)
                        .addProperty("configurationXml", jgroupsXml)
                    .globalJmxStatistics()
                        .jmxDomain("org.apache.marmotta.ldcache")
                        .allowDuplicateDomains(true)
                    .serialization()
                        .addAdvancedExternalizer(new ModelExternalizer())
                        .addAdvancedExternalizer(new ValueExternalizer())
                    .build();
        } catch (IOException ex) {
            log.warn("error loading JGroups configuration from archive: {}", ex.getMessage());
            log.warn("some configuration options will not be available");

            globalConfiguration = new GlobalConfigurationBuilder()
                    .classLoader(LDCachingInfinispanBackend.class.getClassLoader())
                    .transport()
                        .defaultTransport()
                        .clusterName(clusterName)
                        .machineId(machineName)
                        .addProperty("configurationFile", "jgroups-ldcache.xml")
                    .globalJmxStatistics()
                        .jmxDomain("org.apache.marmotta.ldcache")
                        .allowDuplicateDomains(true)
                    .serialization()
                        .addAdvancedExternalizer(new ModelExternalizer())
                        .addAdvancedExternalizer(new ValueExternalizer())
                    .build();
        }


        defaultConfiguration = new ConfigurationBuilder()
                .clustering()
                    .cacheMode(CacheMode.DIST_ASYNC)
                        .async()
                        .asyncMarshalling()
                    .l1()
                        .lifespan(5, TimeUnit.MINUTES)
                    .hash()
                        .numOwners(2)
                        .numSegments(40)
                        .consistentHashFactory(new SyncConsistentHashFactory())
                .stateTransfer()
                    .fetchInMemoryState(false)
                .eviction()
                    .strategy(EvictionStrategy.LIRS)
                    .maxEntries(100000)
                .expiration()
                    .lifespan(7, TimeUnit.DAYS)
                    .maxIdle(1, TimeUnit.DAYS)
                .build();


        clustered = true;


    }


    public synchronized Cache<String,CacheEntry> getEntryCache() {
        return entryCache;

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
        CacheEntry entry = getEntryCache().get(resource.stringValue());

        log.debug("retrieved entry for resource {}: {}", resource.stringValue(), entry);

        return entry;
    }

    /**
     * Update the cache entry for the given resource with the given entry.
     *
     * @param resource the resource to update
     * @param entry    the entry for the resource
     */
    @Override
    public void putEntry(URI resource, CacheEntry entry) {
        log.debug("updating entry for resource {} to {}", resource.stringValue(), entry);

        getEntryCache().put(resource.stringValue(), entry);
    }

    /**
     * Remove the cache entry for the given resource if it exists. Does nothing otherwise.
     *
     * @param resource the resource to remove the entry for
     */
    @Override
    public void removeEntry(URI resource) {
        log.debug("removing entry for resource {}", resource.stringValue());

        getEntryCache().remove(resource.stringValue());
    }

    /**
     * Clear all entries in the cache backend.
     */
    @Override
    public void clear() {
        getEntryCache().clear();
    }

    /**
     * Carry out any initialization tasks that might be necessary
     */
    @Override
    public void initialize() {
        cacheManager = new DefaultCacheManager(globalConfiguration, defaultConfiguration, true);

        if(entryCache == null) {
            cacheManager.defineConfiguration(LDCACHE_ENTRY_CACHE, defaultConfiguration);

            entryCache = cacheManager.<String,CacheEntry>getCache(LDCACHE_ENTRY_CACHE).getAdvancedCache().withFlags(Flag.SKIP_LOCKING, Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP);
        }


        log.info("initialised cache manager ({})", globalConfiguration.isClustered() ? "cluster name: "+globalConfiguration.transport().clusterName() : "single host");

    }

    /**
     * Shutdown the backend and free all runtime resources.
     */
    @Override
    public void shutdown() {
        log.warn("shutting down cache manager ...");
        if(cacheManager.getTransport() != null) {
            log.info("... shutting down transport ...");
            cacheManager.getTransport().stop();
        }
        log.info("... shutting down main component ...");
        cacheManager.stop();
        log.info("... done!");
    }
}
