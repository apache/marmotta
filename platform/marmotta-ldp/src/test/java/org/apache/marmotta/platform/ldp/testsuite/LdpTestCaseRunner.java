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

package org.apache.marmotta.platform.ldp.testsuite;

import com.jayway.restassured.RestAssured;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.ldp.webservices.LdpWebService;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;

/**
 * LDP Test Case JUnit Runner
 *
 * @author Sergio Fernández
 */
public class LdpTestCaseRunner extends Runner {

    private static Logger log = LoggerFactory.getLogger(LdpTestCaseRunner.class);

    private final LdpTestCase testCase;

    private String baseUrl;

    public LdpTestCaseRunner(LdpTestCase testCase) {
        this.testCase = testCase;
    }

    @Override
    public Description getDescription() {
        return Description.createSuiteDescription(testCase.getUri().getLocalName());
    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.fireTestRunStarted(getDescription());
        try {
            run();
        } catch (Exception e) {
            //TODO
            //notifier.fireTestFailure(e);
        }
        notifier.fireTestFinished(getDescription());
    }

    /**
     * Actual test case run method, which build the test case execution
     * on the fly based on
     */
    private void run() {
        Assume.assumeNotNull(baseUrl);
        assertNotNull(testCase);
        assertNotNull(testCase.getUri());
        String context = buildContext(testCase);
        log.warn("Executing LDP Test Case {} over context {}...", testCase.getUri().getLocalName(), context);

        //basic pre-checking
        //RestAssured.expect().statusCode(200).get(baseUrl); //TODO: clarify this (root container?)
        RestAssured.expect().statusCode(404).get(context);

        //load all information of the test cases
        readTestCase(testCase);

        //actual test case execution

    }

    private void readTestCase(LdpTestCase testCase) {
        //TODO: read all details
    }

    private String buildContext(LdpTestCase testCase) {
        return baseUrl + "/" + testCase.getUri().getLocalName().toLowerCase();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

}
