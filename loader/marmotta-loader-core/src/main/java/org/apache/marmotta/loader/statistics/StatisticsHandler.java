/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
