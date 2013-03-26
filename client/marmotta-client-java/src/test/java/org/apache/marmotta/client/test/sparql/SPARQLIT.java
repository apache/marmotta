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
package org.apache.marmotta.client.test.sparql;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasValue;

import java.io.InputStream;

import org.apache.marmotta.client.ClientConfiguration;
import org.apache.marmotta.client.clients.SPARQLClient;
import org.apache.marmotta.client.model.sparql.SPARQLResult;
import org.apache.marmotta.client.test.AbstractClientIT;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPARQL Client Integration tests
 * 
 * @author Sebastian Schaffert
 */
public class SPARQLIT extends AbstractClientIT {
	
    private final static Logger log = LoggerFactory.getLogger(SPARQLIT.class);

    private static JettyMarmotta marmotta;

    private static ClientConfiguration config;

    @BeforeClass
    public static void init() throws MarmottaImportException {
        marmotta = new JettyMarmotta("/marmotta");
        config = new ClientConfiguration("http://localhost:" + marmotta.getPort() + marmotta.getContext());

        ImportService importService = marmotta.getService(ImportService.class);
        InputStream data = getTestData("demo-data.foaf"); // load initial data
        importService.importData(data, "application/rdf+xml", null, null);
    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testSparqlSelect() throws Exception {
        SPARQLClient client = new SPARQLClient(config);

        SPARQLResult result = client.select("SELECT ?r ?n WHERE { ?r <http://xmlns.com/foaf/0.1/name> ?n }");
        Assert.assertEquals(3, result.size());
        Assert.assertThat(result, (Matcher) hasItems(hasKey("r"), hasKey("n")));
        Assert.assertThat(result,(Matcher)hasItem(hasValue(hasProperty("content", equalTo("Sepp Huber")))));
    }

    @Test
    public void testSparqlAsk() throws Exception {
        SPARQLClient client = new SPARQLClient(config);

        boolean result = client.ask("ASK { ?r <http://xmlns.com/foaf/0.1/name> ?n }");
        Assert.assertTrue(result);

    }
    
    @Test
    public void testSparqlUpdate() throws Exception {
        SPARQLClient client = new SPARQLClient(config);

        try {
        	client.update(
        			"INSERT DATA { \n" +
        			"    <http://www.dajobe.org/foaf.rdf#i> <http://purl.org/dc/elements/1.1/date> \"1999-04-01T00:00:00\" . \n" +
        			"    <http://www.w3.org/People/Berners-Lee/card#i> <http://purl.org/dc/elements/1.1/date> \"1998-05-03T00:00:00\" .  \n" +
        			"    <http://www.w3.org/People/Connolly/#me> <http://purl.org/dc/elements/1.1/date> \"2001-02-08T00:00:00\"  \n" +
        			"}");
        } catch (Exception e) {
        	Assert.fail("Update query failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testSparqlUpdateGraph() throws Exception {
        SPARQLClient client = new SPARQLClient(config);

        try {
        	client.update(
        			"INSERT DATA { \n" +
        			"  GRAPH <http://BookStore.com> {  \n" +
        			"    <http://www.dajobe.org/foaf.rdf#i> <http://purl.org/dc/elements/1.1/date> \"1999-04-01T00:00:00\" . \n" +
        			"    <http://www.w3.org/People/Berners-Lee/card#i> <http://purl.org/dc/elements/1.1/date> \"1998-05-03T00:00:00\" .  \n" +
        			"    <http://www.w3.org/People/Connolly/#me> <http://purl.org/dc/elements/1.1/date> \"2001-02-08T00:00:00\"  \n" +
        			"  } \n" +
        			"}");
        } catch (Exception e) {
        	Assert.fail("Update query failed: " + e.getMessage());
        }
    }

}
