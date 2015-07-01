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
package org.apache.marmotta.platform.sparql.webservices;

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.exporter.ExportService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.util.WebServiceUtil;
import org.apache.marmotta.platform.sparql.api.sparql.QueryType;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.jboss.resteasy.spi.NoLogWebApplicationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.openrdf.rio.RDFFormat.RDFXML;

/**
 * Execute SPARQL query (both query and update) on the LMF triple store
 * according the SPARQL 1.1 Protocol
 * 
 * @see <a href="http://www.w3.org/TR/sparql11-protocol/">http://www.w3.org/TR/sparql11-protocol/</a>
 * @see <a href="http://www.w3.org/TR/sparql11-service-description/">http://www.w3.org/TR/sparql11-service-description/</a>
 * 
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
@ApplicationScoped
@Path("/" + SparqlWebService.PATH)
public class SparqlWebService {

    public static final String PATH = "sparql";
    public static final String SELECT = "/select";
    public static final String UPDATE = "/update";

    private static final Map<String,String> outputMapper = new HashMap<String, String>() {
        {
            put("json","application/sparql-results+json");
            put("xml","application/sparql-results+xml");
            put("tabs","text/tab-separated-values");
            put("csv","text/csv");
            put("html","text/html");
        }
    };

    @Inject
    private Logger log;

    @Inject
    private SparqlService sparqlService;

    @Inject
    private ConfigurationService configurationService;
    
    @Inject
    private ExportService exportService;

    @Inject
    private TemplatingService templatingService;
    
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
     * Execute a SPARQL 1.1 tuple query on the LMF triple store using the query passed as query parameter to the
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
        if (StringUtils.isBlank(query)) {
            return createServiceDescriptionResponse(request, false);
        }
        //get real return type: even it is not in the standard, this is useful
        if(resultType != null && outputMapper.containsKey(resultType)) {
            resultType = outputMapper.get(resultType);
        }
        return select(query, resultType, request);
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
        if(resultType != null && outputMapper.containsKey(resultType)) resultType = outputMapper.get(resultType);
        return select(query, resultType, request);
    }    
    
    /**
     * Execute a SPARQL 1.1 tuple query on the LMF triple store using the query passed in the body of the
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
            if (resultType != null && outputMapper.containsKey(resultType)) resultType = outputMapper.get(resultType);
            if (request.getCharacterEncoding() == null) {
                request.setCharacterEncoding("utf-8");
            }
            String query = CharStreams.toString(request.getReader());
            //String query = IOUtils.toString(request.getInputStream(),"utf-8");
            log.debug("Query: {}", query);
            return select(query, resultType, request);
        } catch (IOException e) {
            log.error("body not found", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }    

    /**
     * Actual SELECT implementation
     * 
     * @param query
     * @param resultType
     * @param request
     * @return
     */
    private Response select(String query, String resultType, HttpServletRequest request) {
        try {
            List<String> acceptHeaders = EnumerationUtils.toList(request.getHeaders(ACCEPT));
            if (StringUtils.isBlank(query)) { //empty query
                for(String acceptHeader : acceptHeaders) {
                    if (acceptHeader.contains("html")) {
                        return Response.seeOther(new URI(configurationService.getServerUri() + "sparql/admin/squebi.html")).build();
                    }
                }

                return Response.status(Status.ACCEPTED).entity("no SPARQL query specified").build();
            } else {
                //query duck typing
                QueryType queryType = sparqlService.getQueryType(QueryLanguage.SPARQL, query);
                List<ContentType> acceptedTypes;
                List<ContentType> offeredTypes;
                if (resultType != null) {
                    acceptedTypes = MarmottaHttpUtils.parseAcceptHeader(resultType);
                } else {
                    acceptedTypes = MarmottaHttpUtils.parseAcceptHeaders(acceptHeaders);
                }
                if (QueryType.TUPLE.equals(queryType)) {
                    offeredTypes = MarmottaHttpUtils.parseQueryResultFormatList(TupleQueryResultWriterRegistry.getInstance().getKeys());
                } else if (QueryType.BOOL.equals(queryType)) {
                    offeredTypes = MarmottaHttpUtils.parseQueryResultFormatList(BooleanQueryResultWriterRegistry.getInstance().getKeys());
                } else if (QueryType.GRAPH.equals(queryType)) {
                    Set<String> producedTypes = new HashSet<String>(exportService.getProducedTypes());
                    producedTypes.remove("application/xml");
                    producedTypes.remove("text/plain");
                    producedTypes.remove("text/html");
                    producedTypes.remove("application/xhtml+xml");
                    offeredTypes = MarmottaHttpUtils.parseStringList(producedTypes);
                } else {
                    return Response.status(Response.Status.BAD_REQUEST).entity("no result format specified or unsupported result format").build();
                }
                ContentType bestType = MarmottaHttpUtils.bestContentType(offeredTypes, acceptedTypes);
                if (bestType == null) {
                    return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity("no result format specified or unsupported result format").build();
                } else {
                    return buildQueryResponse(bestType, query, queryType);
                }
            }
        } catch (InvalidArgumentException e) {
            log.error("query parsing threw an exception", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("query execution threw an exception", e);
            return Response.serverError().entity("query not supported").build();
        }
    }

    /**
     * For CORS operations TODO: make it more fine grained (maybe user dependent)
     * + TODO filter chain do not work properly
     * 
     * @param reqHeaders
     * @return responde

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
    */

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
    public Response updateGet(@QueryParam("update") String update, @QueryParam("query") String query, @QueryParam("output") String resultType, @Context HttpServletRequest request) {
        String q = getUpdateQuery(update, query);
        if (StringUtils.isBlank(q)) {
            return createServiceDescriptionResponse(request, true);
        }
        return update(q, resultType, request);
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
    public Response updatePostDirectly(@Context HttpServletRequest request, @QueryParam("output") String resultType) {
        try {
            if (request.getCharacterEncoding() == null) {
                request.setCharacterEncoding("utf-8");
            }
            String q = CharStreams.toString(request.getReader());
            return update(q, resultType, request);
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
            Map<String, String> params = parseEncodedQueryParameters(CharStreams.toString(request.getReader()));
            String q = StringUtils.defaultString(params.get("update"));
            String resultType = StringUtils.defaultString(params.get("output"));
            return update(q, resultType, request);
        } catch (IOException e) {
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        }
    }    

    /**
     * Actual update implementation
     * 
     */
    private Response update(String update, String resultType, HttpServletRequest request) {
        try {
            if (StringUtils.isNotBlank(update)) {
                sparqlService.update(QueryLanguage.SPARQL, update);
                return Response.ok().build();
            } else {
                if (resultType == null) {
                    List<ContentType> acceptedTypes = MarmottaHttpUtils.parseAcceptHeaders(EnumerationUtils.toList(request.getHeaders(ACCEPT)));
                    List<ContentType> offeredTypes = MarmottaHttpUtils.parseStringList(Lists.newArrayList("*/*", "text/html"));
                    ContentType bestType = MarmottaHttpUtils.bestContentType(offeredTypes, acceptedTypes);
                    if (bestType != null) {
                        resultType = bestType.getMime();
                    }
                }
                if (parseSubType(resultType).equals("html"))
                    return Response.seeOther(new URI(configurationService.getServerUri() + "sparql/admin/update.html")).build();
                else
                    return Response.status(Status.ACCEPTED).entity("no SPARQL query specified").build();
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
     * Parse the encoded query parameters
     * 
     * @todo this should be somewhere already implemented
     * @param body
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
    
    private Response createServiceDescriptionResponse(final HttpServletRequest request, final boolean isUpdate) {
        final List<ContentType> acceptedTypes;
        if (StringUtils.isBlank(request.getHeader(ACCEPT))) {
            acceptedTypes = Collections.singletonList(MarmottaHttpUtils.parseContentType(RDFXML.getDefaultMIMEType()));
        } else {
            acceptedTypes = MarmottaHttpUtils.parseAcceptHeaders(EnumerationUtils.toList(request.getHeaders(ACCEPT)));
        }
        
        ContentType _bestType = null;
        RDFFormat _format = null;
        for (ContentType ct : acceptedTypes) {
            final RDFFormat f = Rio.getWriterFormatForMIMEType(ct.getMime());
            if (f != null) {
                _bestType = ct;
                _format = f;
                break;
            }
        }
        if (_bestType == null || _format == null) {
            // FIXME: todo
            return Response.status(Status.BAD_REQUEST).entity("Could not determine Format").build();
        }
        
        final RDFFormat format = _format;
        final ContentType returnType = _bestType;
        
        final StreamingOutput entity = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException,
                    WebApplicationException {
                try {
                    final RDFWriter writer = Rio.createWriter(format, outputStream);
                    sparqlService.createServiceDescription(writer, request.getRequestURL().toString(), isUpdate);
                } catch (RDFHandlerException e) {
                    log.warn("Could not send SpaqlServiceDescription: {}", e);
                    throw new NoLogWebApplicationException(e, Response.serverError().entity(e).build());
                }
            }
        };

        return Response.ok(entity, new MediaType(returnType.getType(), returnType.getSubtype(), returnType.getCharset().name())).build();
    }
    
    private Response buildQueryResponse(final ContentType format, final String query, final QueryType queryType) throws Exception {
        StreamingOutput entity = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                	sparqlService.query(QueryLanguage.SPARQL, query, output, format.getMime(), configurationService.getIntConfiguration("sparql.timeout", 60));
                } catch (MarmottaException ex) {
                    throw new WebApplicationException(ex.getCause(), Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(ex)).build());
                } catch (MalformedQueryException e) {
                    throw new WebApplicationException(e.getCause(), Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(e)).build());
                } catch (TimeoutException e) {
                    throw new WebApplicationException(e.getCause(), Response.status(Response.Status.GATEWAY_TIMEOUT).entity(WebServiceUtil.jsonErrorResponse(e)).build());
                }
            }
        };
        
        final ResponseBuilder responseBuilder = Response.ok().entity(entity).header(CONTENT_TYPE, format.getMime());
        final TupleQueryResultFormat fmt = QueryResultIO.getWriterFormatForMIMEType(format.getMime());
        if (fmt != null) {
            responseBuilder.header("Content-Disposition", String.format("attachment; filename=\"%s.%s\"", queryType.toString().toLowerCase(), fmt.getDefaultFileExtension()));
        }
        return responseBuilder.build();
    }

    private static Pattern subTypePattern = Pattern.compile("[a-z]+/([a-z0-9-._]+\\+)?([a-z0-9-._]+)(;.*)?");
    private String parseSubType(String mimeType) {
        Matcher matcher = subTypePattern.matcher(mimeType);
        if (matcher.matches())
            return matcher.group(2);
        else
            return mimeType;
    }

}
