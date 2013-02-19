/*
 * Copyright (c) 2013 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.marmotta.kiwi.reasoner.test.engine;

import info.aduna.iteration.Iterations;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.reasoner.engine.ReasoningConfiguration;
import org.apache.marmotta.kiwi.reasoner.engine.ReasoningEngine;
import org.apache.marmotta.kiwi.reasoner.model.program.Justification;
import org.apache.marmotta.kiwi.reasoner.model.program.Program;
import org.apache.marmotta.kiwi.reasoner.model.program.Rule;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParser;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParserBase;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningConnection;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningPersistence;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.apache.marmotta.kiwi.transactions.api.TransactionalSail;
import org.apache.marmotta.kiwi.transactions.model.TransactionData;
import org.apache.marmotta.kiwi.transactions.sail.KiWiTransactionalSail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;

/**
 * This test verifies the functionality of the KiWi Reasonong Engine. Based on a small sample program, it will test
 * both incremental reasoning (by manually adding triples) and full reasoning. After reasoning completes, it will
 * check if the expected inferred triples as well as their justifications are present.
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
public class ReasoningEngineTest {

    private static Logger log = LoggerFactory.getLogger(ReasoningEngineTest.class);

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
    private TransactionalSail tsail;
    private KiWiPersistence persistence;
    private KiWiReasoningPersistence rpersistence;
    private ReasoningEngine engine;

    private Repository repository;


    public ReasoningEngineTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
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

        persistence = new KiWiPersistence("test",jdbcUrl,jdbcUser,jdbcPass,dialect);
        persistence.initDatabase();


        store      = new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred");
        tsail      = new KiWiTransactionalSail(store);
        repository = new SailRepository(tsail);
        repository.initialize();

        rpersistence = new KiWiReasoningPersistence(persistence, repository.getValueFactory());
        rpersistence.initDatabase();


        // store a program, very simple transitive and symmetric rules:
        // ($1 ex:property $2), ($2 ex:property $3) -> ($1 ex:property $3)
        // ($1 ex:symmetric $2) -> ($2 ex:symmetric $1)
        KWRLProgramParserBase parser = new KWRLProgramParser(repository.getValueFactory(), this.getClass().getResourceAsStream("simple.kwrl"));
        Program p = parser.parseProgram();
        p.setName("simple");

        KiWiReasoningConnection connection = rpersistence.getConnection();
        try {
            // should not throw an exception and the program should have a database ID afterwards
            connection.storeProgram(p);
            connection.commit();
        } finally {
            connection.close();
        }

        // instantiate reasoning engine, will load the programs into memory
        engine = new ReasoningEngine(rpersistence,tsail,new ReasoningConfiguration());

    }

    @After
    public void dropDatabase() throws Exception {
        engine.shutdown();

        rpersistence.dropDatabase();

        persistence.dropDatabase();
        persistence.shutdown();

        repository.shutDown();
    }


    /**
     * Test the reasoning engine by incrementally adding and later removing triples through explicit calls to the
     * reasoning engine methods. This test checks pure in-memory processing for rule2, which only contains a
     * single query pattern.
     *
     * @throws Exception
     */
    @Test
    public void testIncrementalReasoningMemory() throws Exception {
        RepositoryConnection con = repository.getConnection();
        KiWiReasoningConnection rcon = rpersistence.getConnection();
        try {
            con.begin();
            // create a triple (ex:a ex:symmetric ex:b); this should trigger rule 2 of the reasoner and add the
            // inverse relationship
            Resource subject  = con.getValueFactory().createURI(NS+"a");
            URI      property = con.getValueFactory().createURI(NS+"symmetric");
            Resource object   = con.getValueFactory().createURI(NS+"b");

            con.add(subject,property,object);
            con.commit();

            // load the statement from the connection so we can add it to the reasoner
            List<Statement> statements = Iterations.asList(con.getStatements(subject,property,object, false));
            Assert.assertEquals(1,statements.size());

            // add triple to engine
            TransactionData data = new TransactionData();
            data.getAddedTriples().add(statements.get(0));
            engine.afterCommit(data);

            // wait for reasoning to complete
            while(engine.isRunning()) {
                log.debug("sleeping for 100ms to let engine finish processing ... ");
                Thread.sleep(100);
            }
            con.begin();

            // after the engine completes, we check whether
            // 1) the expected inferred triple exists
            // 2) the inferred triple is properly justified (based on rule2 and on the triple contained in the transaction data)
            List<Statement> inferred = Iterations.asList(con.getStatements(object,property,subject, true));
            Assert.assertEquals("number of inferred triples differs from expected result",1,inferred.size());

            KiWiTriple triple = (KiWiTriple)inferred.get(0);
            List<Justification> justifications = Iterations.asList(rcon.listJustificationsForTriple(triple));
            Assert.assertEquals("number of justifications for triple differs from expected result",1,justifications.size());

            Justification j = justifications.get(0);
            Assert.assertEquals("number of supporting triples differs from expected result",1,j.getSupportingTriples().size());
            Assert.assertEquals("number of supporting rules differs from expected result",1,j.getSupportingRules().size());

            Assert.assertThat("supporting triple does not match expectation", j.getSupportingTriples(), hasItem((KiWiTriple)statements.get(0)));

            con.commit();


            // now remove again the base triple and inform the reasoner about it, as a consequence, the inferred
            // triple should also be removed
            con.remove(subject,property,object);
            con.commit();
            TransactionData data2 = new TransactionData();
            data2.getRemovedTriples().add(statements.get(0));
            engine.afterCommit(data2);

            // wait for reasoning to complete
            while(engine.isRunning()) {
                log.debug("sleeping for 100ms to let engine finish processing ... ");
                Thread.sleep(100);
            }
            con.begin();

            List<Statement> inferred2 = Iterations.asList(con.getStatements(object,property,subject, true));
            Assert.assertEquals("number of inferred triples differs from expected result", 0, inferred2.size());

            con.commit();
            rcon.commit();
        } finally {
            con.close();
            rcon.close();
        }
    }


    /**
     * Test the reasoning engine by incrementally adding and later removing triples through explicit calls to the
     * reasoning engine methods. This test checks rule1 and thus involves both, in-memory pattern matching and database
     * queries.
     *
     * @throws Exception
     */
    @Test
    public void testIncrementalReasoningConjunction() throws Exception {
        RepositoryConnection con = repository.getConnection();
        KiWiReasoningConnection rcon = rpersistence.getConnection();
        try {
            con.begin();
            // create a triple (ex:a ex:symmetric ex:b); this should trigger rule 2 of the reasoner and add the
            // inverse relationship
            Resource a  = con.getValueFactory().createURI(NS+"a");
            URI      property = con.getValueFactory().createURI(NS+"transitive");
            Resource b   = con.getValueFactory().createURI(NS+"b");
            Resource c   = con.getValueFactory().createURI(NS+"c");
            Resource d   = con.getValueFactory().createURI(NS+"d");

            con.add(a,property,b);
            con.add(b,property,c);
            con.commit();

            // load the statement from the connection so we can add it to the reasoner
            List<Statement> statements = Iterations.asList(con.getStatements(null,property,null, false));
            Assert.assertEquals(2,statements.size());

            // add triples to engine
            TransactionData data = new TransactionData();
            data.getAddedTriples().addAll(statements);
            engine.afterCommit(data);

            // wait for reasoning to complete
            while(engine.isRunning()) {
                log.debug("sleeping for 100ms to let engine finish processing ... ");
                Thread.sleep(100);
            }
            con.begin();

            // after the engine completes, we check whether
            // 1) the expected inferred triple exists (must be (ex:a ex:transitive ex:c) )
            // 2) the inferred triple is properly justified (based on rule2 and on the triple contained in the transaction data)
            List<Statement> inferred = Iterations.asList(con.getStatements(a,property,c, true));
            Assert.assertEquals("number of inferred triples differs from expected result",1,inferred.size());

            KiWiTriple triple = (KiWiTriple)inferred.get(0);
            List<Justification> justifications = Iterations.asList(rcon.listJustificationsForTriple(triple));
            Assert.assertEquals("number of justifications for triple differs from expected result",1,justifications.size());

            Justification j = justifications.get(0);
            Assert.assertEquals("number of supporting triples differs from expected result",2,j.getSupportingTriples().size());
            Assert.assertEquals("number of supporting rules differs from expected result",1,j.getSupportingRules().size());

            Assert.assertThat("supporting triple does not match expectation", j.getSupportingTriples(), hasItem((KiWiTriple)statements.get(0)));
            Assert.assertThat("supporting triple does not match expectation", j.getSupportingTriples(), hasItem((KiWiTriple)statements.get(1)));

            con.commit();


            // add another triple and check if the incremental reasoning works
            con.add(c,property,d);
            con.commit();

            // load the statement from the connection so we can add it to the reasoner
            List<Statement> statements2 = Iterations.asList(con.getStatements(c,property,d, false));
            Assert.assertEquals(1, statements2.size());

            // add triples to engine
            TransactionData data2 = new TransactionData();
            data2.getAddedTriples().addAll(statements2);
            engine.afterCommit(data2);

            // wait for reasoning to complete
            while(engine.isRunning()) {
                log.debug("sleeping for 100ms to let engine finish processing ... ");
                Thread.sleep(100);
            }
            con.begin();


            // after the engine completes, we check whether the expected inferred triples exist
            // (must be (ex:a ex:transitive ex:d) and (ex:b ex:transitive ex:d) )
            List<Statement> inferred2 = Iterations.asList(con.getStatements(a,property,d, true));
            Assert.assertEquals("number of inferred triples differs from expected result", 1, inferred2.size());

            List<Statement> inferred3 = Iterations.asList(con.getStatements(b,property,d, true));
            Assert.assertEquals("number of inferred triples differs from expected result", 1, inferred3.size());


            // now remove again the base triple and inform the reasoner about it, as a consequence, the inferred
            // triple should also be removed
            con.remove(c, property, d);
            con.commit();
            TransactionData data3 = new TransactionData();
            data3.getRemovedTriples().add(statements2.get(0));
            engine.afterCommit(data3);

            // wait for reasoning to complete
            while(engine.isRunning()) {
                log.debug("sleeping for 100ms to let engine finish processing ... ");
                Thread.sleep(100);
            }
            con.begin();

            List<Statement> inferred4 = Iterations.asList(con.getStatements(a,property,d, true));
            Assert.assertEquals("number of inferred triples differs from expected result", 0, inferred4.size());

            List<Statement> inferred5 = Iterations.asList(con.getStatements(b,property,d, true));
            Assert.assertEquals("number of inferred triples differs from expected result", 0, inferred5.size());

            con.commit();
            rcon.commit();
        } finally {
            con.close();
            rcon.close();
        }
    }


    /**
     * Test running a full reasoning over the triple store based on the simple program and the simple.ttl data file.
     * Test if the expected triples are present. Since we are only evaluating a single reasoning round, we cannot
     * expect more complicated triples that involve chaining.
     *
     * @throws Exception
     */
    @Test
    public void testFullReasoning() throws Exception {
        RepositoryConnection con = repository.getConnection();
        KiWiReasoningConnection rcon = rpersistence.getConnection();
        try {
            // add some triples
            con.begin();

            Resource a   = con.getValueFactory().createURI(NS+"a");
            Resource b   = con.getValueFactory().createURI(NS+"b");
            Resource c   = con.getValueFactory().createURI(NS+"c");
            Resource d   = con.getValueFactory().createURI(NS+"d");
            URI      t   = con.getValueFactory().createURI(NS+"transitive");
            URI      s   = con.getValueFactory().createURI(NS+"symmetric");

            con.add(this.getClass().getResourceAsStream("simple.ttl"),"http://localhost/resource/", RDFFormat.TURTLE);
            con.commit();

            // run the full reasoner
            engine.reRunPrograms();

            // wait for reasoning to complete
            while(engine.isRunning()) {
                log.debug("sleeping for 100ms to let engine finish processing ... ");
                Thread.sleep(100);
            }
            con.begin();

            // after reasoning is finished, we expect to find the following triples:

            Assert.assertTrue("expected inferred triple not found", con.hasStatement(a,t,c,true));
            Assert.assertTrue("expected inferred triple not found", con.hasStatement(b,t,d,true));
            Assert.assertTrue("expected inferred triple not found", con.hasStatement(b,s,a,true));

            con.commit();
        } finally {
            con.close();
            rcon.close();
        }

    }


    /**
     * Test adding and removing rules to an already inferred state of the triple store. When a rule is added, all
     * possible new inferences should be added to the inferred triples. When a rule is removed, all inferences
     * based on this rule should also be removed.
     *
     * @throws Exception
     */
    //@Test
    public void testAddRemoveRule() throws Exception {
        RepositoryConnection con = repository.getConnection();
        KiWiReasoningConnection rcon = rpersistence.getConnection();
        try {
            // add some triples
            con.begin();

            Resource a   = con.getValueFactory().createURI(NS+"a");
            Resource b   = con.getValueFactory().createURI(NS+"b");
            Resource c   = con.getValueFactory().createURI(NS+"c");
            Resource d   = con.getValueFactory().createURI(NS+"d");
            URI      t   = con.getValueFactory().createURI(NS+"transitive");
            URI      s   = con.getValueFactory().createURI(NS+"symmetric");

            con.add(this.getClass().getResourceAsStream("simple.ttl"),"http://localhost/resource/", RDFFormat.TURTLE);
            con.commit();

            // run the full reasoner
            engine.reRunPrograms();

            // wait for reasoning to complete
            while(engine.isRunning()) {
                log.debug("sleeping for 100ms to let engine finish processing ... ");
                Thread.sleep(100);
            }
            con.begin();

            // after reasoning is finished, we expect to find the following triples:

            Assert.assertTrue("expected inferred triple not found", con.hasStatement(a,t,c,true));
            Assert.assertTrue("expected inferred triple not found", con.hasStatement(b,t,d,true));
            Assert.assertTrue("expected inferred triple not found", con.hasStatement(b,s,a,true));

            con.commit();


            // now we remove the rule2 (symmetric rule) from the program, update the program in the database and then
            // inform the reasoning engine about the removed rule
            Program p = rcon.loadProgram("simple");
            Rule removed = null;
            Iterator<Rule> it = p.getRules().iterator();
            while(it.hasNext()) {
                Rule r = it.next();
                if(r.getName().equals("rule2")) {
                    it.remove();
                    removed = r;
                }
            }
            Assert.assertNotNull("rule 2 not found in program", removed);
            rcon.updateProgram(p);
            rcon.commit();

            engine.notifyRemoveRules();

            // after removing, the inferred symmetric triple should be gone, but the others should still exist
            con.begin();
            Assert.assertTrue("expected inferred triple not found", con.hasStatement(a,t,c,true));
            Assert.assertTrue("expected inferred triple not found", con.hasStatement(b, t, d, true));
            Assert.assertFalse("unexpected inferred triple found", con.hasStatement(b, s, a, true));
            con.commit();


            // let's add the rule again to the program, update the database, and inform the engine
            p.getRules().add(removed);
            rcon.updateProgram(p);
            rcon.commit();

            engine.notifyAddRule(removed);

            // after adding, the inferred symmetric triple should again be present
            con.begin();
            Assert.assertTrue("expected inferred triple not found", con.hasStatement(a,t,c,true));
            Assert.assertTrue("expected inferred triple not found", con.hasStatement(b,t,d,true));
            Assert.assertTrue("expected inferred triple not found", con.hasStatement(b, s, a, true));
            con.commit();

        } finally {
            con.close();
            rcon.close();
        }

    }

}
