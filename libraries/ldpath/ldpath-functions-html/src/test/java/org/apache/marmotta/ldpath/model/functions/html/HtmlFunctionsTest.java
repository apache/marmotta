/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collection;

import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.RdfPathParser;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;


public class HtmlFunctionsTest extends AbstractTestBase {

    @Before
    public void loadData() throws RepositoryException, RDFParseException, IOException {
        super.loadData("data.n3", RDFFormat.N3);
    }

    @Test
    public void testCleanHtmlFunction() throws ParseException {
        URI uri = repository.getValueFactory().createURI(NSS.get("ex") + "Simple");

        final RdfPathParser<Value> parser = createParserFromString("fn:cleanHtml(foo:html) :: xsd:string");
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);

        final Collection<Object> result = rule.getValues(backend, uri);
        assertEquals(1, result.size());
        final String txt = result.iterator().next().toString();

        assertThat("attribute: id", txt, not(containsString("id=\"")));
        assertThat("element: h1", txt, not(containsString("<h1>")));
        assertThat("element: div", txt, not(containsString("<div>")));
        assertThat("element: p", txt, containsString("<p>"));
    }

    @Test
    public void testCssSelectFunction() throws ParseException {
        URI uri = repository.getValueFactory().createURI(NSS.get("ex") + "Simple");

        final RdfPathParser<Value> parser = createParserFromString("fn:css(\"p\", foo:html) :: xsd:string");
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);

        final Collection<Object> result = rule.getValues(backend, uri);
        assertEquals(3, result.size());

        for (Object object : result) {
            String s = object.toString();
            assertThat("String start", s, CoreMatchers.startsWith("<p"));
            assertThat("String end", s, CoreMatchers.endsWith("</p>"));
        }

        final RdfPathParser<Value> parser2 = createParserFromString("fn:css(\"p#p2\", foo:html) :: xsd:string");
        final FieldMapping<Object, Value> rule2 = parser2.parseRule(NSS);

        final Collection<Object> result2 = rule2.getValues(backend, uri);
        assertEquals(1, result2.size());

        String txt = result2.iterator().next().toString();
        assertThat(txt, CoreMatchers.containsString("Most marmots are highly social and use loud whistles to communicate with one another"));

    }


}
