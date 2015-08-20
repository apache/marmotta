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
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import info.aduna.iteration.Iterations;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.commons.sesame.model.StatementCommons;
import org.apache.marmotta.commons.vocabulary.FOAF;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.sparql.function.NativeFunctionRegistry;
import org.apache.marmotta.kiwi.sparql.sail.KiWiSparqlSail;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.apache.marmotta.kiwi.vocabulary.FN_MARMOTTA;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.query.impl.DatasetImpl;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test the KiWi SPARQL
 *
 * @author Sergio Fernámdez
 */
@RunWith(KiWiDatabaseRunner.class)
public class KiWiSparqlTest {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    private KiWiStore store;

    private KiWiSparqlSail ssail;

    private Repository repository;

    private Repository reference;

    private final KiWiConfiguration dbConfig;

    public KiWiSparqlTest(KiWiConfiguration dbConfig) {
        this.dbConfig = dbConfig;
        dbConfig.setFulltextEnabled(true);
        dbConfig.setFulltextLanguages(new String[] {"en"});
    }

    @Before
    public void initDatabase() throws RepositoryException, IOException, RDFParseException {
        store = new KiWiStore(dbConfig);
        ssail = new KiWiSparqlSail(store);
        repository = new SailRepository(ssail);
        repository.initialize();

        // load demo data
        RepositoryConnection conn = repository.getConnection();
        try {
            conn.begin();
            conn.add(this.getClass().getResourceAsStream("demo-data.foaf"), "http://localhost/test/", RDFFormat.RDFXML);
            conn.commit();
        } finally {
            conn.close();
        }

        reference = new SailRepository(new MemoryStore());
        reference.initialize();

        // load demo data again
        RepositoryConnection conn2 = reference.getConnection();
        try {
            conn2.begin();
            conn2.add(this.getClass().getResourceAsStream("demo-data.foaf"), "http://localhost/test/", RDFFormat.RDFXML);
            conn2.commit();
        } finally {
            conn2.close();
        }
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
        store.getPersistence().dropDatabase();
        repository.shutDown();
    }

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        /**
         * Invoked when a test is about to start
         */
        @Override
        protected void starting(Description description) {
        log.info("{} being run...", description.getMethodName());
        }
    };

    /**
     * This method tests a simple triple join with two triple patterns.
     * @throws Exception
     */
    @Test
    public void testQuery37() throws Exception {
        testQuery("MARMOTTA-578.sparql");
    }

    //TODO: generalize this infrastructure code also used by KiWiSparqlJoinTest

    private void testQuery(String filename) throws Exception {
        String queryString = IOUtils.toString(this.getClass().getResourceAsStream(filename), "UTF-8");

        RepositoryConnection conn1 = repository.getConnection();
        RepositoryConnection conn2 = reference.getConnection();
        try {
            conn2.begin();

            TupleQuery query2 = conn2.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result2 = query2.evaluate();

            conn2.commit();

            Assume.assumeTrue(result2.hasNext());

            conn1.begin();

            TupleQuery query1 = conn1.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result1 = query1.evaluate();

            conn1.commit();

            Assert.assertTrue(result1.hasNext());

            compareResults(result1, result2);

        } catch(RepositoryException ex) {
            conn1.rollback();
        } finally {
            conn1.close();
            conn2.close();
        }
    }

    private void compareResults(TupleQueryResult result1, TupleQueryResult result2) throws QueryEvaluationException {
        List<BindingSet> bindingSets1 = Iterations.asList(result1);
        List<BindingSet> bindingSets2 = Iterations.asList(result2);

        Set<Set<Pair>> set1 = new HashSet<Set<Pair>>(Lists.transform(bindingSets1,new BindingSetPairFunction()));
        Set<Set<Pair>> set2 = new HashSet<Set<Pair>>(Lists.transform(bindingSets2,new BindingSetPairFunction()));

        for(Set<Pair> p : set1) {
            Assert.assertTrue("binding " + p + " from result set not found in reference set", set2.contains(p));
        }
        for(Set<Pair> p : set2) {
            Assert.assertTrue("binding " + p + " from reference set not found in result set", set1.contains(p));
        }


        Assert.assertTrue(CollectionUtils.isEqualCollection(set1, set2));
    }

    private static class BindingSetPairFunction implements Function<BindingSet, Set<Pair>> {
        @Override
        public Set<Pair> apply(BindingSet input) {
            Set<Pair> result = new HashSet<Pair>();

            for(Binding b : input) {
                Pair p = new Pair(b.getName(), b.getValue() != null ? (b.getValue() instanceof BNode ? "_" : b.getValue().stringValue()) : null);
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

            if (!key.equals(pair.getKey())) return false;
            if (value != null ? !value.equals(pair.getValue()) : pair.getValue() != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return key + " = " + value;
        }

    }

}
