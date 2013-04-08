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

package org.apache.marmotta.platform.core.test.cors;

import com.google.common.collect.Lists;
import com.jayway.restassured.RestAssured;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.core.webservices.config.ConfigurationWebService;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.jayway.restassured.RestAssured.given;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class CorsFilterTest {

    private static JettyMarmotta marmotta;
    private static ConfigurationService configurationService;

    @BeforeClass
    public static void setUp() {
        marmotta = new JettyMarmotta("/marmotta", ConfigurationWebService.class);
        configurationService = marmotta.getService(ConfigurationService.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();

    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    /**
     * Sparql Service should use the basic cors functionality
     */
    @Test
    public void testCorsRequestOnSparqlService() throws UnsupportedEncodingException {

        //options request
        given().header("Origin", "http://otherhost.com").
        expect().statusCode(200).
        expect().header("Access-Control-Allow-Origin", "*").
        expect().header("Access-Control-Allow-Methods","POST, PUT, GET, DELETE, HEAD").
        when().options("/config/list");

        //change configuration and retry
        configurationService.setConfiguration("Access-Control-Allow-Origin","http://otherhost.com");

        given().header("Origin", "http://otherhost.com").
        expect().statusCode(200).
        expect().header("Access-Control-Allow-Origin", "http://otherhost.com").
        expect().header("Access-Control-Allow-Methods","POST, PUT, GET, DELETE, HEAD").
        when().options("/config/list");

        //get request
        given().header("Origin", "http://otherhost.com").
        given().header("Content-Type","application/json").
        given().content("[\"test\"]").
        expect().statusCode(200).
        expect().header("Access-Control-Allow-Origin", "http://otherhost.com").
        expect().header("Access-Control-Allow-Methods","POST, PUT, GET, DELETE, HEAD").
        when().post("/config/data/key");

    }

    /**
     * resource Service should overwrite the basic cors functionality
     */
    @Test
    public void testCorsRequestOnResourceService() {
        //change configuration
        configurationService.setConfiguration("Access-Control-Allow-Origin","http://my.host.com");

        //options request
        given().header("Origin", "http://otherhost.com").
        expect().statusCode(201).
        expect().header("Access-Control-Allow-Origin", "*").
        expect().header("Access-Control-Allow-Methods","POST, PUT, GET, DELETE, HEAD").
        when().post("/resource/123");
    }

}
