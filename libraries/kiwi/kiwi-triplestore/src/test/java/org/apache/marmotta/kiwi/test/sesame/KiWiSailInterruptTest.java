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
package org.apache.marmotta.kiwi.test.sesame;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInterruptTest;

/**
 * Run the Sesame {@link SailInterruptTest} suite.
 * @author Jakob Frank <jakob@apache.org>
 *
 */
@RunWith(KiWiDatabaseRunner.class)
public class KiWiSailInterruptTest extends SailInterruptTest {

    private final KiWiConfiguration kiwiConfig;

    public KiWiSailInterruptTest(KiWiConfiguration kiwiConfig) {
        super(String.format("%s (%S)", KiWiSailInterruptTest.class.getSimpleName(), kiwiConfig.getName()));
        this.kiwiConfig = kiwiConfig;
        
    }
    
    
    
    @Override
    protected Sail createSail() throws SailException {
        KiWiStore store = new KiWiStore(kiwiConfig);
        store.setDropTablesOnShutdown(true);
        return store;
    }
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    @Override
    @Test
    public void testQueryInterrupt() throws Exception {
        super.testQueryInterrupt();
    }
}
