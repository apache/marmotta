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
package org.apache.marmotta.platform.versioning.webservices;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.commons.util.DateUtils;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.io.MarmottaIOService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.versioning.api.MementoService;
import org.apache.marmotta.platform.versioning.api.VersionSerializerService;
import org.apache.marmotta.platform.versioning.exception.MementoException;
import org.apache.marmotta.platform.versioning.io.VersionSerializer;
import org.apache.marmotta.platform.versioning.model.MementoVersionSet;
import org.apache.marmotta.platform.versioning.services.VersioningSailProvider;
import org.apache.marmotta.platform.versioning.utils.MementoUtils;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.net.HttpHeaders.*;

/**
 * Webservice manages memento related services, namely:
 * <ul>
 *     <li>Memento TimeGate Service: points to permalinks representing resource versions</li>
 *     <li>Memento Resource Service: delivers versions of resources based on a permalink</li>
 *     <li>Memento TimeBundle Service: TODO implement</li>
 *     <li>Memento TimeMap Service: TODO implement</li>
 * </ul>
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
@ApplicationScoped
@Path("/" + MementoUtils.MEMENTO_WEBSERVICE)
public class MementoWebService {

    @Inject
    private Logger log;
    @Inject
    private ConfigurationService configurationService;
    @Inject
    private SesameService sesameService;
    @Inject
    private MementoService mementoService;
    @Inject
    private VersioningSailProvider versioningService;
    @Inject
    private MarmottaIOService lmfIOService;
    @Inject
    private VersionSerializerService versionSerializerService;

    /**
     * redirects to memento permalink resource
     * @param date_string the requested datetime
     * @param resource_string the requested resource string
     * @return a HTTP response
     * @HTTP 303 found resource on given location
     * @HTTP 400 some errors in request
     * @HTTP 404 resource or version cannot be found
     * @HTTP 500 any other failure
     */
    @GET
    @Path("/" + MementoUtils.MEMENTO_TIMEGATE + "/{resource:.+}")
    public Response timgateService(@PathParam("resource") String resource_string, @HeaderParam("Accept-Datetime") String date_string) {

        try {
            //check preconditions
            Preconditions.checkNotNull(resource_string,"Resource URI may not null");
            Preconditions.checkNotNull(date_string, "Accept-Datetime Header may not be null");

            final RepositoryConnection conn = sesameService.getConnection();
            try {
                Date date = DateUtils.parseDate(date_string);

                URI resource = conn.getValueFactory().createURI(resource_string);

                //get versions
                MementoVersionSet versions = mementoService.getVersionSet(resource, date);

                //build version links
                Set<String> links = versions.buildLinks(configurationService.getBaseUri());

                //add timemap link
                links.add("<" + MementoUtils.timemapURI(resource_string, configurationService.getBaseUri()) + ">;rel=timemap");

                //return permalink
                return Response
                        .status(301)
                        .location(MementoUtils.resourceURI(resource_string, versions.getCurrent().getCommitTime(), configurationService.getBaseUri()))
                        .header(VARY, "negotiate, accept-datetime, accept")
                        .header("Memento-Datetime", versions.getCurrent().getCommitTime().toString())
                        .header(LINK, Joiner.on(", ").join(links))
                        .build();

            } catch (MementoException e) {
                return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
            }  finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            return Response.serverError().entity("Versioning sail cannot be initialized").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NullPointerException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * returns a serialisation for a given memento resource uri
     * @param date_string the date of the version
     * @param resource_string the original resource uri
     * @param types_string the accepted content types
     * @return a HTTP response
     * @HTTP 200 return resource in requested format
     * @HTTP 400 some errors in request
     * @HTTP 404 resource or version cannot be found
     * @HTTP 500 any other failure
     */
    @GET
    @Path("/" + MementoUtils.MEMENTO_RESOURCE + "/{date:[^/]+}/{resource:.+}")
    public Response resourceService(@PathParam("date")String date_string,
                                    @PathParam("resource") String resource_string,
                                    @HeaderParam(ACCEPT) String types_string) {

        try {
            //check preconditions
            Preconditions.checkNotNull(resource_string,"Resource URI may not null");
            Preconditions.checkNotNull(date_string, "Date may not be null");
            Preconditions.checkNotNull(types_string, "Accept Header may not be null");

            RepositoryConnection conn = sesameService.getConnection();

            try {
                final Date date = MementoUtils.MEMENTO_DATE_FORMAT.parse(date_string);

                final URI resource = conn.getValueFactory().createURI(resource_string);

                final ContentType type = getContentType(types_string);

                //get serializer
                final RDFFormat serializer = lmfIOService.getSerializer(type.getMime());

                //create response serialisation
                StreamingOutput entity = new StreamingOutput() {
                    @Override
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        RDFWriter writer = Rio.createWriter(serializer, output);
                        try {
                            RepositoryConnection con = versioningService.getSnapshot(date);
                            URI subject = con.getValueFactory().createURI(resource.stringValue());
                            try {
                                con.exportStatements(subject,null,null,true,writer);
                            } catch (RepositoryException e) {
                                throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
                            } catch (RDFHandlerException e) {
                                throw new IOException("error while writing RDF data to stream");
                            } finally {
                                con.commit();
                                con.close();
                            }
                        } catch (RepositoryException e) {
                            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
                        }
                    }
                };

                //get versions
                MementoVersionSet versions = mementoService.getVersionSet(resource, date);

                //build version links
                Set<String> links = versions.buildLinks(configurationService.getBaseUri());

                //add timegate link
                links.add("<" + MementoUtils.timegateURI(resource_string, configurationService.getBaseUri()) + ">;rel=timegate");

                //add timemap link
                links.add("<" + MementoUtils.timemapURI(resource_string, configurationService.getBaseUri()) + ">;rel=timemap");

                //create response
                return Response
                        .ok()
                        .header(LINK, Joiner.on(", ").join(links))
                        .header(CONTENT_TYPE, type.toString())
                        .header("Memento-Datetime", MementoUtils.MEMENTO_DATE_FORMAT.format(versions.getCurrent().getCommitTime()))
                        .entity(entity)
                        .build();

            } catch (ParseException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Cannot parse date").build();
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            } catch (MementoException e) {
                return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }  catch (RepositoryException e) {
            return Response.serverError().entity("Versioning sail cannot be initialized").build();
        } catch (NullPointerException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/" + MementoUtils.MEMENTO_TIMEMAP + "/{resource:.+}")
    public Response timemapService(@PathParam("resource") String resource_string,
                                   @HeaderParam(ACCEPT) String types_string) {

        try {
            //check preconditions
            Preconditions.checkNotNull(resource_string,"Resource URI may not null");
            Preconditions.checkNotNull(types_string, "Accept Header may not be null");

            RepositoryConnection conn = sesameService.getConnection();

            try {

                final URI resource = conn.getValueFactory().createURI(resource_string);

                List<ContentType> types = MarmottaHttpUtils.parseAcceptHeader(types_string);

                //get versions
                final RepositoryResult<Version> versions = versioningService.listVersions(resource);

                //get serializer
                final VersionSerializer serializer = versionSerializerService.getSerializer(types);

                //create response serialisation
                StreamingOutput entity = new StreamingOutput() {
                    @Override
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        serializer.write(resource,versions,output);
                    }
                };

                //create Header Links
                Set<String> links = new HashSet<String>();
                links.add("<" + MementoUtils.timegateURI(resource_string, configurationService.getBaseUri()) + ">;rel=timegate");

                links.add("<" + resource_string + ">;rel=original");

                //create response
                return Response
                        .ok()
                        .header(LINK, Joiner.on(", ").join(links))
                        .header(CONTENT_TYPE, serializer.getContentType().toString())
                        .entity(entity)
                        .build();

            } catch (SailException e) {
                return Response.status(Response.Status.NOT_FOUND).entity("Cannot list versions").build();
            } catch (IOException e) {
                return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity("cannot produce content type").build();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            return Response.serverError().entity("Versioning sail cannot be initialized").build();
        } catch (NullPointerException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * returns the best fitting content type for accept type header string
     * @param types a string of types
     * @return a content type that is supported by the running system
     * @throws IllegalArgumentException if no type is supported
     */
    private ContentType getContentType(String types) throws IllegalArgumentException {
        List<ContentType> acceptedTypes = MarmottaHttpUtils.parseAcceptHeader(types);
        List<ContentType> offeredTypes  = MarmottaHttpUtils.parseStringList(lmfIOService.getProducedTypes());
        ContentType type = MarmottaHttpUtils.bestContentType(offeredTypes,acceptedTypes);
        if(type == null) throw new IllegalArgumentException("Requested type is not supported");
        return type;
    }

}
