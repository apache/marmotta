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

package org.apache.marmotta.commons.collections;

import com.google.common.base.Equivalence;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class BaseEquivalenceHashMapTest {

    // a simple equivalence function on strings, saying they are equal if their first character is the same
    Equivalence<String> equivalence = new Equivalence<String>() {
        @Override
        protected boolean doEquivalent(String a, String b) {
            return a.charAt(0) == b.charAt(0);
        }

        @Override
        protected int doHash(String s) {
            return s.charAt(0) * 31;
        }
    };

    public abstract Map<String,String> createHashMap(Equivalence<String> equivalence);

    @Test
    public void testPutGet() {
        Map<String,String> map = createHashMap(equivalence);

        map.put("abc","a");
        map.put("axy","a");
        map.put("xyz","x");

        assertEquals(2, map.size());
        assertTrue(map.containsKey("abc"));
        assertTrue(map.containsKey("axy"));
        assertTrue(map.containsKey("aef"));

        assertEquals("a", map.get("abc"));
        assertEquals("a", map.get("aef"));

        assertTrue(map.containsValue("a"));
        assertTrue(map.containsValue("x"));
    }

    @Test
    public void testKeySet() {
        Map<String,String> map = createHashMap(equivalence);

        map.put("abc","a");
        map.put("axy","a");
        map.put("xyz","x");

        assertThat(map.keySet(), contains(startsWith("a"), startsWith("x")));
    }

    @Test
    public void testIteration() {
        Map<String,String> map = createHashMap(equivalence);

        map.put("abc","a");
        map.put("axy","a");
        map.put("xyz","x");

        int count = 0;
        for(Map.Entry<String,String> e : map.entrySet()) {
            assertThat(e.getKey(), anyOf(equalTo("abc"), equalTo("axy"), equalTo("xyz")));
            assertThat(e.getValue(), anyOf(equalTo("a"), equalTo("x")));
            count++;
        }
        assertEquals(2, count);
    }

}
