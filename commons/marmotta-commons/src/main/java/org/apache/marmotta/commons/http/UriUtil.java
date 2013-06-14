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
package org.apache.marmotta.commons.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import java.net.URI;

/**
 * Uri utilities
 * 
 * @author Sergio Fernández
 * 
 */
public class UriUtil {

    private static final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES | UrlValidator.ALLOW_LOCAL_URLS);

    /**
     * Validates URI
     * (method wrappering different actual implementations)
     * 
     * @param uri uri
     * @return valid
     */
    public static boolean validate(String uri) {
        // return StringUtils.isNotBlank(uri) && validateApache(uri);
        return StringUtils.isNotBlank(uri) && validateJavaNet(uri);
    }

    /**
     * Validates URI using Apache validator
     * 
     * @param uri uri
     * @return valid
     */
    public static boolean validateApache(String uri) {
        return urlValidator.isValid(uri);
    }

    /**
     * Validates URI just trying to build a java.net.URI object
     * 
     * @param uri uri
     * @return valid
     */
    public static boolean validateJavaNet(String uri) {
        try {
            URI actualUri = URI.create(uri);
            return StringUtils.isNotBlank(actualUri.getScheme());
        } catch (Exception e1) {
            return false;
        }
    }

    /**
     * Get the namespace of a URI
     * 
     * @param uri uri
     * @return namespace
     */
    public static String getNamespace(String uri) {
        uri = uri.trim();
        String last = uri.substring(uri.length() - 1);
        if ("#".equals(last) || "/".equals(last))
            return null;
        else {
            if (uri.contains("#")) // hash namespace
                return uri.split("#")[0] + "#";
            else { // slash namespace
                int index = uri.lastIndexOf('/');
                return uri.substring(0, index + 1);
            }
        }
    }

    /**
     * Get the reference of a URI
     * 
     * @param uri uri
     * @return reference
     */
    public static String getReference(String uri) {
        uri = uri.trim();
        String last = uri.substring(uri.length() - 1);
        if ("#".equals(last) || "/".equals(last))
            return null;
        else {
            if (uri.contains("#")) // hash namespace
                return uri.split("#")[1];
            else { // slash namespace
                int index = uri.lastIndexOf('/');
                return uri.substring(index + 1);
            }
        }
    }

}
