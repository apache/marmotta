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
import org.apache.marmotta.ldpath.model.selectors.PropertySelector;

/**
 * Shortcut for {@link PathEqualityTest} with the property {@literal http://www.w3.org/1999/02/22-rdf-syntax-ns#type}
 * @author Jakob Frank <jakob@apache.org>
 *
 * @param <Node>
 */
public class IsATest<Node> extends PathEqualityTest<Node> {

    public IsATest(Node rdfType, Node node) {
        super(new PropertySelector<Node>(rdfType), node);
    }
    
    @Override
    public String getPathExpression(NodeBackend<Node> rdfBackend) {
        if (rdfBackend.isURI(node)) {
            return String.format("is-a <%s>", rdfBackend.stringValue(node));
        } else if (rdfBackend.isLiteral(node)) {
            return String.format("is-a \"%s\"", rdfBackend.stringValue(node));
        } else {
            // TODO Can this happen?
            return String.format("is-a %s", rdfBackend.stringValue(node));
        }
    }
    
    @Override
    public String getSignature() {
        return "is-a Node :: NodeList -> Boolean";
    }
    
    @Override
    public String getDescription() {
        return "tests if a node has a certain type";
    }
    
}
