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
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.apache.marmotta.kiwi.transactions.sail.KiWiTransactionalSail;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.kiwi.versioning.sail.KiWiVersioningSail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

/**
 * This test checks if the versioning functionality itself works, i.e. the system properly creates versions on
 * transaction commits. It will try running over all available databases. Except for in-memory databases like
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
public class VersioningRepositoryTest {

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

    private KiWiTransactionalSail tsail;

    private KiWiVersioningSail    vsail;

    private Repository repository;

    public VersioningRepositoryTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
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
    public void initDatabase() throws RepositoryException {
        store = new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred");
        tsail = new KiWiTransactionalSail(store);
        vsail = new KiWiVersioningSail(tsail);
        repository = new SailRepository(vsail);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
        vsail.getPersistence().dropDatabase();
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
     * This test imports three small RDF files in sequence and checks afterwards that the number of versions
     * is correct and they contain the correct information
     * @throws Exception
     */
    @Test
    public void testCreateVersions() throws Exception {
        // import three files in sequence and check if the versions are created properly

        Date date1 = new Date();

        mysqlSleep();

        // base data
        InputStream baseData = this.getClass().getResourceAsStream("version-base.rdf");
        assumeThat("Could not load test-data: version-base.rdf", baseData, notNullValue(InputStream.class));

        RepositoryConnection connectionBase = repository.getConnection();
        try {
            connectionBase.add(baseData, "http://marmotta.incubator.apache.org/testing/ns1/", RDFFormat.RDFXML);
            connectionBase.commit();
        } finally {
            connectionBase.close();
        }

        mysqlSleep();

        Date date2 = new Date();

        mysqlSleep();

        // update 1
        InputStream update1Data = this.getClass().getResourceAsStream("version-update1.rdf");
        assumeThat("Could not load test-data: version-update1.rdf", update1Data, notNullValue(InputStream.class));

        RepositoryConnection connectionUpdate1 = repository.getConnection();
        try {
            connectionUpdate1.add(update1Data, "http://marmotta.incubator.apache.org/testing/ns1/", RDFFormat.RDFXML);
            connectionUpdate1.commit();
        } finally {
            connectionUpdate1.close();
        }

        // update 2
        InputStream update2Data = this.getClass().getResourceAsStream("version-update2.rdf");
        assumeThat("Could not load test-data: version-update2.rdf", update2Data, notNullValue(InputStream.class));

        RepositoryConnection connectionUpdate2 = repository.getConnection();
        try {
            connectionUpdate2.add(update2Data, "http://marmotta.incubator.apache.org/testing/ns1/", RDFFormat.RDFXML);
            connectionUpdate2.commit();
        } finally {
            connectionUpdate2.close();
        }

        // list all versions
        List<Version> versions = asList(vsail.listVersions());
        Assert.assertEquals("expected 3 versions!", 3, versions.size());
        Assert.assertEquals(1, (long)versions.get(0).getId());
        Assert.assertEquals(2, (long)versions.get(1).getId());
        Assert.assertEquals(3, (long)versions.get(2).getId());
        Assert.assertEquals(3, (long)versions.get(0).getAddedTriples().size());
        Assert.assertEquals(3, (long)versions.get(1).getAddedTriples().size());
        Assert.assertEquals(1, (long)versions.get(2).getAddedTriples().size());

        List<Version> versions1 = asList(vsail.listVersions(date1,date2));
        Assert.assertEquals("expected 1 version!", 1, versions1.size());
        Assert.assertEquals(1, (long)versions1.get(0).getId());
        Assert.assertEquals(3, (long)versions1.get(0).getAddedTriples().size());
    }


    /**
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
    public static <E> List<E> asList(RepositoryResult<E> result) throws RepositoryException {
        return Iterations.asList(result);
    }
}
