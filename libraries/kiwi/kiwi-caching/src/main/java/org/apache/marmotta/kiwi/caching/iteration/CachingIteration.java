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

package org.apache.marmotta.kiwi.caching.iteration;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIterationBase;
import info.aduna.iteration.CloseableIteratorIteration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class CachingIteration<E,X extends Exception> extends CloseableIterationBase<E,X> implements CloseableIteration<E,X> {

    private static Logger log = LoggerFactory.getLogger(CachingIteration.class);

    private CloseableIteration<E,X> wrapped;

    private CacheFunction<E> cacheFunction;

    public CachingIteration(CacheFunction<E> cacheFunction,BufferingIterationProducer<E, X> producer) throws X {

        this.cacheFunction = cacheFunction;

        List<E> cached = cacheFunction.getResult();
        if(cached != null) {
            log.debug("cache hit, using iterator over cached result (size={})!", cached.size());
            this.wrapped = new CloseableIteratorIteration<>(cached.iterator());
        } else {
            log.debug("cache miss, querying backend!");
            this.wrapped = producer.getIteration();
        }
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if {@link #next} would return an element
     * rather than throwing a <tt>NoSuchElementException</tt>.)
     *
     * @return <tt>true</tt> if the iteration has more elements.
     * @throws X
     */
    @Override
    public boolean hasNext() throws X {
        return wrapped.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     */
    @Override
    public E next() throws X {
        return wrapped.next();
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iteration (optional operation). This method can be called only once per
     * call to next.
     *
     * @throws UnsupportedOperationException if the remove operation is not supported by this Iteration.
     * @throws IllegalStateException         If the Iteration has been closed, or if <tt>next()</tt> has not
     *                                       yet been called, or <tt>remove()</tt> has already been called
     *                                       after the last call to <tt>next()</tt>.
     */
    @Override
    public void remove() throws X {
        wrapped.remove();
    }

    /**
     * Called by {@link #close} when it is called for the first time. This method
     * is only called once on each iteration. By default, this method does
     * nothing.
     *
     * @throws X
     */
    @Override
    protected void handleClose() throws X {
        if(wrapped instanceof BufferingIteration && ((BufferingIteration) wrapped).getBuffer() != null) {
            cacheFunction.cacheResult(((BufferingIteration) wrapped).getBuffer());
        }

        super.handleClose();
    }


    public static interface BufferingIterationProducer<E,X extends Exception> {

        /**
         * This method should lazily create the iteration wrapped by this caching iteration.
         * @return
         */
        public BufferingIteration<E,X> getIteration() throws X;

    }


    public static interface CacheFunction<E> {

        /**
         * Return the cached result for this iteration (or null in case there is no cached result)
         */
        public List<E> getResult();

        /**
         * Cache the result of this iteration.
         *
         * @param buffer
         */
        public void cacheResult(List<E> buffer);

    }
}
