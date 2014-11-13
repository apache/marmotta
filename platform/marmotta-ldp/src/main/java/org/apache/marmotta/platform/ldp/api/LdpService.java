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

import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.ldp.exceptions.IncompatibleResourceTypeException;
import org.apache.marmotta.platform.ldp.exceptions.InvalidInteractionModelException;
import org.apache.marmotta.platform.ldp.exceptions.InvalidModificationException;
import org.apache.marmotta.platform.ldp.patch.InvalidPatchDocumentException;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 *  LDP Service
 *
 *  @author Sergio Fernández
 *  @author Jakob Frank
 */
public interface LdpService {

    public static final Set<URI> SERVER_MANAGED_PROPERTIES = new HashSet<>(Arrays.asList(
            LDP.contains, DCTERMS.CREATED, DCTERMS.MODIFIED
    ));
    public static final List<RDFFormat> SERVER_PREFERED_RDF_FORMATS = Arrays.asList(
            RDFFormat.TURTLE, RDFFormat.JSONLD, RDFFormat.RDFXML, RDFFormat.N3, RDFFormat.NTRIPLES
    );

    public static enum InteractionModel {
        LDPR(LDP.Resource),
        LDPC(LDP.Container);

        private final URI uri;

        InteractionModel(URI uri) {
            this.uri = uri;
        }

        public URI getUri() {
            return uri;
        }

        public String stringValue() {
            return uri.stringValue();
        }

        public static InteractionModel fromURI(String uri) {
            if (LDP.Resource.stringValue().equals(uri)) {
                return LDPR;
            } else if (LDP.Container.stringValue().equals(uri)) {
                return LDPC;
            }
            throw new IllegalArgumentException("Invalid Interaction Model URI: " + uri);
        }

        public static InteractionModel fromURI(URI uri){
            if (uri == null) {
                throw new IllegalArgumentException("Invalid Interaction Model: null");
            } else {
                return fromURI(uri.stringValue());
            }
        }

    }

    /**
     * Initializes the root LDP Container
     *
     * @param connection repository connection
     * @param root root container
     * @throws RepositoryException
     */
    void init(RepositoryConnection connection, URI root) throws RepositoryException;

    String getResourceUri(UriInfo uriInfo);

    UriBuilder getResourceUriBuilder(UriInfo uriInfo);

    /**
     * Check if the specified resource already exists.
     * @param connection the repository connection
     * @param resource the resource to test
     * @return true if it exists
     * @throws RepositoryException
     */
    boolean exists(RepositoryConnection connection, String resource) throws RepositoryException;

    /**
     * Check if the specified resource already exists.
     * @param connection the repository connection
     * @param resource the resource to test
     * @return true if it exists
     * @throws RepositoryException
     */
    boolean exists(RepositoryConnection connection, URI resource) throws RepositoryException;

    /**
     * Check if the specified resource would be a re-used URI.
     * @param connection the repository connection
     * @param resource the resource to test
     * @return true if it had existed
     * @throws RepositoryException
     */
    boolean isReusedURI(RepositoryConnection connection, String resource) throws RepositoryException;

    /**
     * Check if the specified resource would be a re-used URI.
     * @param connection the repository connection
     * @param resource the resource to test
     * @return true if it had existed
     * @throws RepositoryException
     */
    boolean isReusedURI(RepositoryConnection connection, URI resource) throws RepositoryException;

    boolean hasType(RepositoryConnection connection, URI resource, URI type) throws RepositoryException;

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
    String addResource(RepositoryConnection connection, String container, String resource, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException;

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
    String addResource(RepositoryConnection connection, URI container, URI resource, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException;

    /**
     * Add a LDP resource
     *
     * @param connection repository connection
     * @param container container where add the resource
     * @param resource resource to add
     * @param interactionModel the ldp interaction model
     * @param type mimetype of the posted resource
     * @param stream stream from where read the resource representation
     * @return resource location
     * @throws RepositoryException
     * @throws IOException
     * @throws RDFParseException
     */
    String addResource(RepositoryConnection connection, String container, String resource, InteractionModel interactionModel, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException;

    /**
     * Add a LDP resource
     *
     * @param connection repository connection
     * @param container container where add the resource
     * @param resource resource to add
     * @param interactionModel the ldp interaction model
     * @param type mimetype of the posted resource
     * @param stream stream from where read the resource representation
     * @return resource location
     * @throws RepositoryException
     * @throws IOException
     * @throws RDFParseException
     */
    String addResource(RepositoryConnection connection, URI container, URI resource, InteractionModel interactionModel, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException;

    /**
     * Update an existing resource
     *
     * @param connection repository connection
     * @param resource resource to add
     * @param stream stream with the data
     * @param type resource type
     * @return updated resource uri
     * @throws RepositoryException
     * @throws IncompatibleResourceTypeException
     * @throws RDFParseException
     * @throws IOException
     * @throws InvalidModificationException
     */
    String updateResource(RepositoryConnection connection, String resource, InputStream stream, String type) throws RepositoryException, IncompatibleResourceTypeException, RDFParseException, IOException, InvalidModificationException;

    /**
     * Update an existing resource
     *
     * @param connection repository connection
     * @param resource resource to add
     * @param stream stream with the data
     * @param type resource type
     * @return updated resource uri
     * @throws RepositoryException
     * @throws IncompatibleResourceTypeException
     * @throws IOException
     * @throws RDFParseException
     * @throws InvalidModificationException
     */
    String updateResource(RepositoryConnection connection, URI resource, InputStream stream, String type) throws RepositoryException, IncompatibleResourceTypeException, IOException, RDFParseException, InvalidModificationException;

    /**
     * Update an existing resource
     *
     * @param connection repository connection
     * @param resource resource to add
     * @param stream stream with the data
     * @param type resource type
     * @param overwrite overwrite current resource
     * @return updated resource uri
     * @throws RepositoryException
     * @throws IncompatibleResourceTypeException
     * @throws RDFParseException
     * @throws IOException
     * @throws InvalidModificationException
     */
    String updateResource(RepositoryConnection connection, String resource, InputStream stream, String type, boolean overwrite) throws RepositoryException, IncompatibleResourceTypeException, RDFParseException, IOException, InvalidModificationException;

    /**
     * Update an existing resource
     *
     * @param connection repository connection
     * @param resource resource to add
     * @param stream stream with the data
     * @param type resource type
     * @param overwrite overwrite current resource
     * @return updated resource uri
     * @throws RepositoryException
     * @throws IncompatibleResourceTypeException
     * @throws IOException
     * @throws RDFParseException
     * @throws InvalidModificationException
     */
    String updateResource(RepositoryConnection connection, URI resource, InputStream stream, String type, boolean overwrite) throws RepositoryException, IncompatibleResourceTypeException, IOException, RDFParseException, InvalidModificationException;

    List<Statement> getLdpTypes(RepositoryConnection connection, String resource) throws RepositoryException;

    List<Statement> getLdpTypes(RepositoryConnection connection, URI resource) throws RepositoryException;

    void exportResource(RepositoryConnection connection, String resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException;

    void exportResource(RepositoryConnection connection, URI resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException;

    void exportResource(RepositoryConnection outputConn, String resource, OutputStream output, RDFFormat format, Preference preference) throws RDFHandlerException, RepositoryException;

    void exportResource(RepositoryConnection outputConn, URI resource, OutputStream output, RDFFormat format, Preference preference) throws RepositoryException, RDFHandlerException;

    void exportBinaryResource(RepositoryConnection connection, String resource, OutputStream out) throws RepositoryException, IOException;

    void exportBinaryResource(RepositoryConnection connection, URI resource, OutputStream out) throws RepositoryException, IOException;

    EntityTag generateETag(RepositoryConnection connection, String uri) throws RepositoryException;

    EntityTag generateETag(RepositoryConnection connection, URI uri) throws RepositoryException;

    boolean deleteResource(RepositoryConnection connection, URI resource) throws RepositoryException;

    void patchResource(RepositoryConnection connection, URI uri, InputStream patchData, boolean strict) throws RepositoryException, ParseException, InvalidModificationException, InvalidPatchDocumentException;

    boolean deleteResource(RepositoryConnection connection, String resource) throws RepositoryException;

    Date getLastModified(RepositoryConnection connection, String resource) throws RepositoryException;

    Date getLastModified(RepositoryConnection connection, URI uri) throws RepositoryException;

    void patchResource(RepositoryConnection connection, String resource, InputStream patchData, boolean strict) throws RepositoryException, ParseException, InvalidModificationException, InvalidPatchDocumentException;

    String getMimeType(RepositoryConnection connection, String resource) throws RepositoryException;

    String getMimeType(RepositoryConnection connection, URI uri) throws RepositoryException;

    boolean isNonRdfSourceResource(RepositoryConnection connection, String resource) throws RepositoryException;

    boolean isNonRdfSourceResource(RepositoryConnection connection, URI uri) throws RepositoryException;

    URI getRdfSourceForNonRdfSource(RepositoryConnection connection, URI uri) throws RepositoryException;

    URI getRdfSourceForNonRdfSource(RepositoryConnection connection, String resource) throws RepositoryException;

    boolean isRdfSourceResource(RepositoryConnection connection, String resource) throws RepositoryException;

    boolean isRdfSourceResource(RepositoryConnection connection, URI uri) throws RepositoryException;

    URI getNonRdfSourceForRdfSource(RepositoryConnection connection, String resource) throws RepositoryException;

    URI getNonRdfSourceForRdfSource(RepositoryConnection connection, URI uri) throws RepositoryException;

    InteractionModel getInteractionModel(List<Link> linkHeaders) throws InvalidInteractionModelException;

    InteractionModel getInteractionModel(RepositoryConnection connection, String resource) throws RepositoryException;

    InteractionModel getInteractionModel(RepositoryConnection connection, URI uri) throws RepositoryException;

}
