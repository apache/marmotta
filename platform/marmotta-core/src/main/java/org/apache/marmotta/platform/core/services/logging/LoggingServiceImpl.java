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
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.logging.LoggingModule;
import org.apache.marmotta.platform.core.api.logging.LoggingService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.core.events.LoggingStartEvent;
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
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.io.File;
import java.text.Collator;
import java.util.*;
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


    @Inject
    private Instance<LoggingModule> loggingModules;


    private LoadingCache<String,LogFileOutput> logfileCache;
    private LoadingCache<String,SyslogOutput>  syslogCache;

    private ConsoleOutput consoleOutput;

    private LoggerContext loggerContext;

    private Map<LoggingOutput, Appender<ILoggingEvent>> appenders;

    @PostConstruct
    public void initialize() {
        log.info("Apache Marmotta Logging Service starting up ...");

        appenders = new HashMap<>();

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

        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        log.info("- configured logging modules: {}", StringUtils.join(Iterables.transform(listModules(), new Function<LoggingModule, Object>() {
            @Override
            public Object apply(LoggingModule input) {
                return input.getName();
            }
        }), ", "));

        log.info("- configured logging appenders: {}", StringUtils.join(Iterables.transform(listOutputConfigurations(), new Function<LoggingOutput, Object>() {
            @Override
            public Object apply(LoggingOutput input) {
                return input.getName();
            }
        }),", "));

    }

    public void startEventHandler(@Observes LoggingStartEvent event) {
        if(!isTestEnvironment()) {
            log.warn("LOGGING: Switching to Apache Marmotta logging configuration; further output will be found in {}{}log{}*.log", configurationService.getHome(), File.separator, File.separator);
            configureLoggers();
        }
    }

    public void configurationEventHandler(@Observes ConfigurationChangedEvent event) {
        if(!isTestEnvironment()) {
            if (event.containsChangedKeyWithPrefix("logging.")) {
                log.warn("LOGGING: Reloading logging configuration");
                configureLoggers();
            }
        }
    }

    private boolean isTestEnvironment() {
        //TODO: Thread.currentThread().getContextClassLoader().getResource("/logback-test.xml") != null
        //                                                    .getResource("/logback-testing.xml") != null
        return configurationService.getBooleanConfiguration("testing.enabled", false);
    }

    /**
     * Configure all loggers according to their configuration and set some reasonable fallback for the root level
     * (WARN to console, INFO to main logfile)
     */
    private void configureLoggers() {
        // remove all existing appenders
        loggerContext.reset();

        for(LoggingOutput output : listOutputConfigurations()) {
            configureOutput(output);
        }
        for(LoggingModule module : listModules()) {
            configureModule(module);
        }

        // configure defaults for root logger
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(appenders.get(getOutputConfiguration("console")));
        rootLogger.addAppender(appenders.get(getOutputConfiguration("main")));

    }

    /**
     * Configure output appenders using the configuration given as argument.
     *
     * @param output
     */
    private void configureOutput(LoggingOutput output) {
        Appender<ILoggingEvent> appender = appenders.get(output);

        // stop old existing appender
        if(appender != null) {
            appender.stop();
        }

        // create new appender based on configuration
        if(output instanceof ConsoleOutput) {
            appender = new ConsoleAppender<>();

            PatternLayoutEncoder pl = new PatternLayoutEncoder();
            pl.setContext(loggerContext);
            pl.setPattern(output.getPattern());
            pl.start();

            ((ConsoleAppender)appender).setEncoder(pl);

        } else if(output instanceof LogFileOutput) {
            String basePath = configurationService.getHome() + File.separator + "log" + File.separator;

            appender = new RollingFileAppender<>();
            ((RollingFileAppender)appender).setFile(basePath + ((LogFileOutput) output).getFileName());

            TimeBasedRollingPolicy policy = new TimeBasedRollingPolicy();
            policy.setContext(loggerContext);
            policy.setMaxHistory(((LogFileOutput) output).getKeepDays());
            policy.setFileNamePattern(basePath + ((LogFileOutput) output).getFileName() + ".%d{yyyy-MM-dd}.gz");
            policy.setParent((FileAppender) appender);
            policy.start();

            ((RollingFileAppender) appender).setRollingPolicy(policy);

            PatternLayoutEncoder pl = new PatternLayoutEncoder();
            pl.setContext(loggerContext);
            pl.setPattern(output.getPattern());
            pl.start();

            ((RollingFileAppender) appender).setEncoder(pl);

        } else if(output instanceof SyslogOutput) {
            appender = new SyslogAppender();
            ((SyslogAppender)appender).setSyslogHost(((SyslogOutput) output).getHostName());
            ((SyslogAppender)appender).setFacility(((SyslogOutput) output).getFacility());

            ((SyslogAppender) appender).setSuffixPattern(output.getPattern());
        } else {
            throw new IllegalArgumentException("unknown logging output type: "+output.getClass().getName());
        }

        appender.setContext(loggerContext);
        appender.setName(output.getId());

        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(output.getMaxLevel().toString());
        filter.setContext(loggerContext);
        filter.start();
        appender.addFilter(filter);

        appender.start();

        appenders.put(output,appender);

    }


    /**
     * Add the logging configuration for the given module. This method will create loggers for all packages
     * covered by the module and add the appenders configured for the module
     * @param module
     */
    private void configureModule(LoggingModule module) {
        for(String pkg : module.getPackages()) {
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger(pkg);
            logger.detachAndStopAllAppenders();
            logger.setAdditive(false);
            logger.setLevel(module.getCurrentLevel());

            for(LoggingOutput appender : module.getLoggingOutputs()) {
                logger.addAppender(appenders.get(appender));
            }
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
        List<LoggingModule> result = Lists.newArrayList(loggingModules);

        Collections.sort(result, new Comparator<LoggingModule>() {
            @Override
            public int compare(LoggingModule o1, LoggingModule o2) {
                return Collator.getInstance().compare(o1.getName(), o2.getName());
            }
        });

        return result;
    }
}
