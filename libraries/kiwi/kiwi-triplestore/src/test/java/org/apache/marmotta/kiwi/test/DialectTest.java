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
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.hamcrest.Matchers.hasItem;

/**
 * Test if the dialects returns correct values
 * <p/>
 * @author Sebastian Schaffert
 */
@RunWith(KiWiDatabaseRunner.class)
public class DialectTest {

    public final KiWiDialect dialect;


    public DialectTest(KiWiConfiguration configuration) {
        this.dialect = configuration.getDialect();
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
        Assert.assertFalse("".equals(migrateScript));

        String migrateScript2 = dialect.getMigrationScript(4,"base");

        Assert.assertNotNull(migrateScript2);
        Assert.assertTrue("".equals(migrateScript2));
    }


    final Logger logger =
            LoggerFactory.getLogger(DialectTest.class);


}
