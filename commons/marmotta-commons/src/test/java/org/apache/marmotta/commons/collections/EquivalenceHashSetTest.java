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
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class EquivalenceHashSetTest {

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

    public Set<String> createHashSet(Equivalence<String> equivalence) {
        return new EquivalenceHashSet<>(equivalence);
    }

    @Test
    public void testEquivalence() {
        Assert.assertTrue(equivalence.equivalent("abc","axy"));
        Assert.assertFalse(equivalence.equivalent("abc", "xyz"));

        Assert.assertTrue(equivalence.hash("abc") == equivalence.hash("axy"));
        Assert.assertFalse(equivalence.hash("abc") == equivalence.hash("xyz"));
    }

    @Test
    public void testSetContains() {
        String a = "abc";
        String b = "axy";
        String c = "xyz";

        Set<String> set = createHashSet(equivalence);
        set.add(a);

        // set should now also contain b (because first character the same)
        Assert.assertTrue(set.contains(b));

        set.add(b);

        // adding b should not change the set
        Assert.assertEquals(1, set.size());

        set.add(c);

        Assert.assertEquals(2, set.size());

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
        Assert.assertEquals(set1,set2);

        set1.add(a1);
        set1.add(b1);

        set2.add(b2);
        set2.add(a2);


        Assert.assertEquals(2, set1.size());
        Assert.assertEquals(2, set2.size());


        // test sets with elements, insertion order different
        Assert.assertEquals(set1,set2);

        set1.add(c1);

        Assert.assertNotEquals(set1,set2);


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
        Assert.assertEquals(2,count);
    }

}
