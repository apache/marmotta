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
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Linked Data Platform web services.
 *
 * FIXME: Try using less transactions, i.e. use a single RepositoryConnection per request
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
    }

    @GET
    public Response GET(@Context final UriInfo uriInfo, @Context Request r, @HeaderParam(HttpHeaders.ACCEPT) MediaType type) throws RepositoryException {
        final String resource = getResourceUri(uriInfo);
        if (log.isDebugEnabled()) {
            log.debug("GET to LDPR <{}>", resource);
        }
        return buildGetResponse(resource, r, type).build();
    }

    @HEAD
    public Response HEAD(@Context final UriInfo uriInfo, @Context Request r, @HeaderParam(HttpHeaders.ACCEPT) MediaType type)  throws RepositoryException {
        final String resource = getResourceUri(uriInfo);
        if (log.isDebugEnabled()) {
            log.debug("HEAD to LDPR <{}>", resource);
        }
        return buildGetResponse(resource, r, type).entity(null).build();
    }

    private Response.ResponseBuilder buildGetResponse(final String resource, Request r, MediaType type) throws RepositoryException {
        final RepositoryConnection con = sesameService.getConnection();
        try {
            con.begin();

            if (!ldpService.exists(con, resource)) {
                final Response.ResponseBuilder resp = createResponse(con, Response.Status.NOT_FOUND, resource);
                con.rollback();
                return resp;
            }

            // TODO: Maybe this is a LDP-BR?
            // TODO: Proper content negotiation

            final RDFFormat format = Rio.getWriterFormatForMIMEType(type.toString(), RDFFormat.TURTLE);
            if (format == null) {
                log.warn("GET to <{}> with non-RDF format {}, so looking for a LDP-BR", resource, type);
                final StreamingOutput entity = new StreamingOutput() {
                    @Override
                    public void write(OutputStream out) throws IOException, WebApplicationException {
                        try {
                            ldpService.exportResource(con, resource, out);
                        } catch (RepositoryException | IOException e) {
                            throw new WebApplicationException(e, createResponse(Response.status(Response.Status.INTERNAL_SERVER_ERROR)).entity(e).build());
                        }
                    }
                };
                final Response.ResponseBuilder resp = createResponse(con, Response.Status.OK, resource).entity(entity).type(format.getDefaultMIMEType());
                con.commit();
                return resp;
            } else {
                // Deliver all triples with <subject> as subject.
                final StreamingOutput entity = new StreamingOutput() {
                    @Override
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        try {
                            final RepositoryConnection outputCon = sesameService.getConnection();;
                            try {
                                outputCon.begin();
                                ldpService.exportResource(outputCon, resource, output, format);
                                outputCon.commit();
                            } catch (RDFHandlerException e) {
                                outputCon.rollback();
                                throw new NoLogWebApplicationException(e, createResponse(Response.status(Response.Status.INTERNAL_SERVER_ERROR)).entity(e.getMessage()).build());
                            } catch (final Throwable t) {
                                outputCon.rollback();
                                throw t;
                            } finally {
                                outputCon.close();
                            }
                        } catch (RepositoryException e) {
                            throw new WebApplicationException(e, createResponse(Response.status(Response.Status.INTERNAL_SERVER_ERROR)).entity(e).build());
                        }
                    }
                };
                final Response.ResponseBuilder resp = createResponse(con, Response.Status.OK, resource).entity(entity).type(format.getDefaultMIMEType());
                con.commit();
                return resp;
            }
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
    }

    /**
     * LDP Post Request
     *
     * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/ldp.html#ldpr-HTTP_POST">5.4 LDP-R POST</a>
     * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/ldp.html#ldpc-HTTP_POST">6.4 LDP-C POST</a>
     */
    @POST
    public Response POST(@Context UriInfo uriInfo, @HeaderParam("Slug") String slug, InputStream postBody, @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType type)
            throws RepositoryException {

        final String container = getResourceUri(uriInfo);
        log.debug("POST to LDPC <{}>", container);

        final RepositoryConnection conn = sesameService.getConnection();
        try {

            final String localName;
            if (StringUtils.isBlank(slug)) {
                /* Sec. 6.4.9) */
                // FIXME: Maybe us a shorter uid?
                localName = UUID.randomUUID().toString();
            } else {
                // Honor client wishes from Slug-header (Sec. 6.4.11)
                //    http://www.ietf.org/rfc/rfc5023.txt
                log.trace("Slug-Header is '{}'", slug);
                localName = LdpUtils.urify(slug);
                log.trace("Slug-Header urified: {}", localName);
            }

            String newResource = uriInfo.getRequestUriBuilder().path(localName).build().toString();

            conn.begin();

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

            //checking if resource (container) exists is done later in the service
            try {
                String location = ldpService.addResource(conn, container, newResource, type, postBody);
                final Response.ResponseBuilder response = createResponse(conn, Response.Status.CREATED, container).location(java.net.URI.create(location));
                if (newResource.compareTo(location) != 0) {
                    response.link(newResource, "describedby"); //Sec. 6.2.3.12, see also http://www.w3.org/2012/ldp/track/issues/15
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
        } catch (final Throwable t) {
            conn.rollback();
            throw t;
        } finally {
            conn.close();
        }
    }

    /**
     * Handle PUT (Sec. 5.5, Sec. 6.5)
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
                final Response.ResponseBuilder resp = createResponse(con, Response.Status.NOT_FOUND, resource);
                con.rollback();
                return resp.build();
            }

            if (eTag == null) {
                // check for If-Match header (ETag) -> 428 Precondition Required (Sec. 5.5.3)
                log.trace("No If-Match header, but that's a MUST");
                final Response.ResponseBuilder resp = createResponse(con, 428, resource);
                con.rollback();
                return resp.build();
            } else {
                // check ETag -> 412 Precondition Failed (Sec. 5.5.3)
                log.trace("Checking If-Match: {}", eTag);
                EntityTag hasTag = ldpService.generateETag(con, resource);
                if (!EntityTagUtils.equals(eTag, hasTag)) {
                    log.trace("If-Match header did not match, expected {}", hasTag);
                    final Response.ResponseBuilder resp = createResponse(con, Response.Status.PRECONDITION_FAILED, resource);
                    con.rollback();
                    return resp.build();
                }
            }

            /*
             * TODO: PUT implementation
             *
             * clients should not be allowed to update LDPC-membership triples -> 409 Conflict (Sec. 6.5.1)
             *
             * if the target resource exists, replace ALL data of the target.
             */
            final Response.ResponseBuilder resp = createResponse(con, Response.Status.NOT_IMPLEMENTED, resource);
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
     * Handle delete (Sec. 5.6, Sec. 6.6)
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
     * Handle OPTIONS (Sec. 5.9, Sec. 6.9)
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

            // Sec. 5.9.2
            builder.allow("GET", "HEAD", "POST", "PATCH", "OPTIONS");

            // Sec. 6.4.14 / Sec. 8.1
            // builder.header("Accept-Post", "text/turtle, */*");
            builder.header("Accept-Post", "text/turtle");

            // Sec. 5.8.2
            builder.header("Accept-Patch", RdfPatchParser.MIME_TYPE);


            // TODO: Sec. 6.9.1
            //builder.link(resource, "meta");

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
            // Link rel='type' (Sec. 5.2.8, 6.2.8)
            List<Statement> statements = ldpService.getLdpTypes(connection, resource);
            for (Statement stmt : statements) {
                Value o = stmt.getObject();
                if (o instanceof URI && o.stringValue().startsWith(LDP.NAMESPACE)) {
                    rb.link(o.stringValue(), "type");
                }
            }

            // ETag (Sec. 5.2.7)
            rb.tag(ldpService.generateETag(connection, resource));

            // Last modified date
            rb.lastModified(ldpService.getLastModified(connection, resource));
        }

        return new RB(rb);
    }

    /**
     * Add the non-resource related headers specified in LDP to the provided ResponseBuilder
     * @param rb the ResponseBuilder to decorate
     * @return the updated ResponseBuilder for chaining
     */
    private Response.ResponseBuilder createResponse(Response.ResponseBuilder rb) {
        // Link rel='describedby' (Sec. 5.2.11)
        rb.link("http://wiki.apache.org/marmotta/LDPImplementationReport", "describedby");

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

    /**
     * FIXME: THIS SHOULD GO AWAY!
     */
    private class RB extends Response.ResponseBuilder {

        private final Response.ResponseBuilder delegate;

        @Override
        public Response build() {
            Response response = delegate.build();
            log.trace("Sending Response {} ({})", response.getStatus(), response.getStatusInfo().getReasonPhrase());
            return response;
        }

        @Override
        public Response.ResponseBuilder clone() {
            return delegate.clone();
        }

        @Override
        public Response.ResponseBuilder status(int status) {
            return delegate.status(status);
        }

        @Override
        public Response.ResponseBuilder status(Response.StatusType status) {
            return delegate.status(status);
        }

        @Override
        public Response.ResponseBuilder status(Response.Status status) {
            return delegate.status(status);
        }

        @Override
        public Response.ResponseBuilder entity(Object entity) {
            return delegate.entity(entity);
        }

        @Override
        public Response.ResponseBuilder entity(Object entity, Annotation[] annotations) {
            return delegate.entity(entity, annotations);
        }

        @Override
        public Response.ResponseBuilder allow(String... methods) {
            return delegate.allow(methods);
        }

        @Override
        public Response.ResponseBuilder allow(Set<String> methods) {
            return delegate.allow(methods);
        }

        @Override
        public Response.ResponseBuilder cacheControl(CacheControl cacheControl) {
            return delegate.cacheControl(cacheControl);
        }

        @Override
        public Response.ResponseBuilder encoding(String encoding) {
            return delegate.encoding(encoding);
        }

        @Override
        public Response.ResponseBuilder header(String name, Object value) {
            return delegate.header(name, value);
        }

        @Override
        public Response.ResponseBuilder replaceAll(MultivaluedMap<String, Object> headers) {
            return delegate.replaceAll(headers);
        }

        @Override
        public Response.ResponseBuilder language(String language) {
            return delegate.language(language);
        }

        @Override
        public Response.ResponseBuilder language(Locale language) {
            return delegate.language(language);
        }

        @Override
        public Response.ResponseBuilder type(MediaType type) {
            return delegate.type(type);
        }

        @Override
        public Response.ResponseBuilder type(String type) {
            return delegate.type(type);
        }

        @Override
        public Response.ResponseBuilder variant(Variant variant) {
            return delegate.variant(variant);
        }

        @Override
        public Response.ResponseBuilder contentLocation(java.net.URI location) {
            return delegate.contentLocation(location);
        }

        @Override
        public Response.ResponseBuilder cookie(NewCookie... cookies) {
            return delegate.cookie(cookies);
        }

        @Override
        public Response.ResponseBuilder expires(Date expires) {
            return delegate.expires(expires);
        }

        @Override
        public Response.ResponseBuilder lastModified(Date lastModified) {
            return delegate.lastModified(lastModified);
        }

        @Override
        public Response.ResponseBuilder location(java.net.URI location) {
            return delegate.location(location);
        }

        @Override
        public Response.ResponseBuilder tag(EntityTag tag) {
            return delegate.tag(tag);
        }

        @Override
        public Response.ResponseBuilder tag(String tag) {
            return delegate.tag(tag);
        }

        @Override
        public Response.ResponseBuilder variants(Variant... variants) {
            return delegate.variants(variants);
        }

        @Override
        public Response.ResponseBuilder variants(List<Variant> variants) {
            return delegate.variants(variants);
        }

        @Override
        public Response.ResponseBuilder links(Link... links) {
            return delegate.links(links);
        }

        @Override
        public Response.ResponseBuilder link(java.net.URI uri, String rel) {
            return delegate.link(uri, rel);
        }

        @Override
        public Response.ResponseBuilder link(String uri, String rel) {
            return delegate.link(uri, rel);
        }

        public RB(Response.ResponseBuilder delegate) {
            this.delegate = delegate;
        }

    }
}
