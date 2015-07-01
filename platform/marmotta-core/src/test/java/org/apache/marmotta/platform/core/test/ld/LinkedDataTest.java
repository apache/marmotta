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

package org.apache.marmotta.platform.core.test.ld;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.core.webservices.resource.ContentWebService;
import org.apache.marmotta.platform.core.webservices.resource.MetaWebService;
import org.apache.marmotta.platform.core.webservices.resource.ResourceWebService;
import org.junit.*;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;

/**
 * This test verifies the functionality of the Linked Data endpoint
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LinkedDataTest {

    private static JettyMarmotta marmotta;
    private static SesameService sesameService;
    private static ContextService contextService;

    private static ObjectMapper mapper = new ObjectMapper();

    private static Random rnd = new Random();


    @BeforeClass
    public static void setUp() throws RepositoryException, IOException, RDFParseException {
        marmotta = new JettyMarmotta("/marmotta", ResourceWebService.class, MetaWebService.class, ContentWebService.class);
        sesameService = marmotta.getService(SesameService.class);
        contextService = marmotta.getService(ContextService.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();

        // import some test data

        // 1. read into string
        String data = IOUtils.toString(LinkedDataTest.class.getResourceAsStream("/org/apache/marmotta/platform/core/test/sesame/demo-data.foaf"));
        StringReader in = new StringReader(data.replaceAll("http://localhost:8080/LMF", RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath));


        RepositoryConnection connection = sesameService.getConnection();
        try {
            connection.add(in,  RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath, RDFFormat.RDFXML);

            URI sepp = createResourceURI("sepp_huber");

            Assert.assertTrue(connection.hasStatement(sepp,null,null,true));
        } finally {
            connection.close();
        }

    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    /**
     * Test if we can retrieve Linked Data resources as RDF/XML.
     *
     * @throws Exception
     */
    @Test
    public void testGetRDFXML() throws Exception {
        testGetBase(RDFFormat.RDFXML);
    }

    /**
     * Test if we can retrieve Linked Data resources as Turtle
     *
     * @throws Exception
     */
    @Test
    public void testGetTurtle() throws Exception {
        testGetBase(RDFFormat.TURTLE);
    }

    /**
     * Test if we can retrieve Linked Data resources as Turtle
     *
     * @throws Exception
     */
    @Ignore("JSON-LD Parser currently does not parse the result properly")
    @Test
    public void testGetJSONLD() throws Exception {
        testGetBase(RDFFormat.JSONLD);
    }


    private void testGetBase(RDFFormat format) throws Exception {
        String data = given().header("Accept",format.getDefaultMIMEType()).expect().statusCode(200).when().get(createResourceURI("sepp_huber").stringValue()).asString();

        Repository mem = new SailRepository(new MemoryStore());
        mem.initialize();

        RepositoryConnection con = mem.getConnection();
        RepositoryConnection icon = sesameService.getConnection();
        try {
            con.begin();

            con.add(new StringReader(data), RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath, format);

            Assert.assertTrue(con.hasStatement(createResourceURI("sepp_huber"),null,null,true));

            RepositoryResult<Statement> statements = icon.getStatements(createResourceURI("sepp_huber"), null,null,true);
            while(statements.hasNext()) {
                Statement stmt = statements.next();
                Assert.assertTrue("statement "+stmt+" not found in results",con.hasStatement(stmt,true));
            }

            con.commit();
            icon.commit();
        } finally {
            con.close();
            icon.close();
        }


        String invalid = given().header("Accept",format.getDefaultMIMEType()).expect().statusCode(404).when().get(createResourceURI("xyz").stringValue()).asString();
    }


    /**
     * Test if we can write Linked Data resources as RDF/XML.
     *
     * @throws Exception
     */
    @Test
    public void testPostRDFXML() throws Exception {
        testPostPutDeleteBase(RDFFormat.RDFXML);
    }

    /**
     * Test if we can write Linked Data resources as Turtle
     *
     * @throws Exception
     */
    @Test
    public void testPostTurtle() throws Exception {
        testPostPutDeleteBase(RDFFormat.TURTLE);
    }

    /**
     * Test if we can write Linked Data resources as JSON-LD
     *
     * @throws Exception
     */
    @Ignore("JSON-LD Parser currently does not parse the result properly")
    @Test
    public void testPostJSONLD() throws Exception {
        testPostPutDeleteBase(RDFFormat.JSONLD);
    }


    private void testPostPutDeleteBase(RDFFormat format) throws Exception {
        URI resource1 = randomResource();

        // create resource 1 with empty body and expect that we can afterwards retrieve it (even if empty triples)
        expect().statusCode(201).when().post(resource1.stringValue());
        // TODO: at the moment we will return 404 because we have no way to check for existance of a resource without triples
        given().header("Accept",format.getDefaultMIMEType()).expect().statusCode(404).when().get(resource1.stringValue());


        // create resource 2 with some triples that we generate randomly in a temporary repository
        Repository mem = new SailRepository(new MemoryStore());
        mem.initialize();

        RepositoryConnection con = mem.getConnection();
        RepositoryConnection mcon = sesameService.getConnection();
        try {
            for(int i=0; i < rnd.nextInt(20); i++ ) {
                con.add(resource1, randomResource(), randomResource(), contextService.getDefaultContext());
            }


            // send the post to the resource with the triples serialized in the given format
            StringWriter data = new StringWriter();
            con.export(Rio.createWriter(format,data));
            String body = data.toString();

            given()
                    .log().ifValidationFails()
                    .header("Content-Type", format.getDefaultMIMEType())
                    .body(body.getBytes())
            .expect()
                    .statusCode(200)
            .when()
                    .put(resource1.stringValue());


            // now check in the Marmotta triple store if all triples are there
            RepositoryResult<Statement> statements = con.getStatements(resource1,null,null,true);
            while(statements.hasNext()) {
                Statement stmt = statements.next();
                Assert.assertTrue("statement "+stmt+" not found in triple store",mcon.hasStatement(stmt,true));
            }


            con.commit();
            mcon.commit();
        } finally {
            con.close();
            mcon.close();
        }

        given().header("Accept",format.getDefaultMIMEType()).expect().statusCode(200).when().get(resource1.stringValue());


        // test if we can delete the resource and then it is no longer there
        expect().statusCode(200).when().delete(resource1.stringValue());
        given().header("Accept",format.getDefaultMIMEType()).expect().statusCode(404).when().get(resource1.stringValue());

        RepositoryConnection mcon2 = sesameService.getConnection();
        try {
            Assert.assertFalse("resource was not properly deleted", mcon2.hasStatement(resource1,null,null,true));
        } finally {
            mcon2.close();
        }
    }


    private static URI createResourceURI(String id) {
        return sesameService.getRepository().getValueFactory().createURI(RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath + "/resource/" + id);
    }

    private static URI createURI(String id) {
        return sesameService.getRepository().getValueFactory().createURI(id);
    }

    private static URI randomResource() {
        return sesameService.getRepository().getValueFactory().createURI(RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath + "/resource/" + RandomStringUtils.randomAlphanumeric(8));
    }

}
