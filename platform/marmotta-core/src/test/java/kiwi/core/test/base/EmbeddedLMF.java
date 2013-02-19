package kiwi.core.test.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An embedded version of the LMF. Provides support to startup and shutdown the CDI container and the LMF for test cases.
 * After the embedded LMF has been used, it should always be shutdown before being reused.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class EmbeddedLMF extends AbstractLMF {

    private static Logger log = LoggerFactory.getLogger(EmbeddedLMF.class);

    public EmbeddedLMF() {
        super();

        // initiate the first startup phase without a servlet context and with the override definition of the parent
        startupService.startupConfiguration(lmfHome.getAbsolutePath(),override,null);

        // initiate the second startup phase and pretend we are running at localhost
        startupService.startupHost("http://localhost/","http://localhost/");

        log.info("EmbeddedLMF created");
    }

}
