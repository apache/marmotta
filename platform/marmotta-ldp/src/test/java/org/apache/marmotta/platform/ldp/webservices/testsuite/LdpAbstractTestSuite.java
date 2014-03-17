package org.apache.marmotta.platform.ldp.webservices.testsuite;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * LDP Abstract Test Suite
 *
 * @author Sergio Fernández
 * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/Test%20Cases/LDP%20Test%20Cases.html">Linked Data Platform 1.0 Test Cases</a>
 */
public abstract class LdpAbstractTestSuite {

    protected static Logger log = LoggerFactory.getLogger(LdpAbstractTestSuite.class);

    /**
     * Load a dataset into a new in-memory repository
     *
     * @param file file name
     * @return connection to the repository
     * @throws RDFParseException
     * @throws RepositoryException
     * @throws IOException
     */
    protected Repository loadDataset(String file) throws RDFParseException, RepositoryException, IOException {
        log.debug("creating new in-memory repository...");
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        RepositoryConnection conn = repo.getConnection();
        try {
            conn.begin();
            conn.clear();
            loadDataset(conn, file);
            conn.commit();
        } finally {
            conn.close();
        }
        return repo;
    }

    /**
     * Load a dataset to the connection passed
     *
     * @param conn connection
     * @param file file name
     * @throws RDFParseException
     * @throws RepositoryException
     * @throws IOException
     */
    protected void loadDataset(RepositoryConnection conn, String file) throws RDFParseException, RepositoryException, IOException {
        log.debug("loading dataset from {}...", file);
        InputStream dataset = getClass().getResourceAsStream(file);
        try {
            conn.add(dataset, "", Rio.getParserFormatForFileName(file));
        }
        finally {
            dataset.close();
        }
        log.debug("dataset successfully loaded");
    }

}
