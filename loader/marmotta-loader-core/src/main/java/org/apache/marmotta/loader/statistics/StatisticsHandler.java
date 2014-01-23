package org.apache.marmotta.loader.statistics;

import org.apache.commons.configuration.Configuration;
import org.apache.marmotta.loader.api.LoaderHandler;
import org.apache.marmotta.loader.api.LoaderOptions;
import org.apache.marmotta.loader.wrapper.LoaderHandlerWrapper;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class StatisticsHandler extends LoaderHandlerWrapper implements LoaderHandler {

    protected long triples = 0;

    private Statistics statistics;

    private Configuration configuration;

    public StatisticsHandler(LoaderHandler handler, Configuration configuration) {
        super(handler);
        this.configuration = configuration;
    }

    /**
     * Initialise the handler, performing any initialisation steps that are necessary before bulk importing can
     * start (e.g. dropping indexes or establishing a connection).
     *
     * @throws org.openrdf.rio.RDFHandlerException
     */
    @Override
    public void initialise() throws RDFHandlerException {
        statistics = new Statistics(this,configuration);
        statistics.startSampling();

        super.initialise();
    }

    /**
     * Peform cleanup on shutdown, e.g. re-creating indexes after import completed or freeing resources acquired by
     * the handler.
     */
    @Override
    public void shutdown() throws RDFHandlerException {
        super.shutdown();

        statistics.stopSampling();
    }

    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        super.handleStatement(st);

        triples++;

        if(triples % configuration.getLong(LoaderOptions.STATISTICS_INTERVAL, 10000L) == 0) {
            statistics.printStatistics();
        }
    }
}
