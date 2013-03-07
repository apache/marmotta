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
package org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.service;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.exception.LDClientException;
import org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.model.ClientResponse;
import org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.model.Endpoint;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.event.InterceptingRepositoryConnection;
import org.openrdf.repository.event.base.InterceptingRepositoryConnectionWrapper;
import org.openrdf.repository.event.base.RepositoryConnectionInterceptorAdapter;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LDClient {

    private Logger log = LoggerFactory.getLogger(LDClient.class);


    private HttpParams httpParams;

    private LDEndpoints ldEndpoints;

    private Configuration config;


    public LDClient() {
        log.info("Initialising Linked Data Client Service ...");

        ldEndpoints = new LDEndpoints();
        try {
            config = new PropertiesConfiguration("ldclient.properties");
        } catch (ConfigurationException e) {
            log.warn("could not load configuration file ldclient.properties from current directory, home directory, or classpath");
        }


        httpParams = new BasicHttpParams();
        httpParams.setParameter(CoreProtocolPNames.USER_AGENT, "Salzburg NewMediaLab Linked Data Client");

        httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, config.getInt("so_timeout", 60000));
        httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, config.getInt("connection_timeout", 10000));

        httpParams.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS,true);
        httpParams.setIntParameter(ClientPNames.MAX_REDIRECTS,3);
    }




    /**
     * Retrieve all triples for this resource from the Linked Data Cloud. Retrieval will be carried out according
     * to the endpoint definition that matches this resource. In case no endpoint definition is found, the method
     * will try an "default" Linked Data retrieval if the configuration option "ldcache.fallback" is set to true
     *
     * @param resource the URI resource for which to retrieve the triples
     * @return a Sesame in-memory repository containing the triples for this resource
     */
    public ClientResponse retrieveResource(URI resource) throws LDClientException {
        HttpResponse response = null;
        try {
            Endpoint endpoint = ldEndpoints.getEndpoint(resource);

            if(endpoint != null && endpoint.getType() == Endpoint.EndpointType.NONE) {
                return null;
            } else if(endpoint != null) {
                response = retrieveFromEndpoint(resource,endpoint);
                return parseResponse(resource,response,endpoint);
            } else if(config.getBoolean("fallback",true)) {
                response = retrieveFromLDEndpoint(resource,null);
                return parseResponse(resource,response,endpoint);
            }
        } catch(TupleQueryResultHandlerException ex) {
            log.error("repository error while trying to retrieve resource "+resource.stringValue(),ex);

            throw new LDClientException("repository error while trying to retrieve resource "+resource.stringValue(),ex);
        } catch(QueryResultParseException ex) {
            log.error("SPARQL query result parsing error while trying to retrieve resource "+resource.stringValue(),ex);

            throw new LDClientException("SPARQL query result parsing error while trying to retrieve resource "+resource.stringValue(),ex);
        } catch(RDFParseException ex) {
            log.error("RDF parsing error while trying to retrieve resource "+resource.stringValue(),ex);

            throw new LDClientException("RDF parsing error while trying to retrieve resource "+resource.stringValue(),ex);
        } catch(RepositoryException ex) {
            log.error("repository error while trying to retrieve resource "+resource.stringValue(),ex);

            throw new LDClientException("repository error while trying to retrieve resource "+resource.stringValue(),ex);
        } catch(IOException ex) {
            log.error("I/O error while trying to retrieve resource {}: {}",resource.stringValue(),ex.getMessage());

            throw new LDClientException("I/O error while trying to retrieve resource "+resource.stringValue(),ex);
        } finally {
        }


        return null;
    }



    private HttpResponse retrieveFromEndpoint(URI resource, Endpoint endpoint) throws IOException, LDClientException {
        switch (endpoint.getType()) {
            case SPARQL:
                return retrieveFromSparqlEndpoint(resource, endpoint);
            case CACHE:
                return retrieveFromCacheEndpoint(resource,endpoint);
            case LINKEDDATA:
                return retrieveFromLDEndpoint(resource,endpoint);
            default:
                return null;
        }
    }



    private HttpResponse retrieveFromCacheEndpoint(URI resource, Endpoint endpoint) throws IOException {
        // TODO: use last-modified header

        HttpClient httpClient = createClient();


        String contentType = endpoint.getContentType();

        HttpGet get = new HttpGet(endpoint.getEndpointUrl().replace("{uri}", URLEncoder.encode(resource.stringValue(), "UTF-8")));
        get.setHeader("Accept",contentType);
        get.setHeader("Accept-Language", "*"); // PoolParty compatibility

        log.info("retrieving resource {} from cache endpoint; request URI is {}", resource.stringValue(), get.getURI());

        return httpClient.execute(get);
    }


    private HttpResponse retrieveFromLDEndpoint(URI resource, Endpoint endpoint) throws IOException, LDClientException {
        // TODO: use last-modified header

        HttpClient httpClient = createClient();

        String contentType = "application/rdf+xml, text/rdf+n3;q=0.8, text/turtle;q=0.6";
        if(endpoint != null) {
            contentType = endpoint.getContentType();
        }


        HttpGet get = new HttpGet(resource.stringValue());
        get.setHeader("Accept", contentType);
        get.setHeader("Accept-Language", "*"); // PoolParty compatibility

        log.info("retrieving resource {} as Linked Data; request URI is {}",resource.stringValue(),get.getURI());


        HttpResponse result = httpClient.execute(get);
        if(result.getEntity() == null || RDFParserRegistry.getInstance().getFileFormatForMIMEType(result.getEntity().getContentType().getValue().split(";")[0]) == null) {
            get.abort();
            if(result.getEntity() != null) {
                throw new LDClientException("invalid content returned by Linked Data resource "+resource.stringValue()+": "+result.getEntity().getContentType().getValue());
            } else {
                throw new LDClientException("no content returned by Linked Data resource "+resource.stringValue());
            }
        }

        return result;
    }


    private HttpResponse retrieveFromSparqlEndpoint(URI resource, Endpoint endpoint) throws IOException, LDClientException {
        String contentType = "application/sparql-results+xml";
        if(endpoint.getContentType() != null) {
            contentType = endpoint.getContentType();
        }

        String query = "SELECT ?p ?o WHERE { <{uri}> ?p ?o }";

        String url = endpoint.getEndpointUrl()
                .replace("{query}", URLEncoder.encode(query.replace("{uri}",resource.stringValue()),"UTF-8"))
                .replace("{contenttype}", URLEncoder.encode(contentType,"UTF-8"));

        HttpClient httpClient = createClient();

        HttpGet get = new HttpGet(url);
        get.setHeader("Accept",contentType);
        get.setHeader("Accept-Language", "*"); // PoolParty compatibility

        log.info("retrieving resource {} from SPARQL endpoint; request URI is {}", resource.stringValue(), get.getURI());

        return httpClient.execute(get);
    }


    private ClientResponse parseResponse(URI resource, HttpResponse response, Endpoint endpoint) throws LDClientException, IOException, RepositoryException, RDFParseException, QueryResultParseException, TupleQueryResultHandlerException {

        if(response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 400) {

            HttpEntity entity = response.getEntity();

            if(entity != null) {
                String contentType = "application/rdf+xml";
                long defaultExpires = config.getInt("expiry", 86400);
                if(endpoint != null && endpoint.getContentType() != null) {
                    contentType    = endpoint.getContentType();
                } else if(endpoint != null && endpoint.getType() == Endpoint.EndpointType.SPARQL) {
                    contentType = "application/sparql-results+xml";
                } else if(entity.getContentType() != null) {
                    contentType = entity.getContentType().getValue().split(";")[0];
                }
                if(endpoint != null && endpoint.getDefaultExpiry() != null) {
                    defaultExpires = endpoint.getDefaultExpiry();
                }


                InputStream in = entity.getContent();
                try {
                    Repository triples;

                    if(endpoint != null) {
                        switch (endpoint.getType()) {
                            case SPARQL:
                                triples = parseSparqlResponse(resource,in,contentType);
                                break;
                            default:
                                triples = parseRDFResponse(resource,in,contentType);
                        }
                    } else {
                        triples = parseRDFResponse(resource,in,contentType);
                    }



                    ClientResponse result = new ClientResponse(triples);

                    Header expires = response.getFirstHeader("Expires");
                    if(expires != null) {
                        try {
                            Date expiresDate = DateUtils.parseDate(expires.getValue());
                            result.setExpires(expiresDate);
                        } catch (DateParseException e) {
                            log.warn("could not parse Expires: header, using default expiry settings");
                            Date expiresDate = new Date(System.currentTimeMillis()+defaultExpires*1000);
                            result.setExpires(expiresDate);
                        }
                    } else {
                        Date expiresDate = new Date(System.currentTimeMillis()+defaultExpires*1000);
                        result.setExpires(expiresDate);
                    }

                    if(log.isInfoEnabled()) {
                        RepositoryConnection con = triples.getConnection();
                        log.info("retrieved {} triples for resource {}; expiry date: {}",new Object[] {con.size(),resource.stringValue(),result.getExpires()});
                        con.close();
                    }

                    return result;
                } finally {
                    in.close();
                }
            } else {
                throw new IOException("the HTTP request did not contain any data");
            }
        } else {
            log.error("the HTTP request failed (status: {})",response.getStatusLine());
            throw new LDClientException("the HTTP request failed (status: "+response.getStatusLine()+")");
        }

    }



    private Repository parseRDFResponse(final URI resource, InputStream in, String contentType) throws RepositoryException, IOException, RDFParseException {
        RDFFormat format = RDFParserRegistry.getInstance().getFileFormatForMIMEType(contentType, RDFFormat.RDFXML);

        Repository triples = new SailRepository(new MemoryStore());
        triples.initialize();

        InterceptingRepositoryConnection con =
                new InterceptingRepositoryConnectionWrapper(triples,triples.getConnection());

        con.addRepositoryConnectionInterceptor(new RepositoryConnectionInterceptorAdapter() {
            @Override
            public boolean add(RepositoryConnection conn, Resource s, org.openrdf.model.URI p, Value o, Resource... contexts) {
                if(s instanceof org.openrdf.model.URI) {
                    // if s is a URI and subject a KiWiUriResource, return true if they are different
                    return !((org.openrdf.model.URI)s).stringValue().equals(resource.stringValue());
                } else {
                    // in all other cases, return true to filter out the triple
                    return true;
                }
            };

            @Override
            public boolean remove(RepositoryConnection conn, Resource s, org.openrdf.model.URI p, Value o, Resource... contexts) {
                if(s instanceof org.openrdf.model.URI) {
                    // if s is a URI and subject a KiWiUriResource, return true if they are different
                    return !((org.openrdf.model.URI)s).stringValue().equals(resource.stringValue());
                } else {
                    // in all other cases, return true to filter out the triple
                    return true;
                }
            }
        });



        con.add(in,resource.stringValue(),format);
        con.commit();
        con.close();

        return triples;

    }



    private Repository parseSparqlResponse(final URI resource, InputStream in, String contentType) throws RepositoryException, IOException, QueryResultParseException, TupleQueryResultHandlerException {
        TupleQueryResultFormat format = QueryResultIO.getParserFormatForMIMEType(contentType, TupleQueryResultFormat.SPARQL);


        final Repository triples = new SailRepository(new MemoryStore());
        triples.initialize();

        QueryResultIO.parse(in,format,
                new TupleQueryResultHandler() {

                    RepositoryConnection con;
                    URI subject;

                    @Override
                    public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
                        subject = triples.getValueFactory().createURI(resource.stringValue());
                        try {
                            con = triples.getConnection();
                        } catch (RepositoryException e) {
                            throw new TupleQueryResultHandlerException("error while creating repository connection",e);
                        }
                    }

                    @Override
                    public void endQueryResult() throws TupleQueryResultHandlerException   {
                        try {
                            con.commit();
                            con.close();
                        } catch (RepositoryException e) {
                            throw new TupleQueryResultHandlerException("error while closing repository connection",e);
                        }
                    }

                    @Override
                    public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {

                        try {
                            Value predicate = bindingSet.getValue("p");
                            Value object    = bindingSet.getValue("o");

                            if(predicate instanceof URI) {
                                con.add(triples.getValueFactory().createStatement(subject,(URI)predicate,object));
                            } else {
                                log.error("ignoring binding as predicate {} is not a URI",predicate);
                            }
                        } catch (RepositoryException e) {
                            throw new TupleQueryResultHandlerException("error while adding triple to repository connection",e);
                        }
                    }
                },
                triples.getValueFactory());

        return triples;


    }


    private HttpClient createClient() {
        DefaultHttpClient client = new DefaultHttpClient(httpParams);
        client.setRedirectStrategy(new LMFRedirectStrategy());
        client.setHttpRequestRetryHandler(new LMFHttpRequestRetryHandler());
        return client;
    }


    private class LMFRedirectStrategy extends DefaultRedirectStrategy {
        @Override
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
            if (response == null) {
                throw new IllegalArgumentException("HTTP response may not be null");
            }

            int statusCode = response.getStatusLine().getStatusCode();
            String method = request.getRequestLine().getMethod();
            Header locationHeader = response.getFirstHeader("location");
            switch (statusCode) {
                case HttpStatus.SC_MOVED_TEMPORARILY:
                    return (method.equalsIgnoreCase(HttpGet.METHOD_NAME)
                            || method.equalsIgnoreCase(HttpHead.METHOD_NAME)) && locationHeader != null;
                case HttpStatus.SC_MOVED_PERMANENTLY:
                case HttpStatus.SC_TEMPORARY_REDIRECT:
                    return method.equalsIgnoreCase(HttpGet.METHOD_NAME)
                            || method.equalsIgnoreCase(HttpHead.METHOD_NAME);
                case HttpStatus.SC_SEE_OTHER:
                    return true;
                case HttpStatus.SC_MULTIPLE_CHOICES:
                    return true;
                default:
                    return false;
            } //end of switch
        }
    }

    private class LMFHttpRequestRetryHandler implements HttpRequestRetryHandler {
        /**
         * Determines if a method should be retried after an IOException
         * occurs during execution.
         *
         * @param exception      the exception that occurred
         * @param executionCount the number of times this method has been
         *                       unsuccessfully executed
         * @param context        the context for the request execution
         * @return <code>true</code> if the method should be retried, <code>false</code>
         *         otherwise
         */
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            return false;
        }
    }


}
