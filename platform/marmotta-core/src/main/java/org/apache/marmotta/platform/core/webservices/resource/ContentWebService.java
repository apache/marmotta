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

import com.google.common.collect.ImmutableMap;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.content.ContentService;
import org.apache.marmotta.platform.core.api.io.MarmottaIOService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.events.ContentCreatedEvent;
import org.apache.marmotta.platform.core.exception.HttpErrorException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.exception.WritingNotSupportedException;
import org.apache.marmotta.platform.core.qualifiers.event.ContentCreated;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.net.HttpHeaders.*;
import static org.apache.marmotta.platform.core.webservices.resource.ResourceWebServiceHelper.appendContentTypes;

/**
 * Content Web Services
 *
 * @author Thomas Kurz
 * @author Sergio Fernández
 */
@ApplicationScoped
@Path("/" + ConfigurationService.CONTENT_PATH)
public class ContentWebService {

    private static final String ENHANCER_STANBOL_ENHANCER_ENABLED = "enhancer.stanbol.enhancer.enabled";

    @Inject
    private ConfigurationService configurationService;
    
    @Inject
    private TemplatingService templatingService;

    @Inject
    private ContentService contentService;

    @Inject @ContentCreated
    private Event<ContentCreatedEvent> afterContentCreated;

    @Inject
    private MarmottaIOService kiWiIOService;

    @Inject
    private SesameService sesameService;

    /**
     * Returns local resource content with the given uuid and an accepted return
     * type (mimetype)
     *
     * @param uuid
     *            , a unique identifier (must not contain url specific
     *            characters like /,# etc.)
     * @param mimetype
     *            , accepted mimetype follows the pattern .+/.+
     * @return a local resource's content (body is the resource content in
     *         requested format)
     * @HTTP 200 resource content found and returned
     * @HTTP 404 resource cannot be found
     * @HTTP 406 resource cannot be found in the given format
     * @HTTP 500 Internal Error
     * @ResponseHeader Content-Type (for HTTP 406) available content type (if
     *                 resource has content)
     */
    @GET
    @Path(ResourceWebService.MIME_PATTERN + ResourceWebService.UUID_PATTERN)
    public Response getContentLocal(@PathParam("uuid") String uuid, @PathParam("mimetype") String mimetype, @HeaderParam("Range") String range) throws UnsupportedEncodingException, HttpErrorException {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;
        return getContent(uri, mimetype, range);
    }

    /**
     * Returns remote resource content with the given uri and an accepted return
     * type (mimetype)
     *
     * @param uri
     *            , the fully-qualified URI of the resource to create in the
     *            triple store
     * @param mimetype
     *            , accepted mimetype follows the pattern .+/.+
     * @return a remote resource's content (body is the resource content in
     *         requested format)
     * @HTTP 200 resource content found and returned
     * @HTTP 400 bad request (maybe uri is not defined)
     * @HTTP 404 resource cannot be found
     * @HTTP 406 resource cannot be found in the given format
     * @HTTP 500 Internal Error
     * @ResponseHeader Content-Type (for HTTP 406) available content type (if
     *                 resource has content)
     */
    @GET
    @Path(ResourceWebService.MIME_PATTERN)
    public Response getContentRemote(@QueryParam("uri") @NotNull String uri, @PathParam("mimetype") String mimetype, @HeaderParam("Range") String range) throws UnsupportedEncodingException, HttpErrorException {
        return getContent(URLDecoder.decode(uri, "utf-8"), mimetype, range);
    }
    
    /**
     * Creates a redirect depending of the stored mimeType for the requested uri.
     * @param uri the resource requested
     * @return a redirect
     * @throws UnsupportedEncodingException
     * 
     * @HTTP 3xx redirect to the requested content
     * @HTTP 404 if the resource has no contetn
     * @HTTP 500 Internal Error
     */
    @GET
    public Response getContentRemote(@QueryParam("uri") @NotNull String uri) throws UnsupportedEncodingException {
        try {
            final RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                URI resource = conn.getValueFactory().createURI(uri);
                conn.commit();
                final String mimeType = contentService.getContentType(resource);
                if (mimeType != null) {
                    return Response
                            .status(configurationService.getIntConfiguration(
                                    "linkeddata.redirect.status", 303))
                            .header(VARY, ACCEPT)
                            .header(CONTENT_TYPE, mimeType + "; rel=content")
                            .location(
                                    new java.net.URI(ResourceWebServiceHelper
                                            .buildResourceLink(resource,
                                                    "content", mimeType,
                                                    configurationService)))
                            .build();

                } else {
                    return Response.status(Status.NOT_FOUND).entity("No content for <"+resource.stringValue()+">").build();
                }
            } finally {
                conn.close();
            }
        } catch (RepositoryException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        } catch (URISyntaxException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    /**
     * Creates a redirect depending of the stored mimeType for the requested resource.
     * @param uuid the local resource requested
     * @return a redirect
     * @throws UnsupportedEncodingException
     * 
     * @HTTP 3xx redirect to the requested content
     * @HTTP 404 if the resource has no content
     * @HTTP 500 Internal Error
     */
    @GET
    @Path(ResourceWebService.UUID_PATTERN)
    public Response getContentLocal(@PathParam("uuid") String uuid) throws UnsupportedEncodingException {
        String uri = configurationService.getBaseUri() + ConfigurationService.RESOURCE_PATH + "/" + uuid;
        return getContentRemote(uri);
    }


    /**
     * Sets content to a given locale resource
     *
     * @param uuid
     *            , a unique identifier (must not contain url specific
     *            characters like /,# etc.)
     * @param mimetype
     *            content-type of the body (content) follows the pattern .+/.+
     * @return HTTP response (success or error)
     * @HTTP 200 put was successful
     * @HTTP 400 bad request (e.g. body is empty)
     * @HTTP 404 resource cannot be found
     * @HTTP 415 Content-Type is not supported
     * @HTTP 500 Internal Error
     */
    @PUT
    @Path(ResourceWebService.MIME_PATTERN + ResourceWebService.UUID_PATTERN)
    public Response putContentLocal(@PathParam("uuid") String uuid, @PathParam("mimetype") String mimetype, @Context HttpServletRequest request) throws HttpErrorException {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;
        return putContent(uri, mimetype, request);
    }


    /**
     * Sets content to a given remote resource
     *
     * @param uri
     *            , the fully-qualified URI of the resource to create in the
     *            triple store
     * @param mimetype
     *            content-type of the body (content) follows the pattern .+/.+
     * @return HTTP response (success or error)
     * @HTTP 200 put was successful
     * @HTTP 400 bad request (e.g. uri is null)
     * @HTTP 404 resource cannot be found
     * @HTTP 415 Content-Type is not supported
     * @HTTP 500 Internal Error
     */
    @PUT
    @Path(ResourceWebService.MIME_PATTERN)
    public Response putContentRemote(@QueryParam("uri") @NotNull String uri, @PathParam("mimetype") String mimetype, @Context HttpServletRequest request)
            throws UnsupportedEncodingException, HttpErrorException {
        return putContent(URLDecoder.decode(uri, "utf-8"), mimetype, request);
    }

    /**
     * Delete content of remote resource with given uri
     *
     * @param uri
     *            , the fully-qualified URI of the resource to create in the
     *            triple store
     * @return HTTP response (success or error)
     * @HTTP 200 resource content deleted
     * @HTTP 400 bad request (e.g, uri is null)
     * @HTTP 404 resource or resource content not found
     */
    @DELETE
    public Response deleteContentRemote(@QueryParam("uri") @NotNull String uri) throws UnsupportedEncodingException, HttpErrorException {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                Resource resource = conn.getValueFactory().createURI(uri);
                if (resource != null) {
                    if (contentService.deleteContent(resource)) {
                        return Response.ok().build();
                    } else {
                        throw new HttpErrorException(Response.Status.NOT_FOUND, uri, "no content found for this resource in Marmotta right now, but may be available again in the future");
                    }
                }
                return Response.status(Response.Status.NOT_FOUND).build();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (MarmottaException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        } catch (RepositoryException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    /**
     * Delete content of local resource with given uuid
     *
     * @param uuid
     *            , a unique identifier (must not contain url specific
     *            characters like /,# etc.)
     * @return HTTP response (success or error)
     * @HTTP 200 resource deleted
     * @HTTP 404 resource or resource content not found
     */
    @DELETE
    @Path(ResourceWebService.UUID_PATTERN)
    public Response deleteContentLocal(@PathParam("uuid") String uuid) throws UnsupportedEncodingException, HttpErrorException {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;
        return deleteContentRemote(uri);
    }

    private Response getContent(String uri, String mimetype, String range) throws UnsupportedEncodingException, HttpErrorException {
        try {
            // FIXME String appendix = uuid == null ? "?uri=" + URLEncoder.encode(uri, "utf-8") :
            // "/" + uuid;
            final RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                URI resource = conn.getValueFactory().createURI(uri);
                conn.commit();
                if (mimetype == null) {
                    mimetype = contentService.getContentType(resource);
                }
                if (contentService.hasContent(resource, mimetype)) {

                    InputStream is = contentService.getContentStream(resource, mimetype);
                    long length = contentService.getContentLength(resource,mimetype);

                    // build response
                    Response response = null;
                    long fromL = 0;
                    if(range != null) {
                        response = Response.status(206).entity(is).build();
                        Pattern p = Pattern.compile("bytes=([0-9]+)-([0-9]+)?");
                        Matcher m = p.matcher(range);
                        if(m.matches()) {
                            String from = m.group(1);
                            try {
                                fromL = Long.parseLong(from);
                                is.skip(fromL);
                                response.getMetadata().add(CONTENT_RANGE,"bytes "+fromL+"-"+(length-1)+"/"+length);
                            } catch(NumberFormatException ex) {
                                response.getMetadata().add(CONTENT_RANGE,"bytes 0-"+(length-1)+"/"+length);
                            }
                        } else {
                            response.getMetadata().add(CONTENT_RANGE,"bytes 0-"+(length-1)+"/"+length);
                        }
                        response.getMetadata().add(ACCEPT_RANGES,"bytes");
                    } else {
                        response = Response.ok(is).build();
                        response.getMetadata().add(ACCEPT_RANGES,"bytes");
                    }

                    if(mimetype.startsWith("text") || mimetype.startsWith("application/json")) {
                        // Content-Encoding is not what it seems, known values are: gzip, compress,
                        // deflate, identity
                        // response.getMetadata().add("Content-Encoding", "utf-8");
                        response.getMetadata().add(CONTENT_TYPE, mimetype + "; charset=utf-8");
                    } else {
                        response.getMetadata().add(CONTENT_TYPE, mimetype);
                    }
                    if(length > 0) {
                        response.getMetadata().add(CONTENT_LENGTH,length-fromL);
                    }

                    // append data links
                    String s = ResourceWebServiceHelper.buildMetaLinks(resource, kiWiIOService.getProducedTypes(), configurationService);
                    if (s != null) {
                        response.getMetadata().add(LINK, s);
                    }
                    return response;
                } else {
                    ImmutableMap<String, String> headers = ImmutableMap.of(CONTENT_TYPE, appendContentTypes(contentService.getContentType(resource)));
                    throw new HttpErrorException(Status.NOT_ACCEPTABLE, resource.stringValue(), "no content for mimetype " + mimetype, headers);
                }
            } finally {
                conn.close();
            }
        } catch (IOException e) {
            return Response.serverError().entity(e.getMessage()).build();
        } catch (RepositoryException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    public Response putContent(String uri, String mimetype, HttpServletRequest request) throws HttpErrorException {
        try {
            final RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                URI resource = conn.getValueFactory().createURI(uri);
                conn.commit();
                return putContent(resource, mimetype, request);
            } finally {
                conn.close();
            }
        } catch (RepositoryException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    public Response putContent(URI resource, String mimetype, HttpServletRequest request) throws HttpErrorException {
        try {
            if (request.getContentLength() == 0) {
                throw new HttpErrorException(Status.BAD_REQUEST, resource.stringValue(), "content may not be empty for writing");
            }
            contentService.setContentStream(resource, request.getInputStream(), mimetype); // store content
            if(configurationService.getBooleanConfiguration(ENHANCER_STANBOL_ENHANCER_ENABLED, false)) {
                afterContentCreated.fire(new ContentCreatedEvent(resource)); //enhancer
            }
            return Response.ok().build();
        } catch (IOException e) {
            throw new HttpErrorException(Status.BAD_REQUEST, resource.stringValue(), "could not read request body");
        } catch (WritingNotSupportedException e) {
            throw new HttpErrorException(Status.FORBIDDEN, resource.stringValue(), "writting this content is not supported");
        }
    }

}
