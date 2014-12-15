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
package org.apache.marmotta.ldpath.model.functions.text;


import java.util.Collection;
import java.util.Collections;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;

/**
 * Join the string representation of all provided nodes to a single string literal.
 * 
 */
public class StrJoinFunction<Node> extends SelectorFunction<Node> {

    private final StringTransformer<Node> transformer = new StringTransformer<Node>();

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context, Collection<Node>... args) throws IllegalArgumentException {
        if ((args.length < 2 || args.length > 4)) {
            throw new IllegalArgumentException("wrong usage: " + getSignature());
        }
        if (args[1].size() != 1 || (args.length > 2 && args[2].size() != 1) || (args.length > 3 && args[3].size() != 1)) {
            throw new IllegalArgumentException("wrong usage: " + getSignature());
        }

        Collection<Node> nodes = args[0];
        String join = transformer.transform(backend, args[1].iterator().next(), null);
        String prefix = args.length > 2 ? transformer.transform(backend, args[2].iterator().next(), null):"";
        String suffix = args.length > 3 ? transformer.transform(backend, args[3].iterator().next(), null):"";

        final StringBuilder sb = new StringBuilder(prefix);
        boolean first = true;
        for (final Node node : nodes) {
            final String string = backend.stringValue(node);
            if (!first) sb.append(join);
            sb.append(string);
            first = false;
        }
        sb.append(suffix);

        return Collections.singleton(backend.createLiteral(sb.toString()));

    }

    @Override
    public String getSignature() {
        return "fn:strJoin(nodes: NodeList, separator: String [, prefix: String [, suffix: String ]]) :: LiteralList";
    }

    @Override
    public String getDescription() {
        return "join the string representation of all provided nodes.";
    }

    @Override
    public String getLocalName() {
        return "strJoin";
    }

}
