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
package at.newmedialab.ldpath.model.functions.math;

import at.newmedialab.ldpath.api.backend.RDFBackend;
import at.newmedialab.ldpath.model.Constants;
import at.newmedialab.ldpath.model.transformers.DoubleTransformer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public class RoundFunction<Node> extends MathFunction<Node> {

    protected final DoubleTransformer<Node> doubleTransformer = new DoubleTransformer<Node>();
    protected final URI intType = URI.create(Constants.NS_XSD + "integer");

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context,
            Collection<Node>... args) throws IllegalArgumentException {
        if (args.length != 1) {
            throw new IllegalArgumentException("round takes only one argument");
        }

        ArrayList<Node> result = new ArrayList<Node>();
        for (Node node : args[0]) {
            Node res = calc(backend, node);
            if (res != null) {
                result.add(res);
            }
        }
        return result;
    }

    protected Node calc(RDFBackend<Node> backend, Node node) {
        /* SUM */
        try {
            Double val = doubleTransformer.transform(backend, node);
            return backend.createLiteral(String.valueOf(Math.round(val)), null,
                    intType);
        } catch (IllegalArgumentException e) {
            return null;
        }

    }

    @Override
    public String getDescription() {
        return "Round each argument to the closest int/long value";
    }

    @Override
    public String getSignature() {
        return "fn:round(LiteralList l) :: IntegerLiteralList";
    }

    @Override
    public String getLocalName() {
        return "round";
    }

}
