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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.repository.sail.SailRepository;

/**
 * Run the {@link org.openrdf.repository.RepositoryConnectionTest}s.
 * @author Jakob Frank <jakob@apache.org>
 *
 */
@RunWith(KiWiDatabaseRunner.class)
public class HotRodRepositoryConnectionTest extends RepositoryConnectionTest {

    @ClassRule
    public static HotRodServerRule hotrod = new HotRodServerRule(61222);

    private final KiWiConfiguration config;

    public HotRodRepositoryConnectionTest(KiWiConfiguration config) {
        this.config = config;
        config.setClusterAddress("127.0.0.1");
        config.setClustered(true);
        config.setClusterPort(61222);
        config.setClusterTimeout(1000);
        config.setCachingBackend(CachingBackends.INFINISPAN_HOTROD);
    }


    /* (non-Javadoc)
     * @see org.openrdf.repository.RepositoryConnectionTest#createRepository()
     */
    @Override
    protected Repository createRepository() throws Exception {
        hotrod.clearAll();

        config.setDefaultContext(null);
        KiWiStore store = new KiWiStore(config);
        store.setDropTablesOnShutdown(true);
        return new SailRepository(store);
    }

    @Ignore
    @Test
    @Override
    public void testOrderByQueriesAreInterruptable() throws Exception {
    }

    @Ignore("KiWi supports transaction isolation")
    @Test
    @Override
    public void testReadOfAddedStatement1() throws Exception {
    }

    @Ignore("KiWi supports transaction isolation")
    @Test
    @Override
    public void testReadOfAddedStatement2() throws Exception {
    }

}
