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
package org.apache.marmotta.platform.core.webservices.io;

import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.commons.util.DateUtils;
import org.apache.marmotta.platform.core.api.exporter.ExportService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.io.UnsupportedExporterException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.google.common.net.HttpHeaders.*;
import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;

/**
 * A web service for exporting data from the Apache Marmotta triple store
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
@Path("/export")
public class ExportWebService {

    @Inject
    private Logger log;

    @Inject
    private ExportService exportService;

    @Inject
    private SesameService sesameService;

    /**
     * Return a set of all mime types that are acceptable by the importer.
     * @return a set of all mime types that are acceptable by the importer.
     */
    @GET
    @Path("/types")
    @Produces("application/json")
    public List<String> getTypes() {
        ArrayList<String> result = new ArrayList<String>(exportService.getProducedTypes());
        Collections.sort(result);

        return result;
    }

    /**
     * Download the triple data contained in the (optional) context (named graph) in the format specified by the Accept
     * header of the request. If the context parameter is not given, all triples contained in this Apache Marmotta installation will
     * be written to the response.
     *
     * @param types          list of MIME types the client accepts
     * @param context_string URI of the named graph to export; if null, all named graphs will be exported
     * @param qFormat        MIME type for return format, overrides accept header
     * @return the HTTP response
     * @throws IOException   in case writing to the output stream of the connection fails
     *
     * @HTTP 200 in case the triples were written to the output stream correctly
     * @HTTP 404 in case the context passed as argument could not be found
     * @HTTP 406 in case the Apache Marmotta could not find any matching serializer for the MIME types in the Accept header
     */
    @GET
    @Path("/download")
    public Response downloadData(@HeaderParam(ACCEPT) String types, @QueryParam("format") String qFormat, @QueryParam("context") String context_string) throws IOException {
        List<ContentType> acceptedTypes;
        if(qFormat != null) {
            acceptedTypes = MarmottaHttpUtils.parseAcceptHeader(qFormat);
        } else {
            acceptedTypes = MarmottaHttpUtils.parseAcceptHeader(types);
        }
        List<ContentType> offeredTypes  = MarmottaHttpUtils.parseStringList(exportService.getProducedTypes());

        final ContentType bestType = MarmottaHttpUtils.bestContentType(offeredTypes,acceptedTypes);

        // create a file name for the export, preferrably with a good extension ...
        String fileName;
        if(context_string != null) {
            String[] components = context_string.split("/");
            fileName = components[components.length-1] + "-export-" + DateUtils.FILENAME_FORMAT.format(new Date());
        } else {
            fileName = "lmf-export-" + DateUtils.FILENAME_FORMAT.format(new Date());
        }

        if(bestType != null) {
            RDFFormat format = Rio.getWriterFormatForMIMEType(bestType.getMime());
            if(format != null) {
                fileName += "." + format.getDefaultFileExtension();
            }

            URI context = null;
            if(context_string != null) {
                try {
                    RepositoryConnection conn = sesameService.getConnection();
                    try {
                        conn.begin();
                        context = conn.getValueFactory().createURI(context_string);
                    } finally {
                        conn.commit();
                        conn.close();
                    }
                } catch (RepositoryException e) {
                    handleRepositoryException(e,ExportWebService.class);
                }

                if(context == null) return Response.status(Response.Status.NOT_FOUND).entity("the context given as argument could not be found").build();

            } else {
                context = null;
            }
            final URI fcontext = context;

            StreamingOutput entity = new StreamingOutput() {
                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    try {
                        //FIXME: html should not be exported, but rendered?
                        exportService.exportData(output,fcontext,bestType.getMime());
                    } catch (UnsupportedExporterException e) {
                        throw new WebApplicationException(e, Response.Status.NOT_ACCEPTABLE);
                    }
                }
            };

            return Response
                    .status(Response.Status.OK)
                    .header(CONTENT_TYPE, bestType.getMime())
                    .header(CONTENT_DISPOSITION, "attachment; filename=\""+fileName+"\"")
                    .entity(entity)
                    .build();

        } else
            return Response.status(406)
                    .header(CONTENT_TYPE, exportService.getProducedTypes())
                    .entity("could not find matching type for " + acceptedTypes + "; see Content-Type header for possible types")
                    .build();

    }
}
