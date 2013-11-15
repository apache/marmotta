package org.apache.marmotta.kiwi.loader;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.loader.generic.KiWiHandler;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
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
        repository = new SailRepository(store);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException, SailException {
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
    public void testImport() throws Exception {

        // bulk import
        long start = System.currentTimeMillis();
        RDFParser parser = Rio.createParser(RDFFormat.RDFXML);
        parser.setRDFHandler(new KiWiHandler(store, new KiWiLoaderConfiguration()));
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

    }

}
