package org.apache.marmotta.loader.api;

import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface LoaderHandler extends RDFHandler {

    /**
     * Initialise the handler, performing any initialisation steps that are necessary before bulk importing can
     * start (e.g. dropping indexes or establishing a connection).
     *
     * @throws RDFHandlerException
     */
    public void initialise() throws RDFHandlerException;

    /**
     * Peform cleanup on shutdown, e.g. re-creating indexes after import completed or freeing resources acquired by
     * the handler.
     */
    public void shutdown() throws RDFHandlerException;


}
