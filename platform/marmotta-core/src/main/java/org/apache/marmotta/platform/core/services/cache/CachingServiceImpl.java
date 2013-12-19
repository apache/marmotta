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
package org.apache.marmotta.platform.core.services.cache;

import org.apache.marmotta.platform.core.api.cache.CachingService;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.events.SystemRestartingEvent;
import org.apache.marmotta.platform.core.qualifiers.cache.MarmottaCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.distribution.ch.SyncConsistentHashFactory;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A service that offers a EHCache system cache implementation for use by other components
 * <p/>
 * User: sschaffe
 */
@ApplicationScoped
public class CachingServiceImpl implements CachingService {

    /**
     * Get the seam logger for issuing logging statements.
     */
    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    private EmbeddedCacheManager cacheManager;

    private GlobalConfiguration globalConfiguration;

    // default configuration: distributed cache, 100000 entries, 300 seconds expiration, 60 seconds idle
    private Configuration defaultConfiguration;


    public CachingServiceImpl() {
    }


    @PostConstruct
    public void initialize() {
        boolean clustered = configurationService.getBooleanConfiguration("clustering.enabled", false);

        log.info("Apache Marmotta Caching Service starting up ({}) ...", clustered ? "cluster name: " + configurationService.getStringConfiguration("clustering.name", "Marmotta") : "single host" );
        if(clustered) {
            globalConfiguration = new GlobalConfigurationBuilder()
                    .transport()
                        .defaultTransport()
                        .clusterName(configurationService.getStringConfiguration("clustering.name", "Marmotta"))
                        .machineId(configurationService.getServerName())
                        .addProperty("configurationFile", "jgroups-marmotta.xml")
                    .globalJmxStatistics()
                        .jmxDomain("org.apache.marmotta.platform")
                        .allowDuplicateDomains(true)
                    .build();


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
                    .eviction()
                        .strategy(EvictionStrategy.LIRS)
                        .maxEntries(10000)
                    .expiration()
                        .lifespan(30, TimeUnit.MINUTES)
                        .maxIdle(10, TimeUnit.MINUTES)
                    .build();
        } else {
            globalConfiguration = new GlobalConfigurationBuilder()
                    .globalJmxStatistics()
                        .jmxDomain("org.apache.marmotta.platform")
                        .allowDuplicateDomains(true)
                    .build();

            defaultConfiguration = new ConfigurationBuilder()
                    .clustering()
                        .cacheMode(CacheMode.LOCAL)
                    .eviction()
                        .strategy(EvictionStrategy.LIRS)
                        .maxEntries(1000)
                    .expiration()
                        .lifespan(5, TimeUnit.MINUTES)
                        .maxIdle(1, TimeUnit.MINUTES)
                    .build();

        }


        cacheManager = new DefaultCacheManager(globalConfiguration, defaultConfiguration, true);

        log.info("initialised cache manager ({})", globalConfiguration.isClustered() ? "cluster name: "+globalConfiguration.transport().clusterName() : "single host");
    }

    /**
     * Return the cache with the name provided to the KiWiCache annotation of the injection.
     * 
     * 
     * Usage: <code><pre>
     * &#64;Inject &#64;KiWiCache("cache-name")
     * private Ehcache cache;
     * </pre></code>
     * 
     * 
     * @param injectionPoint
     * @return
     */
    @Override
    @Produces @MarmottaCache("")
    public Cache getCache(InjectionPoint injectionPoint) {
        String cacheName = injectionPoint.getAnnotated().getAnnotation(MarmottaCache.class).value();

        return getCacheByName(cacheName);
    }


    /**
     * Allow CDI injection of the default cache
     * @return
     */
    @Produces
    public Configuration getDefaultConfiguration() {
        return defaultConfiguration;
    }


    @Override
    public Cache getCacheByName(String cacheName) {
        return cacheManager.getCache(cacheName, true);
    }


    @Override
    public Set<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }

    @Override
    @Produces
    @ApplicationScoped
    public EmbeddedCacheManager getCacheManager() {
        return cacheManager;
    }


    /**
     * When system is restarted, flush all cache data
     * @param e
     */
    public void systemRestart(@Observes SystemRestartingEvent e) {
        log.warn("system restarted, flushing caches ...");
        cacheManager.stop();
        cacheManager.start();
    }


    @Override
    public void clearAll() {
        Set<String> set =  cacheManager.getCacheNames();
        Iterator<String> iterator =  set.iterator();
        while(iterator.hasNext()){
            String cacheName = iterator.next();
            Cache<String,Object> cache = cacheManager.getCache(cacheName);
            cache.clear();
        }
    }


    @PreDestroy
    public void destroy() {
        log.info("Apache Marmotta Caching Service shutting down ...");
        if(cacheManager.getStatus() == ComponentStatus.RUNNING) {
            log.info("- shutting down Infinispan cache manager ...");
            cacheManager.stop();
            log.info("  ... success!");
        }
        log.info("Apache Marmotta Caching Service shut down successfully.");
    }
}
