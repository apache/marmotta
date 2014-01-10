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

package org.apache.marmotta.ldcache.api;

import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.model.URI;

/**
 * Next generation caching backend API. Needs to be implemented by backend providers to offer caching support.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface LDCachingBackend {


    /**
     * Return the cache entry for the given resource, or null if this entry does not exist.
     *
     *
     * @param resource the resource to retrieve the cache entry for
     * @return
     */
    public CacheEntry getEntry(URI resource);


    /**
     * Update the cache entry for the given resource with the given entry.
     *
     * @param resource the resource to update
     * @param entry    the entry for the resource
     */
    public void putEntry(URI resource, CacheEntry entry);


    /**
     * Remove the cache entry for the given resource if it exists. Does nothing otherwise.
     *
     * @param resource the resource to remove the entry for
     */
    public void removeEntry(URI resource);


    /**
     * Clear all entries in the cache backend.
     */
    public void clear();



    /**
     * Carry out any initialization tasks that might be necessary
     */
    public void initialize();

    /**
     * Shutdown the backend and free all runtime resources.
     */
    public void shutdown();

}
