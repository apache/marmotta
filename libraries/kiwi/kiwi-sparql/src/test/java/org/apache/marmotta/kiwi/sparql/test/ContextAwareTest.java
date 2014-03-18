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

import info.aduna.iteration.Iterations;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.sparql.sail.KiWiSparqlSail;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.contextaware.ContextAwareConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This test tests a complex situation where there is data in two contexts, and the query is carried out
 * over a context aware repository. In this case, it should only list triples from the restricted context.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class ContextAwareTest {

    private KiWiStore store;

    private KiWiSparqlSail ssail;

    private Repository repository;


    private URI context1, context2;

    private URI subject, object11, object21;

    private URI predicate1, predicate2;

    private final KiWiConfiguration dbConfig;

    public ContextAwareTest(KiWiConfiguration dbConfig) {
        this.dbConfig = dbConfig;
    }


    @Before
    public void initDatabase() throws RepositoryException, IOException, RDFParseException {
        store = new KiWiStore(dbConfig);
        ssail = new KiWiSparqlSail(store);
        repository = new SailRepository(ssail);
        repository.initialize();

        context1 = repository.getValueFactory().createURI("http://localhost/test/" + RandomStringUtils.randomAlphanumeric(8));
        context2 = repository.getValueFactory().createURI("http://localhost/test/" + RandomStringUtils.randomAlphanumeric(8));
        subject  = repository.getValueFactory().createURI("http://localhost/test/" + RandomStringUtils.randomAlphanumeric(8));
        object11 = repository.getValueFactory().createURI("http://localhost/test/" + RandomStringUtils.randomAlphanumeric(8));
        object21 = repository.getValueFactory().createURI("http://localhost/test/" + RandomStringUtils.randomAlphanumeric(8));

        predicate1 =  repository.getValueFactory().createURI("http://localhost/test/P1");
        predicate2 =  repository.getValueFactory().createURI("http://localhost/test/P1");

        Literal object12  = repository.getValueFactory().createLiteral("this object should be linked only in context1");
        Literal object22  = repository.getValueFactory().createLiteral("this object should be linked only in context2");

        // load demo data
        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();

            // create a graph in context1 as follows:
            // subject1 -- P1 --> object111 -- P1 --> object2
            con.add(subject,predicate1, object11,context1);
            con.add(object11,predicate1,object12,context1);

            // create a graph in context2 as follows (note different predicate)
            // subject1 -- P2 --> object121 -- P2 --> object3
            con.add(subject,predicate2, object21,context2);
            con.add(object21,predicate2,object22,context2);

            con.commit();
        } finally {
            con.close();
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

    @Test
    public void testSinglePattern() throws Exception {
        ContextAwareConnection con = new ContextAwareConnection(repository, repository.getConnection());
        try {
            con.setInsertContext(context1);
            con.setReadContexts(context1);
            con.setRemoveContexts(context1);

            Assert.assertTrue(con.hasStatement(subject,predicate1, object11));
            Assert.assertFalse(con.hasStatement(subject, predicate2, object21));

            String queryStr = "SELECT ?X ?Z WHERE { ?X ?Y ?Z }";

            TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
            List<BindingSet> result = Iterations.asList(query.evaluate());
            Assert.assertEquals(2,result.size());
            con.commit();
        } finally {
            con.close();
        }
    }

    @Test
    public void testJoinPattern() throws Exception {
        ContextAwareConnection con = new ContextAwareConnection(repository, repository.getConnection());
        try {
            con.setInsertContext(context1);
            con.setReadContexts(context1);
            con.setRemoveContexts(context1);

            Assert.assertTrue(con.hasStatement(subject,predicate1, object11));
            Assert.assertFalse(con.hasStatement(subject, predicate2, object21));

            String queryStr = "SELECT ?X ?Z WHERE { ?X ?P ?Y . ?Y ?P ?Z }";

            TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
            List<BindingSet> result = Iterations.asList(query.evaluate());
            Assert.assertEquals(1,result.size());
            con.commit();
        } finally {
            con.close();
        }
    }

    @Test
    public void testNamedGraphQuery() throws Exception {
        ContextAwareConnection con = new ContextAwareConnection(repository, repository.getConnection());
        try {
            con.setInsertContext(context1);
            con.setReadContexts(context1);
            con.setRemoveContexts(context1);

            Assert.assertTrue(con.hasStatement(subject,predicate1, object11));
            Assert.assertFalse(con.hasStatement(subject, predicate2, object21));

            String queryStr1 = "SELECT * WHERE { GRAPH <"+context1.stringValue()+"> { ?X ?P ?Y } }";

            TupleQuery query1 = con.prepareTupleQuery(QueryLanguage.SPARQL, queryStr1);

            // workaround for a bug in sesame
            DatasetImpl ds = new DatasetImpl();
            ds.addDefaultGraph(context1);
            ds.addNamedGraph(context1);
            ds.addDefaultRemoveGraph(context1);
            ds.setDefaultInsertGraph(context1);
            query1.setDataset(ds);


            List<BindingSet> result1 = Iterations.asList(query1.evaluate());
            Assert.assertEquals(2, result1.size());

            String queryStr2 = "SELECT * WHERE { GRAPH <"+context2.stringValue()+"> { ?X ?P ?Y  } }";

            TupleQuery query2 = con.prepareTupleQuery(QueryLanguage.SPARQL, queryStr2);
            List<BindingSet> result2 = Iterations.asList(query2.evaluate());
            Assert.assertEquals(0, result2.size());

            con.commit();
        } finally {
            con.close();
        }
    }

}
