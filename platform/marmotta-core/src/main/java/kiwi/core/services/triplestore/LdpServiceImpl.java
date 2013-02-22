/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kiwi.core.services.triplestore;
import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import kiwi.core.api.config.ConfigurationService;
import kiwi.core.api.prefix.PrefixService;
import kiwi.core.api.triplestore.ContextService;
import kiwi.core.api.triplestore.LdpService;
import kiwi.core.api.triplestore.SesameService;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

import org.apache.marmotta.commons.sesame.repository.ResourceUtils;

/**
 * (Experimental) Implementation for supporting LDP (WIP)
 * 
 * @author Sergio Fernández
 *
 */
@ApplicationScoped
public class LdpServiceImpl implements LdpService {
    
    private URI context;
    
    @Inject
    private Logger log;
    
    @Inject
    private ConfigurationService configurationService;
    
    @Inject
    private SesameService sesameService;
    
    @Inject
    private ContextService contextService;
    
    @Inject
    private PrefixService prefixService;
    
    @PostConstruct
    public void initialize() {
        String uri = getBaseContainer();
        try {
            this.context = contextService.createContext(uri, "ldp (experimental context)");
            createContainer(uri, "ldp base container");
        } catch (URISyntaxException e) {
            log.error("Root LDP Container {} cannot be created: {}", uri, e.getMessage());
        }        
    }
    
    @Override
    public String getBaseContainer() {
        return configurationService.getBaseContext() + ConfigurationService.LDP_PATH;
    }
    
    @Override
    public List<URI> list() {
        List<URI> containers = new ArrayList<URI>();
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                checkConnectionNamespace(conn);
                conn.begin();
                Iterable<Resource> results = ResourceUtils.listResources(conn, conn.getValueFactory().createURI(prefixService.getNamespace("ldp"), "Container"));
                for (Resource result : results) {
                    if(result instanceof URI) {
                        containers.add((URI)result);
                    }  
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            handleRepositoryException(e, LdpServiceImpl.class);
        }
        return containers;
    }
    
    @Override
    public URI get(String uri) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                checkConnectionNamespace(conn);
                conn.begin();
                return (ResourceUtils.existsResource(conn, uri) ? ResourceUtils.getUriResource(conn, uri) : null);
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex, LdpServiceImpl.class);
        }
        return null;
    }

    @Override
    public boolean create(String uri) throws URISyntaxException {
        return create(uri, null);
    }

    @Override
    public boolean create(String uri, String title) throws URISyntaxException {
        URI parent = getParentContainer(uri);
        if (parent != null) {
            return createResource(uri, parent);
        } else {
           throw new URISyntaxException(uri, "Non suitable parent container found");
        }
    }
    
    private boolean createResource(String uri, URI container) {
        //TODO: refactor this code, already implemented at the ResourceWebService, 
        //by moving all business logic to a service
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                checkConnectionNamespace(conn);
                conn.begin();
                ValueFactory factory = conn.getValueFactory();
                URI resource = ResourceUtils.getUriResource(conn, uri);
                //TODO: chek if already exists
                conn.add(resource, RDF.TYPE, factory.createURI(prefixService.getNamespace("ldp"), "Resource"), container);
                conn.add(resource, factory.createURI(prefixService.getNamespace("dct"), "created"), factory.createLiteral(new Date()), container);
                conn.add(container, RDFS.MEMBER, resource, container);
                return true; //FIXME: 201 vs 200
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            return false;
        }
    }

    private boolean createContainer(String container, String title) throws URISyntaxException {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                checkConnectionNamespace(conn);
                conn.begin();
                ValueFactory factory = conn.getValueFactory();
                URI uri = ResourceUtils.getUriResource(conn, container);
                conn.add(uri, RDF.TYPE, factory.createURI(prefixService.getNamespace("ldp"), "Container"), context);
                conn.add(uri, RDFS.LABEL, factory.createLiteral(title), context);
                conn.add(uri, factory.createURI(prefixService.getNamespace("dct"), "created"), factory.createLiteral(new Date()), context);
                log.info("Created new container <{}>", uri.stringValue());
                return true;
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex, LdpServiceImpl.class);
        }
        return false;
    }
    
//    @Override
//    public boolean addMember(String container, String member) {
//        //TODO: check parent relationship
//        try {
//            RepositoryConnection conn = sesameService.getConnection();
//            try {
//                checkConnectionNamespace(conn);
//                URI parent = ResourceUtils.getUriResource(conn, container);
//                URI uri = ResourceUtils.getUriResource(conn, member);
//                URI context = parent; //TODO: for the moment, every container is also a context (named graph) for us
//                conn.add(parent, RDFS.MEMBER, uri, context);
//                log.info("Created new container <{}>", uri.stringValue());
//            } finally {
//                conn.commit();
//                conn.close();path
//            }
//            return true;
//        } catch(RepositoryException ex) {
//            handleRepositoryException(ex, LdpServiceImpl.class);
//            return false;
//        }
//    }

    @Override
    public boolean delete(String uri) throws RepositoryException {
        if (isContainer(uri)) {
            return deleteContainer(uri);
        } else {
            return deleteResource(uri);
        }
    }

    /**
     * Resource deletion
     * 
     * @todo refactor this code, already implemented at the ResourceWebService, by moving all business logic to a service
     * 
     * @param uri
     * @return
     * @throws RepositoryException
     */
    private boolean deleteResource(String uri) throws RepositoryException {
        //TODO: refactor this code, already implemented at the ResourceWebService, 
        //by moving all business logic to a service
        RepositoryConnection conn = sesameService.getConnection();
        URI parent;
        try {
            parent = getParentContainer(uri);
        } catch (URISyntaxException e) {
            parent = null;
        }
        try {
            conn.begin();
            URI resource = conn.getValueFactory().createURI(uri);
            conn.remove(resource, null, null, parent);
            return true;
        } finally {
            conn.commit();
            conn.close();
        }
    }

    /**
     * Container deleteion
     * (using a composition model)
     * 
     * @param uri
     * @return
     * @throws RepositoryException 
     */
    private boolean deleteContainer(String uri) throws RepositoryException {
        RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();
            URI context = conn.getValueFactory().createURI(uri);
            conn.remove((Resource)null, null, null, context);
            return true;
        } finally {
            conn.commit();
            conn.close();
        }
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
            conn.setNamespace(DEFAULT_PREFIX, getBaseContainer());
        }
    }
    
    /**
     * Check whenever this URI represents a PDPC
     * 
     * @param uri
     * @return
     */
    private boolean isContainer(String uri) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                checkConnectionNamespace(conn);
                conn.begin();
                Iterable<Resource> results = ResourceUtils.listResources(conn, conn.getValueFactory().createURI(prefixService.getNamespace("ldp"), "Container"),  conn.getValueFactory().createURI(uri));
                for (Resource result : results) {
                    if(result instanceof URI) {
                        if (uri.equals(result.stringValue())) {
                            return true;
                        }
                    }  
                }
                return false;
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            handleRepositoryException(e, LdpServiceImpl.class);
            return false;
        }
    }
    
    /**
     * Get the parent container for a resource
     * 
     * @param uri
     * @return
     * @throws URISyntaxException
     */
    private URI getParentContainer(String uri) throws URISyntaxException {
        String base = this.getBaseContainer();
        if (!uri.startsWith(base)) {
            throw new URISyntaxException(uri, "Invalid URI: base URI does not matches with " + base);
        }
        if (base.equals(uri)) {
            log.error("{} is already the base container", uri);
            return null;
        }
        String parent = uri.substring(0, uri.lastIndexOf('/'));
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                checkConnectionNamespace(conn);
                conn.begin();
                if (!ResourceUtils.existsResource(conn, parent)) {
                    log.warn("Container {} does not exist, so creating it...", parent);
                    createContainer(parent, parent);
                }
                return ResourceUtils.getUriResource(conn, parent);
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            log.error("Error checking context {}: {}", parent, e.getMessage());
            return null;
        }
        
    }

}
