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
package org.apache.marmotta.kiwi.test;

import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

/**
 * Test the Sesame repository functionality backed by the KiWi triple store. It will try running over all
 * available databases. Except for in-memory databases like H2 or Derby, database URLs must be passed as
 * system property, or otherwise the test is skipped for this database. Available system properties:
 * <ul>
 *     <li>PostgreSQL:
 *     <ul>
 *         <li>postgresql.url, e.g. jdbc:postgresql://localhost:5433/kiwitest?prepareThreshold=3</li>
 *         <li>postgresql.user (default: lmf)</li>
 *         <li>postgresql.pass (default: lmf)</li>
 *     </ul>
 *     </li>
 *     <li>MySQL:
 *     <ul>
 *         <li>mysql.url, e.g. jdbc:mysql://localhost:3306/kiwitest?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull</li>
 *         <li>mysql.user (default: lmf)</li>
 *         <li>mysql.pass (default: lmf)</li>
 *     </ul>
 *     </li>
 *     <li>H2:
 *     <ul>
 *         <li>h2.url, e.g. jdbc:h2:mem;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10</li>
 *         <li>h2.user (default: lmf)</li>
 *         <li>h2.pass (default: lmf)</li>
 *     </ul>
 *     </li>
 * </ul>
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(Parameterized.class)
public class RepositoryTest {

    private static Logger log = LoggerFactory.getLogger(RepositoryTest.class);

    /**
     * Return database configurations if the appropriate parameters have been set.
     *
     * @return an array (database name, url, user, password)
     */
    @Parameterized.Parameters(name="Database Test {index}: {0} at {1}")
    public static Iterable<Object[]> databases() {
        String[] databases = {"H2", "PostgreSQL", "MySQL"};

        List<Object[]> result = new ArrayList<Object[]>(databases.length);
        for(String database : databases) {
            if(System.getProperty(database.toLowerCase()+".url") != null) {
                result.add(new Object[] {
                        database,
                        System.getProperty(database.toLowerCase()+".url"),
                        System.getProperty(database.toLowerCase()+".user","lmf"),
                        System.getProperty(database.toLowerCase()+".pass","lmf")
                });
            }
        }
        return result;
    }


    private KiWiDialect dialect;

    private String jdbcUrl;

    private String jdbcUser;

    private String jdbcPass;

    private Repository repository;

	private KiWiStore store;

    public RepositoryTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
        this.jdbcPass = jdbcPass;
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;

        if("H2".equals(database)) {
            this.dialect = new H2Dialect();
        } else if("MySQL".equals(database)) {
            this.dialect = new MySQLDialect();
        } else if("PostgreSQL".equals(database)) {
            this.dialect = new PostgreSQLDialect();
        }
        
        DBConnectionChecker.checkDatabaseAvailability(jdbcUrl, jdbcUser, jdbcPass, this.dialect);
    }

	@Before
    public void initDatabase() throws RepositoryException {
        store = new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred" );
		repository = new SailRepository(store);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
        store.getPersistence().dropDatabase();
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
        Assert.assertEquals(4, resources.size());

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
    public void testNamespaces() throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();

        connection.begin();
        connection.setNamespace("ns1","http://localhost/ns1/");
        connection.setNamespace("ns2","http://localhost/ns2/");

        connection.commit();

        Assert.assertEquals("http://localhost/ns1/", connection.getNamespace("ns1"));
        Assert.assertEquals("http://localhost/ns2/", connection.getNamespace("ns2"));
        Assert.assertEquals(2, connection.getNamespaces().asList().size());
        Assert.assertThat(
                connection.getNamespaces().asList(),
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
                connection.getNamespaces().asList(),
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
        Assert.assertEquals(1, connection.getNamespaces().asList().size());


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
            Assert.assertEquals(4, resources.size());

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
            Assert.assertEquals(3, resources2.size());

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

            oldTriples = connectionRDF.getStatements(null,null,null,true).asList();
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

            newTriples = connection.getStatements(null,null,null,true).asList();
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
}
