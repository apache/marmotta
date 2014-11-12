/*
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
package org.apache.marmotta.platform.ldp.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract baseclass to generate new URIs for a created resource.
 */
public abstract class AbstractResourceUriGenerator {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private final LdpService ldpService;
    private final String container;
    private final RepositoryConnection connection;

    protected AbstractResourceUriGenerator(LdpService ldpService, String container, RepositoryConnection connection) {
        this.ldpService = ldpService;
        this.container = StringUtils.removeEnd(container, "/");
        this.connection = connection;
    }
    
    public String generateResourceUri() throws RepositoryException {
        String newResource = String.format("%s/%s", container, generateNextLocalName());
        log.trace("Checking possible name clash for new resource <{}>", newResource);
        if (ldpService.exists(connection, newResource) || ldpService.isReusedURI(connection, newResource)) {
            do {
                final String candidate = String.format("%s/%s", container, generateNextLocalName());
                log.trace("<{}> already exists, trying <{}>", newResource, candidate);
                newResource = candidate;
            } while (ldpService.exists(connection, newResource) || ldpService.isReusedURI(connection, newResource));
            log.debug("resolved name clash, new resource will be <{}>", newResource);
        } else {
            log.debug("no name clash for <{}>", newResource);
        }

        return newResource;
    }

    protected abstract String generateNextLocalName();

}
