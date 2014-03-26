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
package org.apache.marmotta.platform.core.api.cache;

import javax.enterprise.inject.spi.InjectionPoint;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple caching functionality for the Marmotta Platform modules. Note that the KiWi triplestore (and other
 * triple stores) come with their own custom caching implementations.
 * <p/>
 * User: sschaffe
 */
public interface CachingService {


    /**
     * Inject a cache at the given injection point. Creates a new cache if needed.
     *
     * @param injectionPoint
     * @return
     */
    public ConcurrentMap getCache(InjectionPoint injectionPoint);

    /**
     * Return the names of all caches registered in the caching service
     * @return
     */
    public Set<String> getCacheNames();

    /**
     * Clear all caches registered in the caching service
     */
    public void clearAll();

    /**
     * Get the cache with the given name. Creates a new cache if needed.
     *
     * @param cacheName
     * @return
     */
    public ConcurrentMap getCacheByName(String cacheName);

}
