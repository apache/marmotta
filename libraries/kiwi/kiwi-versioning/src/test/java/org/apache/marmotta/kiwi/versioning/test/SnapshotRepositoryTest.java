/*
 * Copyright (c) 2013 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.marmotta.kiwi.versioning.test;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.apache.marmotta.kiwi.transactions.sail.KiWiTransactionalSail;
import org.apache.marmotta.kiwi.versioning.repository.SnapshotRepository;
import org.apache.marmotta.kiwi.versioning.sail.KiWiVersioningSail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.model.Statement;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

/**
 * This test verifies the snapshot functionality, i.e. if the snapshot connection works properly. It will try running
 * over all available databases. Except for in-memory databases like
 * H2 or Derby, database URLs must be passed as system property, or otherwise the test is skipped for this database.
 * Available system properties:
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
 * <p/>
 * @see org.apache.marmotta.kiwi.versioning.repository.SnapshotRepositoryConnection
 * @see org.apache.marmotta.kiwi.versioning.repository.SnapshotRepository
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(Parameterized.class)
public class SnapshotRepositoryTest {

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

    private KiWiTransactionalSail tsail;

    private KiWiVersioningSail vsail;

    private SnapshotRepository repository;

    public SnapshotRepositoryTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
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
    public void initDatabase() throws RepositoryException {
        store = new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred");
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


    @Test
    public void testSnapshotConnection() throws Exception {
        // import three files in sequence and check if the versions are created properly

        Date date1 = new Date();

        Thread.sleep(1000);

        // base data
        InputStream baseData = this.getClass().getResourceAsStream("version-base.rdf");
        assumeThat("Could not load test-data: version-base.rdf", baseData, notNullValue(InputStream.class));

        RepositoryConnection connectionBase = repository.getConnection();
        try {
            connectionBase.add(baseData, "http://marmotta.incubator.apache.org/testing/ns1/", RDFFormat.RDFXML);
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
            connectionUpdate1.add(update1Data, "http://marmotta.incubator.apache.org/testing/ns1/", RDFFormat.RDFXML);
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
            connectionUpdate2.add(update2Data, "http://marmotta.incubator.apache.org/testing/ns1/", RDFFormat.RDFXML);
            connectionUpdate2.commit();
        } finally {
            connectionUpdate2.close();
        }


        // test snapshot connection for date2 (i.e. after base import and before updates)
        RepositoryConnection snapshot1 = repository.getSnapshot(date2);

        // query all triples for http://marmotta.incubator.apache.org/testing/ns1/R1, should be exactly 3
        List<Statement> s1_r1_triples = asList(snapshot1.getStatements(repository.getValueFactory().createURI("http://marmotta.incubator.apache.org/testing/ns1/R1"), null, null, true));
        Assert.assertEquals(3, s1_r1_triples.size());

        // query all triples for http://marmotta.incubator.apache.org/testing/ns1/R2, should be zero
        List<Statement> s1_r2_triples = asList(snapshot1.getStatements(repository.getValueFactory().createURI("http://marmotta.incubator.apache.org/testing/ns1/R2"), null, null, true));
        Assert.assertEquals(0, s1_r2_triples.size());

        snapshot1.commit();
        snapshot1.close();

        // test snapshot connection for date3 (i.e. after first update)
        RepositoryConnection snapshot2 = repository.getSnapshot(date3);

        // query all triples for http://marmotta.incubator.apache.org/testing/ns1/R1, should be exactly 4
        List<Statement> s2_r1_triples = asList(snapshot2.getStatements(repository.getValueFactory().createURI("http://marmotta.incubator.apache.org/testing/ns1/R1"), null, null, true));
        Assert.assertEquals(3, s2_r1_triples.size());

        // query all triples for http://marmotta.incubator.apache.org/testing/ns1/R2, should be 3
        List<Statement> s2_r2_triples = asList(snapshot2.getStatements(repository.getValueFactory().createURI("http://marmotta.incubator.apache.org/testing/ns1/R2"), null, null, true));
        Assert.assertEquals(3, s2_r2_triples.size());

        snapshot2.commit();
        snapshot2.close();


        // test snapshot connection for now (i.e. after both updates)
        RepositoryConnection snapshot3 = repository.getSnapshot(new Date());

        // query all triples for http://marmotta.incubator.apache.org/testing/ns1/R1, should be exactly 4
        List<Statement> s3_r1_triples = asList(snapshot3.getStatements(repository.getValueFactory().createURI("http://marmotta.incubator.apache.org/testing/ns1/R1"), null, null, true));
        Assert.assertEquals(4, s3_r1_triples.size());

        // query all triples for http://marmotta.incubator.apache.org/testing/ns1/R2, should be 3
        List<Statement> s3_r2_triples = asList(snapshot3.getStatements(repository.getValueFactory().createURI("http://marmotta.incubator.apache.org/testing/ns1/R2"), null, null, true));
        Assert.assertEquals(3, s3_r2_triples.size());

        snapshot3.commit();
        snapshot3.close();

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
            connectionBase.add(baseData, "http://marmotta.incubator.apache.org/testing/ns1/", RDFFormat.RDFXML);
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
            connectionUpdate1.add(update1Data, "http://marmotta.incubator.apache.org/testing/ns1/", RDFFormat.RDFXML);
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
            connectionUpdate2.add(update2Data, "http://marmotta.incubator.apache.org/testing/ns1/", RDFFormat.RDFXML);
            connectionUpdate2.commit();
        } finally {
            connectionUpdate2.close();
        }


        // test snapshot connection for date2 (i.e. after base import and before updates)
        RepositoryConnection snapshot1 = repository.getSnapshot(date2);

        // query all triples for http://marmotta.incubator.apache.org/testing/ns1/R1, should be exactly 3
        BooleanQuery query1_1 = snapshot1.prepareBooleanQuery(QueryLanguage.SPARQL, "ASK { <http://marmotta.incubator.apache.org/testing/ns1/R1> ?p ?o }");
        Assert.assertTrue("SPARQL query for R1 did not return true", query1_1.evaluate());

        // query all triples for http://marmotta.incubator.apache.org/testing/ns1/R2, should be zero
        BooleanQuery query1_2 = snapshot1.prepareBooleanQuery(QueryLanguage.SPARQL, "ASK { <http://marmotta.incubator.apache.org/testing/ns1/R2> ?p ?o }");
        Assert.assertFalse("SPARQL query for R2 did not return false", query1_2.evaluate());

        snapshot1.commit();
        snapshot1.close();

        // test snapshot connection for date3 (i.e. after first update)
        RepositoryConnection snapshot2 = repository.getSnapshot(date3);

        // query all triples for http://marmotta.incubator.apache.org/testing/ns1/R1, should be exactly 3
        BooleanQuery query2_1 = snapshot2.prepareBooleanQuery(QueryLanguage.SPARQL, "ASK { <http://marmotta.incubator.apache.org/testing/ns1/R1> ?p ?o }");
        Assert.assertTrue("SPARQL query for R1 did not return true", query2_1.evaluate());

        // query all triples for http://marmotta.incubator.apache.org/testing/ns1/R2, should be 3
        BooleanQuery query2_2 = snapshot2.prepareBooleanQuery(QueryLanguage.SPARQL, "ASK { <http://marmotta.incubator.apache.org/testing/ns1/R2> ?p ?o }");
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
        ArrayList<E> collection = new ArrayList<E>();
        try {
            while (result.hasNext()) {
                collection.add(result.next());
            }

            return collection;
        }
        finally {
            result.close();
        }
    }

}
