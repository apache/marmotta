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

package org.apache.marmotta.kiwi.reasoner.test.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.apache.marmotta.kiwi.reasoner.model.program.Justification;
import org.apache.marmotta.kiwi.reasoner.model.program.Rule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.util.*;

/**
 * Test critical functionality of justification objects, primarily equals and similar
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class JustificationTest {

    protected static Random rnd = new Random();


    private KiWiTriple t1, t2, t3, t4; // base
    private KiWiTriple i1, i2, i3, i4; // inferred
    private Rule r1, r2;

    private KiWiUriResource ctx_inferred;

    @Before
    public void setup() {
        KiWiUriResource s1 = randomURI();
        KiWiUriResource s2 = randomURI();
        KiWiUriResource s3 = randomURI();
        KiWiUriResource s4 = randomURI();
        KiWiUriResource p1 = randomURI();
        KiWiUriResource p2 = randomURI();
        KiWiNode o1 = randomObject();
        KiWiNode o2 = randomObject();
        KiWiNode o3 = randomObject();
        KiWiNode o4 = randomObject();

        ctx_inferred = randomURI();

        t1 = new KiWiTriple(s1,p1,o1, null);
        t2 = new KiWiTriple(s1,p1,o2, null);
        t3 = new KiWiTriple(s2,p1,o3, null);
        t4 = new KiWiTriple(s1,p1,o1, randomURI());


        i1 = new KiWiTriple(s1,p2,o1, ctx_inferred); i1.setInferred(true);
        i2 = new KiWiTriple(s1,p1,o2, ctx_inferred); i2.setInferred(true);
        i3 = new KiWiTriple(s3,p1,o3, ctx_inferred); i3.setInferred(true);
        i4 = new KiWiTriple(s1,p2,o1, ctx_inferred); i4.setInferred(true);


    }


    @Test
    public void testJustificationEquals() {
        Justification j1 = new Justification();
        j1.setTriple(i1);
        j1.getSupportingTriples().add(t1);
        j1.getSupportingTriples().add(t4);

        Justification j2 = new Justification();
        j2.setTriple(i4);
        j2.getSupportingTriples().add(t4);
        j2.getSupportingTriples().add(t1);

        Assert.assertEquals(j1,j2);

        // j3 differs in the inferred triple
        Justification j3 = new Justification();
        j3.setTriple(i2);
        j3.getSupportingTriples().add(t4);
        j3.getSupportingTriples().add(t1);

        Assert.assertNotEquals(j1, j3);

        // j4 differs in the supporting triples
        Justification j4 = new Justification();
        j4.setTriple(i1);
        j4.getSupportingTriples().add(t2);
        j4.getSupportingTriples().add(t4);

        Assert.assertNotEquals(j1, j4);
    }


    @Test
    public void testJustificationSet() {
        Set<Justification> set = new HashSet<>();

        Justification j1 = new Justification();
        j1.setTriple(i1);
        j1.getSupportingTriples().add(t1);
        j1.getSupportingTriples().add(t4);
        set.add(j1);

        Justification j2 = new Justification();
        j2.setTriple(i4);
        j2.getSupportingTriples().add(t4);
        j2.getSupportingTriples().add(t1);

        Assert.assertTrue(set.contains(j2));

        set.add(j2);

        Assert.assertEquals(1, set.size());


        // j3 differs in the inferred triple
        Justification j3 = new Justification();
        j3.setTriple(i2);
        j3.getSupportingTriples().add(t4);
        j3.getSupportingTriples().add(t1);

        Assert.assertFalse(set.contains(j3));

        set.add(j3);

        Assert.assertEquals(2, set.size());


        // j4 differs in the supporting triples
        Justification j4 = new Justification();
        j4.setTriple(i1);
        j4.getSupportingTriples().add(t2);
        j4.getSupportingTriples().add(t4);

        Assert.assertFalse(set.contains(j4));

        set.add(j4);

        Assert.assertEquals(3, set.size());
    }



    /**
     * Return a random URI, with a 10% chance of returning a URI that has already been used.
     * @return
     */
    protected KiWiUriResource randomURI() {
        KiWiUriResource resource = new KiWiUriResource("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        return resource;
    }

    /**
     * Return a random RDF value, either a reused object (10% chance) or of any other kind.
     * @return
     */
    protected KiWiNode randomObject() {
        KiWiNode object;
        switch(rnd.nextInt(6)) {
            case 0: object = new KiWiUriResource("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
                break;
            case 1: object = new KiWiAnonResource(RandomStringUtils.randomAscii(8));
                break;
            case 2: object = new KiWiStringLiteral(RandomStringUtils.randomAscii(40));
                break;
            case 3: object = new KiWiIntLiteral(rnd.nextLong(), new KiWiUriResource(Namespaces.NS_XSD + "integer"));
                break;
            case 4: object = new KiWiDoubleLiteral(rnd.nextDouble(), new KiWiUriResource(Namespaces.NS_XSD + "double"));
                break;
            case 5: object = new KiWiBooleanLiteral(rnd.nextBoolean(), new KiWiUriResource(Namespaces.NS_XSD + "boolean"));
                break;
            default: object = new KiWiUriResource("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
                break;

        }
        return object;
    }

}
