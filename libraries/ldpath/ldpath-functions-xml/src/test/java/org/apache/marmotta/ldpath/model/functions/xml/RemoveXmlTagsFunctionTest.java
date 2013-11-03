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
import java.util.Collection;

import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;


public class RemoveXmlTagsFunctionTest extends AbstractTestBase {

    @Before
    public void loadData() throws RepositoryException, RDFParseException, IOException {
        super.loadData("data.n3", RDFFormat.N3);
    }

    @Test
    public void testRemoveTags() throws ParseException {
        String result = "A quiz is a form of game or mind sport in which the players (as individuals or in teams) attempt to answer questions correctly.";

        final URI context = repository.getValueFactory().createURI(NSS.get("ex") + "Text");

        final LdPathParser<Value> parser = createParserFromString("fn:removeTags(foo:formatted) :: xsd:string");
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);
        final Collection<Object> values = rule.getValues(backend, context);

        Assert.assertEquals(1, values.size());
        Assert.assertEquals(result, values.iterator().next());
    }
}
