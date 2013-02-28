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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.util.JerseyUtils;

/**
 * Knowledge space related services
 * 
 * @author Sergio Fernández
 * 
 */
@ApplicationScoped
@Path("/" + ConfigurationService.KNOWLEDGESPACE_PATH)
@Deprecated
public class KnowledgeSpaceWebService {

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ContextService contextService;
    private static final String  UUID_PATTERN = "{uuid:[^#?]+}";

    /**
     * Get all knowledge spaces
     * 
     * @return a list of URIs representing the current knowledge spaces
     */
    @GET
    @Produces("application/json")
    public Response listSpaces() {
        try {
            List<org.openrdf.model.URI> l = contextService.listContexts();
            ArrayList<String> res = new ArrayList<String>();
            for(org.openrdf.model.URI r : l) {
                String uri = r.stringValue();
                if (uri.startsWith(buildBaseUri())) {
                    res.add(uri);
                }
            }
            return Response.ok().entity(res).build();
        } catch(Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    /**
     * Returns the content stored on this context URI
     * 
     * @param types, accepted formats
     * @param uuid, a unique context identifier
     * @return redirects to the export service
     */
    @GET
    @Path(UUID_PATTERN)
    public Response getContent(@HeaderParam("Accept") String types, @PathParam("uuid") String uuid) throws UnsupportedEncodingException, URISyntaxException {
        String context = buildUri(uuid);
        URI uri = new URI(configurationService.getBaseUri() + "export/download?context=" + context);
        return Response.seeOther(uri).header("Accept", types).build();
    }

    /**
     * Deletes a knowledge space from the system
     * 
     * @param types formats accepted
     * @param uuid knowledge space uuid
     * @return status code
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    @DELETE
    @Path(UUID_PATTERN)
    public Response cleanContent(String types, @PathParam("uuid") String uuid) throws UnsupportedEncodingException, URISyntaxException {
        final boolean deleted = contextService.removeContext(buildUri(uuid));
        return deleted ? Response.ok().build() : Response.status(Status.NOT_FOUND).build();
    }

    private String buildBaseUri() {
        String root = configurationService.getBaseUri();
        return root.substring(0, root.length() - 1) + JerseyUtils.getResourcePath(this) + "/";
    }

    private String buildUri(String uuid) {
        return buildBaseUri() + uuid;
    }

}
