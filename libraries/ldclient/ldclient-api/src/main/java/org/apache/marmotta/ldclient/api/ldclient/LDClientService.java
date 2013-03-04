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
package org.apache.marmotta.ldclient.api.ldclient;

import org.apache.http.client.HttpClient;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;

import java.util.Set;

/**
 * A service offering Linked Data client functionality for retrieving Linked Data resources from the cloud.
 * Implements retrieval of triples from the cloud depending on the Endpoint definitions available for a resource.
 *
 * <p/>
 * User: Sebastian Schaffert
 */
public interface LDClientService {

    /**
     * Perform a ping (HTTP HEAD) to the resource
     * 
     * @param resource
     * @return
     */
    public boolean ping(String resource);

    /**
     * Retrieve all triples for this resource from the Linked Data Cloud. Retrieval will be carried out according
     * to the endpoint definition that matches this resource. In case no endpoint definition is found, the method
     * will try an "default" Linked Data retrieval if the configuration option "ldcache.fallback" is set to true
     *
     *
     *
     * @param resource  the URI resource for which to retrieve the triples
     * @return a Sesame in-memory repository containing the triples for this resource
     */
    public ClientResponse retrieveResource(String resource) throws DataRetrievalException;

    /**
     * Get access to the Apache HTTP Client managed by the connection handler to execute
     * a request.
     *
     * @return
     */
    public HttpClient getClient();


    /**
     * Get the client configuration used by the connection handler
     * @return
     */
    public ClientConfiguration getClientConfiguration();

    /**
     * Retrieve the endpoint matching the KiWiUriResource passed as argument. The endpoint is determined by
     * matching the endpoint's URI prefix with the resource URI. If no matching endpoint exists, returns null.
     * The LinkedDataClientService can then decide (based on configuration) whether to try with a standard
     * LinkedDataRequest or ignore the request.
     *
     * @param resource the KiWiUriResource to check.
     */
    Endpoint getEndpoint(String resource);

    /**
     * Test whether an endpoint definition for the given url pattern already exists.
     *
     * @param urlPattern
     * @return
     */
    boolean hasEndpoint(String urlPattern);

    void shutdown();

    /**
     * Return a collection of all available data providers (i.e. registered through the service loader).
     * @return
     */
    Set<DataProvider> getDataProviders();
}
