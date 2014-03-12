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
package org.apache.marmotta.ldpath.backend.sesame;

import java.net.URI;
import java.util.Collection;
import java.util.Locale;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Generic implementation of a Sesame backend for LDPath. A Sesame repository is passed as argument to the
 * constructor.
 * <p/>
 * <strong>ATTENTION:</strong> This backend is creating an independent (new) transaction for each invocation of {@link #listObjects(Value, Value)} or {@link #listSubjects(Value, Value)}.
 * Consider using the {@link SesameConnectionBackend} for inproved performance!
 * <p/>
 * Implementatins can either use this class directly or implement their own Sesame-based backend by subclassing
 * and calling the super constructor.
 * <p/>
 */
public class SesameRepositoryBackend extends AbstractSesameBackend {

    private Repository repository;
    private final boolean includeInferred;
    private final Resource[] contexts;

    /**
     * Initialise a new sesame backend. Repository needs to be set using setRepository.
     */
    protected SesameRepositoryBackend() {
        this.includeInferred = false;
        this.contexts = new Resource[0];
    }

    /**
     * Initialise a new sesame backend using the repository passed as argument.
     * Queries will include inferred statements and all contexts.
     *
     * @param repository
     */
    public SesameRepositoryBackend(Repository repository) {
        this(repository, true);
    }

    /**
     * Initialise a new sesame backend using the repository.
     *
     * @param repository
     * @param includeInferred
     * @param contexts
     */
    public SesameRepositoryBackend(Repository repository, boolean includeInferred, Resource... contexts) {
        this.repository = repository;
        this.includeInferred = includeInferred;
        this.contexts = contexts;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }


    /**
     * Create a literal node with the content passed as argument
     *
     * @param content string content to represent inside the literal
     * @return a literal node in using the model used by this backend
     */
    @Override
    public Literal createLiteral(String content) {
        return createLiteralInternal(repository.getValueFactory(), content);
    }


    /**
     * Create a literal node with the content passed as argument
     *
     * @param content string content to represent inside the literal
     * @return a literal node in using the model used by this backend
     */
    @Override
    public Literal createLiteral(String content, Locale language, URI type) {
        return createLiteralInternal(repository.getValueFactory(), content, language, type);
    }


    /**
     * Create a URI mode with the URI passed as argument
     *
     * @param uri URI of the resource to create
     * @return a URI node using the model used by this backend
     */
    @Override
    public org.openrdf.model.URI createURI(String uri) {
        return createURIInternal(repository.getValueFactory(), uri);
    }

    /**
     * List the objects of triples in the triple store underlying this backend that have the subject and
     * property given as argument.
     *
     * @param subject  the subject of the triples to look for
     * @param property the property of the triples to look for
     * @return all objects of triples with matching subject and property
     */
    @Override
    public Collection<Value> listObjects(Value subject, Value property) {
        try {
            RepositoryConnection connection = repository.getConnection();

            try {
                connection.begin();
                return listObjectsInternal(connection, (Resource) subject, (org.openrdf.model.URI) property, includeInferred, contexts);
            } finally {
                connection.commit();
                connection.close();
            }
        } catch (RepositoryException e) {
            throw new RuntimeException("error while querying Sesame repository!",e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(
                    "Subject needs to be a URI or blank node, property a URI node " +
                            "(types: [subject: %s, property: %s])",
                    debugType(subject),debugType(property)),e);
        }

    }

    /**
     * List the subjects of triples in the triple store underlying this backend that have the object and
     * property given as argument.
     *
     * @param object   the object of the triples to look for
     * @param property the property of the triples to look for
     * @return all subjects of triples with matching object and property
     * @throws UnsupportedOperationException in case reverse selection is not supported (e.g. when querying Linked Data)
     */
    @Override
    public Collection<Value> listSubjects(Value property, Value object) {
        try {
            final RepositoryConnection connection = repository.getConnection();

            try {
                connection.begin();
                return listSubjectsInternal(connection, (org.openrdf.model.URI) property, object, includeInferred, contexts);
            } finally {
                connection.commit();
                connection.close();
            }
        } catch (RepositoryException e) {
            throw new RuntimeException("error while querying Sesame repository!",e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(
                    "Property needs to be a URI node (property type: %s)",
                    isURI(property)?"URI":isBlank(property)?"bNode":"literal"),e);
        }

    }
}
