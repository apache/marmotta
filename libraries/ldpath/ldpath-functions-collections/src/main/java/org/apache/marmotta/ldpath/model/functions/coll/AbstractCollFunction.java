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
package org.apache.marmotta.ldpath.model.functions.coll;


import java.util.Collection;

import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;

public abstract class AbstractCollFunction<Node> extends SelectorFunction<Node> {

    protected static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    protected int getIndex(NodeBackend<Node> backend, Collection<Node> collection) {
        if (collection.size() != 1) { throw new IllegalArgumentException("index must be a singel integer literal"); }
        return backend.intValue(collection.iterator().next());
    }

    protected boolean hasType(RDFBackend<Node> backend, Node node, String type) {
        return backend.listObjects(node, backend.createURI(RDF + "type")).contains(backend.createURI(type));
    }

    protected boolean isNil(NodeBackend<Node> backend, Node node) {
        return backend.isURI(node) && backend.stringValue(node).equals(RDF + "nil");
    }

}
