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

import org.jsoup.nodes.Element;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.util.Collections;
import java.util.List;

public class CssUriAttrMapper extends CssSelectorMapper {

    protected final String attr;

    public CssUriAttrMapper(String cssSelector, String attr) {
        super(cssSelector);
        this.attr = attr;
    }

    public CssUriAttrMapper(Selector selector, String attr) {
        super(selector);
        this.attr = attr;
    }

    protected String rewriteUrl(String url) {
        return url;
    }

    @Override
    public List<Value> map(String resourceUri, Element elem, ValueFactory factory) {
        final String uri = rewriteUrl(elem.absUrl(attr));
        try {
            return Collections.singletonList((Value) factory.createURI(uri));
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }


}
