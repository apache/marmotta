package kiwi.core.test.embedded;

import kiwi.core.api.config.ConfigurationService;
import kiwi.core.test.base.EmbeddedLMF;
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
