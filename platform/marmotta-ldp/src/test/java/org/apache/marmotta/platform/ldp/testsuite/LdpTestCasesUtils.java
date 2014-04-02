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

package org.apache.marmotta.platform.ldp.testsuite;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utils functions for the test cases
 *
 * @author Sergio Fernández
 */
public class LdpTestCasesUtils {

    /**
     * Load test cases' data
     *
     * @return In-Memory repository with the data
     * @throws org.openrdf.rio.RDFParseException
     * @throws org.openrdf.repository.RepositoryException
     * @throws java.io.IOException
     */
    public static Repository loadData() throws RDFParseException, RepositoryException, IOException {
        String path = LdpTestCases.FILES_PATH + LdpTestCases.MANIFEST_CACHE + ".ttl";
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        RepositoryConnection conn = repo.getConnection();
        try {
            conn.begin();
            conn.clear();
            InputStream is = LdpTestCasesUtils.class.getResourceAsStream(path);
            if (is == null) {
                throw new IOException("Manifest file not found at: " + path);
            } else {
                try {
                    conn.add(is, LdpTestCases.BASE, RDFFormat.TURTLE);
                } finally {
                    is.close();
                }
            }
            addNormativeNamespaces(conn);
            conn.commit();
        } finally {
            conn.close();
        }
        return repo;
    }

    /**
     * Add some normative namespaces
     *
     * @param conn target connection
     * @throws IOException
     * @throws RepositoryException
     *
     * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/Test%20Cases/LDP%20Test%20Cases.html#h3_namespaces-used">Sec. 4.1 Namespaces used</a>
     */
    public static void addNormativeNamespaces(RepositoryConnection conn) throws IOException, RepositoryException {
        String path = LdpTestCases.FILES_PATH + "namespaces.properties";
        Properties properties = new Properties();
        properties.load(LdpTestCasesUtils.class.getResourceAsStream(path));
        for(String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            conn.setNamespace(value, key);
        }
    }
}
