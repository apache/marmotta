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
package org.apache.marmotta.ldclient.provider.html;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.provider.html.mapping.JSoupMapper;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic implementation of an HTML data provider capable of mapping XPath expressions from HTML documents to
 * RDF metadata properties. The HTML data provider will first clean up the potentially messy HTML using HTMLCleaner
 * and then evaluate XPath expressions on it in the same way as the AbstractXMLDataProvider
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class AbstractHTMLDataProvider extends AbstractHttpProvider implements DataProvider {

    /**
     * Return a list of URIs that should be added as types for each processed resource.
     * 
     * @return
     * @param resource
     */
    protected abstract List<String> getTypes(URI resource);

    /**
     * Try to find further URLs in the document that need to be requested to complete the resource
     * data.
     * Used e.g. to parse the result of paging in HTML pages. The default implementation returns an
     * empty list.
     * 
     *
     * @param resource
     * @param document
     * @param requestUrl
     * @return
     */
    protected List<String> findAdditionalRequestUrls(String resource, Document document, String requestUrl) {
        return Collections.emptyList();
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
        String charset = null;
        Pattern pattern = Pattern.compile("charset=([a-zA-Z0-9-_]+)");
        Matcher matcher = pattern.matcher(contentType);
        if(matcher.find()) {
            charset = matcher.group(1);
        }

        try {
            Document htmlDoc = Jsoup.parse(in,charset,requestUrl);

            ValueFactory vf = ValueFactoryImpl.getInstance();
            URI subject = vf.createURI(resource);

            for (Map.Entry<String, JSoupMapper> mapping : getMappings(resource, requestUrl).entrySet()) {
                URI predicate = vf.createURI(mapping.getKey());

                final Elements values = mapping.getValue().select(htmlDoc);
                for(Element value : values) {
                    List<Value> objects = mapping.getValue().map(resource, value, vf);
                    for(Value object : objects) {
                        Statement stmt = vf.createStatement(subject, predicate, object);
                        triples.add(stmt);
                    }
                }
            }

            org.openrdf.model.URI ptype = vf.createURI(Namespaces.NS_RDF + "type");

            for(String typeUri : getTypes(subject)) {
                Resource type_resource = vf.createURI(typeUri);
                triples.add(vf.createStatement(subject, ptype, type_resource));
            }


            return findAdditionalRequestUrls(resource, htmlDoc, requestUrl);

        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response",e);
        }

    }

    protected abstract Map<String, JSoupMapper> getMappings(String resource, String requestUrl);
}
