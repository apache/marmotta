/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.ldcache.services.test.ng;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.model.CacheConfiguration;
import org.apache.marmotta.ldcache.services.LDCache;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base LDCache test
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class BaseLDCacheTest {

    public static final String DBPEDIA = "http://dbpedia.org/resource/Berlin";
    public static final String GEONAMES = "http://sws.geonames.org/3020251/";
    public static final String MARMOTTA = "http://rdfohloh.wikier.org/project/marmotta";
    public static final String WIKIER = "http://www.wikier.org/foaf#wikier";

    private static Logger log = LoggerFactory.getLogger(BaseLDCacheTest.class);

    protected LDCache ldcache;

    protected ValueFactory valueFactory = SimpleValueFactory.getInstance();

    /**
     * Needs to be implemented by tests to provide the correct backend. Backend needs to be properly initialised.
     *
     * @return
     */
    protected abstract LDCachingBackend createBackend();

    @Before
    public void setup() {
        ldcache = new LDCache(new CacheConfiguration(), createBackend());
    }


    @Test
    @Ignore("test failing for the moment because the data returned by the service is wrong")
    public void testDBPedia() throws Exception {
        Assume.assumeTrue(existsClass("org.apache.marmotta.ldclient.provider.rdf.LinkedDataProvider"));

        testResource(DBPEDIA, "dbpedia-berlin.sparql");
    }

    @Test
    public void testGeonames() throws Exception {
        Assume.assumeTrue(existsClass("org.apache.marmotta.ldclient.provider.rdf.LinkedDataProvider"));

        testResource(GEONAMES, "geonames-embrun.sparql");
    }

    @Test
    public void testFOAF() throws Exception {
        Assume.assumeTrue(existsClass("org.apache.marmotta.ldclient.provider.rdf.LinkedDataProvider"));

        testResource(WIKIER, "foaf-wikier.sparql");
    }

    @Test
    @Ignore("test failing for the moment because the issues on the service")
    public void testOHLOH() throws Exception {
        Assume.assumeTrue(existsClass("org.apache.marmotta.ldclient.provider.rdf.LinkedDataProvider"));

        testResource(MARMOTTA, "ohloh-marmotta.sparql");
    }

    /**
     * Test retrieving and caching some resources (provided by DummyProvider).
     */
    @Test
    public void testLocal() throws Exception {
        String uri1 = "http://localhost/resource1";
        String uri2 = "http://localhost/resource2";
        String uri3 = "http://localhost/resource3";

        ldcache.refresh(valueFactory.createIRI(uri1));

        Assert.assertTrue(ldcache.contains(valueFactory.createIRI(uri1)));
        Assert.assertEquals(3, ldcache.get(valueFactory.createIRI(uri1)).size());

        ldcache.refresh(valueFactory.createIRI(uri2));

        Assert.assertTrue(ldcache.contains(valueFactory.createIRI(uri2)));
        Assert.assertEquals(2, ldcache.get(valueFactory.createIRI(uri2)).size());

        ldcache.refresh(valueFactory.createIRI(uri3));

        Assert.assertTrue(ldcache.contains(valueFactory.createIRI(uri3)));
        Assert.assertEquals(2, ldcache.get(valueFactory.createIRI(uri3)).size());
    }


    protected void testResource(String uri, String sparqlFile) throws Exception {

        Assume.assumeTrue(ldcache.getClient().ping(uri));


        Model model = ldcache.get(valueFactory.createIRI(uri));

        Assert.assertTrue(model.size() > 0);

        RepositoryConnection connection = ModelCommons.asRepository(model).getConnection();
        connection.begin();

        // run a SPARQL test to see if the returned data is correct
        InputStream sparql = BaseLDCacheTest.class.getResourceAsStream(sparqlFile);
        final String query = IOUtils.toString(sparql,Charset.defaultCharset());
        BooleanQuery testLabel = connection.prepareBooleanQuery(QueryLanguage.SPARQL, query);
        final boolean testResult = testLabel.evaluate();

        if(!testResult && log.isDebugEnabled()) {
            log.debug("QUERY: {}", query);

            StringWriter out = new StringWriter();
            connection.export(Rio.createWriter(RDFFormat.TURTLE, out));
            log.debug("DATA: {}", out.toString());
        }
        Assert.assertTrue("SPARQL test query failed", testResult);

        connection.commit();
        connection.close();
        connection.getRepository().shutDown();
    }


    protected boolean existsClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
