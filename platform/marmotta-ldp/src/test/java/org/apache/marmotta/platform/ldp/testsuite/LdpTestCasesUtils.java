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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
     * @param path path to the manifest file
     * @param format serialization format used in the manifest file
     * @return In-Memory repository with the data
     * @throws org.openrdf.rio.RDFParseException
     * @throws org.openrdf.repository.RepositoryException
     * @throws java.io.IOException
     */
    public static Repository loadData(String path, RDFFormat format) throws RDFParseException, RepositoryException, IOException {
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
     * Get normative namespaces
     *
     * @throws IOException
     * @throws RepositoryException
     *
     * @see <a href="https://dvcs.w3.org/hg/ldpwg/raw-file/default/Test%20Cases/LDP%20Test%20Cases.html#h3_namespaces-used">Sec. 4.1 Namespaces used</a>
     */
    public static Map<String,String> getNormativeNamespaces() throws IOException {
        String path = LdpTestCases.FILES_PATH + "namespaces.properties";
        Map<String,String> prefixes = new HashMap<>();
        Properties properties = new Properties();
        properties.load(LdpTestCasesUtils.class.getResourceAsStream(path));
        for(String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            prefixes.put(key, value);
        }
        return Collections.unmodifiableMap(prefixes);
    }

    /**
     * Get normative namespaces with SPARQL syntax
     *
     * @throws IOException
     */
    public static String getNormativeNamespacesSparql() throws IOException {
        StringBuffer sb = new StringBuffer();
        Map<String, String> normativeNamespaces = getNormativeNamespaces();
        for (Map.Entry<String, String> entry : normativeNamespaces.entrySet()) {
            //PREFIX dc: <http://purl.org/dc/terms/>
            sb.append("PREFIX ");
            sb.append(entry.getKey());
            sb.append(": <");
            sb.append(entry.getValue());
            sb.append("> \n");
        }
        return sb.toString();
    }

    /**
     * Add normative namespaces
     *
     * @param conn target connection
     * @throws IOException
     * @throws RepositoryException
     */
    public static void addNormativeNamespaces(RepositoryConnection conn) throws IOException, RepositoryException {
        Map<String, String> normativeNamespaces = getNormativeNamespaces();
        for (Map.Entry<String, String> entry : normativeNamespaces.entrySet()) {
            conn.setNamespace(entry.getValue(), entry.getKey());
        }
    }

}
