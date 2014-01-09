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

import org.apache.marmotta.commons.sesame.facading.util.FacadeUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;

class FacadingInvocationHelper {
    private FacadingInvocationHelper() {
        // static Non-Instance Util Class
    }

    static boolean checkMethodSig(Method method, String name, int argNum) {
        // Check the name
        if (!method.getName().equals(name)) { return false; }

        // Check # of arguments
        final Class<?>[] pTypes = method.getParameterTypes();
        if (pTypes.length != argNum) { return false; }

        return true;
    }

    static boolean checkMethodSig(Method method, String name, Class<?>... args) {
        // Do the basic check
        if (!checkMethodSig(method, name, args.length)) {
            return false;
        }

        // Check for the right parameters
        final Class<?>[] pTypes = method.getParameterTypes();
        for (int i = 0; i < pTypes.length; i++) {
            Class<?> p = pTypes[i], a = args[i];
            if (!p.isAssignableFrom(a)) { return false; }
        }

        return true;
    }

    static <A extends Annotation> A getAnnotation(Method method, Class<A> annotation) {
        if (method.isAnnotationPresent(annotation)) { return method.getAnnotation(annotation); }

        final String field = getBaseName(method);
        Class<?> clazz = method.getDeclaringClass();

        for (Method m : clazz.getMethods()) {
            final boolean multiValue = isMultiValue(m);
            if (m.isAnnotationPresent(annotation)) {
                for (String op : FacadingInvocationHandler.OPERATOR.getOperatorPrefixes()) {
                    if (m.getName().equals(op + field)) {
                        return m.getAnnotation(annotation);
                    } else if (multiValue && m.getName().equals(op + field + "s")) {
                        return m.getAnnotation(annotation);
                    } else {}
                }
            }
        }

        return null;
    }

    static String getBaseName(Method method) {
        final String name = method.getName();
        final boolean isMultiValue = isMultiValue(method);

        String bName = null;
        final String[] prefixes = FacadingInvocationHandler.OPERATOR.getLengthSortedOperatorPrefixes();
        for (String op : prefixes) {
            if (name.startsWith(op)) {
                if (isMultiValue && name.endsWith("s")) {
                    bName = name.substring(op.length(), name.length() - 1);
                    break;
                } else {
                    bName = name.substring(op.length());
                    break;
                }
            }
        }
        return bName != null ? bName : name;
    }

    static boolean isMultiValue(Method method) {
        final FacadingInvocationHandler.OPERATOR oper = FacadingInvocationHandler.OPERATOR.getOperator(method);
        final boolean isMultiValue = oper.writeOp && method.getParameterTypes().length == 0 ||
                FacadeUtils.isCollection(oper.writeOp && oper.numArgs > 0 ? method.getParameterTypes()[0] : method.getReturnType());
        return isMultiValue;
    }

    static boolean checkLocale(final Locale loc, final Value object) {
        // Only literals can have a lang-tag
        if (!(object instanceof Literal)) { return false; }

        // Empty locale always matches
        if (loc == null) { return true; }

        return loc.getLanguage().equals(((Literal) object).getLanguage());
    }

    static <C extends Collection<?>, E> Collection<E> createCollection(Class<C> collectionType, Collection<? extends E> elements)
            throws IllegalAccessException, InstantiationException {

        final Collection<E> result;

        // If the collectionType is Abstract (or an Interface) we try to guess a valid
        // implementation...
        if (Modifier.isAbstract(collectionType.getModifiers())) {
            // FIXME: Maybe we should add some more implementations here?
            if (collectionType.isAssignableFrom(HashSet.class)) {
                result = new HashSet<E>();
            } else if (collectionType.isAssignableFrom(LinkedList.class)) {
                result = new LinkedList<E>();
            } else {
                throw new InstantiationException("Could not find an implementation of " + collectionType.getName());
            }
        } else {
            result = createInstance(collectionType);
        }

        if (elements != null) {
            result.addAll(elements);
        }

        return result;

    }

    @SuppressWarnings("unchecked")
    static <E, C extends Collection<?>> Collection<E> createInstance(Class<C> collectionType) throws InstantiationException, IllegalAccessException {
        return (Collection<E>) collectionType.newInstance();
    }
}
