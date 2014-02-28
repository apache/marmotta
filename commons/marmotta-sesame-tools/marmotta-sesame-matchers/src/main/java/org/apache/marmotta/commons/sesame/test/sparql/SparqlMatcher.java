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
package org.apache.marmotta.commons.sesame.test.sparql;

import org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * SPARQL Matcher
 */
public abstract class SparqlMatcher<T extends RepositoryConnection> extends AbstractRepositoryConnectionMatcher<T> {

    protected final String baseUri;
    protected final String query;

    protected SparqlMatcher(String baseUri, String query) {
        super();

        if (query == null) throw new IllegalArgumentException("query must not be null");

        this.baseUri = baseUri;
        this.query = query;
    }

    @Override
    protected final boolean matchesConnection(RepositoryConnection connection) throws RepositoryException {
        try {
            return matchesSPARQL(connection);
        } catch (MalformedQueryException e) {
            throw new IllegalArgumentException("Invalid SPARQL Query: " + query, e);
        } catch (QueryEvaluationException e) {
            throw new RepositoryException(e);
        }
    }

    protected abstract boolean matchesSPARQL(RepositoryConnection connection) throws MalformedQueryException, RepositoryException, QueryEvaluationException;
}
