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
package org.apache.marmotta.ldcache.backend.kiwi.sail;

import org.apache.marmotta.kiwi.sail.KiWiSailConnection;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCachingKiWiSail extends SailWrapper {

    private KiWiStore store;

    /**
     * Creates a new SailWrapper that wraps the supplied Sail.
     */
    public LDCachingKiWiSail(KiWiStore baseSail) {
        super(baseSail);

        this.store = baseSail;
    }

    @Override
    public LDCachingKiWiSailConnection getConnection() throws SailException {
        return new LDCachingKiWiSailConnection((KiWiSailConnection) store.getConnection());
    }

    @Override
    public void initialize() throws SailException {
        // ignore, because we assume that the wrapped store is already initialized
        if(!store.isInitialized()) {
            throw new SailException("the LDCachingKiWiSail is a secondary sail and requires an already initialized store!");
        }
    }

    @Override
    public void shutDown() throws SailException {
        // ignore, because we assume that the wrapped store will be shutdown by another sail
    }
}
