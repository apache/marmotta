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
package org.apache.marmotta.platform.core.webservices.prefix;

import org.apache.marmotta.platform.core.api.prefix.PrefixService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Prefix web service
 * 
 * @author Sergio Fernández
 * 
 */
@ApplicationScoped
@Path("/prefix")
public class PrefixWebService {

    @Inject
    private Logger log;

    @Inject
    private PrefixService prefixService;

    private static final String PREFIX_PATTERN = "{prefix: [a-z][a-zA-Z0-9\\._-]+}";

    /**
     * Get all prefixes mappings
     * 
     * @return all current prefixes mappings
     */
    @GET
    @Produces("application/json")
    public Response getMappings() {
        Map<String, String> mappings = (prefixService != null ? prefixService.getMappings() : new HashMap<String,String>());
        return Response.ok().entity(mappings).build();
    }

    /**
     * Get namespace
     * 
     * @param prefix prefix
     * @return Response with the mapping, if exists
     */
    @GET
    @Path("/" + PREFIX_PATTERN)
    @Produces("application/json")
    public Response getMapping(@PathParam("prefix") String prefix) {
        if (prefixService.containsPrefix(prefix)) {
            Map<String, String> result = new HashMap<String, String>();
            result.put(prefix, prefixService.getNamespace(prefix));
            return Response.ok().entity(result).build();
        } else {
            log.error("prefix " + prefix + " mapping not found");
            return Response.status(Response.Status.NOT_FOUND).entity("prefix " + prefix + " mapping not found").build();
        }
    }
    
    /**
     * Removes a prefix
     * 
     * @param prefix prefix
     * @return Response with the result of the operation
     */
    @DELETE
    @Path("/" + PREFIX_PATTERN)
    public Response deleteMapping(@PathParam("prefix") String prefix) {
        if (prefixService.remove(prefix)) {
            return Response.status(Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("prefix " + prefix + " mapping not found").build();
        }
    }    

    /**
     * Add new mapping
     * 
     * @param prefix prefix
     * @param namespace uri
     * @return operation result
     */
    @POST
    @Path("/" + PREFIX_PATTERN)
    public Response addMapping(@PathParam("prefix") String prefix, @QueryParam("uri") @NotNull String namespace) {
        try {
            prefixService.add(prefix, namespace);
            return Response.status(Response.Status.CREATED).build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    /**
     * Reverse prefix lookup
     * 
     * @param uri namespace
     * @return Response with the result of the reverse search
     */
    @GET
    @Path("/reverse")
    @Produces("application/json")
    public Response getPrefix(@QueryParam("uri") @NotNull String uri) {
        if (StringUtils.isNotBlank(uri)) {
            if (prefixService.containsNamespace(uri)) {
                Map<String, String> result = new HashMap<String, String>();
                result.put(uri, prefixService.getPrefix(uri));
                return Response.ok().entity(result).build();
            } else {
                log.error("namespace " + uri + " mapping not found");
                return Response.status(Response.Status.NOT_FOUND).entity("namespace " + uri + " mapping not found").build();
            }
        } else {
            log.error("Empty namespace requested");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

}
