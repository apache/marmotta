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
import org.apache.marmotta.commons.sesame.transactions.api.TransactionListener;
import org.apache.marmotta.commons.sesame.transactions.api.TransactionalSail;
import org.apache.marmotta.commons.sesame.transactions.model.TransactionData;
import org.apache.marmotta.commons.sesame.transactions.sail.KiWiTransactionalSail;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.*;
import org.apache.marmotta.platform.core.qualifiers.event.transaction.AfterCommit;
import org.apache.marmotta.platform.core.qualifiers.event.transaction.AfterRollback;
import org.apache.marmotta.platform.core.qualifiers.event.transaction.BeforeCommit;
import org.apache.marmotta.platform.core.qualifiers.inject.Fallback;
import org.apache.marmotta.platform.core.util.CDIContext;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;
import java.util.Iterator;

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
     * triple store providers from backend modules
     */
    @Inject
    private Instance<StoreProvider> storeProviders;

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

    /**
     * garbage collectors for backends that support it
     */
    @Inject
    private Instance<GarbageCollectionProvider> garbageCollectionProviders;

    private NotifyingSail store;

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

            if(storeProviders.isUnsatisfied()) {
                log.error("no storage backend found in classpath; please add one of the marmotta-backend-XXX modules");
                throw new UnsatisfiedResolutionException("no storage backend found in classpath; please add one of the marmotta-backend-XXX modules");
            }

            if(storeProviders.isAmbiguous()) {
                log.warn("more than one storage backend in classpath, trying to select the most appropriate ...");
                StoreProvider candidate = null;

                Iterator<StoreProvider> it = storeProviders.iterator();
                while (it.hasNext()) {
                    StoreProvider next = it.next();

                    log.warn("- candidate: {} (annotations: {})", next.getName(), next.getClass().getAnnotations());

                    if(candidate == null || !next.getClass().isAnnotationPresent(Fallback.class)) {
                        candidate = next;
                    }
                }

                log.warn("selected storage backend: {}", candidate.getName());

                store = candidate.createStore();
            } else {
                store = storeProviders.get().createStore();
            }


            NotifyingSail notifyingSail;

            log.info("initialising repository plugins ...");

            // TODO: should also wrap a transactional sail in case there are observers on the classpath!
            if(   !transactionalSailProviders.isUnsatisfied() || CDIContext.hasObservers(this, "beforeCommitEvent")
               || CDIContext.hasObservers(this, "afterCommitEvent") || CDIContext.hasObservers(this, "afterRollbackEvent")) {
                log.info("enabling transaction notification");

                KiWiTransactionalSail tsail = new KiWiTransactionalSail(store);

                // the CDI events should be triggered once all internal events have been handled, so register the transaction listener last
                tsail.addTransactionListener(new LMFTransactionEventProxy());


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
                notifyingSail = transactionalSail;
            } else {
                log.info("not enabling transaction notification, because no transaction observers are registered");
                notifyingSail = store;
            }

            // wrap all stackable notifying sails
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


            repository = storeProviders.get().createRepository(standardSail);

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

    /**
     * Run the triple store garbage collector manually and clean up unreferenced nodes and triples.
     * @throws SailException
     */
    @Override
    public void garbageCollect() throws SailException {
        if(store != null) {
            for(GarbageCollectionProvider p : garbageCollectionProviders) {
                p.garbageCollect(store);
            }
        }
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
