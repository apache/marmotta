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

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
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
 * Test if the FreebaseProvider is working properly.
 *
 * @author Sergio Fernández
 */
public class TestFreebaseProvider extends ProviderTestBase {

    private static Logger log = LoggerFactory.getLogger(TestFreebaseProvider.class);

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
    @Ignore
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
    @Ignore
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

}
