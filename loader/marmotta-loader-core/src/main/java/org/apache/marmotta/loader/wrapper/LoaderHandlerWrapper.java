package org.apache.marmotta.loader.wrapper;

import org.apache.marmotta.loader.api.LoaderHandler;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LoaderHandlerWrapper implements LoaderHandler {

    private LoaderHandler[] handlers;

    public LoaderHandlerWrapper(LoaderHandler... handlers) {
        this.handlers = handlers;
    }

    /**
     * Initialise the handler, performing any initialisation steps that are necessary before bulk importing can
     * start (e.g. dropping indexes or establishing a connection).
     *
     * @throws org.openrdf.rio.RDFHandlerException
     */
    @Override
    public void initialise() throws RDFHandlerException {
        for(LoaderHandler h : handlers) {
            h.initialise();
        }
    }

    /**
     * Peform cleanup on shutdown, e.g. re-creating indexes after import completed or freeing resources acquired by
     * the handler.
     */
    @Override
    public void shutdown() throws RDFHandlerException {
        for(LoaderHandler h : handlers) {
            h.shutdown();
        }
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        for(LoaderHandler h : handlers) {
            h.startRDF();
        }
    }

    /**
     * Signals the end of the RDF data. This method is called when all data has
     * been reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        for(LoaderHandler h : handlers) {
            h.endRDF();
        }
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
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        for(LoaderHandler h : handlers) {
            h.handleNamespace(prefix,uri);
        }

    }

    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        for(LoaderHandler h : handlers) {
            h.handleStatement(st);
        }
    }

    /**
     * Handles a comment.
     *
     * @param comment The comment.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        for(LoaderHandler h : handlers) {
            h.handleComment(comment);
        }
    }

    /**
     * Get the wrapped handlers.
     *
     * @return
     */
    public LoaderHandler[] getHandlers() {
        return handlers;
    }
}
