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
package org.apache.marmotta.commons.sesame.filter;

/**
 * Generic superinterface for all filters. Only defines an accept() method.
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public interface SesameFilter<T> {

    /**
     * Return false in case the filter does not accept the object passed as argument, true otherwise.
     *
     *
     * @param object the object to check
     * @return true in case the object is accepted, false otherwise
     */
    public boolean accept(T object);

}
