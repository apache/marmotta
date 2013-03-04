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
package org.apache.marmotta.ldpath.model.functions.text;


import java.util.Collection;
import java.util.LinkedList;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;

public class SubstringFunction<Node> extends SelectorFunction<Node> {

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context, Collection<Node>... args) throws IllegalArgumentException {
        try {
            if (args.length < 2 || args.length > 3) { throw new IllegalArgumentException("LdPath function " + getLocalName() + " requires 2 or 3 arguments"); }
            if (args[1].size() != 1) { throw new IllegalArgumentException("start argument must be a single literal for function " + getLocalName()); }
            if (args.length > 2 && args[2].size() != 1) { throw new IllegalArgumentException("end argument must be a single literal for function " + getLocalName()); }

            final Collection<Node> nodes = args[0];
            final int start = Math.max(backend.intValue(args[1].iterator().next()), 0);
            final int end;
            if (args.length > 2) {
                end = Math.max(backend.intValue(args[2].iterator().next()), 0);
            } else {
                end = Integer.MAX_VALUE;
            }

            if (end < start) {
                throw new IllegalArgumentException(getLocalName() + " does not allow end beeing smaller than start (end:" + end + " < start:" + start + ")");
            }

            final Collection<Node> result = new LinkedList<Node>();
            for (Node node : nodes) {
                final String str = backend.stringValue(node);
                result.add(backend.createLiteral(str.substring(Math.min(start, str.length()), Math.min(end, str.length()))));
            }

            return result;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(nfe);
        } catch (ArithmeticException ae) {
            throw new IllegalArgumentException(ae);
        }
    }

    @Override
    public String getSignature() {
        return "fn:substr(node::Node, start::NumericLiteral [, end::NumericLiteral]) :: StringLiteral";
    }

    @Override
    public String getDescription() {
        return "Extract a substring for the string representation of a node";
    }

    @Override
    public String getLocalName() {
        return "substr";
    }

}
