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
package org.apache.marmotta.kiwi.test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import info.aduna.iteration.Iterations;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.*;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ConcurrentModificationException;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeThat;

/**
 * Test the Sesame repository functionality backed by the KiWi triple store. 
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class RepositoryTest {

    private static Logger log = LoggerFactory.getLogger(RepositoryTest.class);

    private Repository repository;

    private KiWiStore store;

    private final KiWiConfiguration kiwiConfiguration;

    public RepositoryTest(KiWiConfiguration kiwiConfiguration) {
        this.kiwiConfiguration = kiwiConfiguration;

    }

    @Before
    public void initDatabase() throws RepositoryException {
        store = new KiWiStore(kiwiConfiguration);
        store.setDropTablesOnShutdown(true);
        repository = new SailRepository(store);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
        repository.shutDown();
    }


    /**
     * Test importing data; the test will load a small sample RDF file and check whether the expected resources are
     * present.
     *
     * @throws RepositoryException
     * @throws RDFParseException
     * @throws IOException
     */
    @Test
    public void testImport() throws RepositoryException, RDFParseException, IOException {
        long start, end;

        start = System.currentTimeMillis();
        // load demo data
        InputStream rdfXML = this.getClass().getResourceAsStream("demo-data.foaf");
        assumeThat("Could not load test-data: demo-data.foaf", rdfXML, notNullValue(InputStream.class));

        RepositoryConnection connectionRDF = repository.getConnection();
        try {
            connectionRDF.add(rdfXML, "http://localhost/foaf/", RDFFormat.RDFXML);
            connectionRDF.commit();
        } finally {
            connectionRDF.close();
        }
        end = System.currentTimeMillis();

        log.info("IMPORT: {} ms", end-start);

        start = System.currentTimeMillis();
        // get another connection and check if demo data is available
        RepositoryConnection connection = repository.getConnection();

        List<String> resources = ImmutableList.copyOf(
                Iterables.transform(
                        ResourceUtils.listResources(connection),
                        new Function<Resource, String>() {
                            @Override
                            public String apply(Resource input) {
                                return input.stringValue();
                            }
                        }
                )
        );

        // test if the result has the expected size
        //FIXME: this test is no longer valid, because resource existance is not bound to use as subject
        //Assert.assertEquals(4, resources.size());

        // test if the result contains all resources that have been used as subject
        Assert.assertThat(resources, hasItems(
                "http://localhost:8080/LMF/resource/hans_meier",
                "http://localhost:8080/LMF/resource/sepp_huber",
                "http://localhost:8080/LMF/resource/anna_schmidt"
        ));
        connection.commit();
        connection.close();

        end = System.currentTimeMillis();

        log.info("QUERY EVALUATION: {} ms", end-start);
    }

    // TODO: test delete, test query,

    /**
     * Test setting, retrieving and updating namespaces through the repository API
     * @throws RepositoryException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testNamespaces() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        connection.begin();
        connection.setNamespace("ns1","http://localhost/ns1/");
        connection.setNamespace("ns2","http://localhost/ns2/");

        connection.commit();

        Assert.assertEquals("http://localhost/ns1/", connection.getNamespace("ns1"));
        Assert.assertEquals("http://localhost/ns2/", connection.getNamespace("ns2"));
        Assert.assertEquals(2, Iterations.asList(connection.getNamespaces()).size());
        Assert.assertThat(
                Iterations.asList(connection.getNamespaces()),
                CoreMatchers.<Namespace>hasItems(
                        hasProperty("name", is("http://localhost/ns1/")),
                        hasProperty("name", is("http://localhost/ns2/"))
                )
        );

        // update ns1 to a different URL
        connection.begin();
        connection.setNamespace("ns1","http://localhost/ns3/");
        connection.commit();

        Assert.assertEquals("http://localhost/ns3/", connection.getNamespace("ns1"));
        Assert.assertThat(
                Iterations.asList(connection.getNamespaces()),
                CoreMatchers.<Namespace>hasItems(
                        hasProperty("name", is("http://localhost/ns3/")),
                        hasProperty("name", is("http://localhost/ns2/"))
                )
        );


        // remove ns2
        connection.begin();
        connection.removeNamespace("ns2");
        connection.commit();

        connection.begin();
        Assert.assertEquals(1, Iterations.asList(connection.getNamespaces()).size());


        connection.commit();
        connection.close();

    }


    @Test
    public void testDeleteTriple() throws RepositoryException, RDFParseException, IOException {
        // load demo data
        InputStream rdfXML = this.getClass().getResourceAsStream("demo-data.foaf");
        assumeThat("Could not load test-data: demo-data.foaf", rdfXML, notNullValue(InputStream.class));

        RepositoryConnection connectionRDF = repository.getConnection();
        try {
            connectionRDF.add(rdfXML, "http://localhost/foaf/", RDFFormat.RDFXML);
            connectionRDF.commit();
        } finally {
            connectionRDF.close();
        }
        // get another connection and check if demo data is available
        RepositoryConnection connection = repository.getConnection();

        try {
            connection.begin();
            List<String> resources = ImmutableList.copyOf(
                    Iterables.transform(
                            ResourceUtils.listResources(connection),
                            new Function<Resource, String>() {
                                @Override
                                public String apply(Resource input) {
                                    return input.stringValue();
                                }
                            }
                    )
            );

            // test if the result has the expected size
            // FIXME: MARMOTTA-39 (no xsd:string, so one resource is "missing")
            // Assert.assertEquals(31, resources.size());
            Assert.assertEquals(30, resources.size());

            // test if the result contains all resources that have been used as subject
            Assert.assertThat(resources, hasItems(
                    "http://localhost:8080/LMF/resource/hans_meier",
                    "http://localhost:8080/LMF/resource/sepp_huber",
                    "http://localhost:8080/LMF/resource/anna_schmidt"
            ));
            long oldsize = connection.size();
            connection.commit();


            // remove a resource and all its triples
            connection.begin();
            ResourceUtils.removeResource(connection, connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier"));
            connection.commit();

            connection.begin();
            long newsize = connection.size();

            // new size should be less, since we removed some triples
            Assert.assertThat(newsize, lessThan(oldsize));

            // the resource hans_meier should not be contained in the list of resources
            List<String> resources2 = ImmutableList.copyOf(
                    Iterables.transform(
                            ResourceUtils.listSubjects(connection),
                            new Function<Resource, String>() {
                                @Override
                                public String apply(Resource input) {
                                    return input.stringValue();
                                }
                            }
                    )
            );

            // test if the result has the expected size
            //Assert.assertEquals(3, resources2.size());

            // test if the result does not contain the removed resource
            Assert.assertThat(resources2, not(hasItem(
                    "http://localhost:8080/LMF/resource/hans_meier"
            )));
        } finally {
            connection.commit();
            connection.close();
        }
    }


    /**
     * Test a repeated addition of the same triple, because this is a special case in the database.
     */
    @Test
    public void testRepeatedAdd() throws RepositoryException, IOException, RDFParseException {
        // load demo data
        InputStream rdfXML = this.getClass().getResourceAsStream("srfg-ontology.rdf");
        assumeThat("Could not load test-data: srfg-ontology.rdf", rdfXML, notNullValue(InputStream.class));

        long oldsize, newsize;
        List<Statement> oldTriples, newTriples;

        RepositoryConnection connectionRDF = repository.getConnection();
        try {
            connectionRDF.begin();
            connectionRDF.add(rdfXML, "http://localhost/srfg/", RDFFormat.RDFXML);
            connectionRDF.commit();

            oldTriples = Iterations.asList(connectionRDF.getStatements(null,null,null,true));
            oldsize = connectionRDF.size();
        } finally {
            connectionRDF.close();
        }


        // get another connection and add the same data again
        rdfXML = this.getClass().getResourceAsStream("srfg-ontology.rdf");
        RepositoryConnection connection = repository.getConnection();

        try {
            connection.begin();
            connection.add(rdfXML, "http://localhost/srfg/", RDFFormat.RDFXML);
            connection.commit();

            newTriples = Iterations.asList(connection.getStatements(null,null,null,true));
            newsize = connection.size();
        } finally {
            connection.commit();
            connection.close();
        }

        Assert.assertEquals(oldTriples,newTriples);
        Assert.assertEquals(oldsize,newsize);
    }


    /**
     * Test adding-deleting-adding a triple
     *
     * @throws Exception
     */
    @Test
    public void testRepeatedAddRemove() throws Exception {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.add(subject,predicate,object1);
            connection1.commit();

            Assert.assertTrue(connection1.hasStatement(subject,predicate,object1,true));

            connection1.commit();
        } finally {
            connection1.close();
        }

        Literal object2 = repository.getValueFactory().createLiteral(value);
        RepositoryConnection connection2 = repository.getConnection();
        try {
            Assert.assertTrue(connection2.hasStatement(subject,predicate,object2,true));

            connection2.remove(subject,predicate,object2);
            connection2.commit();

            Assert.assertFalse(connection2.hasStatement(subject,predicate,object2,true));

            connection2.commit();
        } finally {
            connection2.close();
        }

        Literal object3 = repository.getValueFactory().createLiteral(value);
        RepositoryConnection connection3 = repository.getConnection();
        try {
            Assert.assertFalse(connection3.hasStatement(subject,predicate,object3,true));

            connection3.add(subject,predicate,object3);
            connection3.commit();

            Assert.assertTrue(connection3.hasStatement(subject,predicate,object3,true));

            connection3.commit();
        } finally {
            connection3.close();
        }

        Literal object4 = repository.getValueFactory().createLiteral(value);
        RepositoryConnection connection4 = repository.getConnection();
        try {
            Assert.assertTrue(connection4.hasStatement(subject,predicate,object4,true));

            connection4.commit();
        } finally {
            connection4.close();
        }


    }

    /**
     * Test adding-deleting-adding a triple
     *
     * @throws Exception
     */
    @Test
    public void testRepeatedAddRemoveTransaction() throws Exception {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.add(subject,predicate,object1);
            connection1.commit();

            Assert.assertTrue(connection1.hasStatement(subject,predicate,object1,true));

            connection1.commit();
        } finally {
            connection1.close();
        }

        Literal object2 = repository.getValueFactory().createLiteral(value);
        Literal object3 = repository.getValueFactory().createLiteral(value);
        RepositoryConnection connection2 = repository.getConnection();
        try {
            Assert.assertTrue(connection2.hasStatement(subject,predicate,object2,true));

            connection2.remove(subject,predicate,object2);
            Assert.assertFalse(connection2.hasStatement(subject,predicate,object2,true));

            connection2.add(subject,predicate,object3);
            Assert.assertTrue(connection2.hasStatement(subject,predicate,object3,true));

            connection2.commit();
        } finally {
            connection2.close();
        }

        Literal object4 = repository.getValueFactory().createLiteral(value);
        RepositoryConnection connection4 = repository.getConnection();
        try {
            Assert.assertTrue(connection4.hasStatement(subject,predicate,object4,true));

            connection4.commit();
        } finally {
            connection4.close();
        }

        // test repeated adding/removing inside the same transaction
        Literal object5 = repository.getValueFactory().createLiteral(RandomStringUtils.randomAlphanumeric(8));
        RepositoryConnection connection5 = repository.getConnection();
        try {
            Assert.assertFalse(connection5.hasStatement(subject, predicate, object5, true));

            connection5.add(subject,predicate,object5);
            Assert.assertTrue(connection5.hasStatement(subject,predicate,object5,true));

            connection5.remove(subject,predicate,object5);
            Assert.assertFalse(connection5.hasStatement(subject,predicate,object5,true));

            connection5.add(subject,predicate,object5);
            Assert.assertTrue(connection5.hasStatement(subject,predicate,object5,true));
            connection5.commit();
        } finally {
            connection5.close();
        }

        RepositoryConnection connection6 = repository.getConnection();
        try {
            Assert.assertTrue(connection6.hasStatement(subject, predicate, object5, true));

            connection6.commit();
        } finally {
            connection6.close();
        }

    }

    @Test
    public void testRepeatedAddRemoveCrossTransaction() throws RepositoryException {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.add(subject,predicate,object1);
            connection1.commit();

            Assert.assertTrue(connection1.hasStatement(subject,predicate,object1,true));

            connection1.commit();
        } finally {
            connection1.close();
        }

        RepositoryConnection connection2 = repository.getConnection();
        try {
            connection2.remove(subject, predicate, object1);
            Assert.assertFalse(connection2.hasStatement(subject, predicate, object1, true));

            connection2.add(subject,predicate,object1);
            Assert.assertTrue(connection2.hasStatement(subject, predicate, object1, true));

            connection2.commit();
        } finally {
            connection2.close();
        }

        RepositoryConnection connection3 = repository.getConnection();
        try {
            Assert.assertTrue(connection3.hasStatement(subject, predicate, object1, true));
            connection3.commit();
        } finally {
            connection3.close();
        }
    }

    @Test
    public void testRepeatedAddRemoveSPARQL() throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.add(subject,predicate,object1);
            connection1.commit();

            Assert.assertTrue(connection1.hasStatement(subject,predicate,object1,true));

            connection1.commit();
        } finally {
            connection1.close();
        }

        RepositoryConnection connection2 = repository.getConnection();
        try {
            String query = String.format("DELETE { <%s> <%s> ?v } INSERT { <%s> <%s> ?v . } WHERE { <%s> <%s> ?v }", subject.stringValue(), predicate.stringValue(), subject.stringValue(), predicate.stringValue(), subject.stringValue(), predicate.stringValue());

            Update u = connection2.prepareUpdate(QueryLanguage.SPARQL, query);
            u.execute();

            connection2.commit();
        } finally {
            connection2.close();
        }

        RepositoryConnection connection3 = repository.getConnection();
        try {
            Assert.assertTrue(connection3.hasStatement(subject, predicate, object1, true));
            connection3.commit();
        } finally {
            connection3.close();
        }
    }


    /**
     * Test the rollback functionality of the triple store by adding a triple, rolling back, adding the triple again.
     *
     * @throws Exception
     */
    @Test
    public void testRollback() throws Exception {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.begin();
            connection1.add(subject,predicate,object);
            connection1.rollback();

        } finally {
            connection1.close();
        }

        RepositoryConnection connection2 = repository.getConnection();
        try {
            connection2.begin();
            Assert.assertFalse(connection2.hasStatement(subject,predicate,object,true));

            connection2.add(subject,predicate,object);
            connection2.commit();

            Assert.assertTrue(connection2.hasStatement(subject,predicate,object,true));

            connection2.commit();
        } finally {
            connection2.close();
        }

    }

    /**
     * This test is for a strange bug that happens when running SPARQL updates that delete and reinsert a triple in
     * the same transaction. See https://issues.apache.org/jira/browse/MARMOTTA-283
     */
    @Test
    public void testMARMOTTA283() throws RepositoryException, RDFParseException, IOException, MalformedQueryException, UpdateExecutionException {

        InputStream rdfXML = this.getClass().getResourceAsStream("demo-data.foaf");
        assumeThat("Could not load test-data: demo-data.foaf", rdfXML, notNullValue(InputStream.class));

        RepositoryConnection connectionRDF = repository.getConnection();
        try {
            connectionRDF.add(rdfXML, "http://localhost/foaf/", RDFFormat.RDFXML);
            connectionRDF.commit();
        } finally {
            connectionRDF.close();
        }


        String update = "DELETE { ?s ?p ?o } INSERT { <http://localhost:8080/LMF/resource/hans_meier> <http://xmlns.com/foaf/0.1/name> \"Hans Meier\" . <http://localhost:8080/LMF/resource/hans_meier> <http://xmlns.com/foaf/0.1/based_near> <http://dbpedia.org/resource/Traunstein> . <http://localhost:8080/LMF/resource/hans_meier> <http://xmlns.com/foaf/0.1/interest> <http://rdf.freebase.com/ns/en.linux> } WHERE { ?s ?p ?o . FILTER ( ?s = <http://localhost:8080/LMF/resource/hans_meier> ) }";

        RepositoryConnection connectionUpdate = repository.getConnection();
        try {
            Update u = connectionUpdate.prepareUpdate(QueryLanguage.SPARQL, update);
            u.execute();
            connectionUpdate.commit();
        } finally {
            connectionUpdate.close();
        }

        // now there should be two triples
        RepositoryConnection connectionVerify = repository.getConnection();
        try {
            URI hans_meier = repository.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier");
            URI foaf_name  = repository.getValueFactory().createURI("http://xmlns.com/foaf/0.1/name");
            URI foaf_based_near = repository.getValueFactory().createURI("http://xmlns.com/foaf/0.1/based_near");
            URI foaf_interest = repository.getValueFactory().createURI("http://xmlns.com/foaf/0.1/interest");
            URI freebase_linux = repository.getValueFactory().createURI("http://rdf.freebase.com/ns/en.linux");
            URI traunstein = repository.getValueFactory().createURI("http://dbpedia.org/resource/Traunstein");

            Assert.assertTrue(connectionVerify.hasStatement(hans_meier,foaf_name,null, true));
            Assert.assertTrue(connectionVerify.hasStatement(hans_meier,foaf_based_near,traunstein, true));
            Assert.assertTrue(connectionVerify.hasStatement(hans_meier,foaf_interest,freebase_linux, true));

            connectionVerify.commit();
        } finally {
            connectionVerify.close();
        }
    }

    /**
     * This test is for a strange bug that happens when running SPARQL updates that delete and reinsert a triple in
     * the same transaction. It is similar to #testMARMOTTA283, but simulates the issue in more detail.
     * See https://issues.apache.org/jira/browse/MARMOTTA-283
     */
    @Test
    public void testMARMOTTA283_2() throws RepositoryException, RDFParseException, IOException, MalformedQueryException, UpdateExecutionException {

        //insert quadruples
        String insert =
                "WITH <http://resource.org/video>" +
                        "INSERT {" +
                        "   <http://resource.org/video> <http://ontology.org#hasFragment> <http://resource.org/fragment1>." +
                        "   <http://resource.org/annotation1> <http://ontology.org#hasTarget> <http://resource.org/fragment1>." +
                        "   <http://resource.org/annotation1> <http://ontology.org#hasBody> <http://resource.org/subject1>." +
                        "   <http://resource.org/fragment1> <http://ontology.org#shows> <http://resource.org/subject1>." +
                        "} WHERE {}";

        RepositoryConnection connectionInsert = repository.getConnection();
        try {
            Update u = connectionInsert.prepareUpdate(QueryLanguage.SPARQL, insert);
            u.execute();
            connectionInsert.commit();
        } finally {
            connectionInsert.close();
        }

        //update quadruples
        String update =
                "WITH <http://resource.org/video>" +
                        "DELETE { " +
                        "   ?annotation ?p ?v." +
                        "   ?fragment ?r ?s." +
                        "   <http://resource.org/video> <http://ontology.org#hasFragment> ?fragment." +
                        "} INSERT {" +
                        "   <http://resource.org/video> <http://ontology.org#hasFragment> <http://resource.org/fragment1>." +
                        "   <http://resource.org/annotation1> <http://ontology.org#hasTarget> <http://resource.org/fragment1>." +
                        "   <http://resource.org/annotation1> <http://ontology.org#hasBody> <http://resource.org/subject1>." +
                        "   <http://resource.org/fragment1> <http://ontology.org#shows> <http://resource.org/subject1>." +
                        "} WHERE {" +
                        "   ?annotation <http://ontology.org#hasTarget> ?fragment." +
                        "   ?annotation ?p ?v." +
                        "   OPTIONAL {" +
                        "       ?fragment ?r ?s" +
                        "   }" +
                        "   FILTER (?fragment = <http://resource.org/fragment1>)" +
                        "} ";

        RepositoryConnection connectionUpdate = repository.getConnection();
        try {
            Update u = connectionUpdate.prepareUpdate(QueryLanguage.SPARQL, update);
            u.execute();
            connectionUpdate.commit();
        } finally {
            connectionUpdate.close();
        }

        //check quadruples
        RepositoryConnection connectionVerify = repository.getConnection();
        try {
            URI video = repository.getValueFactory().createURI("http://resource.org/video");
            URI hasFragment  = repository.getValueFactory().createURI("http://ontology.org#hasFragment");
            URI fragment = repository.getValueFactory().createURI("http://resource.org/fragment1");
            URI annotation = repository.getValueFactory().createURI("http://resource.org/annotation1");
            URI hasTarget = repository.getValueFactory().createURI("http://ontology.org#hasTarget");
            URI hasBody = repository.getValueFactory().createURI("http://ontology.org#hasBody");
            URI subject = repository.getValueFactory().createURI("http://resource.org/subject1");
            URI shows = repository.getValueFactory().createURI("http://ontology.org#shows");

            Assert.assertTrue(connectionVerify.hasStatement(video,hasFragment,fragment,true,video));
            Assert.assertTrue(connectionVerify.hasStatement(annotation,hasTarget,fragment,true,video));
            Assert.assertTrue(connectionVerify.hasStatement(annotation,hasBody,subject,true,video));
            Assert.assertTrue(connectionVerify.hasStatement(fragment,shows,subject,true,video));

            connectionVerify.commit();
        } finally {
            connectionVerify.close();
        }
    }

    /**
     * Test the concurrent connection problem reported in MARMOTTA-236 for facading:
     * - get two parallel connections
     * - add triple in connection 1; should be available in connection 1 and not in connection 2
     * - add same triple in connection 2; should be available in both, connection 1 and connection 2 or
     *   fail-fast by throwing a ConcurrentModificationException
     * @throws Exception
     */
    @Test
    public void testMARMOTTA236() throws Exception {
        RepositoryConnection con1 = repository.getConnection();
        RepositoryConnection con2 = repository.getConnection();

        try {
            URI r1 = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
            URI r2 = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
            URI r3 = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));

            con1.begin();
            con1.add(r1,r2,r3);

            Assert.assertTrue(con1.hasStatement(r1,r2,r3,true));

            con2.begin();
            Assert.assertFalse(con2.hasStatement(r1,r2,r3,true));

            con2.add(r1,r2,r3);

            Assert.assertTrue(con2.hasStatement(r1,r2,r3,true));

            con2.rollback();
            con1.commit();
        } catch (ConcurrentModificationException ex) {

        } finally {
            con1.close();
            con2.close();
        }


    }


    /**
     * MARMOTTA-506 introduces a more efficient clearing of triples, which abandons some consistency guarantees. This
     * test aims to check for any side effect of this change.
     *
     * @throws Exception
     */
    @Test
    public void testFastClearDifferentTransactions() throws Exception {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.add(subject,predicate,object1);
            connection1.commit();

            Assert.assertTrue(connection1.hasStatement(subject,predicate,object1,true));

            connection1.commit();
        } finally {
            connection1.close();
        }

        RepositoryConnection connection2 = repository.getConnection();
        try {
            connection2.clear();

            connection2.commit();

            Assert.assertFalse(connection2.hasStatement(subject, predicate, object1, true));

            connection2.commit();

        } finally {
            connection2.close();
        }

        RepositoryConnection connection3 = repository.getConnection();
        try {
            connection3.add(subject,predicate,object1);
            connection3.commit();

            Assert.assertTrue(connection3.hasStatement(subject, predicate, object1, true));
            connection3.commit();
        } finally {
            connection3.close();
        }

    }


    /**
     * MARMOTTA-506 introduces a more efficient clearing of triples, which abandons some consistency guarantees. This
     * test aims to check for any side effect of this change.
     *
     * @throws Exception
     */
    @Test
    public void testFastClearSameTransaction() throws Exception {
        String value = RandomStringUtils.randomAlphanumeric(8);

        URI subject = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        URI predicate = repository.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
        Literal object1 = repository.getValueFactory().createLiteral(value);

        RepositoryConnection connection1 = repository.getConnection();
        try {
            connection1.add(subject,predicate,object1);

            Assert.assertTrue(connection1.hasStatement(subject,predicate,object1,true));

            connection1.clear();

            Assert.assertFalse(connection1.hasStatement(subject, predicate, object1, true));

            connection1.add(subject,predicate,object1);

            Assert.assertTrue(connection1.hasStatement(subject, predicate, object1, true));

            connection1.commit();
        } finally {
            connection1.close();
        }

    }

}
