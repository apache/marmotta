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
package org.apache.marmotta.platform.core.test.triplestore;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.jayway.restassured.RestAssured;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Some basic test for context service
 *
 * @author Sergio Fernández
 */
public class ContextServiceTest {

    private static JettyMarmotta marmotta;
    private static ConfigurationService configurationService;
    private static ContextService contextService;

    @BeforeClass
    public static void setUp() {
        marmotta = new JettyMarmotta("/marmotta");
        configurationService = marmotta.getService(ConfigurationService.class);
        contextService = marmotta.getService(ContextService.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();
    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testEmpty() {
        final List<URI> contexts = contextService.listContexts();
        Assert.assertEquals(1, contexts.size());
    }

    @Test
    public void testMarmotta631() {
        final List<URI> contexts = contextService.listContexts();
        Assert.assertTrue(contexts.size() >= 1);
        Assert.assertTrue(Collections2.transform(contexts, new Function<URI, String>() {
            @Override
            public String apply(URI input) {
                return input.stringValue();
            }
        }).contains(configurationService.getDefaultContext()));
    }

    @Test
    public void testMarmotta631AfterImporting() throws URISyntaxException, MarmottaImportException {
        final ImportService importService = marmotta.getService(ImportService.class);
        final InputStream is = ContextServiceTest.class.getResourceAsStream("/org/apache/marmotta/platform/core/test/sesame/demo-data.foaf");
        importService.importData(is, "application/rdf+xml", marmotta.getService(UserService.class).getAnonymousUser(), contextService.getDefaultContext());

        final List<URI> contexts = contextService.listContexts();
        Assert.assertTrue(contexts.size() >= 1);
        Assert.assertTrue(contexts.contains(contextService.getDefaultContext()));
    }

}
