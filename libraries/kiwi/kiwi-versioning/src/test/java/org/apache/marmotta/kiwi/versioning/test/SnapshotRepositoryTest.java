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
package org.apache.marmotta.kiwi.versioning.test;

import info.aduna.iteration.Iterations;
import org.apache.marmotta.commons.sesame.transactions.sail.KiWiTransactionalSail;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.apache.marmotta.kiwi.versioning.repository.SnapshotRepository;
import org.apache.marmotta.kiwi.versioning.sail.KiWiVersioningSail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Statement;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

/**
 * This test verifies the snapshot functionality, i.e. if the snapshot connection works properly. 
 *
 * @see org.apache.marmotta.kiwi.versioning.repository.SnapshotRepositoryConnection
 * @see org.apache.marmotta.kiwi.versioning.repository.SnapshotRepository
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class SnapshotRepositoryTest {


    private KiWiStore store;

    private KiWiTransactionalSail tsail;

    private KiWiVersioningSail vsail;

    private SnapshotRepository repository;

    private final KiWiConfiguration dbConfig;

    public SnapshotRepositoryTest(KiWiConfiguration dbConfig) {
        this.dbConfig = dbConfig;
    }


    @Before
    public void initDatabase() throws RepositoryException {
        store = new KiWiStore(dbConfig);
        tsail = new KiWiTransactionalSail(store);
        vsail = new KiWiVersioningSail(tsail);
        repository = new SnapshotRepository(vsail);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
        vsail.getPersistence().dropDatabase();
        store.getPersistence().dropDatabase();
        repository.shutDown();
    }

    final Logger logger =
            LoggerFactory.getLogger(this.getClass());


    @Test
    public void testSnapshotConnection() throws Exception {

        // doesn't work for H2 because of MERGE statement updating the timestamp when a triple is re-inserted
        if(dbConfig.getDialect() instanceof H2Dialect) {
            return;
        }

        // import three files in sequence and check if the versions are created properly

        Date date1 = new Date();

        Thread.sleep(1000);

        // base data
        InputStream baseData = this.getClass().getResourceAsStream("version-base.rdf");
        assumeThat("Could not load test-data: version-base.rdf", baseData, notNullValue(InputStream.class));

        RepositoryConnection connectionBase = repository.getConnection();
        try {
            connectionBase.add(baseData, "http://marmotta.apache.org/testing/ns1/", RDFFormat.RDFXML);
            connectionBase.commit();
        } finally {
            connectionBase.close();
        }

        Thread.sleep(1000);

        Date date2 = new Date();

        Thread.sleep(1000);

        // update 1
        InputStream update1Data = this.getClass().getResourceAsStream("version-update1.rdf");
        assumeThat("Could not load test-data: version-update1.rdf", update1Data, notNullValue(InputStream.class));

        RepositoryConnection connectionUpdate1 = repository.getConnection();
        try {
            connectionUpdate1.add(update1Data, "http://marmotta.apache.org/testing/ns1/", RDFFormat.RDFXML);
            connectionUpdate1.commit();
        } finally {
            connectionUpdate1.close();
        }

        Thread.sleep(1000);

        Date date3 = new Date();

        Thread.sleep(1000);


        // update 2
        InputStream update2Data = this.getClass().getResourceAsStream("version-update2.rdf");
        assumeThat("Could not load test-data: version-update2.rdf", update2Data, notNullValue(InputStream.class));

        RepositoryConnection connectionUpdate2 = repository.getConnection();
        try {
            connectionUpdate2.add(update2Data, "http://marmotta.apache.org/testing/ns1/", RDFFormat.RDFXML);
            connectionUpdate2.commit();
        } finally {
            connectionUpdate2.close();
        }


        RepositoryConnection connectionCheck = repository.getConnection();
        try {
            List<Statement> c_r1_triples = asList(connectionCheck.getStatements(repository.getValueFactory().createURI("http://marmotta.apache.org/testing/ns1/R1"), null, null, true));
            Assert.assertEquals(4, c_r1_triples.size());  // type + 3 properties
            connectionCheck.commit();
        } finally {
            connectionCheck.close();
        }



        // test snapshot connection for date2 (i.e. after base import and before updates)
        RepositoryConnection snapshot1 = repository.getSnapshot(date2);
        try {
            // query all triples for http://marmotta.apache.org/testing/ns1/R1, should be exactly 3
            List<Statement> s1_r1_triples = asList(snapshot1.getStatements(repository.getValueFactory().createURI("http://marmotta.apache.org/testing/ns1/R1"), null, null, true));
            Assert.assertEquals(3, s1_r1_triples.size());

            // query all triples for http://marmotta.apache.org/testing/ns1/R2, should be zero
            List<Statement> s1_r2_triples = asList(snapshot1.getStatements(repository.getValueFactory().createURI("http://marmotta.apache.org/testing/ns1/R2"), null, null, true));
            Assert.assertEquals(0, s1_r2_triples.size());
        } finally {
            snapshot1.commit();
            snapshot1.close();
        }

        // test snapshot connection for date3 (i.e. after first update)
        RepositoryConnection snapshot2 = repository.getSnapshot(date3);
        try {

            // query all triples for http://marmotta.apache.org/testing/ns1/R1, should be exactly 4
            List<Statement> s2_r1_triples = asList(snapshot2.getStatements(repository.getValueFactory().createURI("http://marmotta.apache.org/testing/ns1/R1"), null, null, true));
            Assert.assertEquals(3, s2_r1_triples.size());

            // query all triples for http://marmotta.apache.org/testing/ns1/R2, should be 3
            List<Statement> s2_r2_triples = asList(snapshot2.getStatements(repository.getValueFactory().createURI("http://marmotta.apache.org/testing/ns1/R2"), null, null, true));
            Assert.assertEquals(3, s2_r2_triples.size());
        } finally {
            snapshot2.commit();
            snapshot2.close();
        }

        // test snapshot connection for now (i.e. after both updates)
        RepositoryConnection snapshot3 = repository.getSnapshot(new Date());
        try {
            // query all triples for http://marmotta.apache.org/testing/ns1/R1, should be exactly 4
            List<Statement> s3_r1_triples = asList(snapshot3.getStatements(repository.getValueFactory().createURI("http://marmotta.apache.org/testing/ns1/R1"), null, null, true));
            Assert.assertEquals(4, s3_r1_triples.size());

            // query all triples for http://marmotta.apache.org/testing/ns1/R2, should be 3
            List<Statement> s3_r2_triples = asList(snapshot3.getStatements(repository.getValueFactory().createURI("http://marmotta.apache.org/testing/ns1/R2"), null, null, true));
            Assert.assertEquals(3, s3_r2_triples.size());
        } finally {
            snapshot3.commit();
            snapshot3.close();
        }

    }

    @Test
    public void testSnapshotSPARQL() throws Exception {
        // import three files in sequence and check if the versions are created properly

        Date date1 = new Date();

        Thread.sleep(1000);

        // base data
        InputStream baseData = this.getClass().getResourceAsStream("version-base.rdf");
        assumeThat("Could not load test-data: version-base.rdf", baseData, notNullValue(InputStream.class));

        RepositoryConnection connectionBase = repository.getConnection();
        try {
            connectionBase.add(baseData, "http://marmotta.apache.org/testing/ns1/", RDFFormat.RDFXML);
            connectionBase.commit();
        } finally {
            connectionBase.close();
        }

        Thread.sleep(1000);

        Date date2 = new Date();

        Thread.sleep(1000);

        // update 1
        InputStream update1Data = this.getClass().getResourceAsStream("version-update1.rdf");
        assumeThat("Could not load test-data: version-update1.rdf", update1Data, notNullValue(InputStream.class));

        RepositoryConnection connectionUpdate1 = repository.getConnection();
        try {
            connectionUpdate1.add(update1Data, "http://marmotta.apache.org/testing/ns1/", RDFFormat.RDFXML);
            connectionUpdate1.commit();
        } finally {
            connectionUpdate1.close();
        }

        Thread.sleep(1000);

        Date date3 = new Date();

        Thread.sleep(1000);


        // update 2
        InputStream update2Data = this.getClass().getResourceAsStream("version-update2.rdf");
        assumeThat("Could not load test-data: version-update2.rdf", update2Data, notNullValue(InputStream.class));

        RepositoryConnection connectionUpdate2 = repository.getConnection();
        try {
            connectionUpdate2.add(update2Data, "http://marmotta.apache.org/testing/ns1/", RDFFormat.RDFXML);
            connectionUpdate2.commit();
        } finally {
            connectionUpdate2.close();
        }


        // test snapshot connection for date2 (i.e. after base import and before updates)
        RepositoryConnection snapshot1 = repository.getSnapshot(date2);

        // query all triples for http://marmotta.apache.org/testing/ns1/R1, should be exactly 3
        BooleanQuery query1_1 = snapshot1.prepareBooleanQuery(QueryLanguage.SPARQL, "ASK { <http://marmotta.apache.org/testing/ns1/R1> ?p ?o }");
        Assert.assertTrue("SPARQL query for R1 did not return true", query1_1.evaluate());

        // query all triples for http://marmotta.apache.org/testing/ns1/R2, should be zero
        BooleanQuery query1_2 = snapshot1.prepareBooleanQuery(QueryLanguage.SPARQL, "ASK { <http://marmotta.apache.org/testing/ns1/R2> ?p ?o }");
        Assert.assertFalse("SPARQL query for R2 did not return false", query1_2.evaluate());

        snapshot1.commit();
        snapshot1.close();

        // test snapshot connection for date3 (i.e. after first update)
        RepositoryConnection snapshot2 = repository.getSnapshot(date3);

        // query all triples for http://marmotta.apache.org/testing/ns1/R1, should be exactly 3
        BooleanQuery query2_1 = snapshot2.prepareBooleanQuery(QueryLanguage.SPARQL, "ASK { <http://marmotta.apache.org/testing/ns1/R1> ?p ?o }");
        Assert.assertTrue("SPARQL query for R1 did not return true", query2_1.evaluate());

        // query all triples for http://marmotta.apache.org/testing/ns1/R2, should be 3
        BooleanQuery query2_2 = snapshot2.prepareBooleanQuery(QueryLanguage.SPARQL, "ASK { <http://marmotta.apache.org/testing/ns1/R2> ?p ?o }");
        Assert.assertTrue("SPARQL query for R2 did not return true", query2_2.evaluate());

        snapshot2.commit();
        snapshot2.close();


    }


    /**
     * Workaround for https://openrdf.atlassian.net/browse/SES-1702 in Sesame 2.7.0-beta1
     * @param <E>
     * @return
     */
    public static <E> List<E> asList(RepositoryResult<E> result) throws RepositoryException {
        return Iterations.asList(result);
    }
}
