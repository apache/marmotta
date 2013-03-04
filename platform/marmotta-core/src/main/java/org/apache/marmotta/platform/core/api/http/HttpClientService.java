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
package org.apache.marmotta.platform.core.api.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public interface HttpClientService {

    /**
     * Execute the {@link HttpRequestBase} passing the response to the provided
     * {@link ResponseHandler}. Connection management is done by the {@link HttpClientService}
     * 
     * @param request the HttpRequest to execute
     * @param handler the {@link ResponseHandler} for the response
     * @return whatever the {@link ResponseHandler} builds.
     * 
     * @see HttpClient#execute(HttpUriRequest, ResponseHandler)
     */
    public <T> T execute(HttpRequestBase request, ResponseHandler<? extends T> handler) throws ClientProtocolException, IOException;

    /**
     * Execute the {@link HttpRequestBase}.
     * 
     * <b>ATTENTION</b> To prevent memory leaks, make sure to close the {@link InputStream} in the
     * {@link HttpEntity}, or
     * use {@link #cleanupResponse(HttpResponse)}!
     * 
     * @param request the HttpRequest to execute.
     * @return the {@link HttpResponse} of the request. <b>ATTENTION</b> To prevent memory leaks,
     *         make sure to close the {@link InputStream} in the {@link HttpEntity}, or
     *         use {@link #cleanupResponse(HttpResponse)}!
     * @see #cleanupResponse(HttpResponse)
     */
    public HttpResponse execute(HttpRequestBase request) throws ClientProtocolException, IOException;

    /**
     * Close and release all resources of the provided {@link HttpResponse}.
     * 
     * @param response the {@link HttpResponse} to clean up.
     * @see org.apache.http.util.EntityUtils#consume(HttpEntity)
     */
    public void cleanupResponse(HttpResponse response);

    /**
     * Convenience method to execute a <code>GET</code>-request.
     * 
     * @param requestUrl the request URL
     * @param responseHandler the {@link ResponseHandler} for the request
     * @return whatever the {@link ResponseHandler} builds.
     */
    public <T> T doGet(String requestUrl, ResponseHandler<? extends T> responseHandler) throws IOException;

    /**
     * Convenience method to execute a <code>GET</code>-request, returning the response-entity as
     * {@link String}.
     * 
     * @param requestUrl the request URL
     * @return the response-entity.
     */
    public String doGet(String requestUrl) throws IOException;

    /**
     * Convenience method to execute a <code>POST</code>-request.
     * 
     * @param requestUrl the request URL
     * @param body the (body) content of the <code>POST</code>-request
     * @param responseHandler the {@link ResponseHandler} for the request
     * @return whatever the {@link ResponseHandler} builds.
     */
    public <T> T doPost(String requestUrl, HttpEntity body, ResponseHandler<? extends T> responseHandler) throws IOException;

    /**
     * Convenience method to execute a <code>POST</code>-request, returning the response-entity as
     * {@link String}.
     * 
     * @param url the request URL
     * @param body the (body) content of the <code>POST</code>-request
     * @return the response-entity.
     */
    public String doPost(String url, String body) throws IOException;

    /**
     * Convenience method to execute a <code>PUT</code>-request.
     * 
     * @param requestUrl the request URL
     * @param body the (body) content of the <code>PUT</code>-request
     * @param responseHandler the {@link ResponseHandler} for the request
     * @return whatever the {@link ResponseHandler} builds.
     */
    public <T> T doPut(String url, HttpEntity body, ResponseHandler<? extends T> responseHandler) throws IOException;

    /**
     * Convenience method to execute a <code>PUT</code>-request, returning the response-entity as
     * {@link String}.
     * 
     * @param url the request URL
     * @param body the (body) content of the <code>PUT</code>-request
     * @return the response-entity.
     */
    public String doPut(String url, String body) throws IOException;

    /**
     * Convenience method to execute a <code>DELETE</code>-request.
     * 
     * @param requestUrl the request URL
     * @param responseHandler the {@link ResponseHandler} for the request
     * @return whatever the {@link ResponseHandler} builds.
     */
    public <T> T doDelete(String url, ResponseHandler<? extends T> responseHandler) throws IOException;

    /**
     * Convenience method to execute a <code>DELETE</code>-request, returning the response code.
     * 
     * @param url the request URL
     * @return the http response code (e.g. <code>200</code> for success)
     * @see org.apache.http.HttpStatus
     */
    public int doDelete(String url) throws IOException;

    /**
     * Convenience method to execute a <code>HEAD</code>-request.
     * 
     * @param requestUrl the request URL
     * @param responseHandler the {@link ResponseHandler} for the request
     * @return whatever the {@link ResponseHandler} builds.
     */
    public <T> T doHead(String url, ResponseHandler<? extends T> responseHandler) throws IOException;

    /**
     * Convenience method to execute a <code>HEAD</code>-request, returning the last-modified date.
     * 
     * @param url the request URL
     * @return the value Last-Modified Header, or <code>null</code> if the header was missing or
     *         could not be parsed.
     */
    public Date doHead(String url) throws IOException;

    /**
     * Get a ready-to-use {@link HttpClient}.
     */
    public HttpClient getHttpClient();

}
