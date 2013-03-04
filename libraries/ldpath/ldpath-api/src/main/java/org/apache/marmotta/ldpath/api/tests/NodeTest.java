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
package org.apache.marmotta.ldpath.api.tests;


import java.util.Collection;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.NodeFunction;

/**
 * Node tests take a node as argument and return a boolean if the node matches the test.
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public abstract class NodeTest<Node> implements NodeFunction<Boolean, Node> {

    @Override
    public final Boolean apply(RDFBackend<Node> backend, Node context, Collection<Node>... args)
            throws IllegalArgumentException {

        if (args.length != 1 || args[0].isEmpty()) { throw new IllegalArgumentException("a test only takes one parameter"); }
        if (args[0].size() != 1) { throw new IllegalArgumentException("a test can only be applied to a single node"); }

        Node node = args[0].iterator().next();

        return accept(backend, context, node);
    }

    public abstract boolean accept(RDFBackend<Node> backend, Node context, Node candidate) throws IllegalArgumentException;

}
