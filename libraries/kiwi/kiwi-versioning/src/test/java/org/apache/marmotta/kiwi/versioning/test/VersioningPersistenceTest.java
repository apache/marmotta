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

import info.aduna.iteration.Iterations;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.model.rdf.KiWiStringLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.kiwi.versioning.persistence.KiWiVersioningConnection;
import org.apache.marmotta.kiwi.versioning.persistence.KiWiVersioningPersistence;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;

/**
 * This test checks if the database persistence for the versioning functionality works properly.
 * 
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class VersioningPersistenceTest {

    private KiWiPersistence persistence;
    private KiWiVersioningPersistence vpersistence;
    private final KiWiConfiguration dbConfig;

    public VersioningPersistenceTest(KiWiConfiguration dbConfig) {
        this.dbConfig = dbConfig;
    }


    @Before
    public void initDatabase() throws SQLException {
        persistence = new KiWiPersistence(dbConfig);
        persistence.initialise();
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

    @Test
    public void testTablesCreateDrop() throws Exception {
        // test if database exists and has a version
        KiWiConnection connection = vpersistence.getConnection();
        try {
            Assert.assertThat(connection.getDatabaseTables(), hasItems("versions", "versions_added", "versions_removed"));
            Assert.assertEquals(KiWiDialect.VERSION, connection.getDatabaseVersion());

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
            List<Version> list1 = Iterations.asList(connection.listVersions());
            Assert.assertEquals("there should be exactly one version",1,list1.size());
            Assert.assertEquals("contents of version differ", version1, list1.get(0));

            // check if listVersions with subject1 now gives exactly one version
            List<Version> listr1 = Iterations.asList(connection.listVersions(subject1));
            Assert.assertEquals("there should be exactly one version", 1, listr1.size());
            Assert.assertEquals("contents of version differ", version1, listr1.get(0));


            Version version2 = new Version();
            version2.setCommitTime(new Date());
            version2.addTriple(triple2);
            version2.removeTriple(triple1);
            connection.storeVersion(version2);
            connection.commit();

            // check if listVersions now gives exactly two versions
            List<Version> list2 = Iterations.asList(connection.listVersions());
            Assert.assertEquals("there should be exactly two version",2,list2.size());
            Assert.assertEquals("contents of version differ", version2, list2.get(1));


            // check if listVersions with subject1 still gives exactly one version
            List<Version> listr2 = Iterations.asList(connection.listVersions(subject1));
            Assert.assertEquals("there should be exactly one version", 2, listr2.size());
            Assert.assertEquals("contents of version differ", version1, listr2.get(0));

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
            List<Version> list1 = Iterations.asList(connection.listVersions(date1,date2));
            Assert.assertEquals("there should be exactly one version from "+date1+" to "+date2,1,list1.size());
            Assert.assertEquals("contents of version differ", version1, list1.get(0));

            // check if getLatestVersion at date2 works
            Version latest2 = connection.getLatestVersion(subject,date2);
            Assert.assertNotNull("latest version for subject was not found",latest2);
            Assert.assertEquals("latest version is not the expected version", version1,latest2);

            // check if listVersions with subject1 now gives exactly one version
            List<Version> listr1 = Iterations.asList(connection.listVersions(subject,date1,date2));
            Assert.assertEquals("there should be exactly one version", 1, listr1.size());
            Assert.assertEquals("contents of version differ", version1, listr1.get(0));


            List<Version> list2 = Iterations.asList(connection.listVersions(date2,date3));
            Assert.assertEquals("there should be exactly one version from "+date2+" to "+date3,1,list2.size());
            Assert.assertEquals("contents of version differ", version2, list2.get(0));
            Assert.assertTrue("order of versions is not correct", version1.getId() < (long) list2.get(0).getId());

            List<Version> list3 = Iterations.asList(connection.listVersions(date3,new Date()));
            Assert.assertEquals("there should be no version from "+date3+" to now",0,list3.size());

            List<Version> list4 = Iterations.asList(connection.listVersions(date1,date3));
            Assert.assertEquals("there should be exactly two versions from "+date1+" to "+date3,2,list4.size());
            Assert.assertEquals("contents of version1 differ", version1, list4.get(0));
            Assert.assertEquals("contents of version2 differ", version2, list4.get(1));


            connection.commit();
        } finally {
            connection.close();
        }


    }


    @Test
    public void testCreateRemoveVersions() throws Exception {
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
            List<Version> list1 = Iterations.asList(connection.listVersions());
            Assert.assertEquals("there should be exactly one version",1,list1.size());
            Assert.assertEquals("contents of version differ", version1, list1.get(0));

            Version version2 = new Version();
            version2.setCommitTime(new Date());
            version2.addTriple(triple2);
            version2.removeTriple(triple1);
            connection.storeVersion(version2);
            connection.commit();

            // check if listVersions now gives exactly two versions
            List<Version> list2 = Iterations.asList(connection.listVersions());
            Assert.assertEquals("there should be exactly two version",2,list2.size());
            Assert.assertEquals("contents of version differ", version2, list2.get(1));

            connection.commit();

            connection.removeVersion(version1.getId());
            connection.commit();

            // check if listVersions now gives exactly two versions
            List<Version> list3 = Iterations.asList(connection.listVersions());
            Assert.assertEquals("there should be exactly one version",1,list3.size());
            Assert.assertEquals("contents of version differ", version2, list3.get(0));

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
