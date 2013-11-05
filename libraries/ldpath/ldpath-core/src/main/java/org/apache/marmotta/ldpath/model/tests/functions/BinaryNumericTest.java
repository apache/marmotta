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
package org.apache.marmotta.ldpath.model.tests.functions;


import java.util.Collection;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.TestFunction;
import org.apache.marmotta.ldpath.model.transformers.DoubleTransformer;

public abstract  class BinaryNumericTest<Node> extends TestFunction<Node> {


    protected final DoubleTransformer<Node> transformer = new DoubleTransformer<Node>();

    @Override
    @SafeVarargs
    public final Boolean apply(RDFBackend<Node> backend, Node context,
            Collection<Node>... args) throws IllegalArgumentException {

        if (args.length != 2) { throw new IllegalArgumentException(getLocalName() + " is a binary function and therefor requires exactly two arguments"); }

        Collection<Node> leftArgs = args[0];
        Collection<Node> rightArgs = args[1];

        for (Node lN : leftArgs) {
            for (Node rN : rightArgs) {
                if (!test(backend, lN, rN)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean test(RDFBackend<Node> backend, Node leftNode, Node rightNode) {
        try {
            return test(transformer.transform(backend, leftNode, null), transformer.transform(backend, rightNode, null));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    protected abstract boolean test(Double left, Double right);

    @Override
    public String getSignature() {
        return "fn:"+getLocalName()+"(NumericNode a, NumericNode b) :: Boolean";
    }

}
