/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.commons.sesame.tripletable;

import org.apache.commons.lang3.RandomStringUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Test cases for triple tables.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class TripleTableTest {

    private final ValueFactory vf;

    public TripleTableTest() {
        this.vf = SimpleValueFactory.getInstance();
    }

    @Test
    public void testListTriples() {
        IRI subject1 = vf.createIRI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        IRI subject2 = vf.createIRI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        IRI predicate1 = vf.createIRI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        IRI predicate2 = vf.createIRI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = vf.createLiteral("http://localhost/" + RandomStringUtils.random(40));
        Literal object2 = vf.createLiteral("http://localhost/" + RandomStringUtils.random(40));

        Statement stmt1 = vf.createStatement(subject1, predicate1, object1);
        Statement stmt2 = vf.createStatement(subject1, predicate1, object2);
        Statement stmt3 = vf.createStatement(subject1, predicate2, object1);
        Statement stmt4 = vf.createStatement(subject1, predicate2, object2);
        Statement stmt5 = vf.createStatement(subject2, predicate1, object1);
        Statement stmt6 = vf.createStatement(subject2, predicate1, object2);
        Statement stmt7 = vf.createStatement(subject2, predicate2, object1);
        Statement stmt8 = vf.createStatement(subject2, predicate2, object2);

        TripleTable<Statement> table = new TripleTable<>();
        table.add(stmt1);
        table.add(stmt2);
        table.add(stmt3);
        table.add(stmt4);
        table.add(stmt5);
        table.add(stmt6);
        table.add(stmt7);
        //table.add(stmt8);

        // tests
        // 1. test existence and non-existence of a triple
        assertEquals(1, table.listTriples(subject2, predicate2, object1, null, true).size());
        assertEquals(0, table.listTriples(subject2, predicate2, object2, null, true).size());

        // 2. test listing with wildcards
        assertEquals(7, table.listTriples(null, null, null, null, true).size());
        assertEquals(4, table.listTriples(subject1, null, null, null, true).size());
        assertEquals(3, table.listTriples(subject2, null, null, null, true).size());
        assertEquals(4, table.listTriples(null, predicate1, null, null, true).size());
        assertEquals(3, table.listTriples(null, predicate2, null, null, true).size());
        assertEquals(4, table.listTriples(null, null, object1, null, true).size());
        assertEquals(3, table.listTriples(null, null, object2, null, true).size());
        assertEquals(2, table.listTriples(subject1, predicate1, null, null, true).size());
        assertEquals(1, table.listTriples(subject2, predicate2, null, null, true).size());
    }

    @Test
    public void testRemoveTriples() {
        IRI subject1 = vf.createIRI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        IRI subject2 = vf.createIRI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        IRI predicate1 = vf.createIRI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        IRI predicate2 = vf.createIRI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = vf.createLiteral("http://localhost/" + RandomStringUtils.random(40));
        Literal object2 = vf.createLiteral("http://localhost/" + RandomStringUtils.random(40));

        Statement stmt1 = vf.createStatement(subject1, predicate1, object1);
        Statement stmt2 = vf.createStatement(subject1, predicate1, object2);
        Statement stmt3 = vf.createStatement(subject1, predicate2, object1);
        Statement stmt4 = vf.createStatement(subject1, predicate2, object2);
        Statement stmt5 = vf.createStatement(subject2, predicate1, object1);
        Statement stmt6 = vf.createStatement(subject2, predicate1, object2);
        Statement stmt7 = vf.createStatement(subject2, predicate2, object1);
        Statement stmt8 = vf.createStatement(subject2, predicate2, object2);

        TripleTable<Statement> table = new TripleTable<>();
        table.add(stmt1);
        table.add(stmt2);
        table.add(stmt3);
        table.add(stmt4);
        table.add(stmt5);
        table.add(stmt6);
        table.add(stmt7);
        table.add(stmt8);

        // tests
        // 1. test existence and non-existence of a triple
        assertEquals(1, table.listTriples(subject2, predicate2, object1, null, true).size());
        assertEquals(1, table.listTriples(subject2, predicate2, object2, null, true).size());

        table.remove(stmt8);

        assertEquals(1, table.listTriples(subject2, predicate2, object1, null, true).size());
        assertEquals(0, table.listTriples(subject2, predicate2, object2, null, true).size());
    }

}
