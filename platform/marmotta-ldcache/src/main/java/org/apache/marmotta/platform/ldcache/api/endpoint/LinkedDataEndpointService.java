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
package org.apache.marmotta.platform.ldcache.api.endpoint;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.openrdf.model.URI;

import java.util.List;

/**
 * A service for managing Linked Data endpoints.
 * <p/>
 * User: sschaffe
 */
public interface LinkedDataEndpointService {

    /**
     * Add a new endpoint to the system. The endpoint will be persisted in the database.
     *
     * @param endpoint
     */
    public void addEndpoint(Endpoint endpoint);

    /**`
     * Update the endpoint passed as argument in the database.
     *
     * @param endpoint
     */
    public void updateEndpoint(Endpoint endpoint);

    /**
     * List all endpoints registered in the system.
     *
     * @return a list of endpoints in the order they were added to the database.
     */
    public List<Endpoint> listEndpoints();

    /**
     * Remove the endpoint given as argument. The endpoint will be deleted in the database.
     *
     * @param endpoint
     */
    public void removeEndpoint(Endpoint endpoint);

    /**
     * Return the endpoint with the given ID.
     *
     * @param id ID of the endpoint to return.
     * @return
     */
    public Endpoint getEndpoint(String id);



    /**
     * Retrieve the endpoint matching the KiWiUriResource passed as argument. The endpoint is determined by
     * matching the endpoint's URI prefix with the resource URI. If no matching endpoint exists, returns null.
     * The LinkedDataClientService can then decide (based on configuration) whether to try with a standard
     * LinkedDataRequest or ignore the request.
     *
     * @param resource the KiWiUriResource to check.
     */
    public Endpoint getEndpoint(URI resource);

    /**
     * Test whether an endpoint definition for the given url pattern already exists.
     *
     * @param urlPattern
     * @return
     */
    public boolean hasEndpoint(String urlPattern);
}
