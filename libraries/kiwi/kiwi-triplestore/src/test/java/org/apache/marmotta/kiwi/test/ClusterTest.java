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

package org.apache.marmotta.kiwi.test;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.marmotta.kiwi.caching.KiWiCacheManager;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ClusterTest {

    private static Logger log = LoggerFactory.getLogger(ClusterTest.class);

    KiWiConfiguration config1, config2;

    KiWiStore store1, store2;

    Repository repository1, repository2;

    KiWiCacheManager cacheManager1, cacheManager2;

    @Before
    public void setup() throws RepositoryException {
        config1 = new KiWiConfiguration(
                "default-H2",
                "jdbc:h2:mem:kiwitest;MVCC=true;DB_CLOSE_ON_EXIT=TRUE;DB_CLOSE_DELAY=-1",
                "kiwi", "kiwi",
                new H2Dialect());
        config1.setDatacenterId(1);
        config1.setClustered(true);

        config2 = new KiWiConfiguration(
                "default-H2",
                "jdbc:h2:mem:kiwitest;MVCC=true;DB_CLOSE_ON_EXIT=TRUE;DB_CLOSE_DELAY=-1",
                "kiwi", "kiwi",
                new H2Dialect());
        config2.setDatacenterId(2);
        config2.setClustered(true);



    }

    public void setupCluster(int port1, int port2) throws RepositoryException {
        config1.setClusterPort(port1);
        config2.setClusterPort(port2);

        store1 = new KiWiStore(config1);
        store2 = new KiWiStore(config2);

        repository1 = new SailRepository(store1);
        repository2 = new SailRepository(store2);

        repository1.initialize();
        repository2.initialize();

        cacheManager1 = store1.getPersistence().getCacheManager();
        cacheManager2 = store2.getPersistence().getCacheManager();
    }


    @After
    public void teardown() throws RepositoryException {
        repository1.shutDown();
        repository2.shutDown();
    }


    @Test
    public void testClusteredCacheSync() throws InterruptedException, RepositoryException {
        setupCluster(61222,61222);

        log.info("testing cache synchronization ...");

        URI u = repository1.getValueFactory().createURI("http://localhost/test1");


        // give the cluster some time to performance asynchronous balancing
        Thread.sleep(100);


        log.debug("test if resource is in cache where it was created ...");
        URI u1 = (URI) cacheManager1.getUriCache().get(createCacheKey("http://localhost/test1"));

        Assert.assertNotNull(u1);
        Assert.assertEquals(u,u1);

        log.debug("test if resource has been synced to other cache in cluster ...");
        URI u2 = (URI) cacheManager2.getUriCache().get(createCacheKey("http://localhost/test1"));

        Assert.assertNotNull(u2);
        Assert.assertEquals(u,u2);
    }

    @Test
    public void testDisjointClusters() throws InterruptedException, RepositoryException {
        setupCluster(61224,61225);

        log.info("testing caches on different ports ...");

        URI u = repository1.getValueFactory().createURI("http://localhost/test1");


        // give the cluster some time to performance asynchronous balancing
        Thread.sleep(100);

        log.debug("test if resource is in cache where it was created ...");
        URI u1 = (URI) cacheManager1.getUriCache().get(createCacheKey("http://localhost/test1"));

        Assert.assertNotNull(u1);
        Assert.assertEquals(u,u1);

        log.debug("test if resource has been synced to other cache in cluster ...");
        URI u2 = (URI) cacheManager2.getUriCache().get(createCacheKey("http://localhost/test1"));

        Assert.assertNull(u2);
    }


    private static Long createCacheKey(String svalue) {
        Hasher hasher = Hashing.goodFastHash(64).newHasher();
        hasher.putString(svalue);
        return hasher.hash().asLong();
    }

}
