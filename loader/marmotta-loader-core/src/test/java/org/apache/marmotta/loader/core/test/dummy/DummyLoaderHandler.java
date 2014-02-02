package org.apache.marmotta.loader.core.test.dummy;

import org.apache.marmotta.loader.api.LoaderHandler;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class DummyLoaderHandler extends RDFHandlerBase implements LoaderHandler {

    private Model model;

    public DummyLoaderHandler() {
        model = new TreeModel();
    }

    public Model getModel() {
        return model;
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

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        model.add(st);
    }
}
