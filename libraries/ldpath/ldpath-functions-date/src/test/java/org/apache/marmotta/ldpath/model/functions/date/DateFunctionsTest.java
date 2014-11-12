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
package org.apache.marmotta.ldpath.model.functions.date;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFParseException;

import org.apache.marmotta.commons.util.DateUtils;

public class DateFunctionsTest extends AbstractTestBase {

    private Date now;
    private Date first;
    private URI uri;
    private URI prop;

    @Before
    public void loadData() throws RepositoryException, RDFParseException, IOException {
        final int day = 60 * 60 * 24, delta = day * 365;
        now = new Date(1000*(System.currentTimeMillis() / 1000));
        first = new Date(now.getTime() - 1000l * delta);

        uri = repository.getValueFactory().createURI(NSS.get("ex") + now.getTime());
        prop = repository.getValueFactory().createURI(NSS.get("foo") + "hasPiH"); // Point in History

        final SailRepositoryConnection con = repository.getConnection();

        try {
            final ValueFactory vF = con.getValueFactory();

            con.add(vF.createStatement(uri, prop, vF.createLiteral(DateUtils.getXMLCalendar(first))));
            con.add(vF.createStatement(uri, prop, vF.createLiteral(DateUtils.getXMLCalendar(now))));

            final Random rnd = new Random();
            for (int i = 0; i < 20; i++) {
                Date d = new Date(first.getTime() + (day + rnd.nextInt(delta-2)) * 1000l);
                con.add(vF.createStatement(uri, prop, vF.createLiteral(DateUtils.getXMLCalendar(d))));
            }

            con.commit();
        } finally {
            con.close();
        }
    }

    @Test
    public void testEarliest() throws ParseException {
        final LdPathParser<Value> parser = createParserFromString("fn:earliest(<" + prop.stringValue() + ">) :: xsd:dateTime");
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);
        final Collection<Object> result = rule.getValues(backend, uri);

        Assert.assertEquals(1, result.size());

        final Object obj = result.iterator().next();
        Assert.assertEquals(first, obj);
    }

    @Test
    public void testLatest() throws ParseException {
        final LdPathParser<Value> parser = createParserFromString("fn:latest(<" + prop.stringValue() + ">) :: xsd:dateTime");
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);
        final Collection<Object> result = rule.getValues(backend, uri);

        Assert.assertEquals(1, result.size());

        final Object obj = result.iterator().next();
        Assert.assertEquals(now, obj);
    }

}
