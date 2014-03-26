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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.marmotta.platform.core.api.cache.CachingService;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.events.SystemRestartingEvent;
import org.apache.marmotta.platform.core.model.config.CoreOptions;
import org.apache.marmotta.platform.core.qualifiers.cache.MarmottaCache;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
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


    private Map<String,Cache> caches;


    public CachingServiceImpl() {
    }


    @PostConstruct
    public void initialize() {
        caches = new HashMap<>();
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
    public ConcurrentMap getCache(InjectionPoint injectionPoint) {
        String cacheName = injectionPoint.getAnnotated().getAnnotation(MarmottaCache.class).value();

        return getCacheByName(cacheName);
    }



    @Override
    public ConcurrentMap getCacheByName(String cacheName) {
        synchronized (caches) {
            if(!caches.containsKey(cacheName)) {
                Cache c = CacheBuilder.newBuilder()
                        .expireAfterAccess(configurationService.getIntConfiguration(CoreOptions.CACHING_EXPIRATION,30), TimeUnit.MINUTES)
                        .maximumSize(configurationService.getLongConfiguration(CoreOptions.CACHING_MAXIMUM_SIZE,10000L))
                        .build();
                caches.put(cacheName,c);
            }
        }

        return caches.get(cacheName).asMap();
    }


    @Override
    public Set<String> getCacheNames() {
        return caches.keySet();
    }



    /**
     * When system is restarted, flush all cache data
     * @param e
     */
    public void systemRestart(@Observes SystemRestartingEvent e) {
        log.warn("system restarted, flushing caches ...");
        caches.clear();
    }


    @Override
    public void clearAll() {
        caches.clear();
    }


    @PreDestroy
    public void destroy() {
        log.info("Apache Marmotta Caching Service shutting down ...");
        caches.clear();
        log.info("Apache Marmotta Caching Service shut down successfully.");
    }
}
