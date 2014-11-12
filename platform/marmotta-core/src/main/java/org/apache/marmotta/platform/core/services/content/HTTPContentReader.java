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
package org.apache.marmotta.platform.core.services.content;

import com.google.common.io.ByteStreams;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.marmotta.commons.sesame.facading.FacadingFactory;
import org.apache.marmotta.platform.core.api.content.ContentReader;
import org.apache.marmotta.platform.core.api.http.HttpClientService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.model.content.MediaContentItem;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class HTTPContentReader implements ContentReader {

    @Inject
    private Logger log;

    @Inject
    private SesameService sesameService;

    @Inject
    private HttpClientService    httpClientService;

    public HTTPContentReader() {
    }

    @PostConstruct
    public void initialise() {
        log.debug("HTTP Content Reader started");

    }


    /**
     * Retrieve the content of the specified mime type for the specified resource. Returns a byte array containing
     * the byte data of the content, or null, indicating that a content of the specified mime type does not exist
     * for the resource.
     * <p/>
     * Uses the property kiwi:hasContentLocation to access the URL for the resource content. If this property is not
     * given and the resource is itself a HTTP URI resource, it will try to access the URI itself.
     *
     * @param resource the resource for which to return the content
     * @param mimetype the mime type to retrieve of the content
     * @return a byte array containing the content of the resource, or null if no content exists
     */
    @Override
    public byte[] getContentData(Resource resource, String mimetype) throws IOException {
        return ByteStreams.toByteArray(getContentStream(resource, mimetype));
    }

    /**
     * Return the name of the content reader. Used to identify and display the content reader to admin users.
     *
     * @return
     */
    @Override
    public String getName() {
        return "HTTP Content Reader";
    }

    /**
     * Retrieve the content of the specified mime type for the specified resource. Returns a input stream containing
     * the byte data of the content, or null, indicating that a content of the specified mime type does not exist
     * for the resource.
     * <p/>
     * Uses the property kiwi:hasContentLocation to access the URL for the resource content. If this property is not
     * given and the resource is itself a HTTP URI resource, it will try to access the URI itself.
     * <p/>
     * This method is preferrable for resources with large amounts of data.
     *
     * @param resource the resource for which to return the content
     * @param mimetype the mime type to retrieve of the content
     * @return a InputStream containing the content of the resource, or null if no content exists
     */
    @Override
    public InputStream getContentStream(final Resource resource, String mimetype) throws IOException {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                MediaContentItem mci = FacadingFactory.createFacading(conn).createFacade(resource, MediaContentItem.class);

                String location = mci.getContentLocation();

                // if no location is explicitly specified, use the resource URI itself
                if(location == null && resource instanceof URI && resource.stringValue().startsWith("http://")) {
                    location = resource.stringValue();
                }

                if(location != null) {
                    log.info("reading remote resource {}",location);
                    HttpGet get = new HttpGet(location);
                    get.setHeader(ACCEPT, mimetype);

                    HttpResponse response = httpClientService.execute(get);
                    if(response.getStatusLine().getStatusCode() == 200)
                        return response.getEntity().getContent();
                    else {
                        log.info("invalid status code while retrieving HTTP remote content for resource {}: {}",resource,response.getStatusLine());
                        return null;
                    }
                } else
                    return null;
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,FileSystemContentReader.class);
            return null;
        }
    }

    /**
     * Check whether the specified resource has content of the specified mimetype for this reader. Returns true
     * in this case, false otherwise.
     *
     * @param resource the resource to check
     * @param mimetype the mimetype to look for
     * @return true if content of this mimetype is associated with the resource, false otherwise
     */
    @Override
    public boolean hasContent(Resource resource, String mimetype) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                MediaContentItem mci = FacadingFactory.createFacading(conn).createFacade(resource, MediaContentItem.class);

                String location = mci.getContentLocation();

                // if no location is explicitly specified, use the resource URI itself
                if(location == null && resource instanceof URI && resource.stringValue().startsWith("http://")) {
                    location = resource.stringValue();
                }

                try {
                    if(location != null) {
                        log.info("reading remote resource {}",location);
                        HttpHead head = new HttpHead(location);
                        head.setHeader(ACCEPT, mimetype);

                        return httpClientService.execute(head, new ResponseHandler<Boolean>() {
                            @Override
                            public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                                return response.getStatusLine().getStatusCode() == 200;
                            }
                        });

                    } else
                        return false;
                } catch(IOException ex) {
                    return false;
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,FileSystemContentReader.class);
            return false;
        }
    }

    /**
     * Return the MIME content type of the resource passed as argument.
     *
     * @param resource resource for which to return the content type
     * @return the MIME content type of the resource
     */
    @Override
    public String getContentType(Resource resource) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                MediaContentItem mci = FacadingFactory.createFacading(conn).createFacade(resource, MediaContentItem.class);

                String location = mci.getContentLocation();

                // if no location is explicitly specified, use the resource URI itself
                if(location == null && resource instanceof URI && resource.stringValue().startsWith("http://")) {
                    location = resource.stringValue();
                }

                try {
                    if(location != null) {
                        log.info("reading remote resource {}",location);
                        HttpHead head = new HttpHead(location);

                        return httpClientService.execute(head, new ResponseHandler<String>() {
                            @Override
                            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                                if (response.getStatusLine().getStatusCode() == 200)
                                    return response.getFirstHeader(CONTENT_TYPE).getValue().split(";")[0];
                                else
                                    return null;
                            }
                        });
                    } else
                        return null;
                } catch(IOException ex) {
                    return null;
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,FileSystemContentReader.class);
            return null;
        }
    }

    /**
     * Return the number of bytes the content of this resource contains.
     *
     * @param resource resource for which to return the content length
     * @return byte count for the resource content
     */
    @Override
    public long getContentLength(Resource resource, String mimetype) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                MediaContentItem mci = FacadingFactory.createFacading(conn).createFacade(resource, MediaContentItem.class);

                String location = mci.getContentLocation();

                // if no location is explicitly specified, use the resource URI itself
                if(location == null && resource instanceof URI && resource.stringValue().startsWith("http://")) {
                    location = resource.stringValue();
                }

                try {
                    if(location != null) {
                        log.info("reading remote resource {}",location);
                        HttpHead head = new HttpHead(location);

                        return httpClientService.execute(head, new ResponseHandler<Long>() {

                            @Override
                            public Long handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                                if (response.getStatusLine().getStatusCode() == 200)
                                    return Long.parseLong(response.getFirstHeader("Content-Length").getValue());
                                else
                                    return 0l;
                            }
                        });
                    } else
                        return 0;
                } catch(Exception ex) {
                    return 0;
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,FileSystemContentReader.class);
            return 0;
        }
    }
}
