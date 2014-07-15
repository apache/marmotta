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

package org.apache.marmotta.platform.ldp;

import com.jayway.restassured.RestAssured;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.ldp.webservices.LdpWebService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * LDP Test Suite runner
 *
 * @author Sergio Fernández
 */
public class LdpTestSuite {

    private static Logger log = LoggerFactory.getLogger(LdpTestSuite.class);

    private static JettyMarmotta marmotta;

    private static String baseUrl;

    @BeforeClass
    public static void setup() throws URISyntaxException, IOException {
        marmotta = new JettyMarmotta("/marmotta", LdpWebService.class);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();
        baseUrl = UriBuilder.fromUri("http://localhost").port(marmotta.getPort()).path(marmotta.getContext()).build().toString();
    }

    @AfterClass
    public static void shutdown() {
        marmotta.shutdown();
        marmotta = null;
    }

    @Test
    public void testSuite() {
        Map<String, String> options = new HashMap<>();
        options.put("server", baseUrl);
        options.put("basic", null);
        options.put("non-rdf", null);
        //org.w3.ldp.testsuite.LdpTestSuite testSuite = new org.w3.ldp.testsuite.LdpTestSuite(options);
        //testSuite.run();
        //Assert.assertEquals(0, testSuite.getStatus());
    }

}
