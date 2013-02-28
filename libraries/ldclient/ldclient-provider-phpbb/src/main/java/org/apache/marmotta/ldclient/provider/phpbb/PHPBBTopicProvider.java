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
package org.apache.marmotta.ldclient.provider.phpbb;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import com.google.common.collect.ImmutableList;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.provider.html.AbstractHTMLDataProvider;
import org.apache.marmotta.ldclient.provider.html.mapping.CssSelectorMapper;
import org.apache.marmotta.ldclient.provider.html.mapping.CssTextLiteralMapper;
import org.apache.marmotta.ldclient.provider.html.mapping.JSoupMapper;
import org.apache.marmotta.ldclient.provider.phpbb.mapping.PHPBBDateMapper;
import org.apache.marmotta.ldclient.provider.phpbb.mapping.PHPBBForumHrefMapper;
import org.apache.marmotta.ldclient.provider.phpbb.mapping.PHPBBPostIdMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Retrieve and parse a whole PHPBB topic; will try to use paging to retrieve all relevant HTML pages. The
 * resource will be represented as a sioc:Container with the title of the first post in the page and sioc:has_reply
 * to all individual posts.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class PHPBBTopicProvider extends AbstractHTMLDataProvider {


    public static final String PROVIDER_NAME = "PHPBB Forum Topic";


    /**
     * Return a list of URIs that should be added as types for each processed resource.
     *
     * @return
     * @param resource
     */
    @Override
    protected List<String> getTypes(org.openrdf.model.URI resource) {
        return ImmutableList.of(
                Namespaces.NS_SIOC + "Thread",
                Namespaces.NS_SIOC + "Collection",
                Namespaces.NS_FOAF + "Document"
                );
    }

    /**
     * Return a mapping table mapping from RDF properties to XPath Value Mappers. Each entry in the map is evaluated
     * in turn; in case the XPath expression yields a result, the property is added for the processed resource.
     *
     * @return
     * @param requestUrl
     */
    @Override
    protected Map<String, JSoupMapper> getMappings(String resource, String requestUrl) {
        URI uri = null;
        try {
            uri = new URI(requestUrl);
            Map<String,String> params = new HashMap<String, String>();
            for(NameValuePair p : URLEncodedUtils.parse(uri, "UTF-8")) {
                params.put(p.getName(),p.getValue());
            }

            if(params.containsKey("t")) {

                Map<String, JSoupMapper> postMappings = new HashMap<String, JSoupMapper>();
                if(params.containsKey("start")) {
                    // when start is set, we only take the replies; we are in a second or further page of the topic
                    postMappings.put(Namespaces.NS_SIOC + "container_of", new PHPBBPostIdMapper("div#pagecontent table td.gensmall a[name]"));
                } else {
                    // otherwise we also take the initial title, creator and date for the topic
                    postMappings.put(Namespaces.NS_DC + "title", new CssTextLiteralMapper("div#pageheader a.titles"));
                    postMappings.put(Namespaces.NS_DC + "creator", new CssTextLiteralMapper(new CssSelectorMapper.Selector() {
                        @Override
                        public Elements select(Element node) {
                            final Element first = node.select("div#pagecontent table b.postauthor").first();
                            if (first != null)
                                return new Elements(first);
                            return new Elements();
                        }
                    }));
                    postMappings.put(Namespaces.NS_DC + "date", new PHPBBDateMapper("div#pagecontent table td.gensmall div") {
                        @Override
                        public Elements select(Element htmlDoc) {
                            final Elements sel = super.select(htmlDoc);
                            if (sel.size() > 0) {
                                final Element e = sel.get(1);
                                if (e != null)
                                    return new Elements(e);
                            }
                            return new Elements();
                        }
                    });
                    postMappings.put(Namespaces.NS_SIOC + "has_container", new PHPBBForumHrefMapper("p.breadcrumbs a") {
                        @Override
                        public Elements select(Element htmlDoc) {
                            final Element select = super.select(htmlDoc).last();
                            return select != null ? new Elements(select) : new Elements();
                        }
                    });
                    postMappings.put(Namespaces.NS_SIOC + "container_of", new PHPBBPostIdMapper("div#pagecontent table td.gensmall a[name]"));
                }

                return postMappings;
            } else
                throw new RuntimeException("the requested resource does not seem to identify a PHPBB topic (t=... parameter missing)");


        } catch (URISyntaxException e) {
            throw new RuntimeException("the requested resource does not seem to identify a PHPBB topic (URI syntax error)");
        }

    }

    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log messages.
     *
     * @return
     */
    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    /**
     * Return the list of mime types accepted by this data provider.
     *
     * @return
     */
    @Override
    public String[] listMimeTypes() {
        return new String[] { "text/html"};
    }

    /**
     * Build the URL to use to call the webservice in order to retrieve the data for the resource passed as argument.
     * In many cases, this will just return the URI of the resource (e.g. Linked Data), but there might be data providers
     * that use different means for accessing the data for a resource, e.g. SPARQL or a Cache.
     *
     *
     * @param resource
     * @param endpoint endpoint configuration for the data provider (optional)
     * @return
     */
    @Override
    public List<String> buildRequestUrl(String resource, Endpoint endpoint) {
        return Collections.singletonList(resource);
    }


    /**
     * Try to find further URLs in the document that need to be requested to complete the resource data.
     * Used e.g. to parse the result of paging in HTML pages. The default implementation returns an empty list.
     * <p/>
     * This implementation tries to locate the paging area of PHPBB and selects the last link of the paging, which will
     * be the "next" page.
     *
     * @param document
     * @param requestUrl
     * @return
     */
    @Override
    protected List<String> findAdditionalRequestUrls(String resource, Document document, String requestUrl) {
        List<String> result = new LinkedList<String>();


        // return the next page in the result list
        Elements values = document.select("div#pagecontent a[href~=viewtopic\\.php.*start=]");
        for (Element o : values) {
            String baseUrl = o.absUrl("href");
            if (baseUrl.length() > 0) {
                result.add(baseUrl);
            }
        }

        return result;

    }
}
