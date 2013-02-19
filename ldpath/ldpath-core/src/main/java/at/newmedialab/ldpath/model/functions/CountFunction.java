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
package at.newmedialab.ldpath.model.functions;


import at.newmedialab.ldpath.api.backend.RDFBackend;
import at.newmedialab.ldpath.api.functions.SelectorFunction;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;

public class CountFunction<Node> extends SelectorFunction<Node> {

    private final URI dataType = URI.create("http://www.w3.org/2001/XMLSchema#integer");

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context, Collection<Node>... args) throws IllegalArgumentException {

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
