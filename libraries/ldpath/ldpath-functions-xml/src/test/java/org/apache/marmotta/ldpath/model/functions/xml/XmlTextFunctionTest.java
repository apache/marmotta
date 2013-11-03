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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;

/**
 * @author Jakob Frank <jakob@apache.org>
 */
public class XmlTextFunctionTest extends AbstractTestBase {

    private static final String XML_TEXT = "<text>This is some text -> with XML-Stuff (tags &amp; entities) and an umlaut (&#x00D6;) contained.</text>";
    private static final String TEXT = "This is some text -> with XML-Stuff (tags & entities) and an umlaut (Ö) contained.";
    private static final String UNESCAPED = "<text>This is some text -> with XML-Stuff (tags & entities) and an umlaut (Ö) contained.</text>";
    
    private URI resource;
    private URI prop;

    @Before
    public void setUp() {
        try {
            final SailRepositoryConnection conn = repository.getConnection();
            try {
                conn.begin();
                resource = createURI("foo", "Start");
                prop = createURI("ex", "text");
                
                conn.add(resource, prop, conn.getValueFactory().createLiteral(XML_TEXT));
                
                conn.commit();
            } catch (final Throwable t) {
                conn.rollback();
                fail(t.getMessage());
            } finally {
                conn.close();
            }
        } catch (RepositoryException e) {
            fail("Could not create test-data");
        }
    }

    @Test
    public void testXmlTextFunction() throws ParseException {
        final Collection<Object> values = evaluateRule(String.format("fn:xmlText(<%s>) :: xsd:string", prop), resource);
        
        assertEquals(1, values.size());
        assertThat(values, CoreMatchers.hasItem(TEXT));
    }
    
    @Test
    public void testXmlUnescapeFunction() throws ParseException {
        final Collection<Object> values = evaluateRule(String.format("fn:xmlUnescape(<%s>) :: xsd:string", prop), resource);
        
        assertEquals(1, values.size());
        assertThat(values, CoreMatchers.hasItem(UNESCAPED));
    }
    
}
