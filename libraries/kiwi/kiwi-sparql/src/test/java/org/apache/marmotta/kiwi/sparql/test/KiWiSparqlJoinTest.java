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
 * Test the KiWi SPARQL Join optimization.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class KiWiSparqlJoinTest {

    private KiWiStore store;

    private KiWiSparqlSail ssail;

    private Repository repository;

    // reference repository for checking if the results are the same
    private Repository reference;

    private final KiWiConfiguration dbConfig;

    public KiWiSparqlJoinTest(KiWiConfiguration dbConfig) {
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

    // nested query
    @Test
    public void testQuery13() throws Exception {
        testQuery("query13.sparql");
    }

    // boolean filter
    @Test
    public void testQuery14() throws Exception {
        testQuery("query14.sparql");
    }

    // contains filter
    @Test
    public void testQuery15() throws Exception {
        testQuery("query15.sparql");
    }

    // strafter filter
    @Test
    public void testQuery16() throws Exception {
        testQuery("query16.sparql");
    }

    // strbefore filter
    @Test
    public void testQuery17() throws Exception {
        testQuery("query17.sparql");
    }

    // replace filter
    @Test
    public void testQuery18() throws Exception {
        testQuery("query18.sparql");
    }

    // strlen filter
    @Test
    public void testQuery19() throws Exception {
        testQuery("query19.sparql");
    }

    // strstarts filter
    @Test
    public void testQuery20() throws Exception {
        testQuery("query20.sparql");
    }

    // strends filter
    @Test
    public void testQuery21() throws Exception {
        testQuery("query21.sparql");
    }

    // order by
    @Test
    public void testQuery24() throws Exception {
        testQuery("query24.sparql");
    }

    // simple group by
    @Test
    public void testQuery25() throws Exception {
        testQuery("query25.sparql");
    }

    // bind/coalesce
    @Test
    public void testQuery27() throws Exception {
        testQuery("query27.sparql");
    }

    // simple union
    @Test
    public void testQuery26() throws Exception {
        testQuery("query26.sparql");
    }

    // union with bind
    @Test
    public void testQuery28() throws Exception {
        testQuery("query28.sparql");
    }

    // union with bind and order by
    @Test
    public void testQuery29() throws Exception {
        testQuery("query29.sparql");
    }

    // bind with order by
    @Test
    public void testQuery30() throws Exception {
        testQuery("query30.sparql");
    }

    // subquery with outer order by
    @Test
    public void testQuery31() throws Exception {
        testQuery("query31.sparql");
    }

    // minus
    @Test
    public void testQuery32() throws Exception {
        testQuery("query32.sparql");
    }

    // not exists
    @Test
    public void testQuery33() throws Exception {
        testQuery("query33.sparql");
    }

    // MARMOTTA-546
    @Test
    public void testQuery34() throws Exception {
        testQuery("query34.sparql");
    }

    // Redlink API-463
    @Test
    public void testQuery35() throws Exception {
        testQuery("query35.sparql");
    }

    // MARMOTTA-552
    @Test
    @Ignore("test skipped because of wrong evaluation in Sesame")
    public void testQuery36() throws Exception {
        testQuery("query36.sparql");
    }

    // INSERT/UPDATE
    @Test
    public void testUpdate01() throws Exception {
        testUpdate("update01.sparql", FOAF.name);
    }

    // fulltext search filter
    @Test
    public void testQuery22() throws Exception {
        Assume.assumeTrue(NativeFunctionRegistry.getInstance().get(FN_MARMOTTA.SEARCH_FULLTEXT).isSupported(dbConfig.getDialect()));
        String queryString = IOUtils.toString(this.getClass().getResourceAsStream("query22.sparql"), "UTF-8");

        RepositoryConnection con1 = repository.getConnection();
        try {
            con1.begin();

            TupleQuery query1 = con1.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result1 = query1.evaluate();

            con1.commit();

            Assert.assertTrue(result1.hasNext());

            BindingSet next = result1.next();

            Assert.assertEquals("http://localhost:8080/LMF/resource/hans_meier", next.getValue("p1").stringValue());

        } catch(RepositoryException ex) {
            con1.rollback();
        } finally {
            con1.close();
        }

    }

    // fulltext query filter
    @Test
    public void testQuery23() throws Exception {
        Assume.assumeTrue(NativeFunctionRegistry.getInstance().get(FN_MARMOTTA.SEARCH_FULLTEXT).isSupported(dbConfig.getDialect()));
        String queryString = IOUtils.toString(this.getClass().getResourceAsStream("query23.sparql"), "UTF-8");

        RepositoryConnection con1 = repository.getConnection();
        try {
            con1.begin();

            TupleQuery query1 = con1.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result1 = query1.evaluate();

            con1.commit();

            Assert.assertTrue(result1.hasNext());

            while (result1.hasNext()) {
                BindingSet next = result1.next();
                Assert.assertThat(next.getValue("p1").stringValue(), Matchers.isOneOf("http://localhost:8080/LMF/resource/hans_meier", "http://localhost:8080/LMF/resource/sepp_huber"));
            }

        } catch(RepositoryException ex) {
            con1.rollback();
        } finally {
            con1.close();
        }

    }

    private void testQuery(String filename) throws Exception {
        String queryString = IOUtils.toString(this.getClass().getResourceAsStream(filename), "UTF-8");

        RepositoryConnection con1 = repository.getConnection();
        RepositoryConnection con2 = reference.getConnection();
        try {
            con2.begin();

            TupleQuery query2 = con2.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result2 = query2.evaluate();

            con2.commit();

            Assume.assumeTrue(result2.hasNext());

            con1.begin();

            TupleQuery query1 = con1.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result1 = query1.evaluate();

            con1.commit();

            Assert.assertTrue(result1.hasNext());


            compareResults(result1, result2);

        } catch(RepositoryException ex) {
            con1.rollback();
        } finally {
            con1.close();
            con2.close();
        }
    }

    private void testUpdate(String filename, URI... properties) throws Exception {
        String queryString = IOUtils.toString(this.getClass().getResourceAsStream(filename), "UTF-8");

        RepositoryConnection con1 = repository.getConnection();
        RepositoryConnection con2 = reference.getConnection();
        try {
            con2.begin();

            Update query2 = con2.prepareUpdate(QueryLanguage.SPARQL, queryString);
            query2.execute();

            con2.commit();

            con1.begin();

            Update query1 = con1.prepareUpdate(QueryLanguage.SPARQL, queryString);
            // workaround for a Sesame bug: we explicitly set the context for the query in the dataset

            URI context = new URIImpl("http://localhost/mycontext");
            DatasetImpl ds = new DatasetImpl();
            //ds.addDefaultGraph(context);
            //ds.addNamedGraph(context);
            //ds.addDefaultRemoveGraph(context);
            ds.setDefaultInsertGraph(context);
            query1.setDataset(ds);

            query1.execute();

            con1.commit();


            con1.begin();
            Set<Statement> set1 = new HashSet<>();
            for(URI u : properties) {
                set1.addAll(Collections2.transform(Iterations.asSet(con1.getStatements(null, u, null, true)), new StatementCommons.TripleEquality()));
            }
            con1.commit();

            con2.begin();
            Set<Statement> set2 = new HashSet<>();
            for(URI u : properties) {
                set2.addAll(Collections2.transform(Iterations.asSet(con2.getStatements(null,u,null,true)), new StatementCommons.TripleEquality()));
            }
            con2.commit();

            for(Statement stmt : set1) {
                Assert.assertTrue(stmt + " not contained in set 2", set2.contains(stmt));
            }
            for(Statement stmt : set2) {
                Assert.assertTrue(stmt + " not contained in set 1", set1.contains(stmt));
            }

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
