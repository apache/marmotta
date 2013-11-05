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

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collection;

import org.apache.marmotta.ldpath.api.selectors.NodeSelector;
import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;



public class FunctionsTest extends AbstractTestBase {


    @Before
    public void loadData() throws RepositoryException, RDFParseException, IOException {
        loadData("/ldpath/test-data.n3", RDFFormat.N3);
    }

    @Test
    public void testConcat() throws ParseException {

        LdPathParser<Value> parser = createParserFromString("fn:concat(foo:title, \" \", foo:subtitle) :: xsd:string; ");
        final URI context = repository.getValueFactory().createURI("http://www.example.com/1");

        final FieldMapping<Object, Value> field = parser.parseRule(NSS);
        final Collection<Object> result = field.getValues(backend, context);

        assertThat(result, CoreMatchers.hasItem("One SubOne"));
    }

    @Test
    public void testFirst() throws ParseException {
        final URI ctx1 = repository.getValueFactory().createURI("http://www.example.com/1");
        final URI ctx2 = repository.getValueFactory().createURI("http://www.example.com/2");

        LdPathParser<Value> parser = createParserFromString("fn:first(foo:not_valid, foo:title, foo:subtitle, foo:not_valid2) :: xsd:string; ");
        final FieldMapping<Object, Value> field = parser.parseRule(NSS);

        final Collection<Object> result = field.getValues(backend, ctx1);
        assertEquals(1, result.size());
        assertThat(result, CoreMatchers.hasItem("One"));

        final Collection<Object> result2 = field.getValues(backend, ctx2);
        assertEquals(1, result2.size());
        assertThat(result2, CoreMatchers.hasItem("Two"));
    }

    @Test
    public void testFirst2() throws ParseException {
        final URI ctx1 = repository.getValueFactory().createURI("http://www.example.com/1");
        final URI ctx2 = repository.getValueFactory().createURI("http://www.example.com/2");

        LdPathParser<Value> parser = createParserFromString("fn:first(foo:i) :: xsd:int; ");
        final FieldMapping<Object, Value> field = parser.parseRule(NSS);

        final Collection<Object> result = field.getValues(backend, ctx1);
        assertEquals(3, result.size());
        assertThat(result, CoreMatchers.hasItem(1));

        final Collection<Object> result2 = field.getValues(backend, ctx2);
        assertEquals(1, result2.size());
        assertThat(result2, CoreMatchers.hasItem(99));
    }

    @Test
    public void testLast() throws ParseException {
        final URI ctx1 = repository.getValueFactory().createURI("http://www.example.com/1");
        final URI ctx2 = repository.getValueFactory().createURI("http://www.example.com/2");

        LdPathParser<Value> parser = createParserFromString("fn:last(foo:not_valid, foo:title, foo:subtitle, foo:not_valid2) :: xsd:string; ");
        final FieldMapping<Object, Value> field = parser.parseRule(NSS);

        final Collection<Object> result = field.getValues(backend, ctx1);
        assertEquals(1, result.size());
        assertThat(result, CoreMatchers.hasItem("SubOne"));

        final Collection<Object> result2 = field.getValues(backend, ctx2);
        assertEquals(1, result2.size());
        assertThat(result2, hasItem("SubTwo"));
    }

    @Test
    public void testLast2() throws ParseException {
        final URI ctx1 = repository.getValueFactory().createURI("http://www.example.com/1");
        final URI ctx2 = repository.getValueFactory().createURI("http://www.example.com/2");

        LdPathParser<Value> parser = createParserFromString("fn:last(foo:i, ex:not_here) :: xsd:int; ");
        final FieldMapping<Object, Value> field = parser.parseRule(NSS);

        final Collection<Object> result = field.getValues(backend, ctx1);
        assertEquals(3, result.size());
        assertThat(result, CoreMatchers.<Object> hasItems(1, 2, 3));

        final Collection<Object> result2 = field.getValues(backend, ctx2);
        assertEquals(1, result2.size());
        assertThat(result2, hasItem(99));
    }

    @Test
    public void testEq() throws ParseException {
        final URI start = repository.getValueFactory().createURI("http://www.example.com/start");
        final URI ex1 = repository.getValueFactory().createURI("http://www.example.com/1");
        final URI ex2 = repository.getValueFactory().createURI("http://www.example.com/2");

        final LdPathParser<Value> parser = createParserFromString("ex:hasItem[fn:eq(foo:i, foo:j)]");
        final NodeSelector<Value> sel = parser.parseSelector(NSS);

        final Collection<Value> result = sel.select(backend, start, null, null);
        assertEquals(1, result.size());
        assertThat(result, allOf(hasItem(ex2), not(hasItem(ex1))));

        final LdPathParser<Value> parseri = createParserFromString("ex:hasItem[! fn:eq(foo:i, foo:j)]");
        final NodeSelector<Value> seli = parseri.parseSelector(NSS);

        final Collection<Value> resulti = seli.select(backend, start, null, null);
        assertEquals(1, resulti.size());
        assertThat(resulti, allOf(hasItem(ex1), not(hasItem(ex2))));

    }
}
