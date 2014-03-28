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
 * The strategy to use for transactional triple registry. This is needed to avoid two parallel transactions creating
 * the same triple with different IDs.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public enum RegistryStrategy {

    /**
     * Use the database to synchronize between several parallel instances. Slow but requires less memory.
     */
    DATABASE,

    /**
     * Use a synchronized replicated infinispan cache to synchronize between parallel instances. Faster but requires
     * more memory.
     */
    CACHE,

    /**
     * Use a local in-memory hash map to synchronize between parallel instances. Does not synchronize across machines
     * in a cluster
     */
    LOCAL

}
