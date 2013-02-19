package kiwi.core.test.base;

import com.google.common.io.Files;
import kiwi.core.jndi.LMFInitialContextFactoryBuilder;
import kiwi.core.startup.LMFStartupService;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.FileUtils;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class AbstractLMF {

    protected static Logger log = LoggerFactory.getLogger(EmbeddedLMF.class);

    protected Weld weld;
    protected WeldContainer container;
    protected LMFStartupService startupService;
    protected Configuration override;

    protected File lmfHome;

    protected AbstractLMF() {
        // initialise JNDI environment
        try {
            NamingManager.setInitialContextFactoryBuilder(new LMFInitialContextFactoryBuilder());
        } catch (NamingException e) {

        } catch (IllegalStateException e) {
        }

        // initialise CDI environment
        weld = new Weld();
        container = weld.initialize();

        cleanJNDI();


        // put bean manager into JNDI
        try {
            new InitialContext().bind("java:comp/BeanManager",container.getBeanManager());
        } catch (NamingException e) {
            log.error("error adding bean manager to JNDI",e);
        }


        // create temporary LMF home directory
        lmfHome = Files.createTempDir();

        // create a temporary configuration with an in-memory database URL for h2
        override = new MapConfiguration(new HashMap<String,Object>());
        override.setProperty("database.h2.url","jdbc:h2:mem;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10");
        override.setProperty("logging.template","/logback-testing.xml");

        // initialise LMF using a temporary directory
        startupService = getService(LMFStartupService.class);
    }


    public <T> T getService(Class<T> serviceClass) {
        return container.instance().select(serviceClass).get();
    }


    public void shutdown() {
        // remove bean manager from JNDI
        cleanJNDI();

        startupService.shutdown();
        weld.shutdown();

        try {
            FileUtils.deleteDirectory(lmfHome);
        } catch (IOException e) {
            log.error("error while deleting temporary LMF home directory");
        }
    }


    private void cleanJNDI() {
        try {
            new InitialContext().unbind("java:comp/env/BeanManager");
        } catch (NamingException e) {
        }
        try {
            new InitialContext().unbind("java:comp/BeanManager");
        } catch (NamingException e) {
        }
        try {
            new InitialContext().unbind("java:app/BeanManager");
        } catch (NamingException e) {
        }

    }
}
