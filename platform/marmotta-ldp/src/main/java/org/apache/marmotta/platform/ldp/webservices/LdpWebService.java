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
import org.apache.marmotta.commons.vocabulary.DCTERMS;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.ldp.util.EntityTagUtils;
import org.apache.marmotta.platform.ldp.util.LdpWebServiceUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
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
 * Linked Data Platform web services
 *
 * @see <a href="http://www.w3.org/TR/ldp/">http://www.w3.org/TR/ldp/</a>
 *
 * @author Sergio Fernández
 * @author Jakob Frank
 */
@ApplicationScoped
/* FIXME: imho this should be root '/' (jakob) */
@Path(LdpWebService.PATH + "{local:.*}")
public class LdpWebService {

    public static final String PATH = "ldp";

    private Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    @PostConstruct
    protected void initialize() {
        // TODO: basic initialisation
    }

    @GET
    public Response GET(@Context final UriInfo uriInfo, @Context Request r, @HeaderParam(HttpHeaders.ACCEPT) MediaType type)
            throws RepositoryException {
        return doGetHead(uriInfo, r ,type).build();
    }

    private Response.ResponseBuilder doGetHead(final UriInfo uriInfo, Request r, MediaType type)
            throws RepositoryException {
        final String subject = getResourceUri(uriInfo);
        log.debug("GET to PDP-R <{}>", subject);
        log.warn("NOT CHECKING EXISTENCE OF <{}>", subject);

        // TODO: Maybe this is a LDP-BR?
        // TODO: Proper content negotiation
        r.evaluatePreconditions();

        final RDFFormat format = Rio.getWriterFormatForMIMEType(type.toString());
        if (format == null) {
            log.warn("GET to <{}> with unknown accept {}", subject, type);
            return createResponse(Response.Status.NOT_IMPLEMENTED, uriInfo);
        } else {
            // Deliver all triples with <subject> as subject.
            final StreamingOutput entity = new StreamingOutput() {
                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    try {
                        RepositoryConnection con = sesameService.getConnection();
                        try {
                            con.begin();
                            URI s = con.getValueFactory().createURI(subject);
                            // TODO: this should be a little more sophisticated...
                            // TODO: non-membership triples flag
                            RDFWriter writer = Rio.createWriter(format, output);
                            con.exportStatements(s, null, null, false, writer);

                            con.commit();
                        } catch (RDFHandlerException e) {
                            con.rollback();
                            throw new WebApplicationException(e, createResponse(Response.Status.INTERNAL_SERVER_ERROR, uriInfo).build());
                        } finally {
                            con.close();
                        }
                    } catch (RepositoryException e) {
                        throw new WebApplicationException(e, createResponse(Response.Status.INTERNAL_SERVER_ERROR, uriInfo).build());
                    }
                }
            };
            return createResponse(Response.Status.OK, uriInfo).entity(entity).type(type);
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
        /*
         * TODO: POST implementation
         * a POST to an existing resource converts this resource into an LDP-C
         */

        final String container = getResourceUri(uriInfo);
        log.debug("POST to LDP-R <{}>", container);

        // TODO: Check if resource exists
        log.warn("NOT CHECKING EXISTENCE OF <{}>", container);

        final String localName;
        if (StringUtils.isBlank(slug)) {
            /* Sec. 6.4.9) */
            localName = UUID.randomUUID().toString();
        } else {
            // Honor client wishes from Slug-header (Sec. 6.4.11)
            //    http://www.ietf.org/rfc/rfc5023.txt
            log.trace("Slug-Header is '{}'", slug);
            localName = LdpWebServiceUtils.urify(slug);
            log.trace("Slug-Header urified: {}", localName);
        }
        final String newResource = uriInfo.getRequestUriBuilder().path(localName).build().toString();
        log.debug("POST to <{}> will create new LDP-R <{}>", container, newResource);

        RepositoryConnection con = sesameService.getConnection();
        try {
            // Add container triples (Sec. 6.4.3)
            con.begin();
            // container and meta triples!
            final URI c = con.getValueFactory().createURI(container);
            final URI s = con.getValueFactory().createURI(newResource);

            Literal now = con.getValueFactory().createLiteral(new Date());

            con.add(c, RDF.TYPE, LDP.BasicContainer);
            con.add(c, LDP.member, s);
            con.remove(c, DCTERMS.modified, null);
            con.add(c, DCTERMS.modified, now);

            con.add(s, DCTERMS.created, now);
            con.add(s, DCTERMS.modified, now);
            // LDP-BC for now!
            con.commit();

            // Add the bodyContent
            log.trace("Content ({}) for new resource <{}>", type, newResource);
            final RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(type.toString());
            if (rdfFormat == null) {
                log.debug("POST creates new LDP-BR with type {}", type);
                log.warn("LDP-BR not (yet) supported!");
                return createResponse(Response.Status.NOT_IMPLEMENTED, uriInfo).entity("LDP-BR not (yet) supported").build();
            } else {
                log.debug("POST creates new LDP-RR, data provided as {}", rdfFormat.getName());
                try {
                    con.begin();

                    // FIXME: We are (are we?) allowed to filter out server-managed properties here
                    con.add(postBody, newResource, rdfFormat);

                    con.commit();
                    return createResponse(Response.Status.CREATED, uriInfo).location(java.net.URI.create(newResource)).build();
                } catch (IOException | RDFParseException e) {
                    return createResponse(Response.Status.BAD_REQUEST, uriInfo).entity(e.getClass().getSimpleName() + ": "+ e.getMessage()).build();
                }
            }
        } catch (final Throwable t) {
            if (con.isActive()) {
                con.rollback();
            }
            throw t;
        } finally {
            con.close();
        }
    }

    @PUT
    public Response PUT(@Context UriInfo uriInfo, @Context Request request,
                        @HeaderParam(HttpHeaders.IF_MATCH) EntityTag eTag,
                        InputStream postBody, @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType type)
            throws RepositoryException {
        /*
         * Handle PUT (Sec. 5.5, Sec. 6.5)
         */
        final String resource = getResourceUri(uriInfo);
        log.debug("PUT to <{}>", resource);

        log.warn("NOT CHECKING EXISTENCE OF <{}>", resource);

        if (eTag == null) {
            // check for If-Match header (ETag) -> 428 Precondition Required (Sec. 5.5.3)
            log.trace("No If-Match header, but that's a MUST");
            return createResponse(428, uriInfo).build();
        } else {
            // check ETag -> 412 Precondition Failed (Sec. 5.5.3)
            log.trace("Checking If-Match: {}", eTag);
            final RepositoryConnection con = sesameService.getConnection();
            try {
                con.begin();
                EntityTag hasTag = generateETag(con, resource);
                con.commit();

                if (!EntityTagUtils.equals(eTag, hasTag)) {
                    log.trace("If-Match header did not match, expected {}", hasTag);
                    return createResponse(Response.Status.PRECONDITION_FAILED, uriInfo).build();
                }
            } finally {
                con.close();
            }

        }

        /*
         * TODO: PUT implementation
         *
         * clients should not be allowed to update LDPC-membership triples -> 409 Conflict (Sec. 6.5.1)
         *
         * if the target resource exists, replace ALL data of the target.
         */
        return createResponse(Response.Status.NOT_IMPLEMENTED, uriInfo).build();
    }

    @DELETE
    public Response DELETE(@Context UriInfo uriInfo) throws RepositoryException {
        /*
         * Handle delete (Sec. 5.6, Sec. 6.6)
         */
        final String resource = getResourceUri(uriInfo);
        log.debug("DELETE to <{}>", resource);

        log.warn("NOT CHECKING EXISTENCE OF <{}>", resource);

        final RepositoryConnection con = sesameService.getConnection();
        try {
            con.begin();

            URI s = con.getValueFactory().createURI(resource);
            Literal now = con.getValueFactory().createLiteral(new Date());

            // Delete corresponding containment and membership triples (Sec. 6.6.1)
            RepositoryResult<Statement> stmts = con.getStatements(null, LDP.member, s, false);
            try {
                while (stmts.hasNext()) {
                    Statement st = stmts.next();
                    con.remove(st.getSubject(), DCTERMS.modified, null);
                    con.add(st.getSubject(), DCTERMS.modified, now);
                    con.remove(st);
                }
            } finally {
                stmts.close();
            }
            // Delete the resource
            con.remove(s, null, null);


            con.commit();
        } finally {
            con.close();
        }

        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @HEAD
    public Response HEAD(@Context final UriInfo uriInfo, @Context Request r, @HeaderParam(HttpHeaders.ACCEPT) MediaType type)
            throws RepositoryException {
        final Response.ResponseBuilder rb = doGetHead(uriInfo, r, type);
        rb.entity(null);
        return rb.build();
    }

    @PATCH
    public Response PATCH() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @OPTIONS
    public Response OPTIONS(@Context final UriInfo uriInfo) {
        /*
         * Handle OPTIONS (Sec. 5.9, Sec. 6.9)
         */
        final String resource = getResourceUri(uriInfo);
        log.debug("OPTIONS to <{}>", resource);

        log.warn("NOT CHECKING EXISTENCE OF <{}>", resource);

        Response.ResponseBuilder builder = createResponse(Response.Status.OK, uriInfo);

        // Sec. 5.9.2
        builder.allow("GET", "HEAD", "POST", "OPTIONS");

        // Sec. 6.4.14 / Sec. 8.1
        builder.header("Accept-Post", "text/turtle, */*");

        // TODO: Sec. 5.8.2
        //builder.header("Accept-Patch", null);


        // TODO: Sec. 6.9.1
        //builder.link(resource, "meta");


        return builder.build();
    }

    protected Response.ResponseBuilder createResponse(int status, UriInfo uriInfo) {
        final String targetURI = uriInfo.getRequestUri().toString();
        return createResponse(Response.status(status), uriInfo);
    }

    protected Response.ResponseBuilder createResponse(Response.ResponseBuilder rb, UriInfo uriInfo) {
        // Link LDP-R rel='type' (Sec. 5.2.8)
        rb.link(LDP.Resource.stringValue(), "type");
        // Link LDP-C rel='type' (Sec. 6.2.8)
        rb.link(LDP.Container.stringValue(), "type");
        rb.link(LDP.BasicContainer.stringValue(), "type");
        // TODO: be more specific here? BasicContainer, DirectContainer, IndirectContainer

        // Link rel='describedby' (Sec. 5.2.11)
        rb.link("http://wiki.apache.org/marmotta/LDPImplementationReport", "describedby");

        // ETag (Sec. 5.2.7)
        final String rUri = getResourceUri(uriInfo);
        try {
            final RepositoryConnection con = sesameService.getConnection();
            try {
                con.begin();
                rb.tag(generateETag(con, rUri));
                con.commit();
            } finally {
                con.close();
            }
        } catch (RepositoryException e) {
            log.error("Could not build ETag", e);
        }

        return new RB(rb);
    }

    protected Response.ResponseBuilder createResponse(Response.Status status, UriInfo uriInfo) {
        return createResponse(status.getStatusCode(), uriInfo);
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

        String uri = uriBuilder.build().toString();
        log.debug("RequestUri: {}", uri);
        return uri;
    }


    protected static EntityTag generateETag(RepositoryConnection con, String uri) throws RepositoryException {
        return generateETag(con, con.getValueFactory().createURI(uri));
    }

    protected static EntityTag generateETag(RepositoryConnection con, URI uri) throws RepositoryException {
        RepositoryResult<Statement> stmts = con.getStatements(uri, DCTERMS.modified, null, true);
        try {
            // TODO: ETag is the last-modified date (explicitly managed) thus only weak.
            Date latest = null;
            while (stmts.hasNext()) {
                Value o = stmts.next().getObject();
                if (o instanceof Literal) {
                    Date d = ((Literal)o).calendarValue().toGregorianCalendar().getTime();
                    if (latest == null || d.after(latest)) {
                        latest = d;
                    }
                }
            }
            if (latest != null) {
                return new EntityTag(String.valueOf(latest.getTime()), true);
            } else {
                return null;
            }
        } finally {
            stmts.close();
        }
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
