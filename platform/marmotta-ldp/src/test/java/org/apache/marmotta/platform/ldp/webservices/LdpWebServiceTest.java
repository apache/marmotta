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
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Tests for testing the LDP web services
 * 
 * @author Sergio Fernández
 *
 */
public class LdpWebServiceTest {

	private static Logger log = LoggerFactory.getLogger(LdpWebServiceTest.class);
	
    private static JettyMarmotta marmotta;

    private static String testResourceTTL;

    @BeforeClass
    public static void setUp() throws MarmottaImportException, URISyntaxException, IOException {
        marmotta = new JettyMarmotta("/marmotta", LdpWebService.class);
        
        //TODO: initialization
        testResourceTTL = IOUtils.toString(LdpWebServiceTest.class.getResourceAsStream("/test.ttl"), "utf8");
        
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();

    }

    @Test
    public void testCRUD() {
        // The container
        final String container = "/ldp/test/container1";
        final String newResourceUri = container + "/resource1";

        RestAssured.expect().statusCode(404).get(container);

        // Create
        RestAssured
            .given()
                .header("Slug", "resource1")
                .body(testResourceTTL.getBytes())
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
            .expect()
                .statusCode(201)
                .header("Location", RestAssured.baseURI + newResourceUri)
                .post(container);

        // now the container exists
        log.info("200 - container");
        RestAssured.given()
                .header("Accept", RDFFormat.TURTLE.getDefaultMIMEType())
                .expect()
                .statusCode(200)
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
                .get(container);

        // also the new resource exists
        RestAssured.given()
                .header("Accept", RDFFormat.TURTLE.getDefaultMIMEType())
                .expect()
                .statusCode(200)
                .contentType(RDFFormat.TURTLE.getDefaultMIMEType())
                .get(newResourceUri);

        // delete
        RestAssured.expect()
                .statusCode(204)
                .delete(newResourceUri);

        // now the new resource does not exist.
        RestAssured.given()
                .header("Accept", RDFFormat.TURTLE.getDefaultMIMEType())
                .expect()
                .statusCode(404)
                .get(newResourceUri);

    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

}
