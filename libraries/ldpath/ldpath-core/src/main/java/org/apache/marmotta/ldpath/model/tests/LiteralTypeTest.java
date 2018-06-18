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

import java.net.URI;

/**
 * Literal type tests allow to select only literals of a specified type, e.g. to ensure that only decimal values are
 * retrieved:
 * <p/>
 * <code>
 * ^^TYPE
 * </code>
 * <p/>
 * where TYPE is the XML Schema type to select.
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public class LiteralTypeTest<Node> extends NodeTest<Node> {

    private final URI typeUri;

    public LiteralTypeTest(String typeUri) {
        this.typeUri = URI.create(typeUri);
    }
    
    public LiteralTypeTest(URI uri) {
        this.typeUri = uri;
    }

    /**
     * Apply the function to the list of nodes passed as arguments and return the result as type T.
     * Throws IllegalArgumentException if the function cannot be applied to the nodes passed as argument
     * or the number of arguments is not correct.
     *
     * @return
     */
    @Override
    public boolean accept(RDFBackend<Node> rdfBackend, Node context, Node node) throws IllegalArgumentException {

        if(rdfBackend.isLiteral(node)) {

            if(typeUri != null) {
                return typeUri.equals(rdfBackend.getLiteralType(node));
            } else {
                // FIXME: MARMOTTA-39
                return null == rdfBackend.getLiteralType(node);
            }
        } else {
            return false;
        }

    }

    /**
     * Return the name of the NodeFunction for registration in the function registry
     *
     * @param rdfBackend
     * @return
     */
    @Override
    public String getPathExpression(NodeBackend<Node> rdfBackend) {
        return String.format("^^<%s>", typeUri);
    }

    /**
     * A string describing the signature of this node function, e.g. "fn:content(uris : Nodes) : Nodes". The
     * syntax for representing the signature can be chosen by the implementer. This method is for informational
     * purposes only.
     *
     * @return the Signature (human readable)
     */
    @Override
    public String getSignature() {
        return "nodes [^^typeUri] :: (NodeList, URI) -> Boolean";
    }

    /**
     * A short human-readable description of what the node function does.
     *
     * @return A short human-readable description of what the node function does.
     */
    @Override
    public String getDescription() {
        return "Tests the types of the nodes passed as argument";
    }

    /**
     * Get the type (uri) to test for.
     * @return the type to test for.
     */
    public URI getTypeUri() {
        return typeUri;
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
        LiteralTypeTest that = (LiteralTypeTest) o;

        return typeUri != null ? typeUri.equals(that.typeUri) : that.typeUri == null;

    }

    @Override
    public int hashCode() {
        return typeUri != null ? typeUri.hashCode() : 0;
    }
}
