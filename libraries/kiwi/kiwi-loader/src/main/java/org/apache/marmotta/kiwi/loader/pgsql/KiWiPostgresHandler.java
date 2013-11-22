package org.apache.marmotta.kiwi.loader.pgsql;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.kiwi.loader.KiWiLoaderConfiguration;
import org.apache.marmotta.kiwi.loader.generic.KiWiBatchHandler;
import org.apache.marmotta.kiwi.persistence.util.ScriptRunner;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.openrdf.rio.RDFHandler;
import org.postgresql.copy.PGCopyOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

/**
 * A fast-lane RDF import handler for PostgreSQL backends. This importer takes advantage of the PostgreSQL COPY command
 * that allows direct injection into the database. It works by creating an intermediate CSV buffer that is flushed into
 * the databases in batches (using a configurable batch size).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiPostgresHandler extends KiWiBatchHandler implements RDFHandler {

    private static Logger log = LoggerFactory.getLogger(KiWiPostgresHandler.class);



    public KiWiPostgresHandler(KiWiStore store, KiWiLoaderConfiguration config) {
        super("PostgreSQL", store, config);
    }


    @Override
    protected void flushBacklogInternal() throws SQLException {
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
    }


    @Override
    protected void dropIndexes() throws SQLException {
        try {
            ScriptRunner runner = new ScriptRunner(connection.getJDBCConnection(), false, false);

            StringBuilder script = new StringBuilder();
            for(String line : IOUtils.readLines(KiWiPostgresHandler.class.getResourceAsStream("drop_indexes.sql"))) {
                if(!line.startsWith("--")) {
                    script.append(line);
                    script.append(" ");
                }
            }
            log.debug("PostgreSQL: running SQL script '{}'", script.toString());
            runner.runScript(new StringReader(script.toString()));
        } catch (IOException ex) {
            throw new SQLException("error while dropping indexes",ex);
        }
    }

    @Override
    protected void createIndexes() throws SQLException {
        try {
            ScriptRunner runner = new ScriptRunner(connection.getJDBCConnection(), false, false);

            StringBuilder script = new StringBuilder();
            for(String line : IOUtils.readLines(KiWiPostgresHandler.class.getResourceAsStream("create_indexes.sql"))) {
                if(!line.startsWith("--")) {
                    script.append(line);
                    script.append(" ");
                }
            }
            log.debug("PostgreSQL: running SQL script '{}'", script.toString());
            runner.runScript(new StringReader(script.toString()));
        } catch (IOException ex) {
            throw new SQLException("error while creating indexes",ex);
        }
    }

}
