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
package org.apache.marmotta.commons.sesame.facading.api;

import org.apache.marmotta.commons.sesame.facading.model.Facade;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import java.util.Collection;

/**
 * Offers methods for loading and proxying KiWiFacades. A KiWi Facade is an interface that defines a Java
 * object with convenient Java methods around a KiWiResource and makes it possible to use RDF properties like
 * Java Bean properties from inside Java.
 * <p/>
 * The facading service is used by many other services, e.g. ContentItemService and TaggingService, to provide
 * access on a higher level than raw RDF resources.
 *
 *
 * <p/>
 * User: sschaffe
 */
public interface Facading {

    /**
     * Create an instance of C that facades the resource given as argument using the @RDF annotations provided
     * to the getter or setter methods of Cto map to properties of the resource in the triple store.
     *
     *
     * @param r the resource to facade
     * @param type the facade type as a class
     * @return
     */
    public <C extends Facade> C createFacade(Resource r, Class<C> type);

    /**
     * Create an instance of C that facades the resource given as argument using the @RDF annotations provided
     * to the getter or setter methods of Cto map to properties of the resource in the triple store.
     * Additionally, it puts the facade into the given context.
     * This is useful if the @RDFContext annotation for Facades is not applicable.
     * E.g. if the context is dynamically generated.
     *
     *
     * @param r the resource to facade
     * @param type the facade type as a class
     * @param context the context into which the facade should be put
     * @return
     */
    public <C extends Facade> C createFacade(Resource r, Class<C> type, URI context);

    /**
     * Create a collection of instances of C that facade the resources given in the collection passed as argument.
     * The facade uses the @RDF annotations provided to the getter or setter methods of C. The returned collection
     * is of the same kind as the passed collection.
     *
     *
     * @param list the collection containing the resources to facade
     * @param type the facade type as a class
     * @return
     */
    public <C extends Facade> Collection<C> createFacade(Collection<? extends Resource> list, Class<C> type);

    /**
     * Create an instance of C that facades the resource identified by the uri given as argument, using the @RDF
     * annotations provided to the getter or setter methods of C to map to properties of the resource in the triple
     * store.
     *
     * @param uri the uri of the resource to facade
     * @param type the facade type as a class
     * @param <C> the facade type as a generic parameter
     * @return
     */
    public <C extends Facade> C createFacade(String uri, Class<C> type);

    /**
     * Check whether the resource fits into the facade.
     * 
     *
     * @param r the resource to check
     * @param type the facade to check for
     * @param context limit all checks to this context
     * @return <code>true</code> if the resource <code>r</code> fulfills all {@link org.apache.marmotta.commons.sesame.facading.annotations.RDFType} and
     *         {@link org.apache.marmotta.commons.sesame.facading.annotations.RDFFilter} requirements of <code>type</code>
     */
    public <C extends Facade> boolean isFacadeable(Resource r, Class<C> type, URI context);

    /**
     * Check whether the resource fits into the facade.
     * 
     *
     * @param r the resource to check
     * @param type the facade to check for
     * @return <code>true</code> if the resource <code>r</code> fulfills all {@link org.apache.marmotta.commons.sesame.facading.annotations.RDFType} and
     *         {@link org.apache.marmotta.commons.sesame.facading.annotations.RDFFilter} requirements of <code>type</code>
     */
    public <C extends Facade> boolean isFacadeable(Resource r, Class<C> type);

    /**
     * Create a collection of instances of C that facade the resources given in the collection passed as argument.
     * The facade uses the {@link org.apache.marmotta.commons.sesame.facading.annotations.RDF} annotations provided to the getter or setter methods of C. The returned collection
     * is of the same kind as the passed collection.
     *
     *
     * @param list the collection containing the resources to facade
     * @param type the facade type as a class
     * @return
     */
    <C extends Facade> Collection<C> createFacade(Collection<? extends Resource> list, Class<C> type, URI context);
}
