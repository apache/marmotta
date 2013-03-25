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
package org.apache.marmotta.platform.ldpath.model.functions;

import org.apache.marmotta.platform.ldpath.api.AutoRegisteredLDPathFunction;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Selector.SelectorParseException;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class JsoupFunction extends AutoRegisteredLDPathFunction {

    private static final Logger               log         = LoggerFactory.getLogger(JsoupFunction.class);

    private final StringTransformer<Value> transformer = new StringTransformer<Value>();

    /**
     * Apply the function to the list of nodes passed as arguments and return the result as type T.
     * Throws IllegalArgumentException if the function cannot be applied to the nodes passed as
     * argument
     * or the number of arguments is not correct.
     * 
     * @param args a nested list of KiWiNodes
     * @return
     */
    @Override
    public Collection<Value> apply(RDFBackend<Value> rdfBackend, Value context, Collection<Value>... args)
            throws IllegalArgumentException {
        if (args.length < 1) throw new IllegalArgumentException("CSS-Selector is required as first argument.");
        Set<String> jsoupSelectors = new HashSet<String>();
        for (Value xpath : args[0]) {
            try {
                jsoupSelectors.add(transformer.transform(rdfBackend, xpath, null));
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("First argument must not contain anything else than String-Literals!");
            }
        }
        Iterator<Value> it;
        if (args.length < 2) {
            log.debug("Use context {} to apply css-selector {}", context, jsoupSelectors);
            it = Collections.singleton(context).iterator();
        } else {
            log.debug("apply css-selector {} on parsed parameters", jsoupSelectors);
            it = org.apache.marmotta.ldpath.util.Collections.iterator(1, args);
        }
        List<Value> result = new ArrayList<Value>();
        while (it.hasNext()) {
            Value n = it.next();
            try {
                final String string = transformer.transform(rdfBackend, n, null);
                final Document jsoup = Jsoup.parse(string);
                if (rdfBackend.isURI(context)) {
                    jsoup.setBaseUri(rdfBackend.stringValue(context));
                }
                for (String r : doFilter(jsoup, jsoupSelectors)) {
                    result.add(rdfBackend.createLiteral(r));
                }
            } catch (IOException e) {
                // This should never happen, since validation is turned off.
            }
        }

        return result;
    }

    private LinkedList<String> doFilter(Document jsoup, Set<String> jsoupSelectors) throws IOException {
        LinkedList<String> result = new LinkedList<String>();
        for (String jsoupSel : jsoupSelectors) {
            try {
                for (Element e : jsoup.select(jsoupSel)) {
                    result.add(e.outerHtml());
                }
            } catch (SelectorParseException xpe) {
                throw new IllegalArgumentException("error while processing jsoup selector: '" + jsoupSel + "'", xpe);
            }
        }
        return result;
    }

    /**
     * Return the name of the NodeFunction for registration in the function registry
     * 
     * @return
     */
    @Override
    public String getLocalName() {
        return "jsoup";
    }

    /**
     * A string describing the signature of this node function, e.g.
     * "fn:content(uris : Nodes) : Nodes". The
     * syntax for representing the signature can be chosen by the implementer. This method is for
     * informational
     * purposes only.
     * 
     * @return
     */
    @Override
    public String getSignature() {
        return "fn:jsoup(jsoup: String [, nodes: XMLLiteralList]) : LiteralList";
    }

    /**
     * A short human-readable description of what the node function does.
     * 
     * @return
     */
    @Override
    public String getDescription() {
        return "Evaluate an JSoup CSS selector on either the value of the context node or the values of the nodes passed as arguments.";
    }
}
