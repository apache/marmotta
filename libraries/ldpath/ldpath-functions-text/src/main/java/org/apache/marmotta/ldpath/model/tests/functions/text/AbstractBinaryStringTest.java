/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ldpath.model.tests.functions.text;

import java.util.Collection;

import com.google.common.collect.Collections2;

/**
 * Abstract base class for binary string tests.
 * @author Jakob Frank <jakob@apache.org>
 *
 */
public abstract class AbstractBinaryStringTest<Node> extends AbstractStringTest<Node> {

    @Override
    protected boolean test(AbstractStringTest<Node>.ToStringFunction toStringFunction, Collection<Node>... args) {
        final Collection<Node> l = args[0], r = args[1];
        
        try {
            for (String first: Collections2.transform(l, toStringFunction)) {
                for (String second: Collections2.transform(r, toStringFunction)) {
                    if (!test(first, second)) return false;
                }
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Do the test.
     * @param first the first (left) argument
     * @param second the second (right) argument
     */
    protected abstract boolean test(String first, String second);
    
    @Override
    protected final int getArgCount() {
        return 2;
    }

}
