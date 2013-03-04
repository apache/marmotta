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
package org.apache.marmotta.commons.sesame.repository;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;

/**
 * Some static utility methods for working with RepositoryResults
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ResultUtils {

    /**
     * Unwrap a repository result, providing a standard Java iterator that can be used e.g. in foreach loops.
     * <p/>
     * The underlying repository result will be closed automatically when the last result element has been consumed.
     * For this reason it is mandatory that the iteration is performed completely by the caller.
     * <p/>
     * In case an exception occurs while iterating, an error message is logged and the next() method will return null,
     * the hasNext() method will return false.
     *
     * @param result
     * @param <T>
     * @return
     */
    public static <T> Iterator<T> unwrap(final RepositoryResult<T> result) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                if(result.isClosed()) {
                    return false;
                }
                try {
                    return result.hasNext();
                } catch (RepositoryException e) {
                    ExceptionUtils.handleRepositoryException(e, ResourceUtils.class);
                    return false;
                }
            }

            @Override
            public T next() {
                try {
                    T next = result.next();
                    if(!result.hasNext()) {
                        result.close();
                    }
                    return next;
                } catch (RepositoryException e) {
                    handleRepositoryException(e, ResourceUtils.class);
                    return null;
                }
            }

            @Override
            public void remove() {
                try {
                    result.remove();
                } catch (RepositoryException e) {
                    handleRepositoryException(e, ResourceUtils.class);
                }
            }

            @Override
            protected void finalize() throws Throwable {
                if(!result.isClosed())
                    result.close();
                super.finalize();
            }
        };

    }

    /**
     * Unwrap a repository result, providing a standard Java iterable that can be used e.g. in foreach loops.
     * <p/>
     * The underlying repository result will be closed automatically when the last result element has been consumed.
     * For this reason it is mandatory that the iteration is performed completely by the caller.
     * <p/>
     * In case an exception occurs while iterating, an error message is logged and the next() method will return null,
     * the hasNext() method will return false.
     *
     * @param result
     * @param <T>
     * @return
     */
    public static <T> Iterable<T> iterable(final RepositoryResult<T> result) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return unwrap(result);
            }
        };
    }


    /**
     * Workaround for https://openrdf.atlassian.net/browse/SES-1702 in Sesame 2.7.0-beta1
     * @param <E>
     * @return
     */
    public static <E> List<E> asList(RepositoryResult<E> result) throws RepositoryException {
        ArrayList<E> collection = new ArrayList<E>();
        try {
            while (result.hasNext()) {
                collection.add(result.next());
            }

            return collection;
        }
        finally {
            result.close();
        }
    }

}
