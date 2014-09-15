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
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.apache.marmotta.platform.ldp.exceptions.IncompatibleResourceTypeException;
import org.apache.marmotta.platform.ldp.exceptions.InvalidInteractionModelException;
import org.apache.marmotta.platform.ldp.exceptions.InvalidModificationException;
import org.apache.marmotta.platform.ldp.patch.InvalidPatchDocumentException;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.apache.marmotta.platform.ldp.patch.parser.RdfPatchParser;
import org.apache.marmotta.platform.ldp.util.EntityTagUtils;
import org.apache.marmotta.platform.ldp.util.LdpUtils;
import org.jboss.resteasy.spi.NoLogWebApplicationException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
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

    public static final String PATH = "/ldp"; //FIXME: imho this should be root '/' (jakob)
    public static final String LDP_SERVER_CONSTRAINTS = "https://wiki.apache.org/marmotta/LDPImplementationReport/2014-03-11";

    private Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private LdpService ldpService;

    @Inject
    private SesameService sesameService;

    @PostConstruct
    protected void initialize() {
        // TODO: basic initialisation
        log.info("Starting up LDP WebService Endpoint");
    }

    @GET
    public Response GET(@Context final UriInfo uriInfo, @Context Request r, @HeaderParam(HttpHeaders.ACCEPT) MediaType type) throws RepositoryException {
        final String resource = getResourceUri(uriInfo);
        log.debug("GET to LDPR <{}>", resource);
        return buildGetResponse(resource, r, type).build();
    }

    @HEAD
    public Response HEAD(@Context final UriInfo uriInfo, @Context Request r, @HeaderParam(HttpHeaders.ACCEPT) MediaType type)  throws RepositoryException {
        final String resource = getResourceUri(uriInfo);
        log.debug("HEAD to LDPR <{}>", resource);
        return buildGetResponse(resource, r, type).entity(null).build();
    }

    private Response.ResponseBuilder buildGetResponse(final String resource, Request r, MediaType type) throws RepositoryException {
        final RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();

            log.trace("Checking existence of {}", resource);
            if (!ldpService.exists(conn, resource)) {
                log.debug("{} does not exist", resource);
                final Response.ResponseBuilder resp = createResponse(conn, Response.Status.NOT_FOUND, resource);
                conn.rollback();
                return resp;
            } else {
                log.trace("{} exists, continuing", resource);
            }

            // TODO: Proper content negotiation

            final RDFFormat format;
            if (type.isWildcardType()) { // No explicit Accept Header
                if (ldpService.isRdfSourceResource(conn, resource)) {
                    format = RDFFormat.TURTLE;
                } else {
                    format = null;
                }
            } else {
                format = Rio.getWriterFormatForMIMEType(LdpUtils.getMimeType(type), null);
            }

            if (format == null) {
                log.debug("GET to <{}> with non-RDF format {}, so looking for a LDP-BR", resource, type);
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
                final String realType = ldpService.getMimeType(conn, resource);
                final Response.ResponseBuilder resp = createResponse(conn, Response.Status.OK, resource).entity(entity).type(realType!=null?MediaType.valueOf(realType):type);
                conn.commit();
                return resp;
            } else {
                // Deliver all triples from the <subject> context.
                log.debug("GET to <{}> with RDF format {}, providing LPD-RR data", resource, format.getDefaultMIMEType());
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
                final Response.ResponseBuilder resp = createResponse(conn, Response.Status.OK, resource).entity(entity).type(format.getDefaultMIMEType());
                conn.commit();
                return resp;
            }
        } catch (final Throwable t) {
            conn.rollback();
            throw t;
        } finally {
            conn.close();
        }
    }

    /**
     * LDP Post Request
     *
     * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/ldp.html#ldpr-HTTP_POST">5.4 LDP-R POST</a>
     * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/ldp.html#ldpc-HTTP_POST">6.4 LDP-C POST</a>
     */
    @POST
    public Response POST(@Context UriInfo uriInfo, @HeaderParam("Slug") String slug,
                         @HeaderParam("Link") List<Link> linkHeaders,
                         InputStream postBody, @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType type)
            throws RepositoryException {

        final String container = getResourceUri(uriInfo);
        log.debug("POST to LDPC <{}>", container);

        final RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();

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
            if (ldpService.exists(conn, newResource)) {
                int i = 0;
                final String base = newResource;
                do {
                    final String candidate = base + "-" + (++i);
                    log.trace("<{}> already exists, trying <{}>", newResource, candidate);
                    newResource = candidate;
                } while (ldpService.exists(conn, newResource));
                log.debug("resolved name clash, new resource will be <{}>", newResource);
            } else {
                log.debug("no name clash for <{}>", newResource);
            }

            log.debug("POST to <{}> will create new LDP-R <{}>", container, newResource);
            final String mimeType = LdpUtils.getMimeType(type);
            //checking if resource (container) exists is done later in the service
            try {
                String location = ldpService.addResource(conn, container, newResource, ldpInteractionModel, mimeType, postBody);
                final Response.ResponseBuilder response = createResponse(conn, Response.Status.CREATED, container).location(java.net.URI.create(location));
                if (newResource.compareTo(location) != 0) {
                    response.link(newResource, "describedby"); //FIXME: Sec. 5.2.3.12, see also http://www.w3.org/2012/ldp/track/issues/15
                }
                conn.commit();
                return response.build();
            } catch (IOException | RDFParseException e) {
                final Response.ResponseBuilder resp = createResponse(conn, Response.Status.BAD_REQUEST, container).entity(e.getClass().getSimpleName() + ": " + e.getMessage());
                conn.rollback();
                return resp.build();
            } catch (UnsupportedRDFormatException e) {
                final Response.ResponseBuilder resp = createResponse(conn, Response.Status.UNSUPPORTED_MEDIA_TYPE, container).entity(e);
                conn.rollback();
                return resp.build();
            }
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
     * Handle PUT (Sec. 4.2.4, Sec. 5.2.4)
     */
    @PUT
    public Response PUT(@Context UriInfo uriInfo, @Context Request request,
                        @HeaderParam(HttpHeaders.IF_MATCH) EntityTag eTag,
                        @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType type, InputStream postBody)
            throws RepositoryException {
        final String resource = getResourceUri(uriInfo);
        log.debug("PUT to <{}>", resource);

        final RepositoryConnection con = sesameService.getConnection();
        try {
            con.begin();

            if (!ldpService.exists(con, resource)) {
                log.trace("Resource does not exists: {}", resource);
                final Response.ResponseBuilder resp = createResponse(con, Response.Status.NOT_FOUND, resource);
                con.rollback();
                return resp.build();
            }

            if (eTag == null) {
                // check for If-Match header (ETag) -> 428 Precondition Required (Sec. 4.2.4.5)
                log.trace("No If-Match header, but that's a MUST");
                final Response.ResponseBuilder resp = createResponse(con, 428, resource);
                con.rollback();
                return resp.build();
            } else {
                // check ETag -> 412 Precondition Failed (Sec. 4.2.4.5)
                log.trace("Checking If-Match: {}", eTag);
                EntityTag hasTag = ldpService.generateETag(con, resource);
                if (!EntityTagUtils.equals(eTag, hasTag)) {
                    log.trace("If-Match header did not match, expected {}", hasTag);
                    final Response.ResponseBuilder resp = createResponse(con, Response.Status.PRECONDITION_FAILED, resource);
                    con.rollback();
                    return resp.build();
                }
            }

            final String mimeType = LdpUtils.getMimeType(type);
            log.trace("updating resource <{}>", resource);
            // NOTE: newResource == resource for now, this might change in the future.
            final String newResource = ldpService.updateResource(con, resource, postBody, mimeType);

            final Response.ResponseBuilder resp;
            if (resource.equals(newResource)) {
                log.trace("PUT update for <{}> successful", resource);
                resp = createResponse(con, Response.Status.OK, resource);
            } else {
                log.trace("PUT on <{}> created new resource <{}>", resource, newResource);
                resp = createResponse(con, Response.Status.CREATED, resource).location(java.net.URI.create(newResource));
            }
            con.commit();

            return resp.build();
        } catch (IOException | RDFParseException e) {
            final Response.ResponseBuilder resp = createResponse(con, Response.Status.BAD_REQUEST, resource).entity(e.getClass().getSimpleName() + ": " + e.getMessage());
            con.rollback();
            return resp.build();
        } catch (InvalidModificationException | IncompatibleResourceTypeException e) {
            final Response.ResponseBuilder resp = createResponse(con, Response.Status.CONFLICT, resource).entity(e.getClass().getSimpleName() + ": " + e.getMessage());
            con.rollback();
            return resp.build();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
    }
    /**
     * Handle delete (Sec. 4.2.5, Sec. 5.2.5)
     */
    @DELETE
    public Response DELETE(@Context UriInfo uriInfo) throws RepositoryException {
        final String resource = getResourceUri(uriInfo);
        log.debug("DELETE to <{}>", resource);

        final RepositoryConnection con = sesameService.getConnection();
        try {
            con.begin();

            if (!ldpService.exists(con, resource)) {
                final Response.ResponseBuilder resp = createResponse(con, Response.Status.NOT_FOUND, resource);
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
        final String resource = getResourceUri(uriInfo);
        log.debug("PATCH to <{}>", resource);

        final RepositoryConnection con = sesameService.getConnection();
        try {
            con.begin();

            if (!ldpService.exists(con, resource)) {
                final Response.ResponseBuilder resp = createResponse(con, Response.Status.NOT_FOUND, resource);
                con.rollback();
                return resp.build();
            }

            if (eTag != null) {
                // check ETag if present
                log.trace("Checking If-Match: {}", eTag);
                EntityTag hasTag = ldpService.generateETag(con, resource);
                if (!EntityTagUtils.equals(eTag, hasTag)) {
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
        final String resource = getResourceUri(uriInfo);
        log.debug("OPTIONS to <{}>", resource);

        final RepositoryConnection con = sesameService.getConnection();
        try {
            con.begin();

            log.warn("NOT CHECKING EXISTENCE OF <{}>", resource);

            Response.ResponseBuilder builder = createResponse(con, Response.Status.OK, resource);

            if (ldpService.isNonRdfSourceResource(con, resource)) {
                // Sec. 4.2.8.2
                builder.allow("GET", "HEAD", "OPTIONS");
            } else if (ldpService.isRdfSourceResource(con, resource)) {
                if (ldpService.getInteractionModel(con, resource) == LdpService.InteractionModel.LDPR) {
                    // Sec. 4.2.8.2
                    builder.allow("GET", "HEAD", "PATCH", "OPTIONS");
                } else {
                    // Sec. 4.2.8.2
                    builder.allow("GET", "HEAD", "POST", "PATCH", "OPTIONS");
                    // Sec. 4.2.3 / Sec. 5.2.3
                    builder.header("Accept-Post", LdpUtils.getAcceptPostHeader("*/*"));
                }
                // Sec. 4.2.7.1
                builder.header("Accept-Patch", RdfPatchParser.MIME_TYPE);
            }

            con.commit();
            return builder.build();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }

    }

    /**
     * Add all the default headers specified in LDP to the Response
     *
     * @param connection
     * @param status the status code
     * @param resource the uri/url of the resouce
     * @return the provided ResponseBuilder for chaining
     */
    protected Response.ResponseBuilder createResponse(RepositoryConnection connection, int status, String resource) throws RepositoryException {
        return createResponse(connection, Response.status(status), resource);
    }

    /**
     * Add all the default headers specified in LDP to the Response
     *
     * @param connection
     * @param rb the ResponseBuilder
     * @param resource the uri/url of the resouce
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
                    rb.link(o.stringValue(), "type");
                }
            }

            final URI rdfSource = ldpService.getRdfSourceForNonRdfSource(connection, resource);
            if (rdfSource != null) {
                // Sec. 5.2.8.1 and 5.2.3.12
                // FIXME: Sec. 5.2.3.12, see also http://www.w3.org/2012/ldp/track/issues/15
                rb.link(rdfSource.stringValue(), "meta");
                rb.link(rdfSource.stringValue(), "describedby");
            }
            final URI nonRdfSource = ldpService.getNonRdfSourceForRdfSource(connection, resource);
            if (nonRdfSource != null) {
                // TODO: Propose to LDP-WG?
                rb.link(nonRdfSource.stringValue(), "content");
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
        // Link rel='describedby' (Sec. 4.2.1.6)
        rb.link(LDP_SERVER_CONSTRAINTS, "describedby");

        return rb;
    }

    /**
     * Add all the default headers specified in LDP to the Response
     *
     * @param connection
     * @param status the StatusCode
     * @param resource the uri/url of the resouce
     * @return the provided ResponseBuilder for chaining
     */
    protected Response.ResponseBuilder createResponse(RepositoryConnection connection, Response.Status status, String resource) throws RepositoryException {
        return createResponse(connection, status.getStatusCode(), resource);
    }

    protected String getResourceUri(UriInfo uriInfo) {
        final UriBuilder uriBuilder;
        if (configurationService.getBooleanConfiguration("ldp.force_baseuri", false)) {
            log.trace("UriBuilder is forced to configured baseuri <{}>", configurationService.getBaseUri());
            uriBuilder = UriBuilder.fromUri(java.net.URI.create(configurationService.getBaseUri()));
        } else {
            uriBuilder = uriInfo.getBaseUriBuilder();
        }
        uriBuilder.path(PATH);
        uriBuilder.path(uriInfo.getPathParameters().getFirst("local"));
//        uriBuilder.path(uriInfo.getPath().replaceFirst("/$", ""));

        String uri = uriBuilder.build().toString();
        log.debug("RequestUri: {}", uri);
        return uri;
    }

}
