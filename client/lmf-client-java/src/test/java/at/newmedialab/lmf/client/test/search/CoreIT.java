/*
 * Copyright (c) 2013 Salzburg Research.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.newmedialab.lmf.client.test.search;

import at.newmedialab.lmf.client.ClientConfiguration;
import at.newmedialab.lmf.client.clients.CoresClient;
import at.newmedialab.lmf.search.api.indexing.SolrIndexingService;
import kiwi.core.test.base.JettyLMF;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class CoreIT {

    private final static Logger log = LoggerFactory.getLogger(CoreIT.class);

    private static JettyLMF lmf;

    private static ClientConfiguration config;

    private static SolrIndexingService indexingService;

    @BeforeClass
    public static void init() {
        lmf = new JettyLMF("/LMF",8080);
        config = new ClientConfiguration("http://localhost:8080/LMF");
        indexingService = lmf.getService(SolrIndexingService.class);

    }

    @AfterClass
    public static void tearDown() {
        lmf.shutdown();
    }


    @Test
    public void testCreateCore() throws Exception {
        String program = "title = rdfs:label :: xsd:string ;";

        CoresClient client = new CoresClient(config);

        client.createCoreConfiguration("lmfclient", program);

        // wait for thread to finish
        do {
            log.info("waiting for server to finish ...");
            Thread.sleep(500);
        } while(indexingService.isRunning());

        List<String> coreNames = client.listCores();
        assertThat(coreNames, hasItem("lmfclient"));

        String testProgram = client.getCoreConfiguration("lmfclient");
        assertThat(testProgram, is(equalToIgnoringWhiteSpace(program)));

        client.deleteCore("lmfclient");
        List<String> coreNames2 = client.listCores();
        assertThat(coreNames2, not(hasItem("lmfclient")));
    }
}
