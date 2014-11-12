/**
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
package org.apache.marmotta.platform.core.test.base;

import com.google.common.io.Files;
import org.apache.marmotta.platform.core.jndi.MarmottaInitialContextFactoryBuilder;
import org.apache.marmotta.platform.core.startup.MarmottaStartupService;
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
 * Abstract Marmotta
 *
 * @author Sebastian Schaffert
 */
public abstract class AbstractMarmotta {

    protected static Logger log = LoggerFactory.getLogger(AbstractMarmotta.class);

    protected Weld weld;
    protected WeldContainer container;
    protected MarmottaStartupService startupService;
    protected Configuration override;

    protected File home;

    protected AbstractMarmotta() {

        // initialise JNDI environment
        try {
            NamingManager.setInitialContextFactoryBuilder(new MarmottaInitialContextFactoryBuilder());
        } catch (NamingException e) {

        } catch (IllegalStateException e) {
        }

        // initialise CDI environment
        weld = new Weld();
        container = weld.initialize();

        cleanJNDI();


        // put bean manager into JNDI
        try {
            new InitialContext().bind("java:comp/BeanManager", container.getBeanManager());
        } catch (NamingException e) {
            log.error("error adding bean manager to JNDI", e);
        }

        // create temporary marmotta home directory
        home = Files.createTempDir();

        // create a temporary configuration with an in-memory database URL for H2
        override = new MapConfiguration(new HashMap<String,Object>());
        override.setProperty("database.h2.url","jdbc:h2:mem;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10");
        override.setProperty("logging.template", "/logback-testing.xml");
        override.setProperty("testing.enabled", true);

        // initialise marmotta using a temporary directory
        startupService = getService(MarmottaStartupService.class);
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
            FileUtils.deleteDirectory(home);
        } catch (IOException e) {
            log.error("error while deleting temporary Marmotta home directory");
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
