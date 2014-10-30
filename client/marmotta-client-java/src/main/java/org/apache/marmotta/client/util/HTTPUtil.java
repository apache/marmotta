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
package org.apache.marmotta.client.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.marmotta.client.ClientConfiguration;

import java.io.IOException;

/**
 * HTTP Utilities
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class HTTPUtil {

    private static final String CONTEXT = "context";

    public static HttpClient createClient(ClientConfiguration config) {
        return createClient(config, config.getMarmottaContext());
    }

    public static HttpClient createClient(ClientConfiguration config, String context) {

        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(CoreProtocolPNames.USER_AGENT, "Marmotta Client Library/" + MetaUtil.getVersion());

        httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, config.getSoTimeout());
        httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, config.getConnectionTimeout());

        httpParams.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
        httpParams.setIntParameter(ClientPNames.MAX_REDIRECTS, 3);

        if (StringUtils.isNotBlank(context)) {
            httpParams.setParameter(CONTEXT, context);
        }

        DefaultHttpClient client;
        if (config.getConectionManager() != null) {
            client = new DefaultHttpClient(config.getConectionManager(), httpParams);
        } else {
            client = new DefaultHttpClient(httpParams);
        }
        client.setRedirectStrategy(new MarmottaRedirectStrategy());
        client.setHttpRequestRetryHandler(new MarmottaHttpRequestRetryHandler());
        return client;
    }

    public static HttpPost createPost(String path, ClientConfiguration config) {
        String serviceUrl = config.getMarmottaUri() + path;

        //FIXME: switch to a more elegant way, such as Jersey's UriBuilder
        if (StringUtils.isNotBlank(config.getMarmottaContext())) {
            serviceUrl += "?" + CONTEXT + "=" + config.getMarmottaContext();
        }

        return new HttpPost(serviceUrl);
    }


    private static class MarmottaRedirectStrategy extends DefaultRedirectStrategy {
        @Override
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
            if (response == null) {
                throw new IllegalArgumentException("HTTP response may not be null");
            }

            int statusCode = response.getStatusLine().getStatusCode();
            String method = request.getRequestLine().getMethod();
            Header locationHeader = response.getFirstHeader("location");
            switch (statusCode) {
                case HttpStatus.SC_MOVED_TEMPORARILY:
                    return (method.equalsIgnoreCase(HttpGet.METHOD_NAME)
                            || method.equalsIgnoreCase(HttpHead.METHOD_NAME)) && locationHeader != null;
                case HttpStatus.SC_MOVED_PERMANENTLY:
                case HttpStatus.SC_TEMPORARY_REDIRECT:
                    return method.equalsIgnoreCase(HttpGet.METHOD_NAME)
                            || method.equalsIgnoreCase(HttpHead.METHOD_NAME);
                case HttpStatus.SC_SEE_OTHER:
                    return true;
                case HttpStatus.SC_MULTIPLE_CHOICES:
                    return true;
                default:
                    return false;
            } //end of switch
        }
    }

    private static class MarmottaHttpRequestRetryHandler implements HttpRequestRetryHandler {
        /**
         * Determines if a method should be retried after an IOException
         * occurs during execution.
         *
         * @param exception      the exception that occurred
         * @param executionCount the number of times this method has been
         *                       unsuccessfully executed
         * @param context        the context for the request execution
         * @return <code>true</code> if the method should be retried, <code>false</code>
         * otherwise
         */
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            return false;
        }
    }


}
