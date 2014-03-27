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
import org.apache.marmotta.platform.ldp.webservices.util.HeaderMatchers;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URISyntaxException;

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
        //marmotta.shutdown();
        marmotta = null;
        testResourceTTL = null;
    }

    @Test
    public void testCRUD() {
        final String testBase = "test";
        final String containerName = "container1";
        final String resourceName = "resource1";

        // The container
        final String container = UriBuilder.fromPath(LdpWebService.PATH).path(testBase).path(containerName).build().toString();
        final String newResource = UriBuilder.fromUri(container).path(resourceName).build().toString();
        final String mimeType = RDFFormat.TURTLE.getDefaultMIMEType();

        RestAssured.expect().statusCode(404).get(container);

        // Create
        RestAssured
            .given()
                .header("Slug", resourceName)
                .body(testResourceTTL.getBytes())
                .contentType(mimeType)
            .expect()
                .statusCode(201)
                .header("Location", baseUrl + newResource)
            .post(container);

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
                .body(SesameMatchers.rdfStringMatches(mimeType, baseUrl+container,
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + container), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + container), RDF.TYPE, LDP.BasicContainer)
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
                .body(SesameMatchers.rdfStringMatches(mimeType, baseUrl + container,
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + newResource), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + newResource), RDF.TYPE, LDP.Resource)
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
            .given()
                .header("Accept", mimeType)
            .expect()
                .statusCode(404)
            .get(newResource);

    }

    @Test
    public void testNR() throws IOException {
        final String testBase = "test";
        final String containerName = "container2";
        final String resourceName = "resource1";

        // The container
        final String container = UriBuilder.fromPath(LdpWebService.PATH).path(testBase).path(containerName).build().toString();
        final String newResource = UriBuilder.fromUri(container).path(resourceName).build().toString();
        final String mimeType = "image/png";

        RestAssured.expect().statusCode(404).get(container);

        // Create
        RestAssured
            .given()
                .header("Slug", resourceName)
                .body(IOUtils.toByteArray(LdpWebServiceTest.class.getResourceAsStream("/test.png")))
                .contentType(mimeType)
            .expect()
                .statusCode(201)
                .header("Location", baseUrl + newResource + ".png")
                .header("Link", CoreMatchers.anyOf( //TODO: RestAssured only checks the FIRST header...
                        HeaderMatchers.isLink(baseUrl + newResource, "describedby"),
                        HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"),
                        HeaderMatchers.isLink(LDP.BasicContainer.stringValue(), "type"))
                )
            .post(container);

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
                .body(SesameMatchers.rdfStringMatches(RDFFormat.TURTLE.getDefaultMIMEType(), baseUrl + container,
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + container), RDF.TYPE, LDP.Resource),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + container), RDF.TYPE, LDP.RDFSource),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + container), RDF.TYPE, LDP.Container),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + container), RDF.TYPE, LDP.BasicContainer),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + container), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + container), LDP.contains, new URIImpl(baseUrl + newResource + ".png")))
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
                .body(SesameMatchers.rdfStringMatches(RDFFormat.TURTLE.getDefaultMIMEType(), baseUrl + newResource,
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + newResource), RDF.TYPE, LDP.Resource),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + newResource), RDF.TYPE, LDP.RDFSource),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + newResource), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + newResource), DCTERMS.HAS_FORMAT, new URIImpl(baseUrl + newResource + ".png"))
                ))
            .get(newResource);

        // now the resource hasType
        RestAssured
                .given()
                .header("Accept", RDFFormat.TURTLE.getDefaultMIMEType())
                .expect()
                .statusCode(200)
                .header("Link", CoreMatchers.anyOf( //TODO: RestAssured only checks the FIRST header...
                        HeaderMatchers.isLink(LdpWebService.LDP_SERVER_CONSTRAINTS, "describedby"),
                        HeaderMatchers.isLink(LDP.Resource.stringValue(), "type"),
                        HeaderMatchers.isLink(LDP.NonRdfResource.stringValue(), "type"))
                )
                .header("ETag", HeaderMatchers.hasEntityTag(false)) // FIXME: be more specific here
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
                .body(SesameMatchers.rdfStringMatches(RDFFormat.TURTLE.getDefaultMIMEType(), baseUrl + newResource+".png",
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + newResource+".png"), RDF.TYPE, LDP.Resource),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + newResource+".png"), RDF.TYPE, LDP.NonRdfResource),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + newResource+".png"), DCTERMS.MODIFIED, null),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + newResource+".png"), DCTERMS.FORMAT, new LiteralImpl(mimeType)),
                        SesameMatchers.hasStatement(new URIImpl(baseUrl + newResource+".png"), DCTERMS.IS_FORMAT_OF, new URIImpl(baseUrl + newResource))
                ))
                .get(newResource + ".png");

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
            .get(newResource + ".png")
                .body().asByteArray();

        assertEquals("md5sum",expectedMD5, HashUtils.md5sum(data));
    }

    @Test
    public void testInteractionModel() throws Exception {
        final String container = baseUrl+LdpWebService.PATH + "/iam";

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

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

}
