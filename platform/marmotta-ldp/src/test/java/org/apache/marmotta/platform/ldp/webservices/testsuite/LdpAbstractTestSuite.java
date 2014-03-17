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

package org.apache.marmotta.platform.ldp.webservices.testsuite;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * LDP Abstract Test Suite
 *
 * @author Sergio Fernández
 * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/Test%20Cases/LDP%20Test%20Cases.html">Linked Data Platform 1.0 Test Cases</a>
 */
public abstract class LdpAbstractTestSuite {

    protected static Logger log = LoggerFactory.getLogger(LdpAbstractTestSuite.class);

    public final static String FILES_PATH = "/testsuite/";

    public final static String TEST_CASES_CACHE = "LDP-Test-Cases-WD-20140317";

    protected Repository repo;

    @Rule
    public TestName name = new TestName();

    @Before
    public final void before() throws RepositoryException, RDFParseException, IOException {
        log.info("initializing test case {}...", name.getMethodName());
        repo = loadData(TEST_CASES_CACHE);
        Assume.assumeNotNull(repo);
    }

    @After
    public final void after() throws RepositoryException {
        if (repo != null) {
            repo.shutDown();
            repo = null;
        }
    }

    /**
     * Load a dataset into a new in-memory repository
     *
     * @param file file name
     * @return connection to the repository
     * @throws RDFParseException
     * @throws RepositoryException
     * @throws IOException
     */
    protected Repository loadData(String file) throws RDFParseException, RepositoryException, IOException {
        log.debug("creating new in-memory repository...");
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        RepositoryConnection conn = repo.getConnection();
        try {
            conn.begin();
            conn.clear();
            conn.clearNamespaces();
            addNormativeNamespaces(conn);
            loadData(conn, file);
            conn.commit();
        } finally {
            conn.close();
        }
        return repo;
    }

    /**
     * Load a dataset to the connection passed
     *
     * @param conn connection
     * @param file test case identifier
     * @throws RDFParseException
     * @throws RepositoryException
     * @throws IOException
     */
    protected void loadData(RepositoryConnection conn, String file) throws RDFParseException, RepositoryException, IOException {
        log.debug("loading test cases {}...", file);
        String path = FILES_PATH + file + ".ttl";
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) {
            log.error("test cases data {} not found where expected ({})", file, path);
        } else {
            try {
                conn.add(is, "", RDFFormat.TURTLE);
            } finally {
                is.close();
            }
            log.debug("test cases data {} successfully loaded ({} triples)", file, conn.size());
        }
    }

    /**
     * Add some normative namespaces
     *
     * @param conn target connection
     * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/Test%20Cases/LDP%20Test%20Cases.html#h3_namespaces-used">Sec. 4.1 Namespaces used</a>
     */
    private void addNormativeNamespaces(RepositoryConnection conn) {
        Properties properties = new Properties();
        String path = FILES_PATH + "namespaces.properties";
        try {
            properties.load(getClass().getResourceAsStream(path));
            for(String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                try {
                    conn.setNamespace(value, key);
                } catch (RepositoryException e) {
                    log.error("error adding namespace {}: {}", key, e.getMessage());
                }
            }
        } catch (IOException | NullPointerException e) {
            log.error("error loading normative namespaces at {}: {}", path, e.getMessage());
        }

    }

}
