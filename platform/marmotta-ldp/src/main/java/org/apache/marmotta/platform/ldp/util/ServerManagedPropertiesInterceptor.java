/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.ldp.util;

import org.apache.marmotta.platform.ldp.api.LdpService;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.event.base.RepositoryConnectionInterceptorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A ConnectionInterceptor that filters out all write operations that happen to
 * <li>the <strong>managed context</strong>, or</li>
 * <li>use a <strong>managed property</strong></li>
 */
public class ServerManagedPropertiesInterceptor extends RepositoryConnectionInterceptorAdapter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final URI managedContext;
    private final Resource subject;
    private final Set<? extends Value> managedProperties;
    private final Set<URI> deniedProperties;

    public ServerManagedPropertiesInterceptor(URI managedContext, Resource subject) {
        this(managedContext, subject, LdpService.SERVER_MANAGED_PROPERTIES);
    }

    public ServerManagedPropertiesInterceptor(URI managedContext, Resource subject, Set<? extends Value> managedProperties) {
        this.managedContext = managedContext;
        this.subject = subject;
        this.managedProperties = managedProperties;
        deniedProperties = new HashSet<>();
    }

    @Override
    public boolean add(RepositoryConnection conn, Resource subject, URI predicate, Value object, Resource... contexts) {
        return isManaged(conn, subject, predicate, object, "ADD");
    }

    @Override
    public boolean remove(RepositoryConnection conn, Resource subject, URI predicate, Value object, Resource... contexts) {
        return isManaged(conn, subject, predicate, object, "DEL");
    }

    private boolean isManaged(RepositoryConnection conn, Resource subject, URI predicate, Value object, String operation) {
        try {
            if (conn.hasStatement(subject, predicate, object, true, managedContext)) {
                // Ignore/Strip any triple that is already present in the mgmt-context (i.e. "unchanged" props).
                if (log.isTraceEnabled()) {
                    log.trace("[{}] filtering out statement that is already present in the managed context: {}", operation, new StatementImpl(subject, predicate, object));
                }
                return true;
            } else if (this.subject.equals(subject) && managedProperties.contains(predicate)) {
                // We do NOT allow changing server-managed properties.
                if (log.isTraceEnabled()) {
                    log.trace("[{}] filtering out statement with managed propterty {}: {}", operation, predicate, new StatementImpl(subject, predicate, object));
                }
                deniedProperties.add(predicate);
                return true;
            }
        } catch (RepositoryException e) {
            log.error("Error while filtering server managed properties: {}", e.getMessage());
        }
        return false;
    }

    public Set<URI> getDeniedProperties() {
        return Collections.unmodifiableSet(deniedProperties);
    }

}
