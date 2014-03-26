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

package org.apache.marmotta.kiwi.ehcache.util;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A map API wrapper around EHCache caches so it is compatible with the KiWi caching API.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class CacheMap<K,V> implements Map<K,V> {

    private Cache delegate;

    public CacheMap(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return delegate.getSize();
    }

    @Override
    public boolean isEmpty() {
        return delegate.getSize() == 0;
    }

    @Override
    public boolean containsKey(Object o) {
        return delegate.isKeyInCache(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return delegate.isValueInCache(o);
    }

    @Override
    public V get(Object o) {
        Element e = delegate.get(o);

        if(e != null) {
            return (V) e.getObjectValue();
        } else {
            return null;
        }
    }

    @Override
    public V put(K k, V v) {
        Element e = delegate.get(k);
        delegate.put(new Element(k, v));

        if(e != null) {
            return (V) e.getObjectValue();
        } else {
            return null;
        }
    }

    @Override
    public V remove(Object o) {
        Element e = delegate.removeAndReturnElement(o);
        if(e != null) {
            return (V) e.getObjectValue();
        } else {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for(Map.Entry entry : map.entrySet()) {
            put((K)entry.getKey(), (V)entry.getValue());
        }
    }

    @Override
    public void clear() {
        delegate.removeAll();
    }

    @Override
    public Set<K> keySet() {
        return ImmutableSet.copyOf(delegate.getKeys());
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException("listing values not supported by cache");
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return ImmutableSet.copyOf(Collections2.transform(delegate.getKeys(), new Function<K,Entry<K,V>> () {
            @Override
            public Entry<K, V> apply(final K input) {
                final Element e = delegate.get(input);
                return new Entry<K,V>() {
                    @Override
                    public K getKey() {
                        return input;
                    }

                    @Override
                    public V getValue() {
                        return (V) e.getObjectValue();
                    }

                    @Override
                    public V setValue(V v) {
                        delegate.put(new Element(input,v));
                        return (V) e.getObjectValue();
                    }
                };
            }
        }));
    }
}
