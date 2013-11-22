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

package org.apache.marmotta.kiwi.exception;

import java.sql.SQLException;

/**
 * Used to signal that retrieving the results has been interrupted in one or the other way. Necessary to
 * throw the proper interrupted exceptions when interrupting SPARQL queries.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ResultInterruptedException extends SQLException {

    /**
     * Constructs a <code>SQLException</code> object with a given
     * <code>reason</code>. The  <code>SQLState</code>  is initialized to
     * <code>null</code> and the vender code is initialized to 0.
     * <p/>
     * The <code>cause</code> is not initialized, and may subsequently be
     * initialized by a call to the
     * {@link Throwable#initCause(Throwable)} method.
     * <p/>
     *
     * @param reason a description of the exception
     */
    public ResultInterruptedException(String reason) {
        super(reason,"57014");
    }
}
