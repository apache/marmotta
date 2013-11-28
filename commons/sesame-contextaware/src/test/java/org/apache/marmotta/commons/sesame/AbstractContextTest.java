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
package org.apache.marmotta.commons.sesame;

import info.aduna.iteration.CloseableIteration;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

public abstract class AbstractContextTest {

    protected static final String NS = "http://test.marmotta.apache.org/resource/";
    
    protected static boolean hasStatement(SailConnection con, Resource subj,
            URI pred, Value object, Resource... contexts) throws SailException {
                final CloseableIteration<? extends Statement, SailException> stmts = con.getStatements(subj, pred, object, true, contexts);
                try {
                    return stmts.hasNext();
                } finally {
                    stmts.close();
                }
            }

    protected Sail sail;

    protected URI u1, u2, u3, u4;
    protected URI p1, p2, p3, p4;
    protected Resource c1, c2;
    protected Literal l1, l2, l3, l4;
    
    @Before
    public void setUp() throws SailException {
        sail = new MemoryStore();
        sail.initialize();

        final ValueFactory vf = sail.getValueFactory();
        u1 = vf.createURI(NS, UUID.randomUUID().toString());
        u2 = vf.createURI(NS, UUID.randomUUID().toString());
        u3 = vf.createURI(NS, UUID.randomUUID().toString());
        u4 = vf.createURI(NS, UUID.randomUUID().toString());
        
        p1 = vf.createURI(NS, UUID.randomUUID().toString());
        p2 = vf.createURI(NS, UUID.randomUUID().toString());
        p3 = vf.createURI(NS, UUID.randomUUID().toString());
        p4 = vf.createURI(NS, UUID.randomUUID().toString());
        
        c1 = vf.createURI(NS, UUID.randomUUID().toString());
        c2 = vf.createBNode();
        
        l1 = vf.createLiteral(UUID.randomUUID().toString());
        l2 = vf.createLiteral(UUID.randomUUID().toString());
        l3 = vf.createLiteral(UUID.randomUUID().toString());
        l4 = vf.createLiteral(UUID.randomUUID().toString());
        
        final SailConnection con = sail.getConnection();
        try {
            con.begin();
            
            con.addStatement(u1, p1, l1, c1);
            con.addStatement(u2, p2, l2, c2);
            con.addStatement(u3, p3, l3, c1);
            con.addStatement(u4, p4, l4, c2);
            
            con.commit();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
    }
    
    @After
    public void tearDown() throws SailException {
        if (sail != null) {
            sail.shutDown();
        }
        sail = null;
    }
    
}
