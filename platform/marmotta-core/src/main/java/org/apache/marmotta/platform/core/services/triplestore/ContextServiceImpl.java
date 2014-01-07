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
package org.apache.marmotta.platform.core.services.triplestore;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.UriUtil;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.qualifiers.kspace.ActiveKnowledgeSpaces;
import org.apache.marmotta.platform.core.qualifiers.kspace.DefaultKnowledgeSpace;
import org.apache.marmotta.platform.core.qualifiers.kspace.InferredKnowledgeSpace;
import org.apache.marmotta.platform.core.qualifiers.kspace.SystemKnowledgeSpace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;

/**
 * The context (named graphs in Apache Marmotta, formerly "knowledge space" in KiWi) service offers convenience
 * functions for working with Apache Marmotta Contexts. Low level manipulation of contexts is offered by
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
@Named("knowledgeSpaceService")
@ApplicationScoped
public class ContextServiceImpl implements ContextService {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    @Inject
    private ImportService importService;

    @Inject
    private UserService userService;
    
//    @PostConstruct
//    public void initialize() {
//        log.debug("Creating default contexts...");
//        createContext(configurationService.getDefaultContext(), "default");
//        createContext(configurationService.getCacheContext(), "cache");
//        createContext(configurationService.getInferredContext(), "inferred");
//    }

    @Override
    public List<URI> listContexts() {
        return listContexts(false);
    }

    @Override
    public List<URI> listContexts(boolean filter) {
        List<URI> contexts = new ArrayList<URI>();
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                RepositoryResult<Resource> result = conn.getContextIDs();
                while(result.hasNext()) {
                    Resource next = result.next();
                    if(next instanceof URI) {
                        URI uri = (URI)next;
                        if (filter) {
                            if (uri.stringValue().startsWith(configurationService.getBaseContext())) {
                                contexts.add(uri);
                            }
                        } else {
                            contexts.add(uri);
                        }
                    }
                }
                result.close();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {

        }
        return contexts;
    }

    /**
     * Check the connection's namespace, using the base context when needed
     * 
     * @param conn
     * @throws RepositoryException
     */
    private void checkConnectionNamespace(RepositoryConnection conn)
            throws RepositoryException {
        if(conn.getNamespace(DEFAULT_PREFIX) == null) {
            conn.setNamespace(DEFAULT_PREFIX, getBaseContext());
        }
    }

    @Override
    public Set<String> getAcceptFormats() {
        return importService.getAcceptTypes();
    }

    //****************************************
    // get/create default, inferred
    //****************************************

    /**
     * Create a new context with the given URI or return the already existing context. Essentially
     * just calls resourceService.createUriResource, but sets some resource parameters correctly.
     *
     *
     * @param uri the uri of the context to create
     * @return a URI representing the created context, or null if the URI could not be created
     * @throws URISyntaxException 
     */
    @Override
    public URI createContext(String uri) throws URISyntaxException {
        return createContext(uri, null);
    }

    @Override
    public URI createContext(String uri, String label) throws URISyntaxException {
        if(uri == null) {
            return null;
        }
    	if (!UriUtil.validate(uri)) {
    		uri = configurationService.getBaseContext() + uri;
    		if (!UriUtil.validate(uri)) {
    			throw new URISyntaxException(uri, "not valid context uri");
    		}
    	}
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                checkConnectionNamespace(conn);
                ValueFactory valueFactory = conn.getValueFactory();
				URI ctx = valueFactory.createURI(uri);
                if (StringUtils.isNotBlank(label)) {
                    conn.add(ctx, RDFS.LABEL, Literals.createLiteral(valueFactory, label), ctx);
                }
                return ctx;
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex,ContextServiceImpl.class);
        }
        return null;
    }

    @Override
    public URI getContext(String context_uri) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                checkConnectionNamespace(conn);
                if (ResourceUtils.isContext(conn, context_uri)) return conn.getValueFactory().createURI(context_uri);
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex, ContextServiceImpl.class);
        }
        return null;
    }

    /**
     * Return the context used for storing system information.
     *
     * @return a KiWiUriResource representing the system knowledge space
     * @throws URISyntaxException 
     */
    @Override
    @Produces @RequestScoped @SystemKnowledgeSpace
    public URI getSystemContext() throws URISyntaxException {
        return createContext(configurationService.getSystemContext());
    }

    /**
     * Return the set of contexts that is currently active for reading. The set of active contexts
     * is either selected explicitly in web service calls or it consists of all contexts.
     *
     * @return a set of KiWiUriResources indicating the active contexts
     */
    @Override
    @Produces @RequestScoped @ActiveKnowledgeSpaces
    public Set<URI> getActiveContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get the base context URI
     * 
     * @return base context
     */
    @Override
    public String getBaseContext() {
        return configurationService.getBaseContext();
    }

    /**
     * Get the uri of the inferred context
     *
     * @return uri of this inferred context
     * @throws URISyntaxException 
     */
    @Override
    @Produces @RequestScoped @InferredKnowledgeSpace
    public URI getInferredContext() throws URISyntaxException {
        return createContext(configurationService.getInferredContext());
    }

    /**
     * Get the uri of the default context
     *
     * @return
     * @throws URISyntaxException 
     */
    @Override
    @Produces @RequestScoped @DefaultKnowledgeSpace
    public URI getDefaultContext() throws URISyntaxException {
        return createContext(configurationService.getDefaultContext());
    }

    /**
     * Get the uri of the context used for caching linked data
     *
     * @return
     * @throws URISyntaxException 
     */
    @Override
    public URI getCacheContext() throws URISyntaxException {
        return createContext(configurationService.getCacheContext());
    }

    /**
     * Return a human-readable label for the context, either the rdfs:label or the last part of the URI.
     *
     *
     * @param context
     * @return
     */
    @Override
    public String getContextLabel(URI context) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                return ResourceUtils.getLabel(conn, context);
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex, ContextServiceImpl.class);
        }
        return null;
    }
    
    /**
     * Return the number of triples for the context.
     * @param context
     */
    @Override
    public long getContextSize(URI context) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                return conn.size(context);
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            handleRepositoryException(e, ContextServiceImpl.class);
        }
        return 0;
    }

    /**
     * Import content into the context
     * 
     * @param context
     * @param is
     * @param format
     * @return
     */
    @Override
    public boolean importContent(String context, InputStream is, String format) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                checkConnectionNamespace(conn);
                URI ctx = conn.getValueFactory().createURI(context);
                int imported = importService.importData(is, format, userService.getCurrentUser(), ctx);
                return imported > 0;
            } catch (MarmottaImportException e) {
                log.error(e.getMessage(), e);
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex,ContextServiceImpl.class);
        }
        return false;
    }

    /**
     * Remove (clean whole content) the context represented by this URI
     *
     * @param context_uri uri of the context to remove
     * @return operation result, false if context does not exist
     */
    @Override
    public boolean removeContext(String context_uri) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                URI context = conn.getValueFactory().createURI(context_uri);
                conn.remove((Resource)null, null, null, context);
                return true;
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex, ContextServiceImpl.class);
        }
        return false;
    }

    /**
     * Remove (clean whole content) the context represented by this resource
     *
     *
     * @param context resource
     * @return operation result, false if context does not exist
     */
    @Override
    public boolean removeContext(URI context) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                conn.remove((Resource)null, null, null, context);
                return true;
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex, ContextServiceImpl.class);
        }
        return false;
    }

}
