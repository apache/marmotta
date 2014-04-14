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

import org.apache.commons.lang3.StringUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkField;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * LDP Test Cases JUnit Runner
 *
 * @author Sergio Fernández
 */
public class LdpTestCasesRunner extends Suite {

    private static Logger log = LoggerFactory.getLogger(LdpTestCasesRunner.class);

    private LdpTestCases.MarmottaResource marmotta;

    public LdpTestCasesRunner(Class<?> klass) throws Throwable {
        super(klass, buildTestCasesFromManifest());

        //TODO: it should be an easier way to do it...
        for (TestRule rule : this.classRules()) {
            if (LdpTestCases.MarmottaResource.class.equals(rule.getClass())) {
                marmotta = (LdpTestCases.MarmottaResource)rule;
                break;
            }
        }
    }

    @Override
    protected void runChild(Runner runner, RunNotifier notifier) {
        if (runner instanceof LdpTestCaseRunner) {
            ((LdpTestCaseRunner)runner).setBaseUrl(marmotta.baseUrl);
        }
        super.runChild(runner, notifier);
    }

    private static List<Runner> buildTestCasesFromManifest() throws Throwable {
        List<Runner> runners = new ArrayList<>();

        String path = LdpTestCases.ROOT_PATH + LdpTestCases.MANIFEST_CACHE + ".ttl";
        try {
            Repository repo = LdpTestCasesUtils.loadData(path, RDFFormat.TURTLE);
            RepositoryConnection conn = repo.getConnection();
            try {
                conn.begin();

                String testCasesQuery = LdpTestCasesUtils.getNormativeNamespacesSparql()+ "\n"
                        + "SELECT ?tc WHERE { ?tc a td:TestCase }";
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, testCasesQuery);
                TupleQueryResult results = tupleQuery.evaluate();
                try {
                    while (results.hasNext()) {
                        BindingSet bindings = results.next();
                        LdpTestCase testCase = new LdpTestCase((URI)bindings.getValue("tc"));
                        runners.add(new LdpTestCaseRunner(testCase));
                    }
                } finally {
                    results.close();
                }
                conn.commit();
            } catch (RepositoryException e) {
                log.error("Error loading test cases: {}", e.getMessage(), e);
                return runners;
            } catch (QueryEvaluationException | MalformedQueryException e) {
                log.error("Error performing test cases' query: {}", e.getMessage(), e);
                return runners;
            } finally {
                conn.close();
            }
        } catch (RDFParseException | IOException e) {
            log.error("Error loading test cases: {}", e.getMessage(), e);
            return runners;
        } catch (RepositoryException e) {
            log.error("Error connecting with the repository: {}", e.getMessage(), e);
            return runners;
        }

        log.info("Initialized LDP test suite with {} test cases", runners.size());
        return runners;
    }

}
