package org.apache.marmotta.platform.core.test.embedded;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.test.base.EmbeddedLMF;
import org.junit.Assert;
import org.junit.Test;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class EmbeddedLMFTest {

    @Test
    public void testLMFStartup() {
        EmbeddedLMF lmf = new EmbeddedLMF();

        ConfigurationService cs = lmf.getService(ConfigurationService.class);

        Assert.assertNotNull(cs);

        lmf.shutdown();
    }
}
