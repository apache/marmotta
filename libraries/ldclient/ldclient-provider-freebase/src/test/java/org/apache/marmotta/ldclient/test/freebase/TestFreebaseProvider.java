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

import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;

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

    @Override
    protected void testResource(String uri) throws Exception {
        try {
            super.testResource(uri);
        } catch (final Exception e) {
            // Unfortunately, freebase often serves corrupt/invalid/unparsable data, e.g. non-escaped quotes in literals
            Assume.assumeFalse("Freebase provided invalid RDF data for <" + uri + ">", checkCauseStack(e, DataRetrievalException.class, IOException.class, DataRetrievalException.class, RDFParseException.class));
            throw e;
        }
    }

    @Override
    protected void testResource(String uri, String sparqlFile) throws Exception {
        try {
            super.testResource(uri, sparqlFile);
        } catch (final Exception e) {
            // Unfortunately, freebase often serves corrupt/invalid/unparsable data, e.g. non-escaped quotes in literals
            Assume.assumeFalse("Freebase provided invalid RDF data for <" + uri + ">", checkCauseStack(e, DataRetrievalException.class, IOException.class, DataRetrievalException.class, RDFParseException.class));
            throw e;
        }
    }

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
    public void test_m_0b1t1() throws Exception {
        testResource("http://rdf.freebase.com/ns/m.0b1t1");
    }

    @Test
    public void test_m_04jpl() throws Exception {
        testResource("http://rdf.freebase.com/ns/m.04jpl");
    }

    @Test
    public void test_m_036wy() throws Exception {
        testResource("http://rdf.freebase.com/ns/m.036wy");
    }

    @Test
    public void test_m_01d0fp() throws Exception {
        testResource("http://rdf.freebase.com/ns/m.01d0fp");
    }


    @SafeVarargs
    protected static boolean checkCauseStack(Throwable t, Class<? extends Throwable>... stack) {
        return checkCauseStack(t, 0, stack);
    }

    @SafeVarargs
    private static boolean checkCauseStack(Throwable t, int i, Class<? extends Throwable>... stack) {
        return i >= stack.length || stack[i].isInstance(t) && checkCauseStack(t.getCause(), i + 1, stack);
    }



}
