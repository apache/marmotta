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
package org.apache.marmotta.ldclient.test.freebase;

import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some tests over random data to Freebase to warranty that the provider
 * fixes some common deficiencies in the original data.
 *
 * @author Sergio Fernández
 */
public class TestFreebaseProvider extends ProviderTestBase {

    private static final String ASF = "http://rdf.freebase.com/ns/m.0nzm";
    private static final String MARMOTTA = "http://rdf.freebase.com/ns/m.0wqhskn";
    private static final String SERGIO = "http://rdf.freebase.com/ns/m.07zqbwz";
    private static final String WAS = "http://rdf.freebase.com/ns/m.0h21k1c";

    /**
     * Tests accessing ASF's page from Freebase.
     *
     * @throws Exception
     *
     */
    @Test
    @Ignore
    public void testASF() throws Exception {
        testResource(ASF, "m.0nzm.sparql");
    }

    /**
     * Tests accessing Marmotta's page from Freebase.
     *
     * @throws Exception
     *
     */
    @Test
    public void testMarmotta() throws Exception {
        testResource(MARMOTTA, "m.0wqhskn.sparql");
    }

    /**
     * Tests accessing Sergio's profile from Freebase.
     *
     * @throws Exception
     *
     */
    @Test
    public void testSergio() throws Exception {
        testResource(SERGIO, "m.07zqbwz.sparql");
    }

    /**
     * Tests accessing WAS's page from Freebase.
     *
     * @throws Exception
     *
     */
    @Test
    public void testWAS() throws Exception {
        testResource(WAS, "m.0h21k1c.sparql");
    }

    @Test
    public void testAditional() throws Exception {
        testResource("http://rdf.freebase.com/ns/m.0b1t1");
        testResource("http://rdf.freebase.com/ns/m.04jpl");
        testResource("http://rdf.freebase.com/ns/m.036wy");
        testResource("http://rdf.freebase.com/ns/m.01d0fp");
    }

}
