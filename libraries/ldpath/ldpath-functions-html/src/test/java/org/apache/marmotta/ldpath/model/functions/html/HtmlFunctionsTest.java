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
package org.apache.marmotta.ldpath.model.functions.html;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collection;

import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;


public class HtmlFunctionsTest extends AbstractTestBase {

    private URI resource, prop2;

    @Before
    public void loadData() throws RepositoryException, RDFParseException, IOException {
        super.loadData("data.n3", RDFFormat.N3);
        resource = repository.getValueFactory().createURI(NSS.get("ex") + "Simple");
        prop2 = createURI("foo", "simple");
        final SailRepositoryConnection con = repository.getConnection();
        con.begin();
        con.add(resource, prop2, con.getValueFactory().createLiteral("Und sein Name war <strong>&quot;K&ouml;nig Ruprecht&quot;</stron>"));
        con.commit();
        con.close();
       
    }

    @Test
    public void testCleanHtmlFunction() throws ParseException {

        final LdPathParser<Value> parser = createParserFromString("fn:cleanHtml(foo:html) :: xsd:string");
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);

        final Collection<Object> result = rule.getValues(backend, resource);
        assertEquals(1, result.size());
        final String txt = result.iterator().next().toString();

        assertThat("attribute: id", txt, not(containsString("id=\"")));
        assertThat("element: h1", txt, not(containsString("<h1>")));
        assertThat("element: div", txt, not(containsString("<div>")));
        assertThat("element: p", txt, containsString("<p>"));
    }

    @Test
    public void testCssSelectFunction() throws ParseException {

        final LdPathParser<Value> parser = createParserFromString("fn:css(\"p\", foo:html) :: xsd:string");
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);

        final Collection<Object> result = rule.getValues(backend, resource);
        assertEquals(3, result.size());

        for (Object object : result) {
            String s = object.toString();
            assertThat("String start", s, CoreMatchers.startsWith("<p"));
            assertThat("String end", s, CoreMatchers.endsWith("</p>"));
        }

        final LdPathParser<Value> parser2 = createParserFromString("fn:css(\"p#p2\", foo:html) :: xsd:string");
        final FieldMapping<Object, Value> rule2 = parser2.parseRule(NSS);

        final Collection<Object> result2 = rule2.getValues(backend, resource);
        assertEquals(1, result2.size());

        String txt = result2.iterator().next().toString();
        assertThat(txt, containsString("Most marmots are highly social and use loud whistles to communicate with one another"));

    }

    @Test
    public void testHtmlTextFunction() throws ParseException {
        
        final Collection<Object> values = evaluateRule(String.format("fn:htmlText(foo:html) :: xsd:string"), resource);
        assertEquals(1, values.size());
        assertThat(values, hasItem(equalToIgnoringWhiteSpace("Marmotta Marmotta, italian for \"Marmot\" Marmots are generally large ground squirrels in the genus Marmota, of which there are 15 species. Those most often referred to as marmots tend to live in mountainous areas, such as the Alps, northern Apennines, Eurasian steppes, Carpathians, Tatras, and Pyrenees in Europe and northwestern Asia; the Rocky Mountains, Black Hills, Cascades, and Sierra Nevada in North America; and the Deosai Plateau in Pakistan and Ladakh in India. The groundhog, however, is also sometimes called a marmot, while the similarly sized, but more social, prairie dog is not classified in the genus Marmota but in the related genus Cynomys. Marmots typically live in burrows (often within rockpiles, particularly in the case of the yellow-bellied marmot), and hibernate there through the winter. Most marmots are highly social and use loud whistles to communicate with one another, especially when alarmed. Marmots mainly eat greens and many types of grasses, berries, lichens, mosses, roots and flowers.")));
        
        final Collection<Object> values2 = evaluateRule(String.format("fn:htmlText(foo:simple) :: xsd:string"), resource);
        assertEquals(1, values2.size());
        assertThat(values2, hasItem("Und sein Name war \"König Ruprecht\""));
    }

}
