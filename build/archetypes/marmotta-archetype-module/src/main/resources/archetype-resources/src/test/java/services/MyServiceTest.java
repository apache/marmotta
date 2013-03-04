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
package ${package}.services;

import junit.framework.Assert;
import kiwi.core.test.base.EmbeddedLMF;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ${package}.api.MyService;

public class MyServiceTest {

    private static EmbeddedLMF lmf;
    private static MyService myService;

    @BeforeClass
    public static void setUp() {
        lmf = new EmbeddedLMF();
        myService = lmf.getService(MyService.class);
    }

    @Test
    public void testDoThis() {

    }

    @Test
    public void testDoThat() {

    }

    @Test
    public void testHelloWorld() {
        Assert.assertEquals("Hello You", myService.helloWorld("You"));
        Assert.assertEquals("Hello Steve", myService.helloWorld("Steve"));
        Assert.assertEquals("Hello Tom", myService.helloWorld("Tom"));
        Assert.assertEquals("Hello Ron", myService.helloWorld("Ron"));
        Assert.assertEquals("Hello Wüterich", myService.helloWorld("Wüterich"));
    }

    @AfterClass
    public static void tearDown() {
        lmf.shutdown();
    }

}
