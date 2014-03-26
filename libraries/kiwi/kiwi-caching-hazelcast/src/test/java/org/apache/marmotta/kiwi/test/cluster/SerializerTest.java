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

package org.apache.marmotta.kiwi.test.cluster;

import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.nio.serialization.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.commons.vocabulary.SCHEMA;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.kiwi.hazelcast.serializer.*;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.apache.marmotta.kiwi.test.TestValueFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

/**
 * Test the different externalizer implementations we provide for Infinispan
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SerializerTest {

    private static ValueFactory valueFactory = new TestValueFactory();

    private static Random rnd = new Random();

    private static Logger log = LoggerFactory.getLogger(SerializerTest.class);


    private static SerializationService simpleService, fullService;

    @BeforeClass
    public static void setup() {
        simpleService = new SerializationServiceBuilder().build();


        SerializationConfig config = new SerializationConfig();
        SerializerConfig scBNode = new SerializerConfig().setImplementation(new BNodeSerializer()).setTypeClass(KiWiAnonResource.class);
        config.addSerializerConfig(scBNode);

        SerializerConfig scBoolean = new SerializerConfig().setImplementation(new BooleanLiteralSerializer()).setTypeClass(KiWiBooleanLiteral.class);
        config.addSerializerConfig(scBoolean);

        SerializerConfig scDate = new SerializerConfig().setImplementation(new DateLiteralSerializer()).setTypeClass(KiWiDateLiteral.class);
        config.addSerializerConfig(scDate);

        SerializerConfig scDouble = new SerializerConfig().setImplementation(new DoubleLiteralSerializer()).setTypeClass(KiWiDoubleLiteral.class);
        config.addSerializerConfig(scDouble);

        SerializerConfig scInt = new SerializerConfig().setImplementation(new IntLiteralSerializer()).setTypeClass(KiWiIntLiteral.class);
        config.addSerializerConfig(scInt);

        SerializerConfig scString = new SerializerConfig().setImplementation(new StringLiteralSerializer()).setTypeClass(KiWiStringLiteral.class);
        config.addSerializerConfig(scString);

        SerializerConfig scTriple = new SerializerConfig().setImplementation(new TripleSerializer()).setTypeClass(KiWiTriple.class);
        config.addSerializerConfig(scTriple);

        SerializerConfig scUri = new SerializerConfig().setImplementation(new UriSerializer()).setTypeClass(KiWiUriResource.class);
        config.addSerializerConfig(scUri);


        fullService   = new SerializationServiceBuilder().setConfig(config).build();


    }


    @Test
    public void testUriResource() throws Exception {
        marshall((KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8)), new UriSerializer());
    }

    @Test
    public void testCompressedUriResource() throws Exception {
        marshall((KiWiUriResource) valueFactory.createURI(XSD.Double.stringValue()), new UriSerializer());
        marshall((KiWiUriResource) valueFactory.createURI(RDFS.LABEL.stringValue()), new UriSerializer());
        marshall((KiWiUriResource) valueFactory.createURI(OWL.SAMEAS.stringValue()), new UriSerializer());
        marshall((KiWiUriResource) valueFactory.createURI(SCHEMA.Place.stringValue()), new UriSerializer());
        marshall((KiWiUriResource) valueFactory.createURI("http://dbpedia.org/resource/Colorado"), new UriSerializer());
        marshall((KiWiUriResource) valueFactory.createURI("http://rdf.freebase.com/ns/test"), new UriSerializer());
    }


    @Test
    public void testBNode() throws Exception {
        marshall((KiWiAnonResource) valueFactory.createBNode(), new BNodeSerializer());
    }

    @Test
    public void testStringLiteral() throws Exception {
        marshall((KiWiStringLiteral) valueFactory.createLiteral(RandomStringUtils.randomAscii(40)), new StringLiteralSerializer());
    }

    @Test
    public void testLangLiteral() throws Exception {
        marshall((KiWiStringLiteral) valueFactory.createLiteral(RandomStringUtils.randomAscii(40),"en"), new StringLiteralSerializer());
    }

    @Test
    public void testTypeLiteral() throws Exception {
        marshall((KiWiStringLiteral) valueFactory.createLiteral(RandomStringUtils.randomAscii(40),valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8))), new StringLiteralSerializer());
    }


    @Test
    public void testIntLiteral() throws Exception {
        marshall((KiWiIntLiteral) valueFactory.createLiteral(rnd.nextInt()), new IntLiteralSerializer());
    }


    @Test
    public void testTriple() throws Exception {
        KiWiUriResource s = (KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        KiWiUriResource p = (KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        KiWiNode o = (KiWiNode) randomNode();
        KiWiTriple t = (KiWiTriple) valueFactory.createStatement(s,p,o);

        marshall(t, new TripleSerializer());
    }

    @Test
    public void testPrefixCompressedTriple() throws Exception {
        KiWiUriResource s = (KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        KiWiUriResource p = (KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        KiWiUriResource o = (KiWiUriResource) valueFactory.createURI("http://localhost/" + RandomStringUtils.randomAlphanumeric(8));
        KiWiTriple t = (KiWiTriple) valueFactory.createStatement(s,p,o);

        marshall(t, new TripleSerializer());
    }


    /**
     * Run the given object through the marshaller using an in-memory stream.
     * @param origin
     * @param <T>
     * @return
     */
    private <T> void marshall(T origin, StreamSerializer<T> externalizer) throws IOException, ClassNotFoundException, InterruptedException {
        log.info("- testing Java ObjectStream ...");
        ByteArrayOutputStream outBytesOS = new ByteArrayOutputStream();
        ObjectOutputStream outOS = new ObjectOutputStream(outBytesOS);

        outOS.writeObject(origin);

        outOS.close();

        log.info("  object {}: serialized with {} bytes", origin, outBytesOS.size());


        log.info("- testing serializer directly ...");
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        ObjectDataOutputStream out = new ObjectDataOutputStream(outBytes, simpleService);

        externalizer.write(out, origin);
        out.close();

        log.info("  object {}: serialized with {} bytes", origin, outBytes.size());

        ByteArrayInputStream inBytes = new ByteArrayInputStream(outBytes.toByteArray());
        ObjectDataInputStream in = new ObjectDataInputStream(inBytes, simpleService);

        T destination1 = externalizer.read(in);

        Assert.assertEquals(origin,destination1);



        log.info("- testing serializer with Hazelcast serialization service ...");


        ByteArrayOutputStream outBytesFull = new ByteArrayOutputStream();
        ObjectDataOutputStream outFull = new ObjectDataOutputStream(outBytesFull, fullService);

        fullService.writeObject(outFull, origin);
        outFull.close();

        log.info("  object {}: serialized with {} bytes", origin, outBytesFull.size());

        ByteArrayInputStream inBytesFull = new ByteArrayInputStream(outBytesFull.toByteArray());
        ObjectDataInputStream inFull = new ObjectDataInputStream(inBytesFull, fullService);

        T destination2 = (T) fullService.readObject(inFull);

        Assert.assertEquals(origin, destination2);

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

}
