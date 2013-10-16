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
package org.apache.marmotta.ldpath.model.functions.html;

import org.apache.marmotta.ldpath.model.functions.AbstractTextFilterFunction;
import org.jsoup.Jsoup;

/**
 * @author Jakob Frank <jakob@apache.org>
 *
 */
public class HtmlTextFunction<Node> extends AbstractTextFilterFunction<Node> {

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldpath.model.functions.AbstractTextFilterFunction#doFilter(java.lang.String)
     */
    @Override
    protected String doFilter(String in) {
        return Jsoup.parse(in).text();
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldpath.api.functions.SelectorFunction#getLocalName()
     */
    @Override
    protected String getLocalName() {
        return "htmlText";
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldpath.api.functions.NodeFunction#getDescription()
     */
    @Override
    public String getDescription() {
        return "strips all html tags and resolves html-entities like &amp;";
    }
}
