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
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class BaseEquivalenceHashSetTest {

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

    public abstract Set<String> createHashSet(Equivalence<String> equivalence);

    @Test
    public void testEquivalence() {
        assertTrue(equivalence.equivalent("abc", "axy"));
        assertFalse(equivalence.equivalent("abc", "xyz"));

        assertTrue(equivalence.hash("abc") == equivalence.hash("axy"));
        assertFalse(equivalence.hash("abc") == equivalence.hash("xyz"));
    }

    @Test
    public void testSetContains() {
        String a = "abc";
        String b = "axy";
        String c = "xyz";

        Set<String> set = createHashSet(equivalence);
        set.add(a);

        // set should now also contain b (because first character the same)
        assertTrue(set.contains(b));

        set.add(b);

        // adding b should not change the set
        assertEquals(1, set.size());

        set.add(c);

        assertEquals(2, set.size());

        assertTrue(set.containsAll(Sets.newHashSet(a, b, c)));
    }


    @Test
    public void testSetEquals() {
        String a1 = "abc";
        String a2 = "axy";
        String b1 = "bcd";
        String b2 = "bxy";
        String c1 = "cde";

        Set<String> set1 = createHashSet(equivalence);
        Set<String> set2 = createHashSet(equivalence);

        // test empty sets
        assertEquals(set1, set2);

        set1.add(a1);
        set1.add(b1);

        set2.add(b2);
        set2.add(a2);


        assertEquals(2, set1.size());
        assertEquals(2, set2.size());


        // test sets with elements, insertion order different
        assertEquals(set1, set2);
        assertEquals(set1.hashCode(), set2.hashCode());

        set1.add(c1);

        assertNotEquals(set1, set2);
        assertNotEquals(set1.hashCode(), set2.hashCode());


    }

    @Test
    public void testIteration() {
        String a = "abc";
        String b = "axy";
        String c = "xyz";

        Set<String> set = createHashSet(equivalence);
        set.add(a);
        set.add(b);
        set.add(c);

        int count = 0;
        for(String x : set) {
            count++;
        }
        assertEquals(2, count);
    }


    @Test
    public void testEmpty() {
        Set<String> set = createHashSet(equivalence);

        assertTrue(set.isEmpty());

        set.add("abc");

        assertFalse(set.isEmpty());
    }


    @Test
    public void testToArray() {
        String a = "abc";
        String b = "axy";
        String c = "xyz";

        Set<String> set = createHashSet(equivalence);
        set.add(a);
        set.add(b);
        set.add(c);

        String[] arr = new String[4];

        arr = set.toArray(arr);

        assertEquals(2, countNotNull(arr));
        assertThat(arr, hasItemInArray(startsWith("a")));
        assertThat(arr, hasItemInArray(startsWith("x")));
    }


    @Test
    public void testRemove() {
        String a = "abc";
        String b = "axy";
        String c = "xyz";

        Set<String> set = createHashSet(equivalence);
        set.add(a);
        set.add(b);
        set.add(c);

        assertEquals(2, set.size());

        set.remove(a);

        assertEquals(1, set.size());
        assertTrue(set.contains(c));
        assertFalse(set.contains(b));

        set.remove(c);

        assertEquals(0, set.size());
        assertFalse(set.contains(c));
        assertFalse(set.contains(b));
    }


    private static <T> int countNotNull(T[] arr) {
        int count = 0;
        for(int i=0; i<arr.length; i++) {
            if(arr[i] != null) {
                count++;
            }
        }
        return count;
    }
}
