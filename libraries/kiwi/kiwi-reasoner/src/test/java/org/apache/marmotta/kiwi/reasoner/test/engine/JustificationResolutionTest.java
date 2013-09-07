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

package org.apache.marmotta.kiwi.reasoner.test.engine;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.sesame.model.StatementCommons;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.apache.marmotta.kiwi.reasoner.engine.ReasoningEngine;
import org.apache.marmotta.kiwi.reasoner.model.program.Justification;
import org.apache.marmotta.kiwi.reasoner.model.program.Rule;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningConnection;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;

import java.sql.SQLException;
import java.util.*;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;

/**
 * Test if the ReasoningEngine's way of resolving base justifications works. Mocks the lookup for base justifications.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class JustificationResolutionTest {

    private MockReasoningEngine engine;

    private Map<Statement,Set<Justification>> baseJustifications;


    protected static Random rnd = new Random();


    private KiWiTriple t1, t2, t3, t4; // base
    private KiWiTriple i1, i2, i3, i4; // inferred
    private Justification j1, j2, j3, j4;
    private Rule r1, r2;

    private KiWiUriResource ctx_inferred;

    @Before
    public void setup() {
        engine = new MockReasoningEngine();

        baseJustifications = StatementCommons.newQuadrupleMap();


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

        // assume i1 is justified by t1 and t2;
        j1 = new Justification();
        j1.setTriple(i1);
        j1.getSupportingTriples().add(t1);
        j1.getSupportingTriples().add(t2);

        baseJustifications.put(i1, Collections.singleton(j1));

        // assume i2 is justified by t3 and t4, as well as by t2 and t4
        j2 = new Justification();
        j2.setTriple(i2);
        j2.getSupportingTriples().add(t3);
        j2.getSupportingTriples().add(t4);


        j3 = new Justification();
        j3.setTriple(i2);
        j3.getSupportingTriples().add(t2);
        j3.getSupportingTriples().add(t4);

        baseJustifications.put(i2, Sets.newHashSet(j2,j3));



    }


    @Test
    public void testResolveBaseTriples() throws Exception {
        // i3 justified by i1 and t3
        Justification tj1 = new Justification();
        tj1.setTriple(i3);
        tj1.getSupportingTriples().add(i1);
        tj1.getSupportingTriples().add(t3);

        Collection<Justification> r1 = engine.getBaseJustifications(null,Collections.singleton(tj1));
        Assert.assertEquals(1, r1.size());

        Justification tj1r = r1.iterator().next();
        Assert.assertEquals(3,tj1r.getSupportingTriples().size());
        Assert.assertTrue(tj1r.getSupportingTriples().contains(t1));
        Assert.assertTrue(tj1r.getSupportingTriples().contains(t2));
        Assert.assertTrue(tj1r.getSupportingTriples().contains(t3));


        // i4 justified by i1 and i2
        Justification tj2 = new Justification();
        tj2.setTriple(i4);
        tj2.getSupportingTriples().add(i1);
        tj2.getSupportingTriples().add(i2);

        Collection<Justification> r2 = engine.getBaseJustifications(null,Collections.singleton(tj2));

        // since i2 is justified by two justifications, the result for i4 also needs to have two
        Assert.assertEquals(2, r2.size());

        Assert.assertThat(r2,Matchers.<Justification>hasItem(hasProperty("supportingTriples", hasItems(t1,t2,t3,t4))));
        Assert.assertThat(r2,Matchers.<Justification>hasItem(hasProperty("supportingTriples", hasItems(t1,t2,t4))));
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



    private class MockReasoningEngine extends ReasoningEngine {
        private MockReasoningEngine() {
        }


        /**
         * Return the justifications for the triple passed as argument.
         *
         * @param t
         * @return
         */
        @Override
        protected Collection<Justification> getJustifications(KiWiReasoningConnection connection, KiWiTriple t, Set<Justification> transactionJustifications) throws SQLException {
            return baseJustifications.get(t);
        }

        /**
         * For all justifications contained in the set passed as argument, create corresponding base justifications,
         * i.e. justifications that only contain base triples and no inferred triples.
         *
         * @param justifications
         * @return
         */
        @Override
        public Set<Justification> getBaseJustifications(KiWiReasoningConnection connection, Set<Justification> justifications) throws SQLException {
            return super.getBaseJustifications(connection, justifications);
        }
    }
}
