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

package org.apache.marmotta.kiwi.config;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public enum CacheMode {
    /**
     * In local cache mode, the cache is not shared among the servers in a cluster. Each machine keeps a local cache.
     * This allows quick startups and eliminates network traffic in the cluster, but subsequent requests to different
     * cluster members cannot benefit from the cached data.
     */
    LOCAL,

    /**
     * In distributed cache mode, the cluster forms a big hash table used as a cache. This allows to make efficient
     * use of the large amount of memory available, but requires cache rebalancing and a lot of network transfers,
     * especially in case cluster members are restarted often.
     */
    DISTRIBUTED,

    /**
     * In replicated cache mode, each node in the cluster has an identical copy of all cache data. This allows
     * very efficient cache lookups and reduces the rebalancing effort, but requires more memory.
     */
    REPLICATED
}
