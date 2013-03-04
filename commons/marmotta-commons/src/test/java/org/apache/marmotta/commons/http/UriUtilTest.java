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
package org.apache.marmotta.commons.http;

import static org.junit.Assert.assertEquals;

import org.apache.marmotta.commons.http.UriUtil;
import org.junit.Test;

/**
 * UriUtil tests
 * 
 * @author Sergio Fernández
 */
public class UriUtilTest {


    private static final String URI_2 = "http://localhost:8080/context/default";
    private static final String URI_1 = "http://rdfs.org/sioc/ns#Post";

    @Test
    public void testCommonUris() throws Exception {
        assertEquals("http://rdfs.org/sioc/ns#", UriUtil.getNamespace(URI_1));
        assertEquals("Post", UriUtil.getReference(URI_1));
        assertEquals("http://localhost:8080/context/", UriUtil.getNamespace(URI_2));
        assertEquals("default", UriUtil.getReference(URI_2));
    }

}
