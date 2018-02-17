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
package org.apache.marmotta.loader.core.test.dummy;

import org.apache.marmotta.loader.api.LoaderHandler;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class DummyLoaderHandler extends AbstractRDFHandler implements LoaderHandler {

    private final long sleep;
    private final Model model;

    public DummyLoaderHandler() {
        this(0);
    }

    public DummyLoaderHandler(long methodSleep) {
        model = new TreeModel();
        sleep = methodSleep;
    }

    public Model getModel() {
        return model;
    }

    /**
     * Initialise the handler, performing any initialisation steps that are necessary before bulk importing can
     * start (e.g. dropping indexes or establishing a connection).
     *
     * @throws org.eclipse.rdf4j.rio.RDFHandlerException
     */
    @Override
    public void initialise() throws RDFHandlerException {

    }

    /**
     * Perform cleanup on shutdown, e.g. re-creating indexes after import completed or freeing resources acquired by
     * the handler.
     */
    @Override
    public void shutdown() throws RDFHandlerException {

    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        model.add(st);
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                // ignored
            }
        }
    }
}
