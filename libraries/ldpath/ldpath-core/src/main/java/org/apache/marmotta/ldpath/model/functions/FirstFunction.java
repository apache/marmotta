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
package org.apache.marmotta.ldpath.model.functions;


import java.util.Collection;
import java.util.Collections;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;

/**
 * Selects the <code>first</code> node in the argument list.
 *
 *
 * @param <Node> the node type used by the backend
 * @author Jakob Frank <jakob@apache.org>
 * 
 */
public class FirstFunction<Node> extends SelectorFunction<Node> {


    /**
     * Apply the function to the list of nodes passed as arguments and return the result as type T.
     * Throws IllegalArgumentException if the function cannot be applied to the nodes passed as argument
     * or the number of arguments is not correct.
     *
     * @param args a nested list of KiWiNodes
     * @return
     */
    @Override
    @SafeVarargs
    public final Collection<Node> apply(RDFBackend<Node> rdfBackend, Node context, Collection<Node>... args) throws IllegalArgumentException {
        for (Collection<Node> arg : args) {
            if (arg.size() > 0) { return arg; }
        }
        return Collections.emptyList();
    }

    /**
     * Return the name of the NodeFunction for registration in the function registry
     */
    @Override
    public String getLocalName() {
        return "first";
    }

    /**
     * A string describing the signature of this node function, e.g. "fn:content(uris : Nodes) : Nodes". The
     * syntax for representing the signature can be chosen by the implementer. This method is for informational
     * purposes only.
     */
    @Override
    public String getSignature() {
        return "fn:first(nodes : NodeList) : Node";
    }

    /**
     * A short human-readable description of what the node function does.
     */
    @Override
    public String getDescription() {
        return "Selects the first node in the argument list.";
    }
}
