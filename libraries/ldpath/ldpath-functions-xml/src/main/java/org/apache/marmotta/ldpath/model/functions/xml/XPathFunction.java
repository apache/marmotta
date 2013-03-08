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
package org.apache.marmotta.ldpath.model.functions.xml;


import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Execute XPath functions over the content of the selected value.
 *
 * @param <Node>
 */
public class XPathFunction<Node> extends SelectorFunction<Node> {

    private static final Logger log = LoggerFactory.getLogger(XPathFunction.class);

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
        if (args.length < 1) { throw new IllegalArgumentException("XPath expression is required as first argument."); }
        Set<String> xpaths = new HashSet<String>();
        for (Node xpath : args[0]) {
            try {
                xpaths.add(transformer.transform(rdfBackend,xpath, null));
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("First argument must not contain anything else than String-Literals!");
            }
        }
        Iterator<Node> it;
        if(args.length < 2){
            log.debug("Use context {} to execute xpaths {}",context,xpaths);
            it = Collections.singleton(context).iterator();
        } else {
            log.debug("execute xpaths {} on parsed parameters",xpaths);
            it = org.apache.marmotta.ldpath.util.Collections.iterator(1,args);
        }
        List<Node> result = new ArrayList<Node>();
        while (it.hasNext()) {
            Node n = it.next();
            try {
                for (String r : doFilter(transformer.transform(rdfBackend,n, null), xpaths)) {
                    result.add(rdfBackend.createLiteral(r));
                }
            } catch (IOException e) {
                // This should never happen, since validation is turned off.
            }
        }

        return result;
    }

    private LinkedList<String> doFilter(String in, Set<String> xpaths) throws IOException {
        LinkedList<String> result = new LinkedList<String>();
        try {
            Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(new StringReader(in));
            XMLOutputter out = new XMLOutputter();

            for (String xp : xpaths) {
                XPathExpression<Content> xpath = XPathFactory.instance().compile(xp, Filters.content());
                for (Content node : xpath.evaluate(doc)) {
                    if(node instanceof Element) {
                        result.add(out.outputString((Element) node));
                    } else if(node instanceof Text) {
                        result.add(out.outputString((Text) node));
                    }
                }
            }
            return result;
        } catch (JDOMException xpe) {
            throw new IllegalArgumentException("error while processing xpath expressions: '" + xpaths + "'", xpe);
        }
    }


    /**
     * Return the name of the NodeFunction for registration in the function registry
     *
     * @return
     */
    @Override
    public String getLocalName() {
        return "xpath";
    }


    /**
     * A string describing the signature of this node function, e.g. "fn:content(uris : Nodes) : Nodes". The
     * syntax for representing the signature can be chosen by the implementer. This method is for informational
     * purposes only.
     *
     * @return
     */
    @Override
    public String getSignature() {
        return "fn:xpath(xpath: String [, nodes: XMLLiteralList]) : LiteralList";
    }

    /**
     * A short human-readable description of what the node function does.
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Evaluate an XPath expression on either the value of the context node or the values of the nodes passed as arguments.";
    }
}
