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
package org.apache.marmotta.platform.reasoner.services;

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.api.triplestore.TransactionalSailProvider;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.kiwi.reasoner.engine.ReasoningConfiguration;
import org.apache.marmotta.kiwi.reasoner.model.program.Justification;
import org.apache.marmotta.kiwi.reasoner.model.program.Program;
import org.apache.marmotta.kiwi.reasoner.parser.ParseException;
import org.apache.marmotta.kiwi.reasoner.sail.KiWiReasoningSail;
import org.apache.marmotta.kiwi.transactions.api.TransactionalSail;
import org.apache.marmotta.kiwi.transactions.wrapper.TransactionalSailWrapper;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class ReasoningSailProvider implements TransactionalSailProvider {

    public static final String REASONING_ENABLED = "reasoning.enabled";
    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    private KiWiReasoningSail sail;

    /**
     * Return the name of the provider. Used e.g. for displaying status information or logging.
     *
     * @return
     */
    @Override
    public String getName() {
        return "KWRL Reasoner";
    }

    /**
     * Create the sail wrapper provided by this SailProvider
     *
     * @param parent the parent sail to wrap by the provider
     * @return the wrapped sail
     */
    @Override
    public TransactionalSailWrapper createSail(TransactionalSail parent) {
        ReasoningConfiguration config = new ReasoningConfiguration();
        config.setBatchSize(configurationService.getIntConfiguration("reasoning.batchsize",1000));
        config.setRemoveDuplicateJustifications(configurationService.getBooleanConfiguration("reasoning.remove_duplicate_justifications",false));

        sail = new KiWiReasoningSail(parent,config);

        return sail;
    }

    /**
     * Return true if this sail provider is enabled in the configuration.
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return configurationService.getBooleanConfiguration(REASONING_ENABLED,true);
    }


    public void configurationChanged(@Observes ConfigurationChangedEvent e) {
        if(e.containsChangedKey(REASONING_ENABLED)) {
            sesameService.restart();
        } else if(e.containsChangedKeyWithPrefix("reasoning")) {
            ReasoningConfiguration config = sail.getConfig();
            config.setBatchSize(configurationService.getIntConfiguration("reasoning.batchsize",1000));
            config.setRemoveDuplicateJustifications(configurationService.getBooleanConfiguration("reasoning.remove_duplicate_justifications",false));
        }
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
     * @throws java.io.IOException    in case the stream cannot be read
     * @throws org.openrdf.sail.SailException  in case the program already exists
     * @throws org.apache.marmotta.kiwi.reasoner.parser.ParseException in case the program cannot be parsed
     */
    public void addProgram(String name, InputStream data) throws IOException, SailException, ParseException {
        sail.addProgram(name, data);
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
     * @throws org.openrdf.sail.SailException  in case the program already exists
     */
    public void addProgram(Program program) throws SailException {
        sail.addProgram(program);
    }

    /**
     * Remove the program with the given name. This method will first remove the program from the database and
     * then inform the reasoning engine to run cleanups.
     * <p/>
     * If a program with this name does not exist, does nothing
     *
     * @param name the unique name of the program to remove
     * @throws org.openrdf.sail.SailException
     */
    public void deleteProgram(String name) throws SailException {
        sail.deleteProgram(name);
    }

    /**
     * Return the program with the given name. In case the program does not exist, the method will
     * return null.
     *
     * @param name the unique name of the program to retrieve
     * @return the parsed program, or null in case a program with the given name does not exist
     * @throws org.openrdf.sail.SailException  in case an error occurs
     */
    public Program getProgram(String name) throws SailException {
        return sail.getProgram(name);
    }

    /**
     * List all reasoning programs currently stored in the triplestore.
     *
     * @return
     */
    public CloseableIteration<Program, SailException> listPrograms() throws SailException {
        return sail.listPrograms();
    }

    /**
     * Clean all inferred triples and re-run all reasoning rules.
     */
    public void reRunPrograms() {
        sail.reRunPrograms();
    }

    /**
     * Update the program with the name given as argument using the data provided in the stream.
     * This method will first calculate the difference between the
     * previous version of the program and the new version of the program. It then updates the program in
     * the database and notifies the engine of all removed and added rules.
     *
     * @throws java.io.IOException    in case the stream cannot be read
     * @throws org.openrdf.sail.SailException  in case the program already exists
     * @throws org.apache.marmotta.kiwi.reasoner.parser.ParseException in case the program cannot be parsed
     */
    public void updateProgram(String name, InputStream data) throws IOException, SailException, ParseException {
        sail.updateProgram(name, data);
    }

    /**
     * Update the program given as argument. This method will first calculate the difference between the
     * previous version of the program and the new version of the program. It then updates the program in
     * the database and notifies the engine of all removed and added rules.
     *
     * @param program  the updated version of the program
     * @throws org.openrdf.sail.SailException in case a database error occurs
     */
    public void updateProgram(Program program) throws SailException {
        sail.updateProgram(program);
    }

    /**
     * List the justifications for the triple with the id given as argument. For informational purposes.
     *
     * @param tripleId
     * @return
     * @throws org.openrdf.sail.SailException
     */
    public CloseableIteration<Justification, SailException> justify(long tripleId) throws SailException {
        return sail.justify(tripleId);
    }
}
