/**
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
package org.apache.marmotta.ldpath.model.tests.functions.text;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.marmotta.ldpath.api.tests.NodeTest;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.sail.SailRepositoryConnection;

@RunWith(Parameterized.class)
public class StringTestTest extends AbstractTestBase {
    
    @Parameters(name = "Case-{index}")
    public static List<String[]> data() {
        return Arrays.asList(
                new String[] { "Welcome to The Apache Software Foundation!" },
                new String[] { "Apache Marmotta is an Open Platform for Linked Data" }
                );
    }
    private static Random rnd;
    
    @Parameter
    public String text;
    
    private int textLen;
    
    private int testWindow;
    
    private String errSalt;
    
    private URI subject, predicate;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rnd = new Random();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        textLen = text.length();
        Assert.assertTrue(textLen > 4);
        testWindow = 1+rnd.nextInt((textLen / 4));
        
        errSalt = UUID.randomUUID().toString();
        
        subject = repository.getValueFactory().createURI(ns("foo", UUID.randomUUID().toString()));
        predicate = repository.getValueFactory().createURI(ns("foo", UUID.randomUUID().toString()));

        final SailRepositoryConnection con = repository.getConnection();
        try {
            final ValueFactory vf = con.getValueFactory();

            con.add(vf.createStatement(subject, predicate, vf.createLiteral(text)));

            con.commit();
        } finally {
            con.close();
        }
    }

    @After
    public void tearDown() throws Exception {
    }
    
    private boolean checkTest(String ldPathTest, URI context) throws ParseException {
        final LdPathParser<Value> parser = createParserFromString(ldPathTest);
        final NodeTest<Value> test = parser.parseTest(NSS);
        return test.accept(backend, context, context);
    }

    @Test
    public void testEquals() throws ParseException {
        final String ldPath_T = String.format("fn:equals(<%s>, \"%s\")", predicate.stringValue(), text);
        final String ldPath_F = String.format("fn:equals(<%s>, \"%s\")", predicate.stringValue(), errSalt);

        Assert.assertTrue(checkTest(ldPath_T, subject));
        Assert.assertFalse(checkTest(ldPath_F, subject));
    }

    @Test
    public void testEqualsIgnoreCase() throws ParseException {
        final String ldPath_T = String.format("fn:equalsIgnoreCase(<%s>, \"%s\")", predicate.stringValue(), text);
        final String ldPath_T_LC = String.format("fn:equalsIgnoreCase(<%s>, \"%s\")", predicate.stringValue(), text.toLowerCase());
        final String ldPath_T_UC = String.format("fn:equalsIgnoreCase(<%s>, \"%s\")", predicate.stringValue(), text.toUpperCase());

        final String ldPath_F = String.format("fn:equalsIgnoreCase(<%s>, \"%s\")", predicate.stringValue(), errSalt);
        final String ldPath_F_LC = String.format("fn:equalsIgnoreCase(<%s>, \"%s\")", predicate.stringValue(), errSalt.toLowerCase());
        final String ldPath_F_UC = String.format("fn:equalsIgnoreCase(<%s>, \"%s\")", predicate.stringValue(), errSalt.toUpperCase());

        Assert.assertTrue(checkTest(ldPath_T, subject));
        Assert.assertTrue(checkTest(ldPath_T_LC, subject));
        Assert.assertTrue(checkTest(ldPath_T_UC, subject));
        
        Assert.assertFalse(checkTest(ldPath_F, subject));
        Assert.assertFalse(checkTest(ldPath_F_LC, subject));
        Assert.assertFalse(checkTest(ldPath_F_UC, subject));
    }

    @Test
    public void testContains() throws ParseException {
        final String ldPath_T1 = String.format("fn:contains(<%s>, \"%s\")", predicate.stringValue(), text.substring(textLen/2-testWindow, textLen/2+testWindow));
        final String ldPath_T2 = String.format("fn:contains(<%s>, \"%s\")", predicate.stringValue(), text.substring(textLen-testWindow));
        final String ldPath_T3 = String.format("fn:contains(<%s>, \"%s\")", predicate.stringValue(), text.substring(0,testWindow));
        final String ldPath_F = String.format("fn:contains(<%s>, \"%s\")", predicate.stringValue(), UUID.randomUUID().toString());

        Assert.assertTrue(checkTest(ldPath_T1, subject));
        Assert.assertTrue(checkTest(ldPath_T2, subject));
        Assert.assertTrue(checkTest(ldPath_T3, subject));
        Assert.assertFalse(checkTest(ldPath_F, subject));
    }

    @Test
    public void testEndsWith() throws ParseException {
        final String ldPath_S = String.format("fn:endsWith(<%s>, \"%s\")", predicate.stringValue(), text.substring(0,testWindow));
        final String ldPath_M = String.format("fn:endsWith(<%s>, \"%s\")", predicate.stringValue(), text.substring(textLen/2-testWindow, textLen/2+testWindow));
        final String ldPath_E = String.format("fn:endsWith(<%s>, \"%s\")", predicate.stringValue(), text.substring(textLen-testWindow));

        Assert.assertFalse(checkTest(ldPath_S, subject));
        Assert.assertFalse(checkTest(ldPath_M, subject));
        Assert.assertTrue(checkTest(ldPath_E, subject));
    }

    @Test
    public void testStartsWith() throws ParseException {
        final String ldPath_S = String.format("fn:startsWith(<%s>, \"%s\")", predicate.stringValue(), text.substring(0,testWindow));
        final String ldPath_M = String.format("fn:startsWith(<%s>, \"%s\")", predicate.stringValue(), text.substring(textLen/2-testWindow, textLen/2+testWindow));
        final String ldPath_E = String.format("fn:startsWith(<%s>, \"%s\")", predicate.stringValue(), text.substring(textLen-testWindow));

        Assert.assertTrue(checkTest(ldPath_S, subject));
        Assert.assertFalse(checkTest(ldPath_M, subject));
        Assert.assertFalse(checkTest(ldPath_E, subject));
    }

    @Test
    public void testIsEmpty() throws ParseException {
        final String ldPath_T = "fn:isEmpty(\"\")";
        final String ldPath_F = String.format("fn:isEmpty(<%s>)", predicate.stringValue());
        
        Assert.assertTrue(checkTest(ldPath_T, subject));
        Assert.assertFalse(checkTest(ldPath_F, subject));
    }

}
