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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.marmotta.ldpath.model.functions.AbstractTextFilterFunction;

/**
 * LDPath function that cleans up an XML string, i.e. removes all tags and resolves all xml-entities.
 * 
 * @see StringEscapeUtils#unescapeXml(String)
 * @see RemoveXmlTagsFunction
 * @author Jakob Frank <jakob@apache.org>
 *
 */
public class XmlTextFunction<Node> extends AbstractTextFilterFunction<Node> {

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldpath.model.functions.xml.AbstractTextFilterFunction#doFilter(java.lang.String)
     */
    @Override
    protected String doFilter(String in) {
        return StringEscapeUtils.unescapeXml(RemoveXmlTagsFunction.XML_TAG.matcher(in).replaceAll(""));
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldpath.api.functions.NodeFunction#getDescription()
     */
    @Override
    public String getDescription() {
        return "function to remove all XML-Tags and -Entities (&...; stuff) from the content.";
    }


    /* (non-Javadoc)
     * @see org.apache.marmotta.ldpath.api.functions.SelectorFunction#getLocalName()
     */
    @Override
    protected String getLocalName() {
        return "xmlText";
    }

}
