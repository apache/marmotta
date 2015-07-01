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
package org.apache.marmotta.platform.core.test.jaxrs;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ResponseBody;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.core.webservices.resource.ResourceWebService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.expect;

/**
 * Test for exceptions handling
 *
 * @author Sergio Fernández
 */
public class ExceptionHandlingTest {

    private static JettyMarmotta marmotta;

    @BeforeClass
    public static void setUp() {
        marmotta = new JettyMarmotta("/marmotta", ResourceWebService.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();
    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testNotFound() {

        final ResponseBody response = expect().
            statusCode(404).
            contentType("text/html").
        given().
            header("Accept", "text/html").
        when().
            get(ConfigurationService.RESOURCE_PATH + "/foo").
            getBody();
        //response.print();

        final ResponseBody responseJson = expect().
            statusCode(404).
            contentType("application/json").
        given().
            header("Accept", "application/json").
        when().
            get(ConfigurationService.RESOURCE_PATH + "/foo").
            getBody();
        responseJson.print();
        Assert.assertEquals(404, responseJson.jsonPath().get("status"));
        Assert.assertEquals("Not Found", responseJson.jsonPath().get("reason"));

    }

}
