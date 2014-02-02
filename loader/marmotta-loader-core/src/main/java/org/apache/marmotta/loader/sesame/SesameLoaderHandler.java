package org.apache.marmotta.loader.sesame;

import org.apache.marmotta.loader.api.LoaderHandler;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerWrapper;

/**
 * A simple wrapper that allows using standard RDF handlers as MarmottaHandler
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SesameLoaderHandler extends RDFHandlerWrapper implements LoaderHandler {


    public SesameLoaderHandler(RDFHandler... rdfHandlers) {
        super(rdfHandlers);
    }

    /**
     * Initialise the handler, performing any initialisation steps that are necessary before bulk importing can
     * start (e.g. dropping indexes or establishing a connection).
     *
     * @throws org.openrdf.rio.RDFHandlerException
     */
    @Override
    public void initialise() throws RDFHandlerException {

    }

    /**
     * Peform cleanup on shutdown, e.g. re-creating indexes after import completed or freeing resources acquired by
     * the handler.
     */
    @Override
    public void shutdown() throws RDFHandlerException {

    }
}
