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
package org.apache.marmotta.platform.ldp.api;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *  LDP Service
 *
 *  @author Sergio Fernández
 */
public interface LdpService {

    boolean exists(String resource) throws RepositoryException;

    boolean exists(URI resource) throws RepositoryException;

    boolean addResource(InputStream stream, MediaType type, String container, String resource) throws RepositoryException, IOException, RDFParseException;

    boolean addResource(InputStream stream, MediaType type, URI container, URI resource) throws RepositoryException, IOException, RDFParseException;

    List<Statement> getStatements(String resource) throws RepositoryException;

    List<Statement> getStatements(URI resource) throws RepositoryException;

    void exportResource(OutputStream output, String resource, RDFFormat format) throws RepositoryException, RDFHandlerException;

    void exportResource(OutputStream output, URI resouce, RDFFormat format) throws RepositoryException, RDFHandlerException;

    EntityTag generateETag(String uri) throws RepositoryException;

    EntityTag generateETag(URI uri) throws RepositoryException;

    boolean deleteResource(URI resource) throws RepositoryException;

    boolean deleteResource(String resource) throws RepositoryException;

}
