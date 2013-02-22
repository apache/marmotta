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

package at.newmedialab.lmf.client.test.io;

import at.newmedialab.lmf.client.ClientConfiguration;
import at.newmedialab.lmf.client.clients.ImportClient;
import at.newmedialab.lmf.client.clients.ResourceClient;
import at.newmedialab.lmf.client.exception.LMFClientException;
import at.newmedialab.lmf.client.model.meta.Metadata;
import org.apache.marmotta.platform.core.test.base.JettyLMF;
import org.apache.marmotta.platform.core.webservices.io.ImportWebService;
import org.apache.marmotta.platform.core.webservices.resource.MetaWebService;
import org.apache.marmotta.platform.core.webservices.resource.ResourceWebService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ImportIT {

    private static JettyLMF lmf;

    private static ClientConfiguration config;

    @BeforeClass
    public static void init() {
        lmf = new JettyLMF("/LMF", 8080, ImportWebService.class, ResourceWebService.class, MetaWebService.class);

        config = new ClientConfiguration("http://localhost:8080/LMF");

    }

    @AfterClass
    public static void tearDown() {
        lmf.shutdown();
    }



    @Test
    public void testUpload() throws IOException, LMFClientException {
        ImportClient client = new ImportClient(config);

        String data = "<http://example.com/resource/r1> <http://example.com/resource/p1> \"Test Data\".";
        client.uploadDataset(data,"text/rdf+n3");

        ResourceClient resourceClient = new ResourceClient(config);
        Metadata m = resourceClient.getResourceMetadata("http://example.com/resource/r1");
        Assert.assertNotNull(m);
        Assert.assertEquals(1,m.size());
        Assert.assertEquals("Test Data", m.getFirst("http://example.com/resource/p1").toString());

        resourceClient.deleteResource("http://example.com/resource/r1");
    }

}
