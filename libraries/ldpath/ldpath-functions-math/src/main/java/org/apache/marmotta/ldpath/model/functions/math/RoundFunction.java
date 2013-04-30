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
package org.apache.marmotta.ldpath.model.functions.math;


import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.model.Constants;
import org.apache.marmotta.ldpath.model.transformers.DoubleTransformer;

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
            Double val = doubleTransformer.transform(backend, node, null);
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
