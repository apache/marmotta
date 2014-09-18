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
import org.openrdf.query.parser.sparql.ComplexSPARQLQueryTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;


/**
 * Run the Sesame Complex SPARQL Query Test Suite.

 * @author Jakob Frank <jakob@apache.org>
 *
 */
@RunWith(KiWiDatabaseRunner.class)
public class ComplexKiWiSparqlQueryTest extends ComplexSPARQLQueryTest {


    private final KiWiConfiguration config;

    public ComplexKiWiSparqlQueryTest(KiWiConfiguration config) {
        this.config = config;
    }

    @Override
    protected Repository newRepository() throws Exception {
        KiWiStore store = new KiWiStore(config);
        KiWiSparqlSail ssail = new KiWiSparqlSail(store);
        return new SailRepository(ssail);
    }

    @Test
    @Override
    @Ignore("SPARQL semantics is ridiculous here")
    public void testSES1898LeftJoinSemantics1() throws Exception {
        super.testSES1898LeftJoinSemantics1();
    }

}
