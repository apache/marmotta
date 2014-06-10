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
     * @param subject   the subject of the statement, use {@code null} as wildcard.
     * @param predicate the predicate of the statement, use {@code null} as wildcard.
     * @param object    the object of the statement, use {@code null} as wildcard.
     * @param contexts  the contexts in which to look for the statement, use an empty varargs array to look in all contexts available.
     * @see org.apache.marmotta.commons.sesame.test.connection.HasStatementMatcher
     * @see org.openrdf.repository.RepositoryConnection#hasStatement(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, boolean, org.openrdf.model.Resource...)
     */
    public static <T extends RepositoryConnection> Matcher<T> hasStatement(Resource subject, URI predicate, Value object, Resource... contexts) {
        return HasStatementMatcher.hasStatement(subject, predicate, object, contexts);
    }

    /**
     * Create a Repository Matcher checking if the provided Statement is contained in the Repository.
     *
     * @param subject   the subject of the statement, use {@code null} as wildcard.
     * @param predicate the predicate of the statement, use {@code null} as wildcard.
     * @param object    the object of the statement, use {@code null} as wildcard.
     * @param contexts  the contexts in which to look for the statement, use an empty varargs array to look in all contexts available.
     * @see org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher
     * @see org.apache.marmotta.commons.sesame.test.connection.HasStatementMatcher
     * @see org.openrdf.repository.RepositoryConnection#hasStatement(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, boolean, org.openrdf.model.Resource...)
     */
    public static <T extends Repository> Matcher<T> repositoryHasStatement(Resource subject, URI predicate, Value object, Resource... contexts) {
        return RepositoryMatcher.wrap(hasStatement(subject, predicate, object, contexts));
    }

    /**
     * Create a String Matcher that checks for the given RDF Statement in the serialized RDF dump
     *
     * @param format    the RDFFormat used for de-serializing the RDF
     * @param baseUri   the baseUri used for de-serializing the RDF
     * @param subject   the subject of the statement, use {@code null} as wildcard.
     * @param predicate the predicate of the statement, use {@code null} as wildcard.
     * @param object    the object of the statement, use {@code null} as wildcard.
     * @param contexts  the contexts in which to look for the statement, use an empty varargs array to look in all contexts available.
     * @see org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher
     * @see org.apache.marmotta.commons.sesame.test.connection.HasStatementMatcher
     */
    public static <T extends String> Matcher<T> rdfHasStatement(RDFFormat format, String baseUri, Resource subject, URI predicate, Value object, Resource... contexts) {
        return RdfStringMatcher.wrap(format, baseUri, hasStatement(subject, predicate, object, contexts));
    }

    /**
     * Create a RepositoryConnection Matcher that evaluates a SPARQL ASK query.
     *
     * @param askQuery the SPARQL ASK query to check, test will fail if the result is {@code false}
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlAskMatcher
     * @see org.openrdf.query.BooleanQuery#evaluate()
     */
    public static <T extends RepositoryConnection> Matcher<T> sparqlAsk(String askQuery) {
        return SparqlAskMatcher.sparqlAsk(askQuery);
    }

    /**
     * Create a RepositoryConnection Matcher that evaluates a SPARQL ASK query.
     *
     * @param baseUri  The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param askQuery the SPARQL ASK query to check, test will fail if the result is {@code false}
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlAskMatcher
     * @see org.openrdf.query.BooleanQuery#evaluate()
     */
    public static <T extends RepositoryConnection> Matcher<T> sparqlAsk(String baseUri, String askQuery) {
        return SparqlAskMatcher.sparqlAsk(baseUri, askQuery);
    }

    /**
     * Create a Repository Matcher that evaluates a SPARQL ASK query.
     *
     * @param askQuery the SPARQL ASK query to check, test will fail if the result is {@code false}
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlAskMatcher
     * @see org.openrdf.query.BooleanQuery#evaluate()
     * @see org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher#wrap(org.hamcrest.Matcher)
     */
    public static <T extends Repository> Matcher<T> sparqlAskRepository(String askQuery) {
        return RepositoryMatcher.wrap(sparqlAsk(askQuery));
    }

    /**
     * Create a RepositoryConnection Matcher that evaluates a SPARQL ASK query.
     *
     * @param baseUri  The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param askQuery the SPARQL ASK query to check, test will fail if the result is {@code false}
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlAskMatcher
     * @see org.openrdf.query.BooleanQuery#evaluate()
     * @see org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher#wrap(org.hamcrest.Matcher)
     */
    public static <T extends Repository> Matcher<T> sparqlAskRepository(String baseUri, String askQuery) {
        return RepositoryMatcher.wrap(sparqlAsk(baseUri, askQuery));
    }

    /**
     * Create a RdfString Matcher that evaluates a SPARQL ASK query.
     *
     * @param format   the RDFFormat used for de-serializing the RDF
     * @param baseUri  the baseUri used for de-serializing the RDF
     * @param askQuery the SPARQL ASK query to check, test will fail if the result is {@code false}
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlAskMatcher
     * @see org.openrdf.query.BooleanQuery#evaluate()
     */
    public static <T extends String> Matcher<T> sparqlAskRdf(RDFFormat format, String baseUri, String askQuery) {
        return RdfStringMatcher.wrap(format, baseUri, sparqlAsk(askQuery));
    }

    /**
     * Create a RepositoryConnection Matcher that evaluates a SPARQL ASK query.
     *
     * @param format       the RDFFormat used for de-serializing the RDF
     * @param baseUri      the baseUri used for de-serializing the RDF
     * @param queryBaseUri The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param askQuery     the SPARQL ASK query to check, test will fail if the result is {@code false}
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlAskMatcher
     * @see org.openrdf.query.BooleanQuery#evaluate()
     */
    public static <T extends String> Matcher<T> sparqlAskRdf(RDFFormat format, String baseUri, String queryBaseUri, String askQuery) {
        return RdfStringMatcher.wrap(format, baseUri, sparqlAsk(queryBaseUri, askQuery));
    }

    /**
     * Create a RepositoryConnection Matcher that checks for the presence of the provided binding in the ResultSet.
     *
     * @param baseUri     The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery A SPARQL SELECT query to match the provided matcher against.
     * @param matcher     The Matcher to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlTupleQueryMatcher
     * @see org.openrdf.repository.sparql.query.SPARQLTupleQuery
     */
    public static <T extends RepositoryConnection> Matcher<T> sparqlTupleQuery(String baseUri, String sparqlQuery, Matcher<Iterable<BindingSet>> matcher) {
        return SparqlTupleQueryMatcher.sparqlQuery(baseUri, sparqlQuery, matcher);
    }

    /**
     * Create a RepositoryConnection Matcher that checks for the presence of the provided bindings in the ResultSet.
     *
     * @param baseUri     The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery A SPARQL SELECT query to match the provided matcher against.
     * @param matchers    The Matchers to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlTupleQueryMatcher
     * @see org.openrdf.repository.sparql.query.SPARQLTupleQuery
     */
    @SafeVarargs
    public static <T extends RepositoryConnection> Matcher<T> sparqlTupleQuery(String baseUri, String sparqlQuery, Matcher<Iterable<BindingSet>>... matchers) {
        return SparqlTupleQueryMatcher.sparqlQuery(baseUri, sparqlQuery, matchers);
    }

    /**
     * Create a Repository Matcher that checks for the presence of the provided binding in the ResultSet.
     *
     * @param baseUri     The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery A SPARQL SELECT query to match the provided matcher against.
     * @param matcher     The Matcher to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlTupleQueryMatcher
     * @see org.openrdf.repository.sparql.query.SPARQLTupleQuery
     */
    public static <T extends Repository> Matcher<T> sparqlTupleQueryRepository(String baseUri, String sparqlQuery, Matcher<Iterable<BindingSet>> matcher) {
        return RepositoryMatcher.wrap(SparqlTupleQueryMatcher.sparqlQuery(baseUri, sparqlQuery, matcher));
    }

    /**
     * Create a Repository Matcher that checks for the presence of the provided bindings in the ResultSet.
     *
     * @param baseUri     The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery A SPARQL SELECT query to match the provided matcher against.
     * @param matchers    The Matchers to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlTupleQueryMatcher
     * @see org.openrdf.repository.sparql.query.SPARQLTupleQuery
     */
    @SafeVarargs
    public static <T extends Repository> Matcher<T> sparqlTupleQueryRepository(String baseUri, String sparqlQuery, Matcher<Iterable<BindingSet>>... matchers) {
        return RepositoryMatcher.wrap(SparqlTupleQueryMatcher.sparqlQuery(baseUri, sparqlQuery, matchers));
    }

    /**
     * Create a RDF-String Matcher that checks for the presence of the provided binding in the ResultSet.
     *
     * @param format       the RDFFormat used for de-serializing the RDF
     * @param baseUri      the baseUri used for de-serializing the RDF
     * @param queryBaseUri The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery  A SPARQL SELECT query to match the provided matcher against.
     * @param matcher      The Matcher to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlTupleQueryMatcher
     * @see org.openrdf.repository.sparql.query.SPARQLTupleQuery
     */
    public static <T extends String> Matcher<T> sparqlTupleQueryRdf(RDFFormat format, String baseUri, String queryBaseUri, String sparqlQuery, Matcher<Iterable<BindingSet>> matcher) {
        return RdfStringMatcher.wrap(format, baseUri, SparqlTupleQueryMatcher.sparqlQuery(queryBaseUri, sparqlQuery, matcher));
    }

    /**
     * Create a RDF-String Matcher that checks for the presence of the provided bindings in the ResultSet.
     *
     * @param format       the RDFFormat used for de-serializing the RDF
     * @param baseUri      the baseUri used for de-serializing the RDF
     * @param queryBaseUri The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery  A SPARQL SELECT query to match the provided matcher against.
     * @param matchers     The Matchers to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlTupleQueryMatcher
     * @see org.openrdf.repository.sparql.query.SPARQLTupleQuery
     */
    @SafeVarargs
    public static <T extends String> Matcher<T> sparqlTupleQueryRdf(RDFFormat format, String baseUri, String queryBaseUri, String sparqlQuery, Matcher<Iterable<BindingSet>>... matchers) {

        return RdfStringMatcher.wrap(format, baseUri, SparqlTupleQueryMatcher.sparqlQuery(queryBaseUri, sparqlQuery, matchers));
    }

    /**
     * Create a {@link org.apache.marmotta.commons.sesame.test.sparql.SparqlGraphQueryMatcher} that matches the given
     * {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} against the result of
     * the given SPARQL CONSTRUCT query.
     *
     * @param baseUri     The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery A SPARQL CONSTRUCT query
     * @param matcher     the AbstractRepositoryConnectionMatcher to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlGraphQueryMatcher
     */
    public static <T extends RepositoryConnection> Matcher<T> sparqlGraphQuery(String baseUri, String sparqlQuery, Matcher<? extends RepositoryConnection> matcher) {
        return SparqlGraphQueryMatcher.<T>sparqlGraphQuery(baseUri, sparqlQuery, matcher);
    }

    /**
     * Create a {@link org.apache.marmotta.commons.sesame.test.sparql.SparqlGraphQueryMatcher} that matches the given
     * {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} against the result of
     * the given SPARQL CONSTRUCT query.
     *
     * @param baseUri     The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery A SPARQL CONSTRUCT query
     * @param matchers    the {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher}s to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlGraphQueryMatcher
     */
    @SafeVarargs
    public static <T extends RepositoryConnection, V extends RepositoryConnection> Matcher<T> sparqlGraphQuery(String baseUri, String sparqlQuery, Matcher<V>... matchers) {
        return SparqlGraphQueryMatcher.<T>sparqlGraphQuery(baseUri, sparqlQuery, CoreMatchers.allOf(matchers));
    }

    /**
     * Create a {@link org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher} that matches the given
     * {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} against the result of
     * the given SPARQL CONSTRUCT query on a Repository.
     *
     * @param baseUri     The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery A SPARQL CONSTRUCT query
     * @param matcher     the RepositoryMatcher to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlGraphQueryMatcher
     */
    public static <T extends Repository, V extends RepositoryConnection> Matcher<T> sparqlGraphQueryRepository(String baseUri, String sparqlQuery, Matcher<V> matcher) {
        return RepositoryMatcher.wrap(sparqlGraphQuery(baseUri, sparqlQuery, matcher));
    }

    /**
     * Create a {@link org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher} that matches the given
     * {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher}s against the result of
     * the given SPARQL CONSTRUCT query on a Repository.
     *
     * @param baseUri     The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery A SPARQL CONSTRUCT query
     * @param matchers    the RepositoryMatchers to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlGraphQueryMatcher
     */
    @SafeVarargs
    public static <T extends Repository, V extends RepositoryConnection> Matcher<T> sparqlGraphQueryRepository(String baseUri, String sparqlQuery, Matcher<V>... matchers) {
        return RepositoryMatcher.wrap(sparqlGraphQuery(baseUri, sparqlQuery, matchers));
    }

    /**
     * Create a {@link org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher} that matches the given
     * {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher}s against the result of
     * the given SPARQL CONSTRUCT query on a Repository.
     *
     * @param baseUri     The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery A SPARQL CONSTRUCT query
     * @param matcher     the RepositoryMatcher to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlGraphQueryMatcher
     */
    public static <T extends String, V extends RepositoryConnection> Matcher<T> sparqlGraphQueryRdf(RDFFormat format, String baseUri, String queryBaseUri, String sparqlQuery, Matcher<V> matcher) {
        return RdfStringMatcher.wrap(format, baseUri, sparqlGraphQuery(queryBaseUri, sparqlQuery, matcher));
    }

    /**
     * Create a {@link org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher} that matches the given
     * {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher}s against the result of
     * the given SPARQL CONSTRUCT query on a Repository.
     *
     * @param baseUri     The base URI to resolve any relative URIs that are in the query against, can be null if the query does not contain any relative URIs.
     * @param sparqlQuery A SPARQL CONSTRUCT query
     * @param matchers    the RepositoryMatchers to match
     * @see org.apache.marmotta.commons.sesame.test.sparql.SparqlGraphQueryMatcher
     */
    @SafeVarargs
    public static <T extends String, V extends RepositoryConnection> Matcher<T> sparqlGraphQueryRdf(RDFFormat format, String baseUri, String queryBaseUri, String sparqlQuery, Matcher<V>... matchers) {
        return RdfStringMatcher.wrap(format, baseUri, sparqlGraphQuery(queryBaseUri, sparqlQuery, matchers));
    }

    /**
     * Wrap a {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} with a {@link org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher}.
     *
     * @param matcher the Matcher to wrap
     * @see org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher#wrap(org.hamcrest.Matcher)
     */
    public static <T extends Repository, V extends RepositoryConnection> Matcher<T> repositoryMatches(Matcher<V> matcher) {
        return RepositoryMatcher.wrap(matcher);
    }

    /**
     * Wrap the {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} with a {@link org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher}.
     *
     * @param matcher1 the Matcher to wrap
     * @param matcher2 the Matcher to wrap
     * @see org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher#wrap(org.hamcrest.Matcher)
     */
    public static <T extends Repository, V extends RepositoryConnection> Matcher<T> repositoryMatches(Matcher<V> matcher1, Matcher<V> matcher2) {
        return RepositoryMatcher.wrap(CoreMatchers.allOf(matcher1, matcher2));
    }

    /**
     * Wrap the {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} with a {@link org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher}.
     *
     * @param matchers the Matchers to wrap
     * @see org.apache.marmotta.commons.sesame.test.base.RepositoryMatcher#wrap(org.hamcrest.Matcher)
     */
    @SafeVarargs
    public static <T extends Repository, V extends RepositoryConnection> Matcher<T> repositoryMatches(Matcher<V>... matchers) {
        return RepositoryMatcher.wrap(CoreMatchers.allOf(matchers));
    }

    /**
     * Wrap a {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} with a {@link org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher},
     * to match the provided matcher against an serialized RDF-String.
     *
     * @param format  the RDFFormat used for de-serializing the RDF
     * @param baseUri the baseUri used for de-serializing the RDF
     * @param matcher the Matcher to wrap
     * @see org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher#wrap(org.openrdf.rio.RDFFormat, String, org.hamcrest.Matcher)
     */
    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(RDFFormat format, String baseUri, Matcher<V> matcher) {
        return RdfStringMatcher.wrap(format, baseUri, matcher);
    }

    /**
     * Wrap a {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} with a {@link org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher},
     * to match the provided matcher against an serialized RDF-String.
     *
     * @param format   the RDFFormat used for de-serializing the RDF
     * @param baseUri  the baseUri used for de-serializing the RDF
     * @param matcher1 the Matcher to wrap
     * @param matcher2 the Matcher to wrap
     * @see org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher#wrap(org.openrdf.rio.RDFFormat, String, org.hamcrest.Matcher)
     */
    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(RDFFormat format, String baseUri, Matcher<V> matcher1, Matcher<V> matcher2) {
        return RdfStringMatcher.wrap(format, baseUri, CoreMatchers.allOf(matcher1, matcher2));
    }

    /**
     * Wrap a {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} with a {@link org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher},
     * to match the provided matcher against an serialized RDF-String.
     *
     * @param format   the RDFFormat used for de-serializing the RDF
     * @param baseUri  the baseUri used for de-serializing the RDF
     * @param matchers the Matchers to wrap
     * @see org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher#wrap(org.openrdf.rio.RDFFormat, String, org.hamcrest.Matcher)
     */
    @SafeVarargs
    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(RDFFormat format, String baseUri, Matcher<V>... matchers) {
        return RdfStringMatcher.wrap(format, baseUri, CoreMatchers.allOf(matchers));
    }

    /**
     * Wrap a {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} with a {@link org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher},
     * to match the provided matcher against an serialized RDF-String.
     *
     * @param mimeType the MimeType used to guess the RDFFormat for de-serializing the RDF
     * @param baseUri  the baseUri used for de-serializing the RDF
     * @param matcher  the Matcher to wrap
     * @see Rio#getParserFormatForMIMEType(String)
     * @see org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher#wrap(org.openrdf.rio.RDFFormat, String, org.hamcrest.Matcher)
     */
    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(String mimeType, String baseUri, Matcher<V> matcher) {
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType);
        if (format == null) throw new UnsupportedRDFormatException(mimeType);
        return RdfStringMatcher.wrap(format, baseUri, matcher);
    }

    /**
     * Wrap a {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} with a {@link org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher},
     * to match the provided matcher against an serialized RDF-String.
     *
     * @param mimeType the MimeType used to guess the RDFFormat for de-serializing the RDF
     * @param baseUri  the baseUri used for de-serializing the RDF
     * @param matcher1 the Matcher to wrap
     * @param matcher2 the Matcher to wrap
     * @see Rio#getParserFormatForMIMEType(String)
     * @see org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher#wrap(org.openrdf.rio.RDFFormat, String, org.hamcrest.Matcher)
     */
    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(String mimeType, String baseUri, Matcher<V> matcher1, Matcher<V> matcher2) {
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType);
        if (format == null) throw new UnsupportedRDFormatException(mimeType);
        return RdfStringMatcher.wrap(format, baseUri, CoreMatchers.allOf(matcher1, matcher2));
    }

    /**
     * Wrap a {@link org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher} with a {@link org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher},
     * to match the provided matcher against an serialized RDF-String.
     *
     * @param mimeType the MimeType used to guess the RDFFormat for de-serializing the RDF
     * @param baseUri  the baseUri used for de-serializing the RDF
     * @param matchers the Matchers to wrap
     * @see Rio#getParserFormatForMIMEType(String)
     * @see org.apache.marmotta.commons.sesame.test.base.RdfStringMatcher#wrap(org.openrdf.rio.RDFFormat, String, org.hamcrest.Matcher)
     */
    @SafeVarargs
    public static <T extends String, V extends RepositoryConnection> Matcher<T> rdfStringMatches(String mimeType, String baseUri, Matcher<V>... matchers) {
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType);
        if (format == null) throw new UnsupportedRDFormatException(mimeType);
        return RdfStringMatcher.wrap(format, baseUri, CoreMatchers.allOf(matchers));
    }

    private SesameMatchers() {
        // static access only.
    }

}
