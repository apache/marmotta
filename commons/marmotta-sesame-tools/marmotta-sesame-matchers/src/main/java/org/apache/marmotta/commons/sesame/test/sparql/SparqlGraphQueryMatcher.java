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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 */
public class SparqlGraphQueryMatcher<T extends RepositoryConnection> extends SparqlMatcher<T> {
    private final Matcher<? extends RepositoryConnection> matcher;

    protected SparqlGraphQueryMatcher(String baseUri, String query, Matcher<? extends RepositoryConnection> matcher) {
        super(baseUri, query);
        this.matcher = matcher;
    }

    @Override
    protected boolean matchesSPARQL(RepositoryConnection connection) throws MalformedQueryException, RepositoryException, QueryEvaluationException {
        final GraphQuery graphQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, query, baseUri);

        final Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        try {
            final RepositoryConnection connection2 = repo.getConnection();
            try {
                connection2.begin();
                final GraphQueryResult graph = graphQuery.evaluate();
                try {
                    connection2.add(graph);
                } finally {
                    graph.close();
                }
                connection2.commit();

                connection2.begin();
                final boolean result = matcher.matches(connection2);
                connection2.commit();
                return result;
            } finally {
                connection2.close();
            }
        } finally {
            repo.shutDown();
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(" Graph ").appendValue(query).appendText(" ").appendDescriptionOf(matcher);
    }

    public static <T extends RepositoryConnection> Matcher<T> sparqlGraphQuery(String baseUri, String query, Matcher<? extends RepositoryConnection> matcher) {
        return new SparqlGraphQueryMatcher<T>(baseUri, query, matcher);
    }
}
