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

package org.apache.marmotta.kiwi.test.cluster;

import org.apache.marmotta.kiwi.caching.CacheManager;
import org.apache.marmotta.kiwi.config.CachingBackends;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.model.rdf.KiWiAnonResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.junit.AfterClass;
import org.junit.Assert;
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
public abstract class BaseClusterTest {

    public static final int REGISTRY_TESTS = 10000;
    private static Logger log = LoggerFactory.getLogger(BaseClusterTest.class);

    private static int datacenterIds = 1;

    protected static Repository repositorySync1, repositorySync2, repositoryAsync1, repositoryAsync2;

    private static CacheManager cacheManagerSync1, cacheManagerSync2, cacheManagerAsync1, cacheManagerAsync2;


    @AfterClass
    public static void teardown() throws RepositoryException {
        repositorySync1.shutDown();
        repositorySync2.shutDown();
        repositoryAsync1.shutDown();
        repositoryAsync2.shutDown();
    }


    @Test
    public void testClusteredCacheUri() throws InterruptedException, RepositoryException {

        log.info("testing cache synchronization ...");

        KiWiUriResource u = (KiWiUriResource) repositorySync1.getValueFactory().createURI("http://localhost/test1");


        // give the cluster some time to performance asynchronous balancing
        Thread.sleep(100);


        log.debug("test if resource is in cache where it was created ...");
        KiWiUriResource u1 = cacheManagerSync1.getUriCache().get("http://localhost/test1");

        Assert.assertNotNull(u1);
        Assert.assertEquals(u, u1);
        Assert.assertEquals(u.getId(), u1.getId());

        log.debug("test if resource has been synced to other cache in cluster ...");
        KiWiUriResource u2 = cacheManagerSync2.getUriCache().get("http://localhost/test1");

        Assert.assertNotNull(u2);
        Assert.assertEquals(u, u2);
        Assert.assertEquals(u.getId(), u2.getId());
    }


    @Test
    public void testClusteredCacheBNode() throws InterruptedException, RepositoryException {

        log.info("testing cache synchronization ...");

        KiWiAnonResource u = (KiWiAnonResource) repositorySync1.getValueFactory().createBNode();


        // give the cluster some time to performance asynchronous balancing
        Thread.sleep(100);


        log.debug("test if resource is in cache where it was created ...");
        KiWiAnonResource u1 = cacheManagerSync1.getBNodeCache().get(u.getID());

        Assert.assertNotNull(u1);
        Assert.assertEquals(u,u1);
        Assert.assertEquals(u.getId(), u1.getId());

        log.debug("test if resource has been synced to other cache in cluster ...");
        KiWiAnonResource u2 = cacheManagerSync2.getBNodeCache().get(u.getID());

        Assert.assertNotNull(u2);
        Assert.assertEquals(u, u2);
        Assert.assertEquals(u.getId(), u2.getId());
    }


    @Test
    public void testDisjointClusters() throws InterruptedException, RepositoryException {

        log.info("testing caches on different ports ...");

        URI u = repositoryAsync1.getValueFactory().createURI("http://localhost/test1");


        // give the cluster some time to performance asynchronous balancing
        Thread.sleep(100);

        log.debug("test if resource is in cache where it was created ...");
        URI u1 = (URI) cacheManagerAsync1.getUriCache().get("http://localhost/test1");

        Assert.assertNotNull(u1);
        Assert.assertEquals(u,u1);

        log.debug("test if resource has been synced to other cache in cluster ...");
        URI u2 = (URI) cacheManagerAsync2.getUriCache().get("http://localhost/test1");

        Assert.assertNull(u2);
    }


    @Test
    public void testRegistry() {

        log.info("testing synchronized registry ...");

        for(int i=0; i < REGISTRY_TESTS; i++) {
            cacheManagerSync1.getRegistryCache().put((long)i,(long)i);

            Long j = cacheManagerSync1.getRegistryCache().get((long)i);
            Long k = cacheManagerSync2.getRegistryCache().get((long)i);

            Assert.assertEquals("objects in same cache were not identical!", (long)i, (long)j);
            Assert.assertEquals("objects in caches 1 and 2 were not identical!", (long)i, (long)k);
        }

    }

    protected static class ClusterTestSupport {

        CachingBackends type;

        public ClusterTestSupport(CachingBackends type) {
            this.type = type;
        }

        public void setup() {
            setup(null);
        }

        public void setup(KiWiConfiguration base) {
            try {
                repositorySync1 = createConfiguration(base,61222);
                repositorySync2 = createConfiguration(base,61222);
                repositoryAsync1 = createConfiguration(base,61223);
                repositoryAsync2 = createConfiguration(base,61224);

                cacheManagerSync1 = getCacheManager(repositorySync1);
                cacheManagerSync2 = getCacheManager(repositorySync2);
                cacheManagerAsync1 = getCacheManager(repositoryAsync1);
                cacheManagerAsync2 = getCacheManager(repositoryAsync2);


            } catch (RepositoryException ex) {
                Assert.fail(ex.getMessage());
            }
        }

        public KiWiConfiguration buildBaseConfiguration() {
            return new KiWiConfiguration(
                    "default-H2",
                    "jdbc:h2:mem:kiwitest;MVCC=true;DB_CLOSE_ON_EXIT=TRUE;DB_CLOSE_DELAY=-1",
                    "kiwi", "kiwi",
                    new H2Dialect());
        }

        private Repository createConfiguration(KiWiConfiguration base, int port) throws RepositoryException {
            KiWiConfiguration config;

            if(base != null) {
                config = base;
            } else {
                config = buildBaseConfiguration();
            }
            config.setDatacenterId(datacenterIds++);
            config.setClustered(true);
            config.setCachingBackend(type);
            config.setClusterPort(port);

            KiWiStore store = new KiWiStore(config);

            Repository repository = new SailRepository(store);
            repository.initialize();

            return repository;
        }

        private static CacheManager getCacheManager(Repository repository) {
            return ((KiWiStore)((SailRepository)repository).getSail()).getPersistence().getCacheManager();
        }

    }
}
