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
package org.apache.marmotta.ldclient.services.provider;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class BlockingProvider implements DataProvider {


    public static final String PROVIDER_NAME = "NONE";

    private static Logger log = LoggerFactory.getLogger(BlockingProvider.class);

    private static final Model empty_repository = new TreeModel();

    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log messages.
     *
     * @return
     */
    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    /**
     * Return the list of mime types accepted by this data provider.
     *
     * @return
     */
    @Override
    public String[] listMimeTypes() {
        return new String[0];
    }

    /**
     * Retrieve the data for a resource using the given http client and endpoint definition. The service is
     * supposed to manage the connection handling itself. See AbstractHttpProvider
     * for a generic implementation of this method.
     *
     * @param resource the resource to be retrieved
     * @param endpoint the endpoint definition
     * @return a completely specified client response, including expiry information and the set of triples
     */
    @Override
    public ClientResponse retrieveResource(String resource, LDClientService client, Endpoint endpoint) throws DataRetrievalException {
        log.info("blocked retrieval of resource {}", resource);

        long defaultExpires = client.getClientConfiguration().getDefaultExpiry();
        Date expiresDate = new Date(System.currentTimeMillis() + defaultExpires * 1000);

        ClientResponse result = new ClientResponse(200, empty_repository);
        result.setExpires(expiresDate);
        return result;
    }
}
