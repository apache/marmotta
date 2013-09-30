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
package org.apache.marmotta.platform.core.services.prefix;

import java.io.IOException;
import java.util.HashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.util.EntityUtils;
import org.apache.marmotta.platform.core.api.http.HttpClientService;
import org.apache.marmotta.platform.core.api.prefix.PrefixProvider;
import org.apache.marmotta.platform.core.util.http.HttpRequestUtil;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;

/**
 * Prefix.cc Provider
 * 
 * @author Sergio Fernández
 *
 */
@ApplicationScoped
public class PrefixCC implements PrefixProvider {

    private static final String URI = "http://prefix.cc/";
    private static final String USER_AGENT = "Apache Marmotta Prefix";

    @Inject
    private Logger log;

    @Inject
    private HttpClientService   httpClientService;

    @Override
    public String getNamespace(final String prefix) {
        HttpGet get = new HttpGet(URI + prefix + ".file.json");
        HttpRequestUtil.setUserAgentString(get, USER_AGENT);
        get.setHeader("Accept", "application/json");
        try {
            return httpClientService.execute(get, new ResponseHandler<String>() {

                @Override
                public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    if (200 == response.getStatusLine().getStatusCode()) {
                        HttpEntity entity = response.getEntity();
                        JsonFactory factory = new JsonFactory(); 
                        ObjectMapper mapper = new ObjectMapper(factory); 
                        TypeReference<HashMap<String,String>> typeRef = new TypeReference<HashMap<String,String>>() {}; 
                        HashMap<String,String> result = mapper.readValue(EntityUtils.toString(entity), typeRef); 
                        if (result.containsKey(prefix)) {
                        	return result.get(prefix);
                        } else {
                        	log.error("Error: prefix '" + prefix + "' not found at prefix.cc");
                            return null;
                        }
                    } else {
                        log.error("Error: prefix '" + prefix + "' not found at prefix.cc");
                        return null;
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error retrieving prefix '" + prefix + "' from prefix.cc: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getPrefix(final String namespace) {
        HttpHead head = new HttpHead(URI + "reverse?uri=" + namespace);
        HttpRequestUtil.setFollowRedirect(head, false);
        HttpRequestUtil.setUserAgentString(head, USER_AGENT);
        try {
            return httpClientService.execute(head, new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    if (response.containsHeader("location")) {
                        Header location = response.getFirstHeader("location");
                        return location.getValue().substring(URI.length());
                    } else {
                    	log.error("Error: reverse namespace lookup for '" + namespace + "' not found at prefix.cc");
                        return null;
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error trying to retrieve prefic.cc reverse lookup for namespace '" + namespace + "': " + e.getMessage());
            return null;
        }
    }

}
