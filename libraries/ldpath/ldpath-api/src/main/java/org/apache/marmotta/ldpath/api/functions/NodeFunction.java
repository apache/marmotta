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
package org.apache.marmotta.ldpath.api.functions;


import org.apache.marmotta.ldpath.api.LDPathConstruct;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;

import java.util.Collection;

/**
 * A function applied to nodes.
 *
 * @param <T> the return type of the function
 * @param <Node> the node type of the function
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public interface NodeFunction<T,Node> extends LDPathConstruct<Node> {

    /**
     * Apply the function to the list of nodes passed as arguments and return the result as type T.
     * Throws IllegalArgumentException if the function cannot be applied to the nodes passed as argument
     * or the number of arguments is not correct.
     *
     * @param context the context of the execution. Same as using the 
     * {@link NodeSelector} '.' as parameter.
     * @param args a nested list of KiWiNodes
     * @return
     */
    T apply(RDFBackend<Node> backend, Node context, @SuppressWarnings("unchecked") Collection<Node>... args) throws IllegalArgumentException;

    /**
     * A string describing the signature of this node function, e.g. "fn:content(uris : Nodes) : Nodes". The
     * syntax for representing the signature can be chosen by the implementer. This method is for informational
     * purposes only.
     * @return
     */
    String getSignature();

    /**
     * A short human-readable description of what the node function does.
     * @return
     */
    String getDescription();
}
