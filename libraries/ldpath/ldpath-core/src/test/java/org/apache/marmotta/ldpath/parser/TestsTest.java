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

package org.apache.marmotta.ldpath.parser;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.tests.NodeTest;
import org.hamcrest.CoreMatchers;
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestsTest {

    @Parameters(name = "{index}: {1}Test")
    public static List<String[]> testCases() {
        return Arrays.asList(new String[][] {
                {"<http://foo.bar> & <http://bar.foo>", "And"},
                {"fn:eq(\"1\", \"2\")", "Function"},
                {"@en", "LiteralLanguage"},
                {"^^<http://foo.bar>", "LiteralType"},
                {"!@en", "Not"},
                {"<http://foo.bar> | <http://bar.foo>", "Or"},
                {"<http://www.example.com/> is <http://foo.bar>", "PathEquality"},
                {"is-a <http://foo.bar>", "IsA"},
                {"<http://www.example.com/>", "Path"},
                });
    }
    
    @Parameter
    public String expr;
    
    @Parameter(1)
    public String name;

    private NodeTest<String> test;
    
    private static NodeBackend<String> backend;

    @BeforeClass
    public static void beforeClass() {
        backend = new StringTestingBackend();
    }
    
    @Before
    public void before() throws ParseException {
        LdPathParser<String> rdfPathParser = new LdPathParser<String>(backend,new StringReader(expr));
        test = rdfPathParser.parseTest(Collections.<String,String>emptyMap());
    }
    
    @Test
    public void testGetPathExpression() {
        Assert.assertThat(test.getPathExpression(backend), IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expr));
    }
    
    @Test
    public void testParseSelector() {
        final String className = "org.apache.marmotta.ldpath.model.tests." + name + "Test";
        try {
            final Class<?> cls = Class.forName(className);
            Assert.assertThat(test, CoreMatchers.instanceOf(cls));
        } catch (ClassNotFoundException e) {
            Assert.fail("Could not load class: " + className);
        }
        
    }
    
    
}
