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
package org.apache.marmotta.ldclient.test.rdf;

import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;

/**
 * Test if the LinkedDataProvider is working properly.
 * 
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class TestLinkedDataProvider extends ProviderTestBase {

    private static final String DBPEDIA = "http://dbpedia.org/resource/Berlin";
    private static final String GEONAMES = "http://sws.geonames.org/3020251/";
    private static final String GEONAMES2 = "http://sws.geonames.org/2658434/about.rdf";
    private static final String MARMOTTA = "http://rdfohloh.wikier.org/project/marmotta";
    private static final String WIKIER = "http://www.wikier.org/foaf#wikier";
    private static final String EXAMPLE = "http://example.org/foo";
    private static final String SSL = "https://example.org/foo";

    /**
     * This method tests accessing the DBPedia Linked Data service, which uses Virtuoso and delivers RDF/XML as
     * well as text/turtle.
     *
     * @throws Exception
     * @todo find a better way to deal with errors actually in the services and not in the code
     */
    @Test 
    @Ignore("dbpedia is not reliable")
    public void testDBPedia() throws Exception {
        testResource(DBPEDIA, "dbpedia-berlin.sparql" );
    }

    /**
     * This method tests accessing the GeoNames Linked Data service, which uses HTTP negotiation with redirection to
     * plan RDF files.
     *
     * @throws Exception
     */
    @Test
    public void testGeoNames() throws Exception {
        testResource(GEONAMES, "geonames-embrun.sparql");
    }

    @Test
    @Ignore("just to debug a user report")
    public void testGeoNames2() throws Exception {
        Assume.assumeTrue(ldclient.ping(GEONAMES2));

        ClientResponse response = ldclient.retrieveResource(GEONAMES2);

        RepositoryConnection connection = ModelCommons.asRepository(response.getData()).getConnection();
        connection.begin();
        Assert.assertTrue(connection.size() == 7);
        connection.commit();
        connection.close();
    }

    /**
     * This method tests accessing the RDFohloh Linked Data service, 
     * which uses HTTP negotiation with redirection to provide RDF.
     *
     * @throws Exception
     *
     */
    @Test
    public void testRDFOhloh() throws Exception {
        testResource(MARMOTTA, "ohloh-marmotta.sparql");
    }
    
    /**
     * This method tests accessing Sergio's FOAF profile, which is
     * directly server by Apache HTTPd without content negotiation
     *
     * @throws Exception
     *
     */
    @Test
    public void testFoafWikier() throws Exception {
        testResource(WIKIER, "foaf-wikier.sparql");
    }
    
    /**
     * This method tests accessing a non-RDF resource
     *
     * @throws Exception
     *
     */
    @Test(expected=DataRetrievalException.class)
    public void testNotRDF() throws Exception {
        ClientResponse response = ldclient.retrieveResource(EXAMPLE);
        Assert.assertTrue(response.getData().size() == 0);
    }

    /**
     * This method tests accessing a SSL resource - should throw a DataRetrievalException but otherwise work
     *
     * @throws Exception
     *
     */
    @Test(expected=DataRetrievalException.class)
    public void testSSL() throws Exception {
        ClientResponse response = ldclient.retrieveResource(SSL);
        Assert.assertTrue(response.getData().size() == 0);
    }

}
