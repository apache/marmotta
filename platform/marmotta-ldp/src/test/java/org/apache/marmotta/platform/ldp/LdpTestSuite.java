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

import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.ldp.webservices.LdpWebService;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * LDP Test Suite runner, see <a href="https://github.com/w3c/ldp-testsuite">https://github.com/w3c/ldp-testsuite</a>.
 *
 * @author Sergio Fernández
 * @author Jakob Frank
 */
public class LdpTestSuite {

    /** @see org.testng.TestNG#HAS_FAILURE */
    private static final int TESTNG_STATUS_HAS_FAILURE = 1;
    /** @see org.testng.TestNG#HAS_NO_TEST */
    private static final int TESTNG_STATUS_HAS_NO_TEST = 8;

    private static Logger log = LoggerFactory.getLogger(LdpTestSuite.class);

    private static JettyMarmotta marmotta;

    private static String baseUrl;

    private org.w3.ldp.testsuite.LdpTestSuite testSuite;

    @BeforeClass
    public static void setup() throws URISyntaxException, IOException {
        marmotta = new JettyMarmotta("/marmotta", LdpWebService.class);
        baseUrl = UriBuilder.fromUri("http://localhost").port(marmotta.getPort()).path(marmotta.getContext()).build().toString();
    }

    @AfterClass
    public static void shutdown() {
        marmotta.shutdown();
        marmotta = null;
    }

    @Before
    public void before() {
        log.info("Running W3C official LDP Test Suite against '{}' server", baseUrl);
        System.out.println("Running ldp-testsuite against " + baseUrl);
        Map<String, String> options = new HashMap<>();
        options.put("server", baseUrl + "/ldp");
        options.put("basic", null);
        options.put("non-rdf", null);
        testSuite = new org.w3.ldp.testsuite.LdpTestSuite(options);
    }

    @After
    public void after() {
        testSuite = null;
    }

    @Test
    public void testRunSuite() {
        testSuite.run();
        Assert.assertTrue("ldp-testsuite finished with errors", (testSuite.getStatus() & TESTNG_STATUS_HAS_FAILURE) == 0);
        Assert.assertTrue("ldp-testsuite is empty - no test run", (testSuite.getStatus() & TESTNG_STATUS_HAS_NO_TEST) == 0);
    }

}
