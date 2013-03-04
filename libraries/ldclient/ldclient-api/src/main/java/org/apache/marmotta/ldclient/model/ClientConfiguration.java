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
package org.apache.marmotta.ldclient.model;

import org.apache.http.client.HttpClient;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;

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
     * Maximum number of HTTP requests to run in parallel
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
     * A collection of endpoint definitions to use by this Linked Data Client.
     */
    private Set<Endpoint> endpoints;
    
    /**
     * A HttpClient used for retrieving the resource data.
     */
    private HttpClient httpClient;

    public ClientConfiguration() {
        excludeUris = new HashSet<String>();
        endpoints   = new HashSet<Endpoint>();
        httpClient = null;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getMaxParallelRequests() {
        return maxParallelRequests;
    }

    public void setMaxParallelRequests(int maxParallelRequests) {
        this.maxParallelRequests = maxParallelRequests;
    }


    public long getDefaultExpiry() {
        return defaultExpiry;
    }

    public void setDefaultExpiry(long defaultExpiry) {
        this.defaultExpiry = defaultExpiry;
    }

    public long getMinimumExpiry() {
        return minimumExpiry;
    }

    public void setMinimumExpiry(long minimumExpiry) {
        this.minimumExpiry = minimumExpiry;
    }

    public void addExcludeUri(String uriPrefix) {
        excludeUris.add(uriPrefix);
    }

    public Set<String> getExcludeUris() {
        return excludeUris;
    }

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


    public void addEndpoint(Endpoint endpoint) {
        endpoints.add(endpoint);
    }

    public Set<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

}
