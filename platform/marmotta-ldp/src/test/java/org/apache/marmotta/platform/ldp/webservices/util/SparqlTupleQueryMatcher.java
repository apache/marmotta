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
package org.apache.marmotta.platform.ldp.webservices.util;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.Collection;
import java.util.List;

/**
 * Match against Sparql Tuple Query.
 */
public class SparqlTupleQueryMatcher extends SparqlMatcher {
    private final Matcher<Collection<BindingSet>> matcher;

    protected SparqlTupleQueryMatcher(String baseUri, String mimeType, String query, Matcher<Collection<BindingSet>> matcher) {
        super(baseUri, mimeType, query);
        this.matcher = matcher;
    }

    @Override
    protected boolean matches(RepositoryConnection con) throws RepositoryException, QueryEvaluationException {
        try {
            final TupleQuery tupleQuery;
            tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query, baseUri);
            final TupleQueryResult result = tupleQuery.evaluate();
            final List<BindingSet> bindingSets = QueryResults.asList(result);

            return matcher.matches(bindingSets);
        } catch (MalformedQueryException e) {
            throw new IllegalArgumentException("Invalid SPARQL Query: " + query, e);
        }
    }

    public static SparqlTupleQueryMatcher sparqlQuery(String mime, String baseUri, String query, Matcher<Collection<BindingSet>> matcher) {
        return new SparqlTupleQueryMatcher(mime, baseUri, query, matcher);
    }

    @SafeVarargs
    public static SparqlTupleQueryMatcher sparqlQuery(String mime, String baseUri, String query, Matcher<Collection<BindingSet>>... matchers) {
        return new SparqlTupleQueryMatcher(mime, baseUri, query, CoreMatchers.allOf(matchers));
    }

}
