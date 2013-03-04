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
package org.apache.marmotta.platform.core.model.filter;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.util.CDIContext;

import org.apache.marmotta.commons.sesame.filter.resource.ResourceFilter;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

/**
 * Accept only resources that are considered "local", i.e. either have the same URI prefix as the system,
 * start with file:, start with urn:, or are blank nodes.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class MarmottaLocalFilter implements ResourceFilter {

    private ConfigurationService configurationService;

    public MarmottaLocalFilter() {
        configurationService = CDIContext.getInstance(ConfigurationService.class);
    }


    private static MarmottaLocalFilter instance = null;

    public static MarmottaLocalFilter getInstance() {
        if(instance == null) {
            instance = new MarmottaLocalFilter();
        }
        return instance;
    }


    /**
     * Return false in case the filter does not accept the resource passed as argument, true otherwise.
     *
     *
     * @param resource
     * @return
     */
    @Override
    public boolean accept(Resource resource) {
        if(resource instanceof BNode) {
            return true;
        }

        URI uri = (URI)resource;

        if(uri.stringValue().startsWith("file:") || uri.stringValue().startsWith("urn:")) {
            return true;
        }

        return uri.stringValue().startsWith(configurationService.getBaseUri());


    }

}
