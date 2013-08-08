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
import javolution.util.FastSet;
import javolution.util.function.Equality;

import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class EquivalenceHashSet2Test extends EquivalenceHashSetTest {

    @Override
    public Set<String> createHashSet(final Equivalence<String> equivalence) {
        return new FastSet<>(new Equality<String>() {
            @Override
            public int hashCodeOf(String object) {
                return equivalence.hash(object);
            }

            @Override
            public boolean areEqual(String left, String right) {
                return equivalence.equivalent(left, right);
            }

            @Override
            public int compare(String left, String right) {
                return equivalence.hash(left) - equivalence.hash(right);
            }

            @Override
            public int hashCode() {
                return equivalence.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return obj.hashCode() == hashCode();
            }
        });
    }
}
