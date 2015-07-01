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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A functor for transforming result set rows into entities. Should be implemented by callers of IterationFromResultSet
 * <p/>
 * Author: Sebastian Schaffert
 */
public interface ResultTransformerFunction<E> {

    /**
     * Transform the result row into an entity of type E.
     *
     * @param row the row to transform, should not be modified
     * @return an entity of type E constructed from the result row
     * @throws SQLException if a database error occurs when transforming the row
     */
    public E apply(ResultSet row) throws SQLException;

}
