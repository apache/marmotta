package org.apache.marmotta.kiwi.loader.pgsql;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.kiwi.loader.KiWiLoaderConfiguration;
import org.apache.marmotta.kiwi.loader.generic.KiWiHandler;
import org.apache.marmotta.kiwi.loader.pgsql.csv.PGCopyUtil;
import org.apache.marmotta.kiwi.model.rdf.KiWiAnonResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.persistence.util.ScriptRunner;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.openrdf.model.Literal;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.postgresql.copy.PGCopyOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * A fast-lane RDF import handler for PostgreSQL backends. This importer takes advantage of the PostgreSQL COPY command
 * that allows direct injection into the database. It works by creating an intermediate CSV buffer that is flushed into
 * the databases in batches (using a configurable batch size).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiPostgresHandler extends KiWiHandler implements RDFHandler {

    private static Logger log = LoggerFactory.getLogger(KiWiPostgresHandler.class);


    private List<KiWiNode> nodeBacklog;
    private List<KiWiTriple> tripleBacklog;

    private Map<Literal,KiWiLiteral> literalBacklogLookup;
    private Map<String,KiWiUriResource> uriBacklogLookup;
    private Map<String,KiWiAnonResource> bnodeBacklogLookup;


    public KiWiPostgresHandler(KiWiStore store, KiWiLoaderConfiguration config) {
        super(store, config);
    }

    /**

     /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        log.info("starting import using optimized PostgreSQL data loader");

        this.tripleBacklog = new ArrayList<>(config.getStatementBatchSize());
        this.nodeBacklog   = new ArrayList<>(config.getStatementBatchSize()*2);
        this.literalBacklogLookup = new HashMap<>();
        this.uriBacklogLookup = new HashMap<>();
        this.bnodeBacklogLookup = new HashMap<>();

        super.startRDF();

        if(config.isDropIndexes()) {
            try {
                dropIndexes();
                connection.commit();
            } catch (SQLException | IOException e) {
                throw new RDFHandlerException("error while dropping indexes", e);
            }
        }
    }


    /**
     * Signals the end of the RDF data. This method is called when all data has
     * been reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        try {
            flushBacklog();
        } catch (SQLException e) {
            throw new RDFHandlerException(e);
        }

        if(config.isDropIndexes()) {
            try {
                createIndexes();
                connection.commit();
            } catch (SQLException | IOException e) {
                throw new RDFHandlerException("error while dropping indexes", e);
            }
        }

        super.endRDF();

    }


    @Override
    protected KiWiAnonResource createBNode(String nodeID) {
        // check in backlog, if not found call super method
        KiWiAnonResource result = bnodeBacklogLookup.get(nodeID);
        if(result == null) {
            result = super.createBNode(nodeID);
        }
        return result;
    }

    @Override
    protected KiWiLiteral createLiteral(Literal l) throws ExecutionException {
        KiWiLiteral result = literalBacklogLookup.get(l);
        if(result == null) {
            result = super.createLiteral(l);
        }
        return result;
    }

    @Override
    protected KiWiUriResource createURI(String uri) {
        KiWiUriResource result = uriBacklogLookup.get(uri);
        if(result == null) {
            result = super.createURI(uri);
        }
        return result;
    }

    @Override
    protected void storeNode(KiWiNode node) throws SQLException {
        if(node.getId() == null) {
            node.setId(connection.getNextSequence("nodes"));
        }

        nodeBacklog.add(node);

        if(node instanceof KiWiUriResource) {
            uriBacklogLookup.put(node.stringValue(),(KiWiUriResource)node);
        } else if(node instanceof KiWiAnonResource) {
            bnodeBacklogLookup.put(node.stringValue(), (KiWiAnonResource)node);
        } else if(node instanceof KiWiLiteral) {
            literalBacklogLookup.put((KiWiLiteral)node, (KiWiLiteral)node);
        }

        nodes++;
    }

    @Override
    protected void storeTriple(KiWiTriple result) throws SQLException {
        if(result.getId() == null) {
            result.setId(connection.getNextSequence("triples"));
        }

        tripleBacklog.add(result);

        triples++;

        if(triples % config.getCommitBatchSize() == 0) {
            try {
                flushBacklog();
                connection.commit();
            } catch (SQLException ex) {
                log.warn("could not flush out data ({}), retrying with fresh connection", ex.getCause().getMessage());
                log.warn("exception:", ex.getCause());
                connection.close();
                connection = store.getPersistence().getConnection();
                flushBacklog();
                connection.commit();
            }

            printStatistics();
        }
    }

    private synchronized void flushBacklog() throws SQLException {
        try {
            // flush out nodes
            PGCopyOutputStream nodesOut = new PGCopyOutputStream(PGCopyUtil.getWrappedConnection(connection.getJDBCConnection()), "COPY nodes FROM STDIN (FORMAT csv)");
            PGCopyUtil.flushNodes(nodeBacklog, nodesOut);
            nodesOut.close();

            // flush out triples
            PGCopyOutputStream triplesOut = new PGCopyOutputStream(PGCopyUtil.getWrappedConnection(connection.getJDBCConnection()), "COPY triples FROM STDIN (FORMAT csv)");
            PGCopyUtil.flushTriples(tripleBacklog, triplesOut);
            triplesOut.close();
        } catch (IOException ex) {
            throw new SQLException("error while flushing out data",ex);
        }

        nodeBacklog.clear();
        tripleBacklog.clear();

        uriBacklogLookup.clear();
        bnodeBacklogLookup.clear();
        literalBacklogLookup.clear();

    }


    private void dropIndexes() throws SQLException, IOException {
        ScriptRunner runner = new ScriptRunner(connection.getJDBCConnection(), false, false);

        log.info("PostgreSQL: dropping indexes before import");
        StringBuilder script = new StringBuilder();
        for(String line : IOUtils.readLines(KiWiPostgresHandler.class.getResourceAsStream("drop_indexes.sql"))) {
            if(!line.startsWith("--")) {
                script.append(line);
                script.append(" ");
            }
        }
        log.debug("PostgreSQL: running SQL script '{}'", script.toString());
        runner.runScript(new StringReader(script.toString()));
    }

    private void createIndexes() throws SQLException, IOException {
        ScriptRunner runner = new ScriptRunner(connection.getJDBCConnection(), false, false);

        log.info("PostgreSQL: re-creating indexes after import");
        StringBuilder script = new StringBuilder();
        for(String line : IOUtils.readLines(KiWiPostgresHandler.class.getResourceAsStream("create_indexes.sql"))) {
            if(!line.startsWith("--")) {
                script.append(line);
                script.append(" ");
            }
        }
        log.debug("PostgreSQL: running SQL script '{}'", script.toString());
        runner.runScript(new StringReader(script.toString()));
    }

}
