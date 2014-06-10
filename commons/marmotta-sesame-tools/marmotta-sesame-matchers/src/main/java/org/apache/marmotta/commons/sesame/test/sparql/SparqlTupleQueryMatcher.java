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

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.List;

/**
 * Match against Sparql Tuple Query.
 */
public class SparqlTupleQueryMatcher<T extends RepositoryConnection> extends SparqlMatcher<T> {
    private final Matcher<Iterable<BindingSet>> matcher;

    protected SparqlTupleQueryMatcher(String baseUri, String query, Matcher<Iterable<BindingSet>> matcher) {
        super(baseUri, query);
        this.matcher = matcher;
    }

    @Override
    protected boolean matchesSPARQL(RepositoryConnection con) throws RepositoryException, QueryEvaluationException, MalformedQueryException {
        final TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query, baseUri);
        final TupleQueryResult result = tupleQuery.evaluate();
        final List<BindingSet> bindingSets = QueryResults.asList(result);

        // FIXME: test this!
        return matcher.matches(bindingSets);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(" TupleQuery ").appendValue(query).appendText(" to ").appendDescriptionOf(matcher);
    }

    public static <T extends RepositoryConnection> Matcher<T> sparqlQuery(String baseUri, String query, Matcher<Iterable<BindingSet>> matcher) {
        return new SparqlTupleQueryMatcher<T>(baseUri, query, matcher);
    }

    @SafeVarargs
    public static <T extends RepositoryConnection> Matcher<T> sparqlQuery(String baseUri, String query, Matcher<Iterable<BindingSet>>... matchers) {
        return new SparqlTupleQueryMatcher<T>(baseUri, query, CoreMatchers.allOf(matchers));
    }

}
