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
package org.apache.marmotta.platform.core.services.statistics.ehcache;

import org.apache.marmotta.platform.core.api.cache.CachingService;
import org.apache.marmotta.platform.core.api.statistics.StatisticsModule;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Statistics;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Offer statistical information about the EHCache caches used in the KiWi System
 * <p/>
 * User: sschaffe
 */
public class EHCacheStatisticsModule implements StatisticsModule {

    @Inject
    private Logger log;

    @Inject
    private CachingService cachingService;

    @PostConstruct
    public void initialize() {


    }

     /**
     * Enable this module. Depending on the type of information, this may involve additional runtime overhead.
     */
    @Override
    public void enable() {
        for(String cacheName : cachingService.getCacheNames()) {
             Ehcache cache = cachingService.getCacheByName(cacheName);
             if(cache != null) {
                 cache.setStatisticsEnabled(true);
                 cache.setStatisticsAccuracy(Statistics.STATISTICS_ACCURACY_GUARANTEED);
             }
         }
    }

    /**
     * Disable this module.
     */
    @Override
    public void disable() {
        for(String cacheName : cachingService.getCacheNames()) {
             Ehcache cache = cachingService.getCacheByName(cacheName);
             if(cache != null) {
                 cache.setStatisticsEnabled(false);
                 cache.setStatisticsAccuracy(Statistics.STATISTICS_ACCURACY_NONE);
             }
         }
    }

    /**
     * Return true if the module is enabled.
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Return all names of properties supported by this module.
     *
     * @return
     */
    @Override
    public List<String> getPropertyNames() {

        List<String> propertyNames = new LinkedList<String>();
        for(String cacheName : cachingService.getCacheNames()) {
            propertyNames.add(cacheName + " hits");
            propertyNames.add(cacheName + " misses");
            propertyNames.add(cacheName + " size");
        }
        return propertyNames;
    }

    /**
     * Return the statistics as a map from key to value
     *
     * @return
     */
    @Override
    public Map<String, String> getStatistics() {

        LinkedHashMap<String,String> result = new LinkedHashMap<String, String>();
        for(String cacheName : cachingService.getCacheNames()) {
            Ehcache cache = cachingService.getCacheByName(cacheName);
            if(cache != null) {
                Statistics stat = cache.getStatistics();

                result.put(cacheName + " hits",""+stat.getCacheHits());
                result.put(cacheName + " misses",""+stat.getCacheMisses());
                result.put(cacheName + " size",""+stat.getObjectCount());
            } else {
                log.warn("cache with name {} does not exist",cacheName);
            }
        }
        return result;
    }

    /**
     * Return the display name of the statistics module.
     *
     * @return
     */
    @Override
    public String getName() {
        return "Cache Statistics";
    }
}
