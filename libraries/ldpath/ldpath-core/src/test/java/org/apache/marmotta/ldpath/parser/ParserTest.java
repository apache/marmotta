/*
 * Copyright (c) 2013 Salzburg Research.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.marmotta.ldpath.parser;

import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.RdfPathParser;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.tests.NodeTest;
import org.apache.marmotta.ldpath.api.transformers.NodeTransformer;
import org.apache.marmotta.ldpath.model.programs.Program;
import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class ParserTest {

    private static RDFBackend<String> backend;
    private static final String NS_TEST = "http://example.com/";
    private static final String NS_FOO = "http://foo.com/some/path#";

    private static final Map<String, String> NAMESPACES;
    static {
        Map<String, String> ns = new HashMap<String, String>();
        ns.put("test", NS_TEST);
        ns.put("foo", NS_FOO);
        NAMESPACES = Collections.unmodifiableMap(ns);
    }

    @BeforeClass
    public static void initClass() {
        backend = new EmptyTestingBackend();
    }


    @Test
    public void testParseProgram() throws IOException {
        RdfPathParser<String> parser = createParser("program.txt");
        try {
            Program<String> program = parser.parseProgram();
            assertNotNull(program.getField("path"));
            assertNotNull(program.getField("lang_test"));
            assertNotNull(program.getField("type_test"));
            assertNotNull(program.getField("int_s"));
            assertNotNull(program.getField("int_p"));
            assertNotNull(program.getField("inverse"));
            assertNotNull(program.getField("config"));
            assertNotNull(program.getBooster());
            assertNotNull(program.getFilter());

        } catch (ParseException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testParseTest() throws IOException {
        RdfPathParser<String> parser = createParser("test.txt");
        try {
            NodeTest<String> test = parser.parseTest(NAMESPACES);
            assertNotNull(test);
            assertNotNull(test.getPathExpression(backend));
        } catch (ParseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testParsePrefixes() throws IOException {
        RdfPathParser<String> parser = createParser("namespaces.txt");
        try {
            Map<String, String> prefixes = parser.parsePrefixes();
            assertTrue(prefixes.containsKey("test"));
            assertTrue(prefixes.containsKey("foo"));
            assertEquals(NS_TEST, prefixes.get("test"));
            assertEquals(NS_FOO, prefixes.get("foo"));
        } catch (ParseException e) {
            assertFalse(e.getMessage(), true);
        }
    }


    private RdfPathParser<String> createParser(String input) throws IOException {
        final URL resource = ParserTest.class.getResource(input);
        Assume.assumeThat("Could not load test input data '" + input + "'", resource, CoreMatchers.notNullValue());

        RdfPathParser<String> rdfPathParser = new RdfPathParser<String>(backend,new StringReader(IOUtils.toString(resource)));
        rdfPathParser.registerTransformer(NS_TEST + "type", new NodeTransformer<String, String>() {
            @Override
            public String transform(RDFBackend<String> backend, String node)
                    throws IllegalArgumentException {
                return node;
            }
        });
        return rdfPathParser;
    }
}
