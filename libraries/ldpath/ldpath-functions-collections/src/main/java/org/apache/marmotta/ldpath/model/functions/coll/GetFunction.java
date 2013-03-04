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
import java.util.Collections;
import java.util.HashSet;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;

public class GetFunction<Node> extends AbstractCollFunction<Node> {

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context, Collection<Node>... args) throws IllegalArgumentException {
        final Collection<Node> nodes;
        final int index;
        switch (args.length) {
        case 1:
            nodes = Collections.singleton(context);
            index = getIndex(backend, args[0]);
            break;
        case 2:
            nodes = args[0];
            index = getIndex(backend, args[1]);
            break;
        default:
            throw new IllegalArgumentException(getLocalName() + " must not have more than one or two parameters");
        }

        Collection<Node> result = new HashSet<Node>();
        for (Node node : nodes) {
            if (hasType(backend, node, RDF + "Bag")) {
                result.addAll(getFromContainer(backend, node, index));
            } else if (hasType(backend, node, RDF + "Seq")) {
                result.addAll(getFromContainer(backend, node, index));
            } else if (hasType(backend, node, RDF + "List")) {
                result.addAll(getFromCollection(backend, node, index));
            } else {
                result.addAll(getFromCollection(backend, node, index));
            }
        }
        return result;
    }

    private Collection<Node> getFromCollection(RDFBackend<Node> backend, Node node, int index) {
        if (index < 0 || isNil(backend, node)) {
            return Collections.emptySet();
        } else if (index == 0) {
            return backend.listObjects(node, backend.createURI(RDF + "first"));
        } else {
            HashSet<Node> result = new HashSet<Node>();
            for (Node n : backend.listObjects(node, backend.createURI(RDF + "rest"))) {
                result.addAll(getFromCollection(backend, n, index - 1));
            }
            return result;
        }
    }

    private Collection<Node> getFromContainer(RDFBackend<Node> backend, Node node, int index) {
        return backend.listObjects(node, backend.createURI(RDF + "_" + (index + 1)));
    }

    @Override
    public String getSignature() {
        return "fn:get([nodes: NodeList,] n: Integer) :: NodeList";
    }

    @Override
    public String getDescription() {
        return "retrieve the nth element from a rdf-Collection (0-based)";
    }

    @Override
    protected String getLocalName() {
        return "get";
    }

}
