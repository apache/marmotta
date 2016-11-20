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
package org.apache.marmotta.platform.core.webservices.resource;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.ETagGenerator;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.commons.http.UriUtil;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.content.ContentService;
import org.apache.marmotta.platform.core.api.io.MarmottaIOService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.HttpErrorException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.google.common.net.HttpHeaders.*;
import static javax.ws.rs.core.Response.status;
import static org.apache.marmotta.platform.core.model.config.CoreOptions.*;
import static org.apache.marmotta.platform.core.webservices.resource.ResourceWebServiceHelper.appendMetaTypes;

/**
 * Resource Web Services
 *
 * @author Thomas Kurz
 * @author Sergio Fernández
 */
@ApplicationScoped
@Path("/" + ConfigurationService.RESOURCE_PATH)
public class ResourceWebService {

    @Inject
    private Logger log;

    public static final String CHARSET = "utf-8";

    public static final String MIME_PATTERN = "/{mimetype:[^/]+/[^/]+}";

    public static final String UUID_PATTERN = "/{uuid:[^#?]+}";

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private TemplatingService templatingService;

    @Inject
    private ContentService contentService;

    @Inject
    private MarmottaIOService kiWiIOService;

    @Inject
    private SesameService sesameService;

    @Inject
    private ContentWebService contentWebService;

    @Inject
    private MetaWebService metaWebService;

    /**
     * Return the options that are available for a resource with the given URI.
     *
     * @param uri , the fully-qualified URI of the resource to create in the
     *            triple store
     * @return HTTP response (empty body)
     * @ResponseHeader Access-Control-Allow-Methods the HTTP methods that are allowable for the given resource
     * @ResponseHeader Access-Control-Allow-Origin the origins that are allowable for cross-site scripting on this resource
     */
    @OPTIONS
    public Response optionsResourceRemote(@QueryParam("uri") String uri, @HeaderParam(ACCESS_CONTROL_REQUEST_HEADERS) String reqHeaders) {
        if (reqHeaders == null) {
            reqHeaders = "Accept, Content-Type";
        }

        String methods;
        if (uri == null) {
            methods = "POST";
        } else {
            try {
                uri = URLDecoder.decode(uri, "utf-8");

                RepositoryConnection conn = sesameService.getConnection();
                try {
                    conn.begin();
                    URI resource = ResourceUtils.getUriResource(conn, uri);
                    conn.commit();

                    methods = resource != null ? "PUT, GET, DELETE" : "POST";
                } finally {
                    conn.close();
                }
            } catch (UnsupportedEncodingException | RepositoryException ex) {
                return Response.serverError().entity(ex.getMessage()).build();
            }
        }

        return Response.ok()
                .header(ALLOW, methods)
                .header(ACCESS_CONTROL_ALLOW_METHODS, methods)
                .header(ACCESS_CONTROL_ALLOW_HEADERS, reqHeaders)
                .header(ACCESS_CONTROL_ALLOW_ORIGIN, configurationService.getStringConfiguration(HTTP_ALLOW_ORIGIN, "*"))
                .build();

    }

    /**
     * Return the options that are available for a resource with the given URI.
     *
     * @param uuid , a unique identifier (must not contain url specific
     *             characters like /,# etc.)
     * @return HTTP response (empty body)
     * @ResponseHeader Access-Control-Allow-Methods the HTTP methods that are allowable for the given resource
     * @ResponseHeader Access-Control-Allow-Origin the origins that are allowable for cross-site scripting on this resource
     */
    @OPTIONS
    @Path(UUID_PATTERN)
    public Response optionsResourceLocal(@PathParam("uuid") String uuid, @HeaderParam(ACCESS_CONTROL_REQUEST_HEADERS) String reqHeaders) {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;

        if (reqHeaders == null) {
            reqHeaders = "Accept, Content-Type";
        }

        if (uuid == null)
            return Response.status(Status.NOT_FOUND).build();
        else {
            try {
                uri = URLDecoder.decode(uri, "utf-8");
                RepositoryConnection conn = sesameService.getConnection();
                try {
                    conn.begin();
                    URI resource = ResourceUtils.getUriResource(conn, uri);
                    conn.commit();

                    String methods = resource != null ? "PUT, GET, DELETE" : "POST";


                    return Response.ok()
                            .header(ALLOW, methods)
                            .header(ACCESS_CONTROL_ALLOW_METHODS, methods)
                            .header(ACCESS_CONTROL_ALLOW_HEADERS, reqHeaders)
                            .header(ACCESS_CONTROL_ALLOW_ORIGIN, configurationService.getStringConfiguration(HTTP_ALLOW_ORIGIN, "*"))
                            .build();

                } finally {
                    conn.close();
                }
            } catch (UnsupportedEncodingException | RepositoryException ex) {
                return Response.serverError().entity(ex.getMessage()).build();
            }
        }

    }

    // ******************************************* P O S T
    // ***********************************
    // **************** POST (New or Remote)

    /**
     * Creates new resource with given uri. If no uri is defined it creates a
     * local uri with random uuid
     *
     * @param uri , the fully-qualified URI of the resource to create in the
     *            triple store
     * @return HTTP response (body is a String message)
     * @HTTP 201 new resource created
     * @HTTP 302 resource already exists
     * @HTTP 500 Internal Error
     * @ResponseHeader Location the url of the new/found resource
     */
    @POST
    public Response postNewOrRemote(@QueryParam("uri") String uri) throws UnsupportedEncodingException {
        if (uri == null)
            return post(configurationService.getBaseUri() + "resource/" + UUID.randomUUID().toString(), false);
        else
            return post(uri, true);
    }

    // **************** POST (Locale)

    /**
     * Creates new local resource with given uuid.
     *
     * @param uuid a unique identifier (must not contain url specific characters like /,# etc.)
     * @return HTTP response (body is a String message)
     * @HTTP 201 new resource created
     * @HTTP 200 resource already exists
     * @HTTP 500 Internal Error
     * @ResponseHeader Location the url of the new/found resource
     */
    @POST
    @Path(UUID_PATTERN)
    public Response postLocal(@PathParam("uuid") String uuid) throws UnsupportedEncodingException {
        return post(configurationService.getBaseUri() + "resource/" + uuid, false);
    }

    // **************** POST (Generic)
    private Response post(String uri, boolean remote) throws UnsupportedEncodingException {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                String location = remote ? configurationService.getServerUri() + ConfigurationService.RESOURCE_PATH + "?uri=" + uri : uri;
                Response.Status status;
                if (ResourceUtils.getUriResource(conn, uri) != null) {
                    status = Status.OK;
                } else {
                    conn.getValueFactory().createURI(uri);
                    status = Status.CREATED;
                }
                return status(status)
                                .header(LOCATION, location)
                                .header(VARY, CONTENT_TYPE)
                                .entity(uri)
                                .build();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    /**
     * Returns a link to a local resource (data or content) with the given uuid
     * and an accepted return type
     *
     * @param uuid , a unique identifier (must not contain url specific
     *             characters like /,# etc.)
     * @return a link to a local resource's data or content
     * @HTTP 303 resource can be found in the requested format under Location
     * @HTTP 404 resource cannot be found
     * @HTTP 406 resource cannot be found in the given format
     * @HTTP 500 Internal Error
     * @RequestHeader Accept accepted mimetypes; value must follow the pattern
     * (.+/.+(;rel=(content|meta))?,)+
     * @ResponseHeader Location (for HTTP 303) the url of the resource in the
     * requested format
     * @ResponseHeader Content-Type (for HTTP 406) a list of available types
     * (content and meta)
     */
    @GET
    @Path(UUID_PATTERN)
    public Response getLocal(@PathParam("uuid") String uuid, @HeaderParam(javax.ws.rs.core.HttpHeaders.ACCEPT) String types) throws UnsupportedEncodingException, HttpErrorException {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;
        try {
            return get(uri, types);
        } catch (URISyntaxException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    // **************** GET REMOTE (eq. generic) ***********************

    /**
     * Returns a link to a remote resource (data or content) with the given uri
     * and an accepted return type
     *
     * @param uri    the fully-qualified URI of the resource to create in the triple store
     * @param format forces representation format (optional, normal content negotiation performed if empty)
     * @HTTP 303 resource can be found in the requested format under Location
     * @HTTP 400 bad request (maybe uri is not defined)
     * @HTTP 404 resource cannot be found
     * @HTTP 406 resource cannot be found in the given format
     * @HTTP 500 Internal Error
     * @RequestHeader Accept accepted mimetypes; value must follow the pattern
     * (.+/.+(;rel=(content|meta))?,)+
     * @ResponseHeader Location the url of the resource in the requested format
     * @ResponseHeader Content-Type (for HTTP 406) a list of available types
     * (content and meta)
     */
    @GET
    public Response getRemote(@QueryParam("uri") String uri, @QueryParam("genid") String genid, @QueryParam("format") String format, @HeaderParam("Accept") String types)
            throws UnsupportedEncodingException, HttpErrorException {
        try {
            if (StringUtils.isNotBlank(uri)) {
                if (format != null && StringUtils.isNotBlank(format)) {
                    types = format;
                }
                //TODO: add 'If-None-Match' support, sending a '304 Not Modified' when the ETag matches
                return get(uri, types);
            } else if (StringUtils.isNotBlank(genid)) {
                if (format != null && StringUtils.isNotBlank(format)) {
                    types = format;
                }
                //TODO: add 'If-None-Match' support, sending a '304 Not Modified' when the ETag matches
                return get(genid, types);
            } else {
                return Response.status(400).entity("resource not identified").build();
            }
        } catch (URISyntaxException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private Response get(String resource, String types) throws URISyntaxException, UnsupportedEncodingException, HttpErrorException {
        try {

            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                Resource r = null;
                if (UriUtil.validate(resource)) {
                    try {
                        if (ResourceUtils.isSubject(conn, resource)) {  //tests if a resource is used as subject
                            r = ResourceUtils.getUriResource(conn, resource);
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving the resource <{}>: {}", resource, e.getMessage());
                        log.debug("So redirecting directly to it...");
                        return Response.seeOther(new java.net.URI(resource)).build();
                    }
                } else {
                    try {
                        r = ResourceUtils.getAnonResource(conn, resource);
                    } catch (Exception e) {
                        log.error("Error retrieving the blank node <{}>: {}", resource, e.getMessage());
                        return Response.status(Status.NOT_FOUND).entity("blank node id " + resource + " not found").build();
                    }
                }
                if (r == null) {
                    throw new HttpErrorException(Status.NOT_FOUND, resource, "the requested resource could not be found in Marmotta right now, but may be available again in the future", ImmutableMap.of(ACCEPT, types));
                }
                // FIXME String appendix = uuid == null ? "?uri=" + URLEncoder.encode(uri, "utf-8") :
                // "/" + uuid;

                List<ContentType> offeredTypes = MarmottaHttpUtils.parseStringList(kiWiIOService.getProducedTypes());
                for (ContentType t : offeredTypes) {
                    t.setParameter("rel", "meta");
                }
                String contentmime = contentService.getContentType(r);
                if (contentmime != null) {
                    ContentType tContent = MarmottaHttpUtils.parseContentType(contentmime);
                    tContent.setParameter("rel", "content");
                    offeredTypes.add(0, tContent);
                }

                if (types == null || types.equals("")) {
                    return build406(Collections.<ContentType>emptyList(), offeredTypes);
                }

                List<ContentType> acceptedTypes = MarmottaHttpUtils.parseAcceptHeader(types);
                ContentType bestType = MarmottaHttpUtils.bestContentType(offeredTypes, acceptedTypes);

                log.debug("identified best type: {}", bestType);

                if (bestType != null) {
                    Response response = buildGetResponse(r, bestType);
                    response.getMetadata().add(LAST_MODIFIED, ResourceUtils.getLastModified(conn, r));
                    response.getMetadata().add(ETAG, "W/\"" + ETagGenerator.getWeakETag(conn, r) + "\"");
                    return response;
                } else {
                    return build406(acceptedTypes, offeredTypes);
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (RepositoryException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private Response build406(List<ContentType> acceptedTypes, List<ContentType> offeredTypes) {
        ResponseBuilder response = Response.status(Status.NOT_ACCEPTABLE);
        response.header(CONTENT_TYPE, "text/plain; charset=UTF-8");

        StringBuilder entity = new StringBuilder();
        entity.append("Could not find matching type for ").append(acceptedTypes).append("\n");
        entity.append("choose one of the following:");
        for (ContentType contentType : offeredTypes) {
            entity.append("  ").append(contentType).append("\n");
        }
        entity.append("\n");
        response.entity(entity.toString());
        return response.build();
    }


    // ******************************************* P U T
    // ***********************************
    // **************** PUT LOCALE ***********************

    /**
     * Returns a Link where the given data (metadata or content) can be put to
     * the local resource
     *
     * @param uuid , a unique identifier (must not contain url specific
     *             characters like /,# etc.)
     * @return a link where the data can be put (depends on Content-Type)
     * @HTTP 303 resource in given format can be put under Location
     * @HTTP 404 resource cannot be found
     * @HTTP 415 Content-Type is not supported
     * @HTTP 500 Internal Error
     * @RequestHeader Content-Type type of the body; value must follow the
     * pattern .+/.+(;rel=(content|meta))?
     * @ResponseHeader Location (for HTTP 303) the url where data can be put
     */
    @PUT
    @Path(UUID_PATTERN)
    public Response putLocal(@PathParam("uuid") String uuid, @HeaderParam(CONTENT_TYPE) String type, @Context HttpServletRequest request) throws UnsupportedEncodingException, HttpErrorException {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;
        try {
            return put(uri, type, uuid, request);
        } catch (URISyntaxException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    // **************** PUT REMOTE (eq. generic) ***********************

    /**
     * Returns a Link where the given data (metadata or content) can be put to
     * the remote resource
     *
     * @param uri , the fully-qualified URI of the resource to create in the
     *            triple store
     * @return a link where the data can be put (depends on Content-Type)
     * @HTTP 303 resource in given format can be put under Location
     * @HTTP 400 bad request (e.g. uri is null)
     * @HTTP 404 resource cannot be found
     * @HTTP 415 Content-Type is not supported
     * @HTTP 500 Internal Error
     * @RequestHeader Content-Type type of the body; value must follow the
     * pattern .+/.+(;rel=(content|meta))?
     * @ResponseHeader Location (for HTTP 303) the url where data can be put
     */
    @PUT
    public Response putRemote(@QueryParam("uri") String uri, @HeaderParam(CONTENT_TYPE) String type, @Context HttpServletRequest request) throws UnsupportedEncodingException, HttpErrorException {
        try {
            if (uri != null) return put(URLDecoder.decode(uri, "utf-8"), type, null, request);
            else
                return Response.status(400).entity("uri may not be null").build();
        } catch (URISyntaxException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private Response put(String uri, String mimetype, String uuid, HttpServletRequest request) throws URISyntaxException, UnsupportedEncodingException, HttpErrorException {
        try {
            // FIXME String appendix = uuid == null ? "?uri=" + URLEncoder.encode(uri, "utf-8") :
            // "/" + uuid;
            if (mimetype == null) return Response.status(400).entity("content-type may not be null").build();


            // the offered types are those sent by the client in the Content-Type header; if the rel attribute is not
            // given, we add the default rel value
            List<ContentType> types = MarmottaHttpUtils.parseAcceptHeader(mimetype);
            for (ContentType type : types) {
                if (type.getParameter("rel") == null) {
                    type.setParameter("rel", configurationService.getStringConfiguration(LINKEDDATA_MIME_REL_DEFAULT, "meta"));
                }
            }

            // the acceptable types are all types for content and the meta types we have parsers for; we do not care so
            // much about the order ...
            List<ContentType> acceptable = MarmottaHttpUtils.parseStringList(kiWiIOService.getProducedTypes());
            for (ContentType a : acceptable) {
                a.setParameter("rel", "meta");
            }
            ContentType allContent = new ContentType("*", "*");
            allContent.setParameter("rel", "content");
            acceptable.add(0, allContent);

            // determine the best match between the offered types and the acceptable types
            ContentType bestType = MarmottaHttpUtils.bestContentType(types, acceptable);

            if (bestType != null) {
                if (configurationService.getBooleanConfiguration(LINKEDDATA_REDIRECT_PUT, true)) {
                    final RepositoryConnection con = sesameService.getConnection();
                    try {
                        con.begin();
                        URI resource = ResourceUtils.getUriResource(con, uri);
                        con.commit();
                        return Response
                                .status(configurationService.getIntConfiguration(LINKEDDATA_REDIRECT_STATUS, 303))
                                        // .location(new URI(configurationService.getBaseUri() +
                                        // bestType.getParameter("rel") + "/" + bestType.getMime() + appendix))
                                .location(new java.net.URI(ResourceWebServiceHelper.buildResourceLink(resource, bestType, configurationService)))
                                .build();
                    } finally {
                        con.close();
                    }
                } else {
                    if ("content".equalsIgnoreCase(bestType.getParameter("rel")))
                        return contentWebService.putContent(uri, bestType.getMime(), request);
                    else if ("meta".equalsIgnoreCase(bestType.getParameter("rel")))
                        return metaWebService.putMeta(uri, bestType.getMime(), request);
                    else
                        return Response.serverError().entity("request did not specify whether it uploads content or metadata; use rel=content|meta attribute in Content-Type header").build();
                }
            } else {
                Response response = Response.status(415).entity("type " + mimetype + " not supported").build();
                ResourceWebServiceHelper.addHeader(response, CONTENT_TYPE, appendMetaTypes(kiWiIOService.getAcceptTypes()));
                return response;
            }
        } catch (RepositoryException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    // ******************************************* D E L E T E
    // ***********************************
    // **************** DELETE RESOURCE ***********************

    /**
     * Delete remote resource with given uri
     *
     * @param uri , the fully-qualified URI of the resource to create in the
     *            triple store
     * @return HTTP response (success or error)
     * @HTTP 200 resource deleted
     * @HTTP 400 bad request (e.g, uri is null)
     * @HTTP 404 resource not found
     */
    @DELETE
    public Response deleteResourceRemote(@QueryParam("uri") String uri) throws UnsupportedEncodingException {

        if (uri != null) {
            try {
                RepositoryConnection conn = sesameService.getConnection();
                try {
                    conn.begin();
                    Resource resource = ResourceUtils.getUriResource(conn, URLDecoder.decode(uri, "utf-8"));
                    if (resource != null) {
                        ResourceUtils.removeResource(conn, resource);
                        return Response.ok().build();
                    } else
                        return Response.status(Response.Status.NOT_FOUND).build();
                } finally {
                    conn.commit();
                    conn.close();
                }
            } catch (RepositoryException ex) {
                return Response.serverError().entity(ex.getMessage()).build();
            }
        } else
            return Response.status(400).entity("uri may not be null").build();
    }

    /**
     * Delete local resource with given uuid
     *
     * @param uuid , a unique identifier (must not contain url specific
     *             characters like /,# etc.)
     * @return HTTP response (success or error)
     * @HTTP 200 resource deleted
     * @HTTP 404 resource not found
     */
    @DELETE
    @Path(UUID_PATTERN)
    public Response deleteResourceLocal(@PathParam("uuid") String uuid) throws UnsupportedEncodingException {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;
        return deleteResourceRemote(uri);
    }

    private Response buildGetResponse(Resource resource, ContentType type) {
        try {

            return Response
                    .status(configurationService.getIntConfiguration(
                            LINKEDDATA_REDIRECT_STATUS, 303))
                    .header("Vary", ACCEPT)
                    .header(CONTENT_TYPE, type.toString())
                            // .location(new URI(configurationService.getBaseUri() +
                            // type.getParameter("rel") + "/" + type.getType() + "/"
                            // +type.getSubtype() +
                            // appendix))
                    .location(
                            new java.net.URI(ResourceWebServiceHelper.buildResourceLink(resource,
                                    type.getParameter("rel"), type.getMime(), configurationService)))
                    .build();

        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

}
