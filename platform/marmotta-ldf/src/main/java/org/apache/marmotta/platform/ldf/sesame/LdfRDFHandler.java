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
package org.apache.marmotta.platform.ldf.sesame;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.platform.ldf.api.LdfService;
import org.apache.marmotta.platform.ldf.vocab.HYDRA;
import org.apache.marmotta.platform.ldf.vocab.VOID;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Specialized statements handler doing some LDF specific thing,
 * such as paging or metadata generation, before sending them to
 * the delegated RDFHandler.
 *
 * (TODO: find a more performance solution)
 *
 * @author Sergio Fernández
 */
public class LdfRDFHandler implements RDFHandler {

    private List<Statement> statements;
    private final RDFHandler handler;
    private final Resource context;
    private final int page;

    /**
     * Constructs a PagedRDFHandler with a delegate handler
     *
     * @param handler The handler to delegate the calls to
     */
    public LdfRDFHandler(RDFHandler handler) {
        this(handler, null, 1);
    }

    /**
     * Constructs a PagedRDFHandler with a delegate handler
     *
     * @param handler The handler to delegate the calls to
     * @param page number of page (starting with 1)
     */
    public LdfRDFHandler(RDFHandler handler, int page) {
        this(handler, null, page);
    }

    /**
     * Constructs a PagedRDFHandler with a delegate handler
     *
     * @param handler The handler to delegate the calls to
     * @param context dataset
     */
    public LdfRDFHandler(RDFHandler handler, Resource context) {
        this(handler, context, 1);
    }

    /**
     * Constructs a PagedRDFHandler with a delegate handler
     *
     * @param handler The handler to delegate the calls to
     * @param context dataset
     * @param page number of page (starting with 1)
     */
    public LdfRDFHandler(RDFHandler handler, Resource context, int page) {
        super();
        this.statements = new ArrayList<>();
        this.handler = handler;
        this.context = context;
        this.page = page;
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        handler.startRDF();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        //first order by a fixed criteria
        Collections.sort(statements, new Comparator<Statement>() {
            @Override
            public int compare(Statement s1, Statement s2) {
                int subjectComparison = s1.getSubject().stringValue().compareTo(s2.getSubject().stringValue());
                int predicatedComparison = s1.getPredicate().stringValue().compareTo(s2.getPredicate().stringValue());
                if (subjectComparison != 0) {
                    return subjectComparison;
                } else if (predicatedComparison != 0) {
                    return predicatedComparison;
                } else if((s1.getObject() instanceof Literal) && (s2.getObject() instanceof Resource)) {
                    return 1;
                } else if((s1.getObject() instanceof Resource) && (s2.getObject() instanceof Literal)) {
                    return -1;
                } else {
                    return s1.getObject().stringValue().compareTo(s2.getObject().stringValue());
                }
            }
        });

        //then filter
        final int size = statements.size();
        final int offset = LdfService.PAGE_SIZE * (page - 1);
        if (offset > size) {
            throw new RDFHandlerException("page " + page + " can't be generated");
        }
        final int limit = LdfService.PAGE_SIZE < size-offset ? LdfService.PAGE_SIZE : size-offset;
        List<Statement> filteredStatements = statements.subList(offset, limit);

        //send statements to delegate writer
        for (Statement statement : filteredStatements) {
            handler.handleStatement(statement);
        }

        //add ldf metadata
        final ValueFactoryImpl vf = new ValueFactoryImpl();

        Resource dataset = this.context != null ? this.context : vf.createBNode();
        handler.handleStatement(new StatementImpl(dataset, RDF.TYPE, VOID.Dataset));
        handler.handleStatement(new StatementImpl(dataset, RDF.TYPE, HYDRA.Collection));

        Resource fragment = vf.createBNode(); //TODO
        handler.handleStatement(new StatementImpl(dataset, VOID.subset, fragment));
        handler.handleStatement(new StatementImpl(fragment, RDF.TYPE, HYDRA.Collection));
        if (offset != 0 && limit != size) handler.handleStatement(new StatementImpl(fragment, RDF.TYPE, HYDRA.PagedCollection));
        handler.handleStatement(new StatementImpl(fragment, VOID.triples, vf.createLiteral(Integer.toString(filteredStatements.size()), XSD.Integer)));
        handler.handleStatement(new StatementImpl(fragment, HYDRA.totalItems, vf.createLiteral(Integer.toString(filteredStatements.size()), XSD.Integer)));
        handler.handleStatement(new StatementImpl(fragment, HYDRA.itemsPerPage, vf.createLiteral(Integer.toString(LdfService.PAGE_SIZE), XSD.Integer)));
        //TODO: HYDRA_FIRSTPAGE, HYDRA_PREVIOUSPAGE, HYDRA_NEXTPAGE

        //TODO: hydra controls

        //and actually end the rdf
        handler.endRDF();
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        handler.handleNamespace(prefix, uri);
    }

    @Override
    public void handleStatement(Statement statement) throws RDFHandlerException {
        statements.add(statement);
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        handler.handleComment(comment);
    }

}
