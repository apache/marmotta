/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.commons.sesame.facading;

import org.junit.After;
import org.junit.Before;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;

public abstract class AbstractFacadingTest {

    protected Repository repositoryRDF;

    @Before
    public void setup() throws RepositoryException, IOException, RDFParseException {
        repositoryRDF = new SailRepository(new MemoryStore());
        repositoryRDF.initialize();
    }

    @After
    public void tearDown() throws RepositoryException {
        repositoryRDF.shutDown();
    }

}
