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
package org.apache.marmotta.ldpath.model.functions.xml;

import java.util.regex.Pattern;

import org.apache.marmotta.ldpath.model.functions.AbstractTextFilterFunction;

public class RemoveXmlTagsFunction<Node> extends AbstractTextFilterFunction<Node> {

    static final Pattern XML_TAG = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^>])*>", Pattern.MULTILINE);

    @Override
    protected String doFilter(String in) {
        return XML_TAG.matcher(in).replaceAll("");
    }

    /**
     * Return the name of the NodeFunction for registration in the function registry
     *
     * @return
     */
    @Override
    public String getLocalName() {
        return "removeTags";

    }

    /**
     * A short human-readable description of what the node function does.
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Function to remove all XML or HTML tags from the content. Can be used in-path, using the current context nodes as argument.";
    }
}
