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
package org.apache.marmotta.commons.collections;

import com.google.common.base.Equivalence;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class contains static helper methods for supporting java collections
 * in Java 5.
 *
 * @author Sebastian Schaffert
 */
public class CollectionUtils {

    /**
     * Convert any iterable into a list
     * @param <T>
     * @param iterable
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(Iterable<T> iterable) {
        return toCollection(LinkedList.class,iterable);
    }

    /**
     * Convert any iterable into a set
     * @param <T>
     * @param iterable
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> toSet(Iterable<T> iterable) {
        return toCollection(HashSet.class,iterable);
    }

    private static <C extends Collection<T>,T> C toCollection(Class<C> cls, Iterable<T> iterable) {
        try {
            C result = cls.newInstance();

            for(T item : iterable) {
                result.add(item);
            }

            return result;
        } catch(InstantiationException ex) {
            return null;
        } catch(IllegalAccessException ex) {
            return null;
        }
    }

    public static <T> T first(Iterable<T> iterable) {
        return iterable.iterator().next();
    }

    public static <T> String fold(T[] elements, String separator) {
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<elements.length; i++) {
            builder.append(elements[i].toString());
            if(i < elements.length-1) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }


    public static <T> String fold(Collection<T> elements, String separator) {
        StringBuilder builder = new StringBuilder();
        ArrayList<T> list = new ArrayList<T>(elements);
        for(int i=0; i<list.size(); i++) {
            builder.append(list.get(i).toString());
            if(i < list.size()-1) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }


    public static <T> String fold(Collection<T> elements, Format format, String separator) {
        StringBuilder builder = new StringBuilder();
        ArrayList<T> list = new ArrayList<T>(elements);
        for(int i=0; i<list.size(); i++) {
            builder.append(format.format(list.get(i)));
            if(i < list.size()-1) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    public static <T> String fold(Collection<T> elements, StringSerializer<T> format, String separator) {
        StringBuilder builder = new StringBuilder();
        ArrayList<T> list = new ArrayList<T>(elements);
        for(int i=0; i<list.size(); i++) {
            builder.append(format.serialize(list.get(i)));
            if(i < list.size()-1) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }


    /**
     * Concatenate the collection of lists passed as argument into a new array list.
     * @param lists
     * @param <T>
     * @return
     */
    public static <T> List<T> concat(Collection<? extends Collection<T>> lists) {
        int size = 0;
        for(Collection<T> list : lists) {
            size += list.size();
        }
        List<T> result = new ArrayList<T>(size);
        for(Collection<T> list : lists) {
            result.addAll(list);
        }
        return result;
    }

    /**
     * Allows a custom serialization of objects of type T to a string.
     *
     * @param <T> the object type to serialize as string
     */
    public interface StringSerializer<T> {
        public String serialize(T t);
    }

}
