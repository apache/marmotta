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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.event.base.RepositoryConnectionInterceptorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ConnectionInterceptor that filters out all write operations that happen to
 * <li>the <strong>managed context</strong>, or</li>
 * <li>use a <strong>managed property</strong></li>
 */
public class ServerManagedPropertiesInterceptor extends RepositoryConnectionInterceptorAdapter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final IRI managedContext;
    private final Resource subject;
    private final Set<? extends Value> managedProperties;
    private final Set<IRI> deniedProperties;

    public ServerManagedPropertiesInterceptor(IRI managedContext, Resource subject) {
        this(managedContext, subject, LdpService.SERVER_MANAGED_PROPERTIES);
    }

    public ServerManagedPropertiesInterceptor(IRI managedContext, Resource subject, Set<? extends Value> managedProperties) {
        this.managedContext = managedContext;
        this.subject = subject;
        this.managedProperties = managedProperties;
        deniedProperties = new HashSet<>();
    }

    @Override
    public boolean add(RepositoryConnection conn, Resource subject, IRI predicate, Value object, Resource... contexts) {
        return isManaged(conn, subject, predicate, object, "ADD");
    }

    @Override
    public boolean remove(RepositoryConnection conn, Resource subject, IRI predicate, Value object, Resource... contexts) {
        return isManaged(conn, subject, predicate, object, "DEL");
    }

    private boolean isManaged(RepositoryConnection conn, Resource subject, IRI predicate, Value object, String operation) {
        try {
            if (conn.hasStatement(subject, predicate, object, true, managedContext)) {
                // Ignore/Strip any triple that is already present in the mgmt-context (i.e. "unchanged" props).
                if (log.isTraceEnabled()) {
                    log.trace("[{}] filtering out statement that is already present in the managed context: {}", operation, SimpleValueFactory.getInstance().createStatement(subject, predicate, object));
                }
                return true;
            } else if (this.subject.equals(subject) && managedProperties.contains(predicate)) {
                // We do NOT allow changing server-managed properties.
                if (log.isTraceEnabled()) {
                    log.trace("[{}] filtering out statement with managed propterty {}: {}", operation, predicate, SimpleValueFactory.getInstance().createStatement(subject, predicate, object));
                }
                deniedProperties.add(predicate);
                return true;
            }
        } catch (RepositoryException e) {
            log.error("Error while filtering server managed properties: {}", e.getMessage());
        }
        return false;
    }

    public Set<IRI> getDeniedProperties() {
        return Collections.unmodifiableSet(deniedProperties);
    }

}
