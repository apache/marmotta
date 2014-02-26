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

package org.apache.marmotta.loader.core.test;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.marmotta.loader.api.LoaderHandler;
import org.apache.marmotta.loader.api.LoaderOptions;
import org.apache.marmotta.loader.core.MarmottaLoader;
import org.apache.marmotta.loader.core.test.dummy.DummyLoaderBackend;
import org.apache.marmotta.loader.core.test.dummy.DummyLoaderHandler;
import org.apache.marmotta.loader.wrapper.LoaderHandlerWrapper;
import org.junit.*;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class LoaderTestBase {

    protected static Path tempDir;

    private static Logger log = LoggerFactory.getLogger(LoaderTestBase.class);

    protected Configuration cfg;

    public LoaderTestBase() {
        cfg = new MapConfiguration(new HashMap<String,Object>());
    }

    @BeforeClass
    public static void setup() throws IOException {
        tempDir = Files.createTempDirectory("loader");

        log.info("running loader tests from temporary directory {}", tempDir);

        for(String filename : new String[] {"demo-data.rdf", "demo-data.rdf.gz", "demo-data.rdf.bz2", "demo-data.tar.gz", "demo-data.zip", "demo-data.7z"}) {
            File data = new File(tempDir.toFile(), filename);
            FileUtils.copyInputStreamToFile(ArchiveTest.class.getResourceAsStream("/" + filename), data);
        }
    }

    @AfterClass
    public static void teardown() throws IOException {
        log.info("cleaning up temporary directory {}", tempDir);

        FileUtils.deleteDirectory(tempDir.toFile());
    }

    @Test
    public void testAutoLoad() throws RDFHandlerException {
        log.info("testing automatic loading ...");

        MarmottaLoader loader = new MarmottaLoader(cfg);
        DummyLoaderHandler handler = getBase(loader.load());

        testData(handler.getModel());
    }

    @Test
    public void testStatistics() throws RDFHandlerException {
        log.info("testing statistics loading ...");

        cfg.setProperty(LoaderOptions.STATISTICS_ENABLED, true);
        cfg.setProperty(LoaderOptions.STATISTICS_GRAPH, new File(tempDir.toFile(), "stats.png").toString());
        cfg.setProperty(DummyLoaderBackend.METHOD_SLEEP_MILLIS, 10);

        MarmottaLoader loader = new MarmottaLoader(cfg);
        DummyLoaderHandler handler = getBase(loader.load());

        testData(handler.getModel());
    }

    @Test
    public void testContext() throws RDFHandlerException {
        log.info("testing statistics loading ...");

        cfg.setProperty(LoaderOptions.CONTEXT, "http://localhost/contexts/mycontext");

        MarmottaLoader loader = new MarmottaLoader(cfg);
        DummyLoaderHandler handler = getBase(loader.load());

        testData(handler.getModel(), new URIImpl("http://localhost/contexts/mycontext"));
    }

    private void testData(Model model, URI... contexts) {
        Assert.assertTrue(model.size() > 0);

        URI s = new URIImpl("http://localhost:8080/LMF/resource/hans_meier");
        URI p = new URIImpl("http://xmlns.com/foaf/0.1/interest");
        URI o = new URIImpl("http://rdf.freebase.com/ns/en.software_engineering");

        Assert.assertTrue(model.contains(s,p,o));

        for(URI c : contexts) {
            Assert.assertTrue(model.contains(s,p,o,c));
        }
    }

    private DummyLoaderHandler getBase(LoaderHandler handler) {
        if(handler instanceof LoaderHandlerWrapper) {
            return getBase(((LoaderHandlerWrapper) handler).getHandlers()[0]);
        } else if(handler instanceof DummyLoaderHandler) {
            return (DummyLoaderHandler) handler;
        } else {
            throw new IllegalStateException("unknown loader type");
        }
    }
}
