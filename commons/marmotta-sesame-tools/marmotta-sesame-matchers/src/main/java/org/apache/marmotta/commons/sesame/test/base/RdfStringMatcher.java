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
package org.apache.marmotta.commons.sesame.test.base;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

import java.io.StringReader;

/**
 * Match against an RDF String (in various Formats)
 */
public class RdfStringMatcher<T extends String> extends SesameMatcher<T> implements Matcher<T> {

    private final Matcher<? extends RepositoryConnection>[] delegates;
    private final String baseUri;
    private final RDFFormat format;

    @SafeVarargs
    public RdfStringMatcher(RDFFormat format, String baseUri, Matcher<? extends RepositoryConnection>... delegates) {
        this.delegates = delegates;
        this.baseUri = baseUri;
        this.format = format;
    }

    @Override
    protected boolean matchesSafely(String rdfString) {
        if (baseUri == null) throw new IllegalArgumentException("baseUri must not be null");
        if (format == null) throw new IllegalArgumentException("format must not be null");

        try {
            Repository repo = new SailRepository(new MemoryStore());
            repo.initialize();
            try {
                final RepositoryConnection con = repo.getConnection();
                try (final StringReader r = new StringReader(rdfString)) {
                    con.begin();
                    con.add(r, baseUri, format);
                    con.commit();

                    boolean result = true;
                    for (Matcher<? extends RepositoryConnection> delegate : delegates) {
                        con.begin();
                        result &= delegate.matches(con);
                        con.commit();
                    }
                    return result;
                } catch (final Throwable t) {
                    con.rollback();
                    throw t;
                } finally {
                    con.close();
                }
            } finally {
                repo.shutDown();
            }
        } catch (RuntimeException r) {
            throw r;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(format.getName()).appendText(" String ");
        if (delegates.length == 1) {
            description.appendDescriptionOf(delegates[0]);
        } else {
            for (Matcher<? extends RepositoryConnection> delegate : delegates) {
                description.appendText("\n  ").appendDescriptionOf(delegate);
            }
        }
    }

    /**
     * Wrap an instance of an AbstractRepositoryConnectionMatcher to match it against an RDF-String.
     *
     * @param format   the RDFFormat of the String
     * @param baseUri  the baseUri for de-serializing the String
     * @param delegate the AbstractRepositoryConnectionMatcher to wrap.
     * @see org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher
     */
    public static <T extends String> Matcher<T> wrap(RDFFormat format, String baseUri, Matcher<? extends RepositoryConnection> delegate) {
        return new RdfStringMatcher<T>(format, baseUri, delegate);
    }

    /**
     * Wrap an instance of an AbstractRepositoryConnectionMatcher to match it against an RDF-String.
     *
     * @param format    the RDFFormat of the String
     * @param baseUri   the baseUri for de-serializing the String
     * @param delegates the AbstractRepositoryConnectionMatcher to wrap.
     * @see org.apache.marmotta.commons.sesame.test.base.AbstractRepositoryConnectionMatcher
     */
    @SafeVarargs
    public static <T extends String> Matcher<T> wrap(RDFFormat format, String baseUri, Matcher<? extends RepositoryConnection>... delegates) {
        return new RdfStringMatcher<T>(format, baseUri, delegates);
    }


}
