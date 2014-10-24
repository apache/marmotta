package org.apache.marmotta.platform.ldf.webservices;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.exporter.ExportService;
import org.apache.marmotta.platform.core.util.WebServiceUtil;
import org.apache.marmotta.platform.ldf.api.LdfService;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Linked Data Fragments web service implementation
 *
 * @author Sergio Fernández
 */
@ApplicationScoped
@Path(LdfWebService.PATH)
public class LdfWebService {

    public static final String PATH = "/fragments";

    @Inject
    private LdfService ldfService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ExportService exportService;

    private static final String UUID_PATTERN = "{uuid:[^#?]+}";

    @GET
    public Response getFragment(@QueryParam("subject") @DefaultValue("") String subject,
                                @QueryParam("predicate") @DefaultValue("") String predicate,
                                @QueryParam("object") @DefaultValue("") String object,
                                @QueryParam("page") @DefaultValue("1") String page,
                                @HeaderParam("Accept") String accept) {
        System.out.println("1");
        return getFragment(subject, predicate, object, null, Integer.parseInt(page), accept);
    }

    @GET
    @Path(UUID_PATTERN)
    public Response getFragment(@QueryParam("subject") @DefaultValue("") String subject,
                                @QueryParam("predicate") @DefaultValue("") String predicate,
                                @QueryParam("object") @DefaultValue("") String object,
                                @QueryParam("page") @DefaultValue("1") String page,
                                @PathParam("uuid") String uuid,
                                @HeaderParam("Accept") String accept) {
        System.out.println("2");
        final String context = buildContextUri(uuid);
        return getFragment(subject, predicate, object, context, Integer.parseInt(page), accept);
    }

    private Response getFragment(final String subject,
                                 final String predicate,
                                 final String object,
                                 final String context,
                                 final int page,
                                 final String accept) {
        System.out.println("3");
        final RDFFormat format = getFormat(accept);

        try {
            StreamingOutput stream = new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                    try {
                        ldfService.writeFragment(subject, predicate, object, context, page, format, outputStream);
                    } catch (RepositoryException e) {
                        throw new WebApplicationException(e);
                    }
                }
            };
            return Response.ok(stream).build();
        } catch (WebApplicationException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    private RDFFormat getFormat(String accept) {
        List<ContentType> acceptedTypes = MarmottaHttpUtils.parseAcceptHeader(accept);
        List<ContentType> offeredTypes  = MarmottaHttpUtils.parseStringList(exportService.getProducedTypes());
        offeredTypes.removeAll(Collections.unmodifiableList(Arrays.asList(new ContentType("text", "html"), new ContentType("application", "xhtml+xml"))));
        final ContentType bestType = MarmottaHttpUtils.bestContentType(offeredTypes, acceptedTypes);
        return Rio.getWriterFormatForMIMEType(bestType.getMime());
    }

    private String buildContextUri(String uuid) {
        if (StringUtils.isNotBlank(uuid)) {
            String root = configurationService.getBaseUri();
            return root.substring(0, root.length() - 1) + WebServiceUtil.getResourcePath(this) + "/" + uuid;
        } else {
            return null;
        }
    }

}
