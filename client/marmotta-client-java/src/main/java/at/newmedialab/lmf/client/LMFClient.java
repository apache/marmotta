/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.lmf.client;

import at.newmedialab.lmf.client.clients.ClassificationClient;
import at.newmedialab.lmf.client.clients.ConfigurationClient;
import at.newmedialab.lmf.client.clients.CoresClient;
import at.newmedialab.lmf.client.clients.ImportClient;
import at.newmedialab.lmf.client.clients.LDPathClient;
import at.newmedialab.lmf.client.clients.ResourceClient;
import at.newmedialab.lmf.client.clients.SPARQLClient;
import at.newmedialab.lmf.client.clients.SearchClient;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LMFClient {
    
    public static final String VERSION = "0.1.0";

    private ClientConfiguration config;


    public LMFClient(ClientConfiguration config) {
        this.config = config;
    }
    
    /**
     * Return a client to access the LMF Resource Service. Supports creating and deleting resources as well as
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
     * Return a client that allows executing SPARQL 1.1 queries and updates on the LMF Server.
     * @return
     */
    public SPARQLClient getSPARQLClient() {
        return new SPARQLClient(config);
    }

    /**
     * Return a client that allows importing of datasets and ontologies on the LMF Server.
     * @return
     */
    public ImportClient getImportClient() {
        return new ImportClient(config);
    }


    /**
     * Return a client that allows running SOLR searches on the LMF semantic search cores
     */
    public SearchClient getSearchClient() {
        return new SearchClient(config);
    }


    /**
     * Return a client that allows reading and modifying the LMF semantic search cores.
     */
    public CoresClient getSearchCoresClient() {
        return new CoresClient(config);
    }

    /**
     * Return a client that allows accessing the LDPath service for evaluating LDPath queries.
     * @return
     */
    public LDPathClient getLDPathClient() {
        return new LDPathClient(config);
    }


    /**
     * Return a client that allows managing and training classifiers and classifying text.
     * @return
     */
    public ClassificationClient getClassificationClient() {
        return new ClassificationClient(config);
    }
}
