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
import org.openrdf.sail.RDFStoreTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class CMarmottaSailTest extends RDFStoreTest {

    private static Sail repository;

    @BeforeClass
    public static void setupClass() throws SailException {
        repository = new OstrichSail("localhost", 10000) {
            @Override
            public void shutDown() throws SailException {
                // Clear repository on shutdown, but otherwise reuse it.
                SailConnection con = getConnection();
                con.begin();
                try {
                    con.clear();
                    con.clearNamespaces();
                } finally {
                    con.commit();
                    con.close();
                }
            }
        };
        repository.initialize();
    }

    @AfterClass
    public static void teardownClass() throws SailException {
        repository.shutDown();
    }


    /**
     * Gets an instance of the Sail that should be tested. The returned
     * repository should already have been initialized.
     *
     * @return an initialized Sail.
     * @throws SailException If the initialization of the repository failed.
     */
    @Override
    protected Sail createSail() throws SailException {
        return repository;
    }
}
