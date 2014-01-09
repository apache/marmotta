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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

/**
 * A data provider that allows to wrap the different Youtube Videos pages, linking with the actual
 * entity
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class YoutubeVideoPagesProvider implements DataProvider {

    private static final String YOUTUBE_V = "http://www.youtube.com/v/";
    private static final String YOUTUBE_WATCH = "http://www.youtube.com/watch";
    private static final String YOUTUBE_GDATA = "http://gdata.youtube.com/feeds/api/videos/";
    private static final String FOAF_PRIMARY_TOPIC = "http://xmlns.com/foaf/0.1/primaryTopic";

    private static Logger log = LoggerFactory.getLogger(YoutubeVideoPagesProvider.class);



    @Override
    public String getName() {
        return "YouTube Page";
    }

    @Override
    public String[] listMimeTypes() {
        return new String[] {
                "text/html"
        };
    }

    @Override
    public ClientResponse retrieveResource(String resource, LDClientService client, Endpoint endpoint) throws DataRetrievalException {

        Model model = new TreeModel();

        String uri = resource;
        URI objUri;
        try {
            objUri = new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI '" + uri + "'could not be parsed, it is not a valid URI");
        }

        String video_id = null;
        if (uri.startsWith(YOUTUBE_V)) { // YouTube short watch video URL
            String[] p_components = objUri.getPath().split("/");
            video_id = p_components[p_components.length - 1];
        } else if (resource.startsWith(YOUTUBE_WATCH)) { // YouTube watch video URL
            List<NameValuePair> params = URLEncodedUtils.parse(objUri, "UTF-8");
            for (NameValuePair pair : params) {
                if ("v".equals(pair.getName())) {
                    video_id = pair.getValue();
                    break;
                }
            }
        } else if (uri.startsWith(YOUTUBE_GDATA)) { // GData URI
            video_id = StringUtils.removeStart(uri, YOUTUBE_GDATA);
        }
        if (StringUtils.isBlank(video_id)) {
            String msg = "Not valid video id found in '" + uri + "'";
            log.error(msg);
            throw new DataRetrievalException(msg);
        } else {
            model.add(new URIImpl(uri), new URIImpl(FOAF_PRIMARY_TOPIC), new URIImpl(YoutubeVideoProvider.YOUTUBE_BASE_URI + video_id), (Resource)null);
            // FIXME: add inverse triple, but maybe at the YoutubeVideoProvider

            ClientResponse clientResponse = new ClientResponse(200, model);
            clientResponse.setExpires(DateUtils.addYears(new Date(), 10));
            return clientResponse;
        }

    }

}
