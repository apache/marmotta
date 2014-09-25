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
package org.apache.marmotta.platform.ldp.services;

import org.apache.marmotta.platform.core.test.base.AbstractMarmotta;
import org.apache.marmotta.platform.core.test.base.EmbeddedMarmotta;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdpBinaryStoreServiceImplTest {

    private static AbstractMarmotta marmotta;
    private static Logger log = LoggerFactory.getLogger(LdpBinaryStoreServiceImplTest.class);

    @BeforeClass
    public static void init() {
        marmotta = new EmbeddedMarmotta();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        marmotta.shutdown();
        marmotta = null;
    }

    @Test
    public void testGetFile() throws Exception {
        final LdpBinaryStoreServiceImpl store = marmotta.getService(LdpBinaryStoreServiceImpl.class);

        final String test1 = "http://localhost:8080/foo/bar/123";
        Assert.assertThat(store.getFile(test1).toString(), CoreMatchers.endsWith("/localhost.8080/foo/bar/123"));

        // There might be more testing like this here...
    }
}