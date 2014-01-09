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
package org.apache.marmotta.ldcache.infinispan.test;

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.ldcache.backend.infinispan.LDCachingInfinispanBackend;
import org.apache.marmotta.ldcache.model.CacheConfiguration;
import org.apache.marmotta.ldcache.services.LDCache;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LDCacheInfinispanTest {


    private LDCachingInfinispanBackend backend;

    private LDCache ldcache;

    private ValueFactory valueFactory;

    @Before
    public void initCache() throws RepositoryException {
        backend = new LDCachingInfinispanBackend();
        backend.initialize();

        ldcache = new LDCache(new CacheConfiguration(),backend);

        valueFactory = new ValueFactoryImpl();
    }

    @After
    public void shutdownCache() throws RepositoryException, SQLException {
        backend.shutdown();
    }


    /**
     * Test retrieving and caching some resources (provided by DummyProvider).
     */
    @Test
    public void testCacheResources() throws Exception {
        String uri1 = "http://localhost/resource1";
        String uri2 = "http://localhost/resource2";
        String uri3 = "http://localhost/resource3";

        ldcache.refreshResource(valueFactory.createURI(uri1),false);

        Assert.assertEquals(1, asList(ldcache.listCacheEntries()).size());

        RepositoryConnection con1 = ldcache.getCacheConnection(uri1);
        try {
            con1.begin();
            Assert.assertEquals(3, asList(con1.getStatements(con1.getValueFactory().createURI(uri1), null, null, false)).size());
            con1.commit();
        } finally {
            con1.close();
        }

        ldcache.refreshResource(valueFactory.createURI(uri2), false);

        Assert.assertEquals(2, asList(ldcache.listCacheEntries()).size());

        RepositoryConnection con2 = ldcache.getCacheConnection(uri2);
        try {
            con2.begin();
            Assert.assertEquals(2, asList(con2.getStatements(con2.getValueFactory().createURI(uri2), null, null, false)).size());
            con2.commit();
        } finally {
            con2.close();
        }

        ldcache.refreshResource(valueFactory.createURI(uri3), false);

        Assert.assertEquals(3,asList(ldcache.listCacheEntries()).size());

        RepositoryConnection con3 = ldcache.getCacheConnection(uri3);
        try {
            con3.begin();
            Assert.assertEquals(2, asList(con3.getStatements(con3.getValueFactory().createURI(uri3), null, null, false)).size());
            con3.commit();
        } finally {
            con3.close();
        }
    }


    /*

    /**
     * Workaround for https://openrdf.atlassian.net/browse/SES-1702 in Sesame 2.7.0-beta1
     * @param <E>
     * @return
     */
    public static <E,X extends Exception> List<E> asList(CloseableIteration<E,X> result) throws RepositoryException {
        ArrayList<E> collection = new ArrayList<E>();
        try {
            try {
                while (result.hasNext()) {
                    collection.add(result.next());
                }

                return collection;
            } finally {
                result.close();
            }
        } catch(Throwable ex) {
            throw new RepositoryException(ex);
        }
    }

}
