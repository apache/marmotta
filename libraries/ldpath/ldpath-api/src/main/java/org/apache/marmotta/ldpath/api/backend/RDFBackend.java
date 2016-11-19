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
package org.apache.marmotta.ldpath.api.backend;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A generic API for RDF models and triple stores; provides the testing and navigation functions needed for LDPath.
 * Implementations exist for Sesame, Jena and Clerezza. The API is somewhat typesafe by making use of a generic
 * for nodes.
 * 
 * @param <Node> most generic type of a Node (e.g. Value in Sesame).
 */
public interface RDFBackend<Node> extends NodeBackend<Node> {

	/**
	 * @deprecated subject to be removed in the next release
	 */
	@Deprecated
    boolean supportsThreading();
	
	/**
	 * @deprecated subject to be removed in the next release
	 */
	@Deprecated
    ThreadPoolExecutor getThreadPool();


    /**
     * List the objects of triples in the triple store underlying this backend that have the subject and
     * property given as argument.
     *
     * @param subject  the subject of the triples to look for
     * @param property the property of the triples to look for, <code>null</code> is interpreted as wildcard
     * @return all objects of triples with matching subject and property
     */
    Collection<Node> listObjects(Node subject, Node property);


    /**
     * List the subjects of triples in the triple store underlying this backend that have the object and
     * property given as argument.
     *
     * @param object  the object of the triples to look for
     * @param property the property of the triples to look for, <code>null</code> is interpreted as wildcard
     * @return all subjects of triples with matching object and property
     * @throws UnsupportedOperationException in case reverse selection is not supported (e.g. when querying Linked Data)
     */
    Collection<Node> listSubjects(Node property, Node object);

}
