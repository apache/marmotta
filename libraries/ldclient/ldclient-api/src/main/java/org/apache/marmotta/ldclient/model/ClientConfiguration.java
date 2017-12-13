/*
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
package org.apache.marmotta.ldclient.model;

import org.apache.http.client.HttpClient;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.provider.DataProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration options for the Linked Data Client.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ClientConfiguration {

    /**
     * Socket timeout in milliseconds; maximum time a socket may be idle; default 60 seconds
     */
    private int socketTimeout = 60000;

    /**
     * HTTP connection timeout in milliseconds; default 10 seconds, because we don't want slow servers to slow down
     * batch retrievals
     */
    private int connectionTimeout = 10000;

    /**
     * Maximum number of HTTP requests to run in parallel. Default: 10.
     */
    private int maxParallelRequests = 10;


    /**
     * Default expiry time in seconds if not given by the server.
     */
    private long defaultExpiry = 86400;

    /**
     * Minimum expiry time in seconds in case the server returns a lower expiry time
     */
    private long minimumExpiry = 30;

    /**
     * The URI prefixes to exclude from retrieval; this is typically used for resources that are
     * considered "local", i.e. managed by the local triple store
     */
    private Set<String> excludeUris;

    /**
     * A collection of endpoint definitions to use by this Linked Data Client. Can be used to define custom
     * endpoint access strategies or in case the ServiceLoader is not working (e.g. in OSGi environments).
     */
    private Set<Endpoint> endpoints;

    /**
     * A collection of provider definitions to use by this Linked Data Client. Can be used in case the
     * ServiceLoader is not working (e.g. in OSGi environments)
     */
    private Set<DataProvider> providers;


    /**
     * A HttpClient used for retrieving the resource data.
     */
    private HttpClient httpClient;

    public ClientConfiguration() {
        excludeUris = new HashSet<>();
        endpoints   = new HashSet<>();
        providers   = new HashSet<>();
        httpClient = null;
    }

    /**
     * HTTP connection timeout in milliseconds; default 10 seconds, because we don't want slow servers to slow down
     * batch retrievals
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * HTTP connection timeout in milliseconds; default 10 seconds, because we don't want slow servers to slow down
     * batch retrievals
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Socket timeout in milliseconds; maximum time a socket may be idle; default 60 seconds
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Socket timeout in milliseconds; maximum time a socket may be idle; default 60 seconds
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * Maximum number of HTTP requests to run in parallel. Default: 10.
     */
    public int getMaxParallelRequests() {
        return maxParallelRequests;
    }

    /**
     * Maximum number of HTTP requests to run in parallel. Default: 10.
     */
    public void setMaxParallelRequests(int maxParallelRequests) {
        this.maxParallelRequests = maxParallelRequests;
    }


    /**
     * Default expiry time in seconds if not given by the server.
     */
    public long getDefaultExpiry() {
        return defaultExpiry;
    }

    /**
     * Default expiry time in seconds if not given by the server.
     */
    public void setDefaultExpiry(long defaultExpiry) {
        this.defaultExpiry = defaultExpiry;
    }

    /**
     * Minimum expiry time in seconds in case the server returns a lower expiry time
     */
    public long getMinimumExpiry() {
        return minimumExpiry;
    }

    /**
     * Minimum expiry time in seconds in case the server returns a lower expiry time
     */
    public void setMinimumExpiry(long minimumExpiry) {
        this.minimumExpiry = minimumExpiry;
    }

    /**
     * A collection of endpoint definitions to use by this Linked Data Client. Can be used to define custom
     * endpoint access strategies or in case the ServiceLoader is not working (e.g. in OSGi environments).
     */
    public void addExcludeUri(String uriPrefix) {
        excludeUris.add(uriPrefix);
    }

    /**
     * A collection of endpoint definitions to use by this Linked Data Client. Can be used to define custom
     * endpoint access strategies or in case the ServiceLoader is not working (e.g. in OSGi environments).
     */
    public Set<String> getExcludeUris() {
        return excludeUris;
    }

    /**
     * A collection of endpoint definitions to use by this Linked Data Client. Can be used to define custom
     * endpoint access strategies or in case the ServiceLoader is not working (e.g. in OSGi environments).
     */
    public void setExcludeUris(Set<String> excludeUris) {
        this.excludeUris = excludeUris;
    }

    /**
     * Return true in case the URI passed as argument is considered to be excluded.
     *
     * @param uri
     * @return
     */
    public boolean isExcludedUri(String uri) {
        for(String prefix : excludeUris) {
            if(uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }


    /**
     * A collection of endpoint definitions to use by this Linked Data Client. Can be used to define custom
     * endpoint access strategies or in case the ServiceLoader is not working (e.g. in OSGi environments).
     */
    public void addEndpoint(Endpoint endpoint) {
        endpoints.add(endpoint);
    }

    /**
     * A collection of endpoint definitions to use by this Linked Data Client. Can be used to define custom
     * endpoint access strategies or in case the ServiceLoader is not working (e.g. in OSGi environments).
     */
    public Set<Endpoint> getEndpoints() {
        return endpoints;
    }

    /**
     * A collection of endpoint definitions to use by this Linked Data Client. Can be used to define custom
     * endpoint access strategies or in case the ServiceLoader is not working (e.g. in OSGi environments).
     */
    public void setEndpoints(Set<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }


    /**
     * A collection of provider definitions to use by this Linked Data Client. Can be used in case the
     * ServiceLoader is not working (e.g. in OSGi environments)
     */
    public void addProvider(DataProvider provider) {
        providers.add(provider);
    }

    /**
     * A collection of provider definitions to use by this Linked Data Client. Can be used in case the
     * ServiceLoader is not working (e.g. in OSGi environments)
     */
    public Set<DataProvider> getProviders() {
        return providers;
    }

    /**
     * A collection of provider definitions to use by this Linked Data Client. Can be used in case the
     * ServiceLoader is not working (e.g. in OSGi environments)
     */
    public void setProviders(Set<DataProvider> providers) {
        this.providers = providers;
    }

    public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

}
