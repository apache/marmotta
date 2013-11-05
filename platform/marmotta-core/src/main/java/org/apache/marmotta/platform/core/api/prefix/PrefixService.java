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
package org.apache.marmotta.platform.core.api.prefix;

import java.net.URISyntaxException;
import java.util.Map;

/**
 * Prefix Manager definition
 * 
 * @author Sergio Fernández
 * 
 */
public interface PrefixService {

    /**
     * Check prefix mapping
     * 
     * @param prefix prefix
     * @return mapping exists
     */
    boolean containsPrefix(String prefix);

    /**
     * Check if there is registered any prefix for such namespace
     * 
     * @param namespace uri
     * @return mapping exists
     */
    boolean containsNamespace(String namespace);

    /**
     * Get namespace for such prefix
     * 
     * @param prefix prefix
     * @return namespace
     */
    String getNamespace(String prefix);

    /**
     * Get prefix which identifies this namespace
     * 
     * @param namespace uri
     * @return prefix
     */
    String getPrefix(String namespace);

    /**
     * Add a new prefix mapping
     * 
     * @param prefix prefix
     * @param namespace uri
     * @throws IllegalArgumentException when one of both is already mapped
     * @throws URISyntaxException 
     */
    void add(String prefix, String namespace) throws IllegalArgumentException, URISyntaxException;

    /**
     * Force addition of a new prefix mapping, even if it already exists
     * 
     * @param prefix prefix
     * @param namespace uri
     */
    void forceAdd(String prefix, String namespace);

    /**
     * Get all current mappings
     * 
     * @return mappings
     */
    Map<String, String> getMappings();
    
    /**
     * Delete a prefix mapping 
     * 
     * @param prefix
     * @return
     */
    boolean remove(String prefix);

    /**
     * Get the CURIE for this URI if possible
     * 
     * @param uri uri
     * @return curie
     */
    String getCurie(String uri);

    /**
     * Serializes the current mapping to the the syntax requited by vocab in HTML
     * (i.e., a white space separated list of prefix-name IRI pairs of the form)
     * 
     * @see http://www.w3.org/TR/rdfa-core/#A-prefix
     * @return prefixes mapping
     */
    String serializePrefixMapping();

    /**
     * Serializes all prefixes according the SPARQL syntax
     * 
     * @return prefixes declaration
     */
    String serializePrefixesSparqlDeclaration();

}
