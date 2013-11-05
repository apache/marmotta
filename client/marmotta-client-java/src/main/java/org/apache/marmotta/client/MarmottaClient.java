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
package org.apache.marmotta.client;

import org.apache.marmotta.client.clients.ConfigurationClient;
import org.apache.marmotta.client.clients.ImportClient;
import org.apache.marmotta.client.clients.LDPathClient;
import org.apache.marmotta.client.clients.ResourceClient;
import org.apache.marmotta.client.clients.SPARQLClient;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class MarmottaClient {

    protected ClientConfiguration config;

    public MarmottaClient(ClientConfiguration config) {
        this.config = config;
    }
    
    /**
     * Return a client to access the Marmotta Resource Service. Supports creating and deleting resources as well as
     * updating and retrieving the metadata and content of resources.
     *
     * @return
     */
    public ResourceClient getResourceClient() {
        return new ResourceClient(config);
    }


    /**
     * Return a client that allows to access and modify the server configuration.
     * @return
     */
    public ConfigurationClient getConfigurationClient() {
        return new ConfigurationClient(config);
    }

    /**
     * Return a client that allows executing SPARQL 1.1 queries and updates on the Marmotta Server.
     * @return
     */
    public SPARQLClient getSPARQLClient() {
        return new SPARQLClient(config);
    }

    /**
     * Return a client that allows importing of datasets and ontologies on the Marmotta Server.
     * @return
     */
    public ImportClient getImportClient() {
        return new ImportClient(config);
    }


    /**
     * Return a client that allows accessing the LDPath service for evaluating LDPath queries.
     * @return
     */
    public LDPathClient getLDPathClient() {
        return new LDPathClient(config);
    }
}
