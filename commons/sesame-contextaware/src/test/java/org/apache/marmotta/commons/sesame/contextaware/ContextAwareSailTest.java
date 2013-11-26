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
package org.apache.marmotta.commons.sesame.contextaware;

import info.aduna.iteration.CloseableIteration;

import org.apache.marmotta.commons.sesame.AbstractContextTest;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * @author jakob
 *
 */
public class ContextAwareSailTest extends AbstractContextTest {

    protected ContextAwareSail cas;

    @Override
    @Before
    public void setUp() throws SailException {
        super.setUp();

        cas = new ContextAwareSail(sail, c1);
    }

    @Override
    @After
    public void tearDown() throws SailException {
        super.tearDown();
    }


    @Test
    public void testGetStatements() throws SailException {
        final SailConnection con = cas.getConnection();
        try {
            con.begin();

            Assert.assertTrue(hasStatement(con, u1, p1, l1));
            Assert.assertFalse(hasStatement(con, u2, p2, l2));

            con.commit();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
    }

    @Test
    public void testAddStatement() throws SailException {
        final SailConnection con = cas.getConnection();
        try {
            con.begin();
            con.addStatement(u1, p2, l3);
            con.commit();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
        final SailConnection con2 = sail.getConnection();
        try {
            con2.begin();

            Assert.assertTrue(hasStatement(con2, u1, p2, l3, c1));
            Assert.assertFalse(hasStatement(con2, u1, p2, l3, c2));

            con2.commit();
        } catch (final Throwable t) {
            con2.rollback();
            throw t;
        } finally {
            con2.close();
        }
    }

    @Test
    public void testGetContextIDs() throws SailException {
        final SailConnection con = cas.getConnection();
        try {
            con.begin();

            final CloseableIteration<? extends Resource, SailException> cid = con.getContextIDs();
            try {
                Assert.assertTrue(cid.hasNext());
                Assert.assertThat(cid.next(), CoreMatchers.is(c1));
                Assert.assertFalse(cid.hasNext());
            } finally {
                cid.close();
            }

            con.commit();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
    }

    protected static boolean hasStatement(SailConnection con, Resource subj, URI pred, Value object, Resource... contexts) throws SailException {
        final CloseableIteration<? extends Statement, SailException> stmts = con.getStatements(subj, pred, object, true, contexts);
        try {
            return stmts.hasNext();
        } finally {
            stmts.close();
        }
    }

}
