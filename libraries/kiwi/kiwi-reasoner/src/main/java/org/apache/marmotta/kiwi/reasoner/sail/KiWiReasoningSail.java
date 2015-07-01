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
package org.apache.marmotta.kiwi.reasoner.sail;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ExceptionConvertingIteration;

import org.apache.marmotta.commons.sesame.transactions.api.TransactionalSail;
import org.apache.marmotta.commons.sesame.transactions.wrapper.TransactionalSailWrapper;
import org.apache.marmotta.kiwi.reasoner.engine.ReasoningConfiguration;
import org.apache.marmotta.kiwi.reasoner.engine.ReasoningEngine;
import org.apache.marmotta.kiwi.reasoner.model.program.Justification;
import org.apache.marmotta.kiwi.reasoner.model.program.Program;
import org.apache.marmotta.kiwi.reasoner.model.program.Rule;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParser;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParserBase;
import org.apache.marmotta.kiwi.reasoner.parser.ParseException;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningConnection;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningPersistence;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * This sail adds the KWRL reasoner to the stack of sails. Because the reasoner is tightly coupled with the
 * database schema of KiWi, it requires that the persistence on the root of the stack is a KiWiStore.
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiReasoningSail extends TransactionalSailWrapper {

    private static Logger log = LoggerFactory.getLogger(KiWiReasoningSail.class);

    private ReasoningConfiguration   config;

    private ReasoningEngine          engine;

    private KiWiReasoningPersistence persistence;

    private boolean initialized = false;

    public KiWiReasoningSail(TransactionalSail parent, ReasoningConfiguration config) {
        super(parent);
        this.config = config;
    }

    @Override
    public void initialize() throws SailException {
        synchronized (this) {
            if(!initialized) {
                super.initialize();

                KiWiStore store = getBaseStore();

                try {
                    persistence = new KiWiReasoningPersistence(store.getPersistence(), getValueFactory());
                    persistence.initDatabase();

                    engine      = new ReasoningEngine(persistence,this,config);
                    addTransactionListener(engine);

                    initialized = true;
                } catch (SQLException e) {
                    log.error("error initializing reasoning database",e);
                    throw new SailException("error initializing reasoning database",e);
                }
            }
        }
    }

    @Override
    public void shutDown() throws SailException {
        engine.shutdown();
        super.shutDown();
    }

    /**
     * Return the KiWi store that is at the base of the SAIL stack. Throws an IllegalArgumentException in case the base
     * store is not a KiWi store.
     *
     * @return
     */
    public KiWiStore getBaseStore() {
        StackableSail current = this;
        while(current != null && current.getBaseSail() instanceof StackableSail) {
            current = (StackableSail) current.getBaseSail();
        }
        if(current != null && current.getBaseSail() instanceof KiWiStore) {
            return (KiWiStore) current.getBaseSail();
        } else {
            throw new IllegalStateException("the base store is not a KiWiStore (type: "+current.getBaseSail().getClass().getCanonicalName()+")!");
        }
    }

    public ReasoningConfiguration getConfig() {
        return config;
    }

    /**
     * Add a program to the reasoner using the given name. The program data will be read from the stream passed as
     * second argument. The program is persisted to the database and the reasoning engine is
     * notified of the added rules and immediately calculates the inferences. Inferencing in this case is
     * synchronous, so the method only returns when the first round of reasoning is completed for all added
     * rules.
     * <p/>
     * If a program with this name already exists, a SailException is thrown. To update existing programs,
     * please use updateProgram().
     *
     * @param name a unique name for the program
     * @param data the program data in KWRL syntax
     * @throws IOException    in case the stream cannot be read
     * @throws SailException  in case the program already exists
     * @throws ParseException in case the program cannot be parsed
     */
    public void addProgram(String name, InputStream data) throws IOException, SailException, ParseException {
        KWRLProgramParserBase parser = new KWRLProgramParser(getValueFactory(), data);
        Program p = parser.parseProgram();
        p.setName(name);

        addProgram(p);
    }

    /**
     * Add a program to the reasoner. The program is persisted to the database and the reasoning engine is
     * notified of the added rules and immediately calculates the inferences. Inferencing in this case is
     * synchronous, so the method only returns when the first round of reasoning is completed for all added
     * rules.
     * <p/>
     * If a program with this name already exists, a SailException is thrown. To update existing programs,
     * please use updateProgram().
     *
     * @param program the program data in KWRL syntax
     * @throws SailException  in case the program already exists
     */
    public void addProgram(Program program) throws SailException {
        // store program in the database
        try {
            KiWiReasoningConnection connection = persistence.getConnection();
            try {
                // should not throw an exception and the program should have a database ID afterwards
                connection.storeProgram(program);
                connection.commit();
            } finally {
                connection.close();
            }
        } catch (SQLException ex) {
            throw new SailException("cannot store program in database",ex);
        }

        engine.loadPrograms();

        // now add all added rules to the reasoner
        for(Rule rule : program.getRules()) {
            engine.notifyAddRule(rule);
        }
    }

    /**
     * Update the program with the name given as argument using the data provided in the stream.
     * This method will first calculate the difference between the
     * previous version of the program and the new version of the program. It then updates the program in
     * the database and notifies the engine of all removed and added rules.
     *
     * @throws IOException    in case the stream cannot be read
     * @throws SailException  in case the program already exists
     * @throws ParseException in case the program cannot be parsed
     */
    public void updateProgram(String name, InputStream data) throws IOException, SailException, ParseException  {
        KWRLProgramParserBase parser = new KWRLProgramParser(getValueFactory(), data);
        Program p = parser.parseProgram();
        p.setName(name);

        updateProgram(p);
    }


    /**
     * Update the program given as argument. This method will first calculate the difference between the
     * previous version of the program and the new version of the program. It then updates the program in
     * the database and notifies the engine of all removed and added rules.
     *
     * @param program  the updated version of the program
     * @throws SailException in case a database error occurs
     */
    public void updateProgram(Program program) throws SailException {
        Set<Rule> added = new HashSet<Rule>();
        Set<Rule> removed = new HashSet<Rule>();
        try {
            KiWiReasoningConnection connection = persistence.getConnection();
            try {
                // load old version of program and calculate difference
                Program old = connection.loadProgram(program.getName());
                if(old != null) {
                    for(Rule r : old.getRules()) {
                        if(!program.getRules().contains(r)) {
                            removed.add(r);
                        }
                    }
                    for(Rule r : program.getRules()) {
                        if(!old.getRules().contains(r)) {
                            added.add(r);
                        }
                    }

                }

                // store program in the database
                connection.updateProgram(program);
                connection.commit();
            } finally {
                connection.close();
            }
        } catch (SQLException ex) {
            throw new SailException("cannot store program in database",ex);
        }

        engine.loadPrograms();

        // if rules have been removed, clean up
        if(removed.size() > 0) {
            engine.notifyRemoveRules();
        }

        // now add all added rules to the reasoner
        for(Rule rule : added) {
            engine.notifyAddRule(rule);
        }
    }

    /**
     * List all reasoning programs currently stored in the triplestore.
     *
     * @return
     */
    public CloseableIteration<Program,SailException> listPrograms() throws SailException {
        try {
            final KiWiReasoningConnection connection = persistence.getConnection();

            return new ExceptionConvertingIteration<Program, SailException>(connection.listPrograms()) {
                /**
                 * Converts an exception from the underlying iteration to an exception of
                 * type <tt>X</tt>.
                 */
                @Override
                protected SailException convert(Exception e) {
                    return new SailException(e);
                }

                @Override
                protected void handleClose() throws SailException {
                    super.handleClose();

                    try {
                        connection.commit();
                        connection.close();
                    } catch (SQLException ex) {
                        throw new SailException("database error while committing/closing connection");
                    }
                }
            };
        } catch (SQLException ex) {
            throw new SailException("cannot list programs in database",ex);
        }

    }


    /**
     * Return the program with the given name. In case the program does not exist, the method will
     * return null.
     *
     * @param name the unique name of the program to retrieve
     * @return the parsed program, or null in case a program with the given name does not exist
     * @throws SailException  in case an error occurs
     */
    public Program getProgram(String name) throws SailException {
        try {
            KiWiReasoningConnection connection = persistence.getConnection();
            try {
                // should not throw an exception and the program should have a database ID afterwards
                Program p = connection.loadProgram(name);
                connection.commit();
                return p;
            } finally {
                connection.close();
            }
        } catch (SQLException ex) {
            throw new SailException("cannot load program from database",ex);
        }
    }


    /**
     * Remove the program with the given name. This method will first remove the program from the database and
     * then inform the reasoning engine to run cleanups.
     * <p/>
     * If a program with this name does not exist, does nothing
     *
     * @param name the unique name of the program to remove
     * @throws SailException
     */
    public void deleteProgram(String name) throws SailException {
        try {
            KiWiReasoningConnection connection = persistence.getConnection();
            try {
                Program p = connection.loadProgram(name);
                connection.deleteProgram(p);
                connection.commit();
            } finally {
                connection.close();
            }
        } catch (SQLException ex) {
            throw new SailException("cannot load program from database",ex);
        }
        engine.loadPrograms();
        engine.notifyRemoveRules();
    }


    /**
     * Clean all inferred triples and re-run all reasoning rules.
     */
    public void reRunPrograms() {
        engine.reRunPrograms();
    }

    /**
     * Return a reference to the underlying reasoning engine.
     * @return
     */
    public ReasoningEngine getEngine() {
        return engine;
    }

    /**
     * Return a reference to the underlying database persistence layer.
     * @return
     */
    public KiWiReasoningPersistence getPersistence() {
        return persistence;
    }

    /**
     * List the justifications for the triple with the id given as argument. For informational purposes.
     *
     * @param tripleId
     * @return
     * @throws SailException
     */
    public CloseableIteration<Justification,SailException> justify(long tripleId) throws SailException {
        try {
            final KiWiReasoningConnection connection = persistence.getConnection();

            return new ExceptionConvertingIteration<Justification, SailException>(connection.listJustificationsForTriple(tripleId)) {
                /**
                 * Converts an exception from the underlying iteration to an exception of
                 * type <tt>X</tt>.
                 */
                @Override
                protected SailException convert(Exception e) {
                    return new SailException(e);
                }

                @Override
                protected void handleClose() throws SailException {
                    super.handleClose();

                    try {
                        connection.commit();
                        connection.close();
                    } catch (SQLException ex) {
                        throw new SailException("database error while committing/closing connection");
                    }
                }
            };
        } catch (SQLException ex) {
            throw new SailException("cannot list programs in database",ex);
        }

    }
}
