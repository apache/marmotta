package org.apache.marmotta.kiwi.loader.pgsql;

import org.apache.marmotta.kiwi.loader.KiWiLoaderConfiguration;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A fast-lane RDF import handler for PostgreSQL backends. This importer takes advantage of the PostgreSQL COPY command
 * that allows direct injection into the database. It works by creating an intermediate CSV buffer that is flushed into
 * the databases in batches (using a configurable batch size).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiPostgresHandler implements RDFHandler {

    private static Logger log = LoggerFactory.getLogger(KiWiPostgresHandler.class);


    private KiWiConnection connection;
    private KiWiStore store;

    private KiWiLoaderConfiguration config;

    private List<KiWiNode> nodeBacklog;
    private List<KiWiTriple> tripleBacklog;




    public KiWiPostgresHandler(KiWiStore store, KiWiLoaderConfiguration config) {
        this.store = store;
        this.config = config;
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Handles a namespace declaration/definition. A namespace declaration
     * associates a (short) prefix string with the namespace's URI. The prefix
     * for default namespaces, which do not have an associated prefix, are
     * represented as empty strings.
     *
     * @param prefix The prefix for the namespace, or an empty string in case of a
     *               default namespace.
     * @param uri    The URI that the prefix maps to.
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Handles a comment.
     *
     * @param comment The comment.
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }



}
