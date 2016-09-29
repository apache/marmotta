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
package org.apache.marmotta.platform.ldp.patch.parser;

import org.apache.marmotta.platform.ldp.patch.model.PatchLine;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FOAF;

import java.util.Iterator;
import java.util.List;

/**
 * Testing the RdfPatchParserImpl
 *
 * @author Jakob Frank
 */
public class RdfPatchParserImplTest {


    private RdfPatchParserImpl parser;
    private IRI alice, bob, charlie;
    private Literal lcBob, ucBob;

    @Before
    public void setUp() {
        parser = new RdfPatchParserImpl(this.getClass().getResourceAsStream("/illustrative.rdfp"));
        
        ValueFactory f = SimpleValueFactory.getInstance();

        alice = f.createIRI("http://example/alice");
        bob = f.createIRI("http://example/bob");
        charlie = f.createIRI("http://example/charlie");

        lcBob = f.createLiteral("bob");
        ucBob = f.createLiteral("Bob");
    }

    @After
    public void tearDown() {
        parser = null;
    }


    @Test
    public void testParsing() throws ParseException {
        List<PatchLine> patchLines = parser.parsePatch();

        Iterator<PatchLine> it = patchLines.iterator();

        Assert.assertTrue(it.hasNext());
        checkPatchLine(it.next(), PatchLine.Operator.DELETE, bob, FOAF.NAME, lcBob);

        Assert.assertTrue(it.hasNext());
        checkPatchLine(it.next(), PatchLine.Operator.ADD, bob, FOAF.NAME, ucBob);

        Assert.assertTrue(it.hasNext());
        checkPatchLine(it.next(), PatchLine.Operator.ADD, null, FOAF.KNOWS, alice);

        Assert.assertTrue(it.hasNext());
        checkPatchLine(it.next(), PatchLine.Operator.DELETE, null, null, charlie);
    }

    private void checkPatchLine(PatchLine line, PatchLine.Operator operator, Resource subejct, IRI predicate, Value object) {
        Assert.assertEquals("Wrong patch operation", operator, line.getOperator());

        Statement statement = line.getStatement();
        Assert.assertEquals("Wrong subject", subejct, statement.getSubject());
        Assert.assertEquals("Wrong predicate", predicate, statement.getPredicate());
        Assert.assertEquals("Wrong object", object, statement.getObject());
    }

    @Test
    public void testFullParsing() throws ParseException {
        parser.ReInit(getClass().getResourceAsStream("/rdf-patch.rdfp"));

        List<PatchLine> patchLines = parser.parsePatch();
        Assert.assertNotNull(patchLines);
        Assert.assertEquals(22, patchLines.size());
    }

}
