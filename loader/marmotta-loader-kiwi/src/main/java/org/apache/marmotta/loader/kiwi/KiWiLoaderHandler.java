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
package org.apache.marmotta.loader.kiwi;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.loader.KiWiLoaderConfiguration;
import org.apache.marmotta.kiwi.loader.generic.KiWiHandler;
import org.apache.marmotta.kiwi.loader.mysql.KiWiMySQLHandler;
import org.apache.marmotta.kiwi.loader.pgsql.KiWiPostgresHandler;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.loader.api.LoaderHandler;
import org.apache.marmotta.loader.api.LoaderOptions;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiLoaderHandler implements LoaderHandler {

    private static Logger log = LoggerFactory.getLogger(KiWiLoaderHandler.class);

    private KiWiConfiguration kiwi;

    protected KiWiStore store;

    protected SailRepository repository;

    private KiWiHandler handler;

    private Configuration configuration;

    public KiWiLoaderHandler(Configuration configuration) {
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
        String dbCon = configuration.getString("backend.kiwi.connect");
        String dbUser = configuration.getString("backend.kiwi.user");
        String dbPasswd = configuration.getString("backend.kiwi.password");

        log.info("Initializing KiWiLoader for {}; user: {}, password: {}", dbCon, dbUser, String.format("%"+dbPasswd.length()+"s", "*"));

        try {
            KiWiConfiguration kiwi = new KiWiConfiguration("kiwiLoader", dbCon, dbUser, dbPasswd, getDialect(dbCon).newInstance());

            store = new KiWiStore(kiwi);

            repository = new SailRepository(store);
            repository.initialize();



            KiWiLoaderConfiguration loaderConfiguration = new KiWiLoaderConfiguration();
            if(configuration.containsKey(LoaderOptions.CONTEXT)) {
                loaderConfiguration.setContext(configuration.getString(LoaderOptions.CONTEXT));
            }

            loaderConfiguration.setDropIndexes(configuration.getBoolean("backend.kiwi.drop-indexes", false));

            if(kiwi.getDialect() instanceof PostgreSQLDialect) {
                log.info("- using PostgreSQL bulk loader ... ");
                loaderConfiguration.setCommitBatchSize(100000);
                handler = new KiWiPostgresHandler(store,loaderConfiguration);
            } else if(kiwi.getDialect() instanceof MySQLDialect) {
                log.info("- using MySQL bulk loader ... ");
                loaderConfiguration.setCommitBatchSize(100000);
                handler = new KiWiMySQLHandler(store,loaderConfiguration);
            } else {
                log.info("- using generic KiWi loader ... ");
                handler = new KiWiHandler(store,loaderConfiguration);
            }
            handler.initialise();
        } catch (RepositoryException e) {
            throw new RDFHandlerException("error initialising KiWi repository",e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RDFHandlerException("could not instatiate KiWi dialect",e);
        }

    }

    /**
     * Peform cleanup on shutdown, e.g. re-creating indexes after import completed or freeing resources acquired by
     * the handler.
     */
    @Override
    public void shutdown() throws RDFHandlerException {
        try {
            handler.shutdown();
            repository.shutDown();
            repository = null;
        } catch (RepositoryException e) {
            throw new RDFHandlerException("error shutting down KiWi repository",e);
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
        handler.handleNamespace(prefix,uri);
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
        handler.handleComment(comment);
    }




    private Class<? extends KiWiDialect> getDialect(String jdbcUrl) {
        String[] components = jdbcUrl.split(":");
        if(StringUtils.equalsIgnoreCase("postgresql", components[1])) {
            return PostgreSQLDialect.class;
        } else if(StringUtils.equalsIgnoreCase("mysql", components[1])) {
            return MySQLDialect.class;
        } else if(StringUtils.equalsIgnoreCase("h2", components[1])) {
            return H2Dialect.class;
        } else {
            throw new IllegalArgumentException("database dialect "+components[1]+" not supported by KiWi");
        }
    }
}
