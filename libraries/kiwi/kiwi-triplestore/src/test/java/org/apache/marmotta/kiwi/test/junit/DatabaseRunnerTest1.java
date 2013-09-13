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
package org.apache.marmotta.kiwi.test.junit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner.ForDialects;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(KiWiDatabaseRunner.class)
public class DatabaseRunnerTest1 {

    private final KiWiConfiguration dbConfig;

    public DatabaseRunnerTest1(KiWiConfiguration dbConfig) {
        this.dbConfig = dbConfig;
    }
    
    @Test
    public void testDatabase() {
        assertNotNull(dbConfig);
    }

    @Test
    @ForDialects(H2Dialect.class)
    public void testOnlyH2() {
        assertThat(dbConfig.getDialect(), instanceOf(H2Dialect.class));
    }
    
    @Test
    @ForDialects(PostgreSQLDialect.class)
    public void testOnlyPostgreSQL() {
        assertThat(dbConfig.getDialect(), instanceOf(PostgreSQLDialect.class));
    }

    @Test
    @ForDialects(MySQLDialect.class)
    public void testOnlyMySQL() {
        assertThat(dbConfig.getDialect(), instanceOf(MySQLDialect.class));
    }

    @Test
    @ForDialects({PostgreSQLDialect.class, MySQLDialect.class})
    public void testNotH2() {
        assertThat(dbConfig.getDialect(), not(instanceOf(H2Dialect.class)));
    }
    
    @Test
    @ForDialects({H2Dialect.class, MySQLDialect.class})
    public void testNotPostgreSQL() {
        assertThat(dbConfig.getDialect(), not(instanceOf(PostgreSQLDialect.class)));
    }

    @Test
    @ForDialects({PostgreSQLDialect.class, H2Dialect.class})
    public void testNotMySQL() {
        assertThat(dbConfig.getDialect(), not(instanceOf(MySQLDialect.class)));
    }

}
