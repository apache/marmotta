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

package org.apache.marmotta.kiwi.test;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.SailException;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * This test starts many triplestore operations in parallel to check if concurrent operations will break things,
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MySQLConcurrencyTest extends ConcurrencyTestBase {

    private static KiWiStore store;

    @BeforeClass
    public static void setup() throws RepositoryException {
        logger = LoggerFactory.getLogger(MySQLConcurrencyTest.class);

        KiWiConfiguration mysqlConfig = KiWiDatabaseRunner.createKiWiConfig("MySQL", new MySQLDialect());
        DBConnectionChecker.checkDatabaseAvailability(mysqlConfig);
        
        rnd = new Random();

        store = new KiWiStore(mysqlConfig);
        store.setDropTablesOnShutdown(true);
        repository = new SailRepository(store);
        repository.initialize();
    }

    @AfterClass
    public static void dropDatabase() throws RepositoryException, SQLException, SailException {
    	if (store != null && store.isInitialized()) {
            assertTrue(store.checkConsistency());
            repository.shutDown();
    	}
    }
}
