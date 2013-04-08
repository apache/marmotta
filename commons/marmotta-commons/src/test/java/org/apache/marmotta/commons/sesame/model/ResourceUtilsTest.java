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
package org.apache.marmotta.commons.sesame.model;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assume.assumeThat;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * Test the ResourceUtils (issue 108).
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ResourceUtilsTest {

    private static final String TEST_DATA = "demo-data.foaf";

    private Repository repository;

    /**
     * Setup memory repository and load initial data (demo-data.foaf)
     * @throws RepositoryException
     */
    @Before
    public void setup() throws RepositoryException, IOException, RDFParseException {
        repository = new SailRepository(new MemoryStore());
        repository.initialize();

        // load demo data
        InputStream rdfXML = this.getClass().getResourceAsStream(TEST_DATA);
        // assumeThat("Could not load testfiles", Arrays.<Object> asList(vcard, sparql),
        // everyItem(notNullValue()));
        assumeThat("Could not load testData from '" + TEST_DATA + "'", rdfXML, notNullValue(InputStream.class));

        RepositoryConnection connection = repository.getConnection();
        try {
            connection.add(rdfXML, "http://localhost/foaf/", RDFFormat.RDFXML);
            connection.commit();
        } finally {
            connection.close();
        }
    }

    /**
     * Shutdown the repository properly before the next test.
     *
     * @throws RepositoryException
     */
    @After
    public void teardown() throws RepositoryException {
        repository.shutDown();
    }


    /**
     * Test if listing resources yields the correct results
     */
    @Test
    public void testListResources() throws RepositoryException {
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
        Assert.assertEquals(4,resources.size());

        // test if the result contains all resources that have been used as subject
        Assert.assertThat(resources, hasItems(
                "http://localhost:8080/LMF/resource/hans_meier",
                "http://localhost:8080/LMF/resource/sepp_huber",
                "http://localhost:8080/LMF/resource/anna_schmidt"
                ));
        connection.close();
    }

    /**
     * Test if listing resources yields the correct results
     */
    @Test
    public void testListResourcesByType() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        URI persons = connection.getValueFactory().createURI(Namespaces.NS_FOAF + "Person");

        List<String> resources = ImmutableList.copyOf(
                Iterables.transform(
                        ResourceUtils.listResources(connection,persons),
                        new Function<Resource, String>() {
                            @Override
                            public String apply(Resource input) {
                                return input.stringValue();
                            }
                        }
                        )
                );

        // test if the result has the expected size
        Assert.assertEquals(3,resources.size());

        // test if the result contains all resources that have been used as subject
        Assert.assertThat(resources, hasItems(
                "http://localhost:8080/LMF/resource/hans_meier",
                "http://localhost:8080/LMF/resource/sepp_huber",
                "http://localhost:8080/LMF/resource/anna_schmidt"
                ));
        connection.close();
    }


    /**
     * Test listing resources with a given prefix.
     *
     * @throws RepositoryException
     */
    @Test
    public void testListResourcesByPrefix() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            // Part 1: unlimited listing; we test that the resources with correct prefix are contained and the
            // resources with incorrect prefix not

            List<String> resources1 = ImmutableList.copyOf(
                    Iterables.transform(
                            ResourceUtils.listResourcesByPrefix(connection, "http://localhost:8080/LMF/resource/h"),
                            new Function<Resource, String>() {
                                @Override
                                public String apply(Resource input) {
                                    return input.stringValue();
                                }
                            }
                            )
                    );

            // test if the result has the expected size
            Assert.assertEquals(1,resources1.size());

            // test if the result contains all resources that have been used as subject
            Assert.assertThat(resources1, hasItems(
                    "http://localhost:8080/LMF/resource/hans_meier"
                    ));
            Assert.assertThat(resources1, not(hasItems(
                    "http://localhost:8080/LMF/resource/sepp_huber",
                    "http://localhost:8080/LMF/resource/anna_schmidt"
                    )));


            // Part 2: limited listing; check whether limit and offset work as expected
            List<String> resources2 = ImmutableList.copyOf(
                    Iterables.transform(
                            ResourceUtils.listResourcesByPrefix(connection, "http://localhost:8080/LMF/resource/",0,2),
                            new Function<Resource, String>() {
                                @Override
                                public String apply(Resource input) {
                                    return input.stringValue();
                                }
                            }
                            )
                    );

            // test if the result has the expected size
            Assert.assertEquals(2,resources2.size());

            // test if the result contains some of resources that have been used as subject
            Assert.assertThat(resources2, anyOf(
                    hasItem("http://localhost:8080/LMF/resource/hans_meier"),
                    hasItem("http://localhost:8080/LMF/resource/sepp_huber"),
                    hasItem("http://localhost:8080/LMF/resource/anna_schmidt")
                    ));

            // increase offset by 2 (i.e. next batch of resources)
            List<String> resources3 = ImmutableList.copyOf(
                    Iterables.transform(
                            ResourceUtils.listResourcesByPrefix(connection, "http://localhost:8080/LMF/resource/",2,2),
                            new Function<Resource, String>() {
                                @Override
                                public String apply(Resource input) {
                                    return input.stringValue();
                                }
                            }
                            )
                    );

            // test if the result has the expected size
            Assert.assertEquals(1,resources3.size());

            // test if the result contains some of resources that have been used as subject
            Assert.assertThat(resources3, anyOf(
                    hasItem("http://localhost:8080/LMF/resource/hans_meier"),
                    hasItem("http://localhost:8080/LMF/resource/sepp_huber"),
                    hasItem("http://localhost:8080/LMF/resource/anna_schmidt")
                    ));
        } finally {
            connection.close();
        }

    }


    /**
     * Test retrieving properties
     */
    @Test
    public void testGetProperty() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI sepp = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/sepp_huber");
            URI anna = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/anna_schmidt");
            URI hans = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier");

            // test if getProperty returns the correct names
            Assert.assertEquals("Hans Meier", ResourceUtils.getProperty(connection,hans,"foaf:name"));
            Assert.assertEquals("Sepp Huber", ResourceUtils.getProperty(connection,sepp,"foaf:name"));
            Assert.assertEquals("Anna Schmidt", ResourceUtils.getProperty(connection,anna,"foaf:name"));

            // firstName not set
            Assert.assertNull(ResourceUtils.getProperty(connection,hans,"foaf:firstName"));
        } finally {
            connection.close();
        }
    }

    /**
     * Test retrieving properties
     */
    @Test
    public void testGetProperties() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI sepp = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/sepp_huber");
            URI anna = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/anna_schmidt");
            URI hans = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier");

            List<String> sepp_names = ImmutableList.copyOf(ResourceUtils.getProperties(connection,sepp,"foaf:name"));
            List<String> anna_names = ImmutableList.copyOf(ResourceUtils.getProperties(connection,anna,"foaf:name"));
            List<String> hans_names = ImmutableList.copyOf(ResourceUtils.getProperties(connection,hans,"foaf:name"));
            List<String> hans_xyz = ImmutableList.copyOf(ResourceUtils.getProperties(connection,hans,"foaf:xyz"));

            Assert.assertEquals(1,sepp_names.size());
            Assert.assertEquals(1,anna_names.size());
            Assert.assertEquals(1,hans_names.size());
            Assert.assertEquals(0,hans_xyz.size());

            Assert.assertThat(sepp_names, hasItem("Sepp Huber"));
            Assert.assertThat(anna_names, hasItem("Anna Schmidt"));
            Assert.assertThat(hans_names, hasItem("Hans Meier"));

        } finally {
            connection.close();
        }
    }


    /**
     * Test setting properties
     */
    @Test
    public void testSetProperty() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI toni = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/toni_schneider");
            URI name = connection.getValueFactory().createURI(Namespaces.NS_FOAF + "name");

            ResourceUtils.setProperty(connection,toni,"foaf:name","Anton Schneider");

            // test if getProperty returns the correct names
            Assert.assertEquals("Anton Schneider", ResourceUtils.getProperty(connection,toni,"foaf:name"));

            // test if getStatements returns the correct statement
            RepositoryResult<Statement> triples = connection.getStatements(toni,name,null,true);
            Assert.assertTrue(triples.hasNext());
            Assert.assertEquals("Anton Schneider", triples.next().getObject().stringValue());
            triples.close();
        } finally {
            connection.close();
        }
    }


    /**
     * Test removing properties
     */
    @Test
    public void testRemoveProperty() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI hans = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier");

            // test if getProperty returns the correct names
            Assert.assertEquals("Hans Meier", ResourceUtils.getProperty(connection,hans,"foaf:name"));

            ResourceUtils.removeProperty(connection,hans, "foaf:name");

            Assert.assertNull(ResourceUtils.getProperty(connection,hans,"foaf:name"));
        } finally {
            connection.close();
        }
    }

    /**
     * Test listing outgoing statements
     */
    @Test
    public void testListOutgoingStatements() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI hans = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier");

            List<Statement> result = ImmutableList.copyOf(
                    ResourceUtils.listOutgoing(connection,hans)
                    );

            // check that the number of results is correct
            Assert.assertEquals(12,result.size());

            Assert.assertThat(result, allOf(
                    CoreMatchers.<Statement>hasItem(hasProperty("object", is(connection.getValueFactory().createLiteral("Hans Meier")))),
                    CoreMatchers.<Statement>hasItem(hasProperty("object", is(connection.getValueFactory().createURI(Namespaces.NS_FOAF + "Person"))))
                    ));

        } finally {
            connection.close();
        }
    }

    /**
     * Test listing outgoing nodes
     */
    @Test
    public void testListOutgoingNodes() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI hans = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier");

            List<Value> result = ImmutableList.copyOf(
                    ResourceUtils.listOutgoingNodes(connection, hans, "foaf:name")
                    );

            // check that the number of results is correct
            Assert.assertEquals(1,result.size());

            Assert.assertThat(result, hasItems(
                    (Value) connection.getValueFactory().createLiteral("Hans Meier")
                    ));

        } finally {
            connection.close();
        }
    }


    /**
     * Test adding outgoing statements (by string label of the property)
     */
    @Test
    public void testaddOutgoingNodeLabel() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI toni = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/toni_schneider");
            URI name = connection.getValueFactory().createURI(Namespaces.NS_FOAF + "name");
            String property = "foaf:name";
            Literal value    = connection.getValueFactory().createLiteral("Anton Schneider");

            ResourceUtils.addOutgoingNode(connection,toni,property,value,null);

            // test if getProperty returns the correct names
            Assert.assertEquals("Anton Schneider", ResourceUtils.getProperty(connection,toni,"foaf:name"));

            // test if getStatements returns the correct statement
            RepositoryResult<Statement> triples = connection.getStatements(toni,name,null,true);
            Assert.assertTrue(triples.hasNext());
            Assert.assertEquals("Anton Schneider", triples.next().getObject().stringValue());
            triples.close();

        } finally {
            connection.close();
        }
    }

    /**
     * Test adding outgoing statements (by string label of the property)
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testaddOutgoingNodeProperty() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI toni = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/toni_schneider");
            URI name = connection.getValueFactory().createURI(Namespaces.NS_FOAF + "name");
            Literal value    = connection.getValueFactory().createLiteral("Anton Schneider");

            ResourceUtils.addOutgoingNode(connection,toni,name,value,null);

            // test if getProperty returns the correct names
            Assert.assertEquals("Anton Schneider", ResourceUtils.getProperty(connection,toni,"foaf:name"));

            // test if getStatements returns the correct statement
            RepositoryResult<Statement> triples = connection.getStatements(toni,name,null,true);
            Assert.assertTrue(triples.hasNext());
            Assert.assertEquals("Anton Schneider", triples.next().getObject().stringValue());
            triples.close();

        } finally {
            connection.close();
        }
    }


    /**
     * Test listing outgoing nodes
     */
    @Test
    public void testRemoveOutgoingNodes() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI hans = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier");

            List<Value> result1 = ImmutableList.copyOf(
                    ResourceUtils.listOutgoingNodes(connection, hans, "foaf:name")
                    );

            // check that the number of results is correct
            Assert.assertEquals(1,result1.size());

            Assert.assertThat(result1, hasItems(
                    (Value) connection.getValueFactory().createLiteral("Hans Meier")
                    ));

            ResourceUtils.removeOutgoingNode(connection,hans,"foaf:name", null, null);

            List<Value> result2 = ImmutableList.copyOf(
                    ResourceUtils.listOutgoingNodes(connection, hans, "foaf:name")
                    );

            // check that the number of results is correct
            Assert.assertEquals(0,result2.size());


        } finally {
            connection.close();
        }
    }

    /**
     * Test listing incoming statements
     */
    @Test
    public void testListIncomingStatements() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI hans = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/sepp_huber");

            List<Statement> result = ImmutableList.copyOf(
                    ResourceUtils.listIncoming(connection, hans)
                    );

            // check that the number of results is correct
            Assert.assertEquals(2,result.size());

            Assert.assertThat(result, allOf(
                    CoreMatchers.<Statement>hasItem(hasProperty("subject", is(connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier")))),
                    CoreMatchers.<Statement>hasItem(hasProperty("subject", is(connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/anna_schmidt"))))
                    ));

        } finally {
            connection.close();
        }
    }

    /**
     * Test listing incoming nodes
     */
    @Test
    public void testListIncomingNodes() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI hans = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/sepp_huber");

            List<Resource> result = ImmutableList.copyOf(
                    ResourceUtils.listIncomingNodes(connection, hans, "foaf:knows")
                    );

            // check that the number of results is correct
            Assert.assertEquals(2,result.size());

            Assert.assertThat(result, hasItems(
                    (Resource) connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier"),
                    (Resource) connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/anna_schmidt")
                    ));

        } finally {
            connection.close();
        }
    }


    /**
     * Test getting various forms of labels
     */
    @Test
    public void testGetLabel() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI r1 = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/r1");
            URI r2 = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/r2");
            URI r3 = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/r3");
            URI c1 = connection.getValueFactory().createURI("http://localhost:8080/LMF/context/c1");

            URI rdfs_label = connection.getValueFactory().createURI(Namespaces.NS_RDFS + "label");
            URI dct_title  = connection.getValueFactory().createURI(Namespaces.NS_DC_TERMS + "title");
            URI skos_label = connection.getValueFactory().createURI(Namespaces.NS_SKOS + "prefLabel");

            connection.add(r1,rdfs_label,connection.getValueFactory().createLiteral("R1"));
            connection.add(r2,dct_title,connection.getValueFactory().createLiteral("R2","en"));
            connection.add(r3,skos_label,connection.getValueFactory().createLiteral("R3"),c1);

            Assert.assertEquals("R1", ResourceUtils.getLabel(connection,r1));
            Assert.assertEquals("R2", ResourceUtils.getLabel(connection,r2));
            Assert.assertEquals("R3", ResourceUtils.getLabel(connection,r3));

            Assert.assertEquals("r1", ResourceUtils.getLabel(connection,r1, Locale.ENGLISH));
            Assert.assertEquals("R2", ResourceUtils.getLabel(connection,r2, Locale.ENGLISH));
            Assert.assertEquals("r3", ResourceUtils.getLabel(connection,r3, Locale.ENGLISH));

            Assert.assertEquals("r1", ResourceUtils.getLabel(connection,r1, c1));
            Assert.assertEquals("r2", ResourceUtils.getLabel(connection,r2, c1));
            Assert.assertEquals("R3", ResourceUtils.getLabel(connection,r3, c1));
        } finally {
            connection.close();
        }
    }

    /**
     * Test setting labels.
     *
     * @throws RepositoryException
     */
    @Test
    public void testSetLabel() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI r1 = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/r1");
            URI r2 = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/r2");
            URI r3 = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/r3");
            URI c1 = connection.getValueFactory().createURI("http://localhost:8080/LMF/context/c1");


            ResourceUtils.setLabel(connection,r1,"R1");
            ResourceUtils.setLabel(connection,r2,Locale.ENGLISH,"R2");
            ResourceUtils.setLabel(connection,r3,"R3",c1);


            Assert.assertEquals("R1", ResourceUtils.getLabel(connection,r1));
            Assert.assertEquals("R2", ResourceUtils.getLabel(connection,r2));
            Assert.assertEquals("R3", ResourceUtils.getLabel(connection,r3));

            Assert.assertEquals("r1", ResourceUtils.getLabel(connection,r1, Locale.ENGLISH));
            Assert.assertEquals("R2", ResourceUtils.getLabel(connection,r2, Locale.ENGLISH));
            Assert.assertEquals("r3", ResourceUtils.getLabel(connection,r3, Locale.ENGLISH));

            Assert.assertEquals("r1", ResourceUtils.getLabel(connection,r1, c1));
            Assert.assertEquals("r2", ResourceUtils.getLabel(connection,r2, c1));
            Assert.assertEquals("R3", ResourceUtils.getLabel(connection,r3, c1));
        } finally {
            connection.close();
        }

    }

    /**
     * Test returning types
     */
    @Test
    public void testGetTypes() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI hans = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/sepp_huber");

            List<Resource> result = ImmutableList.copyOf(
                    ResourceUtils.getTypes(connection, hans)
                    );

            // check that the number of results is correct
            Assert.assertEquals(1,result.size());

            Assert.assertThat(result, hasItem(connection.getValueFactory().createURI(Namespaces.NS_FOAF + "Person")));

        } finally {
            connection.close();
        }
    }

    /**
     * Test hasType()
     */
    @Test
    public void testHasTypes() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        try {
            URI hans = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/sepp_huber");

            Assert.assertTrue(ResourceUtils.hasType(connection,hans,connection.getValueFactory().createURI(Namespaces.NS_FOAF + "Person")));
            Assert.assertFalse(ResourceUtils.hasType(connection,hans,connection.getValueFactory().createURI(Namespaces.NS_FOAF + "XYZ")));
        } finally {
            connection.close();
        }
    }



    /**
     * Test if claimed memory resources are properly disposed of when the connection is closed but an iterator is still open
     *
     * TODO: How can we check this?
     *
     */
    public void testResultDispose() {

    }

}
