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
package org.apache.marmotta.ldclient.provider.rdf;

import com.google.common.base.Preconditions;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;

import java.util.Collections;
import java.util.List;

/**
 * Linked Data implementation of a data provider. Allows retrieval of resources using Linked Data standards.
 * 
 * @author Sebastian Schaffert
 */
public class LinkedDataProvider extends AbstractRDFProvider {

    public static final String PROVIDER_NAME = "Linked Data";

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
     * Build the URL to use to call the webservice in order to retrieve the data for the resource passed as argument.
     * In many cases, this will just return the URI of the resource (e.g. Linked Data), but there might be data providers
     * that use different means for accessing the data for a resource, e.g. SPARQL or a Cache.
     *
     * @param resourceUri
     * @return
     */
    @Override
    public List<String> buildRequestUrl(String resourceUri, Endpoint endpoint) {
        Preconditions.checkNotNull(resourceUri);
        return Collections.singletonList(resourceUri);
    }
    
}
