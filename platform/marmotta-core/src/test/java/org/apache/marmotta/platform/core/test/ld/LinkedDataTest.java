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

import com.google.common.collect.Sets;
import com.jayway.restassured.RestAssured;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.core.webservices.config.ConfigurationWebService;
import org.apache.marmotta.platform.core.webservices.resource.ContentWebService;
import org.apache.marmotta.platform.core.webservices.resource.MetaWebService;
import org.apache.marmotta.platform.core.webservices.resource.ResourceWebService;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;
import java.io.StringReader;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.form;
import static com.jayway.restassured.RestAssured.given;

/**
 * This test verifies the functionality of the Linked Data endpoint
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LinkedDataTest {

    private static JettyMarmotta marmotta;
    private static SesameService sesameService;

    private static ObjectMapper mapper = new ObjectMapper();


    @BeforeClass
    public static void setUp() throws RepositoryException, IOException, RDFParseException {
        marmotta = new JettyMarmotta("/marmotta", ResourceWebService.class, MetaWebService.class, ContentWebService.class);
        sesameService = marmotta.getService(SesameService.class);

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
     * Test if we can retrieve Linked Data resources as RDF/XML.
     *
     * @throws Exception
     */
    @Test
    public void testGetTurtle() throws Exception {
        testGetBase(RDFFormat.TURTLE);
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
                Assert.assertTrue(con.hasStatement(statements.next(),true));
            }

            con.commit();
            icon.commit();
        } finally {
            con.close();
            icon.close();
        }


        String invalid = given().header("Accept",format.getDefaultMIMEType()).expect().statusCode(404).when().get(createResourceURI("xyz").stringValue()).asString();
        System.err.println(invalid);
    }


    private static URI createResourceURI(String id) {
        return sesameService.getRepository().getValueFactory().createURI(RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath + "/resource/" + id);
    }

    private static URI createURI(String id) {
        return sesameService.getRepository().getValueFactory().createURI(id);
    }

}
