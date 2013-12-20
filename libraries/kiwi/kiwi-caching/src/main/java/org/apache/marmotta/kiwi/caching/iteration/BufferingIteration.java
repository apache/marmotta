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

import java.util.ArrayList;
import java.util.List;

/**
 * An iterator that buffers iteration results up to a configurable limit.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class BufferingIteration<E,X extends Exception> extends CloseableIterationBase<E,X> implements CloseableIteration<E,X> {

    private int limit = 150;

    private List<E> buffer;

    private CloseableIteration<? extends E,X> wrapped;

    public BufferingIteration(int limit, CloseableIteration<? extends E, X> wrapped) {
        this.limit = limit;

        this.wrapped = wrapped;
        this.buffer  = new ArrayList<>(limit);
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
        E n = wrapped.next();

        if(buffer != null && buffer.size() < limit) {
            buffer.add(n);
        } else {
            buffer = null;
        }

        return n;
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
        buffer.remove(buffer.size() - 1);
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

        super.handleClose();
    }

    /**
     * Return the buffer contents (or null if the buffer has reached its limit)
     *
     * @return
     */
    public List<E> getBuffer() {
        return buffer;
    }

    public int getLimit() {
        return limit;
    }
}
