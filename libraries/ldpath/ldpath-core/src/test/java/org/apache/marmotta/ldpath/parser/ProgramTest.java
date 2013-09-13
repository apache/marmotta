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

import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.transformers.NodeTransformer;
import org.apache.marmotta.ldpath.model.programs.Program;
import org.hamcrest.CoreMatchers;
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProgramTest {
    
    private static StringTestingBackend backend;

    private String expr;

    private Program<String> program;

    @BeforeClass
    public static void beforeClass() {
        backend = new StringTestingBackend();
    }

    
    @Before
    public void before() throws ParseException, IOException {
        final URL resource = ParserTest.class.getResource("/parse/program.ldpath");
        assertThat("Could not load test input data '/parse/program.ldpath'", resource, CoreMatchers.notNullValue());

        expr = IOUtils.toString(resource);
        
        LdPathParser<String> rdfPathParser = new LdPathParser<String>(backend,new StringReader(expr));
        rdfPathParser.registerTransformer("http://example.com/type", new NodeTransformer<String, String>() {
            @Override
            public String transform(RDFBackend<String> backend, String node, Map<String,String> configuration)
                    throws IllegalArgumentException {
                return node;
            }
        });

        program = rdfPathParser.parseProgram();
        
        expr = expr.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "");
    }

    @Test
    public void testGetPathExpression() {
        final String result = program.getPathExpression(backend);
        Assert.assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expr));
    }
}
