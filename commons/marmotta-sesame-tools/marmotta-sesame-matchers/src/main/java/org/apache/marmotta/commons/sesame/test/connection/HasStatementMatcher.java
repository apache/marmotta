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
package org.apache.marmotta.commons.sesame.test.connection;

import org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Has Statement Matcher
 */
public class HasStatementMatcher<T extends RepositoryConnection> extends AbstractRepositoryConnectionMatcher<T> {

    private final Resource subject;
    private final IRI predicate;
    private final Value object;
    private final Resource[] contexts;
    private final boolean includeInferred;

    /**
     * Create a RepositoryConnection Matcher checking if the provided Statement is contained in the Connection.
     *
     * @param subject   the subject of the statement, use {@code null} as wildcard.
     * @param predicate the predicate of the statement, use {@code null} as wildcard.
     * @param object    the object of the statement, use {@code null} as wildcard.
     * @param contexts  the contexts in which to look for the statement, use an empty varargs array to look in all contexts available.
     * @see org.apache.marmotta.commons.sesame.test.connection.HasStatementMatcher
     * @see org.eclipse.rdf4j.repository.RepositoryConnection#hasStatement(org.eclipse.rdf4j.model.Resource, org.eclipse.rdf4j.model.URI, org.eclipse.rdf4j.model.Value, boolean, org.eclipse.rdf4j.model.Resource...)
     */
    public HasStatementMatcher(Resource subject, IRI predicate, Value object, Resource... contexts) {
        this(subject, predicate, object, true, contexts);
    }

    /**
     * Create a RepositoryConnection Matcher checking if the provided Statement is contained in the Connection.
     *
     * @param subject          the subject of the statement, use {@code null} as wildcard.
     * @param predicate        the predicate of the statement, use {@code null} as wildcard.
     * @param object           the object of the statement, use {@code null} as wildcard.
     * @param includeInferrred if false, no inferred statements are considered; if true, inferred statements are considered if available
     * @param contexts         the contexts in which to look for the statement, use an empty varargs array to look in all contexts available.
     * @see org.apache.marmotta.commons.sesame.test.connection.HasStatementMatcher
     * @see org.eclipse.rdf4j.repository.RepositoryConnection#hasStatement(org.eclipse.rdf4j.model.Resource, org.eclipse.rdf4j.model.URI, org.eclipse.rdf4j.model.Value, boolean, org.eclipse.rdf4j.model.Resource...)
     */
    public HasStatementMatcher(Resource subject, IRI predicate, Value object, boolean includeInferrred, Resource... contexts) {
        super();
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.includeInferred = includeInferrred;
        this.contexts = contexts;
    }

    @Override
    protected boolean matchesConnection(RepositoryConnection con) throws RepositoryException {
        return con.hasStatement(subject, predicate, object, includeInferred, contexts);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("\n to contain Statement(")
                .appendValue(subject).appendText(" ")
                .appendValue(predicate).appendText(" ")
                .appendValue(object).appendText(")");
    }

    /**
     * Create a RepositoryConnection Matcher checking if the provided Statement is contained in the Connection.
     *
     * @param subject   the subject of the statement, use {@code null} as wildcard.
     * @param predicate the predicate of the statement, use {@code null} as wildcard.
     * @param object    the object of the statement, use {@code null} as wildcard.
     * @param contexts  the contexts in which to look for the statement, use an empty varargs array to look in all contexts available.
     * @see org.apache.marmotta.commons.sesame.test.connection.HasStatementMatcher
     * @see org.eclipse.rdf4j.repository.RepositoryConnection#hasStatement(org.eclipse.rdf4j.model.Resource, org.eclipse.rdf4j.model.URI, org.eclipse.rdf4j.model.Value, boolean, org.eclipse.rdf4j.model.Resource...)
     */
    public static <T extends RepositoryConnection> Matcher<T> hasStatement(Resource subject, IRI predicate, Value object, Resource... contexts) {
        return new HasStatementMatcher<>(subject, predicate, object, contexts);
    }
}
