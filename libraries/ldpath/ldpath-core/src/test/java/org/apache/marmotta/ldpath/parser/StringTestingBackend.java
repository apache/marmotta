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

package org.apache.marmotta.ldpath.parser;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.marmotta.ldpath.model.backend.AbstractBackend;

public class StringTestingBackend extends AbstractBackend<String> {

    private static final Pattern LANG_PATTERN = Pattern.compile("@(\\w+)"),
            TYPE_PATTERN = Pattern.compile("\\^\\^([\\w:/.#%-]+)");
    
    @Override
    public boolean isLiteral(String n) {
        return n.startsWith("\"");
    }

    @Override
    public boolean isURI(String n) {
        return n.startsWith("<");
    }

    @Override
    public boolean isBlank(String n) {
        return n.startsWith("_");
    }

    @Override
    public Locale getLiteralLanguage(String n) {
        final Matcher m = LANG_PATTERN.matcher(n);
        if (m.find()) {
            return new Locale(m.group(1));
        }
        return null;
    }

    @Override
    public URI getLiteralType(String n) {
        final Matcher m = TYPE_PATTERN.matcher(n);
        if (m.find()) {
            return URI.create(m.group(1));
        }
        return null;
    }

    @Override
    public String createLiteral(String content) {
        return "\""+content+"\"";
    }

    @Override
    public String createLiteral(String content, Locale language, URI type) {
        StringBuilder sb = new StringBuilder('"');
        sb.append(content).append('"');
        if (language != null) {
            sb.append("@").append(language.getLanguage());
        }
        if (type != null) {
            sb.append("^^").append(type.toString());
        }
        return sb.toString();
    }

    @Override
    public String createURI(String uri) {
        return "<" + uri + ">";
    }

    @Override
    public String stringValue(String node) {
        if (node.startsWith("<")) {
            return node.substring(1, node.length()-1);
        } else if (node.startsWith("\"")) {
            return node.substring(1, node.indexOf('"', 1));
        } else
            return node;
    }

}
