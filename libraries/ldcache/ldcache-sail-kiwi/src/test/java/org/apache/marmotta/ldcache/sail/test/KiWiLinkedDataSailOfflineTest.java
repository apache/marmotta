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
package org.apache.marmotta.ldcache.sail.test;

import info.aduna.iteration.Iterations;
import org.apache.marmotta.commons.sesame.filter.resource.ResourceFilter;
import org.apache.marmotta.commons.sesame.filter.resource.UriPrefixFilter;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.ldcache.sail.KiWiLinkedDataSail;
import org.apache.marmotta.ldcache.services.test.dummy.DummyEndpoint;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasToString;


/**
 * This test checks if the transparent linked data caching works, based on an ldclient offline provider. It will try
 * running over all available databases. Except for in-memory databases like
 * H2 or Derby, database URLs must be passed as system property, or otherwise the test is skipped for this database.
 * Available system properties:
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
 *         <li>mysql.pass (default: lmf</li>
 *     </ul>
 *     </li>
 *     <li>H2:
 *     <ul>
 *         <li>h2.url, e.g. jdbc:h2:mem;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10</li>
 *         <li>h2.user (default: lmf)</li>
 *         <li>h2.pass (default: lmf</li>
 *     </ul>
 *     </li>
 * </ul>
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(Parameterized.class)
public class KiWiLinkedDataSailOfflineTest {

    private static final String CACHE_CONTEXT = "http://localhost/context/cache";

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

    private KiWiStore store;

    private KiWiLinkedDataSail lsail;

    private ResourceFilter cacheFilter;

    private Repository repository;

    public KiWiLinkedDataSailOfflineTest(String database, String jdbcUrl, String jdbcUser, String jdbcPass) {
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
    }


    @Before
    public void initDatabase() throws RepositoryException {
        cacheFilter = new UriPrefixFilter("http://localhost/");

        ClientConfiguration config = new ClientConfiguration();
        config.addEndpoint(new DummyEndpoint());

        store = new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred");
        lsail = new KiWiLinkedDataSail(store,cacheFilter,CACHE_CONTEXT, config);
        repository = new SailRepository(lsail);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException {
        store.closeValueFactory();
        lsail.getBackend().getPersistence().dropDatabase();
        store.getPersistence().dropDatabase();
        repository.shutDown();
    }

    /**
     * This test verifies whether the transparent caching works for the three resources provided by our
     * dummy provider.
     * @throws Exception
     */
    @Test
    public void testCachedResources() throws Exception {
        String uri1 = "http://localhost/resource1";
        String uri2 = "http://localhost/resource2";
        String uri3 = "http://localhost/resource3";

        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();

            List<Statement> list1 = Iterations.asList(con.getStatements(con.getValueFactory().createURI(uri1), null, null, true));

            Assert.assertEquals(3,list1.size());
            Assert.assertThat(list1, CoreMatchers.<Statement>hasItems(
                    hasProperty("object", hasToString("\"Value 1\"")),
                    hasProperty("object", hasToString("\"Value X\""))
            ));


            con.commit();

            con.begin();

            List<Statement> list2 = Iterations.asList(con.getStatements(con.getValueFactory().createURI(uri2), null, null, true));

            Assert.assertEquals(2, list2.size());
            Assert.assertThat(list2, allOf(
                    CoreMatchers.<Statement>hasItem(hasProperty("object", hasToString("\"Value 2\"")))
            ));


            con.commit();

            con.begin();

            List<Statement> list3 = Iterations.asList(con.getStatements(con.getValueFactory().createURI(uri3), null, null, true));

            Assert.assertEquals(2, list3.size());
            Assert.assertThat(list3, allOf(
                    CoreMatchers.<Statement>hasItem(hasProperty("object", hasToString("\"Value 3\""))),
                    CoreMatchers.<Statement>hasItem(hasProperty("object", hasToString("\"Value 4\"")))
            ));


            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }
    }

}
