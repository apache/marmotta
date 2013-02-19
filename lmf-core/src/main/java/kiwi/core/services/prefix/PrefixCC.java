/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kiwi.core.services.prefix;

import kiwi.core.api.http.HttpClientService;
import kiwi.core.api.prefix.PrefixProvider;
import kiwi.core.util.http.HttpRequestUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;

/**
 * Prefix.cc Provider
 * 
 * @author Sergio Fernández
 *
 */
@ApplicationScoped
public class PrefixCC implements PrefixProvider {

    private static final String URI = "http://prefix.cc/";
    private static final String USER_AGENT = "LMF Prefix";

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
                    try {
                        if (200 == response.getStatusLine().getStatusCode()) {
                            HttpEntity entity = response.getEntity();
                            JSONObject json = new JSONObject(EntityUtils.toString(entity));
                            return json.getString(prefix);
                        } else {
                            log.error("Error: prefix '" + prefix + "' not found at prefix.cc");
                            return null;
                        }
                    } catch (JSONException e) {
                        throw new IOException(e);
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error retrieving prefix '" + prefix + "' from prefix.cc: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getPrefix(String namespace) {
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
                    } else
                        return null;
                }
            });
        } catch (Exception e) {
            log.error("Error trying to retrieve prefic.cc reverse lookup for namespace '" + namespace + "': " + e.getMessage());
            return null;
        }
    }

}
