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
package org.apache.marmotta.platform.security.model;

/**
 * The HTTP methods supported by the access control filter
 * <p/>
 * Author: Sebastian Schaffert
 */
public enum HTTPMethods {
    GET, PUT, POST, DELETE, OPTIONS, HEAD;

    public static HTTPMethods parse(String s) {
        if("GET".equalsIgnoreCase(s)) {
            return HTTPMethods.GET;
        } else if("PUT".equalsIgnoreCase(s)) {
            return HTTPMethods.PUT;
        } else if("POST".equalsIgnoreCase(s)) {
            return HTTPMethods.POST;
        } else if("DELETE".equalsIgnoreCase(s)) {
            return HTTPMethods.DELETE;
        } else if("OPTIONS".equalsIgnoreCase(s)) {
            return HTTPMethods.OPTIONS;
        } else if("HEAD".equalsIgnoreCase(s)) {
            return HTTPMethods.HEAD;
        } else {
            return null;
        }
    }
}

