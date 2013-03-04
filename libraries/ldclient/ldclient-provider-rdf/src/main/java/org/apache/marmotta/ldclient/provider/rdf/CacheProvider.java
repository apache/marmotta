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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

/**
 * A data provider supporting retrieval from Linked Data caches, i.e. services that offer to retrieve a copy of a
 * resource like the Sindice Live Cache or Apache Stanbol Entity Hub. The concrete details of the service definition
 * are passed in the Endpoint configuration of the buildRequestUrl method.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class CacheProvider extends AbstractRDFProvider {

    public static final String PROVIDER_NAME = "LD Cache";

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
     *
     *
     * @param resourceUri
     * @param endpoint endpoint configuration for the data provider (optional)
     * @return
     */
    @Override
    public List<String> buildRequestUrl(String resourceUri, Endpoint endpoint) {
        Preconditions.checkNotNull(resourceUri);
        Preconditions.checkNotNull(endpoint);
        try {
            return Collections.singletonList(endpoint.getEndpointUrl().replace("{uri}", URLEncoder.encode(resourceUri, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encoding UTF-8 not supported; the Java environment is severely broken");
        }
    }
}
