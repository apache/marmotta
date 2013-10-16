/*
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
package org.apache.marmotta.ldpath.model.functions.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;


@RunWith(Parameterized.class)
public class XPathFunctionTest extends AbstractTestBase {

    @Parameters(name = "{1}")
    public static List<String[]> data() {
        ArrayList<String[]> data = new ArrayList<String[]>();
        data.add(new String[] { "1", "The Pacific Ocean" });
        data.add(new String[] { "2", "Magma" });
        data.add(new String[] { "3", "Red" });
        data.add(new String[] { "4", "Oxygen" });
        data.add(new String[] { "5", "Jupiter" });
        data.add(new String[] { "6", "Titan" });
        data.add(new String[] { "7", "Mercury" });
        data.add(new String[] { "8", "11" });
        data.add(new String[] { "9", "9" });
        data.add(new String[] { "10", "The median" });
        return data;
    }

    @Parameter
    public String index;
    @Parameter(1)
    public String answer;

    @Before
    public void loadData() throws RepositoryException, RDFParseException, IOException {
        super.loadData("data.n3", RDFFormat.N3);
    }

    @Test
    public void testXpathFunction() throws ParseException {
        final URI ctx = repository.getValueFactory().createURI(NSS.get("ex") + "Quiz");

        final LdPathParser<Value> parser = createParserFromString("fn:xpath(\"/quiz/question[" +
                index +
                "]/answers/answer[@correct='true']/text()\", foo:xml) :: xsd:string");
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);
        final Collection<Object> values = rule.getValues(backend, ctx);

        Assert.assertEquals(1, values.size());
        Assert.assertEquals(answer, values.iterator().next());
    }
}
