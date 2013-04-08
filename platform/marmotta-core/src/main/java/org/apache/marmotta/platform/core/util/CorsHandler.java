/*
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

package org.apache.marmotta.platform.core.util;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class CorsHandler {

    private static final String[] CORS_HEADERS= {
            "Access-Control-Allow-Origin",
            "Access-Control-Expose-Headers",
            "Access-Control-Max-Age",
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers"};

    /**
     * This method sets the response headers for CORS request. The options may contain following fields:
     * <ul>
     * <li>
     *     "Access-Control-Allow-Origin" : List&lt;String&gt; | "*"
     * </li>
     * <li>
     *     "Access-Control-Expose-Headers" : List&lt;String&gt;
     * </li>
     * <li>
     *     "Access-Control-Max-Age" : long
     * </li>
     * <li>
     *    "Access-Control-Allow-Credentials" : boolean
     * </li>
     * <li>
     *     "Access-Control-Allow-Methods" : List&lt;String&gt;
     * </li>
     * <li>
     *     "Access-Control-Allow-Headers" : List&lt;String&gt;
     * </li>
     * </ul>
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param options the options
     */
    public static void run(HttpServletRequest request, HttpServletResponse response, Map<String,Object> options) {

        //remove all existing cors headers
        for(String header : CORS_HEADERS) {
            response.setHeader(header,null);
        }

        //add headers from options
        for(String key : options.keySet()) {
            response.addHeader(key,buildHeader(options.get(key)));
        }

    }

    private static String buildHeader(Object value) {
        if(value instanceof List) {
            return StringUtils.join((List)value,",");
        }
        return value.toString();
    }

}
