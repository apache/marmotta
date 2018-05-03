/*
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

import com.google.common.collect.Collections2;

import java.util.Collection;

/**
 * LDPath test function to check if the string representation of an node is empty.
 * 
 * @author Jakob Frank <jakob@apache.org>
 *
 */
public class StringIsEmptyTest<Node> extends AbstractStringTest<Node> {

    @Override
    public String getDescription() {
        return "Check if the string representation of the node is empty";
    }

    @SafeVarargs
    @Override
    protected final boolean test(AbstractStringTest<Node>.ToStringFunction toStringFunction, Collection<Node>... args) {
        try {
            for (String str: Collections2.transform(args[0], toStringFunction)) {
                if (!str.isEmpty()) return false;
            }
            
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
        
    }

    @Override
    protected final int getArgCount() {
        return 1;
    }

    @Override
    public String getLocalName() {
        return "isEmpty";
    }

}
