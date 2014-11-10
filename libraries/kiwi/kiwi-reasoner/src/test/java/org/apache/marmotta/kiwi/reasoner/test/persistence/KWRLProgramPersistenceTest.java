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

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.reasoner.model.program.Program;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParser;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParserBase;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningConnection;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningPersistence;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;

/**
 * This test verifies the persistence functionality of the reasoning component regarding storing, loading and deleting
 * reasoning programs.
 *
 * @see org.apache.marmotta.kiwi.persistence.KiWiConnection
 * @see org.apache.marmotta.kiwi.persistence.KiWiPersistence
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class KWRLProgramPersistenceTest {

    private KiWiPersistence persistence;
    private KiWiReasoningPersistence rpersistence;

    private Repository repository;
    private final KiWiConfiguration config;


    public KWRLProgramPersistenceTest(KiWiConfiguration config) {
        this.config = config;
        
    }


    @Before
    public void initDatabase() throws Exception {
        repository = new SailRepository(new MemoryStore());
        repository.initialize();

        persistence = new KiWiPersistence(config);
        persistence.initialise();
        persistence.initDatabase();

        rpersistence = new KiWiReasoningPersistence(persistence, repository.getValueFactory());
        rpersistence.initDatabase();

    }

    @After
    public void dropDatabase() throws Exception {
        rpersistence.dropDatabase();

        persistence.dropDatabase();
        persistence.shutdown();

        repository.shutDown();
    }

    final Logger logger =
            LoggerFactory.getLogger(KWRLProgramPersistenceTest.class);



    @Test
    public void testTablesCreateDrop() throws Exception {
        // test if database exists and has a version
        KiWiConnection connection = rpersistence.getConnection();
        try {
            Assert.assertThat(connection.getDatabaseTables(), hasItems(
                    "reasoner_programs", "reasoner_program_namespaces", "reasoner_program_rules",
                    "reasoner_rules", "reasoner_justifications", "reasoner_just_supp_triples", "reasoner_just_supp_rules"));
            Assert.assertEquals(KiWiDialect.VERSION, connection.getDatabaseVersion());

            connection.commit();
        } finally {
            connection.close();
        }
    }

    @Test
    public void testStoreLoadProgram() throws Exception {
        KWRLProgramParserBase parser = new KWRLProgramParser(repository.getValueFactory(), this.getClass().getResourceAsStream("test-001.kwrl"));
        Program p = parser.parseProgram();
        p.setName("test-001");

        KiWiReasoningConnection connection = rpersistence.getConnection();
        try {
            // should not throw an exception and the program should have a database ID afterwards
            connection.storeProgram(p);
            connection.commit();

            Assert.assertTrue("program did not get a database ID", p.getId() >= 0);

            // load the program by name and check if it is equal to the original program
            Program p1 = connection.loadProgram("test-001");
            connection.commit();

            Assert.assertNotNull("load program by name: loaded program is null",p1);
            Assert.assertEquals("load program by name: loaded program differs from original",p,p1);

            // load the program by name and check if it is equal to the original program
            Program p2 = connection.loadProgram(p.getId());
            connection.commit();

            Assert.assertNotNull("load program by ID: loaded program is null",p2);
            Assert.assertEquals("load program by ID: loaded program differs from original",p,p2);

        } finally {
            connection.close();
        }
    }

    /**
     * Test storing and then updating a program (by removing two rules)
     */
    @Test
    public void testUpdateProgram() throws Exception {
        KWRLProgramParserBase parser = new KWRLProgramParser(repository.getValueFactory(), this.getClass().getResourceAsStream("test-001.kwrl"));
        Program p = parser.parseProgram();
        p.setName("test-001");

        KiWiReasoningConnection connection = rpersistence.getConnection();
        try {
            // should not throw an exception and the program should have a database ID afterwards
            connection.storeProgram(p);
            connection.commit();

            Assert.assertTrue("program did not get a database ID", p.getId() >= 0);


            // load the program by name and check if it is equal to the original program
            Program p1 = connection.loadProgram("test-001");
            connection.commit();

            Assert.assertNotNull("load program by name: loaded program is null",p1);
            Assert.assertEquals("load program by name: loaded program differs from original",p,p1);


            PreparedStatement listRules1 = connection.getJDBCConnection().prepareStatement("SELECT count(*) AS count FROM reasoner_rules");
            ResultSet resultListRules1 = listRules1.executeQuery();

            Assert.assertTrue(resultListRules1.next());
            Assert.assertEquals(5, resultListRules1.getInt("count"));
            resultListRules1.close();
            connection.commit();



            // now remove two rules from the original and update the existing program
            p.getRules().remove(p.getRules().size()-1);
            p.getRules().remove(p.getRules().size()-1);
            p.addNamespace("myns","http://example.com/myns");

            connection.updateProgram(p);

            // load the program by name and check if it is equal to the original program
            Program p2 = connection.loadProgram(p.getName());
            connection.commit();

            Assert.assertNotNull("load program by name: loaded program is null",p2);
            Assert.assertEquals("load program by name: loaded program differs from original",p,p2);

            PreparedStatement listRules2 = connection.getJDBCConnection().prepareStatement("SELECT count(*) AS count FROM reasoner_rules");
            ResultSet resultListRules2 = listRules2.executeQuery();

            Assert.assertTrue(resultListRules2.next());
            Assert.assertEquals(3, resultListRules2.getInt("count"));
            resultListRules2.close();
            connection.commit();



        } finally {
            connection.close();
        }
    }

    /**
     * Add a bunch of reasoner programs to the database and check if listing the programs works
     *
     * @throws Exception
     */
    @Test
    public void testListDeletePrograms() throws Exception {
        KiWiReasoningConnection connection = rpersistence.getConnection();
        try {
            List<Program> programs = new ArrayList<Program>();
            for(String name : new String[] {"test-001", "test-002", "test-003", "test-004"}) {
                KWRLProgramParserBase parser = new KWRLProgramParser(repository.getValueFactory(), this.getClass().getResourceAsStream(name+".kwrl"));
                Program p = parser.parseProgram();
                p.setName(name);
                connection.storeProgram(p);
                connection.commit();

                programs.add(p);
            }

            // now we should have a collection of 4 programs in the database
            PreparedStatement checkNodeStmt = connection.getJDBCConnection().prepareStatement("SELECT count(*) AS count FROM reasoner_programs");
            ResultSet result = checkNodeStmt.executeQuery();

            Assert.assertTrue(result.next());
            Assert.assertEquals(4, result.getInt("count"));
            result.close();
            connection.commit();

            // list programs should return the same number and equal programs
            List<Program> dbPrograms1 = asList(connection.listPrograms());
            Assert.assertEquals(4, dbPrograms1.size());
            Assert.assertEquals("list of original programs differs from list of database",programs,dbPrograms1);

            // delete all programs and check if the database does not contain any remaining entries in the different tables
            for(Program p : programs) {
                connection.deleteProgram(p);
                connection.commit();
            }
            List<Program> dbPrograms2 = asList(connection.listPrograms());
            Assert.assertEquals(0, dbPrograms2.size());

            PreparedStatement listPrograms = connection.getJDBCConnection().prepareStatement("SELECT count(*) AS count FROM reasoner_programs");
            ResultSet resultListPrograms = listPrograms.executeQuery();

            Assert.assertTrue(resultListPrograms.next());
            Assert.assertEquals(0, resultListPrograms.getInt("count"));
            resultListPrograms.close();
            connection.commit();

            PreparedStatement listRules = connection.getJDBCConnection().prepareStatement("SELECT count(*) AS count FROM reasoner_rules");
            ResultSet resultListRules = listRules.executeQuery();

            Assert.assertTrue(resultListRules.next());
            Assert.assertEquals(0, resultListRules.getInt("count"));
            resultListRules.close();
            connection.commit();

            PreparedStatement listNamespaces = connection.getJDBCConnection().prepareStatement("SELECT count(*) AS count FROM reasoner_program_namespaces");
            ResultSet resultListNamespaces = listNamespaces.executeQuery();

            Assert.assertTrue(resultListNamespaces.next());
            Assert.assertEquals(0, resultListNamespaces.getInt("count"));
            resultListNamespaces.close();
            connection.commit();

            PreparedStatement listProgramRules = connection.getJDBCConnection().prepareStatement("SELECT count(*) AS count FROM reasoner_program_rules");
            ResultSet resultListProgramRules = listProgramRules.executeQuery();

            Assert.assertTrue(resultListProgramRules.next());
            Assert.assertEquals(0, resultListProgramRules.getInt("count"));
            resultListProgramRules.close();
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
