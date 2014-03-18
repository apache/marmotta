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
package org.apache.marmotta.loader.titan;

import com.thinkaurelius.titan.core.TitanFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.marmotta.loader.api.LoaderHandler;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple loader handler using an underlying Titan repository for storing the data.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class TitanLoaderHandler implements LoaderHandler {

    private static Logger log = LoggerFactory.getLogger(TitanLoaderHandler.class);

    private Configuration configuration;

    private TitanRDFHandler handler;

    private long triples = 0;

    public TitanLoaderHandler(Configuration configuration) {
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
        log.info("Initializing new Titan Graph Store (backend: {})", configuration.getString("storage.backend"));

        handler = new TitanRDFHandler(TitanFactory.open(configuration), configuration.getString("storage.indexes","p,c,pc"));
     }

    /**
     * Peform cleanup on shutdown, e.g. re-creating indexes after import completed or freeing resources acquired by
     * the handler.
     */
    @Override
    public void shutdown() throws RDFHandlerException {

    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        handler.startRDF();
    }

    /**
     * Signals the end of the RDF data. This method is called when all data has
     * been reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        handler.endRDF();
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
        handler.handleNamespace(prefix, uri);
    }

    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        handler.handleStatement(st);
    }

    /**
     * Handles a comment.
     *
     * @param comment The comment.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleComment(String comment) throws RDFHandlerException {

    }
}
