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
package org.apache.marmotta.platform.core.webservices.triplestore;

import static javax.ws.rs.core.Response.status;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.LdpService;
import org.apache.marmotta.platform.core.util.JerseyUtils;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

/**
 * LDP Web Service (isolated for the moment for experimenting with
 * the concepts, but at some point it should be merged back to 
 * {@link org.apache.marmotta.platform.core.webservices.resource.ResourceWebService})
 * 
 * @author Sergio Fernández
 *
 */
@ApplicationScoped
@Path("/" + ConfigurationService.LDP_PATH)
public class LdpWebService {

    @Inject
    private ConfigurationService configurationService;
    
    @Inject
    private LdpService ldpService;

    private static final String  UUID_PATTERN = "{uuid:[^#?]+}";
    
    /**
     * Produces a list of all containers available 
     * 
     * @return list of container
     */
    @GET
    @Produces("application/json")
    public Response list() {
        try {
            List<String> l = new ArrayList<String>();
            for(URI container : ldpService.list()) {
                l.add(container.stringValue());
            }
            return Response.ok().entity(l).build();
        } catch(Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
    
    /**
     * Returns the content stored on this resource/container
     * 
     * @param types, accepted formats
     * @param uuid, a unique container identifier
     * @return redirect response to the export service
     */
    @GET
    @Path(UUID_PATTERN) 
    public Response get(@HeaderParam("Accept") String types, @PathParam("uuid") String uuid) throws UnsupportedEncodingException,
    URISyntaxException {
        String uri = buildUri(uuid);
        if (ldpService.get(uri) != null) {
            java.net.URI seeAlso = new java.net.URI(configurationService.getBaseUri() + ConfigurationService.RESOURCE_PATH + "?uri=" + uri);
            return Response.seeOther(seeAlso).header("Accept", types).build();
        } else {
            return Response.status(Status.NOT_FOUND).entity("Container not found").header("Accept", types).build();
        }
    }
    
    @POST
    @Path(UUID_PATTERN) 
    public Response create(@HeaderParam("Accept") String types, @PathParam("uuid") String uuid, @QueryParam("title") String title) throws UnsupportedEncodingException,
    URISyntaxException {
        String uri = buildUri(uuid);
        if (ldpService.create(uri)) {
            Response response = status(Status.CREATED).entity("Container created").header("Accept", types).build();
            response.getMetadata().add("Location", uri);
            response.getMetadata().add("Vary", "Content-Type");
            return response;
        } else {
            return Response.status(Status.CONFLICT).entity("Container already exists").header("Accept", types).build();
        }
    }
    
    /**
     * Deletes this resource/container
     * 
     * @param types formats accepted
     * @param uuid container identifier
     * @return response
     */
    @DELETE
    @Path(UUID_PATTERN)
    public Response delete(String types, @PathParam("uuid") String uuid) {
        String uri = buildUri(uuid);
        try {
            final boolean deleted = ldpService.delete(uri);
            if (deleted) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (RepositoryException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    private String buildBaseUri() {
        String root = configurationService.getBaseUri();
        return root.substring(0, root.length() - 1) + JerseyUtils.getResourcePath(this) + "/";
    }

    private String buildUri(String uuid) {
        return buildBaseUri() + uuid;
    }
    
}
