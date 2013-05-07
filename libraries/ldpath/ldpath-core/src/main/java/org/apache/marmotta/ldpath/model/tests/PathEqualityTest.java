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
package org.apache.marmotta.ldpath.model.tests;

import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;
import org.apache.marmotta.ldpath.api.tests.NodeTest;

/**
 * Checks whether a path selector contains a certain node.  Used for the syntax construct
 *
 * <path> is <node>
 *
 * e.g.
 *
 * rdf:type is skos:Concept
 *
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public class PathEqualityTest<Node> extends NodeTest<Node> {

    private NodeSelector<Node> path;
    private Node node;


    public PathEqualityTest(NodeSelector<Node> path, Node node) {
        this.node = node;
        this.path = path;
    }

    /**
     * Apply the function to the list of nodes passed as arguments and return the result as type T.
     * Throws IllegalArgumentException if the function cannot be applied to the nodes passed as argument
     * or the number of arguments is not correct.
     *
     * @param args a nested list of KiWiNodes
     * @return
     */
    @Override
    public boolean accept(RDFBackend<Node> rdfBackend, Node context, Node candidate) throws IllegalArgumentException {
        return path.select(rdfBackend, candidate,null,null).contains(node);
    }

    /**
     * Return the representation of the NodeFunction or NodeSelector in the RDF Path Language
     *
     * @param rdfBackend
     * @return
     */
    @Override
    public String getPathExpression(NodeBackend<Node> rdfBackend) {
        if (rdfBackend.isURI(node)) {
            return String.format("%s is <%s>", path.getPathExpression(rdfBackend), rdfBackend.stringValue(node));
        } else if (rdfBackend.isLiteral(node)) {
            return String.format("%s is \"%s\"", path.getPathExpression(rdfBackend), rdfBackend.stringValue(node));
        } else {
            // TODO Can this happen?
            return String.format("%s is %s", path.getPathExpression(rdfBackend), rdfBackend.stringValue(node));
        }
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
        return "nodes is nodes :: (NodeList,NodeList) -> Boolean";
    }

    /**
     * A short human-readable description of what the node function does.
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Tests whether the two node lists intersect";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        @SuppressWarnings("rawtypes")
        PathEqualityTest that = (PathEqualityTest) o;

        if (node != null ? !node.equals(that.node) : that.node != null) {
            return false;
        }
        if (path != null ? !path.equals(that.path) : that.path != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (node != null ? node.hashCode() : 0);
        return result;
    }
}
