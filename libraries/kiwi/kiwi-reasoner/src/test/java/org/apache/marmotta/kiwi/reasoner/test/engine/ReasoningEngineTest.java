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
package org.apache.marmotta.kiwi.reasoner.test.engine;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.fail;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iterations;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.marmotta.commons.sesame.transactions.api.TransactionalSail;
import org.apache.marmotta.commons.sesame.transactions.model.TransactionData;
import org.apache.marmotta.commons.sesame.transactions.sail.KiWiTransactionalSail;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
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
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This test verifies the functionality of the KiWi Reasonong Engine. Based on a small sample program, it will test
 * both incremental reasoning (by manually adding triples) and full reasoning. After reasoning completes, it will
 * check if the expected inferred triples as well as their justifications are present.
 *
 * @see org.apache.marmotta.kiwi.persistence.KiWiConnection
 * @see org.apache.marmotta.kiwi.persistence.KiWiPersistence
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class ReasoningEngineTest {

    private static Logger log = LoggerFactory.getLogger(ReasoningEngineTest.class);

    private static final String NS = "http://localhost/resource/";

     private KiWiStore store;
    private TransactionalSail tsail;
    private KiWiPersistence persistence;
    private KiWiReasoningPersistence rpersistence;
    private ReasoningEngine engine;

    private Repository repository;

    private final KiWiConfiguration config;


    public ReasoningEngineTest(KiWiConfiguration config) {
        this.config = config;
    }


    @Before
    public void initDatabase() throws Exception {
        store      = new KiWiStore(config);
        tsail      = new KiWiTransactionalSail(store);
        repository = new SailRepository(tsail);
        repository.initialize();

        persistence = store.getPersistence();

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
        engine.shutdown(true);

        rpersistence.dropDatabase();
        persistence.dropDatabase();

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

            log.debug("reasoning finished, running tests");

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


            // we also expect that there are justifications for all inferred triples
            Resource[][] patterns = new Resource[][] {
                    new Resource[] { a, t, c },
                    new Resource[] { b, t, d },
                    new Resource[] { b, s, a }
            };


            RepositoryResult<Statement> result = con.getStatements(null,null,null,true, con.getValueFactory().createURI(store.getInferredContext()));
            if(result.hasNext()) {
                while (result.hasNext()) {
                    Statement stmt1 = result.next();

                    CloseableIteration<Justification, SQLException> justs1 = rcon.listJustificationsForTriple((KiWiTriple) stmt1);
                    Assert.assertTrue(justs1.hasNext());
                }
            } else {
                fail("no inferred statements found");
            }
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
