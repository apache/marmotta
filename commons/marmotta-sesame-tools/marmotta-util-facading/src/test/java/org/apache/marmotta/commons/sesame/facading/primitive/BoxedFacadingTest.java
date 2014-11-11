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

import org.apache.marmotta.commons.sesame.facading.AbstractFacadingTest;
import org.apache.marmotta.commons.sesame.facading.FacadingFactory;
import org.apache.marmotta.commons.sesame.facading.api.Facading;
import org.apache.marmotta.commons.sesame.facading.primitive.model.Boxed;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class BoxedFacadingTest extends AbstractFacadingTest {

    private Random random;
    private RepositoryConnection facadingConnection;
    private Boxed boxed;

    @Before
    public void before() throws Exception {
        final URI subject = repositoryRDF.getValueFactory().createURI("urn:", UUID.randomUUID().toString());
        random = new Random();

        facadingConnection = repositoryRDF.getConnection();
        facadingConnection.begin();
        final Facading factory = FacadingFactory.createFacading(facadingConnection);
        boxed = factory.createFacade(subject, Boxed.class);
    }

    @After
    public void after() throws Exception {
        if (facadingConnection != null) {
            if (facadingConnection.isActive()) facadingConnection.rollback();
            facadingConnection.close();
        }
    }
    
    @Test
    public void testString() throws RepositoryException {
        final String string = UUID.randomUUID().toString();

        Assert.assertNull(boxed.getString());
        boxed.setString(string);
        Assert.assertEquals(string, boxed.getString());
    }

    @Test
    public void testInteger() {
        final Integer i = new Integer(random.nextInt());
        
        Assert.assertEquals(new Integer(0), boxed.getInteger());
        boxed.setInteger(i);
        Assert.assertEquals(i, boxed.getInteger());
    }

    @Test
    public void testLong() {
        final Long l = new Long(random.nextLong());
        
        Assert.assertEquals(new Long(0), boxed.getLong());
        boxed.setLong(l);
        Assert.assertEquals(l, boxed.getLong());
    }

    @Test
    public void testFloat() {
        final Float f = new Float(random.nextFloat());
        
        Assert.assertEquals(0f, boxed.getFloat().floatValue(), 10e-9);
        boxed.setFloat(f);
        Assert.assertEquals(f, boxed.getFloat().floatValue(), 10e-9);
    }

    @Test
    public void testDouble() {
        final Double d = new Double(random.nextDouble());
        
        Assert.assertEquals(0d, boxed.getDouble().doubleValue(), 10e-12);
        boxed.setDouble(d);
        Assert.assertEquals(d, boxed.getDouble().doubleValue(), 10e-12);
    }

    @Test
    public void testCharacter() {
        final Character c = new Character((char) random.nextInt(255));
        
        Assert.assertNull(boxed.getCharacter());
        boxed.setCharacter(c);
        Assert.assertEquals(c, boxed.getCharacter());
    }

    @Test
    public void testByte() {
        final Byte b = new Byte((byte) random.nextInt(Byte.MAX_VALUE));
                
        Assert.assertEquals(new Byte((byte)0), boxed.getByte());
        boxed.setByte(b);
        Assert.assertEquals(b, boxed.getByte());
    }

    @Test
    public void testBoolean() {
        Assert.assertFalse(boxed.getBoolean());
        boxed.setBoolean(Boolean.FALSE);
        Assert.assertFalse(boxed.getBoolean());
        boxed.setBoolean(Boolean.TRUE);
        Assert.assertTrue(boxed.getBoolean());
    }

    @Test
    public void testLocale() {
        Assert.assertNull(boxed.getLocale());
        
        for (Locale l: Locale.getAvailableLocales()) {
            // FIXME: This is to avoid MARMOTTA-559
            if (l.toString().contains("#")) continue;

            boxed.setLocale(l);

            final Locale locale = boxed.getLocale();

            Assert.assertNotNull("Locale " + l + "not properly unboxed",locale);
            //Assert.assertEquals(l, locale);
            Assert.assertEquals(l.getDisplayLanguage(), locale.getDisplayLanguage());
        }
    }

    @Test
    public void testGetDate() {
        /* Dates are stored with seconds-precision */
        final Date d = new Date(1000*((random.nextLong()%System.currentTimeMillis())/1000));
        
        Assert.assertNull(boxed.getDate());
        boxed.setDate(d);
        Assert.assertEquals(d, boxed.getDate());
    }


}
