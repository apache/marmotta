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
package org.apache.marmotta.ldclient.test;

import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.test.helper.TestLDClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class TestLDClientTest {

    private TestLDClient client;

    @Before
    public void setUp() {
        TestLDClient testLDClient = new TestLDClient(new LDClient());
        client = testLDClient;
    }

    @After
    public void cleanUp() {
        client.shutdown();
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

    @Test(expected = UnsupportedOperationException.class)
    public void testConnectionRefused() throws Exception {
        client.retrieveResource("http://no.host.for/this/url");
        Assert.fail();
    }

    @Test(expected = DataRetrievalException.class)
    public void testLocalhostInvalidPort() throws Exception {
        client.retrieveResource("http://127.1.2.3:66000/");
        Assert.fail();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMissingProvider() throws Exception {
        client.retrieveResource("ftp://no.provider.for/this/url");
        Assert.fail();
    }

}
