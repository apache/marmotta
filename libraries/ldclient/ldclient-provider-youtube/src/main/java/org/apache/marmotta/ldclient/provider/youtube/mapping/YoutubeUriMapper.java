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
package org.apache.marmotta.ldclient.provider.youtube.mapping;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathValueMapper;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class YoutubeUriMapper extends XPathValueMapper {

    public YoutubeUriMapper(String xpath) {
        super(xpath);
    }

    public YoutubeUriMapper(String xpath, Map<String, String> namespaces) {
        super(xpath, namespaces);
    }

    /**
     * Take the selected value, process it according to the mapping definition, and create Sesame Values using the
     * factory passed as argument.
     *
     *
     * @param resourceUri
     * @param selectedValue
     * @param factory
     * @return
     */
    @Override
    public List<Value> map(String resourceUri, String selectedValue, ValueFactory factory) {
        if(selectedValue.startsWith("http://gdata.youtube.com/feeds/api/videos") && selectedValue.indexOf('?') >= 0) {
            return Collections.singletonList((Value)factory.createURI(selectedValue.substring(0,selectedValue.indexOf('?'))));
        } else if(selectedValue.startsWith("http://www.youtube.com/v/")) {
            String[] p_components = selectedValue.split("/");
            String video_id = p_components[p_components.length-1];

            return Collections.singletonList((Value)factory.createURI("http://gdata.youtube.com/feeds/api/videos/"+video_id));
        } else if(selectedValue.startsWith("http://www.youtube.com/watch")) {
            try {
                URI uri = new URI(selectedValue);

                List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");

                String video_id = null;
                for(NameValuePair pair : params) {
                    if("v".equals(pair.getName())) {
                        video_id = pair.getValue();
                    }
                }

                if(video_id != null) {
                    return Collections.singletonList((Value)factory.createURI("http://gdata.youtube.com/feeds/api/videos/"+video_id));
                }
            } catch (URISyntaxException e) {
                return Collections.singletonList((Value)factory.createURI(selectedValue));
            }
        }
        return Collections.singletonList((Value)factory.createURI(selectedValue));
    }
}
