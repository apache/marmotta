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
package org.apache.marmotta.ldclient.provider.html.mapping;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CssLiteralAttrMapper extends CssSelectorMapper {

    protected final String attr;
    protected final String datatype;
    protected final Locale language;

    private CssLiteralAttrMapper(String cssSelector, String attr, Locale lang, String datatype) {
        super(cssSelector);
        this.attr = attr;
        this.language = lang;
        this.datatype = datatype;
    }

    private CssLiteralAttrMapper(Selector selector, String attr, Locale lang, String datatype) {
        super(selector);
        this.attr = attr;
        this.language = lang;
        this.datatype = datatype;
    }

    public CssLiteralAttrMapper(String cssSelector, String attr, Locale lang) {
        this(cssSelector, attr, lang, null);
    }

    public CssLiteralAttrMapper(String cssSelector, String attr, String datatype) {
        this(cssSelector, attr, null, datatype);
    }

    public CssLiteralAttrMapper(String cssSelector, String attr) {
        this(cssSelector, attr, null, null);
    }

    public CssLiteralAttrMapper(Selector selector, String attr, Locale lang) {
        this(selector, attr, lang, null);
    }

    public CssLiteralAttrMapper(Selector selector, String attr, String datatype) {
        this(selector, attr, null, datatype);
    }

    public CssLiteralAttrMapper(Selector selector, String attr) {
        this(selector, attr, null, null);
    }

    protected String cleanValue(String value) {
        return value.trim();
    }

    @Override
    public List<Value> map(String resourceUri, Element elem, ValueFactory factory) {
        final String value = cleanValue(elem.attr(attr));
        if (StringUtils.isBlank(value)) return Collections.emptyList();
        if (language != null)
            return Collections.singletonList((Value) factory.createLiteral(value, language.toString()));
        if (datatype != null)
            return Collections.singletonList((Value) factory.createLiteral(value, factory.createURI(Namespaces.NS_XSD + datatype)));
        else
            return Collections.singletonList((Value) factory.createLiteral(value));
    }


}
