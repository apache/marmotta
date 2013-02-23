/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.ldpath.webservices;

import static org.apache.marmotta.commons.sesame.repository.ResultUtils.iterable;
import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;

import org.apache.marmotta.platform.ldpath.api.LDPathService;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.commons.util.JSONUtils;

import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiStringLiteral;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.backend.sesame.SesameConnectionBackend;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.text.Collator;
import java.util.*;

/**
 * Execute LDPath queries against the LMF backend. Depending on the LMF configuration, this might trigger retrieval
 * of external Linked Data resources before returning results.
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
@Path("/ldpath")
public class LDPathWebService {


    @Inject
    private Logger log;

    @Inject
    private LDPathService ldPathService;

    @Inject
    private SesameService sesameService;

    /**
     * Evaluate a single LDPath selection using the path passed as argument and starting at the resource identified
     * by the uri. Returns a list of RDF Nodes using the same syntax as RDF/JSON, i.e.
     * <ul>
     * <li> <code>{ "type": "uri", "value": "..." }</code> for resources</li>
     * <li><code>{ "type": "literal", "value": "...", "language": "...", "datatype": "..."}</code> for literals (datatype and language optional)</li>
     * </ul>
     *
     * @param path         the LDPath expression to evaluate
     * @param resourceUri  the URI of the resource from which to start the resource
     *
     * @return a list of RDF Nodes using the same syntax as RDF/JSON
     * @HTTP 404 in case the resource with the given URI does not exist
     * @HTTP 400 in case the path could not be parsed or the resource is not a valid URI
     * @HTTP 200 in case the query was evaluated successfully
     */
    @GET
    @Path("/path")
    @Produces("application/json")
    public Response evaluatePathQuery(@QueryParam("path") String path, @QueryParam("uri") String resourceUri) {
        //Preconditions.checkArgument(urlValidator.isValid(resourceUri));

        try {
            RepositoryConnection con = sesameService.getConnection();
            try {
                con.begin();
                if (ResourceUtils.isSubject(con, resourceUri)) {
                    URI resource = con.getValueFactory().createURI(resourceUri);
                    // get list of configured namespaces; we make them available for the path language
                    Map<String,String> namespaces = new HashMap<String, String>();
                    for(Namespace ns : iterable(con.getNamespaces())) {
                        namespaces.put(ns.getPrefix(),ns.getName());
                    }

                    List<Map<String,String>> result = new ArrayList<Map<String, String>>();

                    try {
                        for(Value node : ldPathService.pathQuery(resource,path,namespaces)) {
                            result.add(JSONUtils.serializeNodeAsJson(node));
                        }
                        return Response.ok().entity(result).build();
                    } catch (LDPathParseException e) {
                        log.warn("parse error while evaluating path {}: {}",path,e.getMessage());
                        return Response.status(Response.Status.BAD_REQUEST).entity("parse error while evaluating path '"+path+"': "+e.getMessage()).build();
                    }

                } else
                    return Response.status(Response.Status.NOT_FOUND).entity("resource "+resourceUri+" does not exist").build();
            } finally {
                con.commit();
                con.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,LDPathWebService.class);
            return Response.serverError().entity("error accessing RDF repository: "+ex.getMessage()).build();
        }

    }

    /**
     * Evaluate a LDPath program using the program string passed as argument and starting at the resource identified
     * by the uri. Returns a map from field names to lists of RDF nodes using the same syntax as RDF/JSON, i.e.
     * <ul>
     * <li> <code>{ "type": "uri", "value": "..." }</code> for resources</li>
     * <li><code>{ "type": "literal", "value": "...", "language": "...", "datatype": "..."}</code> for literals (datatype and language optional)</li>
     * </ul>
     *
     * @param program      the program to evaluate
     * @param resourceUri  the URI of the resource where to start
     * @return a map from field names to lists of rdf nodes in rdf/json format
     * @HTTP 404 in case the resource with the given URI does not exist
     * @HTTP 400 in case the path could not be parsed or the resource is not a valid URI
     * @HTTP 200 in case the query was evaluated successfully
     */
    @GET
    @Path("/program")
    @Produces("application/json")
    public Response evaluateProgramQuery(@QueryParam("program") String program, @QueryParam("uri") String resourceUri) {
        //Preconditions.checkArgument(urlValidator.isValid(resourceUri));

        try {
            RepositoryConnection con = sesameService.getConnection();
            try {
                con.begin();
                if (ResourceUtils.isSubject(con, resourceUri)) {
                    URI resource = con.getValueFactory().createURI(resourceUri);
                    // get list of configured namespaces; we make them available for the path language
                    Map<String,String> namespaces = new HashMap<String, String>();
                    for(Namespace ns : iterable(con.getNamespaces())) {
                        namespaces.put(ns.getPrefix(),ns.getName());
                    }


                    Map<String,List<Map<String,String>>> result = new HashMap<String, List<Map<String, String>>>();

                    try {
                        for(Map.Entry<String,Collection<?>> row : ldPathService.programQuery(resource,program).entrySet()) {
                            List<Map<String,String>> rowList = new ArrayList<Map<String, String>>();
                            for(Object o : row.getValue()) {
                                if(o instanceof KiWiNode) {
                                    rowList.add(JSONUtils.serializeNodeAsJson((Value) o));
                                } else {
                                    // we convert always to a literal
                                    rowList.add(JSONUtils.serializeNodeAsJson(new KiWiStringLiteral(o.toString())));
                                }
                            }
                            result.put(row.getKey(),rowList);
                        }
                        return Response.ok().entity(result).build();
                    } catch (LDPathParseException e) {
                        log.warn("parse error while evaluating program {}: {}", program, e.getMessage());
                        return Response.status(Response.Status.BAD_REQUEST).entity("parse error while evaluating program: "+e.getMessage()).build();
                    }


                } else
                    return Response.status(Response.Status.NOT_FOUND).entity("resource "+resourceUri+" does not exist").build();
            } finally {
                con.commit();
                con.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,LDPathWebService.class);
            return Response.serverError().entity("error accessing RDF repository: "+ex.getMessage()).build();
        }
    }


    /**
     * Return a list of all LDPath functions that have been registered in the LDPath installation.
     *
     * @return
     */
    @GET
    @Path("/functions")
    @Produces("application/json")
    public Response listFunctions() {
        List<Map<String,String>> results = new ArrayList<Map<String, String>>();

        try {
            RepositoryConnection con = sesameService.getConnection();
            try {
                con.begin();
                SesameConnectionBackend backend = SesameConnectionBackend.withConnection(con);
                for(SelectorFunction<Value> function : ldPathService.getFunctions()) {
                    Map<String,String> fmap = new HashMap<String, String>();
                    fmap.put("name", function.getPathExpression(backend));
                    fmap.put("signature",function.getSignature());
                    fmap.put("description",function.getDescription());
                    results.add(fmap);
                }
            } finally {
                con.commit();
                con.close();
            }
        } catch (RepositoryException e) {
            return Response.serverError().entity(e).build();
        }
        Collections.sort(results, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                return Collator.getInstance().compare(o1.get("name"),o2.get("name"));
            }
        });

        return Response.ok().entity(results).build();
    }

    /**
     * Return a list of all LDPath functions that have been registered in the LDPath installation.
     *
     * @return
     */
    @GET
    @Path("/functions/{name}")
    @Produces("application/json")
    public Response getFunction(@PathParam("name") String name) {
        try {
            RepositoryConnection con = sesameService.getConnection();
            try {
                con.begin();
                SesameConnectionBackend backend = SesameConnectionBackend.withConnection(con);

                for (SelectorFunction<Value> function : ldPathService.getFunctions()) {
                    final String fName = function.getPathExpression(backend);
                    if (name.equals(fName)) {
                        Map<String, String> fmap = new HashMap<String, String>();
                        fmap.put("name", fName);
                        fmap.put("signature", function.getSignature());
                        fmap.put("description", function.getDescription());
                        return Response.ok(fmap).build();
                    }
                }
                return Response.status(Status.NOT_FOUND).entity("LDPath function with name " + name + " does not exist").build();
            } finally {
                con.commit();
                con.close();
            }
        } catch (RepositoryException e) {
            return Response.serverError().entity(e).build();
        }
    }

    @GET
    @Path("/play/test/program")
    @Produces("application/json")
    public Response testProgram(@QueryParam("program") String program, @QueryParam("uri") String[] resourceUri) {
        if (resourceUri != null && resourceUri.length > 0)
            return evaluateProgramQuery(program, resourceUri[0]);
        return Response.status(Status.BAD_REQUEST).build();
    }


}
