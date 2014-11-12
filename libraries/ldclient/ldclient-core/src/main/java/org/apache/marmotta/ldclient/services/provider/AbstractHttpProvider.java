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
package org.apache.marmotta.ldclient.services.provider;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.util.EntityUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.ACCEPT_LANGUAGE;
import static org.apache.marmotta.commons.http.MarmottaHttpUtils.parseContentType;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class AbstractHttpProvider implements DataProvider {

    public static final int RETRY_AFTER = 60;
    private static Logger log = LoggerFactory.getLogger(AbstractHttpProvider.class);

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
    protected abstract List<String> buildRequestUrl(String resourceUri, Endpoint endpoint) throws DataRetrievalException;

    /**
     * Parse the HTTP response entity returned by the web service call and return its contents in a Sesame RDF
     * repository also passed as argument. The content type returned by the web service is passed as argument to help
     * the implementation decide how to parse the data. The implementation can return a list of additional pages to
     * retrieve for completing the data of the resource
     *
     *
     *
     *
     *
     * @param resourceUri
     * @param model   an RDF repository for storing an RDF representation of the dataset located at the remote resource.
     * @param in           input stream as returned by the remote webservice
     * @param contentType  content type as returned in the HTTP headers of the remote webservice
     * @return a possibly empty list of URLs of additional resources to retrieve to complete the content
     * @throws java.io.IOException in case an error occurs while reading the input stream
     */
    protected abstract List<String> parseResponse(String resourceUri, String requestUrl, Model model, InputStream in, String contentType) throws DataRetrievalException;

    /**
     * Retrieve the data for a resource using the given http client and endpoint definition. The service is
     * supposed to manage the connection handling itself. See {@link AbstractHttpProvider}
     * for a generic implementation of this method.
     *
     *
     *
     * @param resource the resource to be retrieved
     * @param endpoint the endpoint definition
     * @return a completely specified client response, including expiry information and the set of triples
     */
    @Override
    public ClientResponse retrieveResource(String resource, LDClientService client, Endpoint endpoint) throws DataRetrievalException {

        try {

            String contentType;
            if(endpoint != null && endpoint.getContentTypes().size() > 0) {
                contentType = Joiner.on(',').join(Iterables.transform(endpoint.getContentTypes(), new Function<ContentType, String>() {
                    @Override
                    public String apply(ContentType input) {
                        return input.toString("q");
                    }
                }));

            } else {
                contentType = Joiner.on(',').join(Arrays.asList(listMimeTypes()));
            }

            long defaultExpires = client.getClientConfiguration().getDefaultExpiry();
            if(endpoint != null && endpoint.getDefaultExpiry() != null) {
                defaultExpires = endpoint.getDefaultExpiry();
            }

            final ResponseHandler handler = new ResponseHandler(resource, endpoint);

            // a queue for queuing the request URLs needed to build the query response
            Queue<String> requestUrls = new LinkedList<String>();
            requestUrls.addAll(buildRequestUrl(resource, endpoint));

            Set<String> visited = new HashSet<String>();

            String requestUrl = requestUrls.poll();
            while(requestUrl != null) {

                if(!visited.contains(requestUrl)) {
                    HttpGet get = new HttpGet(requestUrl);
                    try {
                        get.setHeader(ACCEPT, contentType);
                        get.setHeader(ACCEPT_LANGUAGE, "*"); // PoolParty compatibility

                        log.info("retrieving resource data for {} from '{}' endpoint, request URI is <{}>", new Object[]  {resource, getName(), get.getURI().toASCIIString()});

                        handler.requestUrl = requestUrl;
                        List<String> additionalRequestUrls = client.getClient().execute(get, handler);
                        requestUrls.addAll(additionalRequestUrls);

                        visited.add(requestUrl);
                    } finally {
                        get.releaseConnection();
                    }
                }

                requestUrl = requestUrls.poll();
            }

            Date expiresDate = handler.expiresDate;
            if (expiresDate == null) {
                expiresDate = new Date(System.currentTimeMillis() + defaultExpires * 1000);
            }

            long min_expires = System.currentTimeMillis() + client.getClientConfiguration().getMinimumExpiry() * 1000;
            if (expiresDate.getTime() < min_expires) {
                log.info("expiry time returned by request lower than minimum expiration time; using minimum time instead");
                expiresDate = new Date(min_expires);
            }

            if(log.isInfoEnabled()) {
                log.info("retrieved {} triples for resource {}; expiry date: {}", new Object[]{handler.triples.size(), resource, expiresDate});
            }

            ClientResponse result = new ClientResponse(handler.httpStatus, handler.triples);
            result.setExpires(expiresDate);
            return result;
        } catch (RepositoryException e) {
            log.error("error while initialising Sesame repository; classpath problem?",e);
            throw new DataRetrievalException("error while initialising Sesame repository; classpath problem?",e);
        } catch (ClientProtocolException e) {
            log.error("HTTP client error while trying to retrieve resource {}: {}", resource, e.getMessage());
            throw new DataRetrievalException("I/O error while trying to retrieve resource "+resource,e);
        } catch (IOException e) {
            log.error("I/O error while trying to retrieve resource {}: {}", resource, e.getMessage());
            throw new DataRetrievalException("I/O error while trying to retrieve resource "+resource,e);
        } catch(RuntimeException ex) {
            log.error("Unknown error while trying to retrieve resource {}: {}", resource, ex.getMessage());
            throw new DataRetrievalException("Unknown error while trying to retrieve resource "+resource,ex);
        }

    }

    /**
     * Check whether the content type returned by the server is acceptable to the endpoint and data provider
     */
    protected boolean isValidContentType(String contentType, Endpoint endpoint) {
        if(endpoint != null && endpoint.getContentTypes().size() > 0) {
            ContentType parsed = parseContentType(contentType);
            for(ContentType valid : endpoint.getContentTypes()) {
                if(valid.matches(parsed) || valid.matchesWildcard(parsed)) {
                    return true;
                }
            }
            return false;
        } else {
            // TODO: should probably be removed, since it is not used
            for(String type : listMimeTypes()) {
                if(type.split(";")[0].equalsIgnoreCase(contentType)) return true;
            }
            return false;
        }
    }

    private class ResponseHandler implements org.apache.http.client.ResponseHandler<List<String>> {

        private Date             expiresDate;

        private String                requestUrl;

        // the repository where the triples will be stored in case the data providers return them
        private final Model triples;

        private final Endpoint   endpoint;

        private final String resource;

        private int httpStatus;

        public ResponseHandler(String resource, Endpoint endpoint) throws RepositoryException {
            this.resource = resource;
            this.endpoint = endpoint;

            triples = new TreeModel();
        }

        @Override
        public List<String> handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            ArrayList<String> requestUrls = new ArrayList<String>();

            if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 400) {
            	final HttpEntity entity = response.getEntity();
            	if (entity == null)
            		throw new IOException("no content returned by Linked Data resource " + resource);

	            if (!isValidContentType(entity.getContentType().getValue().split(";")[0], endpoint)) {
	                // FIXME: here was get.abort()
	            	throw new IOException("invalid content returned by Linked Data resource " + resource + ": "
	            			+ entity.getContentType().getValue());
	            }

                this.httpStatus = response.getStatusLine().getStatusCode();

                if (entity != null) {
                    String parseContentType = "application/rdf+xml";
                    if (endpoint != null && "SPARQL".equals(endpoint.getType())) {
                        parseContentType = "application/sparql-results+xml";
                    } else if (entity.getContentType() != null) {
                        parseContentType = entity.getContentType().getValue().split(";")[0];
                    }

                    InputStream in = entity.getContent();
                    try {

                        List<String> urls = parseResponse(resource, requestUrl, triples, in, parseContentType);
                        requestUrls.addAll(urls);

                        if (expiresDate == null) {
                            Header expires = response.getFirstHeader("Expires");
                            if (expires != null) {
                                try {
                                    expiresDate = DateUtils.parseDate(expires.getValue());
                                } catch (DateParseException e) {
                                    log.debug("error parsing Expires: header");
                                }
                            }
                        }

                    } catch (DataRetrievalException e) {
                        // FIXME: get.abort();
                        throw new IOException(e);
                    } finally {
                        in.close();
                    }
                }
                EntityUtils.consume(entity);
            } else if(response.getStatusLine().getStatusCode() == 500 || response.getStatusLine().getStatusCode() == 503  || response.getStatusLine().getStatusCode() == 504) {
                this.httpStatus = response.getStatusLine().getStatusCode();

                Header retry = response.getFirstHeader("Retry-After");
                if(retry != null) {
                    try {
                        int duration = Integer.parseInt(retry.getValue());
                        expiresDate = new Date(System.currentTimeMillis() + duration*1000);
                    } catch(NumberFormatException ex) {
                        log.debug("error parsing Retry-After: header");
                    }
                } else {
                    expiresDate = new Date(System.currentTimeMillis() + RETRY_AFTER *1000);
                }

            } else {
                log.error("the HTTP request failed (status: {})", response.getStatusLine());
                throw new ClientProtocolException("the HTTP request failed (status: " + response.getStatusLine() + ")");
            }

            return requestUrls;
        }

    }

}
