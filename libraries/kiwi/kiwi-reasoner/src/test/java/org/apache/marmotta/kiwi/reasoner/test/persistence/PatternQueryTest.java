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

import com.google.common.collect.ImmutableSet;
import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.reasoner.model.program.LiteralField;
import org.apache.marmotta.kiwi.reasoner.model.program.Pattern;
import org.apache.marmotta.kiwi.reasoner.model.program.ResourceField;
import org.apache.marmotta.kiwi.reasoner.model.program.VariableField;
import org.apache.marmotta.kiwi.reasoner.model.query.QueryResult;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningConnection;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningPersistence;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class PatternQueryTest {

    // string constants for RDF values
    private static final String SUBJECT1 = "http://localhost/resource/S1";
    private static final String SUBJECT2 = "http://localhost/resource/S2";
    private static final String SUBJECT3 = "http://localhost/resource/S3";
    private static final String PREDICATE1 = "http://localhost/resource/P1";
    private static final String PREDICATE2 = "http://localhost/resource/P2";
    private static final String OBJECT1  = "http://localhost/resource/O1";
    private static final String OBJECT2  = "http://localhost/resource/O2";
    private static final String OBJECT3  = "Literal Value 1";
    private static final String OBJECT4  = "Literal Value 2";
    private static final String OBJECT5  = "Literal Value 3";



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


    public PatternQueryTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
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

        repository = new SailRepository(new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred"));
        repository.initialize();

        rpersistence = new KiWiReasoningPersistence(persistence, repository.getValueFactory());
        rpersistence.initDatabase();

        // add some triples
        RepositoryConnection con = repository.getConnection();
        try {
            ValueFactory v = con.getValueFactory();

            con.add(v.createURI(SUBJECT1), v.createURI(PREDICATE1), v.createURI(SUBJECT2));
            con.add(v.createURI(SUBJECT1), v.createURI(PREDICATE2), v.createLiteral(OBJECT3));
            con.add(v.createURI(SUBJECT2), v.createURI(PREDICATE1), v.createURI(OBJECT2));
            con.add(v.createURI(SUBJECT2), v.createURI(PREDICATE2), v.createLiteral(OBJECT4));
            con.add(v.createURI(SUBJECT3), v.createURI(PREDICATE2), v.createLiteral(OBJECT5));

            con.commit();
        } finally {
            con.close();
        }

    }

    @After
    public void dropDatabase() throws Exception {
        rpersistence.dropDatabase();

        persistence.dropDatabase();
        persistence.shutdown();

        repository.shutDown();
    }


    // test the method for querying patterns by:
    // - evaluating a single pattern without variables (no bindings but justifications non-empty)
    // - evaluating a single pattern with variables    (bindings and justifications non-empty)
    // - evaluating a collection of patterns           (more complex SQL query)

    /**
     * Test a single pattern with constant fields
     */
    @Test
    public void testSingleConstantPattern() throws Exception {
        ValueFactory v = repository.getValueFactory();
        URI subject = v.createURI(SUBJECT1);
        URI predicate = v.createURI(PREDICATE2);
        Literal object = v.createLiteral(OBJECT3);

        Pattern p = new Pattern(new ResourceField(subject), new ResourceField(predicate), new LiteralField(object));
        KiWiReasoningConnection connection = rpersistence.getConnection();
        try {
            List<QueryResult> results = asList(connection.query(Collections.singleton(p), null, null, null, true));
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(1,results.get(0).getJustifications().size());

            KiWiTriple justification = results.get(0).getJustifications().iterator().next();
            Assert.assertEquals(subject, justification.getSubject());
            Assert.assertEquals(predicate, justification.getPredicate());
            Assert.assertEquals(object, justification.getObject());

            connection.commit();
        } finally {
            connection.close();
        }
    }

    /**
     * Test a single pattern with constant fields and variables
     */
    @Test
    public void testSingleVariablePattern() throws Exception {
        ValueFactory v = repository.getValueFactory();
        URI predicate = v.createURI(PREDICATE2);

        Pattern p = new Pattern(new VariableField("X"), new ResourceField(predicate), new VariableField("Y"));
        KiWiReasoningConnection connection = rpersistence.getConnection();
        try {
            List<QueryResult> results = asList(connection.query(Collections.singleton(p), null, null, null, true));
            Assert.assertEquals(3, results.size());

            for(int i=0; i<3; i++) {
                Assert.assertEquals(1,results.get(i).getJustifications().size());

                KiWiTriple justification = results.get(i).getJustifications().iterator().next();
                Assert.assertEquals(predicate, justification.getPredicate());
            }

            connection.commit();
        } finally {
            connection.close();
        }
    }

    /**
     * Test a conjunction of two patterns with variables and constants
     */
    @Test
    public void testMultipleVariablePattern() throws Exception {
        ValueFactory v = repository.getValueFactory();
        URI predicate1 = v.createURI(PREDICATE1);
        URI predicate2 = v.createURI(PREDICATE2);

        VariableField x =  new VariableField("X");
        VariableField y =  new VariableField("Y");
        VariableField z =  new VariableField("Z");

        Pattern p1 = new Pattern(x, new ResourceField(predicate1), y);
        Pattern p2 = new Pattern(y, new ResourceField(predicate1), z);
        KiWiReasoningConnection connection = rpersistence.getConnection();
        try {
            List<QueryResult> results = asList(connection.query(ImmutableSet.of(p1,p2), null, null, null, true));
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(2, results.get(0).getJustifications().size());


            Assert.assertEquals(SUBJECT1, results.get(0).getBindings().get(x).stringValue());
            Assert.assertEquals(SUBJECT2, results.get(0).getBindings().get(y).stringValue());
            Assert.assertEquals(OBJECT2, results.get(0).getBindings().get(z).stringValue());

            KiWiTriple justification1 = results.get(0).getJustifications().iterator().next();
            Assert.assertEquals(predicate1, justification1.getPredicate());

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
