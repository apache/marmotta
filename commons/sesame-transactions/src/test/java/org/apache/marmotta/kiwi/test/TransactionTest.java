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
package org.apache.marmotta.kiwi.test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.commons.sesame.transactions.api.TransactionListener;
import org.apache.marmotta.commons.sesame.transactions.model.TransactionData;
import org.apache.marmotta.commons.sesame.transactions.sail.KiWiTransactionalSail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Test the Sesame repository functionality backed by the KiWi triple store. 
 * 
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class TransactionTest {
    private static Logger log = LoggerFactory.getLogger(TransactionTest.class);

    private Repository repository;

    private NotifyingSail store;

    private KiWiTransactionalSail tstore;

    private MockListener listener;

    @Before
    public void initDatabase() throws RepositoryException {
        store = new MemoryStore();
        tstore = new KiWiTransactionalSail(store);
        listener = new MockListener();
        tstore.addTransactionListener(listener);
        repository = new SailRepository(tstore);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException {
        repository.shutDown();
    }


    /**
     * Test importing data; the test will load a small sample RDF file and check whether the expected resources are
     * present.
     *
     * @throws RepositoryException
     * @throws org.openrdf.rio.RDFParseException
     * @throws java.io.IOException
     */
    @Test
    public void testImport() throws RepositoryException, RDFParseException, IOException {
        long start, end;

        start = System.currentTimeMillis();
        // load demo data
        InputStream rdfXML = this.getClass().getResourceAsStream("demo-data.foaf");
        assumeThat("Could not load test-data: demo-data.foaf", rdfXML, notNullValue(InputStream.class));

        RepositoryConnection connectionRDF = repository.getConnection();
        try {
            connectionRDF.begin();
            connectionRDF.add(rdfXML, "http://localhost/foaf/", RDFFormat.RDFXML);
            connectionRDF.commit();
        } finally {
            connectionRDF.close();
        }
        end = System.currentTimeMillis();

        log.info("IMPORT: {} ms", end-start);


        // check if the transaction data is available and contains added triples
        Assert.assertNotNull("transaction data was null",listener.transactionData);
        Assert.assertTrue("transaction data did not contain added triples", listener.transactionData.getAddedTriples().size() > 0);
    }


    @Test
    public void testDeleteTriple() throws RepositoryException, RDFParseException, IOException {
        // load demo data
        InputStream rdfXML = this.getClass().getResourceAsStream("demo-data.foaf");
        assumeThat("Could not load test-data: demo-data.foaf", rdfXML, notNullValue(InputStream.class));

        RepositoryConnection connectionRDF = repository.getConnection();
        try {
            connectionRDF.begin();
            connectionRDF.add(rdfXML, "http://localhost/foaf/", RDFFormat.RDFXML);
            connectionRDF.commit();
        } finally {
            connectionRDF.close();
        }
        // get another connection and check if demo data is available
        RepositoryConnection connection = repository.getConnection();

        try {
            connection.begin();
            List<String> resources = ImmutableList.copyOf(
                    Iterables.transform(
                            ResourceUtils.listSubjects(connection),
                            new Function<Resource, String>() {
                                @Override
                                public String apply(Resource input) {
                                    return input.stringValue();
                                }
                            }
                    )
            );

            // test if the result has the expected size
            Assert.assertEquals(4, resources.size());

            // test if the result contains all resources that have been used as subject
            Assert.assertThat(resources, hasItems(
                    "http://localhost:8080/LMF/resource/hans_meier",
                    "http://localhost:8080/LMF/resource/sepp_huber",
                    "http://localhost:8080/LMF/resource/anna_schmidt"
            ));
            connection.commit();


            // remove a resource and all its triples
            connection.begin();
            ResourceUtils.removeResource(connection, connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier"));
            connection.commit();


            // check if transaction contains removed triples now
            Assert.assertNotNull("transaction data was null", listener.transactionData);
            Assert.assertTrue("transaction did not contain removed triples", listener.transactionData.getRemovedTriples().size() > 0);
        } finally {
            connection.commit();
            connection.close();
        }
    }


    /**
     * Mock implementation of a transaction listener
     */
    private static class MockListener implements TransactionListener {

        private TransactionData transactionData;

        boolean rolledBack = false;

        @Override
        public void afterCommit(TransactionData data) {
            transactionData = data;
        }

         @Override
        public void beforeCommit(TransactionData data) {
        }

        @Override
        public void rollback(TransactionData data) {
            rolledBack = true;
        }
    }
}
