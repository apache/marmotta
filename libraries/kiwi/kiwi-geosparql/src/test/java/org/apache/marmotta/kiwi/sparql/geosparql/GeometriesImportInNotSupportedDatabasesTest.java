package org.apache.marmotta.kiwi.sparql.geosparql;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.sparql.sail.KiWiSparqlSail;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Test that geo-data imports in the geosparql non-supported databases
 *
 * @author Sergio Fernández (wikier@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
@KiWiDatabaseRunner.ForDialects({H2Dialect.class,MySQLDialect.class})
public class GeometriesImportInNotSupportedDatabasesTest {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    private KiWiStore store;
    private Repository repository;

    private final KiWiConfiguration dbConfig;

    public GeometriesImportInNotSupportedDatabasesTest(KiWiConfiguration dbConfig) {
        this.dbConfig = dbConfig;
        dbConfig.setFulltextEnabled(true);
        dbConfig.setFulltextLanguages(new String[] {"en"});
        DBConnectionChecker.checkDatabaseAvailability(dbConfig);
    }

    @Before
    public void initDatabase() throws RepositoryException, IOException, RDFParseException {
        store = new KiWiStore(dbConfig);
        store.setDropTablesOnShutdown(true);
        repository = new SailRepository(new KiWiSparqlSail(store));
        repository.initialize();

        log.info("load some to test...");
        final RepositoryConnection conn = repository.getConnection();
        try {
            conn.begin();
            conn.add(this.getClass().getResourceAsStream("/demo_data_spain_provinces.rdf"), "http://localhost/test/geosparql", RDFFormat.RDFXML);
            conn.add(this.getClass().getResourceAsStream("/demo_data_spain_towns.rdf"), "http://localhost/test/geosparql", RDFFormat.RDFXML);
            conn.add(this.getClass().getResourceAsStream("/demo_data_spain_rivers.rdf"), "http://localhost/test/geosparql", RDFFormat.RDFXML);
            conn.commit();
        } finally {
            conn.close();
        }
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
        if (store != null && store.isInitialized()) {
            log.info("cleaning up test setup...");
            store.getPersistence().dropDatabase();
            repository.shutDown();
            store = null;
            repository = null;
        }
    }

    private void testLoadFile(String path) throws RepositoryException, IOException, RDFParseException {
        log.info("load test data from {}....", path);
        final RepositoryConnection conn = repository.getConnection();
        try {
            conn.begin();
            conn.add(this.getClass().getResourceAsStream(path), "http://localhost/test", RDFFormat.RDFXML);
            conn.commit();
            //TODO: check size
        } finally {
            conn.close();
        }
    }

    @Test
    public void testSpainshProvices() throws RepositoryException, IOException, RDFParseException {
        testLoadFile("/demo_data_spain_provinces.rdf");
    }

    @Test
    public void testSpainshTowns() throws RepositoryException, IOException, RDFParseException {
        testLoadFile("/demo_data_spain_towns.rdf");
    }

    @Test
    public void testSpainshRivers() throws RepositoryException, IOException, RDFParseException {
        testLoadFile("/demo_data_spain_rivers.rdf");
    }

}
