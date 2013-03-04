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
import java.util.LinkedList;
import java.util.List;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;


public class FlattenFunction<Node> extends AbstractCollFunction<Node> {


    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context, Collection<Node>... args) throws IllegalArgumentException {
        if (args.length > 1) {
            throw new IllegalArgumentException(getLocalName() + " must not have more than one parameter");
        }
        final Collection<Node> nodes;
        if (args.length > 0) {
            nodes = args[0];
        } else {
            nodes = Collections.singleton(context);
        }

        final List<Node> result = new LinkedList<Node>();
        for (Node node : nodes) {
            if (hasType(backend, node, RDF + "Bag")) {
                flattenContainer(backend, node, result);
            } else if (hasType(backend, node, RDF + "Seq")) {
                flattenContainer(backend, node, result);
            } else if (hasType(backend, node, RDF + "List")) {
                flattenCollection(backend, node, result, new HashSet<Node>());
            } else {
                flattenCollection(backend, node, result, new HashSet<Node>());
            }
        }

        return result;
    }

    private void flattenCollection(RDFBackend<Node> backend, Node node, Collection<Node> result, Collection<Node> backtrace) {
        if (isNil(backend, node)) {
            return;
        }

        // Stop if we detect a cycle.
        if (!backtrace.add(node)) {
            return;
        }

        // Add the (all) firsts
        result.addAll(backend.listObjects(node, backend.createURI(RDF + "first")));

        // Recursively add the rest
        final Collection<Node> rest = backend.listObjects(node, backend.createURI(RDF + "rest"));
        for (Node r : rest) {
            flattenCollection(backend, r, result, backtrace);
        }
    }

    private void flattenContainer(RDFBackend<Node> backend, Node node, Collection<Node> result) {
        for (int i = 1; /* exit via break */; i++) {
            final Collection<Node> objects = backend.listObjects(node, backend.createURI(RDF + "_" + i));
            if (objects.size() > 0) {
                result.addAll(objects);
            } else {
                break;
            }
        }
    }

    @Override
    public String getSignature() {
        return "fn:flatten([nodes: NodeList]) :: NodeList";
    }

    @Override
    public String getDescription() {
        return "Flattens a rdf-Collection";
    }

    @Override
    protected String getLocalName() {
        return "flatten";
    }

}
