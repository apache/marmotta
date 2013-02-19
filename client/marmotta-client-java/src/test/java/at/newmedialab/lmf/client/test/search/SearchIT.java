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

package at.newmedialab.lmf.client.test.search;

import at.newmedialab.lmf.client.ClientConfiguration;
import at.newmedialab.lmf.client.clients.SearchClient;
import at.newmedialab.lmf.client.test.ldpath.LDPathIT;
import at.newmedialab.lmf.search.api.indexing.SolrIndexingService;
import kiwi.core.api.importer.ImportService;
import kiwi.core.exception.io.LMFImportException;
import kiwi.core.test.base.JettyLMF;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocumentList;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SearchIT {

    private final static Logger log = LoggerFactory.getLogger(SearchIT.class);



    private static JettyLMF lmf;

    private static ClientConfiguration config;

    // the tests require the demo-data.foaf to be loaded; we do so by first calling the import service before we start with tests
    private static ImportService importService;

    private static SolrIndexingService indexingService;

    @BeforeClass
    public static void init() throws LMFImportException, InterruptedException {
        lmf = new JettyLMF("/LMF",8080);

        config = new ClientConfiguration("http://localhost:8080/LMF");

        importService = lmf.getService(ImportService.class);

        indexingService = lmf.getService(SolrIndexingService.class);

        // load initial data
        InputStream data =  LDPathIT.class.getResourceAsStream("/demo-data.foaf");

        importService.importData(data,"application/rdf+xml",null,null);

        // wait for thread to finish
        do {
            log.info("waiting for server to finish indexing ...");
            Thread.sleep(1000);
        } while(indexingService.isRunning());
    }

    @AfterClass
    public static void tearDown() {
        lmf.shutdown();
    }


    // assumes the demo-data.foaf is loaded on the server
    @Test
    public void searchTest() throws Exception {
        SearchClient client = new SearchClient(config);

        SolrQuery query = new SolrQuery("summary:Sepp");
        SolrDocumentList result = client.search("dc",query);

        Assert.assertTrue(result.size() > 0);
    }
}
