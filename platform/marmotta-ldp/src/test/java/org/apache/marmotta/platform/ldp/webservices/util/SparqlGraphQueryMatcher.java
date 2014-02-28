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

import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.StringWriter;
import java.util.Map;

/**
 *
 */
public class SparqlGraphQueryMatcher extends SparqlMatcher {
    private final BaseRdfMatcher matcher;

    protected SparqlGraphQueryMatcher(String baseUri, String mimeType, String query, BaseRdfMatcher matcher) {
        super(baseUri, mimeType, query);
        this.matcher = matcher;
    }

    @Override
    protected boolean matches(RepositoryConnection con) throws Exception {
        try {
            final GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, query, baseUri);

            final GraphQueryResult graph = graphQuery.evaluate();
            // FIXME: this is not very efficient!
            try {
                final StringWriter stringWriter = new StringWriter();
                final RDFWriter writer = Rio.createWriter(matcher.format, stringWriter);

                writer.startRDF();
                for (Map.Entry<String, String> namespace : graph.getNamespaces().entrySet()) {
                    writer.handleNamespace(namespace.getKey(), namespace.getValue());
                }
                while (graph.hasNext()) {
                    writer.handleStatement(graph.next());
                }
                writer.endRDF();

                return matcher.matches(stringWriter.toString());
            } finally {
                graph.close();
            }
        } catch (MalformedQueryException e) {
            throw new IllegalArgumentException("Invalid SPARQL Query: " + query, e);
        }
    }

    public static SparqlGraphQueryMatcher sparqlGraphQuery(String mimeType, String baseUri, String query, BaseRdfMatcher matcher) {
        return new SparqlGraphQueryMatcher(baseUri, mimeType, query, matcher);
    }
}
