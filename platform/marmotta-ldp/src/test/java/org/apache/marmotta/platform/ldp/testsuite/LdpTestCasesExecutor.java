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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * LDP Test Cases Executor
 *
 * @author Sergio Fernández
 */
@RunWith(AllTests.class)
public final class LdpTestCasesExecutor {

    private static Logger log = LoggerFactory.getLogger(LdpTestCasesExecutor.class);

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        for (Test test : buildTestCasesFromManifest()) {
            suite.addTest(test);
        }
        return suite;
    }

    private static Collection<Test> buildTestCasesFromManifest() {
        Collection<Test> tests = new ArrayList<>();

        try {
            Repository repo = LdpTestCasesUtils.loadData();
            RepositoryConnection conn = repo.getConnection();
            try {
                conn.begin();

                //TODO: this query is not final, it needs to evolve in parallel with the test cases
                String testCasesQuery =
                        "PREFIX dc: <http://purl.org/dc/terms/> \n" +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
                        "PREFIX ht: <http://www.w3.org/2011/http#> \n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                        "PREFIX td: <http://www.w3.org/2006/03/test-description#> \n" +
                        "PREFIX tn: <http://ldp.example.org/NewTestDefinitions#> \n\n" +
                        "SELECT ?tc ?label \n" +
                        "WHERE { \n" +
                        "  ?tc a td:TestCase ; \n" +
                        "      rdfs:label ?label . \n" +
                        "}";
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, testCasesQuery);
                TupleQueryResult results = tupleQuery.evaluate();
                try {
                    while (results.hasNext()) {
                        BindingSet bindings = results.next();
                        LdpTestCase testCase = new LdpTestCase((URI)bindings.getValue("tc"), bindings.getValue("label").stringValue());

                        //TODO

                        tests.add(testCase);
                    }
                } finally {
                    results.close();
                }
            } catch (RepositoryException e) {
                log.error("Error loading test cases: {}", e.getMessage(), e);
                return tests;
            } catch (QueryEvaluationException | MalformedQueryException e) {
                log.error("Error performing test cases' query: {}", e.getMessage(), e);
                return tests;
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RDFParseException | IOException e) {
            log.error("Error loading test cases: {}", e.getMessage(), e);
            return tests;
        } catch (RepositoryException e) {
            log.error("Error connecting with the repository: {}", e.getMessage(), e);
            return tests;
        }

        return tests;
    }

}
