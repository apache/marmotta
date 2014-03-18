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

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * LDPRs Test Suite
 *
 * @author Sergio Fernández
 */
public class LdpResourcesTestSuite extends LdpAbstractTestSuite {

    @Test
    public void TCR1() throws RepositoryException {
        //not really used, traces of the initial approach
        RepositoryConnection conn = repo.getConnection();
        try {
            conn.begin();
            ValueFactory vf = conn.getValueFactory();
            URI uri = vf.createURI(BASE, name.getMethodName());
            URI tdTestCase = vf.createURI("http://www.w3.org/2006/03/test-description#TestCase"); //TOD: import vocab
            Assert.assertTrue(conn.hasStatement(uri, RDF.TYPE, tdTestCase, false));
            //TestCase tc = loadTestCase(uri);
        } finally {
            conn.close();
        }
    }

}
