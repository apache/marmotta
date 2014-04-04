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

import org.apache.marmotta.platform.ldp.patch.model.PatchLine;
import org.apache.marmotta.platform.ldp.patch.model.WildcardStatement;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

import java.util.List;

/**
 * Testing RdfPatchUtil
 *
 * @author Jakob Frank
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

            Assert.assertTrue(con.hasStatement(bob, FOAF.NAME, lcBob, false));
            Assert.assertFalse(con.hasStatement(bob, FOAF.NAME, ucBob, false));
            Assert.assertFalse(con.hasStatement(bob, FOAF.KNOWS, alice, false));
            Assert.assertTrue(con.hasStatement(bob, FOAF.KNOWS, charlie, false));

            RdfPatchUtil.applyPatch(con, this.getClass().getResourceAsStream("/illustrative.rdfp"));

            Assert.assertFalse(con.hasStatement(bob, FOAF.NAME, lcBob, false));
            Assert.assertTrue(con.hasStatement(bob, FOAF.NAME, ucBob, false));
            Assert.assertTrue(con.hasStatement(bob, FOAF.KNOWS, alice, false));
            Assert.assertFalse(con.hasStatement(bob, FOAF.KNOWS, charlie, false));

            con.commit();
        } finally {
            con.close();
        }
    }

    @Test(expected = InvalidPatchDocumentException.class)
    public void testInvalidPatchDocumentException() throws RepositoryException, ParseException, InvalidPatchDocumentException {
        RepositoryConnection con = repository.getConnection();
        try {
            final String invalidPatch = "A <http://example/foo> R <http://example/bar> .";

            RdfPatchUtil.applyPatch(con, invalidPatch);

            Assert.fail("applyPatch should throw an InvalidPatchDocumentException");
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
    }

    @Test
    public void testDiff() throws Exception {
        final RepositoryConnection con1 = repository.getConnection();
        final URI mbox = con1.getValueFactory().createURI("mailto:charlie@example.com");
        try {
            con1.begin();
            con1.remove(bob, FOAF.KNOWS, charlie);
            con1.add(bob, FOAF.KNOWS, alice);
            con1.add(charlie, FOAF.MBOX, mbox);
            con1.commit();
        } finally {
            con1.close();
        }

        final Repository orig = new SailRepository(new MemoryStore());
        orig.initialize();
        final RepositoryConnection con = orig.getConnection();
        try {
            con.begin();
            con.add(this.getClass().getResourceAsStream("/illustrative.in.ttl"), BASE_URI, RDFFormat.TURTLE);
            con.commit();
        } finally {
            con.close();
        }

        // Optimize here to have a predictable order of the patch lines.
        final List<PatchLine> diff = RdfPatchUtil.diff(orig, repository, true);
        Assert.assertEquals("Wrong patch size", 3, diff.size());

        Assert.assertThat("Wrong patch", diff, IsIterableContainingInOrder.contains(
                new PatchLine(PatchLine.Operator.DELETE, new WildcardStatement(bob, FOAF.KNOWS, charlie)),
                new PatchLine(PatchLine.Operator.ADD, new WildcardStatement(null, null, alice)),
                new PatchLine(PatchLine.Operator.ADD, new WildcardStatement(charlie, FOAF.MBOX, mbox))
                ));
    }
}
