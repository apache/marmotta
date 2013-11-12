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
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.kiwi.versioning.sail.KiWiVersioningSail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
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
 * This test checks if the versioning functionality itself works, i.e. the system properly creates versions on
 * transaction commits. 
 * 
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class VersioningRepositoryTest {

    private KiWiStore store;

    private KiWiTransactionalSail tsail;

    private KiWiVersioningSail    vsail;

    private Repository repository;

    private final KiWiConfiguration dbConfig;

    public VersioningRepositoryTest(KiWiConfiguration dbConfig) {
        this.dbConfig = dbConfig;
    }


    @Before
    public void initDatabase() throws RepositoryException {
        store = new KiWiStore(dbConfig);
        tsail = new KiWiTransactionalSail(store);
        vsail = new KiWiVersioningSail(tsail);
        repository = new SailRepository(vsail);
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

    /**
     * This test imports three small RDF files in sequence and checks afterwards that the number of versions
     * is correct and they contain the correct information
     * @throws Exception
     */
    @Test
    public void testCreateVersions() throws Exception {
        // import three files in sequence and check if the versions are created properly

        Date date1 = new Date();

        mysqlSleep();

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

        mysqlSleep();

        Date date2 = new Date();

        mysqlSleep();

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

        // list all versions
        List<Version> versions = asList(vsail.listVersions());
        Assert.assertEquals("expected 3 versions!", 3, versions.size());
        Assert.assertTrue("version order is not correct", versions.get(0).getId() < versions.get(1).getId());
        Assert.assertTrue("version order is not correct", versions.get(1).getId() < versions.get(2).getId());
        Assert.assertEquals(3, (long)versions.get(0).getAddedTriples().size());
        Assert.assertEquals(3, (long)versions.get(1).getAddedTriples().size());
        Assert.assertEquals(1, (long)versions.get(2).getAddedTriples().size());

        List<Version> versions1 = asList(vsail.listVersions(date1,date2));
        Assert.assertEquals("expected 1 version!", 1, versions1.size());
        Assert.assertEquals(3, (long)versions1.get(0).getAddedTriples().size());
    }


    /**
     * This test imports three small RDF files in sequence and checks afterwards that the number of versions
     * is correct and they contain the correct information
     * @throws Exception
     */
    @Test
    public void testRevertVersions() throws Exception {
        // import three files in sequence and check if the versions are created properly

        Date date1 = new Date();

        mysqlSleep();

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

        mysqlSleep();

        Date date2 = new Date();

        mysqlSleep();

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

        // list all versions
        List<Version> versions = asList(vsail.listVersions());
        Assert.assertEquals("expected 3 versions!", 3, versions.size());

        URI subject = repository.getValueFactory().createURI("http://marmotta.apache.org/testing/ns1/R1");
        URI predicate = repository.getValueFactory().createURI("http://marmotta.apache.org/testing/ns1/P2");

        RepositoryConnection connectionBeforeRevert = repository.getConnection();
        try {
            Assert.assertTrue(connectionBeforeRevert.hasStatement(subject,predicate,null,true));
        } finally {
            connectionBeforeRevert.close();
        }

        // revert version; afterwards we expect there to be 4 versions
        vsail.revertVersion(versions.get(0));

        List<Version> versions2 = asList(vsail.listVersions());
        Assert.assertEquals("expected 4 versions!", 4, versions2.size());

        // the repository should now lo longer contain any P2 property for ns1:C
        RepositoryConnection connectionAfterRevert = repository.getConnection();
        try {
            Assert.assertFalse(connectionAfterRevert.hasStatement(subject, predicate, null, true));
        } finally {
            connectionAfterRevert.close();
        }



    }


    /**
     * MYSQL rounds timestamps to the second, so it is sometimes necessary to sleep before doing a test
     */
    private  void mysqlSleep() {
        if(this.dbConfig.getDialect() instanceof MySQLDialect) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
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
