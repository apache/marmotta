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
package org.apache.marmotta.platform.versioning.webservices;

import info.aduna.iteration.Iterations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.marmotta.commons.util.DateUtils;
import org.apache.marmotta.commons.util.JSONUtils;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.events.SystemStartupEvent;
import org.apache.marmotta.platform.versioning.services.VersioningSailProvider;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;

/**
 * Webservice allowing access to the versioning functionality of the LMF. Provides the following functionalities:
 * <ul>
 *     <li>list all versions that are affecting a resource</li>
 *     <li>return detailed information for a version</li>
 * </ul>
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
@Path("/")
public class VersioningWebService {

    @Inject
    private Logger log;

    @Inject
    private VersioningSailProvider versioningService;

    @Inject
    private SesameService sesameService;


    @Inject
    private ConfigurationService configurationService;

    public void startup(@Observes SystemStartupEvent event) {
        if(configurationService.getBooleanConfiguration("versioning.memento",true)) {
            log.info("Versioning Service: enabling Memento support");
            configurationService.setRuntimeFlag("create_memento_links",true);
        }
    }


    /**
     * Return a list of versions that affect the resource whose uri is passed as argument. For each version,
     * the result will contain the id, the creator, and the date when the version was recorded. Further details
     * for a version can be requested by calling the /versioning/versions/{id} webservice.
     * <p/>
     * Note that resource_uri is an optional parameter. In case no resource uri is given, all versions recorded
     * by the LMF are returned, which can take a considerable amount of time.
     *
     * @HTTP 200 in case the versions were retrieved successfully
     * @HTTP 404 in case the resource passed as argument resource_uri could not be found
     *
     * @param resource_uri the URI of the resource for which to return the versions (optional, see warning above)
     * @return a JSON list of versions, each a map with the properties "id" (long), "creator" (uri), "date" (ISO 8601 String)
     */
    @GET
    @Produces("application/json")
    @Path("/versions/list")
    public Response getVersions(@QueryParam("resource") String resource_uri,
                                @QueryParam("from") String dateFrom, @QueryParam("to") String dateTo) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                if(resource_uri != null) {
                    URI resource = conn.getValueFactory().createURI(resource_uri);
                    if(resource != null && resource instanceof KiWiUriResource) {

                        if(dateFrom == null && dateTo == null) {
                            return Response.ok().entity(formatVersions(versioningService.listVersions(resource))).build();
                        } else {
                            Date dateFromD = DateUtils.parseDate(dateFrom);
                            Date dateToD   = DateUtils.parseDate(dateTo);
                            return Response.ok().entity(formatVersions(versioningService.listVersions(resource,dateFromD,dateToD))).build();
                        }
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity("resource with URI "+resource_uri+" was not found in the system").build();
                    }
                } else {
                    if(dateFrom == null && dateTo == null) {
                        return Response.ok().entity(formatVersions(versioningService.listVersions())).build();
                    } else {
                        Date dateFromD = DateUtils.parseDate(dateFrom);
                        Date dateToD   = DateUtils.parseDate(dateTo);
                        return Response.ok().entity(formatVersions(versioningService.listVersions(dateFromD,dateToD))).build();
                    }
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        } catch(SailException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

    }

    private List<Map<String,Object>> formatVersions(RepositoryResult<Version> versions) throws RepositoryException {
        return formatVersions(Iterations.asList(versions));
    }

    private List<Map<String,Object>> formatVersions(List<Version> versions) {
        List<Map<String,Object>> result = new ArrayList<Map<String, Object>>(versions.size());

        for(Version version : versions) {
            Map<String,Object> v_map = new HashMap<String, Object>();
            v_map.put("id",version.getId());
            if(version.getCreator() != null) {
                v_map.put("creator",version.getCreator().stringValue());
            }
            v_map.put("date", DateUtils.ISO8601FORMAT.format(version.getCommitTime()));

            result.add(v_map);
        }

        return result;
    }


    /**
     * Return detailed information about the version whose id is passed as path argument. Returns a JSON map
     * with the fields id, creator, date, added_triples, removed_triples. Triple will be represented in RDF/JSON format.
     *
     * @HTTP 404 in case the requested version does not exist
     *
     * @param id the ID of the version to return
     * @return a JSON map representing the version information as described above
     */
    @GET
    @Produces("application/json")
    @Path("/versions/{id:[0-9]+}")
    public Response getVersion(@PathParam("id") Long id) {
        try {
            Version version = versioningService.getVersion(id);

            if(version != null) {
                Map<String,Object> result = new HashMap<String, Object>();
                result.put("id",version.getId());
                if(version.getCreator() != null) {
                    result.put("creator", version.getCreator().stringValue());
                }
                result.put("date",    DateUtils.ISO8601FORMAT.format(version.getCommitTime()));

                result.put("added_triples", JSONUtils.serializeTriplesAsJson(version.getAddedTriples()));
                result.put("removed_triples", JSONUtils.serializeTriplesAsJson(version.getRemovedTriples()));

                return Response.ok().entity(result).build();

            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("version with id "+id+" does not exist").build();
            }

        } catch (SailException e) {
            return Response.serverError().entity("error loading version "+id+": "+e.getMessage()).build();
        }
    }

    /**
     * Remove the version with the given ID from the triple store. Calling this service will only remove the versioning
     * metadata, not the triples themselves. Triples that are marked as deleted and no longer attached to a version will
     * be cleaned up by a garbage collection process that is running periodically or can be triggered manually.
     *
     * @param id
     * @return
     */
    @DELETE
    @Path("/versions/{id:[0-9]+}")
    public Response deleteVersion(@PathParam("id") Long id) {
        try {
            Version version = versioningService.getVersion(id);

            if(version != null) {
                versioningService.removeVersion(id);

                return Response.ok().entity("success").build();

            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("version with id "+id+" does not exist").build();
            }

        } catch (SailException e) {
            return Response.serverError().entity("error deleting version "+id+": "+e.getMessage()).build();
        }
    }


    /**
     * Revert (undo) the version with the given ID. Calling this service will add all removed triples and remove all
     * added triples of the old version. If versioning is active, this will create a new version as well.
     *
     * @param id
     * @return
     */
    @POST
    @Path("/versions/{id:[0-9]+}")
    public Response revertVersion(@PathParam("id") Long id) {
        try {
            Version version = versioningService.getVersion(id);

            if(version != null) {
                versioningService.removeVersion(id);

                return Response.ok().entity("success").build();

            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("version with id "+id+" does not exist").build();
            }

        } catch (SailException e) {
            return Response.serverError().entity("error deleting version "+id+": "+e.getMessage()).build();
        }
    }

}
