/**
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
package org.apache.marmotta.platform.core.services.triplestore;

import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.NotifyingSailProvider;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.api.triplestore.StandardSailProvider;
import org.apache.marmotta.platform.core.api.triplestore.TransactionalSailProvider;
import org.apache.marmotta.platform.core.qualifiers.event.transaction.AfterCommit;
import org.apache.marmotta.platform.core.qualifiers.event.transaction.AfterRollback;
import org.apache.marmotta.platform.core.qualifiers.event.transaction.BeforeCommit;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.transactions.api.TransactionListener;
import org.apache.marmotta.kiwi.transactions.api.TransactionalSail;
import org.apache.marmotta.kiwi.transactions.model.TransactionData;
import org.apache.marmotta.kiwi.transactions.sail.KiWiTransactionalSail;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.Sail;
import org.slf4j.Logger;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * Offers access to the Sesame repository underlying this Apache Marmotta instance. The activation/deactivation methods
 * of this service make sure the repository is properly initialised and shut down.
 * <p/>
 * Usage: to access the triple store properly through Sesame, you should follow the following
 * pattern:
 * <pre>
 *     RespositoryConnection con = sesameService.getConnection();
 *
 *     URI subject = con.getValueFactory().createURI(...);
 *     ...
 *     RepositoryResult&lt;Statement> result = con.getStatemenrs(subject,predicate,object,inferred,context);
 *     while(result.hasNext()) {
 *         Statement triple = result.next();
 *         ...
 *     }
 *
 *     con.close();
 * </pre>
 *
 * <p/>
 * Will replace the existing TripleStore at some point.
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class SesameServiceImpl implements SesameService {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject @BeforeCommit
    private Event<TransactionData> beforeCommitEvent;

    @Inject @AfterCommit
    private Event<TransactionData> afterCommitEvent;

    @Inject @AfterRollback
    private Event<TransactionData> afterRollbackEvent;

    /**
     * notifying sail providers from other modules
     */
    @Inject
    private Instance<NotifyingSailProvider> notifyingSailProviders;

    /**
     * transactional sail providers from other modules
     */
    @Inject
    private Instance<TransactionalSailProvider> transactionalSailProviders;

    /**
     * normal sail providers from other modules
     */
    @Inject
    private Instance<StandardSailProvider> standardSailProviders;


    private KiWiStore  store;

    private KiWiTransactionalSail tsail;

    private SailRepository repository;

    private ReentrantReadWriteLock restartLock = new ReentrantReadWriteLock();

    /**
     * Initialise the Sesame repository. Should be called on service activation.
     */
    @Override
    public synchronized void initialise() {
        restartLock.writeLock().lock();
        try {
            log.info("Apache Marmotta Sesame Repository Service starting up ...");

            if(repository != null) {
                log.warn("RDF repository has already been initialized");
            }

            String database = configurationService.getStringConfiguration("database.type");
            KiWiDialect dialect;
            if("h2".equalsIgnoreCase(database)) {
                dialect = new H2Dialect();
            } else if("mysql".equalsIgnoreCase(database)) {
                dialect = new MySQLDialect();
            } else if("postgres".equalsIgnoreCase(database)) {
                dialect = new PostgreSQLDialect();
            } else
                throw new IllegalStateException("database type "+database+" currently not supported!");
            String jdbcUrl = configurationService.getStringConfiguration("database.url");
            String dbUser  = configurationService.getStringConfiguration("database.user");
            String dbPass  = configurationService.getStringConfiguration("database.password");
            boolean batchCommit = configurationService.getBooleanConfiguration("database.batchcommit", true);

            KiWiConfiguration configuration = new KiWiConfiguration("lmf", jdbcUrl, dbUser, dbPass, dialect, configurationService.getDefaultContext(), configurationService.getInferredContext());
            configuration.setQueryLoggingEnabled(configurationService.getBooleanConfiguration("database.debug.slowqueries",false));
            configuration.setBatchCommit(batchCommit);
            configuration.setBatchSize(configurationService.getIntConfiguration("database.batchsize",10000));
            configuration.setMemorySequences(configurationService.getBooleanConfiguration("database.memsequences",true));

            store = new KiWiStore(configuration);

            tsail = new KiWiTransactionalSail(store);

            log.info("initialising repository plugins ...");

            // wrap all stackable transactional sails
            TransactionalSail transactionalSail = tsail;
            for(TransactionalSailProvider provider : transactionalSailProviders) {
                if(provider.isEnabled()) {
                    log.info("- transaction plugin: {}",provider.getName());
                    transactionalSail = provider.createSail(transactionalSail);
                } else {
                    log.info("- transaction plugin: {} (DISABLED)", provider.getName());
                }
            }

            // wrap all stackable notifying sails
            NotifyingSail notifyingSail = transactionalSail;
            for(NotifyingSailProvider provider : notifyingSailProviders) {
                if(provider.isEnabled()) {
                    log.info("- notifying plugin: {}",provider.getName());
                    notifyingSail = provider.createSail(notifyingSail);
                } else {
                    log.info("- notifying plugin: {} (DISABLED)", provider.getName());
                }
            }

            // wrap all standard sails
            Sail standardSail = notifyingSail;
            for(StandardSailProvider provider : standardSailProviders) {
                if(provider.isEnabled()) {
                    log.info("- standard plugin: {}",provider.getName());
                    standardSail = provider.createSail(standardSail);
                } else {
                    log.info("- standard plugin: {} (DISABLED)", provider.getName());
                }
            }

            // the CDI events should be triggered once all internal events have been handled, so register the transaction listener last
            tsail.addTransactionListener(new LMFTransactionEventProxy());

            repository = new SailRepository(standardSail);

            try {
                repository.initialize();
            } catch (RepositoryException e) {
                log.error("error while initialising Apache Marmotta Sesame repository",e);
            }
        } finally {
            restartLock.writeLock().unlock();
        }
    }

    /**
     * Shutdown the Sesame repository. Should be called on service deactivation.
     */
    @Override
    @PreDestroy
    public synchronized void shutdown() {
        restartLock.writeLock().lock();
        try {
            if(repository != null) {
                log.info("Apache Marmotta Sesame Repository Service shutting down ...");
                try {
                    repository.shutDown();
                } catch (RepositoryException e) {
                    log.error("error while shutting down Apache Marmotta Sesame repository",e);
                }
                repository = null;
            }
        } finally {
            restartLock.writeLock().unlock();
        }
    }

    /**
     * Restart the Sesame Service.
     */
    @Override
    public void restart() {
        restartLock.writeLock().lock();
        try {
            shutdown();
            initialise();
        } finally {
            restartLock.writeLock().unlock();
        }
    }

    /**
     * Reinit Sesame repository when the configuration has been initialised
     *
     * @param e
     */
    /*
    public void onConfigurationChange(@Observes ConfigurationChangedEvent e) {
        if(e.containsChangedKeyWithPrefix("database")) {
            shutdown();
            initialise();
        }
    }
    */

    /**
     * Return the Sesame Repository underlying this service. Callers should be careful with modifying
     * this object directly.
     *
     * @return the Sesame Repository instance used by this service
     */
    @Override
    @Produces
    public SailRepository getRepository() {
        return repository;
    }

    /**
     * Return a Sesame RepositoryConnection to the underlying repository. The connection has auto-commit disabled,
     * all transaction management must be performed explicitly.
     *
     * @return a RepositoryConnection to the underlying Sesame repository.
     */
    @Override
    @Produces
    public RepositoryConnection getConnection() throws RepositoryException {
        restartLock.readLock().lock();
        RepositoryConnection connection = repository.getConnection();
        return new RepositoryConnectionWrapper(repository,connection) {
            @Override
            public void close() throws RepositoryException {
                super.close();
                restartLock.readLock().unlock();
            }
        };
    }

    /**
     * Return a Sesame ValueFactory for creating new RDF objects.
     *
     * @return the Sesame ValueFactory belonging to the repository that is used by the service
     */
    @Override
    @Produces
    public ValueFactory getValueFactory() {
        return repository.getValueFactory();
    }

    private class LMFTransactionEventProxy implements TransactionListener {

        /**
         * Called before a transaction commits. The transaction data will contain all changes done in the transaction since
         * the last commit. This method should be used in case the transaction listener aims to perform additional activities
         * in the same transaction, like inserting or updating database tables.
         * <p/>
         * The implementation in lmf-core simply wraps the KiWi transaction data in a CDI transaction to notify other
         * services in the system.
         *
         * @param data
         */
        @Override
        public void beforeCommit(TransactionData data) {
            log.debug("transaction: before commit event");
            beforeCommitEvent.fire(data);
        }

        /**
         * Called after a transaction has committed. The transaction data will contain all changes done in the transaction since
         * the last commit. This method should be used in case the transaction listener aims to perform additional activities
         * in a new transaction or outside the transaction management, e.g. notifying a server on the network, adding
         * data to a cache, or similar.
         * <p/>
         * The implementation in lmf-core simply wraps the KiWi transaction data in a CDI transaction to notify other
         * services in the system.
         *
         * @param data
         */
        @Override
        public void afterCommit(TransactionData data) {
            log.debug("transaction: after commit event");
            afterCommitEvent.fire(data);
        }

        /**
         * Called when a transaction rolls back.
         * <p/>
         * The implementation in lmf-core simply wraps the KiWi transaction data in a CDI transaction to notify other
         * services in the system.
         */
        @Override
        public void rollback(TransactionData data) {
            log.debug("transaction: rollback event");
            afterRollbackEvent.fire(data);
        }

    }

}
