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
package org.apache.marmotta.ldclient.api.provider;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;

/**
 * Abstract interface for defining different implementations of data providers. Standard data
 * providers are Linked Data and SPARQL, but in principle any data source can be wrapped as RDF
 * (e.g. Google Data API)
 * <p/>
 * Data provider should be implemented as CDI service beans and can make use of @PostConstruct
 * methods for registering themselves for specific URIs or carrying out maintenance work.
 * 
 * @author Sebastian Schaffert
 */
public interface DataProvider {

    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log messages.
     * @return
     */
    public String getName();


    /**
     * Return the list of mime types accepted by this data provider.
     * 
     * @return
     */
    public String[] listMimeTypes();

    /**
     * Retrieve the data for a resource using the given http client and endpoint definition. The service is
     * supposed to manage the connection handling itself. See AbstractHttpProvider
     * for a generic implementation of this method.
     *
     *
     *
     * @param resource the resource to be retrieved
     * @param endpoint the endpoint definition
     * @return a completely specified client response, including expiry information and the set of triples
     */
    public ClientResponse retrieveResource(String resource, LDClientService client, Endpoint endpoint) throws DataRetrievalException;

}
