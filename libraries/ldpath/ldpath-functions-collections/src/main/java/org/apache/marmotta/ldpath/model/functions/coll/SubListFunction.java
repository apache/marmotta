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
import java.util.LinkedList;
import java.util.List;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;

public class SubListFunction<Node> extends AbstractCollFunction<Node> {

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context, Collection<Node>... args) throws IllegalArgumentException {
        final Collection<Node> nodes;
        final int start, end;
        switch (args.length) {
        case 1:
            nodes = Collections.singleton(context);
            start = getIndex(backend, args[0]);
            end = Integer.MAX_VALUE;
            break;
        case 2:
            if (args[0].size() == 1 && backend.isLiteral(args[0].iterator().next())) {
                nodes = Collections.singleton(context);
                start = getIndex(backend, args[0]);
                end = getIndex(backend, args[1]);
            } else {
                nodes = args[0];
                start = getIndex(backend, args[1]);
                end = Integer.MAX_VALUE;
            }
            break;
        case 3:
            nodes = args[0];
            start = getIndex(backend, args[1]);
            end = getIndex(backend, args[2]);
            break;
        default:
            throw new IllegalArgumentException(getLocalName() + " takes at most 3 arguments");
        }

        final List<Node> result = new LinkedList<Node>();
        for (Node node : nodes) {
            if (hasType(backend, node, RDF + "Bag")) {
                result.addAll(subListFromContainer(backend, node, start, end));
            } else if (hasType(backend, node, RDF + "Seq")) {
                result.addAll(subListFromContainer(backend, node, start, end));
            } else if (hasType(backend, node, RDF + "List")) {
                subListFromCollection(backend, node, start, end, result);
            } else {
                subListFromCollection(backend, node, start, end, result);
            }
        }

        return result;
    }

    private void subListFromCollection(RDFBackend<Node> backend, Node node, int start, int end, List<Node> result) {
        if (isNil(backend, node)) { return; }

        if (end > 0) {
            if (start <= 0) {
                result.addAll(backend.listObjects(node, backend.createURI(RDF + "first")));
            }
            for (Node n : backend.listObjects(node, backend.createURI(RDF + "rest"))) {
                subListFromCollection(backend, n, start - 1, end - 1, result);
            }
        }

    }

    private Collection<? extends Node> subListFromContainer(RDFBackend<Node> backend, Node node, int start, int end) {
        List<Node> result = new LinkedList<Node>();
        for (int i = start; i < end; i++) {
            final Collection<Node> objects = backend.listObjects(node, backend.createURI(RDF + "_" + (i + 1)));
            if (objects.size() > 0) {
                result.addAll(objects);
            } else {
                break;
            }
        }
        return result;
    }

    @Override
    public String getSignature() {
        return "fn:subList([nodes: NodeList,] start: Integer [, end: Integer]) :: NodeList";
    }

    @Override
    public String getDescription() {
        return "select a range from a rdf-Collection (like substring does for strings, 0-based)";
    }

    @Override
    protected String getLocalName() {
        return "subList";
    }

}
