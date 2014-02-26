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
package org.apache.marmotta.ldpath.backend.sesame;

import java.net.URI;
import java.util.Collection;
import java.util.Locale;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class SesameConnectionBackend extends AbstractSesameBackend {

    private final RepositoryConnection connection;
    private final ValueFactory valueFactory;
    private final Resource[] contexts;
    private final boolean includeInferred;

    /**
     * Create a new {@link SesameConnectionBackend}. This backend is context-agnostig (ignores all context information). 
     * @param connection the {@link RepositoryConnection} to use.
     * @see ContextAwareSesameConnectionBackend#ContextAwareSesameConnectionBackend(RepositoryConnection, Resource...)
     */
    public SesameConnectionBackend(RepositoryConnection connection) {
        this(connection, true);
    }

    protected SesameConnectionBackend(RepositoryConnection connection, boolean includeInferred, Resource... contexts) {
        this.connection = connection;
        this.contexts = contexts;
        this.valueFactory = connection.getValueFactory();
        this.includeInferred = includeInferred;
    }

    @Override
    public Literal createLiteral(String content) {
        return createLiteralInternal(valueFactory, content);
    }

    @Override
    public Literal createLiteral(String content, Locale language, URI type) {
        return createLiteralInternal(valueFactory, content, language, type);
    }

    @Override
    public org.openrdf.model.URI createURI(String uri) {
        return createURIInternal(valueFactory, uri);
    }

    @Override
    public Collection<Value> listObjects(Value subject, Value property) {
        try {
            return listObjectsInternal(connection, (Resource) subject, (org.openrdf.model.URI) property, includeInferred, contexts);
        } catch (RepositoryException e) {
            throw new RuntimeException(
                    "error while querying Sesame repository!", e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(
                    "Subject needs to be a URI or blank node, property a URI node "
                            + "(types: [subject: %s, property: %s])",
                    debugType(subject), debugType(property)), e);
        }
    }

    @Override
    public Collection<Value> listSubjects(Value property, Value object) {
        try {
            return listSubjectsInternal(connection, (org.openrdf.model.URI) property, object, includeInferred, contexts);
        } catch (RepositoryException e) {
            throw new RuntimeException("error while querying Sesame repository!",e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(
                    "Property needs to be a URI node (property type: %s)",
                    isURI(property)?"URI":isBlank(property)?"bNode":"literal"),e);
        }
    }

    /**
     * Create a new {@link SesameConnectionBackend}. This backend is context-agnostig (ignores all context information). 
     * @param connection the {@link RepositoryConnection} to use.
     * @see ContextAwareSesameConnectionBackend#withConnection(RepositoryConnection, Resource...)
     */
    public static SesameConnectionBackend withConnection(RepositoryConnection connection) {
        return new SesameConnectionBackend(connection);
    }

    public static SesameConnectionBackend withConnection(RepositoryConnection connection, boolean includeInferred) {
        return new SesameConnectionBackend(connection, includeInferred);
    }

}
