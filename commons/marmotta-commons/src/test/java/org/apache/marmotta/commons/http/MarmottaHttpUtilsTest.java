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
package org.apache.marmotta.commons.http;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * MarmottaHttpUtils tests
 * 
 * @author Sergio Fernández
 */
public class MarmottaHttpUtilsTest {

    @Test
    public void testConentTypeMatching() throws Exception {
        assertEquals("application/rdf+xml", MarmottaHttpUtils.bestContentType(ImmutableList.of(new ContentType("application", "rdf+xml")), ImmutableList.of(new ContentType("application", "rdf+xml"))).getMime());
        assertEquals("application/rdf+xml", MarmottaHttpUtils.bestContentType(ImmutableList.of(new ContentType("application", "rdf+xml")), ImmutableList.of(new ContentType("application", "xml"))).getMime());
        assertEquals(null, MarmottaHttpUtils.bestContentType(ImmutableList.of(new ContentType("text", "tutle")), ImmutableList.of(new ContentType("text", "plain"))));
    }

    @Test
    public void testParseAcceptHeader() throws Exception {
    	List<ContentType> acceptedTypes = MarmottaHttpUtils.parseAcceptHeaders(	ImmutableList.of(
    			  "application/n-triples;q=0.7,"
    			+ " text/plain;q=0.7,"
    			+ " application/rdf+xml;q=0.8,"
    			+ " application/xml;q=0.8,"
    			+ " text/turtle,"
    			+ " application/x-turtle,"
    			+ " application/trig;q=0.8,"
    			+ " application/x-trig;q=0.8"));
    	List<ContentType> offeredTypes = MarmottaHttpUtils.parseAcceptHeaders(ImmutableList.of(
    			"application/ld+json; q=1.0",
    			"application/x-turtle; q=1.0",
    			"application/x-trig; q=1.0",
    			"application/rdf+xml; q=1.0",
    			"text/turtle; q=1.0",
    			"text/rdf+n3; q=1.0", 
    			"application/trix; q=1.0", 
    			"application/rdf+json; q=1.0", 
    			"text/n3; q=1.0",
    			"text/x-nquads; q=1.0"));
    	assertEquals(new ContentType("text", "turtle", 1.0), MarmottaHttpUtils.bestContentType(offeredTypes, acceptedTypes));

    }
}
