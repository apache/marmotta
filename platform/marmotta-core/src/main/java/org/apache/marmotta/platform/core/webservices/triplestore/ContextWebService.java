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

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.exporter.ExportService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.util.WebServiceUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Context Web Service, providing support for the SPARQL 1.1 Graph Store HTTP Protocol
 *
 * @see <a href="http://www.w3.org/TR/sparql11-http-rdf-update/">http://www.w3.org/TR/sparql11-http-rdf-update/</a>
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
    
    @Inject
    private ExportService exportService;

    private static final String  UUID_PATTERN = "{uuid:[^#?]+}";
    
    /**
     * List all contexts
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
                ArrayList<Map<String,Object>> result = new ArrayList<>();
                for(org.openrdf.model.URI r : contextService.listContexts(filter != null)) {
                    Map<String,Object> ctxDesc = new HashMap<String, Object>();
                    ctxDesc.put("uri",r.stringValue());
                    ctxDesc.put("label", contextService.getContextLabel(r));
                    ctxDesc.put("size", contextService.getContextSize(r));
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
     * @param uuid, a unique context identifier
     * @param accept Accept HTTP header
     * @param format format requested (overwrites accept header)
     *
     * @return redirects to the export service
     */
    @GET
    @Path(UUID_PATTERN)
    public Response getContext(@PathParam("uuid") String uuid, @HeaderParam("Accept") String accept, @QueryParam("format") String format) throws UnsupportedEncodingException,
    URISyntaxException {
        String context = buildUri(uuid);        
        URI uri = buildExportUri(context, accept, format);
        return Response.seeOther(uri).build();
    }
    
    /**
     * Indirect context identification, listing in case 'graph' is missing
     *
     * @param context uri
     * @param accept Accept HTTP header
     * @param format format requested (overwrites accept header)
     *
     * @return response
     * @throws URISyntaxException
     * @see <a href="http://www.w3.org/TR/sparql11-http-rdf-update/#indirect-graph-identification">Indirect Graph Identification</a>
     */
    @GET
    public Response get(@QueryParam("graph") String context, @HeaderParam("Accept") String accept, @QueryParam("format") String format) throws URISyntaxException {
        if (StringUtils.isBlank(context)) {
            return Response.seeOther(new URI(configurationService.getServerUri() + ConfigurationService.CONTEXT_PATH + "/list")).header("Accept", accept).build();
        } else {
            URI uri = buildExportUri(context, accept, format);
            return Response.seeOther(uri).build();        
        }
    }

    /**
     * Merge of the enclosed RDF payload enclosed into the RDF graph content identified by the encoded URI.
     *
     * @param context context URI
     * @param type Content-Type header
     * @param request request
     *
     * @return response
     * @throws IOException
     */
    @POST
    public Response post(@QueryParam("graph") String context, @HeaderParam("Content-Type") String type, @Context HttpServletRequest request) throws IOException {
        if (StringUtils.isBlank(context)) {
            return Response.status(Status.NOT_ACCEPTABLE).entity("missing 'graph' uri for indirect graph identification").build();
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

    /**
     * Merge of the enclosed RDF payload enclosed into the RDF graph content identified by the request URI.
     *
     * @param uuid context local id
     * @param type Content-Type header
     * @param request request
     *
     * @return response
     * @throws IOException
     */
    @POST
    public Response postContext(@PathParam("uuid") String uuid, @HeaderParam("Content-Type") String type, @Context HttpServletRequest request) throws IOException {
        return post(buildUri(uuid), type, request);
    }

    /**
     * Store the enclosed RDF payload in the context identified by the encoded URI.
     *
     * @param context context uri
     * @param type Content-Type of the payload
     * @param request request object
     *
     * @return response
     * @throws IOException
     */
    @PUT
    public Response put(@QueryParam("graph") String context, @HeaderParam("Content-Type") String type, @Context HttpServletRequest request) throws IOException {
        if (contextService.removeContext(context)) {
            return post(context, type, request);
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Context was not dropped before").build();
        }
    }

    /**
     * Store the enclosed RDF payload in the context identified by the requested URI.
     *
     * @param uuid context local id
     * @param type Content-Type of the payload
     * @param request request object
     *
     * @return response
     * @throws IOException
     */
    @PUT
    public Response putContext(@PathParam("uuid") String uuid, @HeaderParam("Content-Type") String type, @Context HttpServletRequest request) throws IOException {
        return put(buildUri(uuid), type, request);     
    }

    /**
     * Deletes a named graph from the system
     *
     * @param context context uri
     * @return status code
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
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
        return root.substring(0, root.length() - 1) + WebServiceUtil.getResourcePath(this) + "/";
    }

    private String buildUri(String uuid) {
        return buildBaseUri() + uuid;
    }
    
    private URI buildExportUri(String uri, String accept, String format) throws URISyntaxException {
        List<ContentType> acceptedTypes;
        if(StringUtils.isNoneBlank(format)) {
            acceptedTypes = MarmottaHttpUtils.parseAcceptHeader(format);
        } else {
            acceptedTypes = MarmottaHttpUtils.parseAcceptHeader(accept);
        }
        List<ContentType> offeredTypes  = MarmottaHttpUtils.parseStringList(exportService.getProducedTypes());
        offeredTypes.removeAll(Collections.unmodifiableList(Arrays.asList(new ContentType("text", "html"), new ContentType("application", "xhtml+xml"))));
        final ContentType bestType = MarmottaHttpUtils.bestContentType(offeredTypes, acceptedTypes);
        return new URI(configurationService.getBaseUri() + "export/download?context=" + uri + "&format=" + bestType.getMime());
    }

}
