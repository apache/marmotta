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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.slf4j.Logger;

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
@Path("/" + LdpWebService.PATH)
public class LdpWebService {
	
    public static final String PATH = "ldp";

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @PostConstruct
    protected void initialize() {
        // TODO: basic initialisation
    }

    @GET
    public Response GET(@Context UriInfo uriInfo) {
        log.error("Request: {}", uriInfo.getRequestUri());
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @POST
    public Response POST() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @PUT
    public Response PUT() {
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
}
