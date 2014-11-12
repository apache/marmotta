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
package org.apache.marmotta.platform.ldpath.webservices;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.commons.util.JSONUtils;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.backend.sesame.SesameConnectionBackend;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.ldpath.api.LDPathService;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.*;

import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;
import static org.apache.marmotta.commons.sesame.repository.ResultUtils.iterable;

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
        if (StringUtils.isBlank(program)) {
            return Response.status(Status.BAD_REQUEST).entity("ldpath program must be provided").build();
        }
        if (StringUtils.isBlank(resourceUri)) {
            return Response.status(Status.BAD_REQUEST).entity("context 'uri' to start ldpath evaluation must be provided").build();
        }

        try {
            RepositoryConnection con = sesameService.getConnection();
            try {
                con.begin();
                if (ResourceUtils.isSubject(con, resourceUri)) {
                    URI resource = con.getValueFactory().createURI(resourceUri);

                    Map<String,List<Map<String,String>>> result = new HashMap<String, List<Map<String, String>>>();

                    try {
                        for(Map.Entry<String,Collection<?>> row : ldPathService.programQuery(resource,program).entrySet()) {
                            List<Map<String,String>> rowList = new ArrayList<Map<String, String>>();
                            for(Object o : row.getValue()) {
                                if(o instanceof Value) {
                                    rowList.add(JSONUtils.serializeNodeAsJson((Value) o));
                                } else {
                                    // we convert always to a literal
                                    rowList.add(JSONUtils.serializeNodeAsJson(new LiteralImpl(o.toString())));
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
     * Evaluate a LDPath program using the program string passed as argument and starting at the resource identified
     * by the uri. Returns a map from field names to lists of RDF nodes using the same syntax as RDF/JSON, i.e.
     * <ul>
     * <li> <code>{ "type": "uri", "value": "..." }</code> for resources</li>
     * <li><code>{ "type": "literal", "value": "...", "language": "...", "datatype": "..."}</code> for literals (datatype and language optional)</li>
     * </ul>
     *
     * @param body      the program to evaluate
     * @param resourceUri  the URI of the resource where to start
     * @return a map from field names to lists of rdf nodes in rdf/json format
     * @HTTP 404 in case the resource with the given URI does not exist
     * @HTTP 400 in case the path could not be parsed or the resource is not a valid URI
     * @HTTP 200 in case the query was evaluated successfully
     */
    @POST
    @Path("/program")
    @Produces("application/json")
    public Response evaluateProgramQuery(InputStream body, @QueryParam("uri") String resourceUri) {
        try {
            String program = IOUtils.toString(body);
            return evaluateProgramQuery(program, resourceUri);
        } catch (IOException e) {
            return Response.serverError().entity("could not read ldpath program: "+e.getMessage()).build();
        }
    }

    /**
     * Return a list of all LDPath functions that have been registered in the LDPath installation.
     *
     * @HTTP 200 in case the functions exist; will return the function descriptions
     * @HTTP 500 in case there was an error accessing the triple store
     *
     * @return a list of JSON maps with the fields "name", "signature" and "description"
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
     * Return a description of the function whose name is passed as path argument.
     *
     * @HTTP 200 in case the function exists; will return the function description
     * @HTTP 404 in case the function does not exist
     * @HTTP 500 in case there was an error accessing the triple store
     *
     * @return a JSON map with the fields "name", "signature" and "description"
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

    /**
     * Evaluate the LDPath program send as byte stream in the POST body of the request starting at the contexts (array)
     * given as URL query arguments. Will return a JSON map with an entry for each context and its evaluation result.
     * The value of each entry will have the following format:
     * <ul>
     * <li><code>{ "type": "uri", "value": "..." }</code> for resources</li>
     * <li><code>{ "type": "literal", "value": "...", "language": "...", "datatype": "..."}</code> for literals (datatype and language optional)</li>
     * </ul>

     *
     * @HTTP 200 in case the evaluation was successful for all contexts
     * @HTTP 400 in case the LDPath program was invalid
     * @HTTP 404 in case one of the contexts passed as argument does not exist
     * @HTTP 500 in case there was an error accessing the repository or reading the POST body
     *
     * @param contextURI     the URI of a single context to evaluate the program against
     * @param contextURIarr  an array of URIs to use as contexts to evaluate the program against
     * @param request        a POST request containing the LDPath program in the POST body
     * @return a JSON map with an entry for each context pointing to its evaluation result (another map with field/value pairs)
     */
    @POST
    @Path("/debug")
    @Produces("application/json")
    public Response testProgram(@QueryParam("context") String[] contextURI, @QueryParam("context[]") String[] contextURIarr,  @Context HttpServletRequest request) {
        final String[] cs = contextURI != null ? contextURI : contextURIarr;

        try {
            // 1. read in the program from the post stream
            String program = IOUtils.toString(request.getReader());

            // 2. auto-register all namespaces that are defined in the triple store
            Map<String,String> namespaces = new HashMap<String, String>();
            RepositoryConnection con = sesameService.getConnection();
            try {
                con.begin();
                for(Namespace ns : iterable(con.getNamespaces())) {
                    namespaces.put(ns.getPrefix(),ns.getName());
                }

                // 3. iterate over all context uris passed as argument and run the path query, storing the results
                //    in a hashmap where the context uris are the keys and a result map is the value
                HashMap<String, Object> combined = new HashMap<String, Object>();
                for(String context : cs) {
                    if (ResourceUtils.isSubject(con, context)) {
                        URI resource = con.getValueFactory().createURI(context);

                        Map<String,List<Map<String,String>>> result = new HashMap<String, List<Map<String, String>>>();

                        try {
                            for(Map.Entry<String,Collection<?>> row : ldPathService.programQuery(resource,program).entrySet()) {
                                List<Map<String,String>> rowList = new ArrayList<Map<String, String>>();
                                for(Object o : row.getValue()) {
                                    if(o instanceof Value) {
                                        rowList.add(JSONUtils.serializeNodeAsJson((Value) o));
                                    } else {
                                        // we convert always to a literal
                                        rowList.add(JSONUtils.serializeNodeAsJson(new LiteralImpl(o.toString())));
                                    }
                                }
                                result.put(row.getKey(),rowList);
                            }

                            combined.put(context,result);
                        } catch (LDPathParseException e) {
                            log.warn("parse error while evaluating program {}: {}", program, e.getMessage());
                            return Response.status(Response.Status.BAD_REQUEST).entity("parse error while evaluating program: "+e.getMessage()).build();
                        }
                    }  else {
                        return Response.status(Response.Status.NOT_FOUND).entity("resource "+context+" does not exist").build();
                    }
                }


                return Response.ok(combined).build();
            } finally {
                con.commit();
                con.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,LDPathWebService.class);
            return Response.serverError().entity("error accessing RDF repository: "+ex.getMessage()).build();
        } catch(IOException ex) {
            return Response.serverError().entity("error reading program from stream: "+ex.getMessage()).build();
        }
    }

}
