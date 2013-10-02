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

package org.apache.marmotta.platform.core.api.triplestore;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

/**
 * This interface can be implemented by services in the backend in case the underlying triple store supports
 * explicit garbage colleciton
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface GarbageCollectionProvider {

    /**
     * Run garbage collection for the sail given as argument.
     *
     * @param sail
     */
    public void garbageCollect(Sail sail) throws SailException;
}
