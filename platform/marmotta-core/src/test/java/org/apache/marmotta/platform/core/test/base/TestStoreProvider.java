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

package org.apache.marmotta.platform.core.test.base;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import org.apache.marmotta.platform.core.api.triplestore.StoreProvider;
import org.apache.marmotta.platform.core.qualifiers.inject.Fallback;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.NotifyingSail;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A triple store provider for tests. Uses a Sesame in-memory store for holding triples.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@Alternative
@Fallback
@ApplicationScoped
public class TestStoreProvider implements StoreProvider {

    private static Logger log = LoggerFactory.getLogger(TestStoreProvider.class);

    /**
     * Create the repository using the sail given as argument. This method is needed because some backends
     * use custom implementations of SailRepository.
     *
     * @param sail
     * @return
     */
    @Override
    public SailRepository createRepository(Sail sail) {
        log.warn("using in-memory triple store - no persistence");
        return new SailRepository(sail);
    }

    /**
     * Create the store provided by this SailProvider
     *
     * @return a new instance of the store
     */
    @Override
    public NotifyingSail createStore() {
        return new MemoryStore();
    }

    /**
     * Return the name of the provider. Used e.g. for displaying status information or logging.
     *
     * @return
     */
    @Override
    public String getName() {
        return "Test Store";
    }

    /**
     * Return true if this sail provider is enabled in the configuration.
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}
