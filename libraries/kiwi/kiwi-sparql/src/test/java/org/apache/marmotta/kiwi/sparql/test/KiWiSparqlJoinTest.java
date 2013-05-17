/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.kiwi.sparql.test;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import info.aduna.iteration.Iterations;
import org.apache.commons.io.IOUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.sparql.sail.KiWiSparqlSail;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test the KiWi SPARQL Join optimization.
 *
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
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(Parameterized.class)
public class KiWiSparqlJoinTest {

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

    private KiWiSparqlSail ssail;

    private Repository repository;

    // reference repository for checking if the results are the same
    private Repository reference;

    public KiWiSparqlJoinTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
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
    public void initDatabase() throws RepositoryException, IOException, RDFParseException {
        store = new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred");
        ssail = new KiWiSparqlSail(store);
        repository = new SailRepository(ssail);
        repository.initialize();

        // load demo data
        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();

            con.add(this.getClass().getResourceAsStream("demo-data.foaf"), "http://localhost/test/", RDFFormat.RDFXML);

            con.commit();
        } finally {
            con.close();
        }

        reference = new SailRepository(new MemoryStore());
        reference.initialize();

        // load demo data
        RepositoryConnection con2 = reference.getConnection();
        try {
            con2.begin();

            con2.add(this.getClass().getResourceAsStream("demo-data.foaf"), "http://localhost/test/", RDFFormat.RDFXML);

            con2.commit();
        } finally {
            con2.close();
        }
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
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
     * This method tests a simple triple join with two triple patterns.
     * @throws Exception
     */
    @Test
    public void testQuery1() throws Exception {
        testQuery("query1.sparql");
    }

    @Test
    public void testQuery2() throws Exception {
        testQuery("query2.sparql");
    }

    @Test
    public void testQuery3() throws Exception {
        testQuery("query3.sparql");
    }

    @Test
    public void testQuery4() throws Exception {
        testQuery("query4.sparql");
    }

    // numeric comparison
    @Test
    public void testQuery5() throws Exception {
        testQuery("query5.sparql");
    }

    // language match
    @Test
    public void testQuery6() throws Exception {
        testQuery("query6.sparql");
    }

    // math expression
    @Test
    public void testQuery7() throws Exception {
        testQuery("query7.sparql");
    }

    // isLiteral
    @Test
    public void testQuery8() throws Exception {
        testQuery("query8.sparql");
    }

    // isURI
    @Test
    public void testQuery9() throws Exception {
        testQuery("query9.sparql");
    }

    // term comparison
    @Test
    public void testQuery10() throws Exception {
        testQuery("query10.sparql");
    }

    // optional
    @Test
    public void testQuery11() throws Exception {
        testQuery("query11.sparql");
    }

    // optional with join
    @Test
    public void testQuery12() throws Exception {
        testQuery("query12.sparql");
    }


    private void testQuery(String filename) throws Exception {
        String queryString = IOUtils.toString(this.getClass().getResourceAsStream(filename), "UTF-8");

        RepositoryConnection con1 = repository.getConnection();
        RepositoryConnection con2 = reference.getConnection();
        try {
            con1.begin();

            TupleQuery query1 = con1.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result1 = query1.evaluate();

            con1.commit();

            Assert.assertTrue(result1.hasNext());


            con2.begin();

            TupleQuery query2 = con2.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result2 = query2.evaluate();

            con2.commit();

            compareResults(result1,result2);

        } catch(RepositoryException ex) {
            con1.rollback();
        } finally {
            con1.close();
            con2.close();
        }
    }


    private void compareResults(TupleQueryResult result1, TupleQueryResult result2) throws QueryEvaluationException {
        List<BindingSet> bindingSets1 = Iterations.asList(result1);
        List<BindingSet> bindingSets2 = Iterations.asList(result2);

        Set<Set<Pair>> set1 = new HashSet<Set<Pair>>(Lists.transform(bindingSets1,new BindingSetPairFunction()));
        Set<Set<Pair>> set2 = new HashSet<Set<Pair>>(Lists.transform(bindingSets2,new BindingSetPairFunction()));

        Assert.assertTrue(CollectionUtils.isEqualCollection(set1, set2));
    }


    private static class BindingSetPairFunction implements Function<BindingSet, Set<Pair>> {
        @Override
        public Set<Pair> apply(BindingSet input) {
            Set<Pair> result = new HashSet<Pair>();

            for(Binding b : input) {
                Pair p = new Pair(b.getName(), b.getValue() != null ? b.getValue().stringValue() : null);
                result.add(p);
            }

            return result;
        }
    }

    private static class Pair {
        String key, value;

        private Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        private String getKey() {
            return key;
        }

        private String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair pair = (Pair) o;

            if (!key.equals(pair.key)) return false;
            if (value != null ? !value.equals(pair.value) : pair.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }


}
