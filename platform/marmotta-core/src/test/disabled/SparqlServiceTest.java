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
package org.apache.marmotta.platform.core.test.sparql;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import com.google.common.io.Resources;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;
import org.apache.marmotta.platform.core.test.base.LMFBaseTest;
import org.junit.Rule;
import org.junit.Test;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests the SPARQL Web Service, querying and updating.
 * 
 * @author Sebastian Schaffert
 */
public class SparqlServiceTest extends LMFBaseTest {

    @Rule
    public ConcurrentRule concurrently = new ConcurrentRule();

    @Rule
    public RepeatingRule repeatedly = new RepeatingRule();


    private final static int THREADS = 5;
    private final static int REPETITIONS = 10;


    /**
     * Test running a SPARQL ASK { ...} query
     * @throws IOException
     */
    @Test
    @Concurrent(count = THREADS)
    @Repeating(repetition = REPETITIONS)
    public void testSparqlAsk() throws IOException {

        RestAssured.registerParser("application/sparql-results+xml", Parser.XML);
        RestAssured.registerParser("application/sparql-results+json", Parser.JSON);


        // generate some random N3 data
        String s1 = generateRandomUri();
        String s2 = generateRandomUri();
        String s3 = generateRandomUri();
        String v1 = generateRandomContent(20);
        String v2 = generateRandomContent(15);
        String v3 = generateRandomContent(25);


        String n3data =
                Resources.toString(this.getClass().getResource("data.n3"), Charset.defaultCharset()).
                        replaceAll("\\$1", Matcher.quoteReplacement(s1)).
                        replaceAll("\\$2", Matcher.quoteReplacement(s2)).
                        replaceAll("\\$3", Matcher.quoteReplacement(s3)).
                        replaceAll("\\$v1", Matcher.quoteReplacement(v1)).
                        replaceAll("\\$v2", Matcher.quoteReplacement(v2)).
                        replaceAll("\\$v3", Matcher.quoteReplacement(v3));


        // import data using import webservice; result must be 200 ok
        given().
                header("Content-Type", "text/rdf+n3").
                body(n3data).
        expect().
                statusCode(200).
        when().
                post("/import/upload");


        // carry out SPARQL ASK query for s1 foaf:knows s2; result must be SPARQL/XML and true
        String askQuery =
                "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"+
                "ASK { <$1> foaf:knows <$2> }".
                replaceAll("\\$1", s1).
                replaceAll("\\$2", s2);

        given().
                header("Accept", "application/xml").
                header("Content-Type","text/plain").
                body(askQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("application/sparql-results+xml")).
                body("sparql.boolean", equalTo("true")).
        when().
                post("/sparql/select");


        // carry out SPARQL ASK query for s1 foaf:knows s2; result must be SPARQL/JSON and true
        given().
                header("Accept", "application/json").
                header("Content-Type", "text/plain").
                body(askQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("application/sparql-results+json")).
                body("boolean", equalTo(true)).
        when().
                post("/sparql/select");


        // carry out SPARQL ASK query for s1 foaf:knows s2; result must be SPARQL/JSON and true
        given().
                header("Accept", "text/html").
                header("Content-Type", "text/plain").
                body(askQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("text/html")).
                body("html.head.title", equalTo("SPARQL Query Results")).
        when().
                post("/sparql/select");



        for(String uri : new String[] {s1,s2,s3}) {
            // delete the resource
            expect().
                    statusCode(200).
            when().
                    delete("/resource/"+uri.substring(baseResource.length()));
        }

    }


    /**
     * Test running a SPARQL SELECT ... WHERE { ... } query
     * @throws IOException
     */
    @Test
    @Concurrent(count = THREADS)
    @Repeating(repetition = REPETITIONS)
    public void testSparqlSelect() throws IOException {

        RestAssured.registerParser("application/sparql-results+xml", Parser.XML);
        RestAssured.registerParser("application/sparql-results+json", Parser.JSON);

        // register the SPARQL namespace for XPath queries
        NamespaceContext ns = new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if("".equals(prefix)) {
                    return "http://www.w3.org/2005/sparql-results#";
                } else if(XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                    return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                } else if(XMLConstants.XML_NS_PREFIX.equals(prefix)) {
                    return XMLConstants.XML_NS_URI;
                } else {
                    return XMLConstants.NULL_NS_URI;
                }
            }

            @Override
            public String getPrefix(String namespaceURI) {
               if("http://www.w3.org/2005/sparql-results#".equals(namespaceURI)) {
                   return XMLConstants.DEFAULT_NS_PREFIX;
               } else {
                   return null;
               }
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                if("http://www.w3.org/2005/sparql-results#".equals(namespaceURI)) {
                    return Collections.singleton(XMLConstants.DEFAULT_NS_PREFIX).iterator();
                } else {
                    return null;
                }
            }
        };

        // generate some random N3 data
        String s1 = generateRandomUri();
        String s2 = generateRandomUri();
        String s3 = generateRandomUri();
        String v1 = generateRandomContent(20);
        String v2 = generateRandomContent(15);
        String v3 = generateRandomContent(25);


        String n3data =
                Resources.toString(this.getClass().getResource("data.n3"), Charset.defaultCharset()).
                        replaceAll("\\$1", Matcher.quoteReplacement(s1)).
                        replaceAll("\\$2", Matcher.quoteReplacement(s2)).
                        replaceAll("\\$3", Matcher.quoteReplacement(s3)).
                        replaceAll("\\$v1", Matcher.quoteReplacement(v1)).
                        replaceAll("\\$v2", Matcher.quoteReplacement(v2)).
                        replaceAll("\\$v3", Matcher.quoteReplacement(v3));


        // import data using import webservice; result must be 200 ok
        given().
                header("Content-Type", "text/rdf+n3").
                body(n3data).
                expect().
                statusCode(200).
                when().
                post("/import/upload");


        // carry out SPARQL SELECT query, selecting all persons and their names foaf:known by s1; must be s2 and s3
        String selectQuery =
                ("PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"+
                        "SELECT ?p ?n " +
                        "WHERE { <$1> foaf:knows ?p . " +
                        "        ?p foaf:name ?n      }").
                                replaceAll("\\$1", s1);

        given().
                header("Accept", "application/xml").
                header("Content-Type", "text/plain").
                body(selectQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("application/sparql-results+xml")).
                body(hasXPath("/sparql/results/result/binding[@name='p']/uri", ns, isOneOf(s2, s3))).
                body(hasXPath("/sparql/results/result/binding[@name='n']/literal", ns, isOneOf(v2, v3))).
        when().
                post("/sparql/select");


        // carry out SPARQL ASK query for s1 foaf:knows s2; result must be SPARQL/JSON and true
        given().
                header("Accept", "application/json").
                header("Content-Type", "text/plain").
                body(selectQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("application/sparql-results+json")).
                body("results.bindings.p.value", hasItems(s2, s3)).
                body("results.bindings.n.value", hasItems(v2, v3)).
        when().
                post("/sparql/select");


        // carry out SPARQL ASK query for s1 foaf:knows s2; result must be SPARQL/JSON and true
        given().
                header("Accept", "text/html").
                header("Content-Type", "text/plain").
                body(selectQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("text/html")).
                body("html.head.title", equalTo("SPARQL Query Results")).
        when().
                post("/sparql/select");


        for(String uri : new String[] {s1,s2,s3}) {
            // delete the resource
            expect().
                    statusCode(200).
            when().
                    delete("/resource/"+uri.substring(baseResource.length()));
        }
    }


    /**
     * Test running SPARQL INSERT DATA { ... } statements
     * @throws IOException
     */
    @Test
    @Concurrent(count = THREADS)
    @Repeating(repetition = REPETITIONS)
    public void testSparqlInsertData() throws IOException {
        RestAssured.registerParser("application/sparql-results+xml", Parser.XML);
        RestAssured.registerParser("application/sparql-results+json", Parser.JSON);

        // generate some random N3 data
        String s1 = generateRandomUri();
        String s2 = generateRandomUri();
        String s3 = generateRandomUri();
        String v1 = generateRandomContent(20);
        String v2 = generateRandomContent(15);
        String v3 = generateRandomContent(25);


        String n3data =
                Resources.toString(this.getClass().getResource("data.n3"),Charset.defaultCharset()).
                        replaceAll("\\$1", Matcher.quoteReplacement(s1)).
                        replaceAll("\\$2", Matcher.quoteReplacement(s2)).
                        replaceAll("\\$3", Matcher.quoteReplacement(s3)).
                        replaceAll("\\$v1", Matcher.quoteReplacement(v1)).
                        replaceAll("\\$v2", Matcher.quoteReplacement(v2)).
                        replaceAll("\\$v3", Matcher.quoteReplacement(v3));


        // import base data using import webservice; result must be 200 ok
        given().
                header("Content-Type", "text/rdf+n3").
                body(n3data).
        expect().
                statusCode(200).
        when().
                post("/import/upload");


        // check that not(s2 foaf:knows s3)
        String askQuery =
                "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"+
                "ASK { <$2> foaf:knows <$3> }".
                replaceAll("\\$3", s3).
                replaceAll("\\$2", s2);
        given().
                header("Accept", "application/xml").
                header("Content-Type","text/plain").
                body(askQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("application/sparql-results+xml")).
                body("sparql.boolean", equalTo("false")).
        when().
                post("/sparql/select");


        // load SPARQL INSERT from file insert.sparql
        String sparqlInsert = Resources.toString(this.getClass().getResource("insert.sparql"), Charset.defaultCharset()).
                replaceAll("\\$1",s1).
                replaceAll("\\$2", s2).
                replaceAll("\\$3", s3);

        // execute insert by posting it to the /sparql/update webservice, expect 200
        given().
                header("Content-Type", "text/plain").
                body(sparqlInsert).
        expect().
                statusCode(200).
        when().
                post("/sparql/update");


        // now expect the ASK query to yield "true"
        given().
                header("Accept", "application/xml").
                header("Content-Type", "text/plain").
                body(askQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("application/sparql-results+xml")).
                body("sparql.boolean", equalTo("true")).
        when().
                post("/sparql/select");

        for(String uri : new String[] {s1,s2,s3}) {
            // delete the resource
            expect().
                    statusCode(200).
            when().
                    delete("/resource/"+uri.substring(baseResource.length()));
        }
    }

    /**
     * Test running SPARQL INSERT { ... } WHERE { ... } statements
     * @throws IOException
     */
    @Test
    // no concurrency, since the tests themselves and update programs are not thread safe
    @Repeating(repetition = REPETITIONS)
    public void testSparqlInsertWhere() throws IOException {
        RestAssured.registerParser("application/sparql-results+xml", Parser.XML);
        RestAssured.registerParser("application/sparql-results+json", Parser.JSON);

        // generate some random N3 data
        String s1 = generateRandomUri();
        String s2 = generateRandomUri();
        String s3 = generateRandomUri();
        String v1 = generateRandomContent(20);
        String v2 = generateRandomContent(15);
        String v3 = generateRandomContent(25);


        String n3data =
                Resources.toString(this.getClass().getResource("data.n3"),Charset.defaultCharset()).
                        replaceAll("\\$1", Matcher.quoteReplacement(s1)).
                        replaceAll("\\$2", Matcher.quoteReplacement(s2)).
                        replaceAll("\\$3", Matcher.quoteReplacement(s3)).
                        replaceAll("\\$v1", Matcher.quoteReplacement(v1)).
                        replaceAll("\\$v2", Matcher.quoteReplacement(v2)).
                        replaceAll("\\$v3", Matcher.quoteReplacement(v3));


        // import base data using import webservice; result must be 200 ok
        given().
                header("Content-Type", "text/rdf+n3").
                body(n3data).
        expect().
                statusCode(200).
        when().
                post("/import/upload");


        // check that not(s2 foaf:knows s3)
        String askQuery =
                "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"+
                "ASK { <$2> foaf:knows <$3> }".
                replaceAll("\\$3", s3).
                replaceAll("\\$2", s2);
        given().
                header("Accept", "application/xml").
                header("Content-Type","text/plain").
                body(askQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("application/sparql-results+xml")).
                body("sparql.boolean", equalTo("false")).
        when().
                post("/sparql/select");


        // load SPARQL INSERT from file insert.sparql
        String sparqlInsert = Resources.toString(this.getClass().getResource("update.sparql"), Charset.defaultCharset());

        // execute insert by posting it to the /sparql/update webservice, expect 200
        given().
                header("Content-Type", "text/plain").
                body(sparqlInsert).
        expect().
                statusCode(200).
        when().
                post("/sparql/update");


        // now expect the ASK query to yield "true"
        given().
                header("Accept", "application/xml").
                header("Content-Type","text/plain").
                body(askQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("application/sparql-results+xml")).
                body("sparql.boolean", equalTo("true")).
        when().
                post("/sparql/select");

        for(String uri : new String[] {s1,s2,s3}) {
            // delete the resource
            expect().
                    statusCode(200).
            when().
                    delete("/resource/"+uri.substring(baseResource.length()));
        }
    }


    /**
     * Test running SPARQL DELETE updates
     * @throws IOException
     */
    @Test
    // no concurrency, since the tests themselves and update programs are not thread safe
    @Repeating(repetition = REPETITIONS)
    public void testSparqlDelete() throws IOException {
        RestAssured.registerParser("application/sparql-results+xml", Parser.XML);
        RestAssured.registerParser("application/sparql-results+json", Parser.JSON);

        // generate some random N3 data
        String s1 = generateRandomUri();
        String s2 = generateRandomUri();
        String s3 = generateRandomUri();
        String v1 = generateRandomContent(20);
        String v2 = generateRandomContent(15);
        String v3 = generateRandomContent(25);


        String n3data =
                Resources.toString(this.getClass().getResource("data.n3"), Charset.defaultCharset()).
                        replaceAll("\\$1", Matcher.quoteReplacement(s1)).
                        replaceAll("\\$2", Matcher.quoteReplacement(s2)).
                        replaceAll("\\$3", Matcher.quoteReplacement(s3)).
                        replaceAll("\\$v1", Matcher.quoteReplacement(v1)).
                        replaceAll("\\$v2", Matcher.quoteReplacement(v2)).
                        replaceAll("\\$v3", Matcher.quoteReplacement(v3));


        // import base data using import webservice; result must be 200 ok
        given().
                header("Content-Type", "text/rdf+n3").
                body(n3data).
        expect().
                statusCode(200).
        when().
                post("/import/upload");


        // check that s1 foaf:knows s2
        String askQuery =
                "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"+
                "ASK { <$1> foaf:knows <$2> }".
                replaceAll("\\$1", s1).
                replaceAll("\\$2", s2);
        given().
                header("Accept", "application/xml").
                header("Content-Type","text/plain").
                body(askQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("application/sparql-results+xml")).
                body("sparql.boolean", equalTo("true")).
        when().
                post("/sparql/select");


        // load SPARQL DELETE from file delete.sparql
        String sparqlInsert = Resources.toString(this.getClass().getResource("delete.sparql"), Charset.defaultCharset());

        // execute insert by posting it to the /sparql/update webservice, expect 200
        given().
                header("Content-Type","text/plain").
                body(sparqlInsert).
        expect().
                statusCode(200).
        when().
                post("/sparql/update");


        // now expect the ASK query to yield "false"
        given().
                header("Accept", "application/xml").
                header("Content-Type","text/plain").
                body(askQuery).
        expect().
                statusCode(200).
                header("Content-Type", containsString("application/sparql-results+xml")).
                body("sparql.boolean", equalTo("false")).
        when().
                post("/sparql/select");

        for(String uri : new String[] {s1,s2,s3}) {
            // delete the resource
            expect().
                    statusCode(200).
            when().
                    delete("/resource/"+uri.substring(baseResource.length()));
        }
    }

}
