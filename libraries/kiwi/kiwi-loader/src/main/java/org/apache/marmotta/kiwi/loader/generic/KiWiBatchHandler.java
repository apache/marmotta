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

package org.apache.marmotta.kiwi.loader.generic;

import org.apache.marmotta.commons.sesame.model.LiteralCommons;
import org.apache.marmotta.kiwi.loader.KiWiLoaderConfiguration;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.openrdf.model.Literal;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Generic superclass for batch-mode KiWi import handlers (PostgreSQL and MySQL).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class KiWiBatchHandler extends KiWiHandler implements RDFHandler {

    private static Logger log = LoggerFactory.getLogger(KiWiBatchHandler.class);


    protected List<KiWiNode> nodeBacklog;
    protected List<KiWiTriple> tripleBacklog;

    protected Map<String,KiWiLiteral> literalBacklogLookup;
    protected Map<String,KiWiUriResource> uriBacklogLookup;
    protected Map<String,KiWiAnonResource> bnodeBacklogLookup;


    protected String backend;

    /**
     * Create a new batch handler for the given store using the given configuration.
     *
     * @param backend human-readable name for the backend (e.g. "PostgreSQL"); used for logging
     * @param store   KiWiStore used for importing
     * @param config  loader configuration
     */
    public KiWiBatchHandler(String backend, KiWiStore store, KiWiLoaderConfiguration config) {
        super(store, config);

        this.backend = backend;
    }


    /**
     * Perform initialisation, e.g. dropping indexes or other preparations.
     */
    @Override
    public void initialise() throws RDFHandlerException {
        super.initialise();

        if(config.isDropIndexes()) {
            try {
                log.info("{}: dropping indexes before import", backend);
                dropIndexes();
                connection.commit();
            } catch (SQLException e) {
                throw new RDFHandlerException("error while dropping indexes", e);
            }
        }
    }

    /**
     * Peform cleanup on shutdown, e.g. re-creating indexes after import completed
     */
    @Override
    public void shutdown() throws RDFHandlerException {
        if(config.isDropIndexes()) {
            try {
                log.info("{}: re-creating indexes after import", backend);
                createIndexes();
                connection.commit();
            } catch (SQLException e) {
                throw new RDFHandlerException("error while dropping indexes", e);
            }
        }
        super.shutdown();


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
        log.debug("starting import using optimized {} data loader", backend);

        this.tripleBacklog = new ArrayList<>(config.getStatementBatchSize());
        this.nodeBacklog   = new ArrayList<>(config.getStatementBatchSize()*2);
        this.literalBacklogLookup = new HashMap<>();
        this.uriBacklogLookup = new HashMap<>();
        this.bnodeBacklogLookup = new HashMap<>();

        super.startRDF();

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
        try {
            flushBacklog();
            connection.commit();
        } catch (SQLException e) {
            throw new RDFHandlerException(e);
        }


        super.endRDF();

    }


    @Override
    protected KiWiAnonResource createBNode(String nodeID) {
        // check in backlog, if not found call super method
        KiWiAnonResource result = bnodeBacklogLookup.get(nodeID);
        if(result == null) {
            result = super.createBNode(nodeID);
        }
        return result;
    }

    @Override
    protected KiWiLiteral createLiteral(Literal l) throws ExecutionException {
        KiWiLiteral result = literalBacklogLookup.get(LiteralCommons.createCacheKey(l));
        if(result == null) {
            result = super.createLiteral(l);
        }
        return result;
    }

    @Override
    protected KiWiUriResource createURI(String uri) {
        KiWiUriResource result = uriBacklogLookup.get(uri);
        if(result == null) {
            result = super.createURI(uri);
        }
        return result;
    }

    @Override
    protected void storeNode(KiWiNode node) throws SQLException {
        if(node.getId() < 0) {
            node.setId(connection.getNextSequence());
        }

        nodeBacklog.add(node);

        if(node instanceof KiWiUriResource) {
            uriBacklogLookup.put(node.stringValue(),(KiWiUriResource)node);
        } else if(node instanceof KiWiAnonResource) {
            bnodeBacklogLookup.put(node.stringValue(), (KiWiAnonResource)node);
        } else if(node instanceof KiWiLiteral) {
            literalBacklogLookup.put(LiteralCommons.createCacheKey((Literal) node), (KiWiLiteral)node);
        }

        nodes++;
    }

    @Override
    protected void storeTriple(KiWiTriple result) throws SQLException {

        tripleBacklog.add(result);

        triples++;

        if(triples % config.getCommitBatchSize() == 0) {
            try {
                flushBacklog();
                if(registry != null) {
                    registry.releaseTransaction(connection.getTransactionId());
                }
                connection.commit();
            } catch (SQLException ex) {
                log.warn("could not flush out data ({}), retrying with fresh connection", ex.getCause().getMessage());
                log.warn("exception:", ex.getCause());
                connection.close();
                connection = store.getPersistence().getConnection();
                flushBacklog();
                connection.commit();
            }

        }
    }


    /**
     * Flush the backlog (nodeBacklog and tripleBacklog) to the database; needs to be implemented by subclasses.
     * @throws SQLException
     */
    protected abstract void flushBacklogInternal() throws SQLException;


    private synchronized void flushBacklog() throws SQLException {
        flushBacklogInternal();

        nodeBacklog.clear();
        tripleBacklog.clear();

        uriBacklogLookup.clear();
        bnodeBacklogLookup.clear();
        literalBacklogLookup.clear();

    }

    /**
     * Drop indexes in the database to increase import performance; needs to be implemented by subclasses. If this
     * feature is not supported, can be an empty method.
     * @throws SQLException
     */
    protected abstract void dropIndexes() throws SQLException;

    /**
     * Create indexes again in the database after importing has finished; needs to be implemented by subclasses and
     * should revert all changes done by dropIndexes()
     *
     * @throws SQLException
     */
    protected abstract void createIndexes() throws SQLException;

}
