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


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.TestFunction;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;
import org.apache.marmotta.ldpath.api.tests.NodeTest;

public class FunctionTest<Node> extends NodeTest<Node> {

    private final TestFunction<Node> test;
    private final List<NodeSelector<Node>> argSelectors;

    public FunctionTest(TestFunction<Node> test, List<NodeSelector<Node>> argSelectors) {
        this.test = test;
        this.argSelectors = argSelectors;
    }

    @Override
    public boolean accept(RDFBackend<Node> backend, Node context, Node target) throws IllegalArgumentException {

        ArrayList<Collection<Node>> fktArgs = new ArrayList<Collection<Node>>();
        for (NodeSelector<Node> sel : argSelectors) {
            fktArgs.add(sel.select(backend, target, null, null));
        }

        @SuppressWarnings("unchecked")
        final Boolean isAccepted = test.apply(backend, context, fktArgs.toArray(new Collection[argSelectors.size()]));

        return isAccepted;
    }

    @Override
    public String getSignature() {
        return "(function, argument) :: (TestFunction, List<Nodes>) -> Boolean";
    }

    @Override
    public String getDescription() {
        return "Delegate the test to a TestFunction";
    }

    @Override
    public String getPathExpression(NodeBackend<Node> backend) {
        final StringBuilder sb = new StringBuilder("fn:");
        sb.append(test.getLocalName());
        sb.append("(");
        boolean first = true;
        for (NodeSelector<Node> ns : argSelectors) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(ns.getPathExpression(backend));
            first = false;
        }
        return sb.append(")").toString();
    }

}
