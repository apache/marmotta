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
package org.apache.marmotta.ldclient.test.rdf;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.endpoint.rdf.SPARQLEndpoint;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.test.helper.TestLDClient;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class TestSPARQLProvider {

    /**
     * This method tests accessing the DBPedia SPARQL service, which uses Virtuoso and delivers RDF/XML as
     * well as text/turtle.
     *
     * @throws Exception
     */
    @Test
    public void testDBPedia() throws Exception {

        ClientConfiguration config = new ClientConfiguration();
        config.addEndpoint(new SPARQLEndpoint("DBPedia (SPARQL)","http://dbpedia.org/sparql","^http://dbpedia\\.org/resource/.*"));

        LDClientService ldclient = new TestLDClient(new LDClient(config));

        String uriBerlin = "http://dbpedia.org/resource/Berlin";
        ClientResponse respBerlin = ldclient.retrieveResource(uriBerlin);

        RepositoryConnection conBerlin = respBerlin.getTriples().getConnection();
        conBerlin.begin();
        Assert.assertTrue(conBerlin.size() > 0);

        // run a SPARQL test to see if the returned data is correct
        InputStream sparql = this.getClass().getResourceAsStream("dbpedia-berlin.sparql");
        BooleanQuery testLabel = conBerlin.prepareBooleanQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        Assert.assertTrue("SPARQL test query failed", testLabel.evaluate());

        conBerlin.commit();
        conBerlin.close();

        ldclient.shutdown();

    }

}
