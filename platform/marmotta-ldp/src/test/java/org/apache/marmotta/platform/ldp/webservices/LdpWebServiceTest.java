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

package org.apache.marmotta.platform.ldp.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.RestAssured;

/**
 * Tests for testing the LDP web services
 * 
 * @author Sergio Fernández
 *
 */
public class LdpWebServiceTest {

	private static Logger log = LoggerFactory.getLogger(LdpWebServiceTest.class);
	
    private static JettyMarmotta marmotta;

    @BeforeClass
    public static void setUp() throws MarmottaImportException, URISyntaxException {
        marmotta = new JettyMarmotta("/marmotta", LdpWebService.class);
        
        //TODO: initialization
        
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();

    }

    @Test
    public void test() {
	// Nothing is implemented so far...
        Assert.assertEquals(501, RestAssured.get("/ldp").statusCode());
        Assert.assertEquals(501, RestAssured.get("/ldp/foo/bar/and/some/more").statusCode());
    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

}
