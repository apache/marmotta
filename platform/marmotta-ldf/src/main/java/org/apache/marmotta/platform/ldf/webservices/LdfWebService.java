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
package org.apache.marmotta.platform.ldf.webservices;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.exporter.ExportService;
import org.apache.marmotta.platform.ldf.api.LdfService;
import org.openrdf.model.Model;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
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
    public Response getFragment(@QueryParam("subject") @DefaultValue("") final String subject,
                                @QueryParam("predicate") @DefaultValue("") final String predicate,
                                @QueryParam("object") @DefaultValue("") final String object,
                                @QueryParam("page") @DefaultValue("1") final String page,
                                @HeaderParam("Accept") final String accept,
                                @Context final UriInfo uriInfo) {
        return getFragment(subject, predicate, object, null, Integer.parseInt(page), accept, uriInfo);
    }

    @GET
    @Path(UUID_PATTERN)
    public Response getFragment(@QueryParam("subject") @DefaultValue("") final String subject,
                                @QueryParam("predicate") @DefaultValue("") final String predicate,
                                @QueryParam("object") @DefaultValue("") final String object,
                                @QueryParam("page") @DefaultValue("1") final String page,
                                @PathParam("uuid") final String uuid,
                                @HeaderParam("Accept") final String accept,
                                @Context final UriInfo uriInfo) {
        final String context = buildContextUri(uuid);
        return getFragment(subject, predicate, object, context, Integer.parseInt(page), accept, uriInfo);
    }

    private Response getFragment(final String subject,
                                 final String predicate,
                                 final String object,
                                 final String context,
                                 final int page,
                                 final String accept,
                                 final UriInfo uriInfo) {

        final Model fragment;
        try {
            fragment = ldfService.getFragment(subject, predicate, object, context, page, uriInfo.getRequestUri());
        } catch (RepositoryException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

        final RDFFormat format = getFormat(accept);
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try {
                    Rio.write(fragment, outputStream, format);
                } catch (RDFHandlerException e) {
                    throw new WebApplicationException(e);
                }
            }
        };
        return Response.ok(stream).build();
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
            return configurationService.getBaseUri() + ConfigurationService.CONTEXT_PATH + "/" + uuid;
        } else {
            return null;
        }
    }

}
