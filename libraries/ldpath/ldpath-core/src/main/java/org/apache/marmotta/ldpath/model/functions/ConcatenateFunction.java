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
package org.apache.marmotta.ldpath.model.functions;


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;

/**
 * A node function concatenating a list of nodes interpreted as strings.
 *
 * @param <Node> the node type used by the backend
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public class ConcatenateFunction<Node> extends SelectorFunction<Node> {

    private final StringTransformer<Node> transformer = new StringTransformer<Node>();

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
        Iterator<Node> it = org.apache.marmotta.ldpath.util.Collections.iterator(args);
        StringBuilder result = new StringBuilder();
        while (it.hasNext()) {
            result.append(transformer.transform(rdfBackend,it.next(), null));
        }

        return Collections.singleton(rdfBackend.createLiteral(result.toString()));
    }

    /**
     * Return the name of the NodeFunction for registration in the function registry
     *
     * @return
     */
    @Override
    public String getLocalName() {
        return "concat";
    }

    /**
     * A string describing the signature of this node function, e.g. "fn:content(uris : Nodes) : Nodes". The
     * syntax for representing the signature can be chosen by the implementer. This method is for informational
     * purposes only.
     *
     * @return
     */
    @Override
    public String getSignature() {
        return "fn:concat(nodes : NodeList) : String";
    }

    /**
     * A short human-readable description of what the node function does.
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "A node function concatenating a list of nodes interpreted as strings.";
    }
}
