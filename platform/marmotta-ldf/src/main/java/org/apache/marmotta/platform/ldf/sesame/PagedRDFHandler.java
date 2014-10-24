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
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Paginates statements before sending them to the delegated RDFHandler.
 * (TODO: find a more performance solution)
 *
 * @author Sergio Fernández
 */
public class PagedRDFHandler implements RDFHandler {

    private List<Statement> statements;
    private RDFHandler handler;
    private int offset;
    private int limit;

    /**
     * Constructs a PagedRDFHandler with a delegate handler
     *
     * @param handler The handler to delegate the calls to
     */
    public PagedRDFHandler(RDFHandler handler, int offset, int limit) {
        super();
        this.statements = new ArrayList<>();
        this.handler = handler;
        this.offset = offset;
        this.limit = limit;
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
        List<Statement> filteredStatements = Lists.newArrayList(Iterables.limit(statements, 20));

        //send statements to delegate writer
        for (Statement statement : filteredStatements) {
            handler.handleStatement(statement);
        }

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
