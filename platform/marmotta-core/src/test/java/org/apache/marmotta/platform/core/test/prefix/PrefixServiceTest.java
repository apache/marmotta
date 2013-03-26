/**
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
package org.apache.marmotta.platform.core.test.prefix;

import java.net.URISyntaxException;

import org.apache.marmotta.platform.core.api.prefix.PrefixService;
import org.apache.marmotta.platform.core.test.base.EmbeddedMarmotta;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the functionality of the prefix service
 * 
 * @author Sergio Fernández
 */
public class PrefixServiceTest {

    private static EmbeddedMarmotta marmotta;
    private static PrefixService prefixService;

    @BeforeClass
    public static void setUp() {
        marmotta = new EmbeddedMarmotta();
        prefixService = marmotta.getService(PrefixService.class);
    }
    
    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
        marmotta = null;
        prefixService = null;
    }

    @Test
    public void testSet() throws IllegalArgumentException, URISyntaxException {
        String prefix = "foo";
        String namespace = "http://foo#";

        Assert.assertNull(prefixService.getNamespace(prefix));

        prefixService.add(prefix, namespace);
        Assert.assertNotNull(prefixService.getNamespace(prefix));
        Assert.assertEquals(namespace, prefixService.getNamespace(prefix));
        Assert.assertEquals(prefix, prefixService.getPrefix(namespace));

    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testDoubleSet() throws IllegalArgumentException, URISyntaxException {
        String prefix = "bar";
        String namespace1 = "http://bar1#";
        String namespace2 = "http://bar2#";

        Assert.assertNull(prefixService.getNamespace(prefix));

        prefixService.add(prefix, namespace1);
        Assert.assertNotNull(prefixService.getNamespace(prefix));
        Assert.assertEquals(namespace1, prefixService.getNamespace(prefix));
        Assert.assertEquals(prefix, prefixService.getPrefix(namespace1));
        
        prefixService.add(prefix, namespace2);

    }
    
    @Test
    public void testForceDoubleSet() throws IllegalArgumentException, URISyntaxException {
        String prefix = "baz";
        String namespace1 = "http://baz1#";
        String namespace2 = "http://baz2#";

        Assert.assertNull(prefixService.getNamespace(prefix));

        prefixService.add(prefix, namespace1);
        Assert.assertNotNull(prefixService.getNamespace(prefix));
        Assert.assertEquals(namespace1, prefixService.getNamespace(prefix));
        Assert.assertEquals(prefix, prefixService.getPrefix(namespace1));
        
        prefixService.forceAdd(prefix, namespace2);
        Assert.assertNotNull(prefixService.getNamespace(prefix));
        Assert.assertEquals(namespace2, prefixService.getNamespace(prefix));
        Assert.assertEquals(prefix, prefixService.getPrefix(namespace2));        

    }

}
