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

package org.apache.marmotta.commons.sesame.model;

import info.aduna.iteration.CloseableIteration;
import javolution.util.function.Predicate;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Utilities for working with Sesame Models
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ModelCommons {

    private static Logger log = LoggerFactory.getLogger(ModelCommons.class);

    /**
     * Create an instance of RDFHandler using the model passed as parameter as underlying triple store.
     * @param model  the model to wrap in an RDFHandler
     * @param filters an optional list of filters; if any of the filters rejects the statement it is not added
     * @return
     */
    public static RDFHandler createModelHandler(final Model model, final Predicate<Statement>... filters) {
        return new RDFHandlerBase() {
            @Override
            public void handleStatement(Statement st) throws RDFHandlerException {
                for(Predicate<Statement> f : filters) {
                    if(!f.test(st)) {
                        return;
                    }
                }
                model.add(st);
            }
        };
    }

    /**
     * Add statements from the given input stream to the given model. Similar to RepositoryConnection.add, just works
     * directly on a model.
     *
     * @param model   the model to add the statements to
     * @param in      input stream to read the statements from
     * @param baseURI base URI to resolve relative URIs
     * @param format  RDF format of the data in the input stream
     * @param filters an optional list of filters; if any of the filters rejects the statement it is not added
     * @throws IOException
     * @throws RDFParseException
     */
    public static void add(Model model, InputStream in, String baseURI, RDFFormat format, Predicate<Statement>... filters) throws IOException, RDFParseException {
        try {

            RDFParser parser = Rio.createParser(format);
            parser.setRDFHandler(createModelHandler(model, filters));
            parser.parse(in, baseURI);

        } catch (RDFHandlerException e) {
            log.error("RepositoryException:", e);
        }

    }


    /**
     * Add statements from the given reader to the given model. Similar to RepositoryConnection.add, just works
     * directly on a model.
     *
     * @param model   the model to add the statements to
     * @param in      reader to read the statements from
     * @param baseURI base URI to resolve relative URIs
     * @param format  RDF format of the data in the reader
     * @param filters an optional list of filters; if any of the filters rejects the statement it is not added
     * @throws IOException
     * @throws RDFParseException
     */
    public static void add(Model model, Reader in, String baseURI, RDFFormat format, Predicate<Statement>... filters) throws IOException, RDFParseException {
        try {

            RDFParser parser = Rio.createParser(format);
            parser.setRDFHandler(createModelHandler(model, filters));
            parser.parse(in, baseURI);

        } catch (RDFHandlerException e) {
            log.error("RepositoryException:", e);
        }

    }


    /**
     * Add statements from the given statement iteration to the given model.
     *
     * @param model   the model to add the statements to
     * @param triples a closeable iteration of triples to add (e.g. a RepositoryResult<Statement>)
     * @param filters an optional list of filters; if any of the filters rejects the statement it is not added
     * @throws IOException
     * @throws RDFParseException
     */
    public static <X extends Exception> void add(Model model, CloseableIteration<? extends Statement,X> triples, Predicate<Statement>... filters) throws X {
        try {
            rloop: while(triples.hasNext()) {
                Statement st = triples.next();

                for(Predicate<Statement> f : filters) {
                    if(!f.test(st)) {
                        continue rloop;
                    }
                }

                model.add(st);
            }
        } finally {
            triples.close();
        }
    }



    /**
     * Export all triples in the model passed as argument to the RDF handler passed as second argument. Similar to
     * RepositoryConnection.export.
     *
     * @param model
     * @param handler
     * @throws RDFHandlerException
     */
    public static void export(Model model, RDFHandler handler) throws RDFHandlerException {
        handler.startRDF();
        for(Statement stmt : model) {
            handler.handleStatement(stmt);
        }
        handler.endRDF();
    }


    /**
     * Copy the contents of the model over to a newly initialised in-memory repository.
     *
     * @param model the model to wrap in a memory repository
     * @return the memory repository
     */
    public static Repository asRepository(Model model) throws RepositoryException {
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();

            con.add(model);

            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }

        return repository;

    }


    /**
     * Copy the contents of the given repository over to a newly created model, optionally applying the filters given
     * as variable argument.
     *
     * @param repository
     * @return
     * @throws RepositoryException
     */
    public static Model asModel(Repository repository, Predicate<Statement>... filters) throws RepositoryException {
        Model model = new TreeModel();

        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();

            add(model,con.getStatements(null,null,null,true), filters);

            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }

        return model;
    }

}
