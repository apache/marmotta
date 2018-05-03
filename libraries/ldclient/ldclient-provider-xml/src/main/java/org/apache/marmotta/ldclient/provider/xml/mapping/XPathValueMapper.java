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
package org.apache.marmotta.ldclient.provider.xml.mapping;

import org.apache.marmotta.ldclient.api.provider.ValueMapper;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class that manages an XPath expression for mapping.
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class XPathValueMapper implements ValueMapper {

    private String xpath;

    private XPathExpression<Object> compiled;

    protected XPathValueMapper(String xpath) {
        this(xpath, (Map<String, String>) null);
    }

    protected XPathValueMapper(String xpath, Map<String,String> namespaces) {
        this.xpath = xpath;

        Set<Namespace> xnamespaces = new HashSet<Namespace>();
        if(namespaces != null) {
            for(Map.Entry<String,String> ns : namespaces.entrySet()) {
                xnamespaces.add(Namespace.getNamespace(ns.getKey(), ns.getValue()));
            }
        }
        this.compiled = XPathFactory.instance().compile(xpath, Filters.fpassthrough(),null,xnamespaces);
    }


    protected XPathValueMapper(String xpath, Collection<Namespace> namespaces) {
        this.xpath = xpath;
        this.compiled = XPathFactory.instance().compile(xpath, Filters.fpassthrough(),null,namespaces);
    }

    public String getXpath() {
        return xpath;
    }

    /**
     * Return the precompiled XPath expression represented in this value mapper
     * @return
     */
    public XPathExpression<Object> getCompiled() {
        return compiled;
    }
}
