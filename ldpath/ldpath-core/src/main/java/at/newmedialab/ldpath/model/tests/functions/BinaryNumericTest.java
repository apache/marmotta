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
package at.newmedialab.ldpath.model.tests.functions;

import at.newmedialab.ldpath.api.backend.RDFBackend;
import at.newmedialab.ldpath.api.functions.TestFunction;
import at.newmedialab.ldpath.model.transformers.DoubleTransformer;

import java.util.Collection;

public abstract  class BinaryNumericTest<Node> extends TestFunction<Node> {


    protected final DoubleTransformer<Node> transformer = new DoubleTransformer<Node>();

    @Override
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
            return test(transformer.transform(backend, leftNode), transformer.transform(backend, rightNode));
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
