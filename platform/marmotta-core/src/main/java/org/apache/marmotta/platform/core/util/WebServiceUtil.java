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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.ws.rs.Path;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for web services
 * <p/>
 * Author: Sebastian Schaffert
 */
public class WebServiceUtil {


    /**
     * Create a JSON representation of an exception, to be returned to the client. The JSON object will be formatted
     * as follows:
     * <code>
     * {
     *     type: 'JAVA CLASS NAME',
     *     message: 'EXCEPTION MESSAGE'
     * }
     * </code>
     */
    public static String jsonErrorResponse(Exception e) {
        Map<String,Object> result = new HashMap<>();
        result.put("type", e.getClass().getSimpleName());
        result.put("message", e.getMessage());
        result.put("trace", ExceptionUtils.getStackTrace(e));

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(result);
        } catch (IOException ex) {
            // cannot occur, we write to a string
            return null;
        }
    }

    /**
     * Create a JSON representation of an exception, to be returned to the client. The JSON object will be formatted
     * as follows:
     * <code>
     * {
     *     type: 'JAVA CLASS NAME',
     *     message: 'EXCEPTION MESSAGE'
     * }
     * </code>
     */
    public static void jsonErrorResponse(Exception ex, OutputStream out) throws IOException {
        Map<String,Object> result = new HashMap<>();
        result.put("type",ex.getClass().getSimpleName());
        result.put("message",ex.getMessage());

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(out,result);

    }

    /**
     * Get the resource's path
     *
     * @param resource target resource
     * @return path
     */
    public static String getResourcePath(Object resource) {
        try {
            return (String) ReflectionUtils.getAnnotationValue(resource, Path.class, "value");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
