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
package org.apache.marmotta.ldpath.model.functions;



import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;

public class CountFunction<Node> extends SelectorFunction<Node> {

    private final URI dataType = URI.create("http://www.w3.org/2001/XMLSchema#integer");

    @Override
    @SafeVarargs
    public final Collection<Node> apply(RDFBackend<Node> backend, Node context, Collection<Node>... args) throws IllegalArgumentException {

        LinkedList<Node> result = new LinkedList<Node>();
        for (Collection<Node> coll : args) {
            final Node intLit = backend.createLiteral(String.valueOf(coll.size()), null, dataType);
            result.add(intLit);
        }

        return result;
    }

    @Override
    public String getSignature() {
        return "fn:count(nodes : URIResourceList, ...) : IntegerLiteralList";
    }

    @Override
    public String getDescription() {
        return "Counts the number of resources in the arguments";
    }

    @Override
    public String getLocalName() {
        return "count";
    }

}
