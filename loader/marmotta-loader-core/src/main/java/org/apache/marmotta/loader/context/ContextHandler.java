package org.apache.marmotta.loader.context;

import org.apache.marmotta.loader.api.LoaderHandler;
import org.apache.marmotta.loader.wrapper.LoaderHandlerWrapper;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.rio.RDFHandlerException;

/**
 * A handler adding a pre-defined context to each statement
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContextHandler extends LoaderHandlerWrapper {

    private URI context;

    public ContextHandler(LoaderHandler handler, URI context) {
        super(handler);
        this.context = context;
    }


    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        Statement wrapped = new ContextStatementImpl(st.getSubject(),st.getPredicate(),st.getObject(), context);

        super.handleStatement(wrapped);
    }
}
