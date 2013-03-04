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
package org.apache.marmotta.commons.http;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

import java.io.IOException;
import java.io.InputStream;

import org.apache.marmotta.commons.http.ETagGenerator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Test the ETag Generator
 * 
 * @author Sergio Fernández
 */
public class ETagGeneratorTest {

    private static final String TEST_DATA = "/foaf/demo-data.foaf";
    
    private static final String URI_1 = "http://localhost:8080/LMF/resource/hans_meier";
    
    private static final String URI_2 = "http://localhost:8080/LMF/resource/sepp_huber";
    
    private static final String URI_3 = "http://localhost:8080/LMF/resource/anna_schmidt";

    private Repository repository;

    /**
     * Setup memory repository and load initial data (demo-data.foaf)
     * @throws RepositoryException
     */
    @Before
    public void setup() throws RepositoryException, IOException, RDFParseException {
        repository = new SailRepository(new MemoryStore());
        repository.initialize();

        InputStream rdfXML = this.getClass().getResourceAsStream(TEST_DATA);
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
     * Basic tests
     */
    @Test
    public void runningTest() throws RepositoryException {
        RepositoryConnection conn = repository.getConnection();
        String etag = ETagGenerator.getETag(conn, URI_1);
        Assert.assertNotNull(etag);
        String wetag = ETagGenerator.getWeakETag(conn, URI_1);
        Assert.assertNotNull(wetag);
        conn.close();
    }
    
    /**
     * Invariant test
     */
    @Test
    public void invariantTest() throws RepositoryException {
        RepositoryConnection conn1 = repository.getConnection();
        String etag1 = ETagGenerator.getETag(conn1, URI_1);
        Assert.assertNotNull(etag1);
        String wetag1 = ETagGenerator.getWeakETag(conn1, URI_1);
        Assert.assertNotNull(wetag1);
        conn1.close();
        
        RepositoryConnection conn2 = repository.getConnection();
        String etag2 = ETagGenerator.getETag(conn2, URI_1);
        Assert.assertNotNull(etag2);
        String wetag2 = ETagGenerator.getWeakETag(conn2, URI_1);
        Assert.assertNotNull(wetag2);
        conn2.close();
        
        Assert.assertEquals(etag1, etag2);
        Assert.assertEquals(wetag1, wetag2);
    }
    
}
