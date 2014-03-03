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

package org.apache.marmotta.kiwi.caching;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public enum CacheManagerType {

    /**
     * Simple in-memory cache backend using the Guava library; no clustering support
     */
    GUAVA("org.apache.marmotta.kiwi.caching.GuavaCacheManagerFactory"),

    /**
     * Cache backend based on Infinispan using a dynamic cluster setup (UDP multicast)
     */
    INFINISPAN_CLUSTERED("org.apache.marmotta.kiwi.embedded.InfinispanEmbeddedCacheManagerFactory"),

    /**
     * Cache backend based on Infinispan using a client-server setup (Hotrod)
     */
    INFINISPAN_HOTROD("org.apache.marmotta.kiwi.remote.InfinispanRemoteCacheManagerFactory"),


    /**
     * Cache backend based on Hazelcast using a dynamic cluster setup
     */
    HAZELCAST("org.apache.marmotta.kiwi.hazelcast.caching.HazelcastCacheManagerFactory");


    CacheManagerType(String factoryClass) {
        this.factoryClass = factoryClass;
    }

    private String factoryClass;

    public String getFactoryClass() {
        return factoryClass;
    }
}
