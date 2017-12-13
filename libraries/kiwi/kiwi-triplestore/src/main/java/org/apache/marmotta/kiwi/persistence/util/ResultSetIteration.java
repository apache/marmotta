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
package org.apache.marmotta.kiwi.persistence.util;

import info.aduna.iteration.CloseableIteration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 * A wrapper class for creating ClosableIterations from a SQL JDBC ResultSet. Takes a
 * Guava Function as transformer from result rows to entities.
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ResultSetIteration<E> implements CloseableIteration<E,SQLException> {

    /**
     * The result set wrapped by this iteration
     */
    ResultSet result;

    /**
     * A function for transforming a row in the result set to the desired result type
     */
    ResultTransformerFunction<E> transformer;


    // a flag indicating whether the result set has already been moved to the next row, but the row has
    // not yet been consumed; used to simualte iterator behaviour over the result set
    boolean moved = false;

    // a flag indicating whether the result set has already been closed
    boolean closed = false;

    // set to true if the statement should also be closed when the iteration is closed
    boolean closeStatement = false;

    public ResultSetIteration(ResultSet result, ResultTransformerFunction<E> transformer) {
        this.result = result;
        this.transformer = transformer;
    }

    public ResultSetIteration(ResultSet result, boolean closeStatement, ResultTransformerFunction<E> transformer) {
        this.result = result;
        this.transformer = transformer;
        this.closeStatement = closeStatement;
    }

    /**
     * Closes this iteration, freeing any resources that it is holding. If the
     * iteration has already been closed then invoking this method has no effect.
     */
    @Override
    public void close() throws SQLException {
        try {
            result.close();
            if(closeStatement)
                result.getStatement().close();
            closed = true;
        } catch (SQLException ignore) {
        }
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if {@link #next} would return an element
     * rather than throwing a <tt>NoSuchElementException</tt>.)
     *
     * @return <tt>true</tt> if the iteration has more elements.
     * @throws SQLException
     */
    @Override
    public boolean hasNext() throws SQLException {
        if(closed) {
            return false;
        } else if(moved) {
            return true;
        } else if(result.next()) {
            moved = true;
            return true;
        } else {
            close();
            return false;
        }
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException if the iteration has no more elements or if it has been closed.
     */
    @Override
    public E next() throws SQLException {
        if(moved || result.next()) {
            moved = false;
            return transformer.apply(result);
        } else {
            close();
            throw new NoSuchElementException("no more results");
        }
    }

    @Override
    public void remove() throws SQLException {
        throw new UnsupportedOperationException("removing result rows not supported");
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

}
