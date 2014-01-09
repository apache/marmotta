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

package org.apache.marmotta.commons.sesame.facading.primitive;

import java.util.Random;
import java.util.UUID;

import org.apache.marmotta.commons.sesame.facading.AbstractFacadingTest;
import org.apache.marmotta.commons.sesame.facading.FacadingFactory;
import org.apache.marmotta.commons.sesame.facading.api.Facading;
import org.apache.marmotta.commons.sesame.facading.primitive.model.Primitive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class PrimitiveFacadingTest extends AbstractFacadingTest {

    private static final int ITERATIONS = 2500;
    
    private URI subject;
    private Random random;
    private RepositoryConnection facadingConnection;
    private Primitive primitive;

    @Before
    public void before() throws Exception {
        subject = repositoryRDF.getValueFactory().createURI("urn:", UUID.randomUUID().toString());
        random = new Random();

        facadingConnection = repositoryRDF.getConnection();
        facadingConnection.begin();
        final Facading factory = FacadingFactory.createFacading(facadingConnection);
        primitive = factory.createFacade(subject, Primitive.class);

    }

    @After
    public void after() throws RepositoryException {
        if (facadingConnection != null) {
            if (facadingConnection.isActive()) facadingConnection.rollback();
            facadingConnection.close();
        }
    }

    @Test
    public void testInt() {

        for (int j =0; j < ITERATIONS; j++) {
            Assert.assertEquals(0, primitive.getInt());
        }
        final int i = random.nextInt();
        primitive.setInt(i);
        Assert.assertEquals(i, primitive.getInt());
    }
    
    @Test
    public void testByte() {
        final byte b = (byte) random.nextInt(Byte.MAX_VALUE);
        
        Assert.assertEquals(0, primitive.getByte());
        primitive.setByte(b);
        Assert.assertEquals(b, primitive.getByte());
    }

    @Test
    public void testFloat() {
        final float f = random.nextFloat();
        
        Assert.assertEquals(0f, primitive.getFloat(), 10e-9);
        primitive.setFloat(f);
        Assert.assertEquals(f, primitive.getFloat(), 10e-9);
    }

    @Test
    public void testDouble() {
        final double d = random.nextDouble();
        
        Assert.assertEquals(0d, primitive.getDouble(), 10e-12);
        primitive.setDouble(d);
        Assert.assertEquals(d, primitive.getDouble(), 10e-12);
    }

    @Test
    public void testLong() {
        final long l = random.nextLong();
        
        Assert.assertEquals(0l, primitive.getLong());
        primitive.setLong(l);
        Assert.assertEquals(l, primitive.getLong());
    }

 
    @Test
    public void testGetBoolean() {
        Assert.assertFalse(primitive.getBoolean());
        primitive.setBoolean(false);
        Assert.assertFalse(primitive.getBoolean());
        primitive.setBoolean(true);
        Assert.assertTrue(primitive.getBoolean());
    }

    @Test
    public void testGetChar() {
        final char c = (char) random.nextInt(255);
        
        Assert.assertEquals(0, primitive.getChar());
        primitive.setChar(c);
        Assert.assertEquals(c, primitive.getChar());
    }

}
