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
package org.apache.marmotta.kiwi.test;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;

/**
 * Test if the dialects returns correct values
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(Parameterized.class)
public class DialectTest {

    public KiWiDialect dialect;


    /**
     * Return database configurations if the appropriate parameters have been set.
     *
     * @return an array (database name)
     */
    @Parameterized.Parameters(name="Database Test {index}: {0}")
    public static Iterable<Object[]> databases() {
        String[] databases = {"H2", "PostgreSQL", "MySQL"};

        List<Object[]> result = new ArrayList<Object[]>(databases.length);
        for(String database : databases) {
                result.add(new Object[] {
                        database
                });
        }
        return result;
    }

    public DialectTest(String database) {
        if("H2".equals(database)) {
            this.dialect = new H2Dialect();
        } else if("MySQL".equals(database)) {
            this.dialect = new MySQLDialect();
        } else if("PostgreSQL".equals(database)) {
            this.dialect = new PostgreSQLDialect();
        }
    }

    @Test
    public void testListProperties() {
        Set<String> keys = dialect.getStatementIdentifiers();

        Assert.assertThat(keys,hasItem("load.node_by_id"));

    }


    @Test
    public void testGetStatement() {
        String queryVersion = dialect.getStatement("meta.version");

        Assert.assertEquals("SELECT mvalue FROM metadata WHERE mkey = 'version';", queryVersion);
    }


    @Test
    public void testGetCreateScript() {
        String createScript = dialect.getCreateScript("base");

        Assert.assertNotNull(createScript);
        Assert.assertFalse("".equals(createScript));
    }


    @Test
    public void testGetMigrateScript() {
        String migrateScript = dialect.getMigrationScript(1,"base");

        Assert.assertNotNull(migrateScript);
        Assert.assertTrue("".equals(migrateScript));
    }




}
