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

public class SumFunction<Node> extends MathFunction<Node> {

    protected final DoubleTransformer<Node> doubleTransformer = new DoubleTransformer<Node>();
    protected final URI doubleType = URI.create(Constants.NS_XSD + "double");

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context,
            Collection<Node>... args) throws IllegalArgumentException {

        ArrayList<Node> result = new ArrayList<Node>();
        for (Collection<Node> arg : args) {
            Node res = calc(backend, arg);
            if (res != null) {
                result.add(res);
            }
        }
        return result;
    }

    protected Node calc(RDFBackend<Node> backend, Collection<Node> arg) {
        /* SUM */
        Double d = 0d;
        for (Node n : arg) {
            try {
                Double val = doubleTransformer.transform(backend, n, null);
                d += val.doubleValue();
            } catch (IllegalArgumentException e) {
                // we just ignore non-numeric nodes
            }
        }

        return backend.createLiteral(String.valueOf(d), null, doubleType);
    }

    @Override
    public String getSignature() {
        return "fn:sum(LiteralList l [, ...]) :: NumberLiteral(s)";
    }

    @Override
    public String getDescription() {
        return "Sum up each argument";
    }

    @Override
    public String getLocalName() {
        return "sum";
    }

}
