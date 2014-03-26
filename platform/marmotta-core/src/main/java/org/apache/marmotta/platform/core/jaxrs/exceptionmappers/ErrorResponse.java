/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.platform.core.jaxrs.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Represents a uniform error response for REST service requests.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@XmlRootElement(name = "Error")
public class ErrorResponse {

    private int code;

    private String message;

    private String stackTrace;

    public ErrorResponse() {
    }

    public ErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(int code, String message, String stackTrace) {
        this.code = code;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public ErrorResponse(int code, Exception ex) {
        this(code, ex.getMessage(), ex);
    }

    public ErrorResponse(int code, String message, Exception ex) {
        this.code = code;
        this.message = message;

        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));

        this.stackTrace = writer.toString();
    }

    @XmlElement(name = "code", required = true)
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @XmlElement(name = "message", required = true)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @XmlElement(name = "stackTrace", required = false)
    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public static Response errorResponse(Response.Status status, String message) {
        ErrorResponse entity = new ErrorResponse(status.getStatusCode(), message);
        return Response.status(status).entity(entity).build();
    }

    public static Response errorResponse(Response.Status status, Exception ex) {
        ErrorResponse entity = new ErrorResponse(status.getStatusCode(), ex);
        return Response.status(status).entity(entity).build();
    }

    public static Response errorResponse(Response.Status status, String message, Exception ex) {
        ErrorResponse entity = new ErrorResponse(status.getStatusCode(), message);

        return Response.status(status).entity(entity).build();
    }


    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("Code: ");
        b.append(code);
        b.append(System.getProperty("line.separator"));
        b.append("Message: ");
        b.append(message);
        if(stackTrace != null) {
            b.append(System.getProperty("line.separator"));
            b.append("StackTrace: ");
            b.append(stackTrace);
        }
        return b.toString();
    }
}
