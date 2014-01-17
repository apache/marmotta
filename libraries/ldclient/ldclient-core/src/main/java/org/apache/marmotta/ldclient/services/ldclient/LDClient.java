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
package org.apache.marmotta.ldclient.services.ldclient;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public final class LDClient implements LDClientService {

    private static Logger log = LoggerFactory.getLogger(LDClient.class);

    /**
     * A Java service loader loading all implementations of data providers registered on the classpath.
     */
    private static ServiceLoader<DataProvider> defaultProviders = ServiceLoader.load(DataProvider.class);

    /**
     * A Java service loader loading all auto-registered endpoint configurations on the classpath.
     */
    private static ServiceLoader<Endpoint> defaultEndpoints = ServiceLoader.load(Endpoint.class);

    private HttpClient client;

    private IdleConnectionMonitorThread idleConnectionMonitorThread;

    // limit maximum parallel retrievals of resources
    private Semaphore retrievalSemaphore;

    private ClientConfiguration config;

    private List<DataProvider> providers;
    private List<Endpoint> endpoints;

    public LDClient() {
        this(new ClientConfiguration());
    }

    public LDClient(ClientConfiguration config) {
        log.info("Initialising Linked Data Client Service ...");

        this.config = config;

        endpoints = new ArrayList<>();
        for(Endpoint endpoint : defaultEndpoints) {
            endpoints.add(endpoint);
        }
        endpoints.addAll(config.getEndpoints());

        Collections.sort(endpoints);
        if(log.isInfoEnabled()) {
            for(Endpoint endpoint : endpoints) {
                log.info("- LDClient Endpoint: {}", endpoint.getName());
            }
        }

        providers = new ArrayList<>();
        for(DataProvider provider : defaultProviders) {
            providers.add(provider);
        }
        providers.addAll(config.getProviders());
        if(log.isInfoEnabled()) {
            for(DataProvider provider : providers) {
                log.info("- LDClient Provider: {}", provider.getName());
            }
        }


        retrievalSemaphore = new Semaphore(config.getMaxParallelRequests());

        if (config.getHttpClient() != null) {
            log.debug("Using HttpClient provided in the configuration");
            this.client = config.getHttpClient();
        } else {
            log.debug("Creating default HttpClient based on the configuration");

            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.USER_AGENT, "Apache Marmotta LDClient");

            httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, config.getSocketTimeout());
            httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, config.getConnectionTimeout());

            httpParams.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS,true);
            httpParams.setIntParameter(ClientPNames.MAX_REDIRECTS,3);

            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

            try {
                SSLContext sslcontext = SSLContext.getInstance("TLS");
                sslcontext.init(null, null, null);
                SSLSocketFactory sf = new SSLSocketFactory(sslcontext, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

                schemeRegistry.register(new Scheme("https", 443, sf));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }

            PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
            cm.setMaxTotal(20);
            cm.setDefaultMaxPerRoute(10);

            DefaultHttpClient client = new DefaultHttpClient(cm,httpParams);
            client.setRedirectStrategy(new LMFRedirectStrategy());
            client.setHttpRequestRetryHandler(new LMFHttpRequestRetryHandler());
            idleConnectionMonitorThread = new IdleConnectionMonitorThread(client.getConnectionManager());
            idleConnectionMonitorThread.start();

            this.client = client;
        }
    }

    @Override
    public boolean ping(String resource) {
        //crappy implementation only for http
        if (resource.startsWith("http://") || resource.startsWith("https://")) {
            try {
                return (200 == client.execute(new HttpHead(resource)).getStatusLine().getStatusCode());
            } catch (Exception e) {
                log.error(e.getMessage());
                return false;
            }
        } else {
            throw new UnsupportedOperationException("protocol not supportted");
        }
        
    }


    @Override
    public void shutdown() {
        if(config.getHttpClient() == null) {
            // we manage our own connection pool
            if (idleConnectionMonitorThread != null)
                idleConnectionMonitorThread.shutdown();
            client.getConnectionManager().shutdown();
        }
    }



    /**
     * Retrieve all triples for this resource from the Linked Data Cloud. Retrieval will be carried out according
     * to the endpoint definition that matches this resource. In case no endpoint definition is found, the method
     * will try an "default" Linked Data retrieval if the configuration option "ldcache.fallback" is set to true
     *
     *
     *
     * @param resource the URI resource for which to retrieve the triples
     * @return a Sesame in-memory repository containing the triples for this resource
     */
    @Override
    public ClientResponse retrieveResource(String resource) throws DataRetrievalException {
        try {
            retrievalSemaphore.acquire();
            if(!config.isExcludedUri(resource)) {

                Endpoint endpoint = getEndpoint(resource);

                if(endpoint != null) {
                    DataProvider provider = getDataProvider(endpoint);
                    if(provider != null) {
                        return provider.retrieveResource(resource, this, endpoint);
                    } else {
                        log.error("no service provider for type {}",endpoint.getType());
                    }
                } else {
                    // TODO: the fallback should at least be a Linked Data handler, so maybe we should merge the ldclient-provider-rdf?
                    // TODO: determine service provider from connection handshaking / MIME type
                    throw new UnsupportedOperationException("not implemented: determine service provider from connection handshaking / MIME type");
                }
            } else {
                log.error("cannot retrieve a local resource; linked data caching only allowed for remote resources");
            }
        } catch (InterruptedException e) {
            log.warn("retrieval of resource was interruped: {}",resource);
        } finally {
            retrievalSemaphore.release();
        }


        return null;
    }

    /**
     * Get access to the Apache HTTP Client managed by the connection handler to execute
     * a request.
     *
     * @return
     */
    @Override
    public HttpClient getClient() {
        return client;
    }

    /**
     * Get the client configuration used by the connection handler
     *
     * @return
     */
    @Override
    public ClientConfiguration getClientConfiguration() {
        return config;
    }


    /**
     * Retrieve the endpoint matching the KiWiUriResource passed as argument. The endpoint is determined by
     * matching the endpoint's URI prefix with the resource URI. If no matching endpoint exists, returns null.
     * The LinkedDataClientService can then decide (based on configuration) whether to try with a standard
     * LinkedDataRequest or ignore the request.
     *
     * @param resource the KiWiUriResource to check.
     */
    @Override
    public Endpoint getEndpoint(String resource) {
        for(Endpoint endpoint : endpoints) {
            if (endpoint.handles(resource)) return endpoint;
        }

        return null;
    }

    /**
     * Test whether an endpoint definition for the given url pattern already exists.
     *
     * @param urlPattern
     * @return
     */
    @Override
    public boolean hasEndpoint(String urlPattern) {
        for(Endpoint endpoint : endpoints) {
            if(endpoint.getUriPattern() != null && endpoint.getUriPattern().equals(urlPattern)) return true;
        }
        return false;
    }

    /**
     * Return a collection of all available data providers (i.e. registered through the service loader).
     * @return
     */
    @Override
    public Set<DataProvider> getDataProviders() {
        Set<DataProvider> result = new HashSet<DataProvider>();
        for(DataProvider p : providers) {
            result.add(p);
        }
        return result;
    }


    private DataProvider getDataProvider(Endpoint endpoint) {
        for(DataProvider provider : providers) {
            if(endpoint.getType().equalsIgnoreCase(provider.getName())) return provider;
        }
        return null;
    }

    private static class LMFRedirectStrategy extends DefaultRedirectStrategy {
        @Override
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
            if (response == null) throw new IllegalArgumentException("HTTP response may not be null");

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

    private static class LMFHttpRequestRetryHandler implements HttpRequestRetryHandler  {
        /**
         * Determines if a method should be retried after an IOException
         * occurs during execution.
         *
         * @param exception      the exception that occurred
         * @param executionCount the number of times this method has been
         *                       unsuccessfully executed
         * @param context        the context for the request execution
         * @return <code>true</code> if the method should be retried, <code>false</code>
         *         otherwise
         */
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            return false;
        }
    }

    private static class IdleConnectionMonitorThread extends Thread {

        private final ClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(ClientConnectionManager connMgr) {
            super("LD HTTP Client Idle Connection Manager");
            this.connMgr = connMgr;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // Close expired connections
                        connMgr.closeExpiredConnections();
                        // Optionally, close connections
                        // that have been idle longer than 30 sec
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                // terminate
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }

    }

}
