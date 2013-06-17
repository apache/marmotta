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
package org.apache.marmotta.commons.sesame.facading.util;


import org.apache.commons.lang3.LocaleUtils;
import org.apache.marmotta.commons.sesame.facading.model.Facade;
import org.apache.marmotta.commons.util.DateUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

import java.lang.annotation.Annotation;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

/**
 * @author Sebastian Schaffert
 *
 */
public class FacadeUtils {

    /**
     * Check whether a type is a Facade, i.e. inherits from the {@link Facade} interface
     * 
     * @param <C>
     * @param clazz
     * @return
     */
    public static <C> boolean isFacade(Class<C> clazz) {
        return Facade.class.isAssignableFrom(clazz);
    }

    /**
     * Check whether a type is a {@link Value}.
     * 
     * @param <C>
     * @param clazz
     * @return
     */
    public static <C> boolean isValue(Class<C> clazz) {
        return Value.class.isAssignableFrom(clazz);
    }

    /**
     * Check whether a type is a {@link Resource}.
     * 
     * @param <C>
     * @param clazz
     * @return
     */
    public static <C> boolean isResource(Class<C> clazz) {
        return Resource.class.isAssignableFrom(clazz);
    }


    /**
     * Check whether a type is a {@link Facade}, i.e. the type or one of its superinterfaces has the
     * {@link Facade} annotation.
     * 
     * @param <C>
     * @param clazz
     * @return
     */
    public static <C> boolean isFacadeAnnotationPresent(Class<C> clazz, Class<? extends Annotation> annotation) {
        if (clazz.isAnnotationPresent(annotation)) {
            return true;
        } else {
            for(final Class<?> iface : clazz.getInterfaces()) {
                if(iface.isAnnotationPresent(annotation)) {
                    return true;
                }
            }
            if (clazz.getSuperclass() != null) {
                return isFacadeAnnotationPresent(clazz.getSuperclass(),annotation);
            }
            return false;
        }
    }


    public static <C extends Annotation,D> C getFacadeAnnotation(Class<D> clazz, Class<C> annotation) {
        if (clazz.isAnnotationPresent(annotation)) {
            return clazz.getAnnotation(annotation);
        } else {
            for(final Class<?> iface : clazz.getInterfaces()) {
                if(iface.isAnnotationPresent(annotation)) {
                    return iface.getAnnotation(annotation);
                }
            }
            if (clazz.getSuperclass() != null) {
                return getFacadeAnnotation(clazz.getSuperclass(),annotation);
            }
            return null;
        }

    }


    /**
     * Returns true if the <code>clazz</code> argument is a {@link Facade}, otherwise it returns
     * false.
     * 
     * @param in
     *            the argument to test.
     * @return true if the <code>clazz</code> argument is a {@link Facade}.
     */
    public static boolean isFacade(Object in) {

        if (in == null) {
            return false;
        }

        final Class<?> clazz = in.getClass();
        final boolean result = isFacade(clazz);

        return result;
    }

    /**
     * Check whether a type is a collection (List, Set, ...).
     *
     * @param <C> the type of the class modeled by the
     *            <code>clazz</code> argument Class object. For
     *            example, the type of String.class is Class
     *            &lt;String&gt;
     * @param clazz the type to test.
     * @return true if the type to test is a a type is a
     *         collection (List, Set, ...).
     */
    public static <C> boolean isCollection(Class<C> clazz) {

        if (clazz == null) {
            return false;
        }

        return Collection.class.isAssignableFrom(clazz);
    }


    /**
     * Returns true if the <code>clazz</code> argument is a:
     * <ul>
     * <li>a primitive
     * <li>a primitive wrapper
     * <li>a java.lang.Locale class
     * <li>a java.lang.Date class
     * <li>a java.lang.String class
     * </ul>
     * otherwise it returns false.
     * 
     * @param <C>
     *            the type of the class modeled by the <code>clazz</code> argument Class object. For
     *            example, the type of String.class is Class &lt;String&gt.
     * @param clazz
     *            the argument to test.
     * @return true if the <code>clazz</code> argument is a primitive, primitive wrapper, locale,
     *         date or String.
     */
    public static <C> boolean isBaseType(Class<C> clazz) {

        if (clazz == null) {
            return false;
        }

        final boolean isPrimitive = clazz.isPrimitive();
        if (isPrimitive) {
            return true;
        }

        // if I compare the Locale.class with the clazz argument
        // I can avoid the infamous case when the clazz is null,
        // the Locale.class.equals(null) is false, always - at
        // least this sustains the theory. The same logic for
        // the other equals realtions.
        final boolean isLocale = Locale.class.equals(clazz);
        if (isLocale) {
            return true;
        }

        final boolean isDate = Date.class.equals(clazz);
        if (isDate) {
            return true;
        }

        final boolean isString = String.class.equals(clazz);
        if (isString) {
            return true;
        }

        final boolean isBoolean = Boolean.class.equals(clazz);
        if (isBoolean) {
            return true;
        }

        final Class<? super C> superClass = clazz.getSuperclass();
        final boolean isNumber = Number.class.equals(superClass);
        if (isNumber) {
            return true;
        }

        // even if the char is a primitive is not a number
        final boolean isCharacter = Character.class.equals(clazz);
        if (isCharacter) {
            return true;
        }

        return false;
    }

    /**
     * Returns true if the <code>clazz</code> argument is a:
     * <ul>
     * <li>a primitive
     * <li>a primitive wrapper
     * </ul>
     * otherwise it returns false.
     *
     * @param <C> the type of the class modeled by the
     *            <code>clazz</code> argument Class object. For
     *            example, the type of String.class is Class
     *            &lt;String&gt.
     * @param clazz the argument to test.
     * @return true if the <code>clazz</code> argument is a
     *         primitive or primitive wrapper.
     */
    public static <C> boolean isPrimitive(Class<C> clazz) {

        if (clazz == null) {
            return false;
        }

        final boolean isPrimitive = clazz.isPrimitive();
        if (isPrimitive) {
            return true;
        }

        // even if the char is a primitive is not a number
        final boolean isCharacter = Character.class.equals(clazz);
        if (isCharacter) {
            return true;
        }

        final Class<? super C> superClass = clazz.getSuperclass();
        final boolean isNumber = Number.class.equals(superClass);
        if (isNumber) {
            return true;
        }

        return false;
    }

    /**
     * Returns true if the <code>in</code> argument is a:
     * <ul>
     * <li>a primitive
     * <li>a primitive wrapper
     * </ul>
     * otherwise it returns false.
     *
     * @param in the argument to test.
     * @return true if the <code>clazz</code> argument is a
     *         primitive or primitive wrapper.
     */
    public static boolean isPrimitive(Object in) {
        if (in == null) {
            return false;
        }

        final Class<?> clazz = in.getClass();
        return isPrimitive(clazz);
    }


    /**
     * Transform a value passed as string to the base type (i.e. non-complex type) given as argument
     *
     * @param <T>
     * @param value
     * @param returnType
     * @return
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public static <T> T transformToBaseType(String value, Class<T> returnType) throws IllegalArgumentException {
        // transformation to appropriate primitive type
        /*
         * README: the "dirty" cast: "(T) x" instead of "returnType.cast(x)" is required since
         * .cast does not work for primitive types (int, double, float, etc...).
         * Somehow it results in a ClassCastException
         */
        if(Integer.class.equals(returnType) || int.class.equals(returnType)) {
            if(value == null) {
                return (T)(Integer)(0);
            }
            return (T)(Integer.decode(value));
        } else if(Long.class.equals(returnType) || long.class.equals(returnType)) {
            if(value == null) {
                return (T)(Long)(0L);
            }
            return (T)(Long.decode(value));
        } else if(Double.class.equals(returnType) || double.class.equals(returnType)) {
            if(value == null) {
                return (T)(Double)(0.0);
            }
            return (T)(Double.valueOf(value));
        } else if(Float.class.equals(returnType) || float.class.equals(returnType)) {
            if(value == null) {
                return (T)(Float)(0.0F);
            }
            return (T)(Float.valueOf(value));
        } else if(Byte.class.equals(returnType) || byte.class.equals(returnType)) {
            if(value == null) {
                return (T)(Byte)((byte) 0);
            }
            return (T)(Byte.decode(value));
        } else if(Boolean.class.equals(returnType) || boolean.class.equals(returnType)) {
            return (T)(Boolean.valueOf(value));
        } else if(Character.class.equals(returnType) || char.class.equals(returnType)) {
            if(value == null) {
                if (Character.class.equals(returnType)){
                    return null;
                } else {
                    return (T) new Character((char) 0);
                }
            } else if(value.length() > 0) {
                return (T)(Character)(value.charAt(0));
            } else {
                return null;
            }
        } else if (Locale.class.equals(returnType)) {
            if(value == null) {
                return null;
            } else {
                return returnType.cast(LocaleUtils.toLocale(value));
            }
        } else if (Date.class.equals(returnType)) {
            if(value == null) {
                return null;
            } else {
                try {
                    return returnType.cast(DateUtils.ISO8601FORMAT.parse(value));
                } catch (final ParseException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } else if(String.class.equals(returnType)) {
            return returnType.cast(value);
        } else {
            throw new IllegalArgumentException("primitive type "+returnType.getName()+" not supported by transformation");
        }
    }


}
