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

package org.apache.marmotta.platform.sparql.webservices;

import com.jayway.restassured.RestAssured;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static com.jayway.restassured.RestAssured.expect;

/**
 * Tests for testing the SPAQL endpoint
 *
 * @author Sergio Fernández
 */
public class SparqlWebServiceTest {

    private static Logger log = LoggerFactory.getLogger(SparqlWebServiceTest.class);

    private static JettyMarmotta marmotta;

    @BeforeClass
    public static void setUp() throws MarmottaImportException, URISyntaxException {
        marmotta = new JettyMarmotta("/marmotta", SparqlWebService.class);

        ImportService importService = marmotta.getService(ImportService.class);
        UserService userService = marmotta.getService(UserService.class);
        ContextService contextService = marmotta.getService(ContextService.class);
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("foaf.rdf");
        int n = importService.importData(is, "application/rdf+xml", userService.getAnonymousUser(), contextService.getDefaultContext());
        log.info("Imported RDF test suite with {} triples", n);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();

    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testSelect() throws IOException, InterruptedException {
        expect().
                log().ifError().
                statusCode(200).
            given().
                param("query", "SELECT ?o WHERE { <http://www.wikier.org/foaf#wikier> ?p ?o }").
                when().
            get("/sparql/select");
    }

    @Test
    public void testSelectContentNegotiation() throws IOException, InterruptedException {
        expect().
                log().ifError().
                statusCode(200).
                contentType("application/sparql-results+xml").
            given().
                header("Accept", "application/xml").
                param("query", "SELECT ?o WHERE { <http://www.wikier.org/foaf#wikier> ?p ?o }").
            when().
                get("/sparql/select");
    }

    @Test
    public void testAsk() throws IOException, InterruptedException {
        expect().
                log().ifError().
                statusCode(200).
            given().
                param("query", "ASK WHERE { <http://www.wikier.org/foaf#wikier> ?p ?o }").
            when().
                get("/sparql/select");
    }

    @Test
    public void testConstruct() throws IOException, InterruptedException {
        expect().
                log().ifError().
                statusCode(200).
                contentType("application/rdf+xml").
            given().
                param("query", "CONSTRUCT { <http://www.wikier.org/foaf#wikier> ?p ?o } WHERE { <http://www.wikier.org/foaf#wikier> ?p ?o }").
            when().
                get("/sparql/select");
    }

    @Test
    public void testConstructContentNegotiationXml() throws IOException, InterruptedException {
        expect().
                log().ifError().
                statusCode(200).
                contentType("application/rdf+xml").
            given().
                header("Accept", "application/xml").
                param("query", "CONSTRUCT { <http://www.wikier.org/foaf#wikier> ?p ?o } WHERE { <http://www.wikier.org/foaf#wikier> ?p ?o }").
            when().
                get("/sparql/select");
    }

    @Test
    public void testConstructContentNegotiationPlain() throws IOException, InterruptedException {
        expect().
                log().ifError().
                statusCode(415).
            given().
                header("Accept", "text/plain").
                param("query", "CONSTRUCT { <http://www.wikier.org/foaf#wikier2> ?p ?o } WHERE { <http://www.wikier.org/foaf#wikier> ?p ?o }").
            when().
                get("/sparql/select");
    }

    @Test
    public void testConstructContentNegotiationTurtle() throws IOException, InterruptedException {
        expect().
                log().ifError().
                statusCode(200).
                contentType("text/turtle").
            given().
                header("Accept", "text/turtle").
                param("query", "CONSTRUCT { <http://www.wikier.org/foaf#wikier2> ?p ?o } WHERE { <http://www.wikier.org/foaf#wikier> ?p ?o }").
            when().
                get("/sparql/select");
    }

    @Test
    public void testDescribe() throws IOException, InterruptedException {
        expect().
                log().ifError().
                statusCode(200).
                contentType("application/rdf+xml").
            given().
                param("query", "DESCRIBE <http://www.wikier.org/foaf#wikier>").
            when().
                get("/sparql/select");
    }

    @Test
    public void testDescribeContentNegotiationXml() throws IOException, InterruptedException {
        expect().
                log().ifError().
                statusCode(200).
                contentType("application/rdf+xml").
            given().
                header("Accept", "application/xml").
                param("query", "DESCRIBE <http://www.wikier.org/foaf#wikier>").
            when().
                get("/sparql/select");
    }

    @Test
    public void testDescribeContentNegotiationPlain() throws IOException, InterruptedException {
        expect().
                log().ifError().
                statusCode(415).
            given().
                header("Accept", "plain/text").
                param("query", "DESCRIBE <http://www.wikier.org/foaf#wikier>").
            when().
                get("/sparql/select");
    }

}
