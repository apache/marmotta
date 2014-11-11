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

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.util.EntityUtils;
import org.apache.marmotta.client.ClientConfiguration;
import org.apache.marmotta.client.exception.ContentFormatException;
import org.apache.marmotta.client.exception.MarmottaClientException;
import org.apache.marmotta.client.exception.NotFoundException;
import org.apache.marmotta.client.model.content.Content;
import org.apache.marmotta.client.model.content.StreamContent;
import org.apache.marmotta.client.model.meta.Metadata;
import org.apache.marmotta.client.util.HTTPUtil;
import org.apache.marmotta.client.util.RDFJSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ResourceClient {

    private static Logger log = LoggerFactory.getLogger(ResourceClient.class);

    private static final String URL_RESOURCE_SERVICE = "/resource";
    
    private ClientConfiguration config;

    public ResourceClient(ClientConfiguration config) {
        this.config = config;

    }

    /**
     * Create a resource in the remote Marmotta installation
     * @param uri
     * @return
     * @throws IOException
     */
    public boolean createResource(String uri) throws IOException {
        HttpClient httpClient = HTTPUtil.createClient(config);
            
        HttpPost post = new HttpPost(getServiceUrl(uri));
        
        try {
            
            HttpResponse response = httpClient.execute(post);
            
            switch(response.getStatusLine().getStatusCode()) {
                case 200: 
                    log.debug("resource {} already existed, not creating new",uri);
                    return true;
                case 201: 
                    log.debug("resource {} created",uri);
                    return true;
                default:
                    log.error("error creating resource {}: {} {}",new Object[] {uri,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    return true;
            }
            
        } catch (UnsupportedEncodingException e) {
            log.error("could not encode URI parameter",e);
            return false;
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Test whether the resource with the provided URI exists.
     * <p/>
     * Uses an OPTIONS call to the resource web service to determine whether the resource exists or not
     *
     * @param uri
     * @return
     * @throws IOException
     */
    public boolean existsResource(String uri) throws IOException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        HttpOptions options = new HttpOptions(getServiceUrl(uri));
        
        try {
                
            HttpResponse response = httpClient.execute(options);

            if(response.containsHeader("Access-Control-Allow-Methods") && response.getFirstHeader("Access-Control-Allow-Methods").getValue().equals("POST")) {
                return false;
            } else if(response.containsHeader("Access-Control-Allow-Methods") && response.getFirstHeader("Access-Control-Allow-Methods").getValue().contains("GET")) {
                return true;
            } else {
                log.warn("OPTIONS response did not contain a access-control-allow-methods header");
                return false; 
            }

        } catch (UnsupportedEncodingException e) {
            log.error("could not encode URI parameter",e);
            return false;
        } finally {
            options.releaseConnection();
        }
    }

    /**
     * Return the resource metadata for the resource with the given URI, if it exists. Returns null if the
     * resource exists but there is no metadata. Throws an exception if the resource does not exist or some
     * other error code was returned.
     *
     * @param uri
     * @return
     * @throws IOException
     * @throws MarmottaClientException
     */
    public Metadata getResourceMetadata(String uri) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        HttpGet get = new HttpGet(getServiceUrl(uri));
        get.setHeader(ACCEPT, "application/rdf+json; rel=meta");
        
        try {
            
            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("metadata for resource {} retrieved",uri);
                    Map<String,Metadata> result = RDFJSONParser.parseRDFJSON(response.getEntity().getContent());
                    if(result.containsKey(uri)) {
                        return result.get(uri);
                    } else {
                        return null;
                    }
                case 406:
                    log.error("server does not support metadata type application/rdf+json for resource {}, cannot retrieve", uri);
                    throw new ContentFormatException("server does not support metadata type application/rdf+json for resource "+uri);
                case 404:
                    log.error("resource {} does not exist, cannot retrieve", uri);
                    throw new NotFoundException("resource "+uri+" does not exist, cannot retrieve");
                default:
                    log.error("error retrieving resource {}: {} {}",new Object[] {uri,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error retrieving resource "+uri+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } catch (UnsupportedEncodingException e) {
            log.error("could not encode URI parameter",e);
            throw new MarmottaClientException("could not encode URI parameter");
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Update (overwrite) the metadata of the resource identified by the given uri. The metadata will be serialised to
     * application/json and sent to the Apache Marmotta server. The given metadata will override any metadata
     * for the resource already existing on the server. The resource has to exist or be created before updating, otherwise
     * the method will throw a NotFoundException.
     *
     * @param uri        the URI of the resource to update
     * @param metadata   the metadata to upload to the resource
     * @throws IOException
     * @throws MarmottaClientException
     */
    public void updateResourceMetadata(final String uri, final Metadata metadata) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        HttpPut put = new HttpPut(getServiceUrl(uri));
        put.setHeader(CONTENT_TYPE, "application/rdf+json; rel=meta");
        ContentProducer cp = new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                RDFJSONParser.serializeRDFJSON(ImmutableMap.of(uri, metadata), outstream);
            }
        };
        put.setEntity(new EntityTemplate(cp));

        try {
            
            HttpResponse response = httpClient.execute(put);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("metadata for resource {} updated",uri);
                    break;
                case 415:
                    log.error("server does not support metadata type application/json for resource {}, cannot update", uri);
                    throw new ContentFormatException("server does not support metadata type application/json for resource "+uri);
                case 404:
                    log.error("resource {} does not exist, cannot update", uri);
                    throw new NotFoundException("resource "+uri+" does not exist, cannot update");
                default:
                    log.error("error updating resource {}: {} {}",new Object[] {uri,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error updating resource "+uri+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } catch (UnsupportedEncodingException e) {
            log.error("could not encode URI parameter",e);
            throw new MarmottaClientException("could not encode URI parameter");
        } finally {
            put.releaseConnection();
        }
    }

    /**
     * Retrieve the (human-readable) content of the given mimeType of the given resource. Will return a content
     * object that allows reading the input stream. In case no content of the given mime type exists for the resource,
     * will throw a ContentFormatException.
     *
     * @param uri
     * @param mimeType
     * @return
     * @throws IOException
     * @throws MarmottaClientException
     */
    public Content getResourceContent(String uri, String mimeType) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        HttpGet get = new HttpGet(getServiceUrl(uri));
        get.setHeader(ACCEPT, mimeType + "; rel=content");
        
        try {

            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("metadata for resource {} retrieved",uri);
                    Content content = new StreamContent(response.getEntity().getContent(),response.getEntity().getContentType().getValue(),response.getEntity().getContentLength());
                    return content;
                case 406:
                    log.error("server does not offer content type {} for resource {}, cannot retrieve", mimeType, uri);
                    throw new ContentFormatException("server does not offer content type "+mimeType+" for resource "+uri);
                case 404:
                    log.error("resource {} does not exist, cannot retrieve content", uri);
                    throw new NotFoundException("resource "+uri+" does not exist, cannot retrieve");
                default:
                    log.error("error retrieving resource {}: {} {}",new Object[] {uri,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error retrieving resource "+uri+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } catch (UnsupportedEncodingException e) {
            log.error("could not encode URI parameter",e);
            throw new MarmottaClientException("could not encode URI parameter");
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Update the content of the resource identified by the URI given as argument. The resource has to exist before
     * content can be uploaded to it. Any existing content will be overridden. The stream of the content object
     * will be consumed by this method. Throws ContentFormatException if the content type is not supported,
     * NotFoundException if the resource does not exist.
     * @param uri
     * @param content
     * @throws IOException
     * @throws MarmottaClientException
     */
    public void updateResourceContent(final String uri, final Content content) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        HttpPut put = new HttpPut(getServiceUrl(uri));
        put.setHeader(CONTENT_TYPE, content.getMimeType() + "; rel=content");
        ContentProducer cp = new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                ByteStreams.copy(content.getStream(),outstream);
            }
        };
        put.setEntity(new EntityTemplate(cp));
        
        ResponseHandler<Boolean> handler = new ResponseHandler<Boolean>() {
            @Override
            public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                EntityUtils.consume(response.getEntity());
                switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("content for resource {} updated",uri);
                    return true;
                case 406:
                    log.error("server does not support content type {} for resource {}, cannot update", content.getMimeType(),uri);
                    return false;
                case 404:
                    log.error("resource {} does not exist, cannot update", uri);
                    return false;
                default:
                    log.error("error updating resource {}: {} {}",new Object[] {uri,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    return false;
                }
            }
        };

        try {
            httpClient.execute(put, handler);
        } catch(IOException ex) {
            put.abort();
            throw ex;
        } finally {
            put.releaseConnection();
        }

    }
    
    
    public void deleteResource(String uri) throws IOException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        HttpDelete delete = new HttpDelete(getServiceUrl(uri));
        
        try {
            
            HttpResponse response = httpClient.execute(delete);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("resource {} deleted",uri);
                    break;
                case 400:
                    log.error("resource {} invalid, cannot delete", uri);
                    break;
                case 404:
                    log.error("resource {} does not exist, cannot delete", uri);
                    break;
                default:
                    log.error("error deleting resource {}: {} {}",new Object[] {uri,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
            }

        } catch (UnsupportedEncodingException e) {
            log.error("could not encode URI parameter",e);
        } finally {
            delete.releaseConnection();
        }
    }
    
    private String getServiceUrl(String uri) throws UnsupportedEncodingException {
        return config.getMarmottaUri() + URL_RESOURCE_SERVICE + "?uri=" + URLEncoder.encode(uri,"utf-8");    
    }
    
}
