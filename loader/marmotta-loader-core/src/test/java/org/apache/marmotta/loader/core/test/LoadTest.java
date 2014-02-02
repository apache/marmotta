package org.apache.marmotta.loader.core.test;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.marmotta.loader.api.LoaderHandler;
import org.apache.marmotta.loader.api.LoaderOptions;
import org.apache.marmotta.loader.core.MarmottaLoader;
import org.apache.marmotta.loader.core.test.dummy.DummyLoaderHandler;
import org.apache.marmotta.loader.wrapper.LoaderHandlerWrapper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(Parameterized.class)
public class LoadTest {

    private static Logger log = LoggerFactory.getLogger(LoadTest.class);

    private static Path tempDir;


    private String filename;
    private String compression;

    private Configuration cfg;

    public LoadTest(String compression, String filename) {
        this.filename    = filename;
        this.compression = compression;

        log.info("running test for file {} (compression: {})", filename, compression);

        cfg = new MapConfiguration(new HashMap<String,Object>());
        cfg.setProperty(LoaderOptions.FILES, Collections.singletonList(tempDir.toString() + File.separator + filename));
        cfg.setProperty(LoaderOptions.COMPRESSION, compression);
    }

    @BeforeClass
    public static void setup() throws IOException {
        tempDir = Files.createTempDirectory("loader");

        log.info("running loader tests from temporary directory {}", tempDir);

        for(String filename : new String[] {"demo-data.rdf", "demo-data.rdf.gz", "demo-data.rdf.bz2"}) {
            File data = new File(tempDir.toFile(), filename);
            FileUtils.copyInputStreamToFile(LoadTest.class.getResourceAsStream("/" + filename), data);
        }
    }

    @AfterClass
    public static void teardown() throws IOException {
        log.info("cleaning up temporary directory {}", tempDir);

        FileUtils.deleteDirectory(tempDir.toFile());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] { { null, "demo-data.rdf"}, { CompressorStreamFactory.GZIP, "demo-data.rdf.gz" }, { CompressorStreamFactory.BZIP2, "demo-data.rdf.bz2" } };
        return Arrays.asList(data);
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
