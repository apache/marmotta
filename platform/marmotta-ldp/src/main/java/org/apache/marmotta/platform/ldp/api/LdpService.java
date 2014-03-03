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

import org.apache.marmotta.platform.ldp.exceptions.InvalidModificationException;
import org.apache.marmotta.platform.ldp.patch.InvalidPatchDocumentException;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 *  LDP Service
 *
 *  @author Sergio Fernández
 *  @author Jakob Frank
 */
public interface LdpService {

    boolean exists(RepositoryConnection connection, String resource) throws RepositoryException;

    boolean exists(RepositoryConnection connection, URI resource) throws RepositoryException;

    boolean exists(RepositoryConnection connection, URI resource, URI type) throws RepositoryException;

    /**
     * Add a LDP resource
     *
     * @param connection repository connection
     * @param container container where add the resource
     * @param resource resource to add
     * @param type mimetype of the posted resource
     * @param stream stream from where read the resource representation
     * @return resource location
     * @throws RepositoryException
     * @throws IOException
     * @throws RDFParseException
     */
    String addResource(RepositoryConnection connection, String container, String resource, MediaType type, InputStream stream) throws RepositoryException, IOException, RDFParseException;

    /**
     * Add a LDP resource
     *
     * @param connection repository connection
     * @param container container where add the resource
     * @param resource resource to add
     * @param type mimetype of the posted resource
     * @param stream stream from where read the resource representation
     * @return resource location
     * @throws RepositoryException
     * @throws IOException
     * @throws RDFParseException
     */
    String addResource(RepositoryConnection connection, URI container, URI resource, MediaType type, InputStream stream) throws RepositoryException, IOException, RDFParseException;

    List<Statement> getLdpTypes(RepositoryConnection connection, String resource) throws RepositoryException;

    List<Statement> getLdpTypes(RepositoryConnection conn1, URI resource) throws RepositoryException;

    void exportResource(RepositoryConnection connection, String resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException;

    void exportResource(RepositoryConnection connection, URI resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException;

    void exportResource(RepositoryConnection connection, String resource, OutputStream out) throws RepositoryException, IOException;

    void exportResource(RepositoryConnection connection, URI resource, OutputStream out) throws RepositoryException, IOException;

    EntityTag generateETag(RepositoryConnection connection, String uri) throws RepositoryException;

    EntityTag generateETag(RepositoryConnection connection, URI uri) throws RepositoryException;

    boolean deleteResource(RepositoryConnection connection, URI resource) throws RepositoryException;

    void patchResource(RepositoryConnection connection, URI uri, InputStream patchData, boolean strict) throws RepositoryException, ParseException, InvalidModificationException, InvalidPatchDocumentException;

    boolean deleteResource(RepositoryConnection connection, String resource) throws RepositoryException;

    Date getLastModified(RepositoryConnection connection, String resource) throws RepositoryException;

    Date getLastModified(RepositoryConnection connection, URI uri) throws RepositoryException;

    void patchResource(RepositoryConnection connection, String resource, InputStream patchData, boolean strict) throws RepositoryException, ParseException, InvalidModificationException, InvalidPatchDocumentException;
}
