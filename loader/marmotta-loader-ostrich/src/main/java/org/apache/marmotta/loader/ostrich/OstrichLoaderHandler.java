/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.loader.ostrich;

import org.apache.marmotta.loader.api.LoaderHandler;
import org.apache.marmotta.ostrich.sail.OstrichSail;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class OstrichLoaderHandler implements LoaderHandler {

    private OstrichSail sail;
    private SailConnection con;

    long count = 0;
    long batchSize = 500000;

    public OstrichLoaderHandler(String host, int port, long batchSize) {
        this.batchSize = batchSize;
        this.sail      = new OstrichSail(host,port);
    }

    /**
     * Initialise the handler, performing any initialisation steps that are necessary before bulk importing can
     * start (e.g. dropping indexes or establishing a connection).
     *
     * @throws RDFHandlerException
     */
    @Override
    public void initialise() throws RDFHandlerException {
        try {
            sail.initialize();
            con = sail.getConnection();
        } catch (SailException e) {
            throw new RDFHandlerException("Could not establish Ostrich connection", e);
        }
    }

    /**
     * Peform cleanup on shutdown, e.g. re-creating indexes after import completed or freeing resources acquired by
     * the handler.
     */
    @Override
    public void shutdown() throws RDFHandlerException {
        try {
            con.close();
            sail.shutDown();
        } catch (SailException e) {
            throw new RDFHandlerException("Could not close Ostrich connection", e);
        }

    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        try {
            con.begin();
        } catch (SailException e) {
            throw new RDFHandlerException("Could not start transaction", e);
        }
    }

    /**
     * Signals the end of the RDF data. This method is called when all data has
     * been reported.
     *
     * @throws RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        try {
            con.commit();
        } catch (SailException e) {
            throw new RDFHandlerException("Could not commit transaction", e);
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
     * @throws RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        try {
            con.setNamespace(prefix, uri);
        } catch (SailException e) {
            throw new RDFHandlerException("Could not add namespace", e);
        }
    }

    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        try {
            con.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());

            if (++count % batchSize == 0) {
                con.commit();
                con.begin();
            }
        } catch (SailException e) {
            throw new RDFHandlerException("Could not add statement", e);
        }
    }

    /**
     * Handles a comment.
     *
     * @param comment The comment.
     * @throws RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleComment(String comment) throws RDFHandlerException {

    }
}
