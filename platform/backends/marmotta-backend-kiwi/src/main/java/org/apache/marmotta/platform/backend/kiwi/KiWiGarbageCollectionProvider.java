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

package org.apache.marmotta.platform.backend.kiwi;

import javax.enterprise.context.ApplicationScoped;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.platform.core.api.triplestore.GarbageCollectionProvider;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class KiWiGarbageCollectionProvider implements GarbageCollectionProvider {

    /**
     * Run garbage collection for the sail given as argument.
     *
     * @param sail
     */
    @Override
    public void garbageCollect(Sail sail) throws SailException {
        if(sail instanceof KiWiStore) {
            ((KiWiStore) sail).garbageCollect();
        }
    }
}
