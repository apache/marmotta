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
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.logging.LoggingModule;
import org.apache.marmotta.platform.core.api.logging.LoggingService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.core.exception.MarmottaConfigurationException;
import org.apache.marmotta.platform.core.model.logging.ConsoleOutput;
import org.apache.marmotta.platform.core.model.logging.LogFileOutput;
import org.apache.marmotta.platform.core.model.logging.LoggingOutput;
import org.apache.marmotta.platform.core.model.logging.SyslogOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    private LoadingCache<String,LogFileOutput> logfileCache;
    private LoadingCache<String,SyslogOutput>  syslogCache;

    private ConsoleOutput consoleOutput;

    @PostConstruct
    public void initialize() {
        log.info("Apache Marmotta Logging Service starting up ...");

        for(String key : configurationService.listConfigurationKeys("logging.")) {
            String loggerName   = key.substring("logging.".length());
            setConfiguredLevel(loggerName);
        }

        logfileCache = CacheBuilder.newBuilder().maximumSize(10).expireAfterAccess(10, TimeUnit.MINUTES).build(
                new CacheLoader<String, LogFileOutput>() {
                    @Override
                    public LogFileOutput load(String key) throws Exception {

                        if(configurationService.isConfigurationSet("logging.file."+key+".name")) {
                            return new LogFileOutput(key, configurationService);
                        } else {
                            throw new MarmottaConfigurationException("logfile configuration "+key+" not found");
                        }
                    }
                }
        );

        syslogCache = CacheBuilder.newBuilder().maximumSize(5).expireAfterAccess(10, TimeUnit.MINUTES).build(
                new CacheLoader<String, SyslogOutput>() {
                    @Override
                    public SyslogOutput load(String key) throws Exception {
                        if(configurationService.isConfigurationSet("logging.syslog."+key+".name")) {
                            return new SyslogOutput(key, configurationService);
                        } else {
                            throw new MarmottaConfigurationException("syslog configuration "+key+" not found");
                        }
                    }
                }
        );

        consoleOutput = new ConsoleOutput(configurationService);
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


    private synchronized void reloadLoggingConfiguration() {
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
            rootLogger.setLevel(Level.TRACE);
        } else {
            rootLogger.setLevel(Level.INFO);
        }

        // set child logger levels from configuration file
        for(String key : configurationService.listConfigurationKeys("logging.")) {
            String loggerName   = key.substring("logging.".length());
            setConfiguredLevel(loggerName);
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


    /**
     * Create a new logfile output configuration using the given parameters; further options can be set on the object
     * itself.
     *
     * @param id   unique identifier for the log output configuration
     * @param name human-readable name for configuration (displayed in UI)
     * @param file filename under MARMOTTA_HOME/log
     * @return
     */
    @Override
    public LogFileOutput createLogFileOutput(String id, String name, String file) {
        try {
            return logfileCache.get(id);
        } catch (ExecutionException e) {
            LogFileOutput r = new LogFileOutput(id, configurationService);
            r.setFileName(file);
            r.setName(name);
            return r;
        }
    }

    /**
     * Return a list of all output configurations, reading directly from the configuration service.
     *
     * @return
     */
    @Override
    public List<LoggingOutput> listOutputConfigurations() {
        List<LoggingOutput> logs = new ArrayList<>();

        logs.addAll(
                Lists.transform(configurationService.listConfigurationKeys(Pattern.compile("logging\\.(file|syslog)\\.([^\\.]+)\\.name")), new Function<Matcher, LoggingOutput>() {
                    @Override
                    public LoggingOutput apply(java.util.regex.Matcher input) {
                        return getOutputConfiguration(input.group(2));
                    }
                }
                ));

        logs.add(consoleOutput);

        return logs;
    }

    /**
     * Return the output configuration with the given ID.
     *
     * @param id
     * @return
     */
    @Override
    public LoggingOutput getOutputConfiguration(String id) {
        if("console".equals(id)) {
            return consoleOutput;
        }

        try {
            return logfileCache.get(id);
        } catch (ExecutionException e) {
        }

        try {
            return syslogCache.get(id);
        } catch (ExecutionException e) {
        }

        return null;
    }

    /**
     * Create a new syslog output configuration using the given parameters; further options can be set on the object
     * itself. If not set, the default hostname is "localhost" and the default facility is "LOCAL0".
     *
     * @param id   unique identifier for the log output configuration
     * @param name human-readable name for configuration (displayed in UI)
     * @return
     */
    @Override
    public SyslogOutput createSyslogOutput(String id, String name) {
        try {
            return syslogCache.get(id);
        } catch (ExecutionException e) {
            SyslogOutput r = new SyslogOutput(id,configurationService);
            r.setName(name);
            return r;
        }
    }

    /**
     * Return the console output configuration used by Marmotta. There is only a single console output for any
     * Marmotta instance.
     *
     * @return
     */
    @Override
    public ConsoleOutput getConsoleOutput() {
        return consoleOutput;
    }

    /**
     * Return a list of all modules found on the classpath. This list is assembled via CDI injection. Since modules are
     * managed beans, calling setters on the LoggingModule implementations will directly change the configuration.
     *
     * @return
     */
    @Override
    public List<LoggingModule> listModules() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
