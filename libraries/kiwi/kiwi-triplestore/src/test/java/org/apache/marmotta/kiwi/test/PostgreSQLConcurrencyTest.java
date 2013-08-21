package org.apache.marmotta.kiwi.test;

import java.sql.SQLException;
import java.util.Random;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.SailException;

import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

/**
 * This test starts many triplestore operations in parallel to check if concurrent operations will break things,
 *
 * It will try running over all available databases. Except for in-memory databases like H2 or Derby, database URLs must be passed as
 * system property, or otherwise the test is skipped for this database. Available system properties:
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
 *         <li>mysql.pass (default: lmf)</li>
 *     </ul>
 *     </li>
 *     <li>H2:
 *     <ul>
 *         <li>h2.url, e.g. jdbc:h2:mem;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10</li>
 *         <li>h2.user (default: lmf)</li>
 *         <li>h2.pass (default: lmf)</li>
 *     </ul>
 *     </li>
 * </ul>
 * <p/>
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class PostgreSQLConcurrencyTest extends ConcurrencyTestBase {

    private static KiWiDialect dialect;

    private static String jdbcUrl;

    private static String jdbcUser;

    private static String jdbcPass;

    private static KiWiStore store;

    @BeforeClass
    public static void setup() throws RepositoryException {
        logger = LoggerFactory.getLogger(PostgreSQLConcurrencyTest.class);

        jdbcPass = System.getProperty("postgresql.pass","lmf");
        jdbcUrl = System.getProperty("postgresql.url");
        jdbcUser = System.getProperty("postgresql.user","lmf");
        rnd = new Random();

        dialect = new PostgreSQLDialect();

        DBConnectionChecker.checkDatabaseAvailability(jdbcUrl, jdbcUser, jdbcPass, dialect);

        store = new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred" );
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
