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
import org.apache.marmotta.ldpath.api.tests.NodeTest;

public class NotTest<Node> extends NodeTest<Node> {

    private final NodeTest<Node> delegate;

    public NotTest(NodeTest<Node> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean accept(RDFBackend<Node> backend, Node context, Node args) throws IllegalArgumentException {
        return !delegate.accept(backend, context, args);
    }

    @Override
    public String getPathExpression(NodeBackend<Node> backend) {
        if (delegate instanceof ComplexTest<?>) {
            return String.format("!(%s)", delegate.getPathExpression(backend));
        } else {
            return String.format("!%s", delegate.getPathExpression(backend));
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
        return "!test :: Boolean -> Boolean";
    }

    /**
     * A short human-readable description of what the node function does.
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Negates the test given as argument";
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
        NotTest notTest = (NotTest) o;

        if (delegate != null ? !delegate.equals(notTest.delegate) : notTest.delegate != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return delegate != null ? delegate.hashCode() : 0;
    }

}
