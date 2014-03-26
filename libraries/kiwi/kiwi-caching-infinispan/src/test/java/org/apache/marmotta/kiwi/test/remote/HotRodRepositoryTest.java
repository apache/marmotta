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

package org.apache.marmotta.kiwi.test.remote;

import org.apache.marmotta.kiwi.config.CachingBackends;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryTest;
import org.openrdf.repository.sail.SailRepository;

/**
 * Run the {@link org.openrdf.repository.RepositoryTest}s.
 * @author Jakob Frank <jakob@apache.org>
 *
 */
@RunWith(KiWiDatabaseRunner.class)
public class HotRodRepositoryTest extends RepositoryTest {

    @ClassRule
    public static HotRodServerRule hotrod = new HotRodServerRule(61222);

    private final KiWiConfiguration config;

    private KiWiStore store;

    public HotRodRepositoryTest(KiWiConfiguration config) {
        this.config = config;

        config.setClusterAddress("127.0.0.1");
        config.setClustered(true);
        config.setClusterPort(61222);
        config.setClusterTimeout(10000);
        config.setCachingBackend(CachingBackends.INFINISPAN_HOTROD);
    }



    /* (non-Javadoc)
     * @see org.openrdf.repository.RepositoryTest#createRepository()
     */
    @Override
    protected Repository createRepository() throws Exception {
        hotrod.clearAll();

        store = new KiWiStore(config);
        return new SailRepository(store);
    }

    @Override
    public void tearDown() throws Exception {
        store.getPersistence().dropDatabase();
        super.tearDown();
    }
}
