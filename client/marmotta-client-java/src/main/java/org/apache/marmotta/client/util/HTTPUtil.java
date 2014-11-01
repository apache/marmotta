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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.marmotta.client.ClientConfiguration;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

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

        final HttpClientBuilder httpClientBuilder = HttpClients.custom();

        httpClientBuilder.setUserAgent("Marmotta Client Library/" + MetaUtil.getVersion());
        httpClientBuilder.setRedirectStrategy(new MarmottaRedirectStrategy());
        httpClientBuilder.setRetryHandler(new MarmottaHttpRequestRetryHandler());

        final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setSocketTimeout(config.getSoTimeout());
        requestConfigBuilder.setConnectTimeout(config.getConnectionTimeout());
        requestConfigBuilder.setRedirectsEnabled(true);
        requestConfigBuilder.setMaxRedirects(3);
        httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());

        if (config.getConectionManager() != null) {
            httpClientBuilder.setConnectionManager(config.getConectionManager());
        } else {
            final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    //.register("https", )
                    .build();

            final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
            cm.setMaxTotal(100);
            httpClientBuilder.setConnectionManager(cm);
        }

        return httpClientBuilder.build();
    }

    public static HttpPost createPost(String path, ClientConfiguration config) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(config.getMarmottaUri());
        uriBuilder.setPath(uriBuilder.getPath() + path);

        if (StringUtils.isNotBlank(config.getMarmottaContext())) {
            uriBuilder.addParameter(CONTEXT, config.getMarmottaContext());
        }

        final HttpPost post = new HttpPost(uriBuilder.build());

        if (StringUtils.isNotBlank(config.getMarmottaUser()) && StringUtils.isNotBlank(config.getMarmottaUser())) {
            final String credentials = String.format("%s:%s", config.getMarmottaUser(), config.getMarmottaPassword());
            try {
                final String encoded = DatatypeConverter.printBase64Binary(credentials.getBytes("UTF-8"));
                post.setHeader("Authorization", String.format("Basic %s", encoded));
            } catch (UnsupportedEncodingException e) {
                System.err.println("Error encoding credentials: " + e.getMessage());
            }
        }

        return post;
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
