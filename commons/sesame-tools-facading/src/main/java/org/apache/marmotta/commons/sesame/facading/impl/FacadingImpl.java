/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.commons.sesame.facading.impl;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.marmotta.commons.sesame.facading.annotations.RDF;
import org.apache.marmotta.commons.sesame.facading.annotations.RDFContext;
import org.apache.marmotta.commons.sesame.facading.annotations.RDFFilter;
import org.apache.marmotta.commons.sesame.facading.annotations.RDFType;
import org.apache.marmotta.commons.sesame.facading.api.Facading;
import org.apache.marmotta.commons.sesame.facading.model.Facade;
import org.apache.marmotta.commons.sesame.facading.util.FacadeUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Offers methods for loading and proxying Facades. A {@link Facade} is an interface that defines a
 * Java object with convenient Java methods around a {@link Resource} and makes it possible to use RDF
 * properties like Java Bean properties from inside Java.
 * <p/>
 * The facading service is to provide access on a higher level than raw RDF resources.
 * <p/>
 * @author Sebastian Schaffert <sschaffert@apache.org>
 * @author Jakob Frank <jakob@apache.org>
 */
public class FacadingImpl implements Facading {

    private static final URI RDF_TYPE = org.openrdf.model.vocabulary.RDF.TYPE;


    private static Logger log = LoggerFactory.getLogger(FacadingImpl.class);


    private final RepositoryConnection connection;


    public FacadingImpl(RepositoryConnection connection) {
        this.connection = connection;
    }

    /**
     * Create an instance of {@code C} that facades the resource given as argument using the {@link RDF} annotations provided
     * to the getter or setter methods of {@code C} to map to properties of the resource in the triple store.
     *
     *
     * @param r    the resource to facade
     * @param type the facade type as a class
     * @return a facading proxy of type {@code C}
     */
    @Override
    public <C extends Facade> C createFacade(Resource r, Class<C> type) {
        // support @RDFContext annotation in facade
        URI context = null;
        if(FacadeUtils.isFacadeAnnotationPresent(type, RDFContext.class)) {
            String s_context = FacadeUtils.getFacadeAnnotation(type,RDFContext.class).value();
            context = connection.getValueFactory().createURI(s_context);
            log.debug("applying context {} for facade {} of {}", context, type.getSimpleName(), r);
        }
        return createFacade(r, type, context);
    }

    /**
     * Create an instance of {@code C} that facades the resource given as argument using the {@link RDF} annotations provided
     * to the getter or setter methods of {@code C} to map to properties of the resource in the triple store.
     * Additionally, it puts the facade into the given context, a present {@link RDFContext} annotation is ignored.
     * This is useful if the {@link RDFContext} annotation for Facades is not applicable,
     * e.g. if the context is dynamically generated.
     *
     * @param r       the resource to facade
     * @param type    the facade type as a class
     * @param context the context of the facade
     * @return a facading proxy of type {@code C}
     */
    @Override
    public <C extends Facade> C createFacade(Resource r, Class<C> type, URI context) {
        if(r == null) {
            log.trace("null facade for null resouce");
            return null;
        } else 
            // if the interface is a Facade, we execute the query and then
            // create an invocation handler for each result to create proxy objects
            if(type.isInterface() && FacadeUtils.isFacade(type)) {
                try {
                    // support @RDFType annotation in facade
                    if(FacadeUtils.isFacadeAnnotationPresent(type, RDFType.class)) {
                        if (!connection.isOpen()) { throw new IllegalStateException("the connection is already closed, cannot access triple-store."); }
                        if (!connection.isActive()) { throw new IllegalStateException("no active transaction, cannot access triple-store."); }

                        String[] a_type = FacadeUtils.getFacadeAnnotation(type, RDFType.class).value();
                        for(String s_type : a_type) {
                            final URI r_type = connection.getValueFactory().createURI(s_type);
                            connection.add(r, RDF_TYPE, r_type, context);
                            log.trace("added type {} to {} because of RDFType-Annotation", r_type, r);
                            if(!connection.hasStatement(r, RDF_TYPE, r_type,true,context)) {
                                log.error("error adding type for facade!");
                            }
                        }
                    }

                    FacadingInvocationHandler handler = new FacadingInvocationHandler(r, context, type, this, connection);
                    if (log.isDebugEnabled()) {
                        if (context != null) {
                            log.debug("New Facading: {} delegating to {} (@{})", type.getSimpleName(), r, context);
                        } else {
                            log.debug("New Facading: {} delegating to {}", type.getSimpleName(), r);
                        }
                    }
                    return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler));
                } catch (RepositoryException e) {
                    log.error("error while accessing triple store", e);
                    return null;
                }
            } else {
                throw new IllegalArgumentException("interface passed as parameter is not a Facade (" + type.getCanonicalName() + ")");
            }
    }

    /**
     * Create a collection of instances of C that facade the resources given in the collection passed as argument.
     * The facade uses the {@link RDF} annotations provided to the getter or setter methods of C. The returned collection
     * is of the same kind as the passed collection.
     *
     *
     * @param list the collection containing the resources to facade
     * @param type the facade type as a class
     * @return
     */
    @Override
    public <C extends Facade> Collection<C> createFacade(Collection<? extends Resource> list, Class<C> type) {
        URI context = null;
        if(FacadeUtils.isFacadeAnnotationPresent(type, RDFContext.class)) {
            String s_context = FacadeUtils.getFacadeAnnotation(type,RDFContext.class).value();
            context = connection.getValueFactory().createURI(s_context);
            log.debug("applying context {} for facade {} of {}", context, type.getSimpleName(), list);
        }
        return createFacade(list, type, context);
    }

    /**
     * Create a collection of instances of C that facade the resources given in the collection passed as argument.
     * The facade uses the {@link RDF} annotations provided to the getter or setter methods of C. The returned collection
     * is of the same kind as the passed collection.
     *
     *
     * @param list the collection containing the resources to facade
     * @param type the facade type as a class
     * @return
     */
    @Override
    public <C extends Facade> Collection<C> createFacade(Collection<? extends Resource> list, Class<C> type, URI context) {
        log.trace("createFacadeList: creating {} facade over {} content items",type.getName(),list.size());
        LinkedList<C> result = new LinkedList<C>();
        if(type.isAnnotationPresent(RDFFilter.class)) {
            try {
                if (!connection.isOpen()) { throw new IllegalStateException("the connection is already closed, cannot access triple-store."); }
                if (!connection.isActive()) { throw new IllegalStateException("no active transaction, cannot access triple-store."); }

                // if the RDFType annotation is present, filter out content items that are of the wrong type
                LinkedList<URI> acceptable_types = new LinkedList<URI>();
                if(FacadeUtils.isFacadeAnnotationPresent(type,RDFFilter.class)) {
                    String[]        a_type = FacadeUtils.getFacadeAnnotation(type,RDFFilter.class).value();
                    for(String s_type : a_type) {
                        URI r_type = connection.getValueFactory().createURI(s_type);
                        acceptable_types.add(r_type);
                    }
                }

                // add facades for all content items to the result list
                for(Resource item : list) {
                    boolean accept = acceptable_types.size() == 0; // true for empty filter
                    for(URI rdf_type : acceptable_types) {
                        if(connection.hasStatement(item, RDF_TYPE, rdf_type, true)) {
                            accept = true;
                            log.trace("accepting resource {} because type matches ({})",item.toString(),rdf_type.stringValue());
                            break;
                        }
                    }
                    if(accept) {
                        result.add(createFacade(item,type,context));
                    }
                }
                log.debug("createFacadeList: filtered {} content items because they did not match the necessary criteria",list.size()-result.size());
            } catch (RepositoryException ex) {
                log.error("error while accessing RDF repository",ex);
            }
        } else {
            // add facades for all content items to the result list
            for(Resource item : list) {
                result.add(createFacade(item,type,context));
            }
        }
        return result;
    }


    /**
     * Create an instance of C that facades the resource identified by the uri given as argument, using the {@link RDF}
     * annotations provided to the getter or setter methods of C to map to properties of the resource in the triple
     * store.
     *
     * @param uri  the uri of the resource to facade
     * @param type the facade type as a class
     * @param <C>  the facade type as a generic parameter
     * @return
     */
    @Override
    public <C extends Facade> C createFacade(String uri, Class<C> type) {
        return createFacade(connection.getValueFactory().createURI(uri), type);
    }

    /**
     * Check whether the resource fits into the facade.
     *
     *
     * @param r the resource to check
     * @param type the facade to check for
     * @return <code>true</code> if the resource <code>r</code> fulfills all {@link RDFType} and
     *         {@link RDFFilter} requirements of <code>type</code>
     */
    @Override
    public <C extends Facade> boolean isFacadeable(Resource r, Class<C> type) {
        return isFacadeable(r, type, null);
    }

    /**
     * Check whether the resource fits into the facade.
     *
     *
     * @param r the resource to check
     * @param type the facade to check for
     * @param context limit all checks to this context
     * @return <code>true</code> if the resource <code>r</code> fulfills all {@link RDFType} and
     *         {@link RDFFilter} requirements of <code>type</code>
     */
    @Override
    public <C extends Facade> boolean isFacadeable(Resource r, Class<C> type, URI context) {
        if (FacadeUtils.isFacadeAnnotationPresent(type, RDFType.class)) {
            try {
                if (!connection.isOpen()) { throw new IllegalStateException("the connection is already closed, cannot access triple store."); }
                if (!connection.isActive()) { throw new IllegalStateException("no active transaction, cannot access triple-store."); }

                String[] rdfTypes = FacadeUtils.getFacadeAnnotation(type, RDFType.class).value();
                boolean facadeable = true;
                for (String s_type : rdfTypes) {
                    if(context != null) {
                        facadeable &= connection.hasStatement(r, RDF_TYPE, connection.getValueFactory().createURI(s_type), true, context);
                    } else {
                        facadeable &= connection.hasStatement(r, RDF_TYPE, connection.getValueFactory().createURI(s_type), true);
                    }
                }
                // also check for @RDFFilter
                if (FacadeUtils.isFacadeAnnotationPresent(type, RDFFilter.class)) {
                    String[] filterTypes = FacadeUtils.getFacadeAnnotation(type, RDFFilter.class).value();
                    for (String s_type : filterTypes) {
                        if(context != null) {
                            facadeable &= connection.hasStatement(r, RDF_TYPE, connection.getValueFactory().createURI(s_type), true, context);
                        } else {
                            facadeable &= connection.hasStatement(r, RDF_TYPE, connection.getValueFactory().createURI(s_type), true);
                        }
                    }
                }
                return facadeable;
            } catch(RepositoryException ex) {
                log.error("error while accessing RDF repository",ex);
            }
        }
        return false;
    }
}
