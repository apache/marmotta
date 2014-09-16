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

import com.hp.hpl.jena.xmloutput.impl.Basic;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.apache.marmotta.platform.ldp.webservices.LdpWebService;
import org.junit.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.ldp.testsuite.LdpTestSuite;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * LDP Test Suite runner, see <a href="https://github.com/w3c/ldp-testsuite">https://github.com/w3c/ldp-testsuite</a>.
 *
 * @author Sergio Fernández
 * @author Jakob Frank
 */
public class LdpSuiteTest {

    /** @see org.testng.TestNG#HAS_FAILURE */
    private static final int TESTNG_STATUS_HAS_FAILURE = 1;
    /** @see org.testng.TestNG#HAS_NO_TEST */
    private static final int TESTNG_STATUS_HAS_NO_TEST = 8;

    private static Logger log = LoggerFactory.getLogger(LdpSuiteTest.class);

    private static JettyMarmotta marmotta;

    private static String baseUrl;

    private LdpTestSuite testSuite;

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
        final SesameService sesameService = marmotta.getService(SesameService.class);
        final  LdpService ldpService = marmotta.getService(LdpService.class);
        final RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();

            //warm up and initialization
            HttpClient httpClient = HttpClientBuilder.create().build();
            final HttpResponse response = httpClient.execute(new HttpGet(baseUrl));
            Assume.assumeTrue(response.getStatusLine().getStatusCode() == 200);
            final String container = ldpService.addResource(conn, baseUrl,
                    UriBuilder.fromUri(baseUrl).path(UUID.randomUUID().toString()).build().toString(),
                    LdpService.InteractionModel.LDPC, RDFFormat.TURTLE.getDefaultMIMEType(),
                    IOUtils.toInputStream("<> a <http://example.com/ldp/ContainerInteraction> . "));
            final String resource = ldpService.addResource(conn, baseUrl,
                    UriBuilder.fromUri(baseUrl).path(UUID.randomUUID().toString()).build().toString(),
                    LdpService.InteractionModel.LDPR, RDFFormat.TURTLE.getDefaultMIMEType(),
                    IOUtils.toInputStream("<> a <http://example.com/ldp/ResourceInteraction> . "));
            conn.commit();

            //then actual test suite

            log.info("Running W3C official LDP Test Suite against '{}' server", baseUrl);
            log.debug("(using {} as root container)", container);
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
            testSuite = new LdpTestSuite(options);
        } finally {
            conn.close();
        }
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
