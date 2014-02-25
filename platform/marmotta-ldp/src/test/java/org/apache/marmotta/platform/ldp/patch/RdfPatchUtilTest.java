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
package org.apache.marmotta.platform.ldp.patch;

import org.apache.marmotta.commons.vocabulary.FOAF;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Created by jakob on 2/25/14.
 */
public class RdfPatchUtilTest {

    public static final String BASE_URI = "http://example/";

    private Repository repository;
    private URI alice, bob, charlie;
    private Literal lcBob, ucBob;

    @Before
    public void setUp() throws Exception {
        repository = new SailRepository(new MemoryStore());
        repository.initialize();

        alice = repository.getValueFactory().createURI("http://example/alice");
        bob = repository.getValueFactory().createURI("http://example/bob");
        charlie = repository.getValueFactory().createURI("http://example/charlie");

        lcBob = repository.getValueFactory().createLiteral("bob");
        ucBob = repository.getValueFactory().createLiteral("Bob");

        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();
            con.add(this.getClass().getResourceAsStream("/illustrative.in.ttl"), BASE_URI, RDFFormat.TURTLE);
            con.commit();
        } finally {
            con.close();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (repository != null) {
            repository.shutDown();
        }
    }

    @Test
    public void testApplyPatch() throws Exception {
        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();

            Assert.assertTrue(con.hasStatement(bob, FOAF.name, lcBob, false));
            Assert.assertFalse(con.hasStatement(bob, FOAF.name, ucBob, false));
            Assert.assertFalse(con.hasStatement(bob, FOAF.knows, alice, false));
            Assert.assertTrue(con.hasStatement(bob, FOAF.knows, charlie, false));

            RdfPatchUtil.applyPatch(con, this.getClass().getResourceAsStream("/illustrative.rdfp"));

            Assert.assertFalse(con.hasStatement(bob, FOAF.name, lcBob, false));
            Assert.assertTrue(con.hasStatement(bob, FOAF.name, ucBob, false));
            Assert.assertTrue(con.hasStatement(bob, FOAF.knows, alice, false));
            Assert.assertFalse(con.hasStatement(bob, FOAF.knows, charlie, false));

            con.commit();
        } finally {
            con.close();
        }
    }
}
