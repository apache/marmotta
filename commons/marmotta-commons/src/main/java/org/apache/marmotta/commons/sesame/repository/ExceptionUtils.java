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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ExceptionUtils {


    /**
     * Handle repository exceptions in a uniform way. The output will we written to the logger of the ExceptionUtils
     * class.
     *
     * @param ex
     */
    public static void handleRepositoryException(RepositoryException ex) {
        handleRepositoryException(ex,null);
    }

        /**
        * Handle repository exceptions in a uniform way. If the optional caller class is passed as argument,
        * the logger of this class will be chosen for output, otherwise the output will come from the ExceptionUtils
        * class.
        *
        * @param ex
        */
    public static void handleRepositoryException(RepositoryException ex, Class<?> caller) {
        Logger log;
        if(caller != null) {
            log = LoggerFactory.getLogger(caller);
        } else {
            log = LoggerFactory.getLogger(ExceptionUtils.class);
        }
        log.error("error accessing RDF repository",ex);
    }
}
