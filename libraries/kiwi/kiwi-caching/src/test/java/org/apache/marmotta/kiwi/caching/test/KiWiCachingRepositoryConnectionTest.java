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

package org.apache.marmotta.kiwi.caching.test;

import org.apache.marmotta.kiwi.caching.config.KiWiQueryCacheConfiguration;
import org.apache.marmotta.kiwi.caching.sail.KiWiCachingSail;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.repository.sail.SailRepository;

/**
 * Run the {@link RepositoryConnectionTest}s.
 * @author Jakob Frank <jakob@apache.org>
 *
 */
@RunWith(KiWiDatabaseRunner.class)
@Ignore
public class KiWiCachingRepositoryConnectionTest extends RepositoryConnectionTest {

    private final KiWiConfiguration config;

    public KiWiCachingRepositoryConnectionTest(KiWiConfiguration config) {
        this.config = config;
    }
    
    /* (non-Javadoc)
     * @see org.openrdf.repository.RepositoryConnectionTest#createRepository()
     */
    @Override
    protected Repository createRepository() throws Exception {
        config.setDefaultContext(null);
        KiWiStore store = new KiWiStore(config);
        store.setDropTablesOnShutdown(true);

        KiWiCachingSail cache = new KiWiCachingSail(store, new KiWiQueryCacheConfiguration());
        return new SailRepository(cache);
    }

}
