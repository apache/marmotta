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
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.ldp.webservices.LdpWebService;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;

import javax.ws.rs.core.UriBuilder;

/**
 * LDP Test Cases
 *
 * @author Sergio Fernández
 *
 * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/Test%20Cases/LDP%20Test%20Cases.html">LDP Test Cases</a>
 */
@RunWith(LdpTestCasesRunner.class)
public class LdpTestCases {

    public final static String ROOT_PATH = "/testsuite/";

    public final static String BASE = "http://www.w3.org/TR/ldp-test-cases/";

    public final static String MANIFEST_CACHE = "LDP-Test-Cases-WD-live";

    @ClassRule
    public static ExternalResource marmotta = new MarmottaResource();

    public static class MarmottaResource extends ExternalResource {

        JettyMarmotta marmotta;

        String baseUrl;

        @Override
        protected void before() throws Throwable {
            marmotta = new JettyMarmotta("/marmotta", LdpWebService.class);
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = marmotta.getPort();
            RestAssured.basePath = marmotta.getContext();
            baseUrl = UriBuilder.fromUri("http://localhost").port(marmotta.getPort()).path(marmotta.getContext()).build().toString();
        }

        @Override
        protected void after() {
            //marmotta.shutdown();
            marmotta = null;
        }

    }

}
