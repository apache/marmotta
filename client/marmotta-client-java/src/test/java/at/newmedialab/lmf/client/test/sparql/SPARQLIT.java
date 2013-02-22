/*
 * Copyright 2012 Salzburg Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.newmedialab.lmf.client.test.sparql;

import at.newmedialab.lmf.client.ClientConfiguration;
import at.newmedialab.lmf.client.clients.SPARQLClient;
import at.newmedialab.lmf.client.model.sparql.SPARQLResult;
import at.newmedialab.lmf.client.test.ldpath.LDPathIT;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyLMF;
import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static org.hamcrest.Matchers.*;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SPARQLIT {
    private final static Logger log = LoggerFactory.getLogger(SPARQLIT.class);

    private static JettyLMF lmf;

    private static ClientConfiguration config;

    // the tests require the demo-data.foaf to be loaded; we do so by first calling the import service before we start with tests
    private static ImportService importService;


    @BeforeClass
    public static void init() throws MarmottaImportException {
        lmf = new JettyLMF("/LMF",8080);
        config = new ClientConfiguration("http://localhost:8080/LMF");

        importService = lmf.getService(ImportService.class);

        // load initial data
        InputStream data =  LDPathIT.class.getResourceAsStream("/demo-data.foaf");

        importService.importData(data,"application/rdf+xml",null,null);
    }

    @AfterClass
    public static void tearDown() {
        lmf.shutdown();
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

}
