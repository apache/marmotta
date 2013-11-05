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

import static javax.ws.rs.core.Response.status;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.ETagGenerator;
import org.apache.marmotta.commons.http.LMFHttpUtils;
import org.apache.marmotta.commons.http.UriUtil;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.content.ContentService;
import org.apache.marmotta.platform.core.api.io.MarmottaIOService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.services.sesame.KiWiSesameUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

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
     * @param uri
     *            , the fully-qualified URI of the resource to create in the
     *            triple store
     * @return HTTP response (empty body)
     * @ResponseHeader Access-Control-Allow-Methods the HTTP methods that are allowable for the given resource
     * @ResponseHeader Access-Control-Allow-Origin the origins that are allowable for cross-site scripting on this resource
     */
    @OPTIONS
    public Response optionsResourceRemote(@QueryParam("uri") String uri, @HeaderParam("Access-Control-Request-Headers") String reqHeaders) {
        if(reqHeaders == null) {
            reqHeaders = "Accept, Content-Type";
        }

        if(uri == null)
            return Response.ok()
                    .header("Allow", "POST")
                    .header("Access-Control-Allow-Methods","POST")
                    .header("Access-Control-Allow-Headers", reqHeaders)
                    .header("Access-Control-Allow-Origin", configurationService.getStringConfiguration("kiwi.allow_origin","*"))
                    .build();
        else {
            try {
                uri = URLDecoder.decode(uri, "utf-8");

                RepositoryConnection conn = sesameService.getConnection();
                try {
                    conn.begin();
                    URI resource = ResourceUtils.getUriResource(conn,uri);
                    conn.commit();

                    if(resource != null) return Response.ok()
                            .header("Allow","PUT, GET, DELETE")
                            .header("Access-Control-Allow-Methods","PUT, GET, DELETE")
                            .header("Access-Control-Allow-Headers",reqHeaders)
                            .header("Access-Control-Allow-Origin",configurationService.getStringConfiguration("kiwi.allow_origin","*"))
                            .build();
                    else
                        return Response.ok()
                                .header("Allow", "POST")
                                .header("Access-Control-Allow-Methods","POST")
                                .header("Access-Control-Allow-Headers",reqHeaders)
                                .header("Access-Control-Allow-Origin",configurationService.getStringConfiguration("kiwi.allow_origin","*"))
                                .build();
                } finally {
                    conn.close();
                }

            } catch(UnsupportedEncodingException ex) {
                return Response.serverError().entity(ex.getMessage()).build();
            } catch (RepositoryException ex) {
                return Response.serverError().entity(ex.getMessage()).build();
            }
        }

    }

    /**
     * Return the options that are available for a resource with the given URI.
     *
     * @param uuid
     *            , a unique identifier (must not contain url specific
     *            characters like /,# etc.)
     * @return HTTP response (empty body)
     * @ResponseHeader Access-Control-Allow-Methods the HTTP methods that are allowable for the given resource
     * @ResponseHeader Access-Control-Allow-Origin the origins that are allowable for cross-site scripting on this resource
     */
    @OPTIONS
    @Path(UUID_PATTERN)
    public Response optionsResourceLocal(@PathParam("uuid") String uuid, @HeaderParam("Access-Control-Request-Headers") String reqHeaders) {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;

        if(reqHeaders == null) {
            reqHeaders = "Accept, Content-Type";
        }

        if(uuid == null)
            return Response.status(Status.NOT_FOUND).build();
        else {
            try {
                uri = URLDecoder.decode(uri, "utf-8");
                RepositoryConnection conn = sesameService.getConnection();
                try {
                    conn.begin();
                    URI resource = ResourceUtils.getUriResource(conn,uri);
                    conn.commit();


                    if(resource != null) return Response.ok()
                            .header("Allow","PUT, GET, DELETE")
                            .header("Access-Control-Allow-Methods","PUT, GET, DELETE")
                            .header("Access-Control-Allow-Headers",reqHeaders)
                            .header("Access-Control-Allow-Origin",configurationService.getStringConfiguration("kiwi.allow_origin","*"))
                            .build();
                    else
                        return Response.ok()
                                .header("Allow","POST")
                                .header("Access-Control-Allow-Methods","POST")
                                .header("Access-Control-Allow-Headers",reqHeaders)
                                .header("Access-Control-Allow-Origin",configurationService.getStringConfiguration("kiwi.allow_origin","*"))
                                .build();

                } finally {
                    conn.close();
                }
            } catch(UnsupportedEncodingException ex) {
                return Response.serverError().entity(ex.getMessage()).build();
            } catch (RepositoryException ex) {
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
     * @param uri
     *            , the fully-qualified URI of the resource to create in the
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
                if (ResourceUtils.getUriResource(conn,uri) != null) {
                    status = Status.OK;
                } else {
                    conn.getValueFactory().createURI(uri);
                    status = Status.CREATED;
                }
                Response response = status(status).entity(uri).build();
                response.getMetadata().add("Location", location);
                response.getMetadata().add("Vary", "Content-Type");
                return response;
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
     * @param uuid
     *            , a unique identifier (must not contain url specific
     *            characters like /,# etc.)
     * @return a link to a local resource's data or content
     * @HTTP 303 resource can be found in the requested format under Location
     * @HTTP 404 resource cannot be found
     * @HTTP 406 resource cannot be found in the given format
     * @HTTP 500 Internal Error
     * @RequestHeader Accept accepted mimetypes; value must follow the pattern
     *                (.+/.+(;rel=(content|meta))?,)+
     * @ResponseHeader Location (for HTTP 303) the url of the resource in the
     *                 requested format
     * @ResponseHeader Content-Type (for HTTP 406) a list of available types
     *                 (content and meta)
     */
    @GET
    @Path(UUID_PATTERN)
    public Response getLocal(@PathParam("uuid") String uuid, @HeaderParam("Accept") String types) throws UnsupportedEncodingException {
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
     * @param uri the fully-qualified URI of the resource to create in the triple store
     * @param format forces representation format (optional, normal content negotiation performed if empty)
     * @HTTP 303 resource can be found in the requested format under Location
     * @HTTP 400 bad request (maybe uri is not defined)
     * @HTTP 404 resource cannot be found
     * @HTTP 406 resource cannot be found in the given format
     * @HTTP 500 Internal Error
     * @RequestHeader Accept accepted mimetypes; value must follow the pattern
     *                (.+/.+(;rel=(content|meta))?,)+
     * @ResponseHeader Location the url of the resource in the requested format
     * @ResponseHeader Content-Type (for HTTP 406) a list of available types
     *                 (content and meta)
     */
    @GET
    public Response getRemote(@QueryParam("uri") String uri, @QueryParam("genid") String genid, @QueryParam("format") String format, @HeaderParam("Accept") String types)
            throws UnsupportedEncodingException {
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

    private Response get(String resource, String types) throws URISyntaxException, UnsupportedEncodingException {
        try {

            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                Resource r = null;
            	if (UriUtil.validate(resource)) {
                    try {
                    	if(ResourceUtils.isSubject(conn, resource)) {  //tests if a resource is used as subject
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
	                	return Response.status(Status.NOT_FOUND).entity("blank node id "  + resource + " not found").build();
	                }
            	}
                if (r == null) return ResourceWebServiceHelper.buildErrorPage(resource, configurationService.getBaseUri(), Response.Status.NOT_FOUND, "the requested resource could not be found in LMF right now, but may be available again in the future", configurationService, templatingService);
                // FIXME String appendix = uuid == null ? "?uri=" + URLEncoder.encode(uri, "utf-8") :
                // "/" + uuid;

                List<ContentType> offeredTypes  = LMFHttpUtils.parseStringList(kiWiIOService.getProducedTypes());
                for(ContentType t : offeredTypes) {
                	t.setParameter("rel", "meta");
                }
                String contentmime = contentService.getContentType(r);
                if(contentmime != null) {
                	ContentType tContent = LMFHttpUtils.parseContentType(contentmime);
                	tContent.setParameter("rel", "content");
                	offeredTypes.add(0,tContent);
                }
                
                if (types == null || types.equals("")) {
                	return build406(Collections.<ContentType>emptyList(), offeredTypes);
                }

                List<ContentType> acceptedTypes = LMFHttpUtils.parseAcceptHeader(types);
                ContentType bestType = LMFHttpUtils.bestContentType(offeredTypes,acceptedTypes);

                log.debug("identified best type: {}",bestType);

                if(bestType != null) {
                    Response response = buildGetResponse(r, bestType);
                    response.getMetadata().add("Last-Modified", KiWiSesameUtil.lastModified(r, conn));
                    response.getMetadata().add("ETag", "W/\"" + ETagGenerator.getWeakETag(conn, r) + "\"");
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
		response.header("Content-Type", "text/plain; charset=UTF-8");

		StringBuilder entity = new StringBuilder();
		entity.append("Could not find matching type for "+acceptedTypes+"\n");
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
     * @param uuid
     *            , a unique identifier (must not contain url specific
     *            characters like /,# etc.)
     * @return a link where the data can be put (depends on Content-Type)
     * @HTTP 303 resource in given format can be put under Location
     * @HTTP 404 resource cannot be found
     * @HTTP 415 Content-Type is not supported
     * @HTTP 500 Internal Error
     * @RequestHeader Content-Type type of the body; value must follow the
     *                pattern .+/.+(;rel=(content|meta))?
     * @ResponseHeader Location (for HTTP 303) the url where data can be put
     */
    @PUT
    @Path(UUID_PATTERN)
    public Response putLocal(@PathParam("uuid") String uuid, @HeaderParam("Content-Type") String type, @Context HttpServletRequest request) throws UnsupportedEncodingException {
        String uri = configurationService.getBaseUri() + "resource/" + uuid;
        try {
            return put(uri, type, uuid,request);
        } catch (URISyntaxException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    // **************** PUT REMOTE (eq. generic) ***********************

    /**
     * Returns a Link where the given data (metadata or content) can be put to
     * the remote resource
     *
     * @param uri
     *            , the fully-qualified URI of the resource to create in the
     *            triple store
     * @return a link where the data can be put (depends on Content-Type)
     * @HTTP 303 resource in given format can be put under Location
     * @HTTP 400 bad request (e.g. uri is null)
     * @HTTP 404 resource cannot be found
     * @HTTP 415 Content-Type is not supported
     * @HTTP 500 Internal Error
     * @RequestHeader Content-Type type of the body; value must follow the
     *                pattern .+/.+(;rel=(content|meta))?
     * @ResponseHeader Location (for HTTP 303) the url where data can be put
     */
    @PUT
    public Response putRemote(@QueryParam("uri") String uri, @HeaderParam("Content-Type") String type, @Context HttpServletRequest request) throws UnsupportedEncodingException {
        try {
            if (uri != null) return put(URLDecoder.decode(uri, "utf-8"), type, null, request);
            else
                return Response.status(400).entity("uri may not be null").build();
        } catch (URISyntaxException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private Response put(String uri, String mimetype, String uuid, HttpServletRequest request) throws URISyntaxException, UnsupportedEncodingException {
        try {
            // FIXME String appendix = uuid == null ? "?uri=" + URLEncoder.encode(uri, "utf-8") :
            // "/" + uuid;
            if (mimetype == null) return Response.status(400).entity("content-type may not be null").build();


            // the offered types are those sent by the client in the Content-Type header; if the rel attribute is not
            // given, we add the default rel value
            List<ContentType> types = LMFHttpUtils.parseAcceptHeader(mimetype);
            for(ContentType type : types) {
                if(type.getParameter("rel") == null) {
                    type.setParameter("rel",configurationService.getStringConfiguration("linkeddata.mime.rel.default", "meta"));
                }
            }

            // the acceptable types are all types for content and the meta types we have parsers for; we do not care so
            // much about the order ...
            List<ContentType> acceptable = LMFHttpUtils.parseStringList(kiWiIOService.getProducedTypes());
            for(ContentType a : acceptable) {
                a.setParameter("rel", "meta");
            }
            ContentType allContent = new ContentType("*","*");
            allContent.setParameter("rel", "content");
            acceptable.add(0,allContent);

            // determine the best match between the offered types and the acceptable types
            ContentType bestType = LMFHttpUtils.bestContentType(types,acceptable);

            if (bestType != null) {
                if (configurationService.getBooleanConfiguration("linkeddata.redirect.put", true)) {
                    final RepositoryConnection con = sesameService.getConnection();
                    try {
                        con.begin();
                        URI resource = ResourceUtils.getUriResource(con, uri);
                        con.commit();
                        return Response
                                .status(configurationService.getIntConfiguration("linkeddata.redirect.status", 303))
                                // .location(new URI(configurationService.getBaseUri() +
                                // bestType.getParameter("rel") + "/" + bestType.getMime() + appendix))
                                .location(new java.net.URI(ResourceWebServiceHelper.buildResourceLink(resource, bestType, configurationService)))
                                .build();
                    } finally {
                        con.close();
                    }
                } else {
                    if("content".equalsIgnoreCase(bestType.getParameter("rel")))
                        return contentWebService.putContent(uri, bestType.getMime(), request);
                    else if ("meta".equalsIgnoreCase(bestType.getParameter("rel")))
                        return metaWebService.putMeta(uri, bestType.getMime(), request);
                    else
                        return Response.serverError().entity("request did not specify whether it uploads content or metadata; use rel=content|meta attribute in Content-Type header").build();
                }
            } else {
                Response response = Response.status(415).entity("type " + mimetype + " not supported").build();
                ResourceWebServiceHelper.addHeader(response, "Content-Type", ResourceWebServiceHelper.appendMetaTypes(kiWiIOService.getAcceptTypes()));
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
     * @param uri
     *            , the fully-qualified URI of the resource to create in the
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
                    Resource resource = ResourceUtils.getUriResource(conn,URLDecoder.decode(uri, "utf-8"));
                    if (resource != null) {
                        ResourceUtils.removeResource(conn,resource);
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
     * @param uuid
     *            , a unique identifier (must not contain url specific
     *            characters like /,# etc.)
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
                            "linkeddata.redirect.status", 303))
                            .header("Vary", "Accept")
                            .header("Content-Type", type.toString())
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
