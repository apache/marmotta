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

package org.apache.marmotta.kiwi.caching.config;

/**
 * Configuration object for all query caching options that are configurable.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiQueryCacheConfiguration {


    /**
     * Maximum size of results to cache. Results bigger than this value will not be cached.
     */
    private int maxEntrySize = 150;


    /**
     * Maximum number of entries to keep in the cache.
     */
    private int maxCacheSize = 100000;

    public KiWiQueryCacheConfiguration() {
    }

    /**
     * Maximum size of results to cache. Results bigger than this value will not be cached.
     */
    public int getMaxEntrySize() {
        return maxEntrySize;
    }

    /**
     * Maximum size of results to cache. Results bigger than this value will not be cached.
     */
    public void setMaxEntrySize(int maxEntrySize) {
        this.maxEntrySize = maxEntrySize;
    }

    /**
     * Maximum number of entries to keep in the cache.
     */
    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    /**
     * Maximum number of entries to keep in the cache.
     */
    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }
}
