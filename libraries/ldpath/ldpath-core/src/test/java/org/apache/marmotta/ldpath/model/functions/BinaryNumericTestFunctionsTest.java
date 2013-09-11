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
package org.apache.marmotta.ldpath.model.functions;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.marmotta.ldpath.api.selectors.NodeSelector;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
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
public class BinaryNumericTestFunctionsTest extends AbstractTestBase {

    @Parameters(name = "fn:{0}")
    public static List<String[]> data() {
        ArrayList<String[]> data = new ArrayList<String[]>();
        data.add(new String[] { "eq", "Eq", "Lt,Gt" });
        data.add(new String[] { "ne", "Lt,Gt", "Eq" });
        data.add(new String[] { "lt", "Lt", "Eq,Gt" });
        data.add(new String[] { "le", "Lt,Eq", "Gt" });
        data.add(new String[] { "gt", "Gt", "Eq,Lt" });
        data.add(new String[] { "ge", "Eq,Gt", "Lt" });
        return data;
    }

    @Parameter(0)
    public String funktion;

    @Parameter(1)
    public String expected;

    @Parameter(2)
    public String forbidden;

    @Before
    public void loadData() throws RepositoryException, RDFParseException, IOException {
        super.loadData("/ldpath/test-data.n3", RDFFormat.N3);
    }

    @Test
    public void straight() throws ParseException {
        runTest("fn:" + funktion, expected, forbidden);
    }

    @Test
    public void inverse() throws ParseException {
        runTest("!fn:" + funktion, forbidden, expected);
    }

    private void runTest(String fkt, String pos, String neg) throws ParseException {
        final URI start = repository.getValueFactory().createURI("http://www.example.com/Compare");

        ArrayList<Value> yes = new ArrayList<Value>();
        for (String p : pos.split(",")) {
            yes.add(repository.getValueFactory().createURI(NSS.get("ex") + p.trim()));
        }
        ArrayList<Value> no = new ArrayList<Value>();
        for (String p : neg.split(",")) {
            no.add(repository.getValueFactory().createURI(NSS.get("ex") + p.trim()));
        }


        final LdPathParser<Value> parser = createParserFromString("ex:hasItem[" + fkt + "(foo:left, foo:right)]");
        final NodeSelector<Value> sel = parser.parseSelector(NSS);

        final Collection<Value> result = sel.select(backend, start, null, null);
        assertEquals("items found", yes.size(), result.size());

        for (Value value : yes) {
            assertThat("matching resources", result, hasItem(value));
        }
        for (Value value : no) {
            assertThat("not-matching resources", result, not(hasItem(value)));
        }

    }

}
