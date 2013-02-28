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
import org.apache.marmotta.ldclient.provider.phpbb.mapping.PHPBBDateMapper;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrap a PHPBB Forum Post and try to extract the content from the HTML page using typical patterns.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class PHPBBPostProvider extends AbstractHTMLDataProvider implements DataProvider {

    public static final String PROVIDER_NAME = "PHPBB Forum Post";

    /**
     * Return a list of URIs that should be added as types for each processed resource.
     * 
     * @return
     * @param resource
     */
    @Override
    protected List<String> getTypes(org.openrdf.model.URI resource) {
        return ImmutableList.of(
                Namespaces.NS_SIOC_TYPES + "BoardPost",
                Namespaces.NS_SIOC + "Post",
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

            if (params.containsKey("p")) {
                // mappings for a reply that has directly been addressed using the ?p=... parameter
                // to viewtopic.php, e.g. http://www.carving-ski.de/phpBB/viewtopic.php?p=119208
                Map<String, JSoupMapper> commentMappings = new HashMap<String, JSoupMapper>();
                commentMappings.put(Namespaces.NS_DC + "title",
                        new CssTextLiteralMapper(String.format("div#pagecontent table:has(a[name=p%s]) td.gensmall div", params.get("p"))) {
                    @Override
                    public List<Value> map(String resourceUri, Element elem, ValueFactory factory) {
                        final String val = elem.ownText().replaceFirst("^\\s*:", "").replaceAll("&nbsp;", " ").trim();
                        if (datatype != null)
                            return Collections.singletonList((Value) factory.createLiteral(val,
                                    factory.createURI(Namespaces.NS_XSD + datatype)));
                        else
                            return Collections.singletonList((Value) factory.createLiteral(val));
                    }

                    @Override
                    public Elements select(Element htmlDoc) {
                        final Element first = super.select(htmlDoc).first();
                        return first != null ? new Elements(first) : new Elements();
                    }
                });
                commentMappings.put(Namespaces.NS_DC + "creator",
                        new CssTextLiteralMapper(String.format("div#pagecontent table:has(a[name=p%s]) .postauthor", params.get("p"))));
                commentMappings.put(Namespaces.NS_DC + "description",
                        new CssTextLiteralMapper(String.format("div#pagecontent table:has(a[name=p%s]) div.postbody", params.get("p"))));
                commentMappings.put(Namespaces.NS_DC + "date",
                        new PHPBBDateMapper(String.format("div#pagecontent td.gensmall:has(a[name=p%s]) div", params.get("p"))) {
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

                return commentMappings;
            } else
                throw new RuntimeException("the requested resource does not seem to identify a PHPBB Post (p=... parameter missing)");

        } catch (URISyntaxException e) {
            throw new RuntimeException("the requested resource does not seem to identify a PHPBB Post (URI syntax error)");
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
}
