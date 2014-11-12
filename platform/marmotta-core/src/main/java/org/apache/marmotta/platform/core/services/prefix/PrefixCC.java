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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.marmotta.platform.core.api.http.HttpClientService;
import org.apache.marmotta.platform.core.api.prefix.PrefixProvider;
import org.apache.marmotta.platform.core.util.http.HttpRequestUtil;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import static com.google.common.net.HttpHeaders.ACCEPT;

/**
 * Prefix.cc Provider
 * 
 * @author Sergio Fernández
 * @author Jakob Frank <jakob@apache.org>
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
        HttpGet get = new HttpGet(URI + prefix + ".file.txt");
        HttpRequestUtil.setUserAgentString(get, USER_AGENT);
        get.setHeader(ACCEPT, "text/plain");
        try {
            return httpClientService.execute(get, new ResponseHandler<String>() {

                @Override
                public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    if (200 == response.getStatusLine().getStatusCode()) {
                        HttpEntity entity = response.getEntity();

                        final LineIterator it = IOUtils.lineIterator(entity.getContent(), Charset.defaultCharset());
                        try {
                            while (it.hasNext()) {
                                final String l = it.next();
                                if (l.startsWith(prefix + "\t")) {
                                    return l.substring(prefix.length()+1);
                                }
                            }
                        } finally {
                            it.close();
                        }
                    }
                    log.error("Error: prefix '" + prefix + "' not found at prefix.cc");
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("Error retrieving prefix '" + prefix + "' from prefix.cc: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getPrefix(final String namespace) {
        try {
            HttpGet get = new HttpGet(URI + "reverse?format=txt&uri=" + URLEncoder.encode(namespace, "utf-8"));
            HttpRequestUtil.setUserAgentString(get, USER_AGENT);
            get.setHeader(ACCEPT, "text/plain");

            return httpClientService.execute(get, new ResponseHandler<String>() {

                @Override
                public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    if (200 == response.getStatusLine().getStatusCode()) {
                        HttpEntity entity = response.getEntity();

                        final LineIterator it = IOUtils.lineIterator(entity.getContent(), Charset.defaultCharset());
                        try {
                            while (it.hasNext()) {
                                final String l = it.next();
                                if (l.endsWith("\t" + namespace)) {
                                    return l.substring(0, l.indexOf("\t"));
                                }
                            }
                        } finally {
                            it.close();
                        }
                    }
                    log.error("Error: reverse namespace lookup for '" + namespace + "' not found at prefix.cc");
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("Error trying to retrieve prefic.cc reverse lookup for namespace '" + namespace + "': " + e.getMessage());
            return null;
        }
    }

}
