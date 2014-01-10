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

import org.openrdf.model.Model;
import org.openrdf.model.URI;

/**
 * This is the next-generation API for LDCache that will become the default in Marmotta 3.3 or 4.0. For now,
 * LDCache implements both the old and the new style.
 * <p/>
 * User: sschaffe
 */
public interface LDCachingService {


    /**
     * Refresh the resource passed as argument. If the resource is not yet cached or the cache entry is
     * expired or refreshing is forced, the remote resource is retrieved using LDClient and the result stored
     * in the cache. Otherwise the method does nothing.
     * 
     *
     * @param resource  the resource to refresh
     * @param options   options for refreshing
     */
    public void refresh(URI resource, RefreshOpts... options);


    /**
     * Refresh and return the resource passed as argument. If the resource is not yet cached or the cache entry is
     * expired or refreshing is forced, the remote resource is retrieved using LDClient and the result stored
     * in the cache. Otherwise the method does nothing.
     *
     * @param resource  the resource to retrieve
     * @param options   options for refreshing
     * @return a Sesame Model holding the triples representing the resource
     */
    public Model get(URI resource, RefreshOpts... options);


    /**
     * Manually expire the caching information for the given resource. The resource will be
     * re-retrieved upon the next access.
     *
     * @param resource the resource to expire.
     */
    public void expire(URI resource);


    /**
     * Return true in case the cache contains an entry for the resource given as argument.
     *
     * @param resource the resource to check
     * @return true in case the resource is contained in the cache
     */
    public boolean contains(URI resource);

    /**
     * Manually expire all cached resources.
     */
    public void clear();


    /**
     * Shutdown the caching service and free all occupied runtime resources.
     */
    public void shutdown();



    public enum RefreshOpts {

        /**
         * Refresh the resource even if it is not yet expired
         */
        FORCE
    }
}
