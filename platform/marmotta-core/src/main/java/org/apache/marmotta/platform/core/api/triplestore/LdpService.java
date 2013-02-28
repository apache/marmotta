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
package org.apache.marmotta.platform.core.api.triplestore;

import java.net.URISyntaxException;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

/**
 * Interface for supporting LDP (WIP)
 * 
 * @see http://www.w3.org/TR/ldp/
 * @author Sergio Fernández
 *
 */
public interface LdpService {
    
    static final String DEFAULT_PREFIX = "container";
    
    /**
     * Get the base context URI
     * 
     * @return base context
     */
    String getBaseContainer();
    
    /**
     * Lists the current container
     * 
     * @return list of containers
     */
    List<URI> list();
    
    /**
     * Return the URI for this LDPR
     * 
     * @param container plain text URI
     * @return container's URI, null if does not exit
     */
    URI get(String resource);
    
    /**
     * Creates a new LDPR
     * 
     * @param container plain text URI
     * @return URI of the new container if success
     * @throws URISyntaxException 
     */
    boolean create(String resource) throws URISyntaxException;
    
    /**
     * Creates a new LDPR defining a title for it
     * 
     * @param container plain text URI
     * @return URI of the new container if success
     * @throws URISyntaxException 
     */
    boolean create(String resource, String title) throws URISyntaxException;
    
    /**
     * Delete this LDPR
     * 
     * @param resource uri
     * @return
     * @throws RepositoryException 
     */
    boolean delete(String resource) throws RepositoryException;

}
