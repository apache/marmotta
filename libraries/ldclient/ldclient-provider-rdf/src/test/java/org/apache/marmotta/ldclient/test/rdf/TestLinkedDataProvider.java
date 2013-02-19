/*
 * Copyright (c) 2013 The Apache Software Foundation
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

package org.apache.marmotta.ldclient.test.rdf;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.test.helper.TestLDClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;

/**
 * Test if the LinkedDataProvider is working properly.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class TestLinkedDataProvider {

    private static final String DBPEDIA = "http://dbpedia.org/resource/Berlin";
    private static final String GEONAMES = "http://sws.geonames.org/3020251/";
    
    private LDClientService ldclient;

    @Before
    public void setupClient() {
        ldclient = new TestLDClient(new LDClient());
    }

    @After
    public void shutdownClient() {
        ldclient.shutdown();
    }

    /**
     * This method tests accessing the DBPedia Linked Data service, which uses Virtuoso and delivers RDF/XML as
     * well as text/turtle.
     *
     * @throws Exception
     * @todo find a better way to deal with errors actually in the services and not in the code
     */
    @Test 
    public void testDBPedia() throws Exception {
        Assume.assumeTrue(ldclient.ping(DBPEDIA));
        
        ClientResponse respBerlin = ldclient.retrieveResource(DBPEDIA);

        RepositoryConnection conBerlin = respBerlin.getTriples().getConnection();
        conBerlin.begin();
        Assert.assertTrue(conBerlin.size() > 0);

        // run a SPARQL test to see if the returned data is correct
        InputStream sparql = this.getClass().getResourceAsStream("dbpedia-berlin.sparql");
        BooleanQuery testLabel = conBerlin.prepareBooleanQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        Assert.assertTrue("SPARQL test query failed", testLabel.evaluate());

        conBerlin.commit();
        conBerlin.close();
    }

    /**
     * This method tests accessing the GeoNames Linked Data service, which uses HTTP negotiation with redirection to
     * plan RDF files.
     *
     * @throws Exception
     */
    @Test
    public void testGeoNames() throws Exception {
        Assume.assumeTrue(ldclient.ping(GEONAMES));
        
        ClientResponse respEmbrun = ldclient.retrieveResource(GEONAMES);

        RepositoryConnection conEmbrun = respEmbrun.getTriples().getConnection();
        conEmbrun.begin();
        Assert.assertTrue(conEmbrun.size() > 0);

        // run a SPARQL test to see if the returned data is correct
        InputStream sparql = this.getClass().getResourceAsStream("geonames-embrun.sparql");
        BooleanQuery testLabel = conEmbrun.prepareBooleanQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        Assert.assertTrue("SPARQL test query failed", testLabel.evaluate());

        conEmbrun.commit();
        conEmbrun.close();
    }

    /**
     * This method tests accessing the RDFohloh Linked Data service, which uses HTTP negotiation with redirection to
     * plan RDF files.
     *
     * @throws Exception
     *
     */
    /*
    @Test
    public void testRDFOhloh() throws Exception {

        String uriMarmotta = "http://rdfohloh.wikier.org/project/marmotta";
        ClientResponse respMarmotta = ldclient.retrieveResource(uriMarmotta);

        RepositoryConnection conMarmotta = respMarmotta.getTriples().getConnection();
        conMarmotta.begin();
        Assert.assertTrue(conMarmotta.size() > 0);

        // run a SPARQL test to see if the returned data is correct
        InputStream sparql = this.getClass().getResourceAsStream("geonames-embrun.sparql");
        BooleanQuery testLabel = conMarmotta.prepareBooleanQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        Assert.assertTrue("SPARQL test query failed", testLabel.evaluate());

        conMarmotta.commit();
        conMarmotta.close();
    }
    */

}
