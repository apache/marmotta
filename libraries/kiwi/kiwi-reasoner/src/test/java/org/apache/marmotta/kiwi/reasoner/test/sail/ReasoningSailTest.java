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

import org.apache.marmotta.commons.sesame.transactions.sail.KiWiTransactionalSail;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.reasoner.engine.ReasoningConfiguration;
import org.apache.marmotta.kiwi.reasoner.sail.KiWiReasoningSail;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.RepositoryTest;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
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
 * 
 * @see org.apache.marmotta.kiwi.persistence.KiWiConnection
 * @see org.apache.marmotta.kiwi.persistence.KiWiPersistence
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class ReasoningSailTest {

    private static Logger log = LoggerFactory.getLogger(ReasoningSailTest.class);

    private static final String NS = "http://localhost/resource/";

    private KiWiStore store;
    private KiWiTransactionalSail tsail;
    private KiWiReasoningSail     rsail;

    private Repository repository;

    private final KiWiConfiguration config;


    public ReasoningSailTest(KiWiConfiguration config) {
        this.config = config;
    }


    @Before
    public void initDatabase() throws Exception {
        store = new KiWiStore(config);
        tsail = new KiWiTransactionalSail(store);
        rsail = new KiWiReasoningSail(tsail, new ReasoningConfiguration());
        repository = new SailRepository(rsail);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws Exception {
        rsail.getEngine().shutdown(true);
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
