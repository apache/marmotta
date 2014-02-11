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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

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

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    @PostConstruct
    protected void initialize() {
        // TODO: basic initialisation
    }

    @GET
    public Response GET(@Context UriInfo uriInfo, @Context Request request, @PathParam("local") String localPart) {
        /* Both methods result in the absolute path, but which one is preferable? */
        log.trace("RequestUri: {}", uriInfo.getRequestUri());
        log.trace("BaseUri+LocalPart: {}ldp{}", uriInfo.getBaseUri(), localPart);

        // TODO: Proper content negotiation
        final RDFFormat format = RDFFormat.TURTLE;
        final String subject = uriInfo.getRequestUri().toString();

        // Deliver all triples with <subject> as subject.

        // use #createResponse to add global Headers.
        return createResponse(Response.Status.NOT_IMPLEMENTED, uriInfo).build();
    }

    @POST
    public Response POST(@Context UriInfo uriInfo, @Context Request request) {
        /*
         * TODO: POST implementation
         * a POST to an existing resource converts this resource into an LDP-C
         */
        // Honor client wishes from Slug-header (Sec. 6.4.11)
        //    http://www.ietf.org/rfc/rfc5023.txt
        final String localName = UUID.randomUUID().toString();

        final String container = uriInfo.getRequestUri().toString();
        final String newResource = uriInfo.getRequestUriBuilder().path(localName).build().toString();

        // Add container triples (Sec. 6.4.3)

        // use #createResponse to add global Headers.
        // return createResponse(Response.Status.CREATED, uriInfo).location(java.net.URI.create(newResource)).build();

        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @PUT
    public Response PUT(@Context UriInfo uriInfo, @Context Request request) {
        /*
         * TODO: PUT implementation
         *
         * check for If-Match header (ETag) -> 428 Precondition Required (Sec. 5.5.3)
         * check ETag -> 412 Precondition Failed (Sec. 5.5.3)
         * request.evaluatePreconditions(eTag)
         *
         * clients should not be allowed to update LDPC-membership triples -> 409 Conflict (Sec. 6.5.1)
         *
         * if the target resource exists, replace ALL data of the target.
         */
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @DELETE
    public Response DELETE() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @HEAD
    public Response HEAD() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @PATCH
    public Response PATCH() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @OPTIONS
    public Response OPTIONS() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    protected Response.ResponseBuilder createResponse(int status, UriInfo uriInfo) {
        final String targetURI = uriInfo.getRequestUri().toString();
        final Response.ResponseBuilder rb = Response.status(status);

        /* TODO: Add "global" Headers for LDP
         * - ETag (Sec. 5.2.7)
         * - Link rel='type' (Sec. 5.2.8 and 6.2.8)
         * - Link rel='describedby' (Sec. 5.2.11)
         */

        return rb;
    }

    protected Response.ResponseBuilder createResponse(Response.Status status, UriInfo uriInfo) {
        return createResponse(status.getStatusCode(), uriInfo);
    }
}
