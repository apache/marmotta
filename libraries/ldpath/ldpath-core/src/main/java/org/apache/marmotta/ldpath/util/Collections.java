/*
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
package org.apache.marmotta.ldpath.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class Collections {

    /**
     * Copies all entries of the parsed collections to an new list
     * @param lists
     * @return
     */
    @SafeVarargs
    public static <T> List<T> concat(final Collection<T>... lists) {
        List<T> result = new ArrayList<T>();
        for(Collection<T> list : lists) {
            result.addAll(list);
        }
        return result;
    }
    /**
     * Returns an iterable over all lists without copying any data
     * @param lists the array with the lists
     * @return the plain iterator over all elements of the lists
     */
    @SafeVarargs
    public static <T> Iterator<T> iterator(final Collection<T>...lists){
        return iterator(0,lists);
    }
    /**
     * Returns an iterable over all lists starting by the parsed offset.
     * @param offset the offset of the first entry those elements should be
     * included in the returned entries
     * @param lists the array with the lists of elements
     * @return the plain iterator over all elements of the lists starting from
     * index offset
     */
    @SafeVarargs
    public static <T> Iterator<T> iterator(final int offset,final Collection<T>...lists){
        if(offset < 0){
            throw new IllegalArgumentException("The parsed Offest MUST NOT be < 0!");
        }
        if(lists == null){
            return null;
        } else if( lists.length <= offset){
            return java.util.Collections.<T>emptyList().iterator();
        }
        return new Iterator<T>() {
            
            private int listsIndex = offset-1;
            private Iterator<T> it;
            
            @Override
            public boolean hasNext() {
                while(it == null || !it.hasNext()){
                    listsIndex++;
                    if(listsIndex < lists.length ){
                        Collection<T> list = lists[listsIndex];
                        if(list != null){
                            it = lists[listsIndex].iterator();
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public T next() {
                if(it == null){
                    hasNext();
                }
                return it.next();
            }

            @Override
            public void remove() {
                if(it != null){
                    it.remove();
                }
            }};
    }
}
