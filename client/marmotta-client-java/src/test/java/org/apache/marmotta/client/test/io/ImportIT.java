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
package org.apache.marmotta.client.test.io;

import org.apache.marmotta.client.ClientConfiguration;
import org.apache.marmotta.client.clients.ImportClient;
import org.apache.marmotta.client.clients.ResourceClient;
import org.apache.marmotta.client.exception.MarmottaClientException;
import org.apache.marmotta.client.model.meta.Metadata;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.core.webservices.io.ImportWebService;
import org.apache.marmotta.platform.core.webservices.resource.MetaWebService;
import org.apache.marmotta.platform.core.webservices.resource.ResourceWebService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ImportIT {

    private static JettyMarmotta marmotta;

    private static ClientConfiguration config;

    @BeforeClass
    public static void init() {
        marmotta = new JettyMarmotta("/marmotta", ImportWebService.class, ResourceWebService.class, MetaWebService.class);

        config = new ClientConfiguration("http://localhost:" + marmotta.getPort() + marmotta.getContext());

    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testUpload() throws IOException, MarmottaClientException, URISyntaxException {
        ImportClient client = new ImportClient(config);

        String data = "<http://example.com/resource/r1> <http://example.com/resource/p1> \"Test Data\".";
        client.uploadDataset(data, "text/rdf+n3");

        ResourceClient resourceClient = new ResourceClient(config);
        Metadata m = resourceClient.getResourceMetadata("http://example.com/resource/r1");
        Assert.assertNotNull(m);
        Assert.assertEquals(1, m.size());
        Assert.assertEquals("Test Data", m.getFirst("http://example.com/resource/p1").toString());

        resourceClient.deleteResource("http://example.com/resource/r1");
    }

}
