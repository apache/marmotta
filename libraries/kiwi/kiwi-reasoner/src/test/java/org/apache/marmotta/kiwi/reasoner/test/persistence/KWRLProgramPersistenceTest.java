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
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.reasoner.model.program.Program;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParser;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParserBase;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningConnection;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningPersistence;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;

/**
 * This test verifies the persistence functionality of the reasoning component regarding storing, loading and deleting
 * reasoning programs.
 *
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
public class KWRLProgramPersistenceTest {

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
    private KiWiReasoningPersistence rpersistence;

    private Repository repository;


    public KWRLProgramPersistenceTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
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
        repository = new SailRepository(new MemoryStore());
        repository.initialize();

        persistence = new KiWiPersistence("test",jdbcUrl,jdbcUser,jdbcPass,dialect);
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


    @Test
    public void testTablesCreateDrop() throws Exception {
        // test if database exists and has a version
        KiWiConnection connection = rpersistence.getConnection();
        try {
            Assert.assertThat(connection.getDatabaseTables(), hasItems(
                    "reasoner_programs", "reasoner_program_namespaces", "reasoner_program_rules",
                    "reasoner_rules", "reasoner_justifications", "reasoner_just_supp_triples", "reasoner_just_supp_rules"));
            Assert.assertEquals(1, connection.getDatabaseVersion());

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

            Assert.assertNotNull("program did not get a database ID",p.getId());

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

            Assert.assertNotNull("program did not get a database ID",p.getId());


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
