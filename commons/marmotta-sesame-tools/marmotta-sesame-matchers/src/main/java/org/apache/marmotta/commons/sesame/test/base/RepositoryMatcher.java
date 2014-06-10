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

/**
 * Wrapper for a {@link AbstractRepositoryConnectionMatcher} to accept a {@link Repository}
 */
public class RepositoryMatcher<T extends Repository> extends SesameMatcher<T> implements Matcher<T> {

    private final Matcher<? extends RepositoryConnection> delegate;

    /**
     * @param delegate the Matcher to wrap.
     */
    public RepositoryMatcher(Matcher<? extends RepositoryConnection> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected boolean matchesSafely(Repository repository) {
        try {
            final RepositoryConnection con = repository.getConnection();
            try {
                con.begin();
                boolean matches = delegate.matches(con);
                con.commit();
                return matches;
            } catch (final Throwable t) {
                con.rollback();
                throw t;
            } finally {
                con.close();
            }
        } catch (RuntimeException r) {
            throw r;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        delegate.describeTo(description);
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        delegate.describeMismatch(item, mismatchDescription);
    }

    /**
     * Wrap an instance of an {@link AbstractRepositoryConnectionMatcher} to match against an Sesame {@link Repository}.
     *
     * @param connectionMatcher the {@link AbstractRepositoryConnectionMatcher} to wrap
     */
    public static <T extends Repository> Matcher<T> wrap(Matcher<? extends RepositoryConnection> connectionMatcher) {
        return new RepositoryMatcher<T>(connectionMatcher);
    }
}
