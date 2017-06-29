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

package org.apache.marmotta.commons.sesame.model;

import com.google.common.base.Equivalence;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class StatementCommonsTest {

    protected static Random rnd = new Random();


    Statement stmt1, stmt2, stmt3, stmt4;

    ValueFactory valueFactory;

    @Before
    public void setup() {
        valueFactory = new ValueFactoryImpl();

        Resource s1 = randomIRI();
        IRI p1 = randomIRI();
        IRI p2 = randomIRI();
        Value o1 = randomObject();
        Resource c1 = randomIRI();
        Resource c2 = randomIRI();


        stmt1 = valueFactory.createStatement(s1,p1,o1,c1);
        stmt2 = valueFactory.createStatement(s1,p1,o1,c2);
        stmt3 = valueFactory.createStatement(s1,p2,o1,c1);
        stmt4 = valueFactory.createStatement(s1,p1,o1,c1);
    }


    @Test
    public void testTripleEquivalence() {
        Equivalence<Statement> e = StatementCommons.tripleEquivalence();

        Assert.assertTrue(e.equivalent(stmt1,stmt2));
        Assert.assertTrue(e.equivalent(stmt1,stmt4));
        Assert.assertFalse(e.equivalent(stmt1,stmt3));
    }

    @Test
    public void testQuadrupleEquivalence() {
        Equivalence<Statement> e = StatementCommons.tripleEquivalence();

        Assert.assertTrue(e.equivalent(stmt1,stmt2));
        Assert.assertTrue(e.equivalent(stmt1,stmt4));
        Assert.assertFalse(e.equivalent(stmt1,stmt3));
    }

    @Test
    public void testTripleSet() {
        Set<Statement> set = StatementCommons.newTripleSet();

        set.add(stmt1);

        // triple 2 just has different context, so should be contained already
        Assert.assertTrue(set.contains(stmt2));

        // adding triple 2 should not change size
        set.add(stmt2);

        Assert.assertEquals(1,set.size());

        // statement 3 is different, so not contained and size increased
        Assert.assertFalse(set.contains(stmt3));

        set.add(stmt3);

        Assert.assertEquals(2,set.size());

    }

    @Test
    public void testQuadrupleSet() {
        Set<Statement> set = StatementCommons.newQuadrupleSet();

        set.add(stmt1);

        // triple 2 has different context, so should not be contained already
        Assert.assertFalse(set.contains(stmt2));

        // adding triple 2 should change size
        set.add(stmt2);

        Assert.assertEquals(2,set.size());

        // statement 3 is different, so not contained and size increased
        Assert.assertFalse(set.contains(stmt3));

        set.add(stmt3);

        Assert.assertEquals(3,set.size());

    }


    @Test
    public void testTripleMap() {
        Map<Statement, String> map = StatementCommons.newTripleMap();

        String s1 = RandomStringUtils.random(8);
        String s2 = RandomStringUtils.random(8);
        String s3 = RandomStringUtils.random(8);

        map.put(stmt1, s1);

        // triple 2 just has different context, so should be contained already
        Assert.assertEquals(s1, map.get(stmt2));

        // adding triple 2 should not change size
        map.put(stmt2,s2);

        Assert.assertEquals(1, map.size());

        // value now replaced?
        Assert.assertEquals(s2, map.get(stmt1));

        // statement 3 is different, so not contained and size increased
        Assert.assertFalse(map.containsKey(stmt3));

        map.put(stmt3,s3);

        Assert.assertEquals(2, map.size());

    }

    @Test
    public void testQuadrupleMap() {
        Map<Statement, String> map = StatementCommons.newQuadrupleMap();

        String s1 = RandomStringUtils.random(8);
        String s2 = RandomStringUtils.random(8);
        String s3 = RandomStringUtils.random(8);

        map.put(stmt1, s1);

        // triple 2 just has different context, so should be contained already
        Assert.assertNotEquals(s1, map.get(stmt2));

        // adding triple 2 should change size
        map.put(stmt2,s2);

        Assert.assertEquals(2, map.size());

        // value not replaced?
        Assert.assertEquals(s1, map.get(stmt1));

        // statement 3 is different, so not contained and size increased
        Assert.assertFalse(map.containsKey(stmt3));

        map.put(stmt3,s3);

        Assert.assertEquals(3, map.size());

    }


    /**
     * Return a random IRI, with a 10% chance of returning a IRI that has already been used.
     * @return
     */
    protected IRI randomIRI() {
        IRI resource = valueFactory.createIRI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        return resource;
    }

    /**
     * Return a random RDF value, either a reused object (10% chance) or of any other kind.
     * @return
     */
    protected Value randomObject() {
        Value object;
        switch(rnd.nextInt(6)) {
            case 0: object = valueFactory.createIRI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
                break;
            case 1: object = valueFactory.createBNode();
                break;
            case 2: object = valueFactory.createLiteral(RandomStringUtils.randomAscii(40));
                break;
            case 3: object = valueFactory.createLiteral(rnd.nextInt());
                break;
            case 4: object = valueFactory.createLiteral(rnd.nextDouble());
                break;
            case 5: object = valueFactory.createLiteral(rnd.nextBoolean());
                break;
            default: object = valueFactory.createIRI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
                break;

        }
        return object;


    }
}
