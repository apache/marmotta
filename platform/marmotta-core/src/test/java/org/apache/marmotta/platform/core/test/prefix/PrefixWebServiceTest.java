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
package org.apache.marmotta.platform.core.test.prefix;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;

import org.apache.marmotta.platform.core.api.prefix.PrefixService;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.core.webservices.prefix.PrefixWebService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

/**
 * Test the functionality of the prefix web service
 * 
 * @author Sergio Fernández
 */
public class PrefixWebServiceTest {

    private static JettyMarmotta marmotta;
    private static PrefixService prefixService;

    @BeforeClass
    public static void setUp() {
        marmotta = new JettyMarmotta("/marmotta", PrefixWebService.class);
        prefixService = marmotta.getService(PrefixService.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();

    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testSet() throws IOException, InterruptedException {
    	
        String prefix = "foo";
        String namespace = "http://foo#";
        
        Assert.assertNull(prefixService.getNamespace(prefix));
        
        expect().
    		statusCode(404).
    	when().
    		get("/prefix/" + prefix);
        
        given().
        expect().
        	statusCode(201).
        when().
        	post("/prefix/" + prefix + "?uri=" + namespace);

        Assert.assertNotNull(prefixService.getNamespace(prefix));
        Assert.assertEquals(namespace, prefixService.getNamespace(prefix));
        Assert.assertEquals(prefix, prefixService.getPrefix(namespace));

        expect().
        	statusCode(200).
        	body(prefix, equalTo(namespace)).
        when().
        	get("/prefix/" + prefix);
        
        expect().
    		statusCode(200).
    		//body(namespace, equalTo(prefix)).
    	when().
    		get("/prefix/reverse?uri=" + namespace);

    }

}
