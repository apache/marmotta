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
import org.apache.marmotta.ldpath.api.tests.NodeTest;

/**
 * Tests the disjunction of two tests.
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public class OrTest<Node> extends ComplexTest<Node> {

    private final NodeTest<Node> left;
    private final NodeTest<Node> right;

    public OrTest(NodeTest<Node> left, NodeTest<Node> right) {
        this.left = left;
        this.right = right;
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
    public boolean accept(RDFBackend<Node> rdfBackend, Node context, Node args) throws IllegalArgumentException {
        return left.accept(rdfBackend, context, args) || right.accept(rdfBackend, context, args);
    }

    /**
     * Return the name of the NodeFunction for registration in the function registry
     *
     * @param rdfBackend
     * @return
     */
    @Override
    public String getPathExpression(NodeBackend<Node> rdfBackend) {
        return String.format("%s | %s", left.getPathExpression(rdfBackend), right.getPathExpression(rdfBackend));
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
        return "(left  | right ) :: (Boolean, Boolean) -> Boolean";

    }

    /**
     * A short human-readable description of what the node function does.
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Tests the disjunction of two tests";
    }

    /**
     * Get the left test of this OR
     * @return the left NodeTest
     */
    public NodeTest<Node> getLeft() {
        return left;
    }

    /**
     * Get the right test of this OR
     * @return the right NodeTest
     */
    public NodeTest<Node> getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        @SuppressWarnings("rawtypes")
        OrTest orTest = (OrTest) o;

        if (left != null ? !left.equals(orTest.left) : orTest.left != null) { return false; }
        if (right != null ? !right.equals(orTest.right) : orTest.right != null) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
