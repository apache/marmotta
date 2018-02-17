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

package org.apache.marmotta.ldclient.test.provider;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.test.helper.TestLDClient;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ProviderTestBase {

    protected LDClientService ldclient;

    private static Logger log = LoggerFactory.getLogger(ProviderTestBase.class);

    @Before
    public void setupClient() {
        ldclient = new TestLDClient(new LDClient());
    }

    @After
    public void shutdownClient() {
        ldclient.shutdown();
    }

    final Logger logger =
            LoggerFactory.getLogger(this.getClass());

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        /**
         * Invoked when a test is about to start
         */
        @Override
        protected void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

    protected void testResource(String uri) throws Exception {

        Assume.assumeTrue("LDClient endpoint for <" + uri + "> not available", ldclient.ping(uri));

        ClientResponse response = ldclient.retrieveResource(uri);

        RepositoryConnection connection = ModelCommons.asRepository(response.getData()).getConnection();
        try {
            connection.begin();
            Assert.assertTrue(connection.size() > 0);
        }finally {
            connection.commit();
            connection.close();
        }
    }

    protected void testResource(String uri, String sparqlFile) throws Exception {

        Assume.assumeTrue("LDClient endpoint for <" + uri + "> not available", ldclient.ping(uri));

        ClientResponse response = ldclient.retrieveResource(uri);

        final RepositoryConnection connection = ModelCommons.asRepository(response.getData()).getConnection();
        try {
            connection.begin();
            Assert.assertTrue(connection.size() > 0);

            // run a SPARQL test to see if the returned data is correct
            final InputStream sparql = this.getClass().getResourceAsStream(sparqlFile);
            final String query = IOUtils.toString(sparql, "utf8");
            final BooleanQuery testLabel = connection.prepareBooleanQuery(QueryLanguage.SPARQL, query);
            final boolean testSuccess = testLabel.evaluate();

            if (!testSuccess && log.isDebugEnabled()) {
                log.debug("QUERY:\n{}", query);

                final StringWriter out = new StringWriter();
                connection.export(Rio.createWriter(RDFFormat.TURTLE, out));
                log.debug("DATA:\n{}", out.toString());
            }

            Assert.assertTrue("SPARQL test query failed", testSuccess);
        } finally {
            connection.commit();
            connection.close();
        }
    }

}
