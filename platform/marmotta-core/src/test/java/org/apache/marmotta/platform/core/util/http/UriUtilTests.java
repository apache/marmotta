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
package org.apache.marmotta.platform.core.util.http;

import org.apache.marmotta.commons.http.UriUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * URI utilities tests
 * 
 * @author Sergio Fernández
 * 
 */
public class UriUtilTests {

    private final String base = "http://localhost:8080/marmotta/resource/";

    @Before
    public void setup() {

    }

    private String buildUri(String part) {
        return buildUri(base, part);
    }

    private String buildUri(String base, String part) {
        return base + part;
    }

    @Test
    public void validate() {
        assertTrue(UriUtil.validate(buildRightUri()));
    }

    @Test
    public void validateFtp() {
        assertTrue(UriUtil.validate(buildFtpUri()));
    }

    @Test
    public void validateUrn() {
        assertTrue(UriUtil.validate(buildUrnUri()));
    }

    @Test
    public void validateHashl() {
        assertTrue(UriUtil.validate(buildUriHash()));
    }

    @Test
    public void validateQuery() {
        assertTrue(UriUtil.validate(buildUriQuery()));
    }

    @Test
    public void validateEmptyQuery() {
        assertTrue(UriUtil.validate(buildUriEmtpyQuery()));
    }

    @Test
    public void validateQueryUnicode() {
        assertTrue(UriUtil.validate(buildUriQueryUnicode()));
    }

    @Test
    public void validateQueryUnicodeParam() {
        assertTrue(UriUtil.validate(buildUriQueryUnicodeParam()));
    }

    @Test
    public void validateWrong() {
        assertFalse(UriUtil.validate(buildInvalid()));
    }

    @Test
    public void evaluateValidationPerformace() {
        assertTrue(evaluateValidationPerformace(buildRightUri()) >= 0.0);
        assertTrue(evaluateValidationPerformace(buildFtpUri()) >= 0.0);
        assertTrue(evaluateValidationPerformace(buildUrnUri()) >= 0.0);
        assertTrue(evaluateValidationPerformace(buildUriHash()) >= 0.0);
        assertTrue(evaluateValidationPerformace(buildUriQuery()) >= 0.0);
        assertTrue(evaluateValidationPerformace(buildUriEmtpyQuery()) >= 0.0);
        assertTrue(evaluateValidationPerformace(buildInvalid()) >= 0.0);

        // both implementations are very similar about performance
        // since the result could be unpredictable, not assert added
        evaluateValidationPerformace(buildUriQueryUnicode()); //
        evaluateValidationPerformace(buildUriQueryUnicodeParam());
    }

    private long evaluateValidationPerformace(String uri) {
        long start = System.nanoTime();
        UriUtil.validateApache(uri);
        long apache = System.nanoTime() - start;
        start = System.nanoTime();
        UriUtil.validateJavaNet(uri);
        long javanet = System.nanoTime() - start;
        System.out.println("apache " + apache + " ns, " + "java.net " + javanet + " ns (" + uri + ")");
        return apache - javanet;
    }

    private String buildRightUri() {
        return buildUri(UUID.randomUUID().toString());
    }

    private String buildFtpUri() {
        return buildUri(UUID.randomUUID().toString());
    }

    private String buildUrnUri() {
        return buildUri("urn:issn", UUID.randomUUID().toString());
    }

    private String buildUriHash() {
        return buildUri(UUID.randomUUID().toString() + "#");
    }

    private String buildUriQuery() {
        return buildUri(UUID.randomUUID().toString() + "?foo=bar");
    }

    private String buildUriEmtpyQuery() {
        return buildUri(UUID.randomUUID().toString() + "?");
    }

    private String buildUriQueryUnicode() {
        return buildUri(UUID.randomUUID().toString() + "?foo=bár");
    }

    private String buildUriQueryUnicodeParam() {
        return buildUri(UUID.randomUUID().toString() + "?úri=foo");
    }

    private String buildInvalid() {
        return buildUri(UUID.randomUUID().toString()).substring(4);
    }

    @Test
    public void validateCurie() {
        String[] uris = { base + "foo", base + "foo#bar" };
        for (String uri : uris) {
            String ns = UriUtil.getNamespace(uri);
            String ref = UriUtil.getReference(uri);
            assertTrue(uri.startsWith(ns));
            assertTrue(uri.endsWith(ref));
            assertEquals(uri, ns + ref);
        }
    }

    @Test
    public void validateText() {
        assertFalse(UriUtil.validate("131185"));
        assertFalse(UriUtil.validate("foo"));
    }

}
