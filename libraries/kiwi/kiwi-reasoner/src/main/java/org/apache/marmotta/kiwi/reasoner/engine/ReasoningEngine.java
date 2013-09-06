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
package org.apache.marmotta.kiwi.reasoner.engine;

import com.google.common.base.Equivalence;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.Iterations;
import info.aduna.iteration.SingletonIteration;
import org.apache.marmotta.commons.sesame.model.StatementCommons;
import org.apache.marmotta.kiwi.model.caching.TripleTable;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.reasoner.model.program.*;
import org.apache.marmotta.kiwi.reasoner.model.query.QueryResult;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningConnection;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningPersistence;
import org.apache.marmotta.kiwi.sail.KiWiSailConnection;
import org.apache.marmotta.kiwi.transactions.api.TransactionListener;
import org.apache.marmotta.kiwi.transactions.api.TransactionalSail;
import org.apache.marmotta.kiwi.transactions.model.TransactionData;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class implements the evaluation of reasoning programs on the triple store. It has two different modes:
 * <ul>
 * <li>incremental reasoning: when new triples are added or old triples are removed, the inferred information is updated accordingly</li>
 * <li>full reasoning: (re-)runs the reasoning process over all triples currently contained in the triple store</li>
 * </ul>
 * Since reasoning can require some time, the actual execution of incremental reasonong is implemented in a
 * producer-consumer style. When new transaction data is available, it is added to a queue. A separate reasoning
 * thread then takes new transaction data and processes the rules asynchronously.
 * <p/>
 * The reasoning engine uses its own connection to the database to carry out reasoning tasks.
 * <p/>
 * TODO: we need to clarify conceptually whether it would be correct to run several reasoner threads in parallel.
 * In theory, reasoning here is strictly monotonic so there should not be a problem. In practice, we might miss
 * certain triples because the order of transactions might be messed up. Maybe it makes more sense to parallelize
 * the execution or rules instead.
 * <p/>
 * User: Sebastian Schaffert (sschaffert@apache.org)
 */
public class ReasoningEngine implements TransactionListener {

    private static Logger log = LoggerFactory.getLogger(ReasoningEngine.class);

    private static final String TASK_GROUP = "Reasoner";

    /**
     * A queue of transaction data objects of committed transactions, will be consumed by the reasoner
     * thread in incremental reasoning.
     */
    private LinkedBlockingQueue<TransactionData> reasoningQueue;


    /**
     * A direct connection to the database to perform queries and store program-related information
     */
    private KiWiReasoningPersistence persistence;


    /**
     * A connection to the underlying KiWiStore to store new inferred triples and to access the value
     * factory.
     */
    private TransactionalSail        store;

    /**
     * Configuration for this reasoning engine.
     */
    private ReasoningConfiguration   config;

    /**
     * In-memory cache of the currently active reasoning programs, (re-)initialized by {@link #loadPrograms()}.
     */
    private List<Program> programs;

    /**
     * In-memory cache to store all patterns that are candidates for matching triples and accessing the rules
     * they belong to.
     */
    private Multimap<Pattern,Rule> patternRuleMap;

    /**
     * Internal counter to count executions of the reasoner (informational purposes only)
     */
    private static long taskCounter = 0;


    /**
     * The worker thread for the reasoner.
     */
    private SKWRLReasoner reasonerThread;

    private static Equivalence<Statement> equivalence = StatementCommons.quadrupleEquivalence();

    /**
     * A lock to ensure that only once thread at a time is carrying out persistence
     */
    private Lock persistenceLock;

    public ReasoningEngine(KiWiReasoningPersistence persistence, TransactionalSail store, ReasoningConfiguration config) {
        this.persistence = persistence;
        this.store = store;
        this.config = config;
        this.persistenceLock = new ReentrantLock();

        loadPrograms();

        this.reasoningQueue = new LinkedBlockingQueue<TransactionData>();
        this.reasonerThread = new SKWRLReasoner();
    }

    public void loadPrograms() {
        log.info("program configuration changed, reloading ...");
        patternRuleMap = HashMultimap.<Pattern,Rule>create();

        try {
            KiWiReasoningConnection connection = persistence.getConnection();
            try {
                programs       = Iterations.asList(connection.listPrograms());

                for(Program p : programs) {
                    for(Rule rule : p.getRules()) {
                        for(Pattern pattern : rule.getBody()) {
                            patternRuleMap.put(pattern,rule);
                        }
                    }
                }
            } finally {
                connection.close();
            }
        } catch (SQLException ex) {
            programs = Collections.emptyList();
            log.warn("cannot load reasoning programs, reasoning disabled (error message: {})", ex.getMessage());
        }
    }


    public void programChanged(Program program) {
    }

    /**
     * In case a new rule has been added to one of the reasoning programs, process only this rule addition
     * incrementally. Since the reasoner is strictly monotonic, this is sufficient.
     *
     * @param rule
     */
    public void notifyAddRule(Rule rule) {
        startTask("Addition of rule " + rule.getName(), TASK_GROUP);

        log.debug("processing new rule: {}", rule);

        try {
            updateTaskStatus("processing new rule ...");
            processRule(rule, null, null);

        } catch(Exception ex) {
            log.error("error while processing rule",ex);

            return;
        } finally {
            endTask();
        }
    }

    /**
     * In case an existing rule has been removed from one of the reasoning programs, remove all triples
     * that are based on this rule by consulting the justifications that use this rule. Since the reasoner
     * is strictly monotonic, this is sufficient to get the correct set of materialized triples.
     *
     */
    public void notifyRemoveRules() {
        startTask("Removing Rules", TASK_GROUP);

        // clean up justifications depending on the rule
        updateTaskStatus("cleaning up unsupported triples");


        try {
            KiWiReasoningConnection connection = persistence.getConnection();
            try {

                // this is done automatically now when updating or deleting a program:
                // removeJustificationsByQuery("reasoner.listJustificationsByRule", ImmutableMap.of("rule", (Object) rule));

                // then remove all inferred triples that are no longer supported
                cleanupUnsupported(connection);

                // and finally garbage collect those triples that are inferred and deleted
                // garbage collection is now carried out by a thread in the triple store
                //garbageCollectTriples();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.close();
            }
        } catch (SailException ex) {
            log.error("REPOSITORY ERROR: could not clean up unsupported triples, database state will be inconsistent! Message: {}", ex.getMessage());
            log.debug("Exception details:", ex);
        } catch (SQLException ex) {
            log.error("DATABASE ERROR: could not clean up justifications for triples, database state will be inconsistent! Message: {}", ex.getMessage());
            log.debug("Exception details:", ex);
        }


        endTask();
    }



    /**
     * Incrementally apply the updates that are contained in the transaction
     * data to the reasoning programs that are in the system.
     * <p/>
     * Called after a transaction has committed. The transaction data will contain all changes done in the transaction since
     * the last commit. This method should be used in case the transaction listener aims to perform additional activities
     * in a new transaction or outside the transaction management, e.g. notifying a server on the network, adding
     * data to a cache, or similar.
     *
     * @param data
     */
    @Override
    public void afterCommit(TransactionData data) {
        if( (data.getAddedTriples().size() > 0 || data.getRemovedTriples().size() > 0) && patternRuleMap.size() > 0) {

            reasoningQueue.remove(data);
            if (!reasoningQueue.offer(data)) {
                log.info("waiting for reasoning queue to become available ...");
                try {
                    reasoningQueue.put(data);
                    log.info("reasoning queue available, added data");
                } catch (InterruptedException e) {
                    log.error("interrupted while waiting for reasoning queue to become available ...");
                }
            };


        }
    }

    /**
     * Called before a transaction commits. The transaction data will contain all changes done in the transaction since
     * the last commit. This method should be used in case the transaction listener aims to perform additional activities
     * in the same transaction, like inserting or updating database tables.
     *
     * @param data
     */
    @Override
    public void beforeCommit(TransactionData data) {
        // do nothing
    }

    /**
     * Called when a transaction rolls back.
     */
    @Override
    public void rollback(TransactionData data) {
        // do nothing
    }


    /**
     * Start a new reasoner task to collect status messages. Informational purposes only.
     * @param name
     */
    protected void startTask(String name, String taskGroup) {
        // if a task is already running, this should create a nested subtask
    }

    /**
     * Stop the currently active reasoning task. Informational purposes only.
     */
    protected void endTask() {

    }


    /**
     * Update the status of the reasoner task with the given message. This will simply send all update messages to
     * registered status listeners.
     * @param message
     */
    protected void updateTaskStatus(String message) {

    }

    /**
     * Update the task progress (if any)
     * @param progress
     */
    protected void updateTaskProgress(int progress) {

    }

    /**
     * Set the maximum steps needed in the task progress (if any)
     * @param progress
     */
    protected void updateTaskMaxProgress(int progress) {

    }

    private void executeReasoner(TransactionData data) {
        updateTaskStatus("fetching worklist");
        Set<KiWiTriple> newTriples = StatementCommons.newQuadrupleSet();
        for(Statement stmt : data.getAddedTriples()) {
            KiWiTriple t = (KiWiTriple)stmt;
            if(t.isMarkedForReasoning()) {
                newTriples.add(t);
                t.setMarkedForReasoning(false);
            }
        }

        //taskManagerService.setTaskSteps(newTriples.size() + data.getRemovedTriples().size());
        // evaluate the rules for all added triples
        if(newTriples.size() > 0) {
            long start2 = System.currentTimeMillis();
            updateTaskStatus("reasoning over " + newTriples.size() + " new triples");
            processRules(newTriples);
            log.debug("REASONER: reasoning for {} new triples took {} ms overall", newTriples.size(), System.currentTimeMillis() - start2);
        }

        if(data.getRemovedTriples().size() > 0) {
            log.debug("cleaning up justifications and inferences for {} triples",data.getRemovedTriples().size());
            try {
                KiWiReasoningConnection connection = persistence.getConnection();
                try {
                    // first clean up justifications that are no longer supported
                    cleanupJustifications(connection, data.getRemovedTriples());


                    // then remove all inferred triples that are no longer supported
                    cleanupUnsupported(connection);

                    // and finally garbage collect those triples that are inferred and deleted
                    // garbage collection is now carried out by a thread in the triple store
                    //garbageCollectTriples();
                    connection.commit();
                } catch (SQLException ex) {
                    connection.rollback();
                    throw ex;
                } finally {
                    connection.close();
                }
            } catch (SailException ex) {
                log.error("REPOSITORY ERROR: could not clean up unsupported triples, database state will be inconsistent! Message: {}", ex.getMessage());
                log.debug("Exception details:", ex);
            } catch (SQLException ex) {
                log.error("DATABASE ERROR: could not clean up justifications for triples, database state will be inconsistent! Message: {}", ex.getMessage());
                log.debug("Exception details:", ex);
            }


        }

    }

    /**
     * Clean all inferred triples and re-run all reasoning rules.
     */
    public void reRunPrograms() {
        final String taskName = "Reasoner Task "+ ++taskCounter + " (full reasoning)";

        startTask("Synchronous " + taskName, TASK_GROUP);
        executeReasoner();
        endTask();
    }


    /**
     * Perform a full reasoning over the triples and rules contained in the database. Will first remove all existing
     * inferred triples and justifications and then evaluate each of the rules in turn.
     */
    private void executeReasoner() {
        // clean up all justifications
        updateTaskStatus("removing old justifications");


        try {
            KiWiReasoningConnection connection = persistence.getConnection();
            try {

                // remove all justifications from the database
                connection.deleteJustifications();

                // clean up inferred triples by removing them from the triple store; the transaction system should take care of the rest
                cleanupUnsupported(connection);

                // and finally garbage collect those triples that are inferred and deleted
                // garbage collection is now carried out by a thread in the triple store
                //garbageCollectTriples();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.close();
            }
        } catch (SailException ex) {
            log.error("REPOSITORY ERROR: could not clean up unsupported triples, database state will be inconsistent! Message: {}", ex.getMessage());
            log.debug("Exception details:", ex);
        } catch (SQLException ex) {
            log.error("DATABASE ERROR: could not clean up justifications for triples, database state will be inconsistent! Message: {}", ex.getMessage());
            log.debug("Exception details:", ex);
        }

        // process the rules over the whole dataset
        try {
            updateTaskStatus("processing rules ...");

            //Set<Callable<Boolean>> tasks = new HashSet<Callable<Boolean>>();
            for(Program p : programs) {
                for(final Rule rule : p.getRules()) {
                    startTask("Rule Processing", TASK_GROUP);
                    updateTaskStatus("processing rule " + rule.getName() + " ...");
                    processRule(rule, null, null);
                    endTask();
                    // TODO: multithreading is currently not working reliably (some nodes might get inserted twice)

/*
                    tasks.add(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            startTask("Rule Processing", TASK_GROUP);
                            updateTaskStatus("processing rule " + rule.getName() + " ...");
                            processRule(rule, null, null);
                            endTask();
                            return Boolean.TRUE;
                        }
                    });
*/
                }
            }
            //workers.invokeAll(tasks);
        } catch(Exception ex) {
            log.error("error while processing rules", ex);

            return;
        }

    }


    /**
     * This method iterates over all triples that are passed as argument and
     * checks whether they are used as supporting triples justifications. All
     * such justifications are removed. Triples that are no longer supported
     * will later be cleaned up by {@link #cleanupUnsupported(org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningConnection)}
     *
     * @param removedTriples
     */
    private void cleanupJustifications(KiWiReasoningConnection connection, TripleTable<Statement> removedTriples) throws SQLException {
        updateTaskStatus("cleaning up justifications for " + removedTriples.size() + " removed triples");
        for(Statement stmt : removedTriples) {
            KiWiTriple t = (KiWiTriple)stmt;
            connection.deleteJustifications(t);
        }
    }


    /**
     * Cleanup inferred triples that are no longer supported by any justification.
     */
    private void cleanupUnsupported(KiWiReasoningConnection connection) throws SQLException, SailException {
        updateTaskStatus("cleaning up unsupported triples");

        int count = 0, total = 0;

        startTask("Unsupported Triple Cleaner", TASK_GROUP);
        updateTaskStatus("loading unsupported triples");

        CloseableIteration<KiWiTriple,SQLException> tripleIterator = connection.listUnsupportedTriples();
        try {
            if(tripleIterator.hasNext()) {

                updateTaskStatus("deleting unsupported triples");
                SailConnection tc = store.getConnection();
                KiWiSailConnection ic = getWrappedConnection(tc);
                try {
                    tc.begin();
                    while(tripleIterator.hasNext()) {
                        ic.removeInferredStatement(tripleIterator.next());
                        count++;
                    }
                    log.debug("removed {} unsupported triples",count);
                    tc.commit();
                } catch(SailException ex) {
                    ic.rollback();
                    throw ex;
                } finally {
                    ic.close();
                }
            }
        } finally {
            Iterations.closeCloseable(tripleIterator);

        }
    }


    /**
     *
     * @param addedTriples
     */
    private void processRules(final Set<KiWiTriple> addedTriples) {
        try {
            updateTaskStatus("processing rules ...");
            // select the rules that have at least one matching pattern; the match method will
            // return a set of variable bindings that we will be used to prepopulate the bindings
//            Set<Callable<Boolean>> tasks = new HashSet<Callable<Boolean>>();
            for(final Pattern pattern : patternRuleMap.keySet()) {
                for(KiWiTriple triple : addedTriples) {
                    QueryResult match = matches(pattern,triple);
                    if(match != null) {
                        for(Rule rule : patternRuleMap.get(pattern)) {
                            log.debug("REASONER(rule '{}'): pattern {} matched with triple {}", rule.getName(), pattern.toString(), triple.toString());
                            processRule(rule, match, pattern);
                        }
                    }
                }
                // TODO: for parallel reasoning, the problem is that we should only create one thread per rule and not
                // one per pattern, otherwise we can get duplicates because the same rule is evaluated twice
/*
                tasks.add(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        for(KiWiTriple triple : addedTriples) {
                            QueryResult match = matches(pattern,triple);
                            if(match != null) {
                                log.debug("pattern {} matched with triple {}", pattern.toString(), triple.toString());
                                processRule(patternRuleMap.get(pattern), match, pattern);
                            }
                        }

                        return Boolean.TRUE;
                    }
                });
*/
            }
            //workers.invokeAll(tasks);

        } catch(Exception ex) {
            log.error("error while processing rules",ex);
        }
    }

    /**
     * Process the rule given as argument. The set of bindings passed as argument is used as a seed of
     * bindings and will be further populated by pattern matches. The set of justifications is passed over
     * from previous calls so that justifications can be persisted in a batch.
     *
     * @param rule
     * @param match
     */
    private void processRule(Rule rule, QueryResult match, Pattern p) {

        // get the variable bindings for the rule evaluation
        log.debug("REASONER(rule '{}'): evaluating rule body {} ...", rule.getName() != null ? rule.getName() : rule.getId(), rule);

        // create a collection consisting of the body minus the pattern that already matched
        Set<Pattern> body = new HashSet<Pattern>(rule.getBody());

        if(p != null) {
            body.remove(p);
        }

        CloseableIteration<QueryResult, SQLException> bodyResult;

        try {
            KiWiReasoningConnection connection = persistence.getConnection();
            SailConnection     sail = store.getConnection();
            KiWiSailConnection isail = getWrappedConnection(sail);
            try {

                // if there are further patterns, evaluate them; if the matched pattern was the only pattern, then
                // simply take the match as binding
                if(body.size() > 0) {
                    bodyResult = connection.query(body,match,null,null,true);
                } else if(match != null) {
                    bodyResult = new SingletonIteration<QueryResult, SQLException>(match);
                } else {
                    bodyResult = new EmptyIteration<QueryResult, SQLException>();
                }

                // construct triples out of the bindings and the rule heads
                long counter = 0;

                // initialise a new set of justifications
                Set<Justification> justifications = new HashSet<Justification>();

                sail.begin();
                while(bodyResult.hasNext()) {
                    QueryResult row = bodyResult.next();
                    Map<VariableField,KiWiNode> binding = row.getBindings();

                    Resource subject = null;
                    URI property = null;
                    Value object;

                    if(rule.getHead().getSubject() != null && rule.getHead().getSubject().isVariableField()) {
                        if(!binding.get(rule.getHead().getSubject()).isUriResource() && !binding.get(rule.getHead().getSubject()).isAnonymousResource()) {
                            log.info("cannot use value {} as subject, because it is not a resource",binding.get(rule.getHead().getSubject()));
                            continue;
                        }
                        subject = (KiWiResource)binding.get(rule.getHead().getSubject());
                    } else if(rule.getHead().getSubject() != null && rule.getHead().getSubject().isResourceField()) {
                        subject = ((ResourceField)rule.getHead().getSubject()).getResource();
                    } else
                        throw new IllegalArgumentException("Subject of rule head may only be a variable or a resource; rule: "+rule);

                    if(rule.getHead().getProperty() != null && rule.getHead().getProperty().isVariableField()) {
                        if(!binding.get(rule.getHead().getProperty()).isUriResource()) {
                            log.info("cannot use value {} as property, because it is not a URI resource",binding.get(rule.getHead().getProperty()));
                            continue;
                        }
                        property = (KiWiUriResource)binding.get(rule.getHead().getProperty());
                    } else if(rule.getHead().getProperty() != null && rule.getHead().getProperty().isResourceField()) {
                        property = (KiWiUriResource)((ResourceField)rule.getHead().getProperty()).getResource();
                    } else
                        throw new IllegalArgumentException("Property of rule head may only be a variable or a resource; rule: "+rule);

                    if(rule.getHead().getObject() != null && rule.getHead().getObject().isVariableField()) {
                        object = binding.get(rule.getHead().getObject());
                    } else if(rule.getHead().getObject() != null && rule.getHead().getObject().isResourceField()) {
                        object = ((ResourceField)rule.getHead().getObject()).getResource();
                    } else if(rule.getHead().getObject() != null && rule.getHead().getObject().isLiteralField()) {
                        object = ((LiteralField)rule.getHead().getObject()).getLiteral();
                    } else
                        throw new IllegalArgumentException("Object of rule head may only be a variable, a literal, or a resource; rule: "+rule);


                    KiWiTriple triple = isail.addInferredStatement(subject, property, object);

                    Justification justification = new Justification();
                    justification.setTriple(triple);
                    justification.getSupportingRules().add(rule);
                    justification.getSupportingTriples().addAll(row.getJustifications());
                    justifications.add(justification);

                    // when the batch size is reached, commit the transaction, save the justifications, and start a new
                    // transaction and new justification set
                    if(++counter % config.getBatchSize() == 0) {
                        persistenceLock.lock();

                        try {

                            sail.commit();

                            log.debug("adding {} justifications",justifications.size());

                            updateTaskStatus("storing justifications ...");
                            Set<Justification> baseJustifications = getBaseJustifications(connection,justifications);

                            if(config.isRemoveDuplicateJustifications()) {
                                removeDuplicateJustifications(connection,baseJustifications);
                            }

                            log.debug("{} justifications added after resolving inferred triples", baseJustifications.size());

                            // persist the justifications that have been created in the rule processing
                            if(baseJustifications.size() > 0) {
                                connection.storeJustifications(baseJustifications);
                            }
                            connection.commit();
                            sail.begin();
                        } finally {
                            persistenceLock.unlock();
                        }
                        justifications.clear();
                    }
                }

                persistenceLock.lock();
                try {
                    sail.commit();

                    log.debug("adding {} justifications",justifications.size());
                    updateTaskStatus("storing justifications ...");
                    Set<Justification> baseJustifications = getBaseJustifications(connection,justifications);

                    if(config.isRemoveDuplicateJustifications()) {
                        removeDuplicateJustifications(connection,baseJustifications);
                    }

                    // persist the justifications that have been created in the rule processing
                    if(baseJustifications.size() > 0) {
                        connection.storeJustifications(baseJustifications);
                    }

                    log.debug("{} justifications added after resolving inferred triples", baseJustifications.size());

                    Iterations.closeCloseable(bodyResult);
                    connection.commit();
                } finally {
                    persistenceLock.unlock();
                }
            } catch(SailException ex) {
                connection.rollback();
                sail.rollback();
                throw ex;
            } catch(SQLException ex) {
                sail.rollback();
                connection.rollback();
                throw ex;
            } finally {
                connection.close();
                sail.close();
            }

        } catch(SQLException ex) {
            log.error("DATABASE ERROR: could not process rule, database state will be inconsistent! Message: {}",ex.getMessage());
            log.debug("Exception details:",ex);
        } catch (SailException ex) {
            log.error("REPOSITORY ERROR: could not process rule, database state will be inconsistent! Message: {}",ex.getMessage());
            log.debug("Exception details:", ex);
        }

    }

    /**
     * Return the justifications for the triple passed as argument.
     * @param t
     * @return
     */
    private Collection<Justification> getJustifications(KiWiReasoningConnection connection, KiWiTriple t, Set<Justification> transactionJustifications) throws SQLException {
        // TODO: transactionJustifications are ignored
        HashSet<Justification> justifications = new HashSet<Justification>();
        Iterations.addAll(connection.listJustificationsForTriple(t), justifications);
        for(Justification j : transactionJustifications) {
            if(equivalence.equivalent(j.getTriple(), t)) {
                justifications.add(j);
            }
        }
        return justifications;
    }

    /**
     * For all justifications contained in the set passed as argument, create corresponding base justifications,
     * i.e. justifications that only contain base triples and no inferred triples.
     *
     * @param justifications
     * @return
     */
    private Set<Justification> getBaseJustifications(KiWiReasoningConnection connection, Set<Justification> justifications) throws SQLException {
        Set<Justification> baseJustifications = new HashSet<Justification>();
        Map<KiWiTriple,Collection<Justification>> justificationCache = StatementCommons.newQuadrupleMap();

        for(Justification justification : justifications) {
            KiWiTriple triple = justification.getTriple();

            Justification newJustification = new Justification();
            newJustification.setSupportingRules(justification.getSupportingRules());
            newJustification.setTriple(triple);

            Set<Justification> tripleJustifications = Collections.singleton(newJustification);

            // resolve inferred triples by replacing them by their justifications
            for(KiWiTriple support : justification.getSupportingTriples()) {
                if(support.isInferred()) {
                    Collection<Justification> supportJustifications = justificationCache.get(support);
                    // cache justifications of triple in case they are needed again in this run
                    if(supportJustifications == null || supportJustifications.size() == 0) {
                        supportJustifications = getJustifications(connection, support, baseJustifications);
                        justificationCache.put(support,supportJustifications);
                    }

                    if(supportJustifications.size() == 0) {
                        log.error("error: inferred triple {} is not justified!",support);
                    }

                    // mix the two sets
                    Set<Justification> oldTripleJustifications = tripleJustifications;
                    tripleJustifications = new HashSet<Justification>();
                    for(Justification j1 : oldTripleJustifications) {
                        for(Justification j2 : supportJustifications) {
                            Justification j3 = new Justification();
                            j3.setTriple(triple);
                            j3.getSupportingTriples().addAll(j1.getSupportingTriples());
                            j3.getSupportingTriples().addAll(j2.getSupportingTriples());
                            j3.getSupportingRules().addAll(j1.getSupportingRules());
                            j3.getSupportingRules().addAll(j2.getSupportingRules());
                            tripleJustifications.add(j3);
                        }
                    }
                } else {
                    for(Justification j : tripleJustifications) {
                        j.getSupportingTriples().add(support);
                    }
                }
            }

            baseJustifications.addAll(tripleJustifications);
        }
        return baseJustifications;
    }

    /**
     * The reasoner might create identical justifications in the process of generating the base justifications
     * for a triple. This method removes duplicates at the expense of additional computation time.
     * @param justifications
     */
    private void removeDuplicateJustifications(KiWiReasoningConnection connection, Set<Justification> justifications) throws SQLException {
        // remove duplicate justifications
        Map<KiWiTriple,Collection<Justification>> justificationCache = StatementCommons.newQuadrupleMap();
        for(Iterator<Justification> it = justifications.iterator(); it.hasNext(); ) {
            Justification j = it.next();

            Collection<Justification> supportJustifications = justificationCache.get(j.getTriple());
            // cache justifications of triple in case they are needed again in this run
            if(supportJustifications == null) {
                supportJustifications = getJustifications(connection, j.getTriple(), Collections.<Justification>emptySet());
                justificationCache.put(j.getTriple(),supportJustifications);
            }

            if(supportJustifications.contains(j)) {
                it.remove();
            }
        }
    }


    private QueryResult matches(Pattern pattern, KiWiTriple triple) {
        boolean result = true;

        QueryResult match = new QueryResult();

        if(pattern.getSubject().isResourceField()) {
            result = ((ResourceField)pattern.getSubject()).getResource().equals(triple.getSubject());
        } else if(pattern.getSubject().isVariableField()) {
            KiWiNode binding = match.getBindings().get(pattern.getSubject());
            if(binding != null) {
                result = binding.equals(triple.getSubject());
            } else {
                match.getBindings().put((VariableField) pattern.getSubject(), triple.getSubject());
            }
        }

        if(result && pattern.getProperty().isResourceField()) {
            result = ((ResourceField)pattern.getProperty()).getResource().equals(triple.getPredicate());
        } else if(result && pattern.getProperty().isVariableField()) {
            KiWiNode binding = match.getBindings().get(pattern.getProperty());
            if(binding != null) {
                result = binding.equals(triple.getPredicate());
            } else {
                match.getBindings().put((VariableField) pattern.getProperty(), triple.getPredicate());
            }
        }

        if(result && pattern.getContext() != null && pattern.getContext().isResourceField()) {
            result = ((ResourceField)pattern.getContext()).getResource().equals(triple.getContext());
        } else if(result && pattern.getContext() != null && pattern.getContext().isVariableField()) {
            KiWiNode binding = match.getBindings().get(pattern.getContext());
            if(binding != null) {
                result = binding.equals(triple.getContext());
            } else {
                match.getBindings().put((VariableField) pattern.getContext(), triple.getContext());
            }
        }

        if(result && pattern.getObject().isResourceField()) {
            result = ((ResourceField)pattern.getObject()).getResource().equals(triple.getObject());
        } else if(result && pattern.getObject().isLiteralField()) {
            result = ((LiteralField)pattern.getObject()).getLiteral().equals(triple.getObject());
        } else if(result && pattern.getObject().isVariableField()) {
            KiWiNode binding = match.getBindings().get(pattern.getObject());
            if(binding != null) {
                result = binding.equals(triple.getObject());
            } else {
                match.getBindings().put((VariableField) pattern.getObject(), triple.getObject());
            }
        }


        if(result) {
            match.getJustifications().add(triple);
            return match;
        } else
            return null;
    }

    /**
     * Return true in case the reasoner is currently executing, false otherwise.
     * @return
     */
    public boolean isRunning() {
        return reasonerThread.isRunning() || !reasoningQueue.isEmpty();
    }

    public void shutdown() {
        log.info("shutting down reasoning service ...");

        for(int i = 0; i<10 && isRunning(); i++) {
            log.warn("reasoner not yet finished, waiting for 10 seconds (try={})", i+1);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
        }

        reasonerThread.shutdown();
    }

    /**
     * Return the KiWiSailConnection underlying a given sail connection. The method will follow wrapped
     * connections until it finds the KiWiSailConnection, or otherwise throws a SailException.
     * @param connection
     * @return
     */
    private KiWiSailConnection getWrappedConnection(SailConnection connection) throws SailException {
        SailConnection it = connection;
        while(it instanceof SailConnectionWrapper) {
            it = ((SailConnectionWrapper) it).getWrappedConnection();
            if(it instanceof KiWiSailConnection) {
                return (KiWiSailConnection) it;
            }
        }
        throw new SailException("no underlying KiWiSailConnection found for connection");
    }

    private static int indexerCounter = 0;

    private class SKWRLReasoner extends Thread {
        private boolean shutdown = false;
        private boolean running  = false;

        private SKWRLReasoner() {
            super("SKWRL Reasoner " + ++indexerCounter);
            setDaemon(true);
            start();
        }

        public void shutdown() {
            log.info("REASONER: signalling shutdown to reasoner thread");
            shutdown = true;
            this.interrupt();
        }

        public boolean isRunning() {
            return running;
        }

        @Override
        public void run() {
            log.info("{} starting up ...", getName());

            startTask(getName(), TASK_GROUP);

            while (!shutdown || reasoningQueue.size() > 0) {
                running = false;
                try {
                    updateTaskStatus("idle");

                    TransactionData data = reasoningQueue.take();
                    running = true;

                    updateTaskMaxProgress(reasoningQueue.size());

                    executeReasoner(data);
                } catch (InterruptedException ex) {

                } catch (Exception ex) {
                    log.warn("reasoning task threw an exception",ex);
                }
            }
            try {
                endTask();
            } catch (Exception ex) {
            }
            log.info("{} shutting down ...", getName());
        }
    }

}
