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
package org.apache.marmotta.platform.core.test.config;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

import java.io.IOException;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.core.webservices.config.ConfigurationWebService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jayway.restassured.RestAssured;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ConfigurationWebServiceTest {

    private static JettyMarmotta marmotta;
    private static ConfigurationService configurationService;

    private static ObjectMapper mapper = new ObjectMapper();


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

    @Test
    public void testSetConfiguration() throws IOException {
        // set my.key to the values "value1" and "value2"
        given().
        header("Content-Type","application/json").
        content(mapper.writeValueAsString(Lists.newArrayList("value1", "value2"))).
        expect().
        statusCode(200).
        when().
        post("/config/data/mykey");


        // test whether configuration service has the key
        Assert.assertThat(configurationService.getListConfiguration("mykey"), hasItem("value1"));

        // test whether values appear when retrieving the key
        expect().
        statusCode(200).
        body("mykey",hasItems("value1","value2")).
        when().
        get("/config/data/mykey");

        // test whether values appear in full list
        expect().
        statusCode(200).
        body("mykey.value",hasItems("value1","value2")).
        when().
        get("/config/list");


        // test whether deleting returns OK
        expect().
        statusCode(200).
        when().
        delete("/config/data/mykey");


        // test whether values appear when retrieving the key
        expect().
        statusCode(404).
        when().
        get("/config/data/mykey");
    }

    /*@Test
    TODO fix bug in congigurationService (MARMOTTA-147)
    public void testSetConfigurationTypesAndComments() throws IOException {
        given().
        header("Content-Type","application/json").
        queryParam("type","java.lang.Boolean").
        queryParam("comment","This is a comment").
        content(mapper.writeValueAsString(Lists.newArrayList(true))).
        expect().
        statusCode(200).
        when().
        post("/config/data/mykey2");

        given().
        queryParam("prefix","mykey2").
        expect().
        statusCode(200).
        body("mykey2.value", hasValue(true)).
        body("mykey2.type", hasValue("java.lang.Boolean")).
        body("mykey2.description", hasValue("This is a comment")).
        when().
        get("/config/list");
    }*/

}
