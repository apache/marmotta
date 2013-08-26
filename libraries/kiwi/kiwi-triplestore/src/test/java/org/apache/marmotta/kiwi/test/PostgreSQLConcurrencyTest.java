package org.apache.marmotta.kiwi.test;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Random;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.SailException;
import org.slf4j.LoggerFactory;

/**
 * This test starts many triplestore operations in parallel to check if concurrent operations will break things,
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class PostgreSQLConcurrencyTest extends ConcurrencyTestBase {

    private static KiWiStore store;

    @BeforeClass
    public static void setup() throws RepositoryException {
        logger = LoggerFactory.getLogger(PostgreSQLConcurrencyTest.class);

        KiWiConfiguration psql = KiWiDatabaseRunner.createKiWiConfig("PostgreSQL", new PostgreSQLDialect());
        DBConnectionChecker.checkDatabaseAvailability(psql);
        
        rnd = new Random();

        store = new KiWiStore(psql);
        repository = new SailRepository(store);
        repository.initialize();
    }

    @AfterClass
    public static void dropDatabase() throws RepositoryException, SQLException, SailException {
    	if (store != null && store.isInitialized()) {
            assertTrue(store.checkConsistency());
            store.closeValueFactory(); // release all connections before dropping the database
            store.getPersistence().dropDatabase();
            repository.shutDown();
    	}
    }


}
