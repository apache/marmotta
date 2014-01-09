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
package org.apache.marmotta.ldcache.sail.test;

import com.google.common.io.Files;
import info.aduna.iteration.Iterations;
import org.apache.commons.io.FileUtils;
import org.apache.marmotta.commons.sesame.filter.resource.ResourceFilter;
import org.apache.marmotta.commons.sesame.filter.resource.UriPrefixFilter;
import org.apache.marmotta.ldcache.backend.file.LDCachingFileBackend;
import org.apache.marmotta.ldcache.sail.GenericLinkedDataSail;
import org.apache.marmotta.ldcache.services.test.dummy.DummyEndpoint;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasToString;


/**
 * This test checks if the transparent linked data caching works, based on an ldclient offline provider. I
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
public class GenericLinkedDataSailOfflineTest {

    private static final String CACHE_CONTEXT = "http://localhost/context/cache";

    private ResourceFilter cacheFilter;

    private Repository repository;

    private LDCachingFileBackend backend;

    private GenericLinkedDataSail lsail;

    private File tmpDirectory;

    @Before
    public void initDatabase() throws RepositoryException {
        cacheFilter = new UriPrefixFilter("http://localhost/");

        ClientConfiguration config = new ClientConfiguration();
        config.addEndpoint(new DummyEndpoint());


        tmpDirectory = Files.createTempDir();

        backend = new LDCachingFileBackend(tmpDirectory);
        lsail = new GenericLinkedDataSail(new MemoryStore(),backend, cacheFilter, config);
        repository = new SailRepository(lsail);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException, IOException {
        repository.shutDown();

        FileUtils.deleteDirectory(tmpDirectory);
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
            Assert.assertThat(list1, allOf(
                    CoreMatchers.<Statement>hasItem(hasProperty("object", hasToString("\"Value 1\""))),
                    CoreMatchers.<Statement>hasItem(hasProperty("object", hasToString("\"Value X\"")))
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
