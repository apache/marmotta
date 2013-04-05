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
package org.apache.marmotta.platform.sparql.webservices;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.LMFHttpUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.util.WebServiceUtil;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.platform.sparql.services.sparql.SparqlWritersHelper;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

/**
 * Execute SPARQL query (both query and update) on the kiwi triple store
 * according the SPARQL 1.1 Protocol
 * 
 * @link http://www.w3.org/TR/sparql11-protocol/
 * 
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
@ApplicationScoped
@Path("/" + SparqlWebService.PATH)
public class SparqlWebService {
	
    public final static String PATH = "sparql";
    public static final String SELECT = "/select";
    public static final String UPDATE = "/update";
    public static final String SNORQL = "/snorql";
    
    @Inject
    private Logger log;

    @Inject
    private SparqlService sparqlService;

    @Inject
    private ConfigurationService configurationService;
    
    /**
     * Single SPARQL endpoint, redirecting to the actual select endpoint 
     * when possible
     * 
     * @param query
     * @param update
     * @param request
     * @return
     * @throws URISyntaxException
     */
    @GET
    public Response get(@QueryParam("query") String query, @QueryParam("update") String update, @Context HttpServletRequest request) throws URISyntaxException {
    	if (StringUtils.isNotBlank(update)) {
    		String msg = "update operations are not supported through get"; //or yes?
    		log.error(msg);
			return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
    	} else {
    		UriBuilder builder = UriBuilder.fromPath(PATH + SELECT);
    		if (StringUtils.isNotBlank(query)) {    		
    			builder.replaceQuery(request.getQueryString());
    		}
			return Response.seeOther(builder.build()).build();
		}
    }
    
    /** 
     * Single endpoint for direct post queries (not yet implemented)
     * 
     * @param request
     * @return
     */
    @POST
    public Response post(@Context HttpServletRequest request) {
    	//String query = CharStreams.toString(request.getReader());        
    	//TODO: introspect the query to determine the operation type
    	String msg = "impossible to determine which type of operation (query/update) the request contains";
    	log.error(msg);
		return Response.status(Response.Status.CONFLICT).entity(msg).build();
    }

    /**
     * For CORS operations TODO: make it more fine grained (maybe user dependent)
     * + TODO filter chain do not work properly
     * 
     * @param reqHeaders
     * @return responde
     */
    @OPTIONS
    @Path(UPDATE)
    public Response optionsResourceRemote(@HeaderParam("Access-Control-Request-Headers") String reqHeaders) {
        if(reqHeaders == null) {
            reqHeaders = "Accept, Content-Type";
        }
        return Response.ok()
                .header("Allow", "POST")
                .header("Access-Control-Allow-Methods", "POST")
                .header("Access-Control-Allow-Headers", reqHeaders)
                .header("Access-Control-Allow-Origin", configurationService.getStringConfiguration("sparql.allow_origin","*"))
                .build();

    }
    
    /**
     * Actual select query web service implementation
     * 
     * @param query query
     * @param resultType format requested
     * @param accept accept header
     * @return
     */
    private Response select(String query, String resultType, String accept) {
		if (StringUtils.isBlank(query)) {
			try {
				return Response.temporaryRedirect(
						new URI(configurationService.getServerUri()
								+ "sparql/admin/snorql/snorql.html")).build();
			} catch (URISyntaxException e) {
				return Response.status(Status.NOT_FOUND).build();
			}
		} else {
			try {
				final Query sparqlQuery = sparqlService.parseQuery(QueryLanguage.SPARQL, query);

				final String format;
				if (StringUtils.isBlank(resultType)) {
					List<ContentType> offeredTypes = new ArrayList<ContentType>();
					if (sparqlQuery instanceof TupleQuery
							|| sparqlQuery instanceof BooleanQuery) {
						offeredTypes = LMFHttpUtils.parseStringList(Lists
								.newArrayList("application/sparql-results+xml",
										"application/sparql-results+json",
										"text/html", "text/csv"));
					} else if (sparqlQuery instanceof GraphQuery) {
						offeredTypes = LMFHttpUtils.parseStringList(Lists
								.newArrayList("application/rdf+xml",
										"text/rdf+n3", "text/turtle",
										"application/json"));
					}
					List<ContentType> acceptedTypes = LMFHttpUtils.parseAcceptHeader(accept);
					ContentType bestType = LMFHttpUtils.bestContentType(offeredTypes, acceptedTypes);
					if (bestType != null) {
						format = bestType.getMime();
					} else {
						format = "";
					}
				} else {
					format = resultType; // FIXME: validate?
				}

				return buildQueryResponse(format, query);

			} catch (RepositoryException e) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
			} catch (MalformedQueryException e) {
				return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
			} catch (Exception e) {
				log.error("query execution threw an exception", e);
				return Response.serverError().entity("query not supported").build();
			}
		}
    }
    
    /**
     * Execute a SPARQL 1.1 tuple query on the KiWi triple store using the query passed as query parameter to the
     * GET request. Result will be formatted using the result type passed as argument (either "html", "json" or "xml").
     * <p/>
     * see SPARQL 1.1 Query syntax at http://www.w3.org/TR/sparql11-query/
     *
     * @param query       the SPARQL 1.1 Query as a string parameter
     * @param resultType  the format for serializing the query results ("html", "json", or "xml")
     * @HTTP 200 in case the query was executed successfully
     * @HTTP 500 in case there was an error during the query evaluation
     * @return the query result in the format passed as argument
     */
    @GET
    @Path(SELECT)
    public Response selectGet(@QueryParam("query") String query, @QueryParam("output") String resultType, @Context HttpServletRequest request) {
    	return select(query, resultType, request.getHeader("Accept"));
    }    
    
    /**
     * Execute a SPARQL 1.1 tuple query on the LMF triple store using the query passed as form parameter to the
     * POST request. Result will be formatted using the result type passed as argument (either "html", "json" or "xml").
     * <p/>
     * see SPARQL 1.1 Query syntax at http://www.w3.org/TR/sparql11-query/
     *
     * @param query       the SPARQL 1.1 Query as a string parameter
     * @param resultType  the format for serializing the query results ("html", "json", or "xml")
     * @HTTP 200 in case the query was executed successfully
     * @HTTP 500 in case there was an error during the query evaluation
     * @return the query result in the format passed as argument
     */
    @POST
    @Consumes({"application/x-www-url-form-urlencoded", "application/x-www-form-urlencoded"})
    @Path(SELECT)
    public Response selectPostForm(@FormParam("query") String query, @QueryParam("output") String resultType, @Context HttpServletRequest request) {
    	return select(query, resultType, request.getHeader("Accept"));
    }

    /**
     * Execute a SPARQL 1.1 tuple query on the KiWi triple store using the query passed in the body of the
     * POST request. Result will be formatted using the result type passed as argument (either "html", "json" or "xml").
     * <p/>
     * see SPARQL 1.1 Query syntax at http://www.w3.org/TR/sparql11-query/
     *
     * @param request     the servlet request (to retrieve the SPARQL 1.1 Query passed in the body of the POST request)
     * @param resultType  the format for serializing the query results ("html", "json", or "xml")
     * @HTTP 200 in case the query was executed successfully
     * @HTTP 500 in case there was an error during the query evaluation
     * @return the query result in the format passed as argument
     */
    @POST
    @Path(SELECT)
    public Response selectPost(@QueryParam("output") String resultType, @Context HttpServletRequest request) {
    	try {
    		String query = CharStreams.toString(request.getReader());
    		return select(query, resultType, request.getHeader("Accept"));
    	} catch (IOException e) {
    		return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
    }

    /**
     * Execute a SPARQL 1.1 tuple query on the KiWi triple store using the query passed as form parameter to the
     * POST request. Result will be formatted using the result type passed as argument (either "html", "json" or "xml").
     * <p/>
     * see SPARQL 1.1 Query syntax at http://www.w3.org/TR/sparql11-query/
     *
     * @param query       the SPARQL 1.1 Query as a string parameter
     * @param resultType  the format for serializing the query results ("html", "json", or "xml")
     * @HTTP 200 in case the query was executed successfully
     * @HTTP 500 in case there was an error during the query evaluation
     * @return the query result in the format passed as argument
     */
    @POST
    @Path(SNORQL)
    public Response snorqlPost(@FormParam("output") String resultType, @FormParam("query") String query) {
        try {
            return buildQueryResponse(resultType, query);
        } catch(Exception e) {
            log.error("query execution threw an exception",e);
            return Response.serverError().entity("query not supported").build();
        }
    }

    /**
     * Execute a SPARQL 1.1 Update request passed in the query parameter of the GET. The update will
     * be carried out
     * on the LMF triple store.
     * <p/>
     * see SPARQL 1.1 Update syntax at http://www.w3.org/TR/sparql11-update/
     * 
     * @param update the update query in SPARQL 1.1 syntax
     * @param query the update query in SPARUL syntax
     * @HTTP 200 in case the update was carried out successfully
     * @HTTP 500 in case the update was not successful
     * @return empty content in case the update was successful, the error message in case an error occurred
     */
    @GET
    @Path(UPDATE)
    public Response updateGet(@QueryParam("update") String update, @QueryParam("query") String query, @QueryParam("output") String resultType,
            @Context HttpServletRequest request) {
        try {
            String q = getUpdateQuery(update, query);
            if (StringUtils.isNotBlank(q)) {
                sparqlService.update(QueryLanguage.SPARQL, q);
                return Response.ok().build();
            } else {
                if (resultType == null) {
                    List<ContentType> acceptedTypes = LMFHttpUtils.parseAcceptHeader(request.getHeader("Accept"));
                    List<ContentType> offeredTypes = LMFHttpUtils.parseStringList(Lists.newArrayList("*/*", "text/html"));
                    ContentType bestType = LMFHttpUtils.bestContentType(offeredTypes, acceptedTypes);
                    if (bestType != null) {
                        resultType = bestType.getMime();
                    }
                }
                if (SparqlWritersHelper.parseSubType(resultType).equals("html"))
                    return Response.seeOther(new URI(configurationService.getServerUri() + "sparql/admin/update.html")).build();
                else
                    return Response.status(Response.Status.BAD_REQUEST).entity("no SPARQL query specified").build();
            }
        } catch (MalformedQueryException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(ex)).build();
        } catch(UpdateExecutionException e) {
            log.error("update execution threw an exception",e);
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        } catch (MarmottaException e) {
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        } catch (URISyntaxException e) {
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        }
    }

    /**
     * Get right update query from both possible parameters, for keeping
     * backward compatibility with the old parameter
     * 
     * @param update update parameter
     * @param query query parameter
     * @return
     */
    private String getUpdateQuery(String update, String query) {
        if (StringUtils.isNotBlank(update))
            return update;
        else if (StringUtils.isNotBlank(query)) {
            log.warn("Update query still uses the old 'query' parameter");
            return query;
        } else
            return null;
    }

    /**
     * Execute a SPARQL 1.1 Update request using update via POST directly; 
     * see details at http://www.w3.org/TR/sparql11-protocol/\#update-operation
     * 
     * @param request the servlet request (to retrieve the SPARQL 1.1 Update query passed in the
     *            body of the POST request)
     * @HTTP 200 in case the update was carried out successfully
     * @HTTP 400 in case the update query is missing or invalid
     * @HTTP 500 in case the update was not successful
     * @return empty content in case the update was successful, the error message in case an error
     *         occurred
     */
    @POST
    @Path(UPDATE)
    @Consumes("application/sparql-update")
    public Response updatePostDirectly(@Context HttpServletRequest request) {
        try {
            String query = CharStreams.toString(request.getReader());
            if (StringUtils.isNotBlank(query)) {
                sparqlService.update(QueryLanguage.SPARQL, query);
                return Response.ok().build();
            } else
                return Response.status(Response.Status.BAD_REQUEST).entity("no SPARQL query given").build();
        } catch (MalformedQueryException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(e)).build();
        } catch(UpdateExecutionException e) {
            log.error("update execution threw an exception", e);
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        } catch (MarmottaException e) {
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        } catch (IOException e) {
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        }
    }
    
    /**
     * Execute a SPARQL 1.1 Update request using update via URL-encoded POST; 
     * see details at http://www.w3.org/TR/sparql11-protocol/\#update-operation
     * 
     * @param request the servlet request (to retrieve the SPARQL 1.1 Update query passed in the
     *            body of the POST request)
     * @HTTP 200 in case the update was carried out successfully
     * @HTTP 400 in case the update query is missing or invalid
     * @HTTP 500 in case the update was not successful
     * @return empty content in case the update was successful, the error message in case an error
     *         occurred
     */
    @POST
    @Path(UPDATE)
    @Consumes({"application/x-www-url-form-urlencoded", "application/x-www-form-urlencoded"})
    public Response updatePostUrlEncoded(@Context HttpServletRequest request) {
        try {
            Map<String,String> params = parseEncodedQueryParameters(CharStreams.toString(request.getReader()));           
            if (params.containsKey("update") && StringUtils.isNotBlank(params.get("update"))) {
                sparqlService.update(QueryLanguage.SPARQL, params.get("update"));
                return Response.ok().build();
            } else
                return Response.status(Response.Status.BAD_REQUEST).entity("no SPARQL query given").build();
        } catch (MalformedQueryException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(e)).build();
        } catch(UpdateExecutionException e) {
            log.error("update execution threw an exception", e);
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        } catch (MarmottaException e) {
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        } catch (IOException e) {
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        }
    }    

    /**
     * Parse the encoded query parameters
     * 
     * @todo this should be somewhere already implemented
     * @param string
     * @return parameters
     */
    private Map<String,String> parseEncodedQueryParameters(String body) {
    	Map<String,String> params = new HashMap<String,String>();
        for (String pair : body.split("&")) {
            int eq = pair.indexOf("=");
            try {
	            if (eq < 0) {
	                // key with no value
	                params.put(URLDecoder.decode(pair, "UTF-8"), "");
	            } else {
	                // key=value
	                String key = URLDecoder.decode(pair.substring(0, eq), "UTF-8");
	                String value = URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
	                params.put(key, value);
	            }
            } catch (UnsupportedEncodingException e) {
            	log.error("Query parameter cannot be decoded: {}", e.getMessage(), e);
            }
        }
		return params;
	}
	
	private Response buildQueryResponse(final String format, final String query) throws Exception {
        StreamingOutput entity = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    sparqlService.query(QueryLanguage.SPARQL, query, output, format);
                } catch (MarmottaException e) {
                    throw new WebApplicationException(e.getCause(), Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(e)).build());
                }
            }
        };
        
        return Response.ok().entity(entity).header("Content-Type", format+";charset=utf-8").build();
    }

}
