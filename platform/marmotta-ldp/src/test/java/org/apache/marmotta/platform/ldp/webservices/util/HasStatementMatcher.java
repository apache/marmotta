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

import org.hamcrest.Description;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Has Statement Matcher
 */
public class HasStatementMatcher extends BaseRdfMatcher {

    private final Resource subject;
    private final URI predicate;
    private final Value object;
    private final Resource[] contexts;

    protected HasStatementMatcher(String mimeType, String baseUri, Resource subject, URI predicate, Value object, Resource... contexts) {
        super(mimeType, baseUri);
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.contexts = contexts;
    }

    @Override
    protected boolean matches(RepositoryConnection con) throws RepositoryException {
        return con.hasStatement(subject, predicate, object, true, contexts);
    }

    @Override
    public void describeTo(Description description) {
        super.describeTo(description);
        description.appendText(" containing a Statement(")
                .appendValue(subject).appendText(" ")
                .appendValue(predicate).appendText(" ")
                .appendValue(object).appendText(")");
    }


    public static HasStatementMatcher hasStatement(String mimeType, Resource subject, URI predicate, Value object) {
        return hasStatement(mimeType, "", subject, predicate, object);
    }

    public static HasStatementMatcher hasStatement(String mimeType, String baseUri, Resource subject, URI predicate, Value object) {
        return new HasStatementMatcher(mimeType, baseUri, subject, predicate, object);
    }
}
