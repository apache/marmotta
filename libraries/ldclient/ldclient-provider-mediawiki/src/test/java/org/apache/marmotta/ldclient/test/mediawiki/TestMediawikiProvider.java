/**
 * Copyright (C) 2013 The Apache Software Foundation.
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
package org.apache.marmotta.ldclient.test.mediawiki;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.test.helper.TestLDClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
 * Test accessing  mediawiki resources
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class TestMediawikiProvider {

    private LDClientService ldclient;

    private static Logger log = LoggerFactory.getLogger(TestMediawikiProvider.class);
    
    @Before
    public void setupClient() {
        ldclient = new TestLDClient(new LDClient());
    }

    @After
    public void shutdownClient() {
        ldclient.shutdown();
    }

    /**
     * This method tests accessing the Youtube Video service via the GData API.
     *
     * @throws Exception
     */
    @Test
    public void testArticle() throws Exception {

        String uriArticle = "http://en.wikipedia.org/wiki/Marmot";
        ClientResponse respArticle = ldclient.retrieveResource(uriArticle);

        RepositoryConnection conArticle = respArticle.getTriples().getConnection();
        conArticle.begin();
        Assert.assertTrue(conArticle.size() > 0);

        // run a SPARQL test to see if the returned data is correct
        InputStream sparql = this.getClass().getResourceAsStream("wikipedia-marmot.sparql");
        BooleanQuery testLabel = conArticle.prepareBooleanQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        Assert.assertTrue("SPARQL test query failed", testLabel.evaluate());

        if(log.isDebugEnabled()) {
            StringWriter out = new StringWriter();
            conArticle.export(Rio.createWriter(RDFFormat.TURTLE, out));
            log.debug("DATA:");
            log.debug(out.toString());
        }

        conArticle.commit();
        conArticle.close();
    }

}
