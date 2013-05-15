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
package org.apache.marmotta.ldcache.backend.kiwi.test;

import info.aduna.iteration.CloseableIteration;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.ldcache.api.LDCachingConnection;
import org.apache.marmotta.ldcache.backend.kiwi.LDCachingKiWiBackend;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This test checks if the ldcache backend works, i.e. the system properly stores cache entries and cached triples.
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
public class LDCacheBackendTest {


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

    public LDCacheBackendTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
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
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
        backend.getPersistence().dropDatabase();
        store.getPersistence().dropDatabase();
        repository.shutDown();
    }


    final Logger logger =
            LoggerFactory.getLogger(this.getClass());

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        /**
         * Invoked when a test is about to start
         */
        @Override
        protected void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

    /**
     * This test verifies if triples are added to the correct context using the repository connection obtained from the backend
     * @throws Exception
     */
    @Test
    public void testStoreTriples() throws Exception {

        RepositoryConnection con = backend.getCacheConnection("http://localhost/resource/1");
        try {
            con.begin();

            URI subject    = con.getValueFactory().createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
            URI predicate  = con.getValueFactory().createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
            Literal object1 = con.getValueFactory().createLiteral(RandomStringUtils.random(64));
            Literal object2 = con.getValueFactory().createLiteral(RandomStringUtils.random(64));
            URI context    = con.getValueFactory().createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));

            Statement stmt1 = con.getValueFactory().createStatement(subject,predicate,object1);
            Statement stmt2 = con.getValueFactory().createStatement(subject,predicate,object2,context);

            con.add(stmt1);
            con.add(stmt2);

            con.commit();

            RepositoryResult<Statement> it = con.getStatements(subject,predicate,null,true);
            try {
                while(it.hasNext()) {
                    Statement next =  it.next();
                    Assert.assertEquals(CACHE_CONTEXT, next.getContext().stringValue());
                }
            } finally {
                it.close();
            }

            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }
    }

    /**
     * Test storing and retrieving cache entries
     *
     * @throws Exception
     */
    @Test
    public void testStoreEntries() throws Exception {
        LDCachingConnection con = backend.getCacheConnection("http://localhost/resource/1");
        try {
            con.begin();

            URI subject1     = con.getValueFactory().createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
            URI subject2     = con.getValueFactory().createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
            Literal object1  = con.getValueFactory().createLiteral(RandomStringUtils.random(64));
            Literal object2  = con.getValueFactory().createLiteral(RandomStringUtils.random(64));
            URI predicate    = con.getValueFactory().createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));

            Statement stmt1 = con.getValueFactory().createStatement(subject1,predicate,object1);
            Statement stmt2 = con.getValueFactory().createStatement(subject2,predicate,object2);

            con.add(stmt1);
            con.add(stmt2);

            con.commit();

            con.begin();
            CacheEntry entry1 = new CacheEntry();
            entry1.setExpiryDate(new Date(System.currentTimeMillis()+1000*60));
            entry1.setLastRetrieved(new Date());
            entry1.setUpdateCount(1);
            entry1.setResource(subject1);
            con.addCacheEntry(subject1, entry1);
            con.commit();

            Assert.assertTrue(backend.isCached(subject1.stringValue()));
            Assert.assertFalse(backend.isCached(subject2.stringValue()));
            Assert.assertEquals(1,asList(backend.listCacheEntries()).size());
            Assert.assertEquals(0,asList(backend.listExpiredEntries()).size());

            con.begin();
            CacheEntry entry2 = new CacheEntry();
            entry2.setExpiryDate(new Date(System.currentTimeMillis() - 1000 * 60));
            entry2.setLastRetrieved(new Date());
            entry2.setUpdateCount(1);
            entry2.setResource(subject2);
            con.addCacheEntry(subject2,entry2);

            con.commit();

            Assert.assertTrue(backend.isCached(subject1.stringValue()));
            Assert.assertTrue(backend.isCached(subject2.stringValue()));
            Assert.assertEquals(2,asList(backend.listCacheEntries()).size());
            Assert.assertEquals(1,asList(backend.listExpiredEntries()).size());

            con.begin();
            con.removeCacheEntry(subject1);
            con.commit();

            Assert.assertFalse(backend.isCached(subject1.stringValue()));
            Assert.assertTrue(backend.isCached(subject2.stringValue()));
            Assert.assertEquals(1,asList(backend.listCacheEntries()).size());
            Assert.assertEquals(1,asList(backend.listExpiredEntries()).size());
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
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
