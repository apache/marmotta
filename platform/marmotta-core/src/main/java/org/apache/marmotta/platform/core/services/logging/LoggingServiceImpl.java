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
package org.apache.marmotta.platform.core.services.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.logging.LoggingService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.io.File;

/**
 * LoggingServiceImpl
 *
 * @author Sebastian Schaffert
 *
 */
@ApplicationScoped
public class LoggingServiceImpl implements LoggingService {
    private static Logger log = LoggerFactory.getLogger(LoggingService.class);

    @Inject
    private ConfigurationService configurationService;

    @PostConstruct
    public void initialize() {
        log.info("Apache Marmotta Logging Service starting up ...");

        for(String key : configurationService.listConfigurationKeys("logging.")) {
            String loggerName   = key.substring("logging.".length());
            setConfiguredLevel(loggerName);
        }
    }

    public void configurationChangedEvent(@Observes ConfigurationChangedEvent event) {
        for (String key : event.getKeys())
            if (key.startsWith("logging.")) {
                String loggerName   = key.substring("logging.".length());
                setConfiguredLevel(loggerName);
            } else if(key.equalsIgnoreCase("debug.enabled")) {
                // set root logger level
                reloadLoggingConfiguration();
            }
    }


    private void reloadLoggingConfiguration() {
        log.info("reloading logging configuration");
        File log_configuration = new File(configurationService.getWorkDir() + File.separator + "logback.xml");
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(log_configuration);
        } catch (JoranException e) {
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }

        // set root logger level
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        if(configurationService.getBooleanConfiguration("debug.enabled",false)) {
            rootLogger.setLevel(Level.DEBUG);
        } else {
            rootLogger.setLevel(Level.INFO);
        }
    }



    private void setConfiguredLevel(String loggerName) {
        String logLevelName = configurationService.getStringConfiguration("logging."+loggerName,"INFO");
        Level logLevel = null;

        if("DEBUG".equals(logLevelName.toUpperCase())) {
            logLevel = Level.DEBUG;
        } else if("INFO".equals(logLevelName.toUpperCase())) {
            logLevel = Level.INFO;
        } else if("WARN".equals(logLevelName.toUpperCase())) {
            logLevel = Level.WARN;
        } else if("ERROR".equals(logLevelName.toUpperCase())) {
            logLevel = Level.ERROR;
        } else {
            log.error("unsupported log level for pattern {}: {}",loggerName,logLevelName);
        }

        if(logLevel != null) {
            ch.qos.logback.classic.Logger logger =
                    (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(loggerName);

            logger.setLevel(logLevel);
            log.info("configured logger {} to level {}",loggerName,logLevelName.toUpperCase());
        }
    }

    /**
     * Provide a logger to the injection point given as argument.
     * 
     * @see org.apache.marmotta.platform.core.api.logging.LoggingService#createLogger(javax.enterprise.inject.spi.InjectionPoint)
     */
    @Override
    @Produces
    public Logger createLogger(InjectionPoint injectionPoint) {
        return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass());
    }

}
