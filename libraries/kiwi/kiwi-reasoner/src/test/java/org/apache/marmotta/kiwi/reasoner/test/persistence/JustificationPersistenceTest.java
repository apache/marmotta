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
package org.apache.marmotta.kiwi.reasoner.test.persistence;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import info.aduna.iteration.CloseableIteration;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.reasoner.model.program.Justification;
import org.apache.marmotta.kiwi.reasoner.model.program.Program;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParser;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParserBase;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningConnection;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningPersistence;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This test verifies the persistence functionality of the reasoning component regarding storing, loading and deleting
 * reasoning programs.
 *
 * @see org.apache.marmotta.kiwi.persistence.KiWiConnection
 * @see org.apache.marmotta.kiwi.persistence.KiWiPersistence
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class JustificationPersistenceTest {

    private KiWiPersistence persistence;
    private KiWiReasoningPersistence rpersistence;

    private Repository repository;
    private final KiWiConfiguration config;


    public JustificationPersistenceTest(KiWiConfiguration config) {
      this.config = config;
    }


    @Before
    public void initDatabase() throws Exception {
        KiWiStore store = new KiWiStore(config);

        repository = new SailRepository(store);
        repository.initialize();

        persistence = store.getPersistence();

        rpersistence = new KiWiReasoningPersistence(persistence, repository.getValueFactory());
        rpersistence.initDatabase();

    }

    @After
    public void dropDatabase() throws Exception {
        rpersistence.dropDatabase();
        persistence.dropDatabase();
        repository.shutDown();
    }

    final Logger logger =
            LoggerFactory.getLogger(JustificationPersistenceTest.class);


    /**
     * Test 1: create some triples through a repository connection (some inferred, some base), load a program, and
     * store justifications for the inferred triples based on rules and base triples. Test the different listing
     * functions.
     *
     */
    @Test
    public void testStoreJustifications() throws Exception {
        KiWiValueFactory v = (KiWiValueFactory) repository.getValueFactory();

        URI ctxb = v.createURI("http://localhost/context/default");
        URI ctxi = v.createURI("http://localhost/context/inferred");

        URI s1 = v.createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
        URI s2 = v.createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
        URI s3 = v.createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
        URI p1 = v.createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
        URI p2 = v.createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
        URI o1 = v.createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
        URI o2 = v.createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
        URI o3 = v.createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));


        // first, load a sample program (it does not really matter what it actually contains, since we are not really
        // running the reasoner)
        KWRLProgramParserBase parser = new KWRLProgramParser(v, this.getClass().getResourceAsStream("test-001.kwrl"));
        Program p = parser.parseProgram();
        p.setName("test-001");

        KiWiReasoningConnection connection = rpersistence.getConnection();
        try {
            // should not throw an exception and the program should have a database ID afterwards
            connection.storeProgram(p);
            connection.commit();
        } finally {
            connection.close();
        }

        // then get a connection to the repository and create a number of triples, some inferred and some base
        RepositoryConnection con = repository.getConnection();
        try {
            con.add(s1,p1,o1);
            con.add(s2,p1,o2);
            con.add(s3,p1,o3);

            con.add(s1,p2,o1,ctxi);
            con.add(s2,p2,o2,ctxi);
            con.add(s3,p2,o3,ctxi);

            con.commit();
        } finally {
            con.close();
        }

        connection = rpersistence.getConnection();
        try {
            // retrieve the persisted triples and put them into two sets to build justifications
            List<Statement> baseTriples = asList(connection.listTriples(null,null,null,v.convert(ctxb),false, true));
            List<Statement> infTriples = asList(connection.listTriples(null,null,null,v.convert(ctxi),true, true));

            Assert.assertEquals("number of base triples was not 3", 3, baseTriples.size());
            Assert.assertEquals("number of inferred triples was not 3", 3, infTriples.size());

            // we manually update the "inferred" flag for all inferred triples, since this is not possible through the
            // repository API
            PreparedStatement updateInferred = connection.getJDBCConnection().prepareStatement("UPDATE triples SET inferred = true WHERE id = ?");
            for(Statement stmt : infTriples) {
                KiWiTriple triple = (KiWiTriple)stmt;
                updateInferred.setLong(1,triple.getId());
                updateInferred.addBatch();
            }
            updateInferred.executeBatch();
            updateInferred.close();

            // now we create some justifications for the inferred triples and store them
            Set<Justification> justifications = new HashSet<Justification>();
            Justification j1 = new Justification();
            j1.getSupportingRules().add(p.getRules().get(0));
            j1.getSupportingRules().add(p.getRules().get(1));
            j1.getSupportingTriples().add((KiWiTriple) baseTriples.get(0));
            j1.getSupportingTriples().add((KiWiTriple) baseTriples.get(1));
            j1.setTriple((KiWiTriple) infTriples.get(0));
            justifications.add(j1);

            Justification j2 = new Justification();
            j2.getSupportingRules().add(p.getRules().get(1));
            j2.getSupportingTriples().add((KiWiTriple) baseTriples.get(1));
            j2.getSupportingTriples().add((KiWiTriple) baseTriples.get(2));
            j2.setTriple((KiWiTriple) infTriples.get(1));
            justifications.add(j2);

            connection.storeJustifications(justifications);
            connection.commit();

            // we should now have two justifications in the database
            PreparedStatement listJustifications = connection.getJDBCConnection().prepareStatement("SELECT count(*) AS count FROM reasoner_justifications");
            ResultSet resultListJustifications = listJustifications.executeQuery();

            Assert.assertTrue(resultListJustifications.next());
            Assert.assertEquals(2, resultListJustifications.getInt("count"));
            resultListJustifications.close();
            connection.commit();

            PreparedStatement listSupportingTriples = connection.getJDBCConnection().prepareStatement("SELECT count(*) AS count FROM reasoner_just_supp_triples");
            ResultSet resultListSupportingTriples = listSupportingTriples.executeQuery();

            Assert.assertTrue(resultListSupportingTriples.next());
            Assert.assertEquals(4, resultListSupportingTriples.getInt("count"));
            resultListSupportingTriples.close();
            connection.commit();

            PreparedStatement listSupportingRules = connection.getJDBCConnection().prepareStatement("SELECT count(*) AS count FROM reasoner_just_supp_rules");
            ResultSet resultListSupportingRules = listSupportingRules.executeQuery();

            Assert.assertTrue(resultListSupportingRules.next());
            Assert.assertEquals(3, resultListSupportingRules.getInt("count"));
            resultListSupportingRules.close();
            connection.commit();



            // *** check listing justifications by base triple (supporting triple)

            // there should now be two justifications based on triple baseTriples.get(1))
            List<Justification> supported1 = asList(connection.listJustificationsBySupporting((KiWiTriple) baseTriples.get(1)));
            Assert.assertEquals("number of justifications is wrong",2,supported1.size());
            Assert.assertThat("justifications differ", supported1, hasItems(j1,j2));

            // only j1 should be supported by triple baseTriples.get(0))
            List<Justification> supported2 = asList(connection.listJustificationsBySupporting((KiWiTriple) baseTriples.get(0)));
            Assert.assertEquals("number of justifications is wrong", 1, supported2.size());
            Assert.assertThat("justifications differ", supported2, allOf(hasItem(j1), not(hasItem(j2))));

            // only j2 should be supported by triple baseTriples.get(2))
            List<Justification> supported3 = asList(connection.listJustificationsBySupporting((KiWiTriple) baseTriples.get(2)));
            Assert.assertEquals("number of justifications is wrong", 1, supported3.size());
            Assert.assertThat("justifications differ", supported3, allOf(hasItem(j2), not(hasItem(j1))));

            // *** check listing justificatoins by supporting rule

            // there should now be two justifications based on triple p.getRules().get(1)
            List<Justification> supported4 = asList(connection.listJustificationsBySupporting(p.getRules().get(1)));
            Assert.assertEquals("number of justifications is wrong", 2, supported4.size());
            Assert.assertThat("justifications differ", supported4, hasItems(j1,j2));

            // only j1 should be supported by triple p.getRules().get(0)
            List<Justification> supported5 = asList(connection.listJustificationsBySupporting(p.getRules().get(0)));
            Assert.assertEquals("number of justifications is wrong", 1, supported5.size());
            Assert.assertThat("justifications differ", supported5, allOf(hasItem(j1), not(hasItem(j2))));


            // *** check listing justifications by supported (inferred) triple

            // there should now be one justification supporting infTriples.get(0)
            List<Justification> supported6 = asList(connection.listJustificationsForTriple((KiWiTriple) infTriples.get(0)));
            Assert.assertEquals("number of justifications is wrong", 1, supported6.size());
            Assert.assertThat("justifications differ", supported6, allOf(hasItem(j1), not(hasItem(j2))));

            // there should now be one justification supporting infTriples.get(1)
            List<Justification> supported7 = asList(connection.listJustificationsForTriple((KiWiTriple) infTriples.get(1)));
            Assert.assertEquals("number of justifications is wrong", 1, supported7.size());
            Assert.assertThat("justifications differ", supported7, allOf(hasItem(j2), not(hasItem(j1))));

            // there should now be no justification supporting infTriples.get(2)
            List<Justification> supported8 = asList(connection.listJustificationsForTriple((KiWiTriple) infTriples.get(2)));
            Assert.assertEquals("number of justifications is wrong", 0, supported8.size());


            // *** check listing unsupported triples
            List<KiWiTriple> unsupported = asList(connection.listUnsupportedTriples());
            Assert.assertEquals("number of unsupported triples is wrong",1,unsupported.size());
            Assert.assertThat("unsupported triples differ", unsupported, hasItem((KiWiTriple)infTriples.get(2)));


            // now we delete justification 2; as a consequence,
            // - there should be only once justification left
            // - there should be two unsupported triples
            connection.deleteJustifications(Collections.singleton(j2));


            // we should now have one justifications in the database
            resultListJustifications = listJustifications.executeQuery();

            Assert.assertTrue(resultListJustifications.next());
            Assert.assertEquals(1, resultListJustifications.getInt("count"));
            resultListJustifications.close();
            connection.commit();

            resultListSupportingTriples = listSupportingTriples.executeQuery();

            Assert.assertTrue(resultListSupportingTriples.next());
            Assert.assertEquals(2, resultListSupportingTriples.getInt("count"));
            resultListSupportingTriples.close();
            connection.commit();

            resultListSupportingRules = listSupportingRules.executeQuery();

            Assert.assertTrue(resultListSupportingRules.next());
            Assert.assertEquals(2, resultListSupportingRules.getInt("count"));
            resultListSupportingRules.close();
            connection.commit();

            List<KiWiTriple> unsupported2 = asList(connection.listUnsupportedTriples());
            Assert.assertEquals("number of unsupported triples is wrong",2,unsupported2.size());
            Assert.assertThat("unsupported triples differ", unsupported2, hasItem((KiWiTriple)infTriples.get(1)));


        } catch(BatchUpdateException ex) {
            if(ex.getNextException() != null) {
                ex.printStackTrace();
                throw ex.getNextException();
            } else {
                throw ex;
            }
        } finally {
            connection.close();
        }

    }



    /**
     * Workaround for https://openrdf.atlassian.net/browse/SES-1702 in Sesame 2.7.0-beta1
     * @param <E>
     * @return
     */
    public static <E,X extends Exception> List<E> asList(CloseableIteration<E,X> result) throws Exception {
        ArrayList<E> collection = new ArrayList<E>();
        try {
            while (result.hasNext()) {
                collection.add(result.next());
            }

            return collection;
        } finally {
            result.close();
        }
    }

}
