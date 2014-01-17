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

package org.apache.marmotta.kiwi.test.generator;

import org.apache.marmotta.kiwi.generator.UUIDTimeIDGenerator;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class UUIDTimeTest {

    private static Logger log = LoggerFactory.getLogger(UUIDTimeTest.class);

    private UUIDTimeIDGenerator generator;

    @Before
    public void setup() {
        generator = new UUIDTimeIDGenerator();
    }

    @After
    public void shutdown() {
        generator.shutdown();
    }

    /**
     * Run test for 5 seconds and test how many ids were generated; should be more than 1000 per second
     */
    @Test
    @Ignore
    public void testPerformance() throws SQLException {
        long count = 0;
        long start = System.currentTimeMillis();

        while(System.currentTimeMillis() < start + 5000) {
            long id = generator.getId();
            count++;
            log.trace("Generated ID: {}", id);
        }

        log.info("generated {} ids ({}/sec)", count, count/5);

        Assert.assertTrue(count > 5000);
    }

    @Test
    @Ignore
    public void testNonEqual() throws SQLException {
        long start = System.currentTimeMillis();
        long oldid = 0;

        while(System.currentTimeMillis() < start + 5000) {
            long id = generator.getId();

            Assert.assertNotEquals(oldid,id);
            oldid = id;
        }

    }
}
