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
package org.apache.marmotta.commons.sesame.model;

import org.openrdf.model.URI;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class URICommons {

    /**
     * Return the cache key for the URI passed as argument.
     *
     * @param node a Sesame BNode for which to create a cache key
     * @return a string that can be used as cache key
     */
    public static String createCacheKey(URI node) {
        return node.stringValue();
    }


    /**
     * Return the cache key for the BNode ID passed as argument.
     *
     * @param uri the string representation of a Sesame URI  for which to create a cache key
     * @return a string that can be used as cache key
     */
    public static String createCacheKey(String uri) {
        return uri;
    }

    /**
     * Split a String URI into namespace and local name as described in the comment of {@link URI}
     *
     * @param uri the URI to split
     * @return a String array of length 2 where the first argument is the namespace, the second the local name
     */
    public static String[] splitNamespace(String uri) {
        // split according to algorithm in URI class
        String[] components = uri.split("#",2);

        String namespace, localName;
        if(components.length > 1) {
            namespace = components[0]+"#";
            localName = components[1];
        } else {
            components = uri.split("/");
            if(components.length > 1) {
                namespace = uri.substring(0,uri.length()-components[components.length-1].length());
                localName = components[components.length-1];
            } else {
                components = uri.split(":");

                namespace = uri.substring(0,uri.length()-components[components.length-1].length());
                localName = components[components.length-1];
            }
        }
        return new String[] {namespace,localName};
    }

}
