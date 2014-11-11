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
package org.apache.marmotta.platform.core.exception;

import com.google.common.collect.ImmutableMap;
import edu.emory.mathcs.backport.java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.net.HttpHeaders.ACCEPT;

/**
 * HTTP Error Exception
 *
 * @author Sergio Fernández
 */
public class HttpErrorException extends Exception {

    private final int status;

    private final String reason;

    private final String uri;

    private final Map<String, String> headers;

    /**
     * Constructs an instance with the specified details
     *
     * @param status http status code
     * @param reason reason phrase
     * @param uri resource uri
     * @param msg message
     */
    public HttpErrorException(int status, String reason, String uri, String msg) {
        this(status, reason, uri, msg, new HashMap<String,String>());
    }

    /**
     * Constructs an instance with the specified details
     *
     * @param status http status code
     * @param uri resource uri
     * @param msg message
     */
    public HttpErrorException(Response.Status status, String uri, String msg) {
        this(status.getStatusCode(), status.getReasonPhrase(), uri, msg);
    }

    /**
     * Constructs an instance with the specified details
     *
     * @param status http status code
     * @param uri resource uri
     * @param msg message
     * @param headers custom headers
     */
    public HttpErrorException(Response.Status status, String uri, String msg, Map<String,String> headers) {
        this(status.getStatusCode(), status.getReasonPhrase(), uri, msg, headers);
    }

    /**
     * Constructs an instance with the specified details
     *
     * @param status http status code
     * @param request http servlet request
     * @param e exception
     */
    public HttpErrorException(Response.Status status, HttpServletRequest request, Exception e) {
        this(status.getStatusCode(), status.getReasonPhrase(), request.getRequestURI(), e.getMessage(), ImmutableMap.of(ACCEPT, request.getHeader(ACCEPT)));
    }

    /**
     * Constructs an instance with the specified details
     *
     * @param status http status code
     * @param reason reason phrase
     * @param uri resource uri
     * @param msg message
     * @param headers custom headers
     */
    public HttpErrorException(int status, String reason, String uri, String msg, Map<String, String> headers) {
        super(msg);
        this.status = status;
        this.reason = reason;
        this.uri = uri;
        this.headers = new HashMap<String,String>(headers);
    }

    public int getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

}
