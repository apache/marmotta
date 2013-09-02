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
package org.apache.marmotta.kiwi.test.sesame;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.runner.RunWith;
import org.openrdf.sail.RDFStoreTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;

import java.sql.SQLException;

import static org.junit.Assert.fail;

/**
 * Run the Sesame {@link RDFStoreTest} suite.
 * @author Jakob Frank <jakob@apache.org>
 */
@RunWith(KiWiDatabaseRunner.class)
public class KiWiStoreTest extends RDFStoreTest {

    private final KiWiConfiguration kiwiConfig;
    
    public KiWiStoreTest(KiWiConfiguration kiwiConfig) {
        this.kiwiConfig = kiwiConfig;
    }
    
    @Override
    protected Sail createSail() throws SailException {
        Sail store = new SailWrapper(new KiWiStore(kiwiConfig)) {
            @Override
            public void shutDown() throws SailException {
                try {
                    ((KiWiStore)getBaseSail()).getPersistence().dropDatabase();
                } catch (SQLException e) {
                    fail("SQL exception while deleting database");
                }

                super.shutDown();
            }
        };
        store.initialize();
        return store;
    }




}
