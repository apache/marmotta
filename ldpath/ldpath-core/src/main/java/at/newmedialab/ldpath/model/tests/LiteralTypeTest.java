/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.ldpath.model.tests;

import at.newmedialab.ldpath.api.backend.RDFBackend;
import at.newmedialab.ldpath.api.tests.NodeTest;

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

    private String typeUri;

    public LiteralTypeTest(String typeUri) {
        this.typeUri = typeUri;
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
    public boolean accept(RDFBackend<Node> rdfBackend, Node context, Node node) throws IllegalArgumentException {

        if(rdfBackend.isLiteral(node)) {

            if(typeUri != null) {
                return typeUri.equals(rdfBackend.getLiteralType(node).toString());
            } else {
                return null == rdfBackend.getLiteralType(node).toString();
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
    public String getPathExpression(RDFBackend<Node> rdfBackend) {
        return "^^" + typeUri;
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
        return "nodes [^^typeUri] :: (NodeList, URI) -> Boolean";
    }

    /**
     * A short human-readable description of what the node function does.
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Tests the types of the nodes passed as argument";
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

        if (typeUri != null ? !typeUri.equals(that.typeUri) : that.typeUri != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return typeUri != null ? typeUri.hashCode() : 0;
    }
}
