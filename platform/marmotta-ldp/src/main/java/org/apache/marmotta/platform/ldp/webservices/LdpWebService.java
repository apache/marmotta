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
package org.apache.marmotta.platform.ldp.webservices;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.exporter.ExportService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.events.SesameStartupEvent;
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.apache.marmotta.platform.ldp.exceptions.IncompatibleResourceTypeException;
import org.apache.marmotta.platform.ldp.exceptions.InvalidInteractionModelException;
import org.apache.marmotta.platform.ldp.exceptions.InvalidModificationException;
import org.apache.marmotta.platform.ldp.patch.InvalidPatchDocumentException;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.apache.marmotta.platform.ldp.patch.parser.RdfPatchParser;
import org.apache.marmotta.platform.ldp.util.LdpUtils;
import org.apache.marmotta.platform.ldp.util.ResponseBuilderImpl;
import org.jboss.resteasy.spi.NoLogWebApplicationException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Linked Data Platform web services.
 *
 * @see <a href="http://www.w3.org/TR/ldp/">http://www.w3.org/TR/ldp/</a>
 *
 * @author Sergio Fernández
 * @author Jakob Frank
 */
@ApplicationScoped
@Path(LdpWebService.PATH + "{local:.*}")
public class LdpWebService {

    public static final String PATH = "/ldp"; //TODO: at some point this will be root ('/') in marmotta
    public static final String LDP_SERVER_CONSTRAINTS = "http://wiki.apache.org/marmotta/LDPImplementationReport/2014-03-11";

    static final String LINK_REL_DESCRIBEDBY = "describedby";
    static final String LINK_REL_CONSTRAINEDBY = "http://www.w3.org/ns/ldp#constrainedBy";
    static final String LINK_REL_CONTENT = "content";
    static final String LINK_REL_META = "meta";
    static final String LINK_REL_TYPE = "type";
    static final String LINK_PARAM_ANCHOR = "anchor";
    static final String HTTP_HEADER_SLUG = "Slug";
    static final String HTTP_HEADER_ACCEPT_POST = "Accept-Post";
    static final String HTTP_HEADER_ACCEPT_PATCH = "Accept-Patch";
    static final String HTTP_METHOD_PATCH = "PATCH";

    private Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private LdpService ldpService;

    @Inject
    private ExportService exportService;

    @Inject
    private SesameService sesameService;

    private final List<ContentType> producedRdfTypes;

    public LdpWebService() {
        producedRdfTypes = new ArrayList<>();

        for(RDFFormat format : RDFWriterRegistry.getInstance().getKeys()) {
            final String primaryQ;
            if (format == RDFFormat.TURTLE) {
                primaryQ = ";q=1.0";
            } else if (format == RDFFormat.JSONLD) {
                primaryQ = ";q=0.9";
            } else if (format == RDFFormat.RDFXML) {
                primaryQ = ";q=0.8";
            } else {
                primaryQ = ";q=0.5";
            }
            final String secondaryQ = ";q=0.3";
            final List<String> mimeTypes = format.getMIMETypes();
            for (int i = 0; i < mimeTypes.size(); i++) {
                final String mime = mimeTypes.get(i);
                if (i == 0) {
                    // first mimetype is the default
                    producedRdfTypes.add(MarmottaHttpUtils.parseContentType(mime + primaryQ));
                } else {
                    producedRdfTypes.add(MarmottaHttpUtils.parseContentType(mime + secondaryQ));
                }
            }
        }
        Collections.sort(producedRdfTypes);

        log.debug("Available RDF Serializer: {}", producedRdfTypes);
    }

    protected void initialize(@Observes SesameStartupEvent event) {
        log.info("Starting up LDP WebService Endpoint");
        String root = UriBuilder.fromUri(configurationService.getBaseUri()).path(LdpWebService.PATH).build().toASCIIString();
        try {
            final RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                ldpService.init(conn, conn.getValueFactory().createURI(root));
                log.debug("Created LDP root container <{}>", root);
                conn.commit();
            } finally {
                conn.close();
            }
        } catch (RepositoryException e) {
            log.error("Error creating LDP root container <{}>: {}", root, e.getMessage(), e);
        }
    }

    @GET
    public Response GET(@Context final UriInfo uriInfo, @Context Request r,
                        @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.WILDCARD) String type)
            throws RepositoryException {
        final String resource = ldpService.getResourceUri(uriInfo);
        log.debug("GET to LDPR <{}>", resource);
        return buildGetResponse(resource, r, MarmottaHttpUtils.parseAcceptHeader(type)).build();
    }

    @HEAD
    public Response HEAD(@Context final UriInfo uriInfo, @Context Request r,
                         @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.WILDCARD) String type)
            throws RepositoryException {
        final String resource = ldpService.getResourceUri(uriInfo);
        log.debug("HEAD to LDPR <{}>", resource);
        return buildGetResponse(resource, r, MarmottaHttpUtils.parseAcceptHeader(type)).entity(null).build();
    }

    private Response.ResponseBuilder buildGetResponse(final String resource, Request r, List<ContentType> acceptedContentTypes) throws RepositoryException {
        log.trace("LDPR requested media type {}", acceptedContentTypes);
        final RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();

            log.trace("Checking existence of {}", resource);
            if (!ldpService.exists(conn, resource)) {
                log.debug("{} does not exist", resource);
                final Response.ResponseBuilder resp;
                if (ldpService.isReusedURI(conn, resource)) {
                    resp = createResponse(conn, Response.Status.GONE, resource);
                } else {
                    resp = createResponse(conn, Response.Status.NOT_FOUND, resource);
                }
                conn.rollback();
                return resp;
            } else {
                log.trace("{} exists, continuing", resource);
            }

            // Content-Neg
            if (ldpService.isNonRdfSourceResource(conn, resource)) {
                log.trace("<{}> is marked as LDP-NR", resource);
                // LDP-NR
                final ContentType realType = MarmottaHttpUtils.parseContentType(ldpService.getMimeType(conn, resource));
                if (realType == null) {
                    log.debug("<{}> has no format information - try some magic...");
                    final ContentType rdfContentType = MarmottaHttpUtils.bestContentType(producedRdfTypes, acceptedContentTypes);
                    if (MarmottaHttpUtils.bestContentType(MarmottaHttpUtils.parseAcceptHeader("*/*"), acceptedContentTypes) != null) {
                        log.trace("Unknown type of LDP-NR <{}> is compatible with wildcard - sending back LDP-NR without Content-Type", resource);
                        // Client will accept anything, send back LDP-NR
                        final Response.ResponseBuilder resp = buildGetResponseBinaryResource(conn, resource);
                        conn.commit();
                        return resp;
                    } else if (rdfContentType == null) {
                        log.trace("LDP-NR <{}> has no type information, sending HTTP 409 with hint for wildcard 'Accept: */*'", resource);
                        // Client does not look for a RDF Serialisation, send back 409 Conflict.
                        log.debug("No corresponding LDP-RS found for <{}>, sending HTTP 409 with hint for wildcard 'Accept: */*'", resource);
                        final Response.ResponseBuilder resp = build406Response(conn, resource, Collections.<ContentType>emptyList());
                        conn.commit();
                        return resp;
                    } else {
                        log.debug("Client is asking for a RDF-Serialisation of LDP-NS <{}>, sending meta-data", resource);
                        final Response.ResponseBuilder resp = buildGetResponseSourceResource(conn, resource, Rio.getWriterFormatForMIMEType(rdfContentType.getMime(), RDFFormat.TURTLE));
                        conn.commit();
                        return resp;
                    }
                } else if (MarmottaHttpUtils.bestContentType(Collections.singletonList(realType), acceptedContentTypes) == null) {
                    log.trace("Client-accepted types {} do not include <{}>-s available type {} - trying some magic...", acceptedContentTypes, resource, realType);
                    // requested types do not match the real type - maybe an rdf-type is accepted?
                    final ContentType rdfContentType = MarmottaHttpUtils.bestContentType(producedRdfTypes, acceptedContentTypes);
                    if (rdfContentType == null) {
                        log.debug("Can't send <{}> ({}) in any of the accepted formats: {}, sending 406", resource, realType, acceptedContentTypes);
                        final Response.ResponseBuilder resp = build406Response(conn, resource, Collections.singletonList(realType));
                        conn.commit();
                        return resp;
                    } else {
                        log.debug("Client is asking for a RDF-Serialisation of LDP-NS <{}>, sending meta-data", resource);
                        final Response.ResponseBuilder resp = buildGetResponseSourceResource(conn, resource, Rio.getWriterFormatForMIMEType(rdfContentType.getMime(), RDFFormat.TURTLE));
                        conn.commit();
                        return resp;
                    }
                } else {
                    final Response.ResponseBuilder resp = buildGetResponseBinaryResource(conn, resource);
                    conn.commit();
                    return resp;
                }
            } else {
                // Requested Resource is a LDP-RS
                final ContentType bestType = MarmottaHttpUtils.bestContentType(producedRdfTypes, acceptedContentTypes);
                if (bestType == null) {
                    log.trace("Available formats {} do not match any of the requested formats {} for <{}>, sending 406", producedRdfTypes, acceptedContentTypes, resource);
                    final Response.ResponseBuilder resp = build406Response(conn, resource, producedRdfTypes);
                    conn.commit();
                    return resp;
                } else {
                    final Response.ResponseBuilder resp = buildGetResponseSourceResource(conn, resource, Rio.getWriterFormatForMIMEType(bestType.getMime(), RDFFormat.TURTLE));
                    conn.commit();
                    return resp;
                }
            }
        } catch (final Throwable t) {
            conn.rollback();
            throw t;
        } finally {
            conn.close();
        }
    }

    private Response.ResponseBuilder build406Response(RepositoryConnection connection, String resource, List<ContentType> availableContentTypes) throws RepositoryException {
        final Response.ResponseBuilder response = createResponse(connection, Response.Status.NOT_ACCEPTABLE, resource);
        if (availableContentTypes.isEmpty()) {
            response.entity(String.format("%s is not available in the requested format%n", resource));
        } else {
            response.entity(String.format("%s is only available in the following formats: %s%n", resource, availableContentTypes));
        }
        // Sec. 4.2.2.2
        return addOptionsHeader(connection, resource, response);
    }

    private Response.ResponseBuilder buildGetResponseBinaryResource(RepositoryConnection connection, final String resource) throws RepositoryException {
        final String realType = ldpService.getMimeType(connection, resource);
        log.debug("Building response for LDP-NR <{}> with format {}", resource, realType);
        final StreamingOutput entity = new StreamingOutput() {
            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                try {
                    final RepositoryConnection outputConn = sesameService.getConnection();
                    try {
                        outputConn.begin();
                        ldpService.exportBinaryResource(outputConn, resource, out);
                        outputConn.commit();
                    } catch (RepositoryException | IOException e) {
                        outputConn.rollback();
                        throw new WebApplicationException(e, createResponse(Response.status(Response.Status.INTERNAL_SERVER_ERROR)).entity(e).build());
                    } finally {
                        outputConn.close();
                    }
                } catch (RepositoryException e) {
                    throw new WebApplicationException(e, createResponse(Response.status(Response.Status.INTERNAL_SERVER_ERROR)).entity(e).build());
                }
            }
        };
        // Sec. 4.2.2.2
        return addOptionsHeader(connection, resource, createResponse(connection, Response.Status.OK, resource).entity(entity).type(realType));
    }

    private Response.ResponseBuilder buildGetResponseSourceResource(RepositoryConnection conn, final String resource, final RDFFormat format) throws RepositoryException {
        // Deliver all triples from the <subject> context.
        log.debug("Building response for LDP-RS <{}> with RDF format {}", resource, format.getDefaultMIMEType());
        final StreamingOutput entity = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    final RepositoryConnection outputConn = sesameService.getConnection();
                    try {
                        outputConn.begin();
                        ldpService.exportResource(outputConn, resource, output, format);
                        outputConn.commit();
                    } catch (RDFHandlerException e) {
                        outputConn.rollback();
                        throw new NoLogWebApplicationException(e, createResponse(Response.status(Response.Status.INTERNAL_SERVER_ERROR)).entity(e.getMessage()).build());
                    } catch (final Throwable t) {
                        outputConn.rollback();
                        throw t;
                    } finally {
                        outputConn.close();
                    }
                } catch (RepositoryException e) {
                    throw new WebApplicationException(e, createResponse(Response.status(Response.Status.INTERNAL_SERVER_ERROR)).entity(e).build());
                }
            }
        };
        // Sec. 4.2.2.2
        return addOptionsHeader(conn, resource, createResponse(conn, Response.Status.OK, resource).entity(entity).type(format.getDefaultMIMEType()));
    }

    /**
     * LDP Post Request
     *
     * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/ldp.html#ldpr-HTTP_POST">5.4 LDP-R POST</a>
     * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/ldp.html#ldpc-HTTP_POST">6.4 LDP-C POST</a>
     */
    @POST
    public Response POST(@Context UriInfo uriInfo, @HeaderParam(HTTP_HEADER_SLUG) String slug,
                         @HeaderParam(HttpHeaders.LINK) List<Link> linkHeaders,
                         InputStream postBody, @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType type)
            throws RepositoryException {

        final String container = ldpService.getResourceUri(uriInfo);
        log.debug("POST to LDPC <{}>", container);

        final RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();

            if (!ldpService.exists(conn, container)) {
                final Response.ResponseBuilder resp;
                if (ldpService.isReusedURI(conn, container)) {
                    log.debug("<{}> has been deleted, can't POST to it!", container);
                    resp = createResponse(conn, Response.Status.GONE, container);
                } else {
                    log.debug("<{}> does not exists, can't POST to it!", container);
                    resp = createResponse(conn, Response.Status.NOT_FOUND, container);
                }
                conn.rollback();
                return resp.build();
            }

            // Check that the target container supports the LDPC Interaction Model
            final LdpService.InteractionModel containerModel = ldpService.getInteractionModel(conn, container);
            if (containerModel != LdpService.InteractionModel.LDPC) {
                final Response.ResponseBuilder response = createResponse(conn, Response.Status.METHOD_NOT_ALLOWED, container);
                conn.commit();
                return response.entity(String.format("%s only supports %s Interaction Model", container, containerModel)).build();
            }

            // Get the LDP-Interaction Model (Sec. 5.2.3.4 and Sec. 4.2.1.4)
            final LdpService.InteractionModel ldpInteractionModel = ldpService.getInteractionModel(linkHeaders);

            final String localName;
            if (StringUtils.isBlank(slug)) {
                /* Sec. 5.2.3.8) */
                // FIXME: Maybe us a shorter uid?
                localName = UUID.randomUUID().toString();
            } else {
                // Honor client wishes from Slug-header (Sec. 5.2.3.10)
                //    http://www.ietf.org/rfc/rfc5023.txt
                log.trace("Slug-Header is '{}'", slug);
                localName = LdpUtils.urify(slug);
                log.trace("Slug-Header urified: {}", localName);
            }

            String newResource = uriInfo.getRequestUriBuilder().path(localName).build().toString();

            if (ldpService.isNonRdfSourceResource(conn, container)) {
                log.info("POSTing to a NonRdfSource is not allowed ({})", container);
                final Response.ResponseBuilder response = createResponse(conn, Response.Status.METHOD_NOT_ALLOWED, container).entity("POST to NonRdfSource is not allowed\n");
                conn.commit();
                return response.build();
            }

            log.trace("Checking possible name clash for new resource <{}>", newResource);
            if (ldpService.exists(conn, newResource) || ldpService.isReusedURI(conn, newResource)) {
                int i = 0;
                final String base = newResource;
                do {
                    final String candidate = base + "-" + (++i);
                    log.trace("<{}> already exists, trying <{}>", newResource, candidate);
                    newResource = candidate;
                } while (ldpService.exists(conn, newResource) || ldpService.isReusedURI(conn, newResource));
                log.debug("resolved name clash, new resource will be <{}>", newResource);
            } else {
                log.debug("no name clash for <{}>", newResource);
            }

            log.debug("POST to <{}> will create new LDP-R <{}>", container, newResource);
            // connection is closed by buildPostResponse
            return buildPostResponse(conn, container, newResource, ldpInteractionModel, postBody, type);
        } catch (InvalidInteractionModelException e) {
            log.debug("POST with invalid interaction model <{}> to <{}>", e.getHref(), container);
            final Response.ResponseBuilder response = createResponse(conn, Response.Status.BAD_REQUEST, container);
            conn.commit();
            return response.entity(e.getMessage()).build();
        } catch (final Throwable t) {
            conn.rollback();
            throw t;
        } finally {
            conn.close();
        }
    }

    /**
     * @param connection the RepositoryConnection (with active transaction) to read extra data from. WILL BE COMMITTED OR ROLLBACKED
     * @throws RepositoryException
     */
    private Response buildPostResponse(RepositoryConnection connection, String container, String newResource, LdpService.InteractionModel interactionModel, InputStream requestBody, MediaType type) throws RepositoryException {
        final String mimeType = LdpUtils.getMimeType(type);
        //checking if resource (container) exists is done later in the service
        try {
            String location = ldpService.addResource(connection, container, newResource, interactionModel, mimeType, requestBody);
            final Response.ResponseBuilder response = createResponse(connection, Response.Status.CREATED, container).location(java.net.URI.create(location));
            if (newResource.compareTo(location) != 0) {
                response.links(Link.fromUri(newResource).rel(LINK_REL_DESCRIBEDBY).param(LINK_PARAM_ANCHOR, location).build()); //FIXME: Sec. 5.2.3.12, see also http://www.w3.org/2012/ldp/track/issues/15
            }
            connection.commit();
            return response.build();
        } catch (IOException | RDFParseException e) {
            final Response.ResponseBuilder resp = createResponse(connection, Response.Status.BAD_REQUEST, container).entity(e.getClass().getSimpleName() + ": " + e.getMessage());
            connection.rollback();
            return resp.build();
        } catch (UnsupportedRDFormatException e) {
            final Response.ResponseBuilder resp = createResponse(connection, Response.Status.UNSUPPORTED_MEDIA_TYPE, container).entity(e);
            connection.rollback();
            return resp.build();
        }
    }

    /**
     * Handle PUT (Sec. 4.2.4, Sec. 5.2.4)
     */
    @PUT
    public Response PUT(@Context UriInfo uriInfo, @Context Request request,
                        @HeaderParam(HttpHeaders.LINK) List<Link> linkHeaders,
                        @HeaderParam(HttpHeaders.IF_MATCH) EntityTag eTag,
                        @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType type, InputStream postBody)
            throws RepositoryException, IOException, InvalidModificationException, RDFParseException, IncompatibleResourceTypeException, URISyntaxException {
        final String resource = ldpService.getResourceUri(uriInfo);
        log.debug("PUT to <{}>", resource);

        final RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();

            final String mimeType = LdpUtils.getMimeType(type);
            final Response.ResponseBuilder resp;
            final String newResource;  // NOTE: newResource == resource for now, this might change in the future
            if (ldpService.exists(conn, resource)) {
                log.debug("<{}> exists, so this is an UPDATE", resource);

                if (eTag == null) {
                    // check for If-Match header (ETag) -> 428 Precondition Required (Sec. 4.2.4.5)
                    log.trace("No If-Match header, but that's a MUST");
                    resp = createResponse(conn, 428, resource);
                    conn.rollback();
                    return resp.build();
                } else {
                    // check ETag -> 412 Precondition Failed (Sec. 4.2.4.5)
                    log.trace("Checking If-Match: {}", eTag);
                    EntityTag hasTag = ldpService.generateETag(conn, resource);
                    if (!eTag.equals(hasTag)) {
                        log.trace("If-Match header did not match, expected {}", hasTag);
                        resp = createResponse(conn, Response.Status.PRECONDITION_FAILED, resource);
                        conn.rollback();
                        return resp.build();
                    }
                }

                newResource = ldpService.updateResource(conn, resource, postBody, mimeType);
                log.debug("PUT update for <{}> successful", newResource);
                resp = createResponse(conn, Response.Status.OK, resource);
                conn.commit();
                return resp.build();
            } else if (ldpService.isReusedURI(conn, resource)) {
                log.debug("<{}> has been deleted, we should not re-use the URI!", resource);
                resp = createResponse(conn, Response.Status.GONE, resource);
                conn.commit();
                return resp.build();
            } else {
                log.debug("<{}> does not exist, so this is a CREATE", resource);
                //LDP servers may allow resource creation using PUT (Sec. 4.2.4.6)

                final String container = LdpUtils.getContainer(resource);
                try {
                    // Check that the target container supports the LDPC Interaction Model
                    final LdpService.InteractionModel containerModel = ldpService.getInteractionModel(conn, container);
                    if (containerModel != LdpService.InteractionModel.LDPC) {
                        final Response.ResponseBuilder response = createResponse(conn, Response.Status.METHOD_NOT_ALLOWED, container);
                        conn.commit();
                        return response.entity(String.format("%s only supports %s Interaction Model", container, containerModel)).build();
                    }

                    // Get the LDP-Interaction Model (Sec. 5.2.3.4 and Sec. 4.2.1.4)
                    final LdpService.InteractionModel ldpInteractionModel = ldpService.getInteractionModel(linkHeaders);

                    // connection is closed by buildPostResponse
                    return buildPostResponse(conn, container, resource, ldpInteractionModel, postBody, type);
                } catch (InvalidInteractionModelException e) {
                    log.debug("PUT with invalid interaction model <{}> to <{}>", e.getHref(), container);
                    final Response.ResponseBuilder response = createResponse(conn, Response.Status.BAD_REQUEST, container);
                    conn.commit();
                    return response.entity(e.getMessage()).build();
                }
            }
        } catch (IOException | RDFParseException e) {
            final Response.ResponseBuilder resp = createResponse(conn, Response.Status.BAD_REQUEST, resource).entity(e.getClass().getSimpleName() + ": " + e.getMessage());
            conn.rollback();
            return resp.build();
        } catch (InvalidModificationException | IncompatibleResourceTypeException e) {
            final Response.ResponseBuilder resp = createResponse(conn, Response.Status.CONFLICT, resource).entity(e.getClass().getSimpleName() + ": " + e.getMessage());
            conn.rollback();
            return resp.build();
        } catch (final Throwable t) {
            conn.rollback();
            throw t;
        } finally {
            conn.close();
        }
    }
    /**
     * Handle delete (Sec. 4.2.5, Sec. 5.2.5)
     */
    @DELETE
    public Response DELETE(@Context UriInfo uriInfo) throws RepositoryException {
        final String resource = ldpService.getResourceUri(uriInfo);
        log.debug("DELETE to <{}>", resource);

        final RepositoryConnection con = sesameService.getConnection();
        try {
            con.begin();

            if (!ldpService.exists(con, resource)) {
                final Response.ResponseBuilder resp;
                if (ldpService.isReusedURI(con, resource)) {
                    resp = createResponse(con, Response.Status.GONE, resource);
                } else {
                    resp = createResponse(con, Response.Status.NOT_FOUND, resource);
                }
                con.rollback();
                return resp.build();
            }

            ldpService.deleteResource(con, resource);
            final Response.ResponseBuilder resp = createResponse(con, Response.Status.NO_CONTENT, resource);
            con.commit();
            return resp.build();
        } catch (final Throwable e) {
            log.error("Error deleting LDP-R: {}: {}", resource, e.getMessage());
            con.rollback();
            throw e;
        } finally {
            con.close();
        }

    }

    @PATCH
    public Response PATCH(@Context UriInfo uriInfo,
                          @HeaderParam(HttpHeaders.IF_MATCH) EntityTag eTag,
                          @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType type, InputStream postBody) throws RepositoryException {
        final String resource = ldpService.getResourceUri(uriInfo);
        log.debug("PATCH to <{}>", resource);

        final RepositoryConnection con = sesameService.getConnection();
        try {
            con.begin();

            if (!ldpService.exists(con, resource)) {
                final Response.ResponseBuilder resp;
                if (ldpService.isReusedURI(con, resource)) {
                    resp = createResponse(con, Response.Status.GONE, resource);
                } else {
                    resp = createResponse(con, Response.Status.NOT_FOUND, resource);
                }
                con.rollback();
                return resp.build();
            }

            if (eTag != null) {
                // check ETag if present
                log.trace("Checking If-Match: {}", eTag);
                EntityTag hasTag = ldpService.generateETag(con, resource);
                if (!eTag.equals(hasTag)) {
                    log.trace("If-Match header did not match, expected {}", hasTag);
                    final Response.ResponseBuilder resp = createResponse(con, Response.Status.PRECONDITION_FAILED, resource);
                    con.rollback();
                    return resp.build();
                }
            }

            // Check for the supported mime-type
            if (!type.toString().equals(RdfPatchParser.MIME_TYPE)) {
                log.trace("Incompatible Content-Type for PATCH: {}", type);
                final Response.ResponseBuilder resp = createResponse(con, Response.Status.UNSUPPORTED_MEDIA_TYPE, resource).entity("Unknown Content-Type: " + type + "\n");
                con.rollback();
                return resp.build();
            }

            try {
                ldpService.patchResource(con, resource, postBody, false);
                final Response.ResponseBuilder resp = createResponse(con, Response.Status.NO_CONTENT, resource);
                con.commit();
                return resp.build();
            } catch (ParseException | InvalidPatchDocumentException e) {
                final Response.ResponseBuilder resp = createResponse(con, Response.Status.BAD_REQUEST, resource).entity(e.getMessage() + "\n");
                con.rollback();
                return resp.build();
            } catch (InvalidModificationException e) {
                final Response.ResponseBuilder resp = createResponse(con, 422, resource).entity(e.getMessage() + "\n");
                con.rollback();
                return resp.build();
            }

        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
    }

    /**
     * Handle OPTIONS (Sec. 4.2.8, Sec. 5.2.8)
     */
    @OPTIONS
    public Response OPTIONS(@Context final UriInfo uriInfo) throws RepositoryException {
        final String resource = ldpService.getResourceUri(uriInfo);
        log.debug("OPTIONS to <{}>", resource);

        final RepositoryConnection con = sesameService.getConnection();
        try {
            con.begin();

            if (!ldpService.exists(con, resource)) {
                final Response.ResponseBuilder resp;
                if (ldpService.isReusedURI(con, resource)) {
                    resp = createResponse(con, Response.Status.GONE, resource);
                } else {
                    resp = createResponse(con, Response.Status.NOT_FOUND, resource);
                }
                con.rollback();
                return resp.build();
            }


            Response.ResponseBuilder builder = createResponse(con, Response.Status.OK, resource);

            addOptionsHeader(con, resource, builder);

            con.commit();
            return builder.build();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }

    }

    private Response.ResponseBuilder addOptionsHeader(RepositoryConnection connection, String resource, Response.ResponseBuilder builder) throws RepositoryException {
        log.debug("Adding required LDP Headers (OPTIONS, GET); see Sec. 8.2.8 and Sec. 4.2.2.2");
        if (ldpService.isNonRdfSourceResource(connection, resource)) {
            // Sec. 4.2.8.2
            log.trace("<{}> is an LDP-NR: GET, HEAD, PUT and OPTIONS allowed", resource);
            builder.allow(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.PUT, HttpMethod.OPTIONS);
        } else if (ldpService.isRdfSourceResource(connection, resource)) {
            if (ldpService.getInteractionModel(connection, resource) == LdpService.InteractionModel.LDPR) {
                log.trace("<{}> is a LDP-RS (LDPR interaction model): GET, HEAD, PUT, PATCH and OPTIONS allowed", resource);
                // Sec. 4.2.8.2
                builder.allow(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.PUT, HTTP_METHOD_PATCH, HttpMethod.OPTIONS);
            } else {
                // Sec. 4.2.8.2
                log.trace("<{}> is a LDP-RS (LDPC interaction model): GET, HEAD, POST, PUT, PATCH and OPTIONS allowed", resource);
                builder.allow(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.POST, HttpMethod.PUT, HTTP_METHOD_PATCH, HttpMethod.OPTIONS);
                // Sec. 4.2.3 / Sec. 5.2.3
                builder.header(HTTP_HEADER_ACCEPT_POST, LdpUtils.getAcceptPostHeader("*/*"));
            }
            // Sec. 4.2.7.1
            builder.header(HTTP_HEADER_ACCEPT_PATCH, RdfPatchParser.MIME_TYPE);
        }

        return builder;
    }

    /**
     * Add all the default headers specified in LDP to the Response
     *
     * @param connection the RepositoryConnection (with active transaction) to read extra data from
     * @param status the StatusCode
     * @param resource the iri/uri/url of the resource
     * @return the provided ResponseBuilder for chaining
     */
    protected Response.ResponseBuilder createResponse(RepositoryConnection connection, Response.Status status, String resource) throws RepositoryException {
        return createResponse(connection, status.getStatusCode(), resource);
    }

    /**
     * Add all the default headers specified in LDP to the Response
     *
     * @param connection the RepositoryConnection (with active transaction) to read extra data from
     * @param status the status code
     * @param resource the uri/url of the resource
     * @return the provided ResponseBuilder for chaining
     */
    protected Response.ResponseBuilder createResponse(RepositoryConnection connection, int status, String resource) throws RepositoryException {
        // FIXME: Switch back to the general ResponseBuilder (once RESTEASY-1106 is fixed)
        // return createResponse(connection, Response.status(status), resource);
        return createResponse(connection, new ResponseBuilderImpl(status), resource);
    }

    /**
     * Add all the default headers specified in LDP to the Response
     *
     * @param connection the RepositoryConnection (with active transaction) to read extra data from
     * @param rb the ResponseBuilder
     * @param resource the uri/url of the resource
     * @return the provided ResponseBuilder for chaining
     */
    protected Response.ResponseBuilder createResponse(RepositoryConnection connection, Response.ResponseBuilder rb, String resource) throws RepositoryException {
        createResponse(rb);

        if (ldpService.exists(connection, resource)) {
            // Link rel='type' (Sec. 4.2.1.4, 5.2.1.4)
            List<Statement> statements = ldpService.getLdpTypes(connection, resource);
            for (Statement stmt : statements) {
                Value o = stmt.getObject();
                if (o instanceof URI && o.stringValue().startsWith(LDP.NAMESPACE)) {
                    rb.link(o.stringValue(), LINK_REL_TYPE);
                }
            }

            final URI rdfSource = ldpService.getRdfSourceForNonRdfSource(connection, resource);
            if (rdfSource != null) {
                // Sec. 5.2.8.1 and 5.2.3.12
                // FIXME: Sec. 5.2.3.12, see also http://www.w3.org/2012/ldp/track/issues/15
                rb.link(rdfSource.stringValue(), LINK_REL_DESCRIBEDBY);
                // TODO: Propose to LDP-WG?
                rb.link(rdfSource.stringValue(), LINK_REL_META);
            }
            final URI nonRdfSource = ldpService.getNonRdfSourceForRdfSource(connection, resource);
            if (nonRdfSource != null) {
                // TODO: Propose to LDP-WG?
                rb.link(nonRdfSource.stringValue(), LINK_REL_CONTENT);
            }

            // ETag (Sec. 4.2.1.3)
            rb.tag(ldpService.generateETag(connection, resource));

            // Last modified date
            rb.lastModified(ldpService.getLastModified(connection, resource));
        }

        return rb;
    }

    /**
     * Add the non-resource related headers specified in LDP to the provided ResponseBuilder
     * @param rb the ResponseBuilder to decorate
     * @return the updated ResponseBuilder for chaining
     */
    protected Response.ResponseBuilder createResponse(Response.ResponseBuilder rb) {
        // Link rel='http://www.w3.org/ns/ldp#constrainedBy' (Sec. 4.2.1.6)
        rb.link(LDP_SERVER_CONSTRAINTS, LINK_REL_CONSTRAINEDBY);

        return rb;
    }

}
