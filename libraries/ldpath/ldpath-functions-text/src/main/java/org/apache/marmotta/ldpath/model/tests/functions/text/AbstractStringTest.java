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
package org.apache.marmotta.ldpath.model.tests.functions.text;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.TestFunction;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;

import java.util.Collection;

/**
 * Abstract base class for LDPath Test functions that work on the string representation of nodes.
 * @author Jakob Frank <jakob@apache.org>
 * @see TestFunction
 */
public abstract class AbstractStringTest<Node> extends TestFunction<Node> {

    protected final StringTransformer<Node> transformer = new StringTransformer<>();

    @SafeVarargs
    @Override
    public final Boolean apply(RDFBackend<Node> backend, Node context,
                               Collection<Node>... args) throws IllegalArgumentException {

        if (args.length != getArgCount()) { throw new IllegalArgumentException(getLocalName() + " requires exactly " + getArgCount() + " arguments"); }

        return test(new ToStringFunction(backend), args);
    }

    /**
     * 
     * @param toStringFunction
     * @param args
     */
    protected abstract boolean test(ToStringFunction toStringFunction, Collection<Node>... args);

    /**
     * The signature of this functions (how to use it)
     */
    @Override
    public String getSignature() {
        return "fn:" + getLocalName() + "(" + getArgSignature() + ") :: Boolean";
    }

    private String getArgSignature() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getArgCount(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append("Node ").append('a' + i);
        }
        return sb.toString();
    }

    /**
     * Number of (required) arguments this function accepts.
     * @return
     */
    protected abstract int getArgCount();

    /**
     * A function for the guava-library to convert a node into a string based on
     * the backend provided in the constructor.
     * 
     * @author Jakob Frank <jakob@apache.org>
     * @see Collections2
     * @see Function
     */
    protected class ToStringFunction implements Function<Node, String> {
        private final RDFBackend<Node> backend;

        /**
         * @param backend the RDFBackend to use for transformation
         */
        public ToStringFunction(RDFBackend<Node> backend) {
            this.backend = backend;
        }

        @Override
        public String apply(Node n) {
            return transformer.transform(backend, n, null);
        }
    }
}
