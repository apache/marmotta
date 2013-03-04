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
package org.apache.marmotta.ldclient.endpoint.rdf;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.provider.rdf.SPARQLProvider;

/**
 * Create an endpoint configuration for a SPARQL endpoint using the {@link SPARQLProvider}.
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class SPARQLEndpoint extends Endpoint {

    /**
     * Create an endpoint configuration for a SPARQL endpoint.
     *
     * @param name a name for the endpoint definition    (e.g. DBPedia SPARQL)
     * @param endpointUrl the URL of the SPARQL endpoint (e.g. http://dbpedia.org/sparql)
     * @param resourcePattern the regex pattern for resources that should be handled by this endpoint
     */
    public SPARQLEndpoint(String name, String endpointUrl, String resourcePattern) {
        super(name, SPARQLProvider.PROVIDER_NAME,resourcePattern,buildEndpointUrl(endpointUrl),86400L);
    }

    private static String buildEndpointUrl(String base) {
        return base + "?query={query}&format={contenttype}";
    }
}
