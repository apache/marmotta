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
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.provider.xml.AbstractXMLDataProvider;
import org.apache.marmotta.ldclient.provider.xml.mapping.CommaSeparatedMapper;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathLiteralMapper;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathURIMapper;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathValueMapper;
import org.apache.marmotta.ldclient.provider.youtube.mapping.YoutubeCategoryMapper;
import org.apache.marmotta.ldclient.provider.youtube.mapping.YoutubeLatitudeMapper;
import org.apache.marmotta.ldclient.provider.youtube.mapping.YoutubeLongitudeMapper;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A data provider that allows to wrap Youtube Videos. Video descriptions are mapped according to
 * the Media Ontology:
 * http://www.w3.org/TR/mediaont-10/
 * 
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class YoutubeVideoProvider extends AbstractXMLDataProvider implements DataProvider {

    public static final String YOUTUBE_BASE_URI = "http://youtu.be/";
    private static final String GDATA_VIDEO_FEED = "http://gdata.youtube.com/feeds/api/videos/";
    private static final String NS_MEDIA = "http://www.w3.org/ns/ma-ont#";
    
    private static Logger log = LoggerFactory.getLogger(YoutubeVideoProvider.class);


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
        mediaOntMappings.put(NS_MEDIA + "title", new XPathLiteralMapper("/atom:entry/atom:title", youtubeNamespaces));
        mediaOntMappings.put(NS_MEDIA + "locator", new XPathLiteralMapper("/atom:entry/media:group/media:content/@url", youtubeNamespaces, "anyURI")); // URI
        mediaOntMappings.put(NS_MEDIA + "hasCreator", new XPathURIMapper("/atom:entry/atom:author/@uri", youtubeNamespaces)); // URI
        mediaOntMappings.put(NS_MEDIA + "hasPublisher", new XPathURIMapper("/atom:entry/atom:author/@uri", youtubeNamespaces)); // URI
        mediaOntMappings.put(NS_MEDIA + "date", new XPathLiteralMapper("/atom:entry/atom:published", youtubeNamespaces, "dateTime"));
        mediaOntMappings.put(NS_MEDIA + "locationLatitude", new YoutubeLatitudeMapper("/atom:entry/georss:where/gml:Point/gml:pos",youtubeNamespaces));
        mediaOntMappings.put(NS_MEDIA + "locationLongitude", new YoutubeLongitudeMapper("/atom:entry/georss:where/gml:Point/gml:pos",youtubeNamespaces));
        mediaOntMappings.put(NS_MEDIA + "description", new XPathLiteralMapper("/atom:entry/media:group/media:description",youtubeNamespaces));
        mediaOntMappings.put(NS_MEDIA + "hasKeyword", new XPathLiteralMapper("/atom:entry/atom:category[@scheme='http://gdata.youtube.com/schemas/2007/keywords.cat']/@term",youtubeNamespaces));
        mediaOntMappings.put(NS_MEDIA + "hasGenre", new YoutubeCategoryMapper("/atom:entry/media:group/media:category",youtubeNamespaces));       // URI, should  be mapped to YouTube schemas (http://gdata.youtube.com/schemas/2007#)
        mediaOntMappings.put(NS_MEDIA + "hasRating", new XPathLiteralMapper("/atom:entry/gd:rating/@average",youtubeNamespaces,"float"));          // Float
        mediaOntMappings.put(NS_MEDIA + "copyright", new XPathURIMapper("/atom:entry/media:group/media:license/@href",youtubeNamespaces));         // URI of license terms
        mediaOntMappings.put(NS_MEDIA + "hasCompression", new XPathLiteralMapper("/atom:entry/media:group/media:content/@type",youtubeNamespaces));
        mediaOntMappings.put(NS_MEDIA + "duration", new XPathLiteralMapper("/atom:entry/media:group/media:content/@duration",youtubeNamespaces,"integer"));
        mediaOntMappings.put(NS_MEDIA + "format", new XPathLiteralMapper("/atom:entry/media:group/media:content/@type",youtubeNamespaces));
        mediaOntMappings.put("http://xmlns.com/foaf/0.1/thumbnail", new CommaSeparatedMapper("/atom:entry/media:group/media:thumbnail/@url",youtubeNamespaces,"anyURI"));
        mediaOntMappings.put("http://rdfs.org/sioc/ns#num_views", new XPathLiteralMapper("/atom:entry/yt:statistics/@viewCount",youtubeNamespaces,"integer"));
    }


    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log messages.
     *
     * @return
     */
    @Override
    public String getName() {
        return "YouTube Video";
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
        String uri = resource;
        if (uri.startsWith(YOUTUBE_BASE_URI)) {
            // YouTube video URI, request the GData URI instead
            String video_id = StringUtils.removeStart(uri, YOUTUBE_BASE_URI);
            if (StringUtils.isNotBlank(video_id))
                return Collections.singletonList(GDATA_VIDEO_FEED + video_id);
        }
        return Collections.singletonList(uri);
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
    protected List<String> getTypes(URI resource) {
        return ImmutableList.of(NS_MEDIA + "MediaResource",NS_MEDIA + "VideoTrack", Namespaces.NS_LMF_TYPES + "YoutubeVideo");
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
