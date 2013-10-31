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
package org.apache.marmotta.ldpath.model.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;
import org.apache.marmotta.ldpath.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTextFilterFunction<Node> extends SelectorFunction<Node> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final StringTransformer<Node> transformer = new StringTransformer<Node>();

    /**
     * Apply the function to the list of nodes passed as arguments and return the result as type T.
     * Throws IllegalArgumentException if the function cannot be applied to the nodes passed as argument
     * or the number of arguments is not correct.
     *
     * @param args a nested list of KiWiNodes
     * @return
     */
    @Override
    public Collection<Node> apply(RDFBackend<Node> rdfBackend, Node context, Collection<Node>... args) throws IllegalArgumentException {
        if(args.length < 1){
            log.debug("filter text from context {}",context);
            return java.util.Collections.singleton(
                    rdfBackend.createLiteral(doFilter(transformer.transform(rdfBackend, context, null))));
        } else {
            log.debug("filter text from parameters");
            Iterator<Node> it = Collections.iterator(args);
            List<Node> result = new ArrayList<Node>();
            while (it.hasNext()) {
                result.add(rdfBackend.createLiteral(doFilter(transformer.transform(rdfBackend, it.next(), null))));
            }
            return result;
        }
    }

    protected abstract String doFilter(String in);

    /**
     * A string describing the signature of this node function, e.g. "fn:content(uris : Nodes) : Nodes". The
     * syntax for representing the signature can be chosen by the implementer. This method is for informational
     * purposes only.
     */
    @Override
    public final String getSignature() {
        return String.format("fn:%s(content: LiteralList) : LiteralList", getLocalName());
    }
}