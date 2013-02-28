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
package org.apache.marmotta.platform.core.util.http;

import org.apache.marmotta.platform.core.api.http.HttpClientService;
import org.apache.http.HttpHeaders;
import org.apache.http.client.cache.HeaderConstants;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.CoreProtocolPNames;

/**
 * Utility methods, to be used in conjunction with {@link HttpClientService}.
 * 
 */
public class HttpRequestUtil {

    private HttpRequestUtil() {
        // static util class;
    }

    /**
     * Configure whether redirects for this request should be handled automatically.
     * 
     * @param request the request to modify
     * @param followRedirect <code>true</code> if redirects (HTTP response code 3xx) should be
     *            handled automatically.
     */
    public static void setFollowRedirect(HttpRequestBase request, boolean followRedirect) {
        request.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, followRedirect);
    }

    /**
     * Set the <i>local part</i> of the User-Agent Header String. It will be suffixed with the
     * global part of the User-Agent, which is handled by the corresponding
     * {@link HttpClientService} implementation.
     * 
     * @param request the request to modify
     * @param userAgentString the prefix of the User-Agent string which will be suffixed with a
     *            LMF-global part handled b< the {@link HttpClientService} implementation.
     */
    public static void setUserAgentString(HttpRequestBase request, String userAgentString) {
        request.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgentString);
    }

    /**
     * Set the <code>Cache-Control</code>-header for the provided request.
     * 
     * @param request the request to modify
     * @param cacheControl the cache-control directive
     * 
     * @see HeaderConstants#CACHE_CONTROL_NO_CACHE
     * @see HeaderConstants#CACHE_CONTROL_NO_STORE
     * @see HeaderConstants#CACHE_CONTROL_MAX_AGE
     * @see HeaderConstants#CACHE_CONTROL_MAX_STALE
     * @see HeaderConstants#CACHE_CONTROL_MUST_REVALIDATE
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9">RFC2616#Cache-Control</a>
     * 
     */
    public static void setCacheControl(HttpRequestBase request, String cacheControl) {
        if (cacheControl != null) {
            request.setHeader(HttpHeaders.CACHE_CONTROL, cacheControl);
        } else {
            request.removeHeaders(HttpHeaders.CACHE_CONTROL);
        }
    }

    /**
     * Set the <code>Cache-Control</code>-header for the provided request to <code>no-cache</code>.
     * 
     * @param request the request to modify
     */
    public static void setCacheControl_NoCache(HttpRequestBase request) {
        setCacheControl(request, HeaderConstants.CACHE_CONTROL_NO_CACHE);
    }

}
