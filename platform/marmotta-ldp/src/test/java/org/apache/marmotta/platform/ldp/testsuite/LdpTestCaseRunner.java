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

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import static org.junit.Assert.assertEquals;

/**
 * LDP Test Case JUnit Runner
 *
 * @author Sergio Fernández
 */
public class LdpTestCaseRunner extends Runner {

    private LdpTestCase testCase;

    public LdpTestCaseRunner(LdpTestCase testCase) {
        this.testCase = testCase;
    }

    @Override
    public Description getDescription() {
        return Description.createSuiteDescription(testCase.getLabel());
    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.fireTestRunStarted(getDescription());
        try {
            run();
        } catch (Exception e) {
            //TODO
            //notifier.fireTestFailure(e);
        }
        notifier.fireTestFinished(getDescription());
    }

    private void run() {
        assertEquals(testCase.getLabel().substring(3), testCase.getUri().getLocalName().substring(2));
        //TODO: actual execution
    }

}
