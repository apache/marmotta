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

import com.google.common.base.Preconditions;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;
import java.io.StringReader;

/**
 * Base class for RDF Matchers to be used in Unit-Tests.
 */
public abstract class BaseRdfMatcher extends TypeSafeMatcher<String> {

    protected final RDFFormat format;
    protected final String baseUri;

    protected BaseRdfMatcher(String baseUri, RDFFormat format) {
        this.baseUri = baseUri;
        this.format = format;
    }

    protected BaseRdfMatcher(String mimeType, String baseUri) {
        this(baseUri, Rio.getParserFormatForMIMEType(mimeType));
    }

    /**
     * Create an initialized Repository from the provided String data representation using the default RDFFormat
     *
     * @param data the rdf data
     * @param baseUri the baseUri
     * @return an initialized Repository containing the data
     */
    protected Repository createRepository(String data, String baseUri) throws RepositoryException, RDFParseException, IOException {
        return createRepository(data, baseUri, format);
    }

    /**
     * Create an initialized Repository from the provided String data representation using the default RDFFormat
     *
     * @param data the rdf data
     * @param baseUri the baseUri
     * @param format the RDFFormat
     * @return an initialized Repository containing the data
     */
    protected Repository createRepository(String data, String baseUri, RDFFormat format) throws RepositoryException, RDFParseException, IOException {
        Preconditions.checkArgument(format != null, "RDFFormat must not be null");

        final Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        final RepositoryConnection con = repository.getConnection();
        try (final StringReader r = new StringReader(data)) {
            con.begin();
            con.add(r, baseUri, format);
            con.commit();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }

        return repository;
    }

    @Override
    public final boolean matchesSafely(String item) {
        boolean isMatching = false;
        try {
            final Repository rep = createRepository(item, baseUri);
            final RepositoryConnection con = rep.getConnection();
            try {
                con.begin();
                isMatching = matches(con);
                con.commit();
            } catch (final Throwable t) {
                con.rollback();
                throw t;
            } finally {
                con.close();
                rep.shutDown();
            }
        } catch (Throwable t) {
            isMatching = false;
        }

        return isMatching;
    }

    protected abstract boolean matches(RepositoryConnection con) throws Exception;

    @Override
    public void describeTo(Description description) {
        description.appendText("a RDF ").appendText(format.getName()).appendText(" String");
    }
}
