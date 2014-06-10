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
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Sparql ASK-Query Matcher
 */
public class SparqlAskMatcher<T extends RepositoryConnection> extends SparqlMatcher<T> {
    protected SparqlAskMatcher(String baseUri, String query) {
        super(baseUri, query);
    }

    @Override
    protected boolean matchesSPARQL(RepositoryConnection con) throws MalformedQueryException, RepositoryException, QueryEvaluationException {
        final BooleanQuery booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, query, baseUri);

        return booleanQuery.evaluate();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Query ").appendValue(query).appendText(" to match");
    }

    /**
     * Create a SparqlAskMatcher that evaluates a SPARQL ASK query against the {@link org.openrdf.repository.RepositoryConnection}
     * to test.
     *
     * @param baseUri  The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param askQuery the SPARQL ASK query.
     * @see org.openrdf.query.BooleanQuery#evaluate()
     */
    public static <T extends RepositoryConnection> Matcher<T> sparqlAsk(String baseUri, String askQuery) {
        return new SparqlAskMatcher<T>(baseUri, askQuery);
    }

    /**
     * Create a SparqlAskMatcher that evaluates a SPARQL ASK query against the {@link org.openrdf.repository.RepositoryConnection}
     * to test. The baseUri of the SPARQL Query is assumed {@code null}.
     *
     * @param askQuery the SPARQL ASK query.
     * @see #sparqlAsk(String, String)
     * @see org.openrdf.query.BooleanQuery#evaluate()
     */
    public static <T extends RepositoryConnection> Matcher<T> sparqlAsk(String askQuery) {
        return sparqlAsk(null, askQuery);
    }


}
