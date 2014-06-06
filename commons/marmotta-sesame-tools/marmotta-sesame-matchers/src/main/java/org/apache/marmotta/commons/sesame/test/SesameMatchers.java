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
package org.apache.marmotta.commons.sesame.test;

import org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher;
import org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher;
import org.apache.marmotta.commons.sesame.test.connection.HasStatementMatcher;
import org.apache.marmotta.commons.sesame.test.sparql.SparqlAskMatcher;
import org.apache.marmotta.commons.sesame.test.sparql.SparqlGraphQueryMatcher;
import org.apache.marmotta.commons.sesame.test.sparql.SparqlTupleQueryMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * Collection of useful Matchers for Sesame.
 *
 * @author Jakob Frank
 */
public class SesameMatchers {

    /**
     * Create a RepositoryConnection Matcher checking if the provided Statement is contained in the Connection.
     *
     * @param subject the subject of the statement, use {@code null} as wildcard.
     * @param predicate the predicate of the statement, use {@code null} as wildcard.
     * @param object the object of the statement, use {@code null} as wildcard.
     * @param contexts the contexts in which to look for the statement, use an empty varargs array to look in all contexts available.
     *
     * @see org.apache.marmotta.commons.sesame.test.connection.HasStatementMatcher
     * @see org.openrdf.repository.RepositoryConnection#hasStatement(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, boolean, org.openrdf.model.Resource...)
     */
    public static <T extends RepositoryConnection> Matcher<T> hasStatement(Resource subject, URI predicate, Value object, Resource... contexts) {
        return HasStatementMatcher.hasStatement(subject, predicate, object, contexts);
    }

    /**
     * Create a Repository Matcher checking if the provided Statement is contained in the Repository.
     *
     * @param subject the subject of the statement, use {@code null} as wildcard.
     * @param predicate the predicate of the statement, use {@code null} as wildcard.
     * @param object the object of the statement, use {@code null} as wildcard.
     * @param contexts the contexts in which to look for the statement, use an empty varargs array to look in all contexts available.
     *
     * @see org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher
     * @see org.apache.marmotta.commons.sesame.test.connection.HasStatementMatcher
     * @see org.openrdf.repository.RepositoryConnection#hasStatement(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, boolean, org.openrdf.model.Resource...)
     */
    public static <T extends Repository> Matcher<T> repositoryHasStatement(Resource subject, URI predicate, Value object, Resource... contexts) {
        return RepositoryMatcher.wrap(hasStatement(subject, predicate, object,contexts));
    }

    public static <T extends String> Matcher<T> rdfHasStatement(RDFFormat format, String baseUri, Resource subject, URI predicate, Value object, Resource... contexts) {
        return RdfStringMatcher.wrap(format, baseUri, hasStatement(subject, predicate, object, contexts));
    }

    public static <T extends RepositoryConnection> Matcher<T> sparqlAsk(String askQuery) {
        return SparqlAskMatcher.sparqlAsk(askQuery);
    }

    public static <T extends RepositoryConnection> Matcher<T> sparqlAsk(String baseUri, String askQuery) {
        return SparqlAskMatcher.sparqlAsk(baseUri, askQuery);
    }

    public static <T extends Repository> Matcher<T> sparqlAskRepository(String askQuery) {
        return RepositoryMatcher.wrap(sparqlAsk(askQuery));
    }

    public static <T extends Repository> Matcher<T> sparqlAskRepository(String baseUri, String askQuery) {
        return RepositoryMatcher.wrap(sparqlAsk(baseUri, askQuery));
    }

    public static <T extends String> Matcher<T> sparqlAskRdf(RDFFormat format, String baseUri, String askQuery) {
        return RdfStringMatcher.wrap(format, baseUri, sparqlAsk(askQuery));
    }

    public static <T extends String> Matcher<T> sparqlAskRdf(RDFFormat format, String baseUri, String queryBaseUri, String askQuery) {
        return RdfStringMatcher.wrap(format, baseUri, sparqlAsk(queryBaseUri, askQuery));
    }

    public static <T extends RepositoryConnection> Matcher<T> sparqlTupleQuery(String baseUri, String sparqlQuery, Matcher<Iterable<BindingSet>> matcher) {
        return SparqlTupleQueryMatcher.sparqlQuery(baseUri, sparqlQuery, matcher);
    }

    @SafeVarargs
    public static <T extends RepositoryConnection> Matcher<T> sparqlTupleQuery(String baseUri, String sparqlQuery, Matcher<Iterable<BindingSet>>... matchers) {
        return SparqlTupleQueryMatcher.sparqlQuery(baseUri, sparqlQuery, matchers);
    }

    public static <T extends Repository> Matcher<T> sparqlTupleQueryRepository(String baseUri, String sparqlQuery, Matcher<Iterable<BindingSet>> matcher) {
        return RepositoryMatcher.wrap(SparqlTupleQueryMatcher.sparqlQuery(baseUri, sparqlQuery, matcher));
    }

    @SafeVarargs
    public static <T extends Repository> Matcher<T> sparqlTupleQueryRepository(String baseUri, String sparqlQuery, Matcher<Iterable<BindingSet>>... matchers) {
        return RepositoryMatcher.wrap(SparqlTupleQueryMatcher.sparqlQuery(baseUri, sparqlQuery, matchers));
    }

    public static <T extends String> Matcher<T> sparqlTupleQueryRdf(RDFFormat format, String baseUri, String queryBaseUri, String sparqlQuery, Matcher<Iterable<BindingSet>> matcher) {
        return RdfStringMatcher.wrap(format, baseUri, SparqlTupleQueryMatcher.sparqlQuery(queryBaseUri, sparqlQuery, matcher));
    }

    @SafeVarargs
    public static <T extends String> Matcher<T> sparqlTupleQueryRdf(RDFFormat format, String baseUri, String queryBaseUri, String sparqlQuery, Matcher<Iterable<BindingSet>>... matchers) {
        return RdfStringMatcher.wrap(format, baseUri, SparqlTupleQueryMatcher.sparqlQuery(queryBaseUri, sparqlQuery, matchers));
    }

    public static <T extends RepositoryConnection> Matcher<T> sparqlGraphQuery(String baseUri, String sparqlQuery, Matcher<? extends RepositoryConnection> matcher) {
        return SparqlGraphQueryMatcher.<T>sparqlGraphQuery(baseUri, sparqlQuery, matcher);
    }

    @SafeVarargs
    public static <T extends RepositoryConnection, V extends RepositoryConnection> Matcher<T> sparqlGraphQuery(String baseUri, String sparqlQuery, Matcher<V>... matchers) {
        return SparqlGraphQueryMatcher.<T>sparqlGraphQuery(baseUri, sparqlQuery, CoreMatchers.allOf(matchers));
    }

    public static <T extends Repository, V extends RepositoryConnection> Matcher<T> sparqlGraphQueryRepository(String baseUri, String sparqlQuery, Matcher<V> matcher) {
        return RepositoryMatcher.wrap(sparqlGraphQuery(baseUri, sparqlQuery, matcher));
    }

    @SafeVarargs
    public static <T extends Repository, V extends RepositoryConnection> Matcher<T> sparqlGraphQueryRepository(String baseUri, String sparqlQuery, Matcher<V>... matchers) {
        return RepositoryMatcher.wrap(sparqlGraphQuery(baseUri, sparqlQuery, matchers));
    }

    public static <T extends String, V extends RepositoryConnection> Matcher<T> sparqlGraphQueryRdf(RDFFormat format, String baseUri, String queryBaseUri, String sparqlQuery, Matcher<V> matcher) {
        return RdfStringMatcher.wrap(format, baseUri, sparqlGraphQuery(queryBaseUri, sparqlQuery, matcher));
    }

    @SafeVarargs
    public static <T extends String, V extends RepositoryConnection> Matcher<T> sparqlGraphQueryRdf(RDFFormat format, String baseUri, String queryBaseUri, String sparqlQuery, Matcher<V>... matchers) {
        return RdfStringMatcher.wrap(format, baseUri, sparqlGraphQuery(queryBaseUri, sparqlQuery, matchers));
    }

    public static <T extends Repository, V extends RepositoryConnection> Matcher<T> repositoryMatches(Matcher<V> matcher) {
        return RepositoryMatcher.wrap(matcher);
    }

    public static <T extends Repository, V extends RepositoryConnection> Matcher<T> repositoryMatches(Matcher<V> matcher1, Matcher<V> matcher2) {
        return RepositoryMatcher.wrap(CoreMatchers.allOf(matcher1, matcher2));
    }

    @SafeVarargs
    public static <T extends Repository, V extends RepositoryConnection> Matcher<T> repositoryMatches(Matcher<V>... matchers) {
        return RepositoryMatcher.wrap(CoreMatchers.allOf(matchers));
    }

    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(RDFFormat format, String baseUri, Matcher<V> matcher) {
        return  RdfStringMatcher.wrap(format, baseUri, matcher);
    }

    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(RDFFormat format, String baseUri, Matcher<V> matcher1, Matcher<V> matcher2) {
        return  RdfStringMatcher.wrap(format, baseUri, CoreMatchers.allOf(matcher1, matcher2));
    }

    @SafeVarargs
    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(RDFFormat format, String baseUri, Matcher<V>... matchers) {
        return  RdfStringMatcher.wrap(format, baseUri, CoreMatchers.allOf(matchers));
    }

    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(String mimeType, String baseUri, Matcher<V> matcher) {
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType);
        if (format == null) throw new UnsupportedRDFormatException(mimeType);
        return  RdfStringMatcher.wrap(format, baseUri, matcher);
    }

    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(String mimeType, String baseUri, Matcher<V> matcher1, Matcher<V> matcher2) {
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType);
        if (format == null) throw new UnsupportedRDFormatException(mimeType);
        return  RdfStringMatcher.wrap(format, baseUri, CoreMatchers.allOf(matcher1, matcher2));
    }

    @SafeVarargs
    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(String mimeType, String baseUri, Matcher<V>... matchers) {
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType);
        if (format == null) throw new UnsupportedRDFormatException(mimeType);
        return  RdfStringMatcher.wrap(format, baseUri, CoreMatchers.allOf(matchers));
    }

    private SesameMatchers() {
        // static access only.
    }

}
