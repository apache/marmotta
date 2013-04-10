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
package org.apache.marmotta.ldclient.test.vimeo;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.test.helper.TestLDClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class TestVimeoProvider {

    private LDClientService ldclient;

    private static Logger log = LoggerFactory.getLogger(TestVimeoProvider.class);

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

    /**
     * This method tests accessing the Youtube Video service via the GData API.
     *
     * @throws Exception
     */
    @Test
    public void testVideo() throws Exception {

        String uriLMFVideo = "http://vimeo.com/7223527";
        ClientResponse respLMFVideo = ldclient.retrieveResource(uriLMFVideo);

        RepositoryConnection conLMFVideo = respLMFVideo.getTriples().getConnection();
        conLMFVideo.begin();
        Assert.assertTrue(conLMFVideo.size() > 0);

        conLMFVideo.export(Rio.createWriter(RDFFormat.TURTLE, System.out));


        // run a SPARQL test to see if the returned data is correct
        InputStream sparql = this.getClass().getResourceAsStream("vimeo-video.sparql");
        BooleanQuery testLabel = conLMFVideo.prepareBooleanQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        Assert.assertTrue("SPARQL test query failed", testLabel.evaluate());

        if(log.isDebugEnabled()) {
            StringWriter out = new StringWriter();
            conLMFVideo.export(Rio.createWriter(RDFFormat.TURTLE, out));
            log.debug("DATA:");
            log.debug(out.toString());
        }

        conLMFVideo.commit();
        conLMFVideo.close();
    }

    /**
     * This method tests accessing the Vimeo Channel service via the Vimeo API.
     *
     * @throws Exception
     */
    @Test
    public void testChannel() throws Exception {

        String uriChannel = "http://vimeo.com/channels/ninlive09";
        ClientResponse respChannel = ldclient.retrieveResource(uriChannel);

        RepositoryConnection conChannel = respChannel.getTriples().getConnection();
        conChannel.begin();
        Assert.assertTrue(conChannel.size() > 0);

        conChannel.export(Rio.createWriter(RDFFormat.TURTLE, System.out));


        // run a SPARQL test to see if the returned data is correct
        InputStream sparql = this.getClass().getResourceAsStream("vimeo-channel.sparql");
        BooleanQuery testLabel = conChannel.prepareBooleanQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        Assert.assertTrue("SPARQL test query failed", testLabel.evaluate());

        if(log.isDebugEnabled()) {
            StringWriter out = new StringWriter();
            conChannel.export(Rio.createWriter(RDFFormat.TURTLE, out));
            log.debug("DATA:");
            log.debug(out.toString());
        }

        conChannel.commit();
        conChannel.close();
    }


}
