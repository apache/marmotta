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
package org.apache.marmotta.kiwi.versioning.test;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.marmotta.kiwi.model.rdf.KiWiStringLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.kiwi.versioning.persistence.KiWiVersioningConnection;
import org.apache.marmotta.kiwi.versioning.persistence.KiWiVersioningPersistence;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;

/**
 * This test checks if the database persistence for the versioning functionality works properly.
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
public class VersioningPersistenceTest {


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
    private KiWiVersioningPersistence vpersistence;

    public VersioningPersistenceTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
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
        
        DBConnectionChecker.checkDatabaseAvailability(jdbcUrl, jdbcUser, jdbcPass, dialect);
    }


    @Before
    public void initDatabase() throws SQLException {
        persistence = new KiWiPersistence("test",jdbcUrl,jdbcUser,jdbcPass,dialect);
        persistence.initDatabase();

        vpersistence = new KiWiVersioningPersistence(persistence);
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
        KiWiConnection connection = vpersistence.getConnection();
        try {
            Assert.assertThat(connection.getDatabaseTables(), hasItems("versions", "versions_added", "versions_removed"));
            Assert.assertEquals(2, connection.getDatabaseVersion());

            connection.commit();
        } finally {
            connection.close();
        }
    }

    @Test
    public void testCreateListVersions() throws Exception {
        KiWiVersioningConnection connection = vpersistence.getConnection();
        try {
            KiWiUriResource subject1  = new KiWiUriResource("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
            KiWiUriResource subject2  = new KiWiUriResource("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
            KiWiUriResource pred_1   = new KiWiUriResource("http://localhost/predicate/P1");
            KiWiUriResource pred_2   = new KiWiUriResource("http://localhost/predicate/P2");
            KiWiUriResource object_1 = new KiWiUriResource("http://localhost/resource/"+RandomStringUtils.randomAlphanumeric(8));
            KiWiStringLiteral object_2 = new KiWiStringLiteral(RandomStringUtils.randomAlphanumeric(32));
            KiWiUriResource context  = new KiWiUriResource("http://localhost/context/"+RandomStringUtils.randomAlphanumeric(8));

            connection.storeNode(subject1);
            connection.storeNode(subject2);
            connection.storeNode(pred_1);
            connection.storeNode(pred_2);
            connection.storeNode(object_1);
            connection.storeNode(object_2);
            connection.storeNode(context);

            KiWiTriple triple1 = new KiWiTriple(subject1,pred_1,object_1,context);
            KiWiTriple triple2 = new KiWiTriple(subject2,pred_2,object_2,context);

            connection.storeTriple(triple1);
            connection.storeTriple(triple2);
            connection.commit();

            Version version1 = new Version();
            version1.setCommitTime(new Date());
            version1.addTriple(triple1);
            connection.storeVersion(version1);
            connection.commit();

            // check if listVersions now gives exactly one version
            List<Version> list1 = connection.listVersions().asList();
            Assert.assertEquals("there should be exactly one version",1,list1.size());
            Assert.assertEquals("contents of version differ", version1, list1.get(0));
            Assert.assertEquals("version id is not 1", 1L, (long)list1.get(0).getId());

            // check if listVersions with subject1 now gives exactly one version
            List<Version> listr1 = connection.listVersions(subject1).asList();
            Assert.assertEquals("there should be exactly one version", 1, listr1.size());
            Assert.assertEquals("contents of version differ", version1, listr1.get(0));
            Assert.assertEquals("version id is not 1", 1L, (long)listr1.get(0).getId());


            Version version2 = new Version();
            version2.setCommitTime(new Date());
            version2.addTriple(triple2);
            version2.removeTriple(triple1);
            connection.storeVersion(version2);
            connection.commit();

            // check if listVersions now gives exactly two versions
            List<Version> list2 = connection.listVersions().asList();
            Assert.assertEquals("there should be exactly two version",2,list2.size());
            Assert.assertEquals("contents of version differ", version2, list2.get(1));


            // check if listVersions with subject1 still gives exactly one version
            List<Version> listr2 = connection.listVersions(subject1).asList();
            Assert.assertEquals("there should be exactly one version", 2, listr2.size());
            Assert.assertEquals("contents of version differ", version1, listr2.get(0));
            Assert.assertEquals("version id is not 1", 1L, (long)listr2.get(0).getId());

            connection.commit();
        } finally {
            connection.close();
        }

    }

    /**
     * Test listing versions between two dates
     *
     * @throws Exception
     */
    @Test
    public void testCreateListVersionsBetween() throws Exception {
        KiWiVersioningConnection connection = vpersistence.getConnection();
        try {
            KiWiUriResource subject  = new KiWiUriResource("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
            KiWiUriResource pred_1   = new KiWiUriResource("http://localhost/predicate/P1");
            KiWiUriResource pred_2   = new KiWiUriResource("http://localhost/predicate/P2");
            KiWiUriResource object_1 = new KiWiUriResource("http://localhost/resource/"+RandomStringUtils.randomAlphanumeric(8));
            KiWiStringLiteral object_2 = new KiWiStringLiteral(RandomStringUtils.randomAlphanumeric(32));
            KiWiUriResource context  = new KiWiUriResource("http://localhost/context/"+RandomStringUtils.randomAlphanumeric(8));

            connection.storeNode(subject);
            connection.storeNode(pred_1);
            connection.storeNode(pred_2);
            connection.storeNode(object_1);
            connection.storeNode(object_2);
            connection.storeNode(context);

            KiWiTriple triple1 = new KiWiTriple(subject,pred_1,object_1,context);
            KiWiTriple triple2 = new KiWiTriple(subject,pred_2,object_2,context);

            connection.storeTriple(triple1);
            connection.storeTriple(triple2);
            connection.commit();

            Date date1 = new Date();

            // wait for one second to be sure to capture MySQL cutting milliseconds
            mysqlSleep();


            Version version1 = new Version();
            version1.setCommitTime(new Date());
            version1.addTriple(triple1);
            connection.storeVersion(version1);
            connection.commit();

            // wait for one second to be sure to capture MySQL cutting milliseconds
            mysqlSleep();

            Date date2 = new Date();

            // wait for one second to be sure to capture MySQL cutting milliseconds
            mysqlSleep();


            Version version2 = new Version();
            version2.setCommitTime(new Date());
            version2.addTriple(triple2);
            version2.removeTriple(triple1);
            connection.storeVersion(version2);
            connection.commit();

            // wait for one second to be sure to capture MySQL cutting milliseconds
            mysqlSleep();

            Date date3 = new Date();


            // now we test different ways of listing versions between dates
            List<Version> list1 = connection.listVersions(date1,date2).asList();
            Assert.assertEquals("there should be exactly one version from "+date1+" to "+date2,1,list1.size());
            Assert.assertEquals("contents of version differ", version1, list1.get(0));
            Assert.assertEquals("version id is not 1", 1L, (long)list1.get(0).getId());

            // check if getLatestVersion at date2 works
            Version latest2 = connection.getLatestVersion(subject,date2);
            Assert.assertNotNull("latest version for subject was not found",latest2);
            Assert.assertEquals("latest version is not the expected version", version1,latest2);

            // check if listVersions with subject1 now gives exactly one version
            List<Version> listr1 = connection.listVersions(subject,date1,date2).asList();
            Assert.assertEquals("there should be exactly one version", 1, listr1.size());
            Assert.assertEquals("contents of version differ", version1, listr1.get(0));
            Assert.assertEquals("version id is not 1", 1L, (long)listr1.get(0).getId());


            List<Version> list2 = connection.listVersions(date2,date3).asList();
            Assert.assertEquals("there should be exactly one version from "+date2+" to "+date3,1,list2.size());
            Assert.assertEquals("contents of version differ", version2, list2.get(0));
            Assert.assertEquals("version id is not 2", 2L, (long)list2.get(0).getId());

            List<Version> list3 = connection.listVersions(date3,new Date()).asList();
            Assert.assertEquals("there should be no version from "+date3+" to now",0,list3.size());

            List<Version> list4 = connection.listVersions(date1,date3).asList();
            Assert.assertEquals("there should be exactly two versions from "+date1+" to "+date3,2,list4.size());
            Assert.assertEquals("contents of version1 differ", version1, list4.get(0));
            Assert.assertEquals("contents of version2 differ", version2, list4.get(1));


            connection.commit();
        } finally {
            connection.close();
        }


    }

    /**
     * MYSQL rounds timestamps to the second, so it is sometimes necessary to sleep before doing a test
     */
    private  void mysqlSleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }


}
