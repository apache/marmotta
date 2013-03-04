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
package org.apache.marmotta.commons.sesame.repository;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.sail.SailConnection;

/**
 * A special form of Sesame RepositoryConnection that allows listing resources more efficiently than by listing all
 * statements (e.g. by direct database access).
 * 
 * @author Sebastian Schaffert
 */
public interface ResourceConnection extends SailConnection {

    /**
     * Return an iterator over the resources contained in this repository.
     * @return
     */
    RepositoryResult<Resource> getResources() throws RepositoryException;

    /**
     * Return an iterator over the resources contained in this repository matching the given prefix.
     * @return
     */
    RepositoryResult<URI> getResources(String prefix) throws RepositoryException;

    /**
     * Return the Sesame URI with the given uri identifier if it exists, or null if it does not exist.
     *
     * @param uri
     * @return
     */
    URI getURI(String uri);

    /**
     * Return the Sesame BNode with the given anonymous ID if it exists, or null if it does not exist.
     * @param id
     * @return
     */
    BNode getBNode(String id);

    /**
     * Remove the resource given as argument from the triple store and the resource repository.
     * @param resource
     */
    void removeResource(Resource resource);
    
}
