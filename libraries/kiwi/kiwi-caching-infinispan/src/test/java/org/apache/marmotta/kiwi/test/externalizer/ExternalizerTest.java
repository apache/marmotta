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

package org.apache.marmotta.kiwi.test.externalizer;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.kiwi.infinispan.externalizer.*;
import org.apache.marmotta.kiwi.infinispan.remote.CustomJBossMarshaller;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.apache.marmotta.kiwi.test.TestValueFactory;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.marshall.StreamingMarshaller;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Random;

/**
 * Test the different externalizer implementations we provide for Infinispan
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ExternalizerTest {

    private static ValueFactory valueFactory = new TestValueFactory();

    private static Random rnd = new Random();

    private static Logger log = LoggerFactory.getLogger(ExternalizerTest.class);

    private static StreamingMarshaller marshaller, hotrod;


    @BeforeClass
    public static void setup() {
        AdvancedExternalizer[] externalizers =  new AdvancedExternalizer[] {
                new UriExternalizer(),
                new BNodeExternalizer(),
                new StringLiteralExternalizer(),
                new DateLiteralExternalizer(),
                new BooleanLiteralExternalizer(),
                new IntLiteralExternalizer(),
                new DoubleLiteralExternalizer(),
                new TripleExternalizer()
        };


        GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                .transport()
                    .defaultTransport()
                .serialization()
                    .addAdvancedExternalizer(externalizers)
                .build();

        Configuration             defaultConfiguration = new ConfigurationBuilder()
                .clustering()
                    .cacheMode(CacheMode.DIST_ASYNC)
                .build();

        EmbeddedCacheManager cacheManager = new DefaultCacheManager(globalConfiguration, defaultConfiguration, true);

        marshaller = cacheManager.getCache().getAdvancedCache().getComponentRegistry().getCacheMarshaller();

        hotrod = new CustomJBossMarshaller();
    }


    @Test
    public void testUriResource() throws Exception {
        marshall((KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8)), new UriExternalizer());
    }

    @Test
    public void testCompressedUriResource() throws Exception {
        marshall((KiWiUriResource) valueFactory.createURI(XSD.Double.stringValue()), new UriExternalizer());
        marshall((KiWiUriResource) valueFactory.createURI(RDFS.LABEL.stringValue()), new UriExternalizer());
        marshall((KiWiUriResource) valueFactory.createURI(OWL.SAMEAS.stringValue()), new UriExternalizer());
    }


    @Test
    public void testBNode() throws Exception {
        marshall((KiWiAnonResource) valueFactory.createBNode(), new BNodeExternalizer());
    }

    @Test
    public void testStringLiteral() throws Exception {
        marshall((KiWiStringLiteral) valueFactory.createLiteral(RandomStringUtils.randomAscii(40)), new StringLiteralExternalizer());
    }

    @Test
    public void testLongStringLiteral() throws Exception {
        marshall((KiWiStringLiteral) valueFactory.createLiteral(RandomStringUtils.random(1000)), new StringLiteralExternalizer());
    }


    @Test
    public void testLangLiteral() throws Exception {
        marshall((KiWiStringLiteral) valueFactory.createLiteral(RandomStringUtils.randomAscii(40),"en"), new StringLiteralExternalizer());
    }

    @Test
    public void testTypeLiteral() throws Exception {
        marshall((KiWiStringLiteral) valueFactory.createLiteral(RandomStringUtils.randomAscii(40),valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8))), new StringLiteralExternalizer());
    }


    @Test
    public void testIntLiteral() throws Exception {
        marshall((KiWiIntLiteral) valueFactory.createLiteral(rnd.nextInt()), new IntLiteralExternalizer());
    }


    @Test
    public void testTriple() throws Exception {
        KiWiUriResource s = (KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        KiWiUriResource p = (KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        KiWiNode o = (KiWiNode) randomNode();
        KiWiTriple t = (KiWiTriple) valueFactory.createStatement(s,p,o);

        marshall(t, new TripleExternalizer());
    }

    @Test
    public void testPrefixCompressedTriple() throws Exception {
        KiWiUriResource s = (KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        KiWiUriResource p = (KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        KiWiUriResource o = (KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        KiWiTriple t = (KiWiTriple) valueFactory.createStatement(s,p,o);

        marshall(t, new TripleExternalizer());
    }


    /**
     * Run the given object through the marshaller using an in-memory stream.
     * @param origin
     * @param <T>
     * @return
     */
    private <T> void marshall(T origin, AdvancedExternalizer<T> externalizer) throws IOException, ClassNotFoundException, InterruptedException {
        log.info("- testing Java ObjectStream ...");
        ByteArrayOutputStream outBytesOS = new ByteArrayOutputStream();
        ObjectOutputStream outOS = new ObjectOutputStream(outBytesOS);

        outOS.writeObject(origin);

        outOS.close();

        log.info("  object {}: serialized with {} bytes", origin, outBytesOS.size());


        log.info("- testing externalizer directly ...");
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outBytes);

        externalizer.writeObject(out, origin);
        out.close();

        log.info("  object {}: serialized with {} bytes", origin, outBytes.size());

        ByteArrayInputStream inBytes = new ByteArrayInputStream(outBytes.toByteArray());
        ObjectInputStream in = new ObjectInputStream(inBytes);

        T destination1 = externalizer.readObject(in);

        Assert.assertEquals(origin,destination1);
        assertEqualsId(origin,destination1);

        log.info("- testing externalizer with infinispan cluster marshaller ...");

        byte[] bytes = marshaller.objectToByteBuffer(origin);
        log.info("  object {}: serialized with {} bytes", origin, bytes.length);

        Object destination2 = marshaller.objectFromByteBuffer(bytes);

        Assert.assertEquals(origin, destination2);
        assertEqualsId(origin, destination2);



        log.info("- testing externalizer with infinispan hotrod marshaller ...");

        byte[] bytesH = hotrod.objectToByteBuffer(origin);
        log.info("  object {}: serialized with {} bytes", origin, bytesH.length);

        Object destination3 = hotrod.objectFromByteBuffer(bytesH);

        Assert.assertEquals(origin, destination3);
        assertEqualsId(origin, destination3);

    }


    /**
     * Return a random RDF value, either a reused object (10% chance) or of any other kind.
     * @return
     */
    protected Value randomNode() {
        Value object;
        switch(rnd.nextInt(6)) {
            case 0: object = valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
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
            default: object = valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
                break;

        }
        return object;
    }
      
    private static <T> void assertEqualsId(T o1, T o2) {
        if(o1 instanceof KiWiNode && o2 instanceof KiWiNode) {
            Assert.assertEquals(((KiWiNode) o1).getId(), ((KiWiNode) o2).getId());
        } else if(o1 instanceof KiWiTriple && o2 instanceof KiWiTriple) {
            Assert.assertEquals(((KiWiTriple) o1).getId(), ((KiWiTriple) o2).getId());
        }

    }

}
