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
package org.apache.marmotta.kiwi.sparql.geosparql;

import info.aduna.iteration.Iterations;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.sparql.sail.KiWiSparqlSail;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test suite for all GeoSPARQL implemented functions.
 *
 * There is 35 test cases for each function:
 *     - Simple Features Topological Relations (8)
 *     - Egenhofer Topological Relations (8)
 *     - RCC8 Topological Relations (8)
 *     - Non-Topological Functions (11)
 *
 * @author Xavier Sumba (xavier.sumba93@ucuenca.ec)
 * @author Sergio Fernández (wikier@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
@KiWiDatabaseRunner.ForDialects(PostgreSQLDialect.class)
public class GeoSPARQLFunctionsTest {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    final static public Pattern POSTGIS_FULL_VERSION = Pattern.compile("^POSTGIS\\=\"(\\d+(\\.\\d+)?(\\.\\d+)?(\\sr\\d+)?)\".*");

    private KiWiStore store;
    private KiWiSparqlSail sail;
    private Repository repository;

    private final KiWiConfiguration dbConfig;

    public GeoSPARQLFunctionsTest(KiWiConfiguration dbConfig) {
        this.dbConfig = dbConfig;
        dbConfig.setFulltextEnabled(true);
        dbConfig.setFulltextLanguages(new String[] {"en"});
        DBConnectionChecker.checkDatabaseAvailability(dbConfig);
    }

    @Before
    public void initDatabase() throws RepositoryException, IOException, RDFParseException {
        Assume.assumeTrue(String.format("PostgreSQL not available! (using %s)", this.dbConfig.getDialect().getClass().getSimpleName()),
                PostgreSQLDialect.class.equals(this.dbConfig.getDialect().getClass()));
        Assume.assumeTrue("PostGIS not available!", checkPostGIS(dbConfig));

        store = new KiWiStore(dbConfig);
        store.setDropTablesOnShutdown(true);
        sail = new KiWiSparqlSail(store);
        repository = new SailRepository(sail);
        repository.initialize();

        log.info("loading data to test...");

        // load demo data spain provinces
        RepositoryConnection conn = repository.getConnection();
        try {
            conn.begin();
            conn.add(this.getClass().getResourceAsStream("/demo_data_spain_provinces.rdf"), "http://localhost/test/geosparql", RDFFormat.RDFXML);
            conn.add(this.getClass().getResourceAsStream("/demo_data_spain_towns.rdf"), "http://localhost/test/geosparql", RDFFormat.RDFXML);
            conn.add(this.getClass().getResourceAsStream("/demo_data_spain_rivers.rdf"), "http://localhost/test/geosparql", RDFFormat.RDFXML);
            conn.commit();
        } finally {
            conn.close();
        }
    }

    /**
     * Performs a basic test to see if PostGIS extension is
     * actually available to use
     *
     * @return
     */
    private boolean checkPostGIS(KiWiConfiguration config) {
        final KiWiPersistence persistence = new KiWiPersistence(config);
        try {
            persistence.initialise();

            final Connection conn = persistence.getJDBCConnection();
            if (conn != null) {
                try {
                    //try(PreparedStatement stmt = conn.prepareStatement("CREATE EXTENSION IF NOT EXISTS postgis;")) {
                    //    stmt.executeQuery();
                    //}

                    try (PreparedStatement stmt = conn.prepareStatement("SELECT PostGIS_full_version();")) {
                        ResultSet result = stmt.executeQuery();
                        if (result.next()) {
                            final String postgis_full_version = result.getString("postgis_full_version");
                            final Matcher matcher = POSTGIS_FULL_VERSION.matcher(postgis_full_version);
                            if (matcher.find()) {
                                log.info("Using PostGIS {}...", matcher.group(1));
                            } else {
                                log.info("Using PostGIS {}...", postgis_full_version);
                            }
                        }
                    }

                    //try(PreparedStatement stmt = conn.prepareStatement("DROP EXTENSION IF NOT EXISTS postgis;")) {
                    //    stmt.executeQuery();
                    //}

                    return true;
                } catch (SQLException e) {
                    log.warn("Checking PostGIS extension has failed: {}", e.getMessage());
                    return false;
                }
            } else {
                log.warn("Impossible to get a JDBC connection");
                return false;
            }
        } catch (Exception e) {
            log.warn("Impossible to get a JDBC connection: {}", e.getMessage());
            return false;
        } finally {
            persistence.shutdown();
        }
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
        if (store != null && store.isInitialized()) {
            log.info("cleaning up test setup...");
            store.getPersistence().dropDatabase();
            repository.shutDown();
        }
    }

    private void testQueryBoolean(String filename, String var) throws Exception {
        String queryString = IOUtils.toString(this.getClass().getResourceAsStream("/" + filename), "UTF-8");

        RepositoryConnection conn = repository.getConnection();
        try {

            conn.begin();

            TupleQuery query1 = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result1 = query1.evaluate();

            conn.commit();

            Assert.assertTrue(result1.hasNext());

            List<BindingSet> results = Iterations.asList(result1);

            Assert.assertTrue(Boolean.parseBoolean(results.get(0).getValue(var).stringValue()));
        } catch (RepositoryException ex) {
            conn.rollback();
        } finally {
            conn.close();
        }
    }

    private void testQueryGeometry(String filename) throws Exception {
        String queryString = IOUtils.toString(this.getClass().getResourceAsStream("/" + filename), "UTF-8");

        RepositoryConnection conn = repository.getConnection();
        try {

            conn.begin();

            TupleQuery query1 = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result1 = query1.evaluate();

            conn.commit();

            Assert.assertTrue(result1.hasNext());

            List<BindingSet> results = Iterations.asList(result1);

            Assert.assertEquals(1, results.size());
        } catch (RepositoryException ex) {
            conn.rollback();
        } finally {
            conn.close();
        }
    }

    @Test
    public void testSfContains() throws Exception {
        testQueryBoolean("sfContains.sparql", "contains");
    }

    @Test
    public void testSfCrosses() throws Exception {
        testQueryBoolean("sfCrosses.sparql", "crosses");
    }

    @Test
    public void testSfDisjoint() throws Exception {
        testQueryBoolean("sfDisjoint.sparql", "disjoint");
    }

    @Test
    public void testSfEquals() throws Exception {
        testQueryBoolean("sfEquals.sparql", "equals");
    }

    @Test
    public void testSfIntersects() throws Exception {
        testQueryBoolean("sfIntersects.sparql", "intersects");
    }

    @Test
    public void testSfOverlaps() throws Exception {
        testQueryBoolean("sfOverlaps.sparql", "overlaps");
    }

    @Test
    public void testSfTouches() throws Exception {
        testQueryBoolean("sfTouches.sparql", "touches");
    }

    @Test
    public void testSfWithin() throws Exception {
        testQueryBoolean("sfWithin.sparql", "within");
    }

    @Test
    public void testBoundary() throws Exception {
        testQueryGeometry("boundary.sparql");
    }

    @Test
    public void testBuffer() throws Exception {
        testQueryGeometry("buffer.sparql");
    }

    @Test
    public void testConvexHull() throws Exception {
        testQueryGeometry("convexHull.sparql");
    }

    @Test
    public void testDifference() throws Exception {
        testQueryGeometry("difference.sparql");
    }

    @Test
    public void testDistance() throws Exception {
        testQueryGeometry("distance.sparql");
    }

    @Test
    public void testEnvelope() throws Exception {
        testQueryGeometry("envelope.sparql");
    }

    @Test
    public void testGetSRID() throws Exception {
        testQueryGeometry("getSRID.sparql");
    }

    @Test
    public void testIntersection() throws Exception {
        testQueryGeometry("intersection.sparql");
    }

    @Test
    public void testRelate() throws Exception {
        testQueryBoolean("relate.sparql", "relate");
    }

    @Test
    public void testSymDifference() throws Exception {
        testQueryGeometry("symDifference.sparql");
    }

    @Test
    public void testUnion() throws Exception {
        testQueryGeometry("union.sparql");
    }

    @Test
    public void testEhEquals() throws Exception {
        testQueryBoolean("ehEquals.sparql", "equals");
    }

    @Test
    public void testEhDisjoint() throws Exception {
        testQueryBoolean("ehDisjoint.sparql", "disjoint");
    }

    @Test
    public void testEhMeet() throws Exception {
        testQueryBoolean("ehMeet.sparql", "ehMeet");
    }

    @Test
    public void testEhOverlap() throws Exception {
        testQueryBoolean("ehOverlap.sparql", "overlap");
    }

    @Test
    public void testEhCovers() throws Exception {
        testQueryBoolean("ehCovers.sparql", "covers");
    }

    @Test
    public void testEhCoveredBy() throws Exception {
        testQueryBoolean("ehCoveredBy.sparql", "coveredBy");
    }

    @Test
    public void testEhInside() throws Exception {
        testQueryBoolean("ehInside.sparql", "inside");
    }

    @Test
    public void testEhContains() throws Exception {
        testQueryBoolean("ehContains.sparql", "contains");
    }

    @Test
    public void testRcc8eq() throws Exception {
        testQueryBoolean("rcc8eq.sparql", "rcc8eq");
    }

    @Test
    public void testRcc8dc() throws Exception {
        testQueryBoolean("rcc8dc.sparql", "rcc8dc");
    }

    @Test
    public void testRcc8ec() throws Exception {
        testQueryBoolean("rcc8ec.sparql", "rcc8ec");
    }

    @Test
    public void testRcc8po() throws Exception {
        testQueryBoolean("rcc8po.sparql", "rcc8po");
    }

    @Test
    public void testRcc8tppi() throws Exception {
        testQueryBoolean("rcc8tppi.sparql", "rcc8tppi");
    }

    @Test
    public void testRcc8tpp() throws Exception {
        testQueryBoolean("rcc8tpp.sparql", "rcc8tpp");
    }

    @Test
    public void testRcc8ntpp() throws Exception {
        testQueryBoolean("rcc8ntpp.sparql", "rcc8ntpp");
    }

    @Test
    public void testRcc8ntppi() throws Exception {
        testQueryBoolean("rcc8ntppi.sparql", "rcc8ntppi");
    }

}
