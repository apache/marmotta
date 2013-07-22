package org.apache.marmotta.kiwi.test;

import java.sql.SQLException;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;

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
 * Author: Sebastian Schaffert

 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MySQLConcurrencyTest {


    @Rule public ConcurrentRule concurrently = new ConcurrentRule();
    @Rule public RepeatingRule repeatedly = new RepeatingRule();

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

    private static KiWiDialect dialect;

    private static String jdbcUrl;

    private static String jdbcUser;

    private static String jdbcPass;

    private static Repository repository;

    private static KiWiStore store;

    private static Random rnd;

    private static long runs = 0;

    @BeforeClass
    public static void setup() throws RepositoryException {
        jdbcPass = System.getProperty("mysql.pass","lmf");
        jdbcUrl = System.getProperty("mysql.url");
        jdbcUser = System.getProperty("mysql.user","lmf");
        rnd = new Random();

        dialect = new MySQLDialect();

        DBConnectionChecker.checkDatabaseAvailability(jdbcUrl, jdbcUser, jdbcPass, dialect);

        store = new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred" );
        repository = new SailRepository(store);
        repository.initialize();
    }

    @AfterClass
    public static void dropDatabase() throws RepositoryException, SQLException, SailException {
        assertTrue(store.checkConsistency());
    	if (store != null) {
	        store.closeValueFactory(); // release all connections before dropping the database
	        store.getPersistence().dropDatabase();
	        repository.shutDown();
    	}
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    final Logger logger =
            LoggerFactory.getLogger(MySQLConcurrencyTest.class);


    long tripleCount = 0;

    @Test
    @Concurrent(count = 10)
    @Repeating(repetition = 10)
    public void testConcurrency() throws Exception {
        runs++;

        // generate random nodes and triples and add them
        RepositoryConnection con = repository.getConnection();
        try {
            for(int i=0; i< rnd.nextInt(1000); i++) {
                URI subject = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                URI predicate = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                Value object;
                switch(rnd.nextInt(6)) {
                    case 0: object = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                        break;
                    case 1: object = repository.getValueFactory().createBNode();
                        break;
                    case 2: object = repository.getValueFactory().createLiteral(RandomStringUtils.random(40));
                        break;
                    case 3: object = repository.getValueFactory().createLiteral(rnd.nextInt());
                        break;
                    case 4: object = repository.getValueFactory().createLiteral(rnd.nextDouble());
                        break;
                    case 5: object = repository.getValueFactory().createLiteral(rnd.nextBoolean());
                        break;
                    default: object = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                        break;

                }
                con.add(subject,predicate,object);
                tripleCount++;
            }
            con.commit();
        } finally {
            con.close();
        }


        logger.info("triple count: {}", tripleCount);
    }
}
