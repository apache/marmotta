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
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.apache.marmotta.platform.ldp.webservices.LdpWebService;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.ldp.testsuite.LdpTestSuite;
import org.w3.ldp.testsuite.matcher.HttpStatusSuccessMatcher;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
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
public class LdpSuiteTest {

    /** @see org.testng.TestNG#HAS_FAILURE */
    private static final int TESTNG_STATUS_HAS_FAILURE = 1;
    /** @see org.testng.TestNG#HAS_SKIPPED */
    private static final int TESTNG_STATUS_HAS_SKIPPED = 2;
    /** @see org.testng.TestNG#HAS_NO_TEST */
    private static final int TESTNG_STATUS_HAS_NO_TEST = 8;

    private static Logger log = LoggerFactory.getLogger(LdpSuiteTest.class);

    private static JettyMarmotta marmotta;

    private static String baseUrl;

    private LdpTestSuite testSuite;

    private String reportPath;

    @BeforeClass
    public static void setup() throws URISyntaxException, IOException {
        marmotta = new JettyMarmotta("/marmotta", LdpWebService.class);
        baseUrl = UriBuilder.fromUri("http://localhost").port(marmotta.getPort()).path(marmotta.getContext()).path(LdpWebService.PATH).build().toString();
    }

    @AfterClass
    public static void shutdown() {
        marmotta.shutdown();
        marmotta = null;
    }

    @Before
    public void before() throws RepositoryException, IOException, RDFParseException {
        log.debug("Performing required LDP re-initialization...");
        RestAssured
            .expect()
                .statusCode(HttpStatusSuccessMatcher.isSuccessful())
                .statusLine(CoreMatchers.startsWith("HTTP/1.1"))
            .get(baseUrl);

        final String container = RestAssured
            .given()
                .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
                .header(HttpHeaders.LINK, Link.fromUri(LdpService.InteractionModel.LDPC.stringValue()).rel(LdpWebService.LINK_REL_TYPE).build().toString())
                .body("<> a <http://example.com/ContainerInteraction> . ".getBytes())
            .expect()
                .statusCode(HttpStatusSuccessMatcher.isSuccessful())
                .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
            .post(baseUrl)
                .getHeader(HttpHeaders.LOCATION);

        final String resource = RestAssured
            .given()
                .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
                .header(HttpHeaders.LINK, Link.fromUri(LdpService.InteractionModel.LDPR.stringValue()).rel(LdpWebService.LINK_REL_TYPE).build().toString())
                .body("<> a <http://example.com/ResourceInteraction> .".getBytes())
            .expect()
                .statusCode(HttpStatusSuccessMatcher.isSuccessful())
                .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
            .post(baseUrl)
                .getHeader(HttpHeaders.LOCATION);

        RestAssured.reset();

        log.info("Container: {}", container);
        log.info("Resource: {}", resource);

        //configure test suite
        log.info("Running W3C official LDP Test Suite against '{}' server", baseUrl);
        Map<String, String> options = new HashMap<>();
        options.put("server", container);
        options.put("basic", null);
        options.put("non-rdf", null);
        options.put("cont-res", resource);
        if (!LdpService.SERVER_MANAGED_PROPERTIES.isEmpty()) {
            options.put("read-only-prop", LdpService.SERVER_MANAGED_PROPERTIES.iterator().next().stringValue());
        }
        options.put("httpLogging", null);
        options.put("skipLogging", null);
        options.put("excludedGroups", "MANUAL");

        reportPath = targetWorkingDir().getAbsolutePath();
        options.put("output", reportPath);
        log.debug("You can find LDP Test Suite outputs at {}", reportPath);

        //w3c reporting stuff
        /*
        options.put("earl", null);
        options.put("software", "Apache Marmotta");
        options.put("language", "Java");
        options.put("homepage", "http://marmotta.apache.org");
        options.put("assertor", "http://marmotta.apache.org");
        options.put("shortname", "Marmotta");
        options.put("developer", "Jakob Frank, Sergio Fernández");
        */

        testSuite = new LdpTestSuite(options);
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
        if ((testSuite.getStatus() & TESTNG_STATUS_HAS_SKIPPED) != 0) {
            log.warn("ldp-testsuite has skipped some tests");
        }
    }

    private File targetWorkingDir(){
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath, "ldp-testsuite");
        if(!targetDir.exists()) {
            Assume.assumeTrue("Could not create report-directory", targetDir.mkdir());
        }
        return targetDir;
    }

}
