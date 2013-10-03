package org.apache.marmotta.kiwi.loader;

import static org.junit.Assert.*;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.openrdf.repository.RepositoryException;

@RunWith(KiWiDatabaseRunner.class)
public class KiWiLoaderTest {

    private final KiWiConfiguration kiwiConfig;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    
    public KiWiLoaderTest(KiWiConfiguration kiwiConfig) {
        this.kiwiConfig = kiwiConfig;
    }

    @Before
    public void setUp() {
        // TODO: copy test file to temp folder
    }
    
    
    @Test
    @Ignore("Not yet implemented")
    public void testMain() {
    }

    @Test
    @Ignore("Not yet implemented")
    public void testLoadFile() throws RepositoryException {
        KiWiLoader loader = new KiWiLoader(kiwiConfig, "htto://example.com/test/", null);
        loader.initialize();
        
// FIXME: loader.load(file, format, gzip);
        
        loader.shutdown();
        fail("Not yet implemented");
    }
    
    @Test
    @Ignore("Not yet implemented")
    public void testLoadInputStream() throws RepositoryException {
        KiWiLoader loader = new KiWiLoader(kiwiConfig, "htto://example.com/test/", null);
        loader.initialize();
        
// FIXME: loader.load(is, format);
        
        loader.shutdown();
        fail("Not yet implemented");
    }
    
    

}
