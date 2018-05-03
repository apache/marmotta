/*
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
package org.apache.marmotta.ldclient.test.mediawiki;

import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Test;

/**
 * Test accessing  mediawiki resources
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class TestMediawikiProvider extends ProviderTestBase {


    /**
     * This method tests accessing the mediawiki resource.
     *
     * @throws Exception
     */
    @Test
    public void testArticle() throws Exception {
        testResource("https://en.wikipedia.org/wiki/Marmot", "wikipedia-marmot.sparql");
    }

    @Test
    public void testArticleHttp() throws Exception {
        testResource("http://en.wikipedia.org/wiki/Marmot", "wikipedia-marmot-http.sparql");
    }
}
