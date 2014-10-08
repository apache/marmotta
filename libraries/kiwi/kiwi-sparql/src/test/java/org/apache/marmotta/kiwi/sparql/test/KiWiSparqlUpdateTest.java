/*
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
package org.apache.marmotta.kiwi.sparql.test;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.sparql.sail.KiWiSparqlSail;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.parser.sparql.SPARQLUpdateTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Run the Sesame SPARQL Update Test Suite.
 * @author Jakob Frank <jakob@apache.org>
 *
 */
@RunWith(KiWiDatabaseRunner.class)
public class KiWiSparqlUpdateTest extends SPARQLUpdateTest {

    private final KiWiConfiguration config;

    public KiWiSparqlUpdateTest(KiWiConfiguration config) {
        this.config = config;
    }

    @Override
    protected Repository newRepository() throws Exception {
        KiWiStore store = new KiWiStore(config);
        KiWiSparqlSail ssail = new KiWiSparqlSail(store);
        return new SailRepository(ssail);
    }


    /**
     * This bug is apparently an issue in Sesame and does not really concern our own SPARQL implementation. Not sure
     * how to work around it.
     *
     * See https://openrdf.atlassian.net/browse/SES-2090
     */
    @Test
    @Ignore("ignored until fixed upstream - MARMOTTA-528")
    public void contextualInsertDeleteData() throws RepositoryException, MalformedQueryException, UpdateExecutionException {

        StringBuilder insert = new StringBuilder();
        insert.append(getNamespaceDeclarations());
        insert.append("INSERT DATA { ex:alice foaf:knows ex:bob. ex:alice foaf:mbox \"alice@example.org\" .} ");

        DatasetImpl ds = new DatasetImpl();
        ds.setDefaultInsertGraph(graph2);
        ds.addDefaultRemoveGraph(graph2);

        Update operation1 = con.prepareUpdate(QueryLanguage.SPARQL, insert.toString());
        operation1.setDataset(ds);
        operation1.execute();


        StringBuilder update = new StringBuilder();
        update.append(getNamespaceDeclarations());
        update.append("DELETE DATA { ex:alice foaf:knows ex:bob. ex:alice foaf:mbox \"alice@example.org\" .} ");

        Update operation2 = con.prepareUpdate(QueryLanguage.SPARQL, update.toString());
        operation2.setDataset(ds);

        assertTrue(con.hasStatement(alice, FOAF.KNOWS, bob, true, graph2));
        assertTrue(con.hasStatement(alice, FOAF.MBOX, f.createLiteral("alice@example.org"), true, graph2));
        operation2.execute();

        String msg = "statement should have been deleted.";
        assertFalse(msg, con.hasStatement(alice, FOAF.KNOWS, bob, true, graph2));
        assertFalse(msg, con.hasStatement(alice, FOAF.MBOX, f.createLiteral("alice@example.org"), true, graph2));
    }
}
