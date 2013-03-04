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
package org.apache.marmotta.ldclient.provider.youtube;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import com.google.common.collect.ImmutableList;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.provider.xml.AbstractXMLDataProvider;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathLiteralMapper;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathURIMapper;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathValueMapper;
import org.apache.marmotta.ldclient.provider.youtube.mapping.YoutubeUriMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Support YouTube user channels as data source; they are mapped to collections in the Media Ontology
 * <p/>
 * Author: Sebastian Schaffert
 */
public class YoutubeChannelProvider extends AbstractXMLDataProvider implements DataProvider {

    private static final String NS_MEDIA = "http://www.w3.org/ns/ma-ont#";

    private static Map<String,String> youtubeNamespaces = new HashMap<String, String>();
    static {
        youtubeNamespaces.put("atom","http://www.w3.org/2005/Atom" );
        youtubeNamespaces.put("media","http://search.yahoo.com/mrss/");
        youtubeNamespaces.put("yt","http://gdata.youtube.com/schemas/2007");
        youtubeNamespaces.put("gd","http://schemas.google.com/g/2005");
        youtubeNamespaces.put("georss","http://www.georss.org/georss");
        youtubeNamespaces.put("gml","http://www.opengis.net/gml");
    }


    private static Map<String,XPathValueMapper> mediaOntMappings = new HashMap<String, XPathValueMapper>();
    static {
        mediaOntMappings.put("http://www.w3.org/ns/ma-ont#collectionName",new XPathLiteralMapper("/atom:feed/atom:title", youtubeNamespaces));
        mediaOntMappings.put("http://www.w3.org/ns/ma-ont#hasMember",     new YoutubeUriMapper("/atom:feed/atom:entry/atom:id", youtubeNamespaces));
        mediaOntMappings.put("http://www.w3.org/ns/ma-ont#hasCreator",    new XPathURIMapper("/atom:feed/atom:author/@uri", youtubeNamespaces));               // URI
        mediaOntMappings.put("http://www.w3.org/ns/ma-ont#hasPublisher",  new XPathURIMapper("/atom:feed/atom:author/@uri", youtubeNamespaces));               // URI
    }



    private static Logger log = LoggerFactory.getLogger(YoutubeChannelProvider.class);




    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log messages.
     *
     * @return
     */
    @Override
    public String getName() {
        return "YouTube Channel";
    }

    /**
     * Return the list of mime types accepted by this data provider.
     *
     * @return
     */
    @Override
    public String[] listMimeTypes() {
        return new String[] {
                "application/atom+xml"
        };
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
        if(resource.startsWith("http://www.youtube.com/user/")) {
            // YouTube playlist URI, request the GData URI instead
            try {
                URI uri = new URI(resource);
                String[] p_components = uri.getPath().split("/");
                String user_id = p_components[p_components.length-1];

                return Collections.singletonList("http://gdata.youtube.com/feeds/api/users/" + user_id + "/uploads");

            } catch (URISyntaxException e) {
                throw new RuntimeException("URI '"+resource+"'could not be parsed, it is not a valid URI");
            }
        } else if(resource.startsWith("http://gdata.youtube.com/feeds/api/users/")) {
            if(!resource.endsWith("/uploads"))
                return Collections.singletonList(resource+"/uploads");
            else
                return Collections.singletonList(resource);
        } else
            throw new RuntimeException("URI '"+resource+"' not supported by this data provider");
    }



    /**
     * Return a mapping table mapping from RDF properties to XPath Value Mappers. Each entry in the map is evaluated
     * in turn; in case the XPath expression yields a result, the property is added for the processed resource.
     *
     * @return
     * @param requestUrl
     */
    @Override
    protected Map<String, XPathValueMapper> getXPathMappings(String requestUrl) {
        return mediaOntMappings;
    }

    /**
     * Return a list of URIs that should be added as types for each processed resource.
     *
     * @return
     * @param resource
     */
    @Override
    protected List<String> getTypes(org.openrdf.model.URI resource) {
        return ImmutableList.of(NS_MEDIA + "Collection", Namespaces.NS_LMF_TYPES + "YoutubePlaylist");
    }


    /**
     * Provide namespace mappings for the XPath expressions from namespace prefix to namespace URI. May be overridden
     * by subclasses as appropriate, the default implementation returns an empty map.
     *
     * @return
     */
    @Override
    protected Map<String, String> getNamespaceMappings() {
        return youtubeNamespaces;
    }
}
