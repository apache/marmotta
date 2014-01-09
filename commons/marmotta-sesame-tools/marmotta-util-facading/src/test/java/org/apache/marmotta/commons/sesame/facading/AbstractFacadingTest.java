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

package org.apache.marmotta.commons.sesame.facading;

import java.io.IOException;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.junit.After;
import org.junit.Before;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParseException;

public abstract class AbstractFacadingTest {

    protected Repository repositoryRDF;

    @Before
    public void setup() throws RepositoryException, IOException, RDFParseException {
        // jdbc:h2:mem;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10
        repositoryRDF = new SailRepository(
                new KiWiStore(new KiWiConfiguration(
                        "kiwiTest",
                        "jdbc:h2:mem:facading;MVCC=true;DB_CLOSE_ON_EXIT=TRUE;DB_CLOSE_DELAY=10",
                        "", "", new H2Dialect(),
                        "http://example.com/ctx/default", "http://example.com/ctx/inferred")));
        repositoryRDF.initialize();
    }

    @After
    public void tearDown() throws RepositoryException {
        repositoryRDF.shutDown();
    }

}
