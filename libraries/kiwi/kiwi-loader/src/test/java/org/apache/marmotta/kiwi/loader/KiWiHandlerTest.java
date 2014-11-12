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
package org.apache.marmotta.kiwi.loader;

import info.aduna.iteration.Iterations;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.loader.generic.KiWiHandler;
import org.apache.marmotta.kiwi.loader.mysql.KiWiMySQLHandler;
import org.apache.marmotta.kiwi.loader.pgsql.KiWiPostgresHandler;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.*;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class KiWiHandlerTest {

    private KiWiStore store;
    private Repository repository;

    private final KiWiConfiguration dbConfig;

    public KiWiHandlerTest(KiWiConfiguration dbConfig) {
        this.dbConfig = dbConfig;
        dbConfig.setFulltextEnabled(true);
        dbConfig.setFulltextLanguages(new String[] {"en"});
    }


    @Before
    public void initDatabase() throws RepositoryException, IOException, RDFParseException, SailException {
        store = new KiWiStore(dbConfig);
        store.setDropTablesOnShutdown(true);
        repository = new SailRepository(store);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException, SailException {
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
            logger.info("{}: {} being run...", dbConfig.getDialect(), description.getMethodName());
        }
    };

    @Test
    public void testImportNoCheck() throws Exception {
        testImport(new KiWiLoaderConfiguration(),"demo-data.foaf", RDFFormat.RDFXML);
    }

    @Test
    public void testImportExistanceCheck() throws Exception {
        KiWiLoaderConfiguration cfg = new KiWiLoaderConfiguration();
        cfg.setStatementExistanceCheck(true);
        testImport(cfg, "demo-duplicates.ttl", RDFFormat.TURTLE);

        // check for duplicates
        URI R1 = repository.getValueFactory().createURI("http://localhost:8080/R1");
        URI R2 = repository.getValueFactory().createURI("http://localhost:8080/R2");
        URI T1 = repository.getValueFactory().createURI("http://localhost:8080/T1");

        try {
            RepositoryConnection con = repository.getConnection();
            try {
                con.begin();

                List<Statement> r1l = Iterations.asList(con.getStatements(R1, RDF.TYPE, T1, true));
                Assert.assertEquals(1, r1l.size());

                List<Statement> r2l = Iterations.asList(con.getStatements(R2, null, null, true));
                Assert.assertEquals(1, r2l.size());

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } finally {
                con.close();
            }
        } catch(RepositoryException ex) {
            ex.printStackTrace(); // TODO: handle error
        }

    }


    private void testImport(KiWiLoaderConfiguration c, String file, RDFFormat fmt) throws RDFParseException, IOException, RDFHandlerException {
        KiWiHandler handler;
        if(store.getPersistence().getDialect() instanceof PostgreSQLDialect) {
            handler = new KiWiPostgresHandler(store, c);
        } else if(store.getPersistence().getDialect() instanceof MySQLDialect) {
            handler = new KiWiMySQLHandler(store, c);
        } else {
            handler = new KiWiHandler(store, c);
        }

        try {
            // bulk import
            long start = System.currentTimeMillis();
            RDFParser parser = Rio.createParser(fmt);
            parser.setRDFHandler(handler);
            parser.parse(this.getClass().getResourceAsStream(file),"");

            logger.info("bulk import in {} ms", System.currentTimeMillis() - start);

            // check presence of data
            try {
                RepositoryConnection con = repository.getConnection();
                try {
                    con.begin();

                    Assert.assertTrue(con.hasStatement(null,null,null,true));

                    con.commit();
                } catch(RepositoryException ex) {
                    con.rollback();
                } finally {
                    con.close();
                }
            } catch(RepositoryException ex) {
                ex.printStackTrace(); // TODO: handle error
            }
        } finally {
            handler.shutdown();
        }

    }

}
