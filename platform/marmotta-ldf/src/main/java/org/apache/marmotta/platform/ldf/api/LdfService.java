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
package org.apache.marmotta.platform.ldf.api;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;

/**
 * Linked Media Fragments service
 *
 * @author Sergio Fernández
 */
public interface LdfService {

    final static int PAGE_SIZE = 100;

    /**
     * Gets a fragment matching the specified triple fragment pattern
     * specified (null values are wildcards).
     *
     * @param subject fragment subject
     * @param predicate fragmnent predicate
     * @param object fragment object
     * @param page number of page (starting with 1)
     * @return fragment
     */
    Model getFragment(String subject, String predicate, String object, int page, java.net.URI uri) throws RepositoryException, IllegalArgumentException;

    /**
     * Gets a fragment matching the specified triple fragment pattern
     * specified (null values are wildcards).
     *
     * @param subject fragment subject
     * @param predicate fragmnent predicate
     * @param object fragment object
     * @param page number of page (starting with 1)
     * @param uri uri requested
     * @return fragment
     */
    Model getFragment(URI subject, URI predicate, Value object, int page, java.net.URI uri) throws RepositoryException, IllegalArgumentException;

    /**
     * Gets a fragment matching the specified quad fragment pattern
     * specified (null values are wildcards).
     *
     * @param subject fragment subject
     * @param predicate fragmnent predicate
     * @param object fragment object
     * @param context named graph
     * @param page number of page (starting with 1)
     * @param uri uri requested
     * @return fragment
     */
    Model getFragment(String subject, String predicate, String object, String context, int page, java.net.URI uri) throws RepositoryException, IllegalArgumentException;

    /**
     * Gets a fragment matching the specified quad fragment pattern
     * specified (null values are wildcards).
     *
     * @param subject fragment subject
     * @param predicate fragmnent predicate
     * @param object fragment object
     * @param context named graph
     * @param page number of page (starting with 1)
     * @param uri uri requested
     * @return fragment
     */
    Model getFragment(URI subject, URI predicate, Value object, Resource context, int page, java.net.URI uri) throws RepositoryException, IllegalArgumentException;

}
