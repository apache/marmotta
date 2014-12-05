/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ldpath.model.functions.json;

import com.jayway.jsonpath.JsonPath;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class JsonPathFunction<Node> extends SelectorFunction<Node> {

    private static final Logger log = LoggerFactory.getLogger(JsonPathFunction.class);
    private final StringTransformer<Node> transformer = new StringTransformer<Node>();

    @Override
    protected String getLocalName() {
        return "jsonpath";
    }

    @Override
    public Collection<Node> apply(RDFBackend<Node> rdfBackend, Node context, @SuppressWarnings("unchecked") Collection<Node>... args) throws IllegalArgumentException {

        Set<String> jsonpaths = new HashSet<String>();
        for (Node jsonpath : args[0]) {
            try {
                jsonpaths.add(transformer.transform(rdfBackend,jsonpath, null));
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("First argument must not contain anything else than String-Literals!");
            }
        }

        Iterator<Node> it;
        if(args.length < 2){
            log.debug("Use context {} to execute jsonpaths {}",context,jsonpaths);
            it = Collections.singleton(context).iterator();
        } else {
            log.debug("execute jsonpaths {} on parsed parameters",jsonpaths);
            it = org.apache.marmotta.ldpath.util.Collections.iterator(1,args);
        }

        List<Node> result = new ArrayList<Node>();
        while (it.hasNext()) {
            Node n = it.next();
            try {
                for (String r : doFilter(transformer.transform(rdfBackend,n, null), jsonpaths)) {
                    result.add(rdfBackend.createLiteral(r));
                }
            } catch (IOException e) {
                // This should never happen, since validation is turned off.
            }
        }

        return result;
    }

    private List<String> doFilter(String in, Set<String> jsonpaths) throws IOException {
        List<String> result = new ArrayList<String>();

        for (String jsonpath : jsonpaths) {
            result.add(String.valueOf(JsonPath.read(in, jsonpath)));
        }

        return result;
    }

    @Override
    public String getSignature() {
        return "fn:json(json: String [, nodes: JsonPathList]) : LiteralList";
    }

    @Override
    public String getDescription() {
        return "Evaluate a JSONPath expression on either the value of the context node or the values of the nodes passed as arguments.";
    }
}
