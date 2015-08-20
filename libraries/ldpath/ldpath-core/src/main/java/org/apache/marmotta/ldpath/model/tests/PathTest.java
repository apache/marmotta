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
package org.apache.marmotta.ldpath.model.tests;


import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;
import org.apache.marmotta.ldpath.api.tests.NodeTest;

import java.util.Collection;

/**
 * Tests whether the path given as argument for the constructor yields at least one node when evaluated
 * from the context node to which the test is applied.
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public class PathTest<Node> extends NodeTest<Node> {

    private final NodeSelector<Node> path;

    public PathTest(NodeSelector<Node> path) {
        this.path = path;
    }

    /**
     * Apply the function to the list of nodes passed as arguments and return the result as type T.
     * Throws IllegalArgumentException if the function cannot be applied to the nodes passed as argument
     * or the number of arguments is not correct.
     *
     * @return
     */
    @Override
    public boolean accept(RDFBackend<Node> rdfBackend, Node context, Node candidate) throws IllegalArgumentException {

        if (rdfBackend.isURI(candidate) || rdfBackend.isBlank(candidate)) {
            Collection<Node> testResult = path.select(rdfBackend, candidate,null,null);
            return testResult.size() > 0;
        } else {
            return false;
        }

    }

    /**
     * Return the representation of the NodeFunction or NodeSelector in the RDF Path Language
     *
     * @param rdfBackend
     * @return
     */
    @Override
    public String getPathExpression(NodeBackend<Node> rdfBackend) {
        return path.getPathExpression(rdfBackend);
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
        return "nodes :: NodeList -> Boolean";
    }

    /**
     * A short human-readable description of what the node function does.
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Tests whether the node list is non-empty";
    }

    /**
     * Get the Path/Selector to check existence for
     * @return the Selector to check.
     */
    public NodeSelector<Node> getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        @SuppressWarnings("rawtypes")
        PathTest pathTest = (PathTest) o;

        if (path != null ? !path.equals(pathTest.path) : pathTest.path != null) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}
