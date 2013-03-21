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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.util.JerseyUtils;

import org.apache.commons.lang.StringUtils;

/**
 * Context Web Service
 * 
 * @author Thomas Kurz
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
@ApplicationScoped
@Path("/" + ConfigurationService.CONTEXT_PATH)
public class ContextWebService {

    @Inject
    private ContextService contextService;

    @Inject
    private ConfigurationService configurationService;

    private static final String  UUID_PATTERN = "{uuid:[^#?]+}";

    /**
     * Indirect context identification, listing in case 'graph' is missing
     * 
     * @param types
     * @param context uri
     * @return response
     * @throws URISyntaxException
     * @see <a href="http://www.w3.org/TR/sparql11-http-rdf-update/#indirect-graph-identification">Indirect Graph Identification</a>
     */
    @GET
    public Response get(@HeaderParam("Accept") String types, @QueryParam("graph") String context) throws URISyntaxException {
        URI uri;
        if (StringUtils.isBlank(context)) {
            uri = new URI(configurationService.getServerUri() + ConfigurationService.CONTEXT_PATH + "/list");
        } else {
            uri = buildExportUri(context);            
        }
        return Response.seeOther(uri).header("Accept", types).build();
    }
    
    /**
     *
     * @return a list of URIs representing contexts
     */
    @GET
    @Path("/list")
    @Produces("application/json")
    public Response listContexts(@QueryParam("labels") String labels, @QueryParam("filter") String filter) {
        try {
            if(labels == null) {
                ArrayList<String> res = new ArrayList<String>();
                for(org.openrdf.model.URI r : contextService.listContexts(filter != null)) {
                    res.add(r.stringValue());
                }
                return Response.ok().entity(res).build();
            } else {
                ArrayList<Map<String,String>> result = new ArrayList<Map<String, String>>();
                for(org.openrdf.model.URI r : contextService.listContexts(filter != null)) {
                    Map<String,String> ctxDesc = new HashMap<String, String>();
                    ctxDesc.put("uri",r.stringValue());
                    ctxDesc.put("label", contextService.getContextLabel(r));
                    result.add(ctxDesc);
                }
                return Response.ok().entity(result).build();
            }
        } catch(Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
    
    /**
     * Returns the content stored on this context
     * 
     * @param types, accepted formats
     * @param uuid, a unique context identifier
     * @return redirects to the export service
     */
    @GET
    @Path(UUID_PATTERN)
    public Response getContext(@HeaderParam("Accept") String types, @PathParam("uuid") String uuid) throws UnsupportedEncodingException,
    URISyntaxException {
        String context = buildUri(uuid);
        URI uri = buildExportUri(context);
        return Response.seeOther(uri).header("Accept", types).build();
    }
    
    @PUT
    public Response put(@QueryParam("graph") String context, @HeaderParam("Content-Type") String type, @Context HttpServletRequest request) throws IOException {
        if (StringUtils.isBlank(context)) {
            return Response.status(Status.NOT_ACCEPTABLE).entity("missing 'graph' uri for indirect grpah identification").build();
        } else {
            if(type != null && type.lastIndexOf(';') >= 0) {
                type = type.substring(0,type.lastIndexOf(';'));
            }
            Set<String> acceptedFormats = contextService.getAcceptFormats();
            if (type == null || !acceptedFormats.contains(type)) {
                return Response.status(412).entity("define a valid content-type (types: " + acceptedFormats + ")").build();
            }   
            final boolean imported = contextService.importContent(context, request.getInputStream(), type);
            return imported ? Response.ok().build() : Response.status(Status.NOT_FOUND).build();
        }        
    }
    
    @PUT
    public Response putContext(@PathParam("uuid") String uuid, @HeaderParam("Content-Type") String type, @Context HttpServletRequest request) throws IOException {
        return put(buildUri(uuid), type, request);     
    }

    @DELETE
    public Response delete(@QueryParam("graph") String context) {
        if (StringUtils.isBlank(context)) {
            return Response.status(Status.NOT_ACCEPTABLE).entity("missing 'graph' uri for indirect grpah identification").build();
        } else {
            final boolean deleted = contextService.removeContext(context);
            return deleted ? Response.ok().build() : Response.status(Status.NOT_FOUND).build();
        }
    }
    
    /**
     * Deletes a named graph from the system
     * 
     * @param types formats accepted
     * @param uuid context identifier
     * @return status code
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    @DELETE
    @Path(UUID_PATTERN)
    public Response deleteContext(@PathParam("uuid") String uuid) {
        return delete(buildUri(uuid));
    }

    private String buildBaseUri() {
        String root = configurationService.getBaseUri();
        return root.substring(0, root.length() - 1) + JerseyUtils.getResourcePath(this) + "/";
    }

    private String buildUri(String uuid) {
        return buildBaseUri() + uuid;
    }
    
    private URI buildExportUri(String uri) throws URISyntaxException {
        return new URI(configurationService.getBaseUri() + "export/download?context=" + uri);
    }

}
