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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import info.aduna.iteration.CloseableIteration;

import org.apache.marmotta.commons.sesame.AbstractContextTest;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * @author Jakob Frank <jakob@apache.org>
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
                assertTrue(cid.hasNext());
                assertThat(cid.next(), CoreMatchers.is(c1));
                assertFalse(cid.hasNext());
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
    
    @Test
    public void testClear() throws SailException {
        final SailConnection con = cas.getConnection();
        try {
            con.begin();

            assertTrue(hasStatement(con, null, null, null));
            con.clear();
            assertFalse(hasStatement(con, null, null, null));

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

            assertFalse(hasStatement(con2, null, null, null, c1));
            assertTrue(hasStatement(con2, null, null, null, c2));

            con2.commit();
        } catch (final Throwable t) {
            con2.rollback();
            throw t;
        } finally {
            con2.close();
        }
    }
    
    @Test
    public void testRemoveStatements() throws SailException {
        final SailConnection con = cas.getConnection();
        try {
            con.begin();

            assertTrue(hasStatement(con, u1, p1, l1));
            assertTrue(hasStatement(con, u3, p3, l3));
            
            con.removeStatements(u1, p1, l1, c1);
            con.removeStatements(u2, p2, l2);
            con.removeStatements(u4, p4, l4, c1);
            
            assertFalse(hasStatement(con, u1, p1, l1));
            assertTrue(hasStatement(con, u3, p3, l3));

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

            assertFalse(hasStatement(con2, u1, p1, l1, c1));
            assertTrue(hasStatement(con2, u2, p2, l2, c2));
            assertTrue(hasStatement(con2, u3, p3, l3, c1));

            con2.commit();
        } catch (final Throwable t) {
            con2.rollback();
            throw t;
        } finally {
            con2.close();
        }
    }

}
