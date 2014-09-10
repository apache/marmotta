/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.platform.ldp.webservices;

import com.jayway.restassured.RestAssured;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.commons.sesame.test.SesameMatchers;
import org.apache.marmotta.commons.util.HashUtils;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.ldp.util.EntityTagUtils;
import org.apache.marmotta.platform.ldp.util.LdpUtils;
import org.apache.marmotta.platform.ldp.webservices.util.HeaderMatchers;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.ldp.testsuite.matcher.HttpStatusSuccessMatcher;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * Testing LDP web services
 *
 * @author Sergio Fernández
 * @author Jakob Frank
 */
public class LdpWebServiceTest {

    private static Logger log = LoggerFactory.getLogger(LdpWebServiceTest.class);

    private static JettyMarmotta marmotta;

    private static String baseUrl;

    private static String testResourceTTL;

    @BeforeClass
    public static void setup() throws MarmottaImportException, URISyntaxException, IOException {
        marmotta = new JettyMarmotta("/marmotta", LdpWebService.class);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();
        baseUrl = UriBuilder.fromUri("http://localhost").port(marmotta.getPort()).path(marmotta.getContext()).build().toString();

        //initialization
        testResourceTTL = IOUtils.toString(LdpWebServiceTest.class.getResourceAsStream("/test.ttl"), "utf8");
    }

    @AfterClass
    public static void shutdown() {
        marmotta.shutdown();
        marmotta = null;
        testResourceTTL = null;
    }

    @Test
    public void testCRUD() {
        final String resourceName = "resource1";

        // The container
        final String container = createTestContainer();
        final String mimeType = RDFFormat.TURTLE.getDefaultMIMEType();

        // Create
        final String newResource = RestAssured
            .given()
                .header("Slug", resourceName)
                .body(testResourceTTL.getBytes())
                .contentType(mimeType)
            .expect()
                .statusCode(201)
            .post(container)
                .header(HttpHeaders.LOCATION);

        // now the container hasType
        log.info("200 - container");
        RestAssured
            .given()
                .header("Accept", mimeType)
            .expect()
                .statusCode(200)
                .header("Link", CoreMatchers.anyOf( //TODO: RestAssured only checks the FIRST header...
                        HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"),
                        HeaderMatchers.isLink(LDP.BasicContainer.stringValue(), "type"))
                )
                .header("ETag", HeaderMatchers.hasEntityTag(true)) // FIXME: be more specific here
                .contentType(mimeType)
                .body(SesameMatchers.rdfStringMatches(mimeType, container,
                        SesameMatchers.hasStatement(new URIImpl(container), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(container), RDF.TYPE, LDP.BasicContainer)
                ))
            .get(container);

        // also the new resource hasType
        RestAssured
            .given()
                .header("Accept", mimeType)
            .expect()
                .statusCode(200)
                .header("Link", CoreMatchers.anyOf( //TODO: RestAssured only checks the FIRST header...
                        HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"),
                        HeaderMatchers.isLink(LDP.Resource.stringValue(), "type"))
                )
                .header("ETag", HeaderMatchers.hasEntityTag(true)) // FIXME: be more specific here
                .contentType(mimeType)
                .body(SesameMatchers.rdfStringMatches(mimeType, container,
                        SesameMatchers.hasStatement(new URIImpl(newResource), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(newResource), RDF.TYPE, LDP.Resource)
                ))
            .get(newResource);

        // delete
        RestAssured
            .expect()
                .statusCode(204)
                .header("Link", HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"))
                .header("ETag", HeaderMatchers.headerNotPresent())
                .header("Last-Modified", HeaderMatchers.headerNotPresent())
            .delete(newResource);

        // now the new resource does not exist any more.
        RestAssured
            .given()
                .header("Accept", mimeType)
            .expect()
                .statusCode(410)
            .get(newResource);

    }

    @Test
    public void testNR() throws IOException {
        final String resourceName = "resource1";

        // The container
        final String container = createTestContainer();
        final String mimeType = "image/png";

        // Create
        final String binaryResource = RestAssured
            .given()
                .header("Slug", resourceName)
                .body(IOUtils.toByteArray(LdpWebServiceTest.class.getResourceAsStream("/test.png")))
                .contentType(mimeType)
            .expect()
                .statusCode(201)
                .header("Link", CoreMatchers.anyOf( //TODO: RestAssured only checks the FIRST header...
                        //  HeaderMatchers.isLink(metaResource, "describedby"),
                        HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"),
                        HeaderMatchers.isLink(LDP.BasicContainer.stringValue(), "type"))
                )
            .post(container)
                .getHeader("Location");



        final String metaResource = binaryResource.replaceFirst("\\.png$", "");

        // now the container hasType
        RestAssured
            .given()
                .header("Accept", RDFFormat.TURTLE.getDefaultMIMEType())
            .expect()
                .statusCode(200)
                .header("Link", CoreMatchers.anyOf( //TODO: RestAssured only checks the FIRST header...
                        HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"),
                        HeaderMatchers.isLink(LDP.BasicContainer.stringValue(), "type"))
                )
                .header("ETag", HeaderMatchers.hasEntityTag(true)) // FIXME: be more specific here
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
                .body(SesameMatchers.rdfStringMatches(RDFFormat.TURTLE.getDefaultMIMEType(), container,
                        SesameMatchers.hasStatement(new URIImpl(container), RDF.TYPE, LDP.Resource),
                        SesameMatchers.hasStatement(new URIImpl(container), RDF.TYPE, LDP.RDFSource),
                        SesameMatchers.hasStatement(new URIImpl(container), RDF.TYPE, LDP.Container),
                        SesameMatchers.hasStatement(new URIImpl(container), RDF.TYPE, LDP.BasicContainer),
                        SesameMatchers.hasStatement(new URIImpl(container), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(container), LDP.contains, new URIImpl(binaryResource)))
                )
            .get(container);


        // now the resource hasType
        RestAssured
            .given()
                .header("Accept", RDFFormat.TURTLE.getDefaultMIMEType())
            .expect()
                .statusCode(200)
                .header("Link", CoreMatchers.anyOf( //TODO: RestAssured only checks the FIRST header...
                        HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"),
                        HeaderMatchers.isLink(LDP.Resource.stringValue(), "type"),
                        HeaderMatchers.isLink(LDP.RDFSource.stringValue(), "type"))
                )
                .header("ETag", HeaderMatchers.hasEntityTag(true)) // FIXME: be more specific here
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
                .body(SesameMatchers.rdfStringMatches(RDFFormat.TURTLE.getDefaultMIMEType(), metaResource,
                        SesameMatchers.hasStatement(new URIImpl(metaResource), RDF.TYPE, LDP.Resource),
                        SesameMatchers.hasStatement(new URIImpl(metaResource), RDF.TYPE, LDP.RDFSource),
                        SesameMatchers.hasStatement(new URIImpl(metaResource), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(metaResource), DCTERMS.HAS_FORMAT, new URIImpl(binaryResource))
                ))
            .get(metaResource);

        // now the resource hasType
        RestAssured
            .given()
                .header("Accept", RDFFormat.TURTLE.getDefaultMIMEType())
            .expect()
                .statusCode(200)
                .header("Link", CoreMatchers.anyOf( //TODO: RestAssured only checks the FIRST header...
                        HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"),
                        HeaderMatchers.isLink(LDP.Resource.stringValue(), "type"),
                        HeaderMatchers.isLink(LDP.NonRDFSource.stringValue(), "type"))
                )
                .header("ETag", HeaderMatchers.hasEntityTag(false)) // FIXME: be more specific here
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
                .body(SesameMatchers.rdfStringMatches(RDFFormat.TURTLE.getDefaultMIMEType(), binaryResource,
                        SesameMatchers.hasStatement(new URIImpl(binaryResource), RDF.TYPE, LDP.Resource),
                        SesameMatchers.hasStatement(new URIImpl(binaryResource), RDF.TYPE, LDP.NonRDFSource),
                        SesameMatchers.hasStatement(new URIImpl(binaryResource), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(binaryResource), DCTERMS.FORMAT, new LiteralImpl(mimeType)),
                        SesameMatchers.hasStatement(new URIImpl(binaryResource), DCTERMS.IS_FORMAT_OF, new URIImpl(metaResource))
                ))
            .get(binaryResource);

        // now check that the data is really there
        final String expectedMD5 = HashUtils.md5sum(LdpWebServiceTest.class.getResourceAsStream("/test.png"));
        final byte[] data = RestAssured
            .given()
                .header("Accept", mimeType)
            .expect()
                .statusCode(200)
                .header("Link", CoreMatchers.anyOf( //TODO: RestAssured only checks the FIRST header...
                        HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"),
                        HeaderMatchers.isLink(LDP.Resource.stringValue(), "type"),
                        HeaderMatchers.isLink(LDP.RDFSource.stringValue(), "type"))
                )
                .header("ETag", HeaderMatchers.hasEntityTag(false)) // FIXME: be more specific here
                .contentType(mimeType)
            .get(binaryResource)
                .body().asByteArray();

        assertEquals("md5sum",expectedMD5, HashUtils.md5sum(data));
    }

    @Test
    public void testInteractionModel() throws Exception {
        final String container = createTestContainer();

        // Try LDPR
        final String ldpr = RestAssured
            .given()
                .header("Link", Link.fromUri(LDP.Resource.stringValue()).rel("type").build().toString())
                .body(testResourceTTL.getBytes())
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
            .expect()
                .statusCode(201)
            .post(container)
                .getHeader("Location");

        // Now POSTing to the ldpr should fail
        RestAssured
            .given()
                .body(testResourceTTL.getBytes())
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
            .expect()
                .statusCode(405)
            .post(ldpr);

        // Try an invalid interaction model
        RestAssured
            .given()
                .header("Link", Link.fromUri(baseUrl).rel("type").build().toString())
                .body(testResourceTTL.getBytes())
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
            .expect()
                .statusCode(400)
            .post(container);
    }

    @Test
    public void testPUT() throws Exception {
        final String container = createTestContainer();

        final String put_valid = IOUtils.toString(LdpWebServiceTest.class.getResourceAsStream("/test_update.ttl"), "utf8");
        final String put_invalid = IOUtils.toString(LdpWebServiceTest.class.getResourceAsStream("/test_update_invalid.ttl"), "utf8");


        // Create a resource
        final String resource = RestAssured
            .given()
                .header("Slug", "PUT")
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
                .body(testResourceTTL.getBytes())
            .expect()
                .statusCode(201)
            .post(container)
                .getHeader("Location");
        final URI uri = new URIImpl(resource);

        // Check the data is there
        EntityTag etag = EntityTagUtils.parseEntityTag(RestAssured
            .given()
                .header("Accept", RDFFormat.RDFXML.getDefaultMIMEType())
            .expect()
                .contentType(RDFFormat.RDFXML.getDefaultMIMEType())
                .body(SesameMatchers.rdfStringMatches(RDFFormat.RDFXML, resource,
                        SesameMatchers.hasStatement(uri, RDF.TYPE, new URIImpl("http://example.com/Example")),
                        CoreMatchers.not(SesameMatchers.hasStatement(uri, RDFS.LABEL, null)),
                        CoreMatchers.not(SesameMatchers.hasStatement(uri, LDP.contains, uri))
                ))
            .get(resource)
                .getHeader("ETag"));
        log.debug("ETag for <{}>: {}", resource, etag);

        // Try a Put without if-match header
        RestAssured
            .given()
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
                .body(put_valid.getBytes())
            .expect()
                .statusCode(428)
            .put(resource);

        // Try a Put with wrong if-match header
        RestAssured
            .given()
                .header("If-Match", new EntityTag("invalid").toString())
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
                .body(put_valid.getBytes())
            .expect()
                .statusCode(412)
            .put(resource);

        // Try a Put
        RestAssured
            .given()
                .header("If-Match", etag.toString())
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
                .body(put_valid.getBytes())
            .expect()
                .statusCode(200)
            .put(resource);

        // Check the new data is there
        etag = EntityTagUtils.parseEntityTag(RestAssured
            .given()
                .header("Accept", RDFFormat.RDFXML.getDefaultMIMEType())
            .expect()
                .contentType(RDFFormat.RDFXML.getDefaultMIMEType())
                .body(SesameMatchers.rdfStringMatches(RDFFormat.RDFXML, resource,
                        SesameMatchers.hasStatement(uri, RDF.TYPE, new URIImpl("http://example.com/Example")),
                        SesameMatchers.hasStatement(uri, RDFS.LABEL, null),
                        CoreMatchers.not(SesameMatchers.hasStatement(uri, LDP.contains, uri))
                ))
            .get(resource)
                .header("ETag"));

        // Try an invalid PUT (server-controlled property)
        // Try a Put
        RestAssured
            .given()
                .header("If-Match", etag.toString())
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
                .body(put_invalid.getBytes())
            .expect()
                .statusCode(409)
            .put(resource);

        // Check the data is still there
        RestAssured
            .given()
                .header("Accept", RDFFormat.RDFXML.getDefaultMIMEType())
            .expect()
                .contentType(RDFFormat.RDFXML.getDefaultMIMEType())
                .header("ETag", HeaderMatchers.hasEntityTag(etag))
                .body(SesameMatchers.rdfStringMatches(RDFFormat.RDFXML, resource,
                        SesameMatchers.hasStatement(uri, RDF.TYPE, new URIImpl("http://example.com/Example")),
                        SesameMatchers.hasStatement(uri, RDFS.LABEL, null),
                        CoreMatchers.not(SesameMatchers.hasStatement(uri, LDP.contains, uri))
                ))
            .get(resource);
    }

    private String createTestContainer() {
        return createTestContainer("");
    }

    private String createTestContainer(String slug) {
        return RestAssured
            .given()
                .header("Slug", String.valueOf(slug))
                .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
                .body("<> a <http://example.com/unit-test> .".getBytes())
            .expect()
                .statusCode(HttpStatusSuccessMatcher.isSuccessful())
                .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
            .post(baseUrl + LdpWebService.PATH)
                .getHeader(HttpHeaders.LOCATION);
    }

    /**
     * Test for <a href="https://issues.apache.org/jira/browse/MARMOTTA-525">MARMOTTA-525</a>
     */
    @Test
    public void testMARMOTTA_525() {
        final String resourceName = "r1";

        // The container
        final String container = createTestContainer();
        final String mimeType = RDFFormat.TURTLE.getDefaultMIMEType();

        // Create
        final String newResource = RestAssured
            .given()
                .header("Slug", resourceName)
                .body(testResourceTTL.getBytes())
                .contentType(mimeType)
            .expect()
                .statusCode(201)
            .post(container)
                .header(HttpHeaders.LOCATION);

        // now the container hasType
        log.info("200 - container");
        RestAssured
            .expect()
                .statusCode(200)
                .header("Link", CoreMatchers.anyOf( //TODO: RestAssured only checks the FIRST header...
                                HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"),
                                HeaderMatchers.isLink(LDP.BasicContainer.stringValue(), "type"))
                )
                .header("ETag", HeaderMatchers.hasEntityTag(true)) // FIXME: be more specific here
                .contentType(mimeType)
                .body(SesameMatchers.rdfStringMatches(mimeType, container,
                        SesameMatchers.hasStatement(new URIImpl(container), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(container), RDF.TYPE, LDP.BasicContainer)
                ))
            .get(container);

        // also the new resource hasType
        RestAssured
            .expect()
                .statusCode(200)
                .header("Link", CoreMatchers.anyOf( //TODO: RestAssured only checks the FIRST header...
                                HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"),
                                HeaderMatchers.isLink(LDP.Resource.stringValue(), "type"))
                )
                .header("ETag", HeaderMatchers.hasEntityTag(true)) // FIXME: be more specific here
                .contentType(mimeType)
                .body(SesameMatchers.rdfStringMatches(mimeType, container,
                        SesameMatchers.hasStatement(new URIImpl(newResource), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(newResource), RDF.TYPE, LDP.Resource)
                ))
            .get(newResource);

        // delete
        RestAssured
            .expect()
                .statusCode(204)
                .header("Link", HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"))
                .header("ETag", HeaderMatchers.headerNotPresent())
                .header("Last-Modified", HeaderMatchers.headerNotPresent())
            .delete(newResource);

        // now the new resource does not exist.
        RestAssured
            .expect()
                .statusCode(410)
            .get(newResource);

    }

    @Test
    public void testSlugHeader() {
        final String slug1 = "niceName", slug2 = "with some späcial chars";

        final String container = createTestContainer("slugger");

        // This one is easy:
        RestAssured
            .given()
                .header("Slug", slug1)
                .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
                .body(testResourceTTL.getBytes())
            .expect()
                .statusCode(201)
                .header(HttpHeaders.LOCATION, CoreMatchers.endsWith(slug1))
            .post(container);

        // Trying again with the same SLUG
        RestAssured
            .given()
                .header("Slug", slug1)
                .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
                .body(testResourceTTL.getBytes())
            .expect()
                .statusCode(201)
                .header(HttpHeaders.LOCATION, new TypeSafeMatcher<String>() {
                    @Override
                    protected boolean matchesSafely(String item) {
                        return item.matches(String.format(".*/%s(-\\d+)$", Pattern.quote(slug1)));
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("an URL ending with something like ").appendValue(slug1);
                    }
                })
            .post(container);


        // This one does some magic on the slug
        RestAssured
            .given()
                .header("Slug", slug2)
                .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
                .body(testResourceTTL.getBytes())
            .expect()
                .statusCode(201)
                .header(HttpHeaders.LOCATION, CoreMatchers.endsWith(LdpUtils.urify(slug2)))
            .post(container);

    }
}
