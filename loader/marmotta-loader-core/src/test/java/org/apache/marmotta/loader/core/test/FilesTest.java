package org.apache.marmotta.loader.core.test;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.marmotta.loader.api.LoaderOptions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
public class FilesTest extends LoaderTestBase {

    private static Logger log = LoggerFactory.getLogger(FilesTest.class);


    public FilesTest(String compression, String filename) {
        log.info("running test for file {} (compression: {})", filename, compression);

        cfg = new MapConfiguration(new HashMap<String,Object>());
        cfg.setProperty(LoaderOptions.FILES, Collections.singletonList(tempDir.toString() + File.separator + filename));
        cfg.setProperty(LoaderOptions.COMPRESSION, compression);
    }


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] { { null, "demo-data.rdf"}, { CompressorStreamFactory.GZIP, "demo-data.rdf.gz" }, { CompressorStreamFactory.BZIP2, "demo-data.rdf.bz2" } };
        return Arrays.asList(data);
    }

}
