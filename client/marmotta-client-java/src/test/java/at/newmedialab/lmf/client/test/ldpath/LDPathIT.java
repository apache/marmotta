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

package at.newmedialab.lmf.client.test.ldpath;

import at.newmedialab.lmf.client.ClientConfiguration;
import at.newmedialab.lmf.client.clients.LDPathClient;
import at.newmedialab.lmf.client.model.rdf.RDFNode;
import org.apache.marmotta.platform.ldpath.webservices.LDPathWebService;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.exception.io.LMFImportException;
import org.apache.marmotta.platform.core.test.base.JettyLMF;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LDPathIT {


    private static JettyLMF lmf;

    private static ClientConfiguration config;

    // the tests require the demo-data.foaf to be loaded; we do so by first calling the import service before we start with tests
    private static ImportService importService;

    @BeforeClass
    public static void init() throws LMFImportException {
        lmf = new JettyLMF("/LMF",8080, LDPathWebService.class);

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
    public void testPath() throws Exception {
        LDPathClient client = new LDPathClient(config);

        List<RDFNode> result = client.evaluatePath("http://localhost:8080/LMF/resource/anna_schmidt", "foaf:knows / foaf:name");
        Assert.assertThat(result, CoreMatchers.<RDFNode> hasItem(hasProperty("content", equalTo("Sepp Huber"))));
    }


    @Test
    public void testProgram() throws Exception {
        LDPathClient client = new LDPathClient(config);

        Map<String, List<RDFNode>> result = client.evaluateProgram("http://localhost:8080/LMF/resource/hans_meier", "friend = foaf:knows / foaf:name :: xsd:string; name = foaf:name :: xsd:string; interest   = foaf:interest / (rdfs:label[@en] | rdfs:label[@none] | <http://rdf.freebase.com/ns/type.object.name>[@en]) :: xsd:string;");
        Assert.assertThat(result,hasKey("interest"));
        Assert.assertThat(result.get("interest"), CoreMatchers.<RDFNode> hasItem(hasProperty("content", equalTo("GNU/Linux"))));

    }
}
