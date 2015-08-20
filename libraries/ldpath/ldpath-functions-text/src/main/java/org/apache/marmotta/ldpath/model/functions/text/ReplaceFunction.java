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
package org.apache.marmotta.ldpath.model.functions.text;


import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;

/**
 * Apply a {@link String#replaceAll(String, String)} to the passed Nodes.
 * 
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 * 
 * @see String#replaceAll(String, String)
 */
public class ReplaceFunction<Node> extends SelectorFunction<Node> {

    private final StringTransformer<Node> transformer = new StringTransformer<Node>();

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context, Collection<Node>... args) throws IllegalArgumentException {
        if (args.length != 3 || args[1].size() != 1 || args[2].size() != 1) {
            throw new IllegalArgumentException("wrong usage: " + getSignature());
        }

        Collection<Node> nodes = args[0];
        String regex = transformer.transform(backend, args[1].iterator().next(), null);
        String replace = transformer.transform(backend, args[2].iterator().next(), null);

        try {
            final Pattern pattern = Pattern.compile(regex);

            Set<Node> result = new HashSet<Node>();
            for (Node node : nodes) {
                final String string = backend.stringValue(node);

                final String replaced = pattern.matcher(string).replaceAll(replace);

                if (backend.isURI(node)) {
                    result.add(backend.createURI(replaced));
                } else if (backend.isLiteral(node)) {
                    final Locale lang = backend.getLiteralLanguage(node);
                    final URI type = backend.getLiteralType(node);
                    result.add(backend.createLiteral(replaced, lang, type));
                }
            }

            return result;

        } catch (PatternSyntaxException pex) {
            throw new IllegalArgumentException("could not parse regex pattern: '" + regex + "'", pex);
        } catch (IndexOutOfBoundsException iobex) {
            throw new IllegalArgumentException("invalid replacement string: '" + replace + "'");
        }
    }

    @Override
    public String getSignature() {
        return "fn:replace(nodes: NodeList, regex: String, replace: String) :: NodeList";
    }

    @Override
    public String getDescription() {
        return "applies a String.replaceAll on the nodes (URI or LiteralContent resp.).";
    }

    @Override
    public String getLocalName() {
        return "replace";
    }

}
