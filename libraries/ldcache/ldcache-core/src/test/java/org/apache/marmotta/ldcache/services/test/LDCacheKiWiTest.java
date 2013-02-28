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
package org.apache.marmotta.ldcache.services.test;

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.ldcache.backend.kiwi.LDCachingKiWiBackend;
import org.apache.marmotta.ldcache.model.CacheConfiguration;
import org.apache.marmotta.ldcache.services.LDCache;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This test checks if the ldcache main class works, i.e. the system properly stores cache entries and cached triples.
 * It will try running over all available databases. Except for in-memory databases like
 * H2 or Derby, database URLs must be passed as system property, or otherwise the test is skipped for this database.
 * Available system properties:
 * <ul>
 *     <li>PostgreSQL:
 *     <ul>
 *         <li>postgresql.url, e.g. jdbc:postgresql://localhost:5433/kiwitest?prepareThreshold=3</li>
 *         <li>postgresql.user (default: lmf)</li>
 *         <li>postgresql.pass (default: lmf)</li>
 *     </ul>
 *     </li>
 *     <li>MySQL:
 *     <ul>
 *         <li>mysql.url, e.g. jdbc:mysql://localhost:3306/kiwitest?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull</li>
 *         <li>mysql.user (default: lmf)</li>
 *         <li>mysql.pass (default: lmf</li>
 *     </ul>
 *     </li>
 *     <li>H2:
 *     <ul>
 *         <li>h2.url, e.g. jdbc:h2:mem;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10</li>
 *         <li>h2.user (default: lmf)</li>
 *         <li>h2.pass (default: lmf</li>
 *     </ul>
 *     </li>
 * </ul>
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(Parameterized.class)
public class LDCacheKiWiTest {

    public static final String CACHE_CONTEXT = "http://localhost/context/cache";

    /**
     * Return database configurations if the appropriate parameters have been set.
     *
     * @return an array (database name, url, user, password)
     */
    @Parameterized.Parameters(name="Database Test {index}: {0} at {1}")
    public static Iterable<Object[]> databases() {
        String[] databases = {"H2", "PostgreSQL", "MySQL"};

        List<Object[]> result = new ArrayList<Object[]>(databases.length);
        for(String database : databases) {
            if(System.getProperty(database.toLowerCase()+".url") != null) {
                result.add(new Object[] {
                        database,
                        System.getProperty(database.toLowerCase()+".url"),
                        System.getProperty(database.toLowerCase()+".user","lmf"),
                        System.getProperty(database.toLowerCase()+".pass","lmf")
                });
            }
        }
        return result;
    }


    private KiWiDialect dialect;

    private String jdbcUrl;

    private String jdbcUser;

    private String jdbcPass;

    private KiWiStore store;

    private LDCachingKiWiBackend backend;

    private Repository repository;

    private LDCache ldcache;

    public LDCacheKiWiTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
        this.jdbcPass = jdbcPass;
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;

        if("H2".equals(database)) {
            this.dialect = new H2Dialect();
        } else if("MySQL".equals(database)) {
            this.dialect = new MySQLDialect();
        } else if("PostgreSQL".equals(database)) {
            this.dialect = new PostgreSQLDialect();
        }
    }


    @Before
    public void initDatabase() throws RepositoryException {
        store = new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred");
        repository = new SailRepository(store);
        repository.initialize();

        backend = new LDCachingKiWiBackend(store, CACHE_CONTEXT);
        backend.initialize();

        ldcache = new LDCache(new CacheConfiguration(),backend);
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
        backend.getPersistence().dropDatabase();
        store.getPersistence().dropDatabase();
        backend.shutdown();
        repository.shutDown();
    }


    /**
     * Test retrieving and caching some resources (provided by DummyProvider).
     */
    @Test
    public void textCacheResources() throws Exception {
        String uri1 = "http://localhost/resource1";
        String uri2 = "http://localhost/resource2";
        String uri3 = "http://localhost/resource3";

        ldcache.refreshResource(repository.getValueFactory().createURI(uri1),false);

        Assert.assertEquals(1,asList(ldcache.listCacheEntries()).size());

        RepositoryConnection con1 = ldcache.getCacheConnection(uri1);
        try {
            con1.begin();
            Assert.assertEquals(3, asList(con1.getStatements(con1.getValueFactory().createURI(uri1), null, null, false)).size());
            con1.commit();
        } finally {
            con1.close();
        }

        ldcache.refreshResource(repository.getValueFactory().createURI(uri2),false);

        Assert.assertEquals(2,asList(ldcache.listCacheEntries()).size());

        RepositoryConnection con2 = ldcache.getCacheConnection(uri2);
        try {
            con2.begin();
            Assert.assertEquals(2, asList(con2.getStatements(con2.getValueFactory().createURI(uri2), null, null, false)).size());
            con2.commit();
        } finally {
            con2.close();
        }

        ldcache.refreshResource(repository.getValueFactory().createURI(uri3),false);

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


    /**
     * Test retrieving and caching some resources (provided by DummyProvider).
     */
    @Test
    public void textExpire() throws Exception {
        String uri1 = "http://localhost/resource1";
        String uri2 = "http://localhost/resource2";
        String uri3 = "http://localhost/resource3";

        ldcache.refreshResource(repository.getValueFactory().createURI(uri1),false);
        ldcache.refreshResource(repository.getValueFactory().createURI(uri2),false);
        ldcache.refreshResource(repository.getValueFactory().createURI(uri3),false);

        Assert.assertEquals(3,asList(ldcache.listCacheEntries()).size());
        Assert.assertEquals(0,asList(ldcache.listExpiredEntries()).size());

        ldcache.expire(repository.getValueFactory().createURI(uri1));

        mysqlSleep();

        Assert.assertEquals(1,asList(ldcache.listExpiredEntries()).size());

        ldcache.refreshExpired();

        mysqlSleep();

        Assert.assertEquals(0,asList(ldcache.listExpiredEntries()).size());

    }

    /**
     * Test retrieving and caching some resources (provided by DummyProvider).
     */
    @Test
    public void textExpireAll() throws Exception {
        String uri1 = "http://localhost/resource1";
        String uri2 = "http://localhost/resource2";
        String uri3 = "http://localhost/resource3";

        ldcache.refreshResource(repository.getValueFactory().createURI(uri1),false);
        ldcache.refreshResource(repository.getValueFactory().createURI(uri2),false);
        ldcache.refreshResource(repository.getValueFactory().createURI(uri3),false);

        Assert.assertEquals(3,asList(ldcache.listCacheEntries()).size());
        Assert.assertEquals(0,asList(ldcache.listExpiredEntries()).size());

        ldcache.expireAll();

        mysqlSleep();

        Assert.assertEquals(3,asList(ldcache.listExpiredEntries()).size());

        ldcache.refreshExpired();

        mysqlSleep();

        Assert.assertEquals(0,asList(ldcache.listExpiredEntries()).size());

    }


    /*
     * MYSQL rounds timestamps to the second, so it is sometimes necessary to sleep before doing a test
     */
    private  void mysqlSleep() {
        if(this.dialect instanceof MySQLDialect) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

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
