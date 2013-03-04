package kiwi.core.webservices.resource2;

import com.google.common.base.Preconditions;
import kiwi.core.api.config.ConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.LMFHttpUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
@ApplicationScoped
@Path("/")
public class ResourceWebService {

    public static final String RESOURCE = "resource";
    public static final String META = "meta";
    public static final String CONTENT = "content";

    public static final String PATTERN_UUID = "/{uuid:.+}";
    public static final String PATTERN_MIMETYPE = "/{mimetype:[^/]+/[^/]+}";

    @Inject
    private ConfigurationService configurationService;

    @Path( RESOURCE + PATTERN_UUID )
    @POST
    public Response createResourceWithUUID(@PathParam("uuid")String uuid) {
        String uri =configurationService.getBaseUri() + RESOURCE + uuid;
        return createResourceWithURI(uri);
    }

    @Path( RESOURCE )
    @POST
    public Response createResourceWithURI(@QueryParam("uri")String uri) {
        //try to create catch = 500, created = 201, already exists = 200
        //return location +  vary = Content-Type
        return null;
    }

    @Path( RESOURCE + PATTERN_UUID )
    @GET
    public Response getResourceWithUUID(@PathParam("uuid")String uuid, @HeaderParam("Accept") String accept) {

        try {

            Preconditions.checkState(StringUtils.isNotBlank(uuid),"uuid may not be empty");

            List<ContentType> types = LMFHttpUtils.parseAcceptHeader(accept);
            String url = configurationService.getBaseUri() + getRedirectPath(types) + "/" + uuid;

            return buildRedirect(url);

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (URISyntaxException e) {
            return Response.serverError().build();
        }

    }

    @Path ( RESOURCE )
    @GET
    public Response getResourceWithURI(@QueryParam("uri")String uri, @HeaderParam("Accept") String accept) {

        try {

            Preconditions.checkNotNull(uri,"query parameter 'uri' may not be null");

            List<ContentType> types = LMFHttpUtils.parseAcceptHeader(accept);
            String url = configurationService.getBaseUri() + getRedirectPath(types) + "?" + uri;

            return buildRedirect(url);

        } catch (NullPointerException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (URISyntaxException e) {
            return Response.serverError().build();
        }
    }

    @Path( RESOURCE + PATTERN_UUID )
    @PUT
    public Response putResourceWithUUID(@PathParam("uuid")String uuid, @HeaderParam("Content-Type") String type) {
        try {

            Preconditions.checkState(StringUtils.isNotBlank(uuid),"uuid may not be empty");

            ContentType ctype = LMFHttpUtils.parseContentType(type);
            String url = configurationService.getBaseUri() + getRedirectPath(ctype) + "/" + uuid;

            return buildRedirect(url);

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (URISyntaxException e) {
            return Response.serverError().build();
        }
    }

    @Path ( RESOURCE )
    @PUT
    public Response putResourceWithURI(@QueryParam("uri")String uri, @HeaderParam("Content-Type") String type) {
        try {

            Preconditions.checkNotNull(uri,"query parameter 'uri' may not be null");

            ContentType ctype = LMFHttpUtils.parseContentType(type);
            String url = configurationService.getBaseUri() + getRedirectPath(ctype) + "?" + uri;

            return buildRedirect(url);

        } catch (NullPointerException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (URISyntaxException e) {
            return Response.serverError().build();
        }
    }

    @Path( RESOURCE + PATTERN_UUID )
    @DELETE
    public Response deleteResourceWithUUID(@PathParam("uuid")String uuid) {
        String uri =configurationService.getBaseUri() + RESOURCE + uuid;
        return deleteResourceWithURI(uri);
    }

    @Path ( RESOURCE )
    @DELETE
    public Response deleteResourceWithURI(@QueryParam("uri")String uri) {
        //delete a resource
        return null;
    }

    /**
     * returns redirect path depending on rel and mimetype of types defined in accept header
     * @param types
     * @return
     */
    private String getRedirectPath(List<ContentType> types) {
        for(ContentType type: types) {
            return getRedirectPath(type);
        }
        return META;
    }

    private String getRedirectPath(ContentType type) {
        String rel = (type.getParameter("rel") != null) ? type.getParameter("key") : META;
        return rel + "/" + type.getMime();
    }

    private Response buildRedirect(String url) throws URISyntaxException {
        return Response.status(Response.Status.SEE_OTHER)
                .location(new URI(url))
                .build();
    }

}
