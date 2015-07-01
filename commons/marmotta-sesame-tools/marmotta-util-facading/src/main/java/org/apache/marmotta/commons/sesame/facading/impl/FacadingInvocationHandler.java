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


import org.apache.marmotta.commons.sesame.facading.annotations.RDF;
import org.apache.marmotta.commons.sesame.facading.annotations.RDFInverse;
import org.apache.marmotta.commons.sesame.facading.annotations.RDFPropertyBuilder;
import org.apache.marmotta.commons.sesame.facading.api.Facading;
import org.apache.marmotta.commons.sesame.facading.api.FacadingPredicateBuilder;
import org.apache.marmotta.commons.sesame.facading.model.Facade;
import org.apache.marmotta.commons.sesame.facading.util.FacadeUtils;
import org.apache.marmotta.commons.util.DateUtils;
import org.joda.time.DateTime;
import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;

/**
 * This class implements an invocation handler to be used for proxy classes that delegate to a
 * content item and to data in the triple store. It has to be constructed using the triple store
 * implementation as parameter. Interfaces that make use of this invocation handler need to extend
 * the {@link Facade} interface.
 * 
 * @author Sebastian Schaffert <sschaffert@apache.org>
 * @author Jakob Frank <jakob@apache.org>
 */
class FacadingInvocationHandler implements InvocationHandler {

    public static enum OPERATOR {
        GET(false, 0, "get"),
        SET(true, 1, "set"),
        ADD(true, 1, "add"),
        DEL(true, 0, "del", "delete", "remove", "rm"),
        HAS(false, 0, "has", "is");


        private static final String[] PX, SPX;
        static {
            LinkedList<String> ops = new LinkedList<String>();
            for (OPERATOR op : OPERATOR.values()) {
                for (String px : op.prefixes) {
                    ops.add(px);
                }
            }
            PX = ops.toArray(new String[ops.size()]);
            SPX = ops.toArray(new String[ops.size()]);
            Arrays.sort(SPX, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o2.length() - o1.length();
                }
            });
        }

        final String[] prefixes;
        final int numArgs;
        final boolean writeOp;

        private OPERATOR(boolean isWriteOp, int args, String... strings) {
            this.writeOp = isWriteOp;
            this.numArgs = args;
            this.prefixes = strings;
        }

        @Override
        public String toString() {
            return prefixes[0];
        }

        public static String[] getOperatorPrefixes() {
            return PX;
        }

        public static String[] getLengthSortedOperatorPrefixes() {
            return SPX;
        }

        public static OPERATOR getOperator(Method m) {
            for (OPERATOR op : values()) {
                for (String px : op.prefixes) {
                    if (m.getName().startsWith(px)) {
                        final int numP = m.getParameterTypes().length;
                        if (numP == op.numArgs || numP == op.numArgs + 1) { return op; }
                    }
                }
            }
            return valueOf(m.getName());
        }
    }

    private final RepositoryConnection connection;

    private final Facading facadingService;

    private final Class<? extends Facade> declaredFacade;

    private final FacadingPredicateBuilder propBuilder;

    private final Resource delegate;

    private final URI context;

    private final HashMap<String, Object> fieldCache;

    private final Logger log;

    /**
     * Indicates if the cache is used, by default is false.
     */
    private boolean useCache;

    public FacadingInvocationHandler(Resource item, URI context, Class<? extends Facade> facade, Facading facadingService, RepositoryConnection connection) {
        this.log = LoggerFactory.getLogger(facade.getName() + "!" + this.getClass().getSimpleName() + "@" + item.stringValue());
        this.delegate = item;
        this.facadingService = facadingService;
        this.declaredFacade = facade;
        this.connection = connection;

        if (declaredFacade.isAnnotationPresent(RDFPropertyBuilder.class)) {
            final Class<? extends FacadingPredicateBuilder> bClass = declaredFacade.getAnnotation(RDFPropertyBuilder.class).value();
            FacadingPredicateBuilder _b = null;
            try {
                // Look for a no-arg Constructor
                _b = bClass.getConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                // If there is no no-arg Constructor, try static getInstance()
                try {
                    for (Method m : bClass.getMethods()) {
                        if (Modifier.isStatic(m.getModifiers()) && "getInstance".equals(m.getName()) && m.getParameterTypes().length == 0) {
                            _b = (FacadingPredicateBuilder) m.invoke(null);
                            break;
                        }
                    }
                    if (_b == null) { throw new IllegalArgumentException("Could not find no-arg Constructor or static no-arg factory-method 'getInstance' for "
                            + bClass.getName()); }
                } catch (Exception e1) {
                    throw new IllegalArgumentException("Could not load instance of " + bClass.getSimpleName() + " from static factory 'getInstance()': "
                            + e.getMessage(), e);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not create instance of " + bClass.getSimpleName() + ": " + e.getMessage(), e);
            }
            this.propBuilder = _b;
        } else {
            this.propBuilder = null;
        }

        if (context != null) {
            this.context = context;
        } else {
            // FIXME
            this.context = null;
        }

        fieldCache = new HashMap<String, Object>();

        // disable cache, it does not work well with deleted triples ...
        useCache = false;
    }

    /**
     * Indicates if the cache is allow or not.
     * 
     * @return the useCache true if the cache is done.
     */
    public boolean isUseCache() {
        return useCache;
    }

    /**
     * Used to enable/disable the cache mechanism.
     * 
     * @param useCache
     *            true foe enable cache, false - no cache.
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    /**
     * @return the item
     */
    public Resource getDelegate() {
        return delegate;
    }

    /**
     * Invoke the invocation handler for the given proxy object, method, and arguments. In order to
     * execute the passed method, this method does the following: - if the method has a
     * <code>RDF</code> annotation or if it is a setter and the corresponding getter has a
     * <code>RDF</code> annotation, we try to retrieve the appropriate value by querying the triple
     * store and converting the triple store data to the return type of the method; if the return
     * type is again an interface
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method,
     *      java.lang.Object[])
     * @see org.apache.marmotta.commons.sesame.facading.annotations.RDF
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws InstantiationException, IllegalAccessException, RepositoryException {
        if (!connection.isOpen()) { throw new IllegalAccessException("the connection is already closed, cannot access proxy methods."); }
        if (!connection.isActive()) { throw new IllegalAccessException("no active transaction, cannot access triple-store."); }

        // handle default methods:
        if (FacadingInvocationHelper.checkMethodSig(method, "hashCode")) {
            return delegate.hashCode();
        } else if (FacadingInvocationHelper.checkMethodSig(method, "equals", 1)) {
            final Object other = args[0];
            return other != null && other.getClass().equals(proxy.getClass()) && other.hashCode() == proxy.hashCode();
        } else if (FacadingInvocationHelper.checkMethodSig(method, "toString")) {
            return declaredFacade.getSimpleName() + " with delegate to " + delegate.toString();
        } else if (FacadingInvocationHelper.checkMethodSig(method, "getDelegate")) { return delegate; }

        // caching
        final String fieldName = FacadingInvocationHelper.getBaseName(method);
        if (useCache && method.getName().startsWith("get")) {
            if (fieldCache.get(fieldName) != null) { return fieldCache.get(fieldName); }
        }

        final FacadingPredicate fp = getFacadingPredicate(method);

        // distinguish getters and setters and more...
        switch (OPERATOR.getOperator(method)) {
        case GET:
            return handleGet(method, args, fp);
        case SET:
            return handleSet(method, args, fp);
        case ADD:
            return handleAdd(method, args, fp);
        case DEL:
            return handleDel(method, args, fp);
        case HAS:
            return handleHas(method, args, fp);
        default:
            throw new IllegalArgumentException("Unsupported method: " + method.getName());
        }
    }

    private FacadingPredicate getFacadingPredicate(Method method) throws IllegalArgumentException {
        final String[] rdf_property;
        final boolean inverse;
        // look for RDF annotation and extract the property from it; if not on the getter, look
        // for the corresponding setter and check whether it has a @RDF annotation; if neither has,
        // throw an IllegalArgumentException
        RDF rdf = FacadingInvocationHelper.getAnnotation(method, RDF.class);
        if (rdf != null) {
            rdf_property = rdf.value();
            inverse = false;
            return new FacadingPredicate(inverse, rdf_property);
        } else {
            RDFInverse rdfi = FacadingInvocationHelper.getAnnotation(method, RDFInverse.class);
            if (rdfi != null) {
                rdf_property = rdfi.value();
                inverse = true;
                return new FacadingPredicate(inverse, rdf_property);
            } else {
                if (propBuilder != null) {
                    String fName = FacadingInvocationHelper.getBaseName(method);
                    if (fName.length() > 1) {
                        fName = fName.substring(0, 1).toLowerCase(Locale.ENGLISH) + fName.substring(1);
                    }
                    return propBuilder.getFacadingPredicate(fName, declaredFacade, method);
                } else {
                    throw new IllegalArgumentException("Could not find facading predicate for " + method.getName() + " in " + declaredFacade.getName());
                }
            }
        }
    }

    private Boolean handleHas(Method method, Object[] args, FacadingPredicate predicate) throws RepositoryException {
        final Locale loc;
        if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Locale.class)) {
            loc = (Locale) args[0];
        } else {
            loc = null;
        }

        if (predicate.isInverse()) {
            if (loc != null) { throw new IllegalArgumentException("@RDFInverse not supported for language tagged properties"); }
            else {
                for (String p : predicate.getProperties()) {
                    final URI prop = connection.getValueFactory().createURI(p);
                    final RepositoryResult<Statement> result = connection.getStatements(null, prop, delegate, true, context);
                    try {
                        if (result.hasNext()) { return true; }
                    } finally {
                        result.close();
                    }
                }
            }
        } else {
            for (String p : predicate.getProperties()) {
                final URI prop = connection.getValueFactory().createURI(p);
                final RepositoryResult<Statement> result = connection.getStatements(delegate, prop, null, true, context);
                try {
                    if (loc == null) {
                        if (result.hasNext()) { return true; }
                    } else {
                        while (result.hasNext()) {
                            final Value o = result.next().getObject();
                            if (FacadingInvocationHelper.checkLocale(loc, o)) { return true; }
                        }
                    }
                } finally {
                    result.close();
                }
            }
        }


        return false;
    }

    private Object handleDel(Method method, Object[] args, FacadingPredicate predicate) throws RepositoryException {
        final Locale loc;
        if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Locale.class)) {
            loc = (Locale) args[0];
        } else {
            loc = null;
        }

        delProperties(predicate, loc);

        return null;
    }

    private Object handleAdd(Method method, Object[] args, FacadingPredicate predicate) throws RepositoryException, IllegalArgumentException {
        final Locale loc;
        if (method.getParameterTypes().length == 2 && method.getParameterTypes()[1].equals(Locale.class)) {
            loc = (Locale) args[1];
        } else {
            loc = null;
        }

        final Class<?> paramType = method.getParameterTypes()[0];
        addProperties(method, args, predicate.getProperties(), predicate.isInverse(), loc, paramType);

        return null;
    }

    private Object handleSet(Method method, Object[] args, FacadingPredicate predicate)
            throws RepositoryException, IllegalArgumentException {

        final Locale loc;
        if (method.getParameterTypes().length == 2 && method.getParameterTypes()[1].equals(Locale.class)) {
            loc = (Locale) args[1];
        } else {
            loc = null;
        }

        // add to cache
        if (useCache) {
            fieldCache.put(FacadingInvocationHelper.getBaseName(method), args[0]);
        }

        // This is SET, so delete all previous properties
        delProperties(predicate, loc);

        // *** set the value of a certain RDF property
        final Class<?> paramType = method.getParameterTypes()[0];

        addProperties(method, args, predicate.getProperties(), predicate.isInverse(), loc, paramType);


        return null;
    }

    private void addProperties(Method method, Object[] args, final String[] rdf_property, final boolean inverse, final Locale loc, final Class<?> paramType)
            throws RepositoryException, IllegalArgumentException {
        if (args[0] == null || "".equals(args[0])) {
            // nop;
        } else if (FacadeUtils.isBaseType(paramType) && !inverse) {
            for (String v : rdf_property) {
                final URI prop = connection.getValueFactory().createURI(v);
                connection.add(delegate, prop, createLiteral(args[0], loc), context);
            }
        } else if (FacadeUtils.isValue(paramType) && !inverse) {
            for (String v : rdf_property) {
                final URI prop = connection.getValueFactory().createURI(v);
                // create a new triple for this property, subject, and object
                connection.add(delegate, prop, (Value) args[0], context);
            }
        } else if (FacadeUtils.isResource(paramType) && inverse) {
            for (String v : rdf_property) {
                final URI prop = connection.getValueFactory().createURI(v);
                // create a new triple for this property, subject, and object
                connection.add((Resource) args[0], prop, delegate, context);
            }
        } else if (FacadeUtils.isFacade(paramType) && !inverse) {
            for (String v : rdf_property) {
                final URI prop = connection.getValueFactory().createURI(v);
                // create a new triple for this property, subject, and object
                connection.add(delegate, prop, ((Facade) args[0]).getDelegate(), context);
            }
        } else if (FacadeUtils.isFacade(paramType) && inverse) {
            for (String v : rdf_property) {
                final URI prop = connection.getValueFactory().createURI(v);
                // create a new triple for this property, subject, and object
                connection.add(((Facade) args[0]).getDelegate(), prop, delegate, context);
            }
        } else if (FacadeUtils.isCollection(paramType)) {
            for (String v : rdf_property) {
                final Collection<?> c = (Collection<?>) args[0];

                final URI prop = connection.getValueFactory().createURI(v);

                // add each of the elements in the collection as new triple with prop
                for (final Object o : c) {
                    if (o == null) {
                        // skip
                    } else if (FacadeUtils.isBaseType(o.getClass()) && !inverse) {
                        connection.add(delegate, prop, createLiteral(o, loc), context);
                    } else if (FacadeUtils.isFacade(o.getClass()) && !inverse) {
                        connection.add(delegate, prop, ((Facade) o).getDelegate(), context);
                    } else if (FacadeUtils.isFacade(o.getClass()) && inverse) {
                        connection.add(((Facade) o).getDelegate(), prop, delegate, context);
                    } else if (FacadeUtils.isValue(o.getClass()) && !inverse) {
                        connection.add(delegate, prop, (Value) o, context);
                    } else if (FacadeUtils.isResource(o.getClass()) && inverse) {
                        connection.add((Resource) o, prop, delegate, context);
                    } else if (inverse) {
                        throw new IllegalArgumentException("method " + method.getName() + ": @RDFInverse not supported for parameter type "
                                + paramType.getName());
                    } else {
                        throw new IllegalArgumentException("the type " + o.getClass().getName() + " is not supported in collections");
                    }
                }
            }
        } else if (inverse) {
            throw new IllegalArgumentException("method " + method.getName() + ": @RDFInverse not supported for parameter type " + paramType.getName());
        } else {
            throw new IllegalArgumentException("method " + method.getName() + ": unsupported parameter type " + paramType.getName());
        }
    }

    private void delProperties(final FacadingPredicate predicate, final Locale loc) throws RepositoryException {
        for (String v : predicate.getProperties()) {
            final URI prop = connection.getValueFactory().createURI(v);

            if (!predicate.isInverse() && loc == null) {
                // remove all properties prop that have this subject;
                connection.remove(delegate, prop, null, context);
            } else if (predicate.isInverse() && loc == null) {
                // remove all properties prop that have this object;
                connection.remove((Resource) null, prop, delegate, context);
            } else if (!predicate.isInverse() && loc != null) {
                final RepositoryResult<Statement> statements = connection.getStatements(delegate, prop, null, false, context);
                try {
                    while (statements.hasNext()) {
                        final Statement s = statements.next();
                        if (FacadingInvocationHelper.checkLocale(loc, s.getObject())) {
                            connection.remove(s);
                        }
                    }
                } finally {
                    statements.close();
                }
            } else if (predicate.isInverse() && loc != null) { throw new IllegalArgumentException("A combination of @RDFInverse and a Literal is not possible");
            }
        }
    }

    private Object handleGet(Method method, Object[] args, FacadingPredicate predicate) throws IllegalAccessException, InstantiationException,
    RepositoryException {
        final Locale loc;
        if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Locale.class)) {
            loc = (Locale) args[0];
        } else {
            loc = null;
        }

        // *** get the value of a certain RDF property ***

        final Class<?> returnType = method.getReturnType();
        final Type typeOfGeneric = method.getGenericReturnType();

        // we believe that the result is universal for each property
        // and therefore just return the result for the firstly defined property
        final Object result = transform(returnType, typeOfGeneric, delegate, predicate.getProperties()[0], loc, predicate.isInverse());

        if (useCache) {
            fieldCache.put(FacadingInvocationHelper.getBaseName(method), result);
        }

        return result;
    }

    /**
     * Helper method to transform the object reachable via rdf_property from r to the given
     * returnType; if the returnType is a collection, it is also necessary to provide the generic
     * type. The KiWiEntityManager is used for further querying.<br>
     * Please note that if the <code>returnType</code>is a collection you <b>must</b> use a concrete
     * class (e.g. <code>java.util.ArrayList</code>) not an abstract class or interface.
     * 
     * @param <C>
     * @param returnType
     * @param typeOfGeneric
     * @param rdf_property
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private <C, D extends Facade> C transform(Class<C> returnType, Type typeOfGeneric, Resource entity, String rdf_property, Locale loc, boolean inverse)
            throws IllegalAccessException, InstantiationException, RepositoryException {
        // should not happen actually
        if (entity == null) { return null; }

        if (FacadeUtils.isBaseType(returnType) && !inverse) {
            /*
             * if the return type is string or primitive, get the literal value of the property and
             * transform it appropriately
             */
            final URI property = connection.getValueFactory().createURI(rdf_property);
            final String value = getProperty(entity, property, loc, context);

            try {
                // transformation to appropriate primitive type
                final C result = FacadeUtils.transformToBaseType(value, returnType);

                return result;
            } catch (final IllegalArgumentException ex) {
                return null;
            }

        } else if (FacadeUtils.isValue(returnType) && !inverse) {
            return queryOutgoingSingle(entity, rdf_property, returnType);
        } else if (FacadeUtils.isValue(returnType) && inverse) {
            return queryIncomingSingle(entity, rdf_property, returnType);
        } else if (FacadeUtils.isFacade(returnType) && !inverse) {
            /*
             * for KiWi entities, we retrieve the resource that is targeted by this property (by
             * using getObject) and create a query on the triple store using createQuery() and the
             * resource's uri that returns the result in the appropriate type (can e.g. be again a
             * proxy using this invocation handler!)
             */
            Resource object = queryOutgoingSingle(entity, rdf_property, Resource.class);

            if (object != null) {
                return returnType.cast(facadingService.createFacade(object, returnType.asSubclass(Facade.class), context));
            } else {
                return null;
            }
        } else if (FacadeUtils.isFacade(returnType) && inverse) {
            /*
             * for KiWi entities, we retrieve the resource that is targeted by this property (by
             * using getObject) and create a query on the triple store using createQuery() and the
             * resource's uri that returns the result in the appropriate type (can e.g. be again a
             * proxy using this invocation handler!)
             */
            Resource subject = queryIncomingSingle(entity, rdf_property, Resource.class);

            if (subject != null) {
                return returnType.cast(facadingService.createFacade(subject, returnType.asSubclass(Facade.class), context));
            } else {
                return null;
            }
        } else if (FacadeUtils.isCollection(returnType)) {
            /*
             * if we have a collection, we try to infer the generic type of its contents and use
             * this to generate values; if the generic type is a kiwi entity, we issue a createQuery
             * to the tripleStore to retrieve the corresponding values; if the generic type is a
             * base type, we transform the results to the base type and query for literals
             */
            if (typeOfGeneric instanceof ParameterizedType) {
                final ParameterizedType t = (ParameterizedType) typeOfGeneric;
                final Class<?> tCls = (Class<?>) t.getActualTypeArguments()[0];

                @SuppressWarnings("rawtypes")
                final Class<? extends Collection> collectionType = returnType.asSubclass(Collection.class);

                if (FacadeUtils.isFacade(tCls) && !inverse) {
                    return returnType.cast(FacadingInvocationHelper.createCollection(
                            collectionType,
                            facadingService.createFacade(queryOutgoingAll(entity, rdf_property, Resource.class), tCls.asSubclass(Facade.class), context)));
                } else if (FacadeUtils.isFacade(tCls) && inverse) {
                    return returnType.cast(FacadingInvocationHelper.createCollection(
                            collectionType,
                            facadingService.createFacade(queryIncomingAll(entity, rdf_property, Resource.class), tCls.asSubclass(Facade.class), context)));
                } else if (FacadeUtils.isValue(tCls) && !inverse) {
                    return returnType.cast(FacadingInvocationHelper.createCollection(
                            collectionType,
                            queryOutgoingAll(entity, rdf_property, tCls.asSubclass(Value.class))));
                } else if (FacadeUtils.isValue(tCls) && inverse) {
                    return returnType.cast(FacadingInvocationHelper.createCollection(
                            collectionType,
                            queryIncomingAll(entity, rdf_property, tCls.asSubclass(Value.class))));
                } else if (inverse) {
                    throw new IllegalArgumentException("@RDFInverse not supported for mappings of type " + rdf_property);
                } else if (FacadeUtils.isBaseType(tCls)) {
                    final Collection<Object> result = FacadingInvocationHelper.createCollection(collectionType, Collections.<Object> emptyList());
                    final URI property = connection.getValueFactory().createURI(rdf_property);

                    for (final String s : getProperties(entity, property, loc, context)) {
                        result.add(FacadeUtils.transformToBaseType(s, tCls));
                    }

                    return returnType.cast(result);
                } else {
                    throw new IllegalArgumentException("return type is using generic type " + tCls.getName()
                            + ", which is not supported in RDF-based collections; please use either Java primitive types or KiWi Entities in KiWiFacades");
                }
            } else {
                throw new IllegalArgumentException("return type is unparametrized collection type " + returnType.getName()
                        + ", which is not supported; please use an explicit type parameter in Facades");
            }
        } else if (inverse) {
            throw new IllegalArgumentException("@RDFInverse not supported for mappings of type " + rdf_property);
        } else {
            throw new IllegalArgumentException("unsupported return type " + returnType.getName());
        }

    }

    /**
     * Return the single object of type C that is reachable from entity by rdf_property. Returns
     * null if there is no such object or if the type of the object does not match the type passed
     * as argument.
     * 
     */
    private <C> C queryOutgoingSingle(Resource entity, String rdf_property, Class<C> returnType) throws RepositoryException {
        URI property = connection.getValueFactory().createURI(rdf_property);

        RepositoryResult<Statement> triples = connection.getStatements(entity, property, null, false);
        try {
            if (triples.hasNext()) {
                Statement triple = triples.next();

                Value object = triple.getObject();

                if (returnType.isInstance(object)) {
                    return returnType.cast(object);
                } else {
                    log.error("cannot cast retrieved object {} for property {} to return type {}", object, rdf_property, returnType);
                    return null;
                }

            } else {
                return null;
            }
        } finally {
            triples.close();
        }

    }

    /**
     * Return the single subject of type C that can reach entity by rdf_property. Returns null if
     * there is no such object or if the type of the object does not match the type passed as
     * argument.
     * 
     */
    private <C> C queryIncomingSingle(Resource entity, String rdf_property, Class<C> returnType) throws RepositoryException {
        URI property = connection.getValueFactory().createURI(rdf_property);

        RepositoryResult<Statement> triples = connection.getStatements(null, property, entity, false);
        try {
            if (triples.hasNext()) {
                Statement triple = triples.next();

                Value subject = triple.getSubject();

                if (returnType.isInstance(subject)) {
                    return returnType.cast(subject);
                } else {
                    log.error("cannot cast retrieved object {} for property {} to return type {}", subject, rdf_property, returnType);
                    return null;
                }

            } else {
                return null;
            }
        } finally {
            triples.close();
        }
    }

    /**
     * Return the all objects of type C that are reachable from entity by rdf_property. Returns
     * empty set if there is no such object or if the type of the object does not match the type
     * passed as argument.
     * 
     */
    private <C> Set<C> queryOutgoingAll(Resource entity, String rdf_property, Class<C> returnType) throws RepositoryException {
        final URI property = connection.getValueFactory().createURI(rdf_property);

        final Set<C> dupSet = new LinkedHashSet<C>();
        final RepositoryResult<Statement> triples = connection.getStatements(entity, property, null, false);
        try {
            while (triples.hasNext()) {
                Statement triple = triples.next();
                if (returnType.isInstance(triple.getObject())) {
                    dupSet.add(returnType.cast(triple.getObject()));
                }
            }
        } finally {
            triples.close();
        }

        return dupSet;

    }

    /**
     * Return the all objects of type C that are can reach the entity by rdf_property. Returns empty
     * set if there is no such object or if the type of the object does not match the type passed as
     * argument.
     * 
     */
    private <C> Set<C> queryIncomingAll(Resource entity, String rdf_property, Class<C> returnType) throws RepositoryException {
        final URI property = connection.getValueFactory().createURI(rdf_property);

        final Set<C> dupSet = new LinkedHashSet<C>();
        final RepositoryResult<Statement> triples = connection.getStatements(null, property, entity, false);
        try {
            while (triples.hasNext()) {
                Statement triple = triples.next();
                if (returnType.isInstance(triple.getSubject())) {
                    dupSet.add(returnType.cast(triple.getSubject()));
                }
            }
        } finally {
            triples.close();
        }

        return dupSet;
    }

    private Value createLiteral(Object o, Locale loc) {
        if (o instanceof Date) {
            return connection.getValueFactory().createLiteral(DateUtils.getXMLCalendar((Date) o));
        } else if (o instanceof DateTime) {
            return connection.getValueFactory().createLiteral(DateUtils.getXMLCalendar((DateTime) o));
        } else if (Integer.class.isAssignableFrom(o.getClass())) {
            return connection.getValueFactory().createLiteral((Integer) o);
        } else if (Long.class.isAssignableFrom(o.getClass())) {
            return connection.getValueFactory().createLiteral((Long) o);
        } else if (Double.class.isAssignableFrom(o.getClass())) {
            return connection.getValueFactory().createLiteral((Double) o);
        } else if (Float.class.isAssignableFrom(o.getClass())) {
            return connection.getValueFactory().createLiteral((Float) o);
        } else if (Boolean.class.isAssignableFrom(o.getClass())) {
            return connection.getValueFactory().createLiteral((Boolean) o);
        } else if (loc != null) {
            return connection.getValueFactory().createLiteral(o.toString(), loc.getLanguage());
        } else {
            return connection.getValueFactory().createLiteral(o.toString());
        }
    }

    private Set<String> getProperties(Resource entity, URI property, Locale loc, URI context) throws RepositoryException {
        final String lang = loc == null ? null : loc.getLanguage().toLowerCase();

        final Set<String> values = new HashSet<String>();
        final RepositoryResult<Statement> candidates = connection.getStatements(entity, property, null, false, context);
        try {
            while (candidates.hasNext()) {
                Statement triple = candidates.next();

                if (triple.getObject() instanceof Literal) {
                    Literal l = (Literal) triple.getObject();

                    if (lang == null || lang.equals(l.getLanguage())) {
                        values.add(l.stringValue());
                    }
                }
            }
        } finally {
            candidates.close();
        }

        return values;
    }

    private String getProperty(Resource entity, URI property, Locale loc, URI context) throws RepositoryException {
        Set<String> values = getProperties(entity, property, loc, context);

        if (values.size() > 0) {
            return values.iterator().next();
        } else {
            return null;
        }
    }
}
