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
package org.apache.marmotta.client.clients;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.marmotta.client.ClientConfiguration;
import org.apache.marmotta.client.exception.MarmottaClientException;
import org.apache.marmotta.client.model.rdf.BNode;
import org.apache.marmotta.client.model.rdf.Literal;
import org.apache.marmotta.client.model.rdf.RDFNode;
import org.apache.marmotta.client.model.rdf.URI;
import org.apache.marmotta.client.model.sparql.SPARQLResult;
import org.apache.marmotta.client.util.HTTPUtil;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandler;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.QueryResultParser;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.query.resultio.helpers.QueryResultCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SPARQLClient {

    private static Logger log = LoggerFactory.getLogger(SPARQLClient.class);

    private static final String URL_QUERY_SERVICE  = "/sparql/select";
    private static final String URL_UPDATE_SERVICE = "/sparql/update";

    private ClientConfiguration config;

    public SPARQLClient(ClientConfiguration config) {
        this.config = config;
    }

    /**
     * Run a SPARQL Select query against the Marmotta Server and return the results as SPARQL Result. Results will be
     * transfered and parsed using the SPARQL JSON format.
     * @param query a SPARQL Select query to run on the database
     * @return
     * @throws IOException
     * @throws MarmottaClientException
     */
    public SPARQLResult select(String query) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_QUERY_SERVICE + "?query=" + URLEncoder.encode(query, "utf-8");

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader("Accept", TupleQueryResultFormat.JSON.getDefaultMIMEType());
        
        try {

            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("SPARQL Query {} evaluated successfully",query);
                    QueryResultCollector results = new QueryResultCollector();
                    
                    parse(response.getEntity().getContent(), TupleQueryResultFormat.JSON, results, ValueFactoryImpl.getInstance());

                    if(!results.getHandledTuple() || results.getBindingSets().isEmpty()) {
                        return null;
                    } else {
                        List<String> fieldNames = results.getBindingNames();

                        SPARQLResult result = new SPARQLResult(new LinkedHashSet<String>(fieldNames));

                        //List<?> bindings = resultMap.get("results").get("bindings");
                        for(BindingSet nextRow : results.getBindingSets()) {
                            Map<String,RDFNode> row = new HashMap<String, RDFNode>();
                            
                            for(String nextBindingName : fieldNames) {
                                if(nextRow.hasBinding(nextBindingName)) {
                                    Binding nextBinding = nextRow.getBinding(nextBindingName);
                                    //Map<String,String> nodeDef = (Map<String,String>) entry.getValue();
                                    Value nodeDef = nextBinding.getValue();
                                    RDFNode node = null;
                                    if(nodeDef instanceof org.openrdf.model.URI) {
                                        node = new URI(nodeDef.stringValue());
                                    } else if(nodeDef instanceof org.openrdf.model.BNode) {
                                        node = new BNode(((org.openrdf.model.BNode)nodeDef).getID());
                                    } else if(nodeDef instanceof org.openrdf.model.Literal) {
                                        org.openrdf.model.Literal nodeLiteral = (org.openrdf.model.Literal)nodeDef;
                                        if(nodeLiteral.getLanguage() != null) {
                                            node = new Literal(nodeLiteral.getLabel(), nodeLiteral.getLanguage());
                                        } else if(nodeLiteral.getDatatype() != null) {
                                            node = new Literal(nodeLiteral.getLabel(), new URI(nodeLiteral.getDatatype().stringValue()));
                                        } else {
                                            node = new Literal(nodeLiteral.getLabel());
                                        }
                                    } else {
                                        log.error("unknown result node type: {}",nodeDef);
                                    }
                                    
                                    if(node != null) {
                                        row.put(nextBindingName, node);
                                    }
                                }
                            }
                            result.add(row);
                        }
                        return result;
                    }
                default:
                    log.error("error evaluating SPARQL Select Query {}: {} {}",new Object[] {query,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error evaluating SPARQL Select Query "+query+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } catch(TupleQueryResultHandlerException e) {
            throw new MarmottaClientException("error evaluating SPARQL Select Query ", e);
        } catch(QueryResultParseException e) {
            throw new MarmottaClientException("error evaluating SPARQL Select Query ", e);
        } catch(UnsupportedQueryResultFormatException e) {
            throw new MarmottaClientException("error evaluating SPARQL Select Query ", e);
        } catch(IllegalStateException e) {
            throw new MarmottaClientException("error evaluating SPARQL Select Query ", e);
        } catch(QueryResultHandlerException e) {
            throw new MarmottaClientException("error evaluating SPARQL Select Query ", e);
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Carry out a SPARQL ASK Query and return either true or false, depending on the query result.
     *
     * @param askQuery
     * @return
     * @throws IOException
     * @throws MarmottaClientException
     */
    public boolean ask(String askQuery) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_QUERY_SERVICE + "?query=" + URLEncoder.encode(askQuery, "utf-8");

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader("Accept", BooleanQueryResultFormat.JSON.getDefaultMIMEType());
        
        try {

            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("SPARQL ASK Query {} evaluated successfully",askQuery);
                    QueryResultCollector results = new QueryResultCollector();
                    
                    parse(response.getEntity().getContent(), BooleanQueryResultFormat.JSON, results, ValueFactoryImpl.getInstance());

                    if(!results.getHandledBoolean()) {
                        return false;
                    } else {
                        return results.getBoolean();
                    }
                default:
                    log.error("error evaluating SPARQL ASK Query {}: {} {}",new Object[] {askQuery,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error evaluating SPARQL ASK Query "+askQuery+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } catch(TupleQueryResultHandlerException e) {
            throw new MarmottaClientException("error evaluating SPARQL Ask Query ", e);
        } catch(QueryResultParseException e) {
            throw new MarmottaClientException("error evaluating SPARQL Ask Query ", e);
        } catch(UnsupportedQueryResultFormatException e) {
            throw new MarmottaClientException("error evaluating SPARQL Ask Query ", e);
        } catch(IllegalStateException e) {
            throw new MarmottaClientException("error evaluating SPARQL Ask Query ", e);
        } catch(QueryResultHandlerException e) {
            throw new MarmottaClientException("error evaluating SPARQL Ask Query ", e);
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Execute a SPARQL Update query according to the SPARQL 1.1 standard. The query will only be passed to the server,
     * which will react either with ok (in this the method simply returns) or with error (in this case, the method
     * throws an MarmottaClientException).
     *
     * @param updateQuery         the SPARQL Update 1.1 query string
     * @throws IOException        in case a connection problem occurs
     * @throws MarmottaClientException in case the server returned and error and did not execute the update
     */
    public void update(String updateQuery) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_UPDATE_SERVICE + "?update=" + URLEncoder.encode(updateQuery, "utf-8");

        HttpGet get = new HttpGet(serviceUrl);
        
        try {
                
            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("SPARQL UPDATE Query {} evaluated successfully",updateQuery);
                    break;
                default:
                    log.error("error evaluating SPARQL UPDATE Query {}: {} {}",new Object[] {updateQuery,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error evaluating SPARQL UPDATE Query "+updateQuery +": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }
    
    /**
     * FIXME: Replace this with QueryResultIO.parse after Sesame-2.7.3.
     * 
     * @param in
     * @param format
     * @param handler
     * @param valueFactory
     * @throws IOException
     * @throws QueryResultParseException
     * @throws TupleQueryResultHandlerException
     * @throws UnsupportedQueryResultFormatException
     */
    private static void parse(InputStream in, TupleQueryResultFormat format, QueryResultHandler handler,
            ValueFactory valueFactory)
        throws IOException, QueryResultParseException, QueryResultHandlerException,
        UnsupportedQueryResultFormatException
    {
        QueryResultParser parser = QueryResultIO.createParser(format);
        parser.setValueFactory(valueFactory);
        parser.setQueryResultHandler(handler);
        parser.parseQueryResult(in);
    }
    
    /**
     * FIXME: Replace this with QueryResultIO.parse after Sesame-2.7.3.
     * 
     * @param in
     * @param format
     * @param handler
     * @param valueFactory
     * @throws IOException
     * @throws QueryResultParseException
     * @throws TupleQueryResultHandlerException
     * @throws UnsupportedQueryResultFormatException
     */
    private static void parse(InputStream in, BooleanQueryResultFormat format, QueryResultHandler handler,
            ValueFactory valueFactory)
        throws IOException, QueryResultParseException, QueryResultHandlerException,
        UnsupportedQueryResultFormatException
    {
        QueryResultParser parser = QueryResultIO.createParser(format);
        parser.setValueFactory(valueFactory);
        parser.setQueryResultHandler(handler);
        parser.parseQueryResult(in);
    }
}
