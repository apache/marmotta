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
package org.apache.marmotta.kiwi.reasoner.test.sail;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.reasoner.engine.ReasoningConfiguration;
import org.apache.marmotta.kiwi.reasoner.sail.KiWiReasoningSail;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.apache.marmotta.kiwi.transactions.sail.KiWiTransactionalSail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Test the reasoning sail with small sample datasets. It will test both full and incremental reasoning.
 * <p/>
 * It will try running over all available databases. Except for in-memory databases like H2 or Derby, database
 * URLs must be passed as system property, or otherwise the test is skipped for this database. Available system properties:
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
 * @see org.apache.marmotta.kiwi.persistence.KiWiConnection
 * @see org.apache.marmotta.kiwi.persistence.KiWiPersistence
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(Parameterized.class)
public class ReasoningSailTest {

    private static Logger log = LoggerFactory.getLogger(ReasoningSailTest.class);

    private static final String NS = "http://localhost/resource/";

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
    private KiWiReasoningSail     rsail;

    private Repository repository;


    public ReasoningSailTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
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
    public void initDatabase() throws Exception {
        store = new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred");
        tsail = new KiWiTransactionalSail(store);
        rsail = new KiWiReasoningSail(tsail, new ReasoningConfiguration());
        repository = new SailRepository(rsail);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws Exception {
        rsail.getPersistence().dropDatabase();
        store.getPersistence().dropDatabase();
        repository.shutDown();
    }


    /**
     * Test incremental reasoning with a small sample program. This test will carry out the following steps in order:
     * - load the program "simple.kwrl" into the reasoning engine
     * - import the turtle file "simple.ttl" into the repository
     * - wait for the reasoner to finish
     * - check if all expected inferred triples exist
     *
     * @throws Exception
     */
    @Test
    public void testIncrementalReasoning() throws Exception {

        // load the program "simple.kwrl" into the reasoning engine
        rsail.addProgram("simple", this.getClass().getResourceAsStream("simple.kwrl"));

        // import the turtle file "simple.ttl" into the repository
        RepositoryConnection importCon = repository.getConnection();
        try {
            importCon.begin();
            importCon.add(this.getClass().getResourceAsStream("simple.ttl"),NS, RDFFormat.TURTLE);
            importCon.commit();
        } finally {
            importCon.close();
        }

        // wait for the reasoner to finish
        while(rsail.getEngine().isRunning()) {
            log.debug("sleeping for 100ms to let engine finish processing ... ");
            Thread.sleep(100);
        }

        // check if all expected inferred triples exist
        RepositoryConnection testCon = repository.getConnection();
        try {
            testCon.begin();
            URI a = testCon.getValueFactory().createURI(NS + "a");
            URI b = testCon.getValueFactory().createURI(NS + "b");
            URI c = testCon.getValueFactory().createURI(NS + "c");
            URI d = testCon.getValueFactory().createURI(NS + "d");
            URI s = testCon.getValueFactory().createURI(NS + "symmetric");
            URI t = testCon.getValueFactory().createURI(NS + "transitive");

            // first reasoning round
            Assert.assertTrue("expected inferred triple does not exist", testCon.hasStatement(b,s,a,true));
            Assert.assertTrue("expected inferred triple does not exist", testCon.hasStatement(a,t,c,true));
            Assert.assertTrue("expected inferred triple does not exist", testCon.hasStatement(b,t,d,true));

            // second reasoning round
            Assert.assertTrue("expected inferred triple does not exist", testCon.hasStatement(a,t,d,true));

            testCon.commit();
        } finally {
            testCon.close();
        }
    }

    /**
     * Test full reasoning with a small sample program. This test will carry out the following steps in order:
     * - import the turtle file "simple.ttl" into the repository
     * - load the program "simple.kwrl" into the reasoning engine
     * - wait for the reasoner to finish
     * - check if all expected inferred triples exist
     * - rerun reasoner
     * - check if all expected inferred triples exist
     *
     * @throws Exception
     */
    @Test
    public void testFullReasoning() throws Exception {

        // import the turtle file "simple.ttl" into the repository
        RepositoryConnection importCon = repository.getConnection();
        try {
            importCon.begin();
            importCon.add(this.getClass().getResourceAsStream("simple.ttl"),NS, RDFFormat.TURTLE);
            importCon.commit();
        } finally {
            importCon.close();
        }

        // load the program "simple.kwrl" into the reasoning engine
        rsail.addProgram("simple", this.getClass().getResourceAsStream("simple.kwrl"));

        // wait for the reasoner to finish
        while(rsail.getEngine().isRunning()) {
            log.debug("sleeping for 100ms to let engine finish processing ... ");
            Thread.sleep(100);
        }

        // check if all expected inferred triples exist
        RepositoryConnection testCon1 = repository.getConnection();
        try {
            testCon1.begin();
            URI a = testCon1.getValueFactory().createURI(NS + "a");
            URI b = testCon1.getValueFactory().createURI(NS + "b");
            URI c = testCon1.getValueFactory().createURI(NS + "c");
            URI d = testCon1.getValueFactory().createURI(NS + "d");
            URI s = testCon1.getValueFactory().createURI(NS + "symmetric");
            URI t = testCon1.getValueFactory().createURI(NS + "transitive");

            // first reasoning round
            Assert.assertTrue("expected inferred triple does not exist", testCon1.hasStatement(b,s,a,true));
            Assert.assertTrue("expected inferred triple does not exist", testCon1.hasStatement(a,t,c,true));
            Assert.assertTrue("expected inferred triple does not exist", testCon1.hasStatement(b,t,d,true));

            // second reasoning round
            Assert.assertTrue("expected inferred triple does not exist", testCon1.hasStatement(a,t,d,true));

            testCon1.commit();
        } finally {
            testCon1.close();
        }

        // rerun reasoner
        rsail.reRunPrograms();

        // wait for the reasoner to finish
        while(rsail.getEngine().isRunning()) {
            log.debug("sleeping for 100ms to let engine finish processing ... ");
            Thread.sleep(100);
        }

        // check if all expected inferred triples exist
        RepositoryConnection testCon2 = repository.getConnection();
        try {
            testCon2.begin();
            URI a = testCon2.getValueFactory().createURI(NS + "a");
            URI b = testCon2.getValueFactory().createURI(NS + "b");
            URI c = testCon2.getValueFactory().createURI(NS + "c");
            URI d = testCon2.getValueFactory().createURI(NS + "d");
            URI s = testCon2.getValueFactory().createURI(NS + "symmetric");
            URI t = testCon2.getValueFactory().createURI(NS + "transitive");

            // first reasoning round
            Assert.assertTrue("expected inferred triple does not exist", testCon2.hasStatement(b,s,a,true));
            Assert.assertTrue("expected inferred triple does not exist", testCon2.hasStatement(a,t,c,true));
            Assert.assertTrue("expected inferred triple does not exist", testCon2.hasStatement(b,t,d,true));

            // second reasoning round
            Assert.assertTrue("expected inferred triple does not exist", testCon2.hasStatement(a,t,d,true));

            testCon2.commit();
        } finally {
            testCon2.close();
        }
    }

}
