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

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.SailException;

/**
 * Offers access to the Sesame repository underlying this LMF instance. The activation/deactivation methods
 * of this service make sure the repository is properly initialised and shut down.
 * <p/>
 * Usage: to access the triple store properly through Sesame, you should follow the following
 * pattern:
 * <pre>
 *     RespositoryConnection con = sesameService.getConnection();
 *
 *     URI subject = con.getValueFactory().createURI(...);
 *     ...
 *     RepositoryResult&lt;Statement> result = con.getStatemenrs(subject,predicate,object,inferred,context);
 *     while(result.hasNext()) {
 *         Statement triple = result.next();
 *         ...
 *     }
 *
 *     con.close();
 * </pre>
 *
 * <p/>
 * Will replace the existing TripleStore at some point.
 * <p/>
 * Author: Sebastian Schaffert
 */
public interface SesameService {

    /**
     * Initialise the Sesame repository. Should be called on service activation.
     */
    void initialise();


    /**
     * Shutdown the Sesame repository. Should be called on service deactivation.
     */
    void shutdown();


    /**
     * Return the Sesame Repository underlying this service. Callers should be careful with modifying
     * this object directly.
     *
     * @return the Sesame Repository instance used by this service
     */
    SailRepository getRepository();

    /**
     * Return a Sesame RepositoryConnection to the underlying repository.
     *
     * @return a RepositoryConnection to the underlying Sesame repository.
     */
    RepositoryConnection getConnection() throws RepositoryException;

    /**
     * Return a Sesame ValueFactory for creating new RDF objects.
     *
     * @return the Sesame ValueFactory belonging to the repository that is used by the service
     */
    @Deprecated
    ValueFactory getValueFactory();

    void restart();

    /**
     * Run the triple store garbage collector manually and clean up unreferenced nodes and triples.
     * @throws org.openrdf.sail.SailException
     */
    void garbageCollect() throws SailException;
}
