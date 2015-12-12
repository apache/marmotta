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

package org.apache.marmotta.ostrich.sail.test;

import org.apache.marmotta.ostrich.sail.OstrichSail;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class TestSailConnection {

    private static Repository repository;

    @BeforeClass
    public static void setup() throws RepositoryException {
        repository = new SailRepository(new OstrichSail("localhost", 10000));
        repository.initialize();
    }

    @AfterClass
    public static void teardown() throws RepositoryException {
        repository.shutDown();
    }

    @Test
    public void testQuery() throws RepositoryException, RDFHandlerException {
        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, System.out);
        URI s = repository.getValueFactory().createURI("http://umbel.org/umbel/rc/Zyban");

        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();
            writer.startRDF();

            RepositoryResult<Statement> it = con.getStatements(s, null, null, true);
            while (it.hasNext()) {
                writer.handleStatement(it.next());
            }

            writer.endRDF();

            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }
    }
}
