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
import org.apache.marmotta.platform.ldp.webservices.LdpWebService;
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
import java.util.Collections;
import java.util.List;

/**
 * LDP Test Cases JUnit Runner
 *
 * @author Sergio Fernández
 */
public class LdpTestCasesRunner extends Suite {

    private static Logger log = LoggerFactory.getLogger(LdpTestCasesRunner.class);

    private LdpTestCases.MarmottaResource marmotta;

    private Repository repo;

    private List<Runner> runners;

    public LdpTestCasesRunner(Class<?> klass) throws Throwable {
        super(klass, Collections.EMPTY_LIST);

        //load test cases manifest
        repo = loadManifest();

        //get children runners for each test cases from manifest
        RepositoryConnection conn = repo.getConnection();
        try {
            conn.begin();
            runners = buildTestCasesFromManifest(conn);
            log.info("Initialized LDP test suite with {} test cases", runners.size());
            conn.commit();
        } finally {
            conn.close();
        }

        //get embedded marmotta from rules
        //TODO: it should be an easier way to do it...
        for (TestRule rule : this.classRules()) {
            if (LdpTestCases.MarmottaResource.class.equals(rule.getClass())) {
                marmotta = (LdpTestCases.MarmottaResource)rule;
                break;
            }
        }
    }

    @Override
    protected List<Runner> getChildren() {
        return Collections.unmodifiableList(runners);
    }

    @Override
    protected void runChild(Runner runner, RunNotifier notifier) {
        if (runner instanceof LdpTestCaseRunner) {
            ((LdpTestCaseRunner)runner).setBaseUrl(marmotta.baseUrl + LdpWebService.PATH);
        }
        super.runChild(runner, notifier);
    }

    private Repository loadManifest() throws RepositoryException, RDFParseException, IOException {
        String path = LdpTestCases.ROOT_PATH + LdpTestCases.MANIFEST_CACHE + ".ttl";
        return LdpTestCasesUtils.loadData(path, RDFFormat.TURTLE);
    }

    private static List<Runner> buildTestCasesFromManifest(RepositoryConnection conn) throws Throwable {
        List<Runner> runners = new ArrayList<>();
        try {
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
        } catch (RepositoryException e) {
            log.error("Error loading test cases: {}", e.getMessage(), e);
        } catch (QueryEvaluationException | MalformedQueryException e) {
            log.error("Error performing test cases' query: {}", e.getMessage(), e);
        }
        return runners;
    }

}
