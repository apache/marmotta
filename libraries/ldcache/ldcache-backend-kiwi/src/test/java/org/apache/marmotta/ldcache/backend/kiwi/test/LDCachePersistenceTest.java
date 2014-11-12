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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.ldcache.backend.kiwi.model.KiWiCacheEntry;
import org.apache.marmotta.ldcache.backend.kiwi.persistence.LDCachingKiWiPersistence;
import org.apache.marmotta.ldcache.backend.kiwi.persistence.LDCachingKiWiPersistenceConnection;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;

/**
 * This test checks if the database persistence for the ldcache kiwi backend functionality works properly.
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
public class LDCachePersistenceTest {

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

    private KiWiPersistence persistence;
    private LDCachingKiWiPersistence vpersistence;

    public LDCachePersistenceTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
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
    public void initDatabase() throws SQLException {
        persistence = new KiWiPersistence("test",jdbcUrl,jdbcUser,jdbcPass,dialect);
        persistence.initialise();
        persistence.initDatabase();

        vpersistence = new LDCachingKiWiPersistence(persistence);
        vpersistence.initDatabase();
    }

    @After
    public void dropDatabase() throws SQLException {
        vpersistence.dropDatabase();

        persistence.dropDatabase();
        persistence.shutdown();
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

    @Test
    public void testTablesCreateDrop() throws Exception {
        // test if database exists and has a version
        LDCachingKiWiPersistenceConnection connection = vpersistence.getConnection();
        try {
            Assert.assertThat(connection.getDatabaseTables(), hasItems("ldcache_entries"));
            Assert.assertEquals(KiWiDialect.VERSION, connection.getDatabaseVersion());

            connection.commit();
        } finally {
            connection.close();
        }
    }

    @Test
    public void testCreateListEntries() throws Exception {
        LDCachingKiWiPersistenceConnection connection = vpersistence.getConnection();
        try {
            KiWiUriResource subject1  = new KiWiUriResource("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
            KiWiUriResource subject2  = new KiWiUriResource("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));

            connection.storeNode(subject1);
            connection.storeNode(subject2);

            KiWiCacheEntry entry1 = new KiWiCacheEntry();
            entry1.setExpiryDate(new Date(System.currentTimeMillis()+1000*60));
            entry1.setLastRetrieved(new Date());
            entry1.setUpdateCount(1);
            entry1.setResource(subject1);
            entry1.setTripleCount(1);
            connection.storeCacheEntry(entry1);

            connection.commit();

            Assert.assertEquals(1,asList(connection.listAll()).size());
            Assert.assertEquals(0,asList(connection.listExpired()).size());

            KiWiCacheEntry entry2 = new KiWiCacheEntry();
            entry2.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60));
            entry2.setLastRetrieved(new Date());
            entry2.setUpdateCount(1);
            entry2.setResource(subject2);
            entry2.setTripleCount(1);
            connection.storeCacheEntry(entry2);

            connection.commit();

            Assert.assertEquals(2,asList(connection.listAll()).size());
            Assert.assertEquals(0,asList(connection.listExpired()).size());

            connection.commit();

            connection.removeCacheEntry(entry1);

            connection.commit();

            Assert.assertEquals(1,asList(connection.listAll()).size());
            Assert.assertEquals(0, asList(connection.listExpired()).size());

            connection.removeCacheEntry(entry2.getResource().stringValue());

            connection.commit();

            Assert.assertEquals(0,asList(connection.listAll()).size());
            Assert.assertEquals(0,asList(connection.listExpired()).size());

            connection.commit();

        } finally {
            connection.close();
        }

    }


    @Test
    public void testCreateListExpired() throws Exception {
        LDCachingKiWiPersistenceConnection connection = vpersistence.getConnection();
        try {
            KiWiUriResource subject1  = new KiWiUriResource("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
            KiWiUriResource subject2  = new KiWiUriResource("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));

            connection.storeNode(subject1);
            connection.storeNode(subject2);

            KiWiCacheEntry entry1 = new KiWiCacheEntry();
            entry1.setExpiryDate(new Date(System.currentTimeMillis() - 1000*60));
            entry1.setLastRetrieved(new Date());
            entry1.setUpdateCount(1);
            entry1.setResource(subject1);
            entry1.setTripleCount(1);
            connection.storeCacheEntry(entry1);

            KiWiCacheEntry entry2 = new KiWiCacheEntry();
            entry2.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60));
            entry2.setLastRetrieved(new Date());
            entry2.setUpdateCount(1);
            entry2.setResource(subject2);
            entry2.setTripleCount(1);
            connection.storeCacheEntry(entry2);

            connection.commit();

            Assert.assertEquals(2,asList(connection.listAll()).size());
            Assert.assertEquals(1,asList(connection.listExpired()).size());

            connection.commit();

            connection.removeCacheEntry(entry1);

            connection.commit();

            Assert.assertEquals(1,asList(connection.listAll()).size());
            Assert.assertEquals(0,asList(connection.listExpired()).size());

            connection.removeCacheEntry(entry2);

            connection.commit();

            Assert.assertEquals(0,asList(connection.listAll()).size());
            Assert.assertEquals(0,asList(connection.listExpired()).size());

            connection.commit();

        } finally {
            connection.close();
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
