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
package org.apache.marmotta.platform.core.test.modules;

import com.jayway.restassured.RestAssured;
import org.apache.marmotta.platform.core.api.modules.MarmottaResourceService;
import org.apache.marmotta.platform.core.api.modules.ResourceEntry;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.core.webservices.prefix.PrefixWebService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Marmotta Resources test
 *
 * @author Sergio Fernández
 */
public class MarmottaResourceServiceTest {

    private static JettyMarmotta marmotta;
    private static MarmottaResourceService resourceService;

    @BeforeClass
    public static void setUp() {
        marmotta = new JettyMarmotta("/marmotta", PrefixWebService.class);
        resourceService = marmotta.getService(MarmottaResourceService.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();
    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testMARMOTTA499() {

        final String resource = "/core/public/style/blue/style.css";
        final String expectedMimeType = "text/css";

        ResourceEntry resourceEntry = resourceService.getResource(resource);
        Assert.assertEquals(expectedMimeType, resourceEntry.getContentType());

        given().
        expect().
            statusCode(200).
            contentType(expectedMimeType).
        when().
            get(resource);

    }

}
