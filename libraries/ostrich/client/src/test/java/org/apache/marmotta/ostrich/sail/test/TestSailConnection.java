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
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
        IRI s = repository.getValueFactory().createIRI("http://umbel.org/umbel/rc/Zyban");

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
