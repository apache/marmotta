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
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.provider.html.AbstractHTMLDataProvider;
import org.apache.marmotta.ldclient.provider.html.mapping.CssTextLiteralMapper;
import org.apache.marmotta.ldclient.provider.html.mapping.JSoupMapper;
import org.apache.marmotta.ldclient.provider.phpbb.mapping.PHPBBForumHrefMapper;
import org.apache.marmotta.ldclient.provider.phpbb.mapping.PHPBBTopicHrefMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A wrapper that allows wrapping a whole PHPBB Forum, linking to all its topics. The resource will
 * be of type
 * sioc:Forum, have a dc:title, and have a sioc:container_of relation to all threads
 * <p/>
 * Author: Sebastian Schaffert
 */
public class PHPBBForumProvider extends AbstractHTMLDataProvider implements DataProvider {


    public static final String PROVIDER_NAME = "PHPBB Forum";

    /**
     * Return a list of URIs that should be added as types for each processed resource.
     * 
     * @return
     * @param resource
     */
    @Override
    protected List<String> getTypes(org.openrdf.model.URI resource) {
        return ImmutableList.of(
                Namespaces.NS_SIOC + "Forum",
                Namespaces.NS_SIOC + "Collection",
                Namespaces.NS_FOAF + "Document"
                );
    }

    /**
     * Return a mapping table mapping from RDF properties to XPath Value Mappers. Each entry in the
     * map is evaluated
     * in turn; in case the XPath expression yields a result, the property is added for the
     * processed resource.
     * 
     * @return
     * @param requestUrl
     */
    @Override
    protected Map<String, JSoupMapper> getMappings(String resource, String requestUrl) {
        URI uri = null;
        try {
            uri = new URI(requestUrl);
            Map<String, String> params = new HashMap<String, String>();
            for (NameValuePair p : URLEncodedUtils.parse(uri, "UTF-8")) {
                params.put(p.getName(), p.getValue());
            }

            if (params.containsKey("f")) {

                Map<String, JSoupMapper> postMappings = new HashMap<String, JSoupMapper>();
                if (params.containsKey("start")) {
                    // when start is set, we only take the replies; we are in a second or further
                    // page of the topic
                    postMappings.put(Namespaces.NS_SIOC + "container_of", new PHPBBTopicHrefMapper("a.topictitle"));
                } else {
                    // otherwise we also take the initial title, creator and date for the topic
                    postMappings.put(Namespaces.NS_DC + "title", new CssTextLiteralMapper("div#pageheader a.titles"));
                    postMappings.put(Namespaces.NS_DC + "description", new CssTextLiteralMapper("div#pageheader span.forumdesc"));
                    postMappings.put(Namespaces.NS_SIOC + "container_of", new PHPBBTopicHrefMapper("a.topictitle"));

                    postMappings.put(Namespaces.NS_SIOC + "parent_of", new PHPBBForumHrefMapper("a.forumlink, a.forumtitle"));
                }

                return postMappings;
            } else
                throw new RuntimeException("the requested resource does not seem to identify a PHPBB Forum (t=... parameter missing)");

        } catch (URISyntaxException e) {
            throw new RuntimeException("the requested resource does not seem to identify a PHPBB Forum (URI syntax error)");
        }

    }

    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log
     * messages.
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
        return new String[] { "text/html" };
    }

    /**
     * Build the URL to use to call the webservice in order to retrieve the data for the resource
     * passed as argument.
     * In many cases, this will just return the URI of the resource (e.g. Linked Data), but there
     * might be data providers
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
     * Try to find further URLs in the document that need to be requested to complete the resource
     * data.
     * Used e.g. to parse the result of paging in HTML pages. The default implementation returns an
     * empty list.
     * <p/>
     * This implementation tries to locate the paging area of PHPBB and selects the last link of the
     * paging, which will be the "next" page.
     * 
     * @param document
     * @param requestUrl
     * @return
     */
    @Override
    protected List<String> findAdditionalRequestUrls(String resource, Document document, String requestUrl) {
        List<String> result = new LinkedList<String>();

        // return the next page in the result list
        String foo = "a[href*='start='][href*='viewforum.php']";

        List<Element> values = document.select(foo);
        for (Element e : values) {
            String baseUrl = e.absUrl("href");
            if (baseUrl.length() > 0) {
                result.add(baseUrl);
            }
        }

        return result;

    }

}
