package org.apache.marmotta.kiwi.loader;

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
        testImport(new KiWiLoaderConfiguration());
    }

    @Test
    public void testImportExistanceCheck() throws Exception {
        KiWiLoaderConfiguration cfg = new KiWiLoaderConfiguration();
        cfg.setStatementExistanceCheck(true);
        testImport(cfg);
    }


    private void testImport(KiWiLoaderConfiguration c) throws RDFParseException, IOException, RDFHandlerException {
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
            RDFParser parser = Rio.createParser(RDFFormat.RDFXML);
            parser.setRDFHandler(handler);
            parser.parse(this.getClass().getResourceAsStream("demo-data.foaf"),"");

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
