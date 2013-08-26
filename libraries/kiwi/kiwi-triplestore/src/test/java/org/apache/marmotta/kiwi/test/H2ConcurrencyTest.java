package org.apache.marmotta.kiwi.test;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Random;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
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
public class H2ConcurrencyTest extends ConcurrencyTestBase {

    private static KiWiStore store;

    @BeforeClass
    public static void setup() throws RepositoryException {
        logger = LoggerFactory.getLogger(H2ConcurrencyTest.class);

        KiWiConfiguration h2Config = KiWiDatabaseRunner.createKiWiConfig("H2", new H2Dialect());
        DBConnectionChecker.checkDatabaseAvailability(h2Config);
        
        rnd = new Random();

        store = new KiWiStore(h2Config);
        repository = new SailRepository(store);
        repository.initialize();
    }

    @AfterClass
    public static void dropDatabase() throws RepositoryException, SQLException, SailException {
        assertTrue(store.checkConsistency());
        store.closeValueFactory(); // release all connections before dropping the database
        store.getPersistence().dropDatabase();
        repository.shutDown();
    }
}
