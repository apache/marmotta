/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.api.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public interface CachingService {

    public Ehcache getCache(InjectionPoint injectionPoint);

    public String[] getCacheNames();

    public void clearAll();

    Ehcache getCacheByName(String cacheName);

    CacheManager getCacheManager();
}
