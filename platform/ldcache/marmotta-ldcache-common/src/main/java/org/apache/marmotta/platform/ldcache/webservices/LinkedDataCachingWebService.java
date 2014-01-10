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
package org.apache.marmotta.platform.ldcache.webservices;

import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.ldcache.api.LDCachingService;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.provider.rdf.LinkedDataProvider;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.ldcache.api.endpoint.LinkedDataEndpointService;
import org.apache.marmotta.platform.ldcache.api.ldcache.LDCacheSailProvider;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
@ApplicationScoped
@Path("/cache")
public class LinkedDataCachingWebService {

    @Inject
    private Logger log;

    @Inject
    private LinkedDataEndpointService endpointService;

    @Inject
    private SesameService sesameService;

    @Inject
    private LDCacheSailProvider cacheSailProvider;


    @GET
    @Path("/live")
    public Response retrieveLive(@QueryParam("uri") String uri) {
        if(cacheSailProvider.isEnabled()) {
            try {
                ClientResponse response = cacheSailProvider.getLDClient().retrieveResource(uri);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                RDFHandler handler = new RDFXMLPrettyWriter(out);

                ModelCommons.export(response.getData(), handler);

                return Response.ok().entity( new String(out.toByteArray(), "utf-8")).build();
            } catch (Exception e) {
                log.error("exception while retrieving resource",e);
                return Response.status(500).entity(e.getMessage()).build();
            }
        } else {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity("caching is disabled").build();
        }
    }

    @GET
    @Path("/cached")
    public Response retrieveCached(@QueryParam("uri") String uri) {
        if(cacheSailProvider.isEnabled()) {
            URI resource = sesameService.getValueFactory().createURI(uri);


            try {
                cacheSailProvider.getLDCache().refresh(resource);

                return Response.ok().build();
            } catch (Exception e) {
                log.error("exception while retrieving resource",e);
                return Response.status(500).entity(e.getMessage()).build();
            }
        } else {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity("caching is disabled").build();
        }

    }

    @GET
    @Path("/refresh")
    public Response refreshCached(@QueryParam("uri") String uri) {

        if(cacheSailProvider.isEnabled()) {
            URI resource = sesameService.getValueFactory().createURI(uri);


            try {
                cacheSailProvider.getLDCache().refresh(resource, LDCachingService.RefreshOpts.FORCE);

                return Response.ok().build();
            } catch (Exception e) {
                log.error("exception while retrieving resource",e);
                return Response.status(500).entity(e.getMessage()).build();
            }
        } else {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity("caching is disabled").build();
        }

    }

    @POST
    @Path("/expire")
    public Response expireCache(@QueryParam("uri") String uri) {

        if(cacheSailProvider.isEnabled()) {
            if (uri != null) {
                URI resource = sesameService.getValueFactory().createURI(uri);
                cacheSailProvider.getLDCache().expire(resource);
            } else {
                cacheSailProvider.getLDCache().clear();
            }

            return Response.ok().build();
        } else {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity("caching is disabled").build();
        }
    }

    @POST
    @Path("/endpoint")
    public Response registerEndpoint(@QueryParam("name") String name,
                                     @QueryParam("prefix") String prefix,
                                     @QueryParam("endpoint") String endpointUrl,
                                     @QueryParam("kind") String type,
                                     @QueryParam("mimetype") String mimetype,
                                     @QueryParam("expiry") long expiry) {

        if(cacheSailProvider.isEnabled()) {
            if (type == null || !getProviderNames().contains(type.toLowerCase())) {
                type = LinkedDataProvider.PROVIDER_NAME;
            }

            // Check for valid Regex
            if (prefix != null) {
                try {
                    if (prefix.startsWith(Endpoint.REGEX_INDICATOR)) {
                        Pattern.compile(prefix.substring(Endpoint.REGEX_INDICATOR.length()));
                    } else {
                        Pattern.compile(prefix);
                    }
                } catch (PatternSyntaxException pse) {
                    return Response.status(Status.BAD_REQUEST).entity("Invalid Regex in prefix detected").build();
                }
            }
            if (endpointUrl != null) {
                endpointUrl = endpointUrl.replace('<', '{').replace('>', '}');
            } else {
                endpointUrl = "";
            }
            if (mimetype == null) {
                mimetype ="";
            }
            Endpoint endpoint = new Endpoint(name, type, prefix, endpointUrl, mimetype, expiry);
            endpointService.addEndpoint(endpoint);

            cacheSailProvider.updateEndpoints();


            return Response.ok().build();
        } else {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity("caching is disabled").build();
        }

    }

    @GET
    @Path("/endpoint/list")
    @Produces("application/json")
    public Response listEndpoints() {

        List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();
        for(Endpoint endpoint : endpointService.listEndpoints()) {
            result.add(buildEndpointJSON(endpoint, false));
        }
        for(Endpoint endpoint : cacheSailProvider.getVolatileEndpoints()) {
            result.add(buildEndpointJSON(endpoint, true));
        }

        return Response.ok().entity(result).build();
    }

    @GET
    @Path("/provider/list")
    @Produces(Namespaces.MIME_TYPE_JSON)
    public Response listProviders() {
        if(cacheSailProvider.isEnabled()) {
            return Response.ok(getProviderNames()).build();
        } else {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity("caching is disabled").build();
        }
    }


    @GET
    @Path("/endpoint/{id}")
    @Produces("application/json")
    public Response retrieveEndpoint(@PathParam("id") String id) {

        Endpoint endpoint = endpointService.getEndpoint(id);
        if (endpoint == null) return notFound(id);


        return Response.ok().entity(buildEndpointJSON(endpoint, false)).build();
    }

    @DELETE
    @Path("/endpoint/{id}")
    public Response removeEndpoint(@PathParam("id") String id) {

        Endpoint endpoint = endpointService.getEndpoint(id);
        if (endpoint == null) return notFound(id);

        endpointService.removeEndpoint(endpoint);

        cacheSailProvider.updateEndpoints();

        return Response.ok().build();
    }

    @POST
    @Path("/endpoint/{id}/enable")
    public Response enableEndpoint(@PathParam("id") String id, @QueryParam("enable") @DefaultValue("true") boolean enable) {
        Endpoint endpoint = endpointService.getEndpoint(id);
        if (endpoint == null) return notFound(id);

        endpoint.setActive(enable);
        endpointService.updateEndpoint(endpoint);

        cacheSailProvider.updateEndpoints();

        return Response.ok().build();
    }

    private Response notFound(String id) {
        return Response.status(Status.NOT_FOUND).entity("No endpoint with id " + id + " found!").build();
    }

    @POST
    @Path("/endpoint/{id}/disable")
    public Response disableEndpoint(@PathParam("id") String id) {
        return enableEndpoint(id, false);
    }

    @GET
    @Path("/info")
    @Produces("application/json")
    public Response getInformation() {
        Map<String,String> result = new HashMap<>();
        result.put("backend", cacheSailProvider.getName());
        return Response.ok(result).build();
    }

    private Map<String, Object> buildEndpointJSON(Endpoint endpoint, boolean isVolatile) {
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("id",endpoint.getName().replaceAll("[^A-Za-z0-9 ]", "").toLowerCase());
        resultMap.put("name",endpoint.getName());
        resultMap.put("endpoint",endpoint.getEndpointUrl());
        resultMap.put("expiry", endpoint.getDefaultExpiry());
        resultMap.put("prefix",endpoint.getUriPattern());
        resultMap.put("kind",endpoint.getType().toString());
        resultMap.put("mimetype",endpoint.getContentTypes());
        resultMap.put("active", endpoint.isActive());
        resultMap.put("volatile", isVolatile);

        return resultMap;
    }


    private Set<String> getProviderNames() {
        Set<String> result = new HashSet<String>();

        for(DataProvider provider : cacheSailProvider.getLDClient().getDataProviders()) {
            result.add(provider.getName().toLowerCase());
        }

        return result;
    }
}
