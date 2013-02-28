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
package org.apache.marmotta.platform.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utilities for working with reflection
 * 
 * @author Sergio Fernández
 *
 */
public class ReflectionUtils {

    /**
     * Get the actual class
     * 
     * @param obj target object
     * @return actual class
     */
    public static Class<?> getClass(Object obj) {
        Class<?> cls = obj.getClass();
        while (isProxied(cls)) {
            cls = cls.getSuperclass();
        }
        return cls;
    }

    /**
     * Check is the class is proxies
     * 
     * @param cls class
     * @return proxied
     */
    public static boolean isProxied(Class<?> cls) {
        return cls.getName().contains("$$EnhancerByCGLIB$$") ||
                cls.getName().contains("$$FastClassByCGLIB$$") ||
                cls.getName().contains("_$$_javassist") ||
                cls.getName().contains("_$$_WeldSubclass") ||
                cls.getName().contains("$Proxy$");
    }

    /**
     * Retrieve the value of the annotation
     * 
     * @param obj target object
     * @param annotation target annotation
     * @param field annotation field
     * @return value (null if object not annotated)
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static Object getAnnotationValue(Object obj, Class<? extends Annotation> annotation, String field) throws IllegalArgumentException,
    IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class<?> cls = getClass(obj);
        if (cls.isAnnotationPresent(annotation))
            return invokeMethod(cls.getAnnotation(annotation), field);
        else
            return null;
    }

    /**
     * Get the method based on its name
     * 
     * @param cls target class
     * @param method method name
     * @param params parameters
     * @return method
     * @throws NoSuchMethodException
     */
    public static Method getMethod(Class<?> cls, String method, Object[] params) throws NoSuchMethodException {
        Class<?>[] classes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            classes[i] = params[i].getClass();
        }
        return cls.getMethod(method, classes);
    }

    /**
     * Invoke the method without parameters over the target object
     * 
     * @param obj target object
     * @param method method name
     * @return value returned by the method invocation
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static Object invokeMethod(Object obj, String method) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
    NoSuchMethodException {
        return invokeMethod(obj, method, new Object[0]);
    }

    /**
     * Invoke the method over the target object
     * 
     * @param obj target object
     * @param method method name
     * @param params parameters
     * @return value returned by the method invocation
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static Object invokeMethod(Object obj, String method, Object[] params) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException, NoSuchMethodException {
        return invokeMethod(obj, getMethod(getClass(obj), method, params), params);
    }

    /**
     * Invoke the method without parameters over the target object
     * 
     * @param obj target object
     * @param method method
     * @return value returned by the method invocation
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Object obj, Method method) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        return invokeMethod(obj, method, new Object[0]);
    }

    /**
     * Invoke the method over the target object
     * 
     * @param obj target object
     * @param method method
     * @param params parameters
     * @return value returned by the method invocation
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Object obj, Method method, Object[] params) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
        return method.invoke(obj, params);
    }

}
