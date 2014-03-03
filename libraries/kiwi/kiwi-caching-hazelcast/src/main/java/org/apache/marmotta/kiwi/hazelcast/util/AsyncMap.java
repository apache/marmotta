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

package org.apache.marmotta.kiwi.hazelcast.util;

import com.hazelcast.core.IMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A Map wrapper mapping write methods to their asynchronous equivalents in Hazelcast.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class AsyncMap<K,V> implements Map<K,V> {

    private IMap<K,V> delegate;

    public AsyncMap(IMap<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return delegate.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return delegate.containsValue(o);
    }

    @Override
    public V get(Object o) {
        return delegate.get(o);
    }

    @Override
    public V put(K k, V v) {
        delegate.putAsync(k,v);
        return null;
    }

    @Override
    public V remove(Object o) {
        delegate.removeAsync((K)o);
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for(Entry<? extends K, ? extends V> entry : map.entrySet()) {
            delegate.putAsync(entry.getKey(),entry.getValue());
        }
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }
}
