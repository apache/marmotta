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

package org.apache.marmotta.kiwi.loader.generic;

import com.google.common.cache.Weigher;
import com.google.common.io.ByteStreams;
import com.google.common.io.CountingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class CacheWeigher<K extends Serializable,V extends Serializable> implements Weigher<K,V> {

    private static Logger log = LoggerFactory.getLogger(CacheWeigher.class);

    /**
     * Returns the weight of a cache entry. There is no unit for entry weights; rather they are simply
     * relative to each other.
     *
     * @return the weight of the entry; must be non-negative
     */
    @Override
    public int weigh(K key, V value) {
        try {
            CountingOutputStream counter = new CountingOutputStream(ByteStreams.nullOutputStream());
            ObjectOutputStream out = new ObjectOutputStream(counter);
            out.writeObject(key);
            out.writeObject(value);
            int result = (int) counter.getCount();
            out.close();
            return result + 16 + 48; // add 16 + 48 bytes for the cache maintenance information itself
        } catch (IOException e) {
            log.error("could not determine object size ...");
            return 1024; // assume 1 kB
        }
    }
}
