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
package org.apache.marmotta.platform.core.api.triplestore;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;

/**
 * The context (named graphs in LMF, formerly "knowledge space" in KiWi) service offers convenience
 * functions for working with LMF Contexts. Low level manipulation of contexts is offered by
 * TripleStore.
 * <p/>
 * A context or (other name) named graph represent a own graph for a separation of the whole
 * data in the context. In other words: the relationship between triples and a context is
 * a 1 to N relationship. Every triple is though the context connect to exactly one context.
 * <p/>
 * Every context has own access rights, triples, reasoning rules and other metadata.
 * <p/>
 * You can create contexts for user, for imported ontologies, own created content, inferred
 * triples and system data
 * <p/>
 * every new triple without information of a context is connect with the context to the
 * default context
 * <p/>
 * 
 * @author Stefan
 * @author Sergio Fernández
 */
public interface ContextService {

    String DEFAULT_PREFIX = "context";
    
    //****************************************
    // get/create default, inferred
    //****************************************

    /**
     * Get the base context IRI
     * 
     * @return base context
     */
    String getBaseContext();

    /**
     * Return the set of contexts that is currently active for reading. The set of active contexts
     * is either selected explicitly in web service calls or it consists of all contexts.
     *
     * @return a set of IRIs indicating the active contexts
     */
    Set<IRI> getActiveContext();

    /**
     * Return the context used for storing system information.
     *
     * @return a IRI representing the system context
     * @throws URISyntaxException 
     */
    IRI getSystemContext() throws URISyntaxException;

    /**
     * Get the iri of the inferred context
     *
     * @return iri of this inferred context
     * @throws URISyntaxException 
     */
    IRI getInferredContext() throws URISyntaxException;

    /**
     * Get the iri of the default context
     *
     * @return
     * @throws URISyntaxException 
     */
    IRI getDefaultContext() throws URISyntaxException;

    /**
     * Get the iri of the context used for caching linked data
     * @return
     * @throws URISyntaxException 
     */
    IRI getCacheContext() throws URISyntaxException;

    /**
     * List all contexts currently available
     * 
     * @return
     */
    List<IRI> listContexts();
    
    /**
     * List all accepted formats to ingest content
     * 
     * @return
     */
    Set<String> getAcceptFormats();
    
    /**
     * List all contexts currently available
     * 
     * @param filter filter only the contexts using the normative base iri
     * @return
     */
    List<IRI> listContexts(boolean filter);

    /**
     * Create a new context with the given IRI or return the already existing context. Essentially
     * just calls resourceService.createUriResource, but sets some resource parameters correctly.
     *
     *
     * @param iri the iri of the context to create
     * @return a IRI representing the created context
     * @throws URISyntaxException 
     */
    IRI createContext(String iri) throws URISyntaxException;

    /**
     * Create a new context with the given IRI or return the already existing context. Essentially
     * just calls resourceService.createUriResource, but sets some resource parameters correctly.
     * 
     * @param iri
     * @param label
     * @return
     * @throws URISyntaxException 
     */
    IRI createContext(String iri, String label) throws URISyntaxException;

    /**
     * Return the context with the given IRI if it exists.
     *
     * @param context_iri
     * @return
     */
    IRI getContext(String context_iri);

    /**
     * Return a human-readable label for the context, either the rdfs:label or the last part of the IRI.
     *
     * @param context
     * @return
     */
    String getContextLabel(IRI context);

    /**
     * Return the number of triples for the context.
     * @param context
     */
    long getContextSize(org.eclipse.rdf4j.model.IRI context);
    
    /**
     * Import content into the context
     * 
     * @param context
     * @param is
     * @param format
     * @return
     */
    boolean importContent(String context, InputStream is, String format);

    /**
     * Remove (clean whole content) the context represented by this IRI
     * 
     * @param context iri
     * @return operation result, false if context does not exist
     */
    boolean removeContext(String context);

    /**
     * Remove (clean whole content) the context represented by this resource
     * 
     *
     * @param context resource
     * @return operation result, false if context does not exist
     */
    boolean removeContext(IRI context);


}
