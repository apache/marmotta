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
package org.apache.marmotta.platform.core.webservices.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.collections.CollectionUtils;
import org.apache.marmotta.commons.http.ETagGenerator;
import org.apache.marmotta.commons.http.UriUtil;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.content.ContentService;
import org.apache.marmotta.platform.core.api.io.MarmottaIOService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.services.sesame.ResourceSubjectMetadata;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.event.InterceptingRepositoryConnection;
import org.openrdf.repository.event.base.InterceptingRepositoryConnectionWrapper;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 * Meta Web Services
 *
 * @author Thomas Kurz
 * @author Sergio Fernández
 */
@ApplicationScoped
@Path("/" + ConfigurationService.META_PATH)
public class MetaWebService {

    @Inject
    private ConfigurationService configurationService;
    
    @Inject
    private TemplatingService templatingService;
    
    @Inject
    private ContextService contextService;

    @Inject
    private ContentService contentService;

    @Inject
    private SesameService sesameService;

    @Inject
    private MarmottaIOService kiWiIOService;

    /**
     * Returns remote resource metadata with the given uri and an accepted
     * return type (mimetype)
     *
     * @param uri
     *            , the fully-qualified URI of the resource to create in the
     *            triple store
     * @param mimetype
     *            , accepted mimetype follows the pattern .+/.+
     * @return a remote resource's metadata (body is the resource data in
     *         requested format)
     * @HTTP 200 resource metadata found and returned
     * @HTTP 400 bad request (maybe uri is not defined)
     * @HTTP 404 resource cannot be found
     * @HTTP 406 resource cannot be found in the given format
     * @HTTP 500 Internal Error
     * @ResponseHeader Content-Type (for HTTP 406) a list of available metadata
     *                 types
     */
    @GET
    @Path(ResourceWebService.MIME_PATTERN)
    public Response getMetaRemote(@QueryParam("uri") String uri, @QueryParam("genid") String genid, @PathParam("mimetype") String mimetype) throws UnsupportedEncodingException {
    	if (StringUtils.isNotBlank(uri)) {
    		return getMeta(URLDecoder.decode(uri, "utf-8"), mimetype);
    	} else if (StringUtils.isNotBlank(genid)) {
    		return getMeta(URLDecoder.decode(genid, "utf-8"), mimetype);
    	} else {
    		return ResourceWebServiceHelper.buildErrorPage(uri, configurationService.getBaseUri(), Status.BAD_REQUEST, "Invalid Request", configurationService, templatingService);
    	}
    }

    /**
     * Returns local resource data with the given uuid and an accepted return
     * type (mimetype)
     *
     * @param uuid
     *            , a unique identifier (must not contain url specific
     *            characters like /,# etc.)
     * @param mimetype
     *            , accepted mimetype follows the pattern .+/.+
     * @return a local resource's metadata (body is the resource data in
     *         requested format)
     * @HTTP 200 resource data found and returned
     * @HTTP 404 resource cannot be found
     * @HTTP 406 resource cannot be found in the given format
     * @HTTP 500 Internal Error
     * @ResponseHeader Content-Type (for HTTP 406) a list of available metadata
     *                 types
     */
    @GET
    @Path(ResourceWebService.MIME_PATTERN + ResourceWebService.UUID_PATTERN)
    public Response getMetaLocal(@PathParam("uuid") String uuid, @PathParam("mimetype") String mimetype) throws UnsupportedEncodingException {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;
        return getMeta(uri, mimetype);
    }

    /**
     * Sets metadata to a given locale resource
     *
     * @param uuid
     *            , a unique identifier (must not contain url specific
     *            characters like /,# etc.)
     * @param mimetype
     *            content-type of the body (metadata) follows the pattern .+/.+
     * @return HTTP response (success or error)
     * @HTTP 200 put was successful
     * @HTTP 400 bad request (e.g. body is empty)
     * @HTTP 404 resource cannot be found
     * @HTTP 415 Content-Type is not supported
     * @HTTP 500 Internal Error
     * @ResponseHeader Content-Type (for HTTP 415) a list of available types for
     *                 metadata
     */
    @PUT
    @Path(ResourceWebService.MIME_PATTERN + ResourceWebService.UUID_PATTERN)
    public Response putMetaLocal(@PathParam("uuid") String uuid, @PathParam("mimetype") String mimetype, @Context HttpServletRequest request) {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;
        return putMeta(uri, mimetype, request);
    }

    /**
     * Sets metadata to a given locale resource
     *
     * @param uri
     *            , the fully-qualified URI of the resource to create in the
     *            triple store
     * @param mimetype
     *            content-type of the body (metadata) follows the pattern .+/.+
     * @return HTTP response (success or error)
     * @HTTP 200 put was successful
     * @HTTP 400 bad request (e.g. uri is null)
     * @HTTP 404 resource cannot be found
     * @HTTP 415 Content-Type is not supported
     * @HTTP 500 Internal Error
     * @ResponseHeader Content-Type (for HTTP 415) a list of available types for
     *                 metadata
     */
    @PUT
    @Path(ResourceWebService.MIME_PATTERN)
    public Response putMetaRemote(@QueryParam("uri") @NotNull String uri, @PathParam("mimetype") String mimetype, @Context HttpServletRequest request)
            throws UnsupportedEncodingException {
        return putMeta(URLDecoder.decode(uri, "utf-8"), mimetype, request);
    }

    /**
     * Delete metadata of remote resource with given uri
     *
     * @param uri
     *            , the fully-qualified URI of the resource to create in the
     *            triple store
     * @return HTTP response (success or error)
     * @HTTP 200 resource content deleted
     * @HTTP 400 bad request (e.g, uri is null)
     * @HTTP 404 resource or resource metadata not found
     */
    @DELETE
    public Response deleteMetaRemote(@QueryParam("uri") @NotNull String uri) throws UnsupportedEncodingException {
        try {
            InterceptingRepositoryConnection connection = new InterceptingRepositoryConnectionWrapper(sesameService.getRepository(), sesameService.getConnection());
            connection.begin();
            final Resource subject = connection.getValueFactory().createURI(uri);

            try {
                connection.addRepositoryConnectionInterceptor(new ResourceSubjectMetadata(subject));

                // delete all triples for given subject
                connection.remove(subject,null,null);

                return Response.ok().build();
            } finally {
                connection.commit();
                connection.close();
            }

        } catch (RepositoryException e) {
            return ResourceWebServiceHelper.buildErrorPage(uri, configurationService.getBaseUri(), Status.INTERNAL_SERVER_ERROR, e.getMessage(), configurationService, templatingService);
        }
    }

    /**
     * Delete metadata of local resource with given uuid
     *
     * @param uuid a unique identifier (must not contain url specific
     *             characters like /,# etc.)
     * @return HTTP response (success or error)
     * @HTTP 200 resource content deleted
     * @HTTP 404 resource or resource metadata not found
     */
    @DELETE
    @Path(ResourceWebService.UUID_PATTERN)
    public Response deleteMetaLocal(@PathParam("uuid") String uuid) throws UnsupportedEncodingException {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;
        return deleteMetaRemote(uri);
    }

    private Response getMeta(String resource, String mimetype) throws UnsupportedEncodingException {
        try {
            RepositoryConnection conn = sesameService.getConnection();

            try {
                conn.begin();
                
                Resource r = null;
            	if (UriUtil.validate(resource)) {
                    r = ResourceUtils.getUriResource(conn, resource);
            	} else {
            		r = ResourceUtils.getAnonResource(conn, resource);
            	}
            	
                if (r == null || !ResourceUtils.isUsed(conn, r)) {
                	return ResourceWebServiceHelper.buildErrorPage(resource, configurationService.getBaseUri(), Response.Status.NOT_FOUND, "the requested resource could not be found in Marmotta right now, but may be available again in the future", configurationService, templatingService);
                }
            	
                // create parser
                final RDFFormat serializer = kiWiIOService.getSerializer(mimetype);
                if (serializer == null) {
                    Response response = Response.status(406).entity("mimetype can not be processed").build();
                    ResourceWebServiceHelper.addHeader(response, "Content-Type", ResourceWebServiceHelper.appendMetaTypes(kiWiIOService.getProducedTypes()));
                    return response;
                }

                final Resource subject = r;

                StreamingOutput entity = new StreamingOutput() {
                    @Override
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        // FIXME: This method is executed AFTER the @Transactional!
                        RDFWriter writer = Rio.createWriter(serializer,output);
                        try {
                            RepositoryConnection connection = sesameService.getConnection();
                            try {
                                connection.begin();
                                connection.exportStatements(subject,null,null,true,writer);
                            } finally {
                                connection.commit();
                                connection.close();
                            }
                        } catch (RepositoryException e) {
                            throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
                        } catch (RDFHandlerException e) {
                            throw new IOException("error while writing RDF data to stream", e);
                        }

                    }
                };

                // build response
                Response response = Response.ok(entity).lastModified(ResourceUtils.getLastModified(conn, r)).build();
                response.getMetadata().add("ETag", "W/\"" + ETagGenerator.getWeakETag(conn, r) + "\"");
                
                if (!mimetype.contains("html")) { // then create a proper filename
                    String[] components;
                    if (resource.contains("#")) {
                    	components = resource.split("#");	                    	
                    } else {
                    	components = resource.split("/");
                    }
                    final String fileName = components[components.length-1] + "." + serializer.getDefaultFileExtension();   
                    response.getMetadata().add("Content-Disposition", "attachment; filename=\""+fileName+"\"");
                }

                // create the Content-Type header for the response
                if (mimetype.startsWith("text/") || mimetype.startsWith("application/")) {
                    response.getMetadata().add("Content-Type", mimetype + "; charset=" + ResourceWebService.CHARSET);
                } else {
                    response.getMetadata().add("Content-Type", mimetype);
                }

                // create the Link headers for the response
                List<String> links = new LinkedList<String>();

                // build the link to the human readable content of this resource (if it exists)
                String contentLink = ResourceWebServiceHelper.buildContentLink(r, contentService.getContentType(r), configurationService);
                if(!"".equals(contentLink)) {
                    links.add(contentLink);
                }

                if (links.size() > 0) {
                    response.getMetadata().add("Link", CollectionUtils.fold(links, ", "));
                }
                return response;
        } finally {
                if (conn.isOpen()) {
                    conn.commit();
                    conn.close();
                }
            }
        } catch (RepositoryException e) {
            return ResourceWebServiceHelper.buildErrorPage(resource, configurationService.getBaseUri(), Status.INTERNAL_SERVER_ERROR, e.getMessage(), configurationService, templatingService);
        }
    }

    public Response putMeta(String uri, String mimetype, HttpServletRequest request) {
        try {
            // create parser
            RDFFormat parser = kiWiIOService.getParser(mimetype);
            if (parser == null) {
                Response response = Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity("media type " + mimetype + " not supported").build();
                ResourceWebServiceHelper.addHeader(response, "Content-Type", ResourceWebServiceHelper.appendMetaTypes(kiWiIOService.getProducedTypes()));
                return response;
            }
            if (request.getContentLength() == 0)
                return ResourceWebServiceHelper.buildErrorPage(uri, configurationService.getBaseUri(), Status.BAD_REQUEST, "content may not be empty in resource update", configurationService, templatingService);

            // a intercepting connection that filters out all triples that have
            // the wrong subject
            InterceptingRepositoryConnection connection = new InterceptingRepositoryConnectionWrapper(sesameService.getRepository(), sesameService.getConnection());
            //RepositoryConnection connection = sesameService.getConnection();
            try {
                connection.begin();
                final Resource subject = connection.getValueFactory().createURI(uri);

                connection.addRepositoryConnectionInterceptor(new ResourceSubjectMetadata(subject));

                // delete all triples for given subject
                connection.remove(subject, null, null, (Resource)null);

                // add RDF data from input to the suject
                connection.add(request.getReader(), configurationService.getBaseUri(), parser, contextService.getDefaultContext());
			} finally {
                connection.commit();
                connection.close();
            }
            return Response.ok().build();
        } catch (URISyntaxException e) {
        	return ResourceWebServiceHelper.buildErrorPage(uri, configurationService.getBaseUri(), Status.INTERNAL_SERVER_ERROR, "invalid target context", configurationService, templatingService);
        } catch (RepositoryException e) {
            return ResourceWebServiceHelper.buildErrorPage(uri, configurationService.getBaseUri(), Status.INTERNAL_SERVER_ERROR, e.getMessage(), configurationService, templatingService);
        } catch (IOException e) {
            return ResourceWebServiceHelper.buildErrorPage(uri, configurationService.getBaseUri(), Status.INTERNAL_SERVER_ERROR, "could not read request body", configurationService, templatingService);
        } catch (RDFParseException e) {
            return ResourceWebServiceHelper.buildErrorPage(uri, configurationService.getBaseUri(), Status.UNSUPPORTED_MEDIA_TYPE, "could not parse request body", configurationService, templatingService);
        }
    }

}
