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
package org.apache.marmotta.ldclient.provider.xml;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathValueMapper;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.jdom2.*;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.xpath.XPathExpression;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Abstract implementation of a data provider based on XML documents. Implementing classes need to provide
 * a mapping table mapping from RDF property URIs to XPath Value Mappers that are evaluated on the XML document 
 * (getXPathMappings method), as well as a list of URIs used as types for the created resource.
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class AbstractXMLDataProvider extends AbstractHttpProvider {


    /**
     * Return a mapping table mapping from RDF properties to XPath Value Mappers. Each entry in the map is evaluated
     * in turn; in case the XPath expression yields a result, the property is added for the processed resource.
     *
     * @return
     * @param requestUrl
     */
    protected abstract Map<String,XPathValueMapper> getXPathMappings(String requestUrl);


    /**
     * Return a list of URIs that should be added as types for each processed resource.
     *
     * @return
     * @param resource
     */
    protected abstract List<String> getTypes(URI resource);


    /**
     * Provide namespace mappings for the XPath expressions from namespace prefix to namespace URI. May be overridden
     * by subclasses as appropriate, the default implementation returns an empty map.
     *
     * @return
     */
    protected Map<String,String> getNamespaceMappings() {
        return Collections.emptyMap();
    }


    /**
     * Parse the HTTP response entity returned by the web service call and return its contents as a Sesame RDF
     * repository. The content type returned by the web service is passed as argument to help the implementation
     * decide how to parse the data.
     *
     *
     *
     * @param resource    the subject of the data retrieval
     * @param triples
     *@param in          input stream as returned by the remote webservice
     * @param contentType content type as returned in the HTTP headers of the remote webservice   @return an RDF repository containing an RDF representation of the dataset located at the remote resource.
     * @throws java.io.IOException in case an error occurs while reading the input stream
     */
    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream in, String contentType) throws DataRetrievalException {
        // build a JDOM document
        try {
            SAXBuilder parser = new SAXBuilder(XMLReaders.NONVALIDATING);
            Document doc = parser.build(in);


            Set<Namespace> namespaces = new HashSet<Namespace>();
            for(Map.Entry<String,String> ns : getNamespaceMappings().entrySet()) {
                namespaces.add(Namespace.getNamespace(ns.getKey(), ns.getValue()));
            }


            ValueFactory vf = new ValueFactoryImpl();

            Resource subject = vf.createURI(resource);

            for(Map.Entry<String,XPathValueMapper> mapping : getXPathMappings(requestUrl).entrySet()) {
                XPathExpression<Object> xpath = mapping.getValue().getCompiled();

                org.openrdf.model.URI predicate = triples.getValueFactory().createURI(mapping.getKey());
                for(Object value : xpath.evaluate(doc)) {
                    String str_value;
                    if(value instanceof Element) {
                        str_value = ((Element) value).getValue();
                    } else if(value instanceof Text) {
                        str_value = ((Text) value).getValue();
                    } else if(value instanceof Attribute) {
                        str_value = ((Attribute) value).getValue();
                    } else if(value instanceof CDATA) {
                        str_value = ((CDATA) value).getValue();
                    } else if(value instanceof Comment) {
                        str_value = ((Comment) value).getValue();
                    } else {
                        str_value = value.toString();
                    }
                    List<Value> objects = mapping.getValue().map(resource, str_value,triples.getValueFactory());
                    for(Value object : objects) {
                        Statement stmt = triples.getValueFactory().createStatement(subject,predicate,object);
                        triples.add(stmt);
                    }
                }
            }

            org.openrdf.model.URI ptype = triples.getValueFactory().createURI(Namespaces.NS_RDF + "type");

            for(String typeUri : getTypes(vf.createURI(resource))) {
                Resource type_resource = vf.createURI(typeUri);
                triples.add(vf.createStatement(subject, ptype, type_resource));
            }

            return Collections.emptyList();

        } catch (JDOMException e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format",e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing XML response",e);
        }

    }


}
