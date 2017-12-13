/*
 * Copyright 2004 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.sun.syndication.feed.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Obtains all property descriptors from a bean (interface or implementation).
 * <p>
 * The java.beans.Introspector does not process the interfaces hierarchy chain, this one does.
 * <p>
 * @author Alejandro Abdelnur
 *
 */
public class BeanIntrospector {

    private static final Map _introspected = new HashMap();

    public static synchronized PropertyDescriptor[] getPropertyDescriptors(Class klass) throws IntrospectionException {
        PropertyDescriptor[] descriptors = (PropertyDescriptor[]) _introspected.get(klass);
        if (descriptors==null) {
            descriptors = getPDs(klass);
            _introspected.put(klass,descriptors);
        }
        return descriptors;
    }

    private static PropertyDescriptor[] getPDs(Class klass) throws IntrospectionException {
        Method[] methods = klass.getMethods();
        Map getters = getPDs(methods,false);
        Map setters = getPDs(methods,true);
        List pds     = merge(getters,setters);
        PropertyDescriptor[] array = new PropertyDescriptor[pds.size()];
        pds.toArray(array);
        return array;
    }

    private static final String SETTER = "set";
    private static final String GETTER = "get";
    private static final String BOOLEAN_GETTER = "is";

    private static Map getPDs(Method[] methods,boolean setters) throws IntrospectionException {
        Map pds = new HashMap();
        for (Method method : methods) {
            String pName = null;
            PropertyDescriptor pDescriptor = null;
            if ((method.getModifiers() & Modifier.PUBLIC) != 0) {
                if (setters) {
                    if (method.getName().startsWith(SETTER) &&
                            method.getReturnType() == void.class && method.getParameterTypes().length == 1) {
                        pName = Introspector.decapitalize(method.getName().substring(3));
                        pDescriptor = new PropertyDescriptor(pName, null, method);
                    }
                } else {
                    if (method.getName().startsWith(GETTER) &&
                            method.getReturnType() != void.class && method.getParameterTypes().length == 0) {
                        pName = Introspector.decapitalize(method.getName().substring(3));
                        pDescriptor = new PropertyDescriptor(pName, method, null);
                    } else if (method.getName().startsWith(BOOLEAN_GETTER) &&
                            method.getReturnType() == boolean.class && method.getParameterTypes().length == 0) {
                        pName = Introspector.decapitalize(method.getName().substring(2));
                        pDescriptor = new PropertyDescriptor(pName, method, null);
                    }
                }
            }
            if (pName != null) {
                pds.put(pName, pDescriptor);
            }
        }
        return pds;
    }

    private static List merge(Map getters,Map setters) throws IntrospectionException {
        List props = new ArrayList();
        Set processedProps = new HashSet();
        for (Object o : getters.keySet()) {
            String name = (String) o;
            PropertyDescriptor getter = (PropertyDescriptor) getters.get(name);
            PropertyDescriptor setter = (PropertyDescriptor) setters.get(name);
            if (setter != null) {
                processedProps.add(name);
                PropertyDescriptor prop = new PropertyDescriptor(name, getter.getReadMethod(), setter.getWriteMethod());
                props.add(prop);
            } else {
                props.add(getter);
            }
        }
        Set writeOnlyProps = new HashSet(setters.keySet());
        writeOnlyProps.removeAll(processedProps);
        for (Object writeOnlyProp : writeOnlyProps) {
            String name = (String) writeOnlyProp;
            PropertyDescriptor setter = (PropertyDescriptor) setters.get(name);
            props.add(setter);
        }
        return props;
    }

}
