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
package org.apache.marmotta.ldclient.provider.rdf;

import com.google.common.base.Preconditions;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

/**
 * A data provider that allows accessing SPARQL endpoints for retrieving the data associated with a resource. The
 * data provider will execute SPARQL statements of the form
 *    SELECT ?p ?o WHERE { resource ?p ?o }
 * to retrieve all "outgoing" properties of a resource.
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SPARQLProvider extends AbstractHttpProvider {

    public static final String PROVIDER_NAME = "SPARQL";
    private static Logger log = LoggerFactory.getLogger(SPARQLProvider.class);


    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log messages.
     *
     * @return
     */
    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    /**
     * Return the list of mime types accepted by this data provider.
     *
     * @return
     */
    @Override
    public String[] listMimeTypes() {
        return new String[]{"application/sparql-results+xml"};
    }

    /**
     * Build the URL to use to call the webservice in order to retrieve the data for the resource passed as argument.
     * In many cases, this will just return the URI of the resource (e.g. Linked Data), but there might be data providers
     * that use different means for accessing the data for a resource, e.g. SPARQL or a Cache.
     *
     *
     *
     * @param resourceUri
     * @param endpoint endpoint configuration for the data provider (optional)
     * @return
     */
    @Override
    public List<String> buildRequestUrl(String resourceUri, Endpoint endpoint) {
        Preconditions.checkNotNull(resourceUri);
        Preconditions.checkNotNull(endpoint);
        try {
            String contentType = "application/sparql-results+xml";
            if(endpoint.getContentTypes().size() > 0) {
                contentType = endpoint.getContentTypes().iterator().next().toStringNoParameters();
            }

            String query = "SELECT ?p ?o WHERE { <{uri}> ?p ?o }";

            String url = endpoint.getEndpointUrl()
                    .replace("{query}", URLEncoder.encode(query.replace("{uri}", resourceUri), "UTF-8"))
                    .replace("{contenttype}", URLEncoder.encode(contentType, "UTF-8"));

            return Collections.singletonList(url);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encoding UTF-8 not supported; the Java environment is severely broken");
        }
    }

    /**
     * Parse the HTTP response entity returned by the web service call and return its contents as a Sesame RDF
     * repository. The content type returned by the web service is passed as argument to help the implementation
     * decide how to parse the data.
     *
     *
     *
     *
     * @param resourceUri
     * @param in          input stream as returned by the remote webservice
     * @param contentType content type as returned in the HTTP headers of the remote webservice
     * @return an RDF repository containing an RDF representation of the dataset located at the remote resource.
     * @throws java.io.IOException in case an error occurs while reading the input stream
     */
    @Override
    public List<String> parseResponse(final String resourceUri, String requestUrl, final Repository triples, InputStream in, String contentType) throws DataRetrievalException {
        TupleQueryResultFormat format = QueryResultIO.getParserFormatForMIMEType(contentType, TupleQueryResultFormat.SPARQL);


        try {

            QueryResultIO.parse(in,format,
                    new TupleQueryResultHandler() {

                        RepositoryConnection con;
                        URI subject;

                        @Override
                        public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
                            subject = triples.getValueFactory().createURI(resourceUri);
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

						@Override
						public void handleBoolean(boolean bool	) throws QueryResultHandlerException {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void handleLinks(List<String> links) throws QueryResultHandlerException {
							// TODO Auto-generated method stub
							
						}
						
                    },
                    triples.getValueFactory());

            return Collections.emptyList();
        } catch (QueryResultParseException e) {
            throw new DataRetrievalException("parse error while trying to parse remote SPARQL results",e);
        } catch (TupleQueryResultHandlerException e) {
            throw new DataRetrievalException("parse error while trying to parse remote SPARQL results",e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while trying to read remote RDF content",e);
        }
    }
}
