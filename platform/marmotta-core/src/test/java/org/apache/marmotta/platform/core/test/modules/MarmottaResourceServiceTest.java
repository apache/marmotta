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
import org.junit.*;

import static com.jayway.restassured.RestAssured.given;

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
        testResource("/core/public/style/blue/style.css", "text/css");
    }

    @Test
    public void testPage() {
        testResource("/core/admin/about.html", "text/html");
    }

    @Test
    public void testImage() {
        testResource("/core/public/img/logo/marmotta-logo.png", "image/png");
    }

    private void testResource(String resource, String mimetype) {
        Assume.assumeNotNull(resource);
        Assume.assumeNotNull(mimetype);

        ResourceEntry resourceEntry = resourceService.getResource(resource);
        Assert.assertNotNull(resourceEntry);
        Assert.assertEquals(mimetype, resourceEntry.getContentType());

        given().
        expect().
            statusCode(200).
            contentType(mimetype).
        when().
            get(resource);
    }

}
