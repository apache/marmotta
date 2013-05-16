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
package org.apache.marmotta.ldcache.backend.file.test;

import com.google.common.io.Files;
import info.aduna.iteration.CloseableIteration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.marmotta.ldcache.api.LDCachingConnection;
import org.apache.marmotta.ldcache.backend.file.LDCachingFileBackend;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This test checks if the ldcache backend works, i.e. the system properly stores cache entries and cached triples.
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LDCacheBackendTest {


    private LDCachingFileBackend backend;

    private File tmpDirectory;

    @Before
    public void initDatabase() throws RepositoryException {
        tmpDirectory = Files.createTempDir();

        backend = new LDCachingFileBackend(tmpDirectory);
        backend.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException, IOException {
        backend.shutdown();

        FileUtils.deleteDirectory(tmpDirectory);
    }


    final Logger logger =
            LoggerFactory.getLogger(this.getClass());

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        /**
         * Invoked when a test is about to start
         */
        @Override
        protected void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };
    /**
     * This test verifies if triples are added to the correct context using the repository connection obtained from the backend
     * @throws Exception
     */
    @Test
    public void testStoreTriples() throws Exception {

        RepositoryConnection con = backend.getCacheConnection("http://localhost/resource/1");
        try {
            con.begin();

            URI subject    = con.getValueFactory().createURI("http://localhost/resource/1");
            URI predicate  = con.getValueFactory().createURI("http://localhost/resource/" + RandomStringUtils.randomAlphanumeric(8));
            Literal object1 = con.getValueFactory().createLiteral(RandomStringUtils.random(64));
            Literal object2 = con.getValueFactory().createLiteral(RandomStringUtils.random(64));

            Statement stmt1 = con.getValueFactory().createStatement(subject,predicate,object1);
            Statement stmt2 = con.getValueFactory().createStatement(subject,predicate,object2);

            con.add(stmt1);
            con.add(stmt2);

            con.commit();

            RepositoryResult<Statement> it = con.getStatements(subject,predicate,null,true);
            try {
                while(it.hasNext()) {
                    Statement next =  it.next();
                    Assert.assertEquals(subject, next.getSubject());
                }
            } finally {
                it.close();
            }

            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }
    }

    /**
     * Test storing and retrieving cache entries
     *
     * @throws Exception
     */
    @Test
    public void testStoreEntries() throws Exception {
        LDCachingConnection con = backend.getCacheConnection("http://localhost/resource/1");
        try {
            con.begin();

            URI subject1     = con.getValueFactory().createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
            URI subject2     = con.getValueFactory().createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));
            Literal object1  = con.getValueFactory().createLiteral(RandomStringUtils.random(64));
            Literal object2  = con.getValueFactory().createLiteral(RandomStringUtils.random(64));
            URI predicate    = con.getValueFactory().createURI("http://localhost/resource/"+ RandomStringUtils.randomAlphanumeric(8));

            Statement stmt1 = con.getValueFactory().createStatement(subject1,predicate,object1);
            Statement stmt2 = con.getValueFactory().createStatement(subject2,predicate,object2);

            con.add(stmt1);
            con.add(stmt2);

            con.commit();

            con.begin();
            CacheEntry entry1 = new CacheEntry();
            entry1.setExpiryDate(new Date(System.currentTimeMillis()+1000*60));
            entry1.setLastRetrieved(new Date());
            entry1.setUpdateCount(1);
            entry1.setResource(subject1);
            con.addCacheEntry(subject1, entry1);
            con.commit();

            Assert.assertTrue(backend.isCached(subject1.stringValue()));
            Assert.assertFalse(backend.isCached(subject2.stringValue()));
            Assert.assertEquals(1,asList(backend.listCacheEntries()).size());
            Assert.assertEquals(0,asList(backend.listExpiredEntries()).size());

            con.begin();
            CacheEntry entry2 = new CacheEntry();
            entry2.setExpiryDate(new Date(System.currentTimeMillis() - 1000 * 60));
            entry2.setLastRetrieved(new Date());
            entry2.setUpdateCount(1);
            entry2.setResource(subject2);
            con.addCacheEntry(subject2,entry2);

            con.commit();

            Assert.assertTrue(backend.isCached(subject1.stringValue()));
            Assert.assertTrue(backend.isCached(subject2.stringValue()));
            Assert.assertEquals(2,asList(backend.listCacheEntries()).size());
            Assert.assertEquals(1,asList(backend.listExpiredEntries()).size());

            con.begin();
            con.removeCacheEntry(subject1);
            con.commit();

            Assert.assertFalse(backend.isCached(subject1.stringValue()));
            Assert.assertTrue(backend.isCached(subject2.stringValue()));
            Assert.assertEquals(1,asList(backend.listCacheEntries()).size());
            Assert.assertEquals(1,asList(backend.listExpiredEntries()).size());
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }
    }


    /**
     * Workaround for https://openrdf.atlassian.net/browse/SES-1702 in Sesame 2.7.0-beta1
     * @param <E>
     * @return
     */
    public static <E,X extends Exception> List<E> asList(CloseableIteration<E,X> result) throws RepositoryException {
        ArrayList<E> collection = new ArrayList<E>();
        try {
            try {
                while (result.hasNext()) {
                    collection.add(result.next());
                }

                return collection;
            } finally {
                result.close();
            }
        } catch(Throwable ex) {
            throw new RepositoryException(ex);
        }
    }

}
