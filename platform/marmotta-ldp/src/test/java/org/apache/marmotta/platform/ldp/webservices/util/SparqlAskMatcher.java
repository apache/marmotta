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

import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;

/**
 * Sparql ASK-Query Matcher
 */
public class SparqlAskMatcher extends SparqlMatcher {
    protected SparqlAskMatcher(String baseUri, String mimeType, String query) {
        super(baseUri, mimeType, query);
    }

    @Override
    protected boolean matches(RepositoryConnection con) throws Exception {
        try {
            final BooleanQuery booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, query, baseUri);

            return booleanQuery.evaluate();
        } catch (MalformedQueryException e) {
            throw new IllegalArgumentException("Invalid SPARQL Query: " + query, e);
        }
    }

    public static SparqlAskMatcher sparqlAsk(String mimeType, String baseUri, String askQuery) {
        return new SparqlAskMatcher(baseUri, mimeType, askQuery);
    }

    public static SparqlAskMatcher sparqlAsk(String mimeType, String askQuery) {
        return sparqlAsk(mimeType, "", askQuery);
    }


}
