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
package org.apache.marmotta.platform.core.services.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.core.events.ConfigurationServiceInitEvent;
import org.apache.marmotta.platform.core.util.FallbackConfiguration;
import org.apache.commons.configuration.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.ServletContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

/**
 * This service offers access to the system configuration of the LMF and takes care of initialising the system
 * properly on startup.
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 * 
 */
@ApplicationScoped
public class ConfigurationServiceImpl implements ConfigurationService {

    private String lmfHome;

    private static Logger log = LoggerFactory.getLogger(ConfigurationService.class);

    private String  configFile;

    /**
     * The configuration wrapper, loads first the stored configuration and then all stored configuration options
     */
    private CompositeConfiguration config;

    /**
     * The backend for storing configuration values, usually ${marmotta.home}/system-config.properties
     */
    private Configuration saveConfiguration;

    /**
     * The metadata about the configuration options
     */
    private CompositeConfiguration configDescriptions;

    private HashMap<String, Boolean> runtimeFlags;

    @Inject
    @Any
    private Event<ConfigurationChangedEvent> configurationEvent;

    @Inject
    @Any
    private Event<ConfigurationServiceInitEvent> configurationInitEvent;

    private int serverPort        = 0;
    //private String serverContext;
    private String serverName     = null;

    private boolean                          initialising;
    private boolean                          initialised = false;

    private ServletContext                   servletContext;

    public ConfigurationServiceImpl() {
        runtimeFlags = new HashMap<String, Boolean>();
    }

    /**
     * Initialise the ConfigurationService. Takes the marmotta.home system property from the servlet
     * init parameters
     * (web.xml) as bootstrap home. In case a system-config.properties file is found in this
     * directory, the
     * configuration is then loaded from this file. In case a system-config.properties file does not
     * yet exist,
     * the method loads bootstrap settings from the resource default-config.properties in the
     * classpath (in the
     * standard bundling, the file is in the kiwi-core-XXX.jar archive). After loading the
     * configuration, the
     * service initialises the plugin subsystem and the SOLR home directory by unpacking the default
     * configuration
     * if the SOLR configuration does not yet exist.
     * @param override
     */
    @Override
    public void initialize(String lmfHome, Configuration override) {
        initialising = true;

        log.info("Apache Marmotta Configuration Service starting up ...");

        if(isTomcat7()) {
            log.info("Apache Marmotta running on Apache Tomcat 7.x");
        } else if(isTomcat6()) {
            log.info("Apache Marmotta running on Apache Tomcat <= 6.x");
        } else if(isJetty7()) {
            log.info("Apache Marmotta running on Jetty 7.x");
        } else if(isJetty6()) {
            log.info("Apache Marmotta running on Jetty <= 6.x");
        } else {
            log.info("Apache Marmotta running on an unknown servlet container");
        }


        setLMFHome(lmfHome);

        if (getLMFHome() != null) {
            File f1 = new File(getLMFHome());
            if (!f1.exists()) {
                f1.mkdirs();
            }
            // ensure directory for user configuration files
            File f2 = new File(getLMFHome() + File.separator + "config");
            if(!f2.exists()) {
                f2.mkdirs();
            }

            // ensure directory for logging messages
            File f3 = new File(getLMFHome() + File.separator + "log");
            if(!f3.exists()) {
                f3.mkdirs();
            }
        }

        // the save configuration will be in the  home directory
        try {
            if (getLMFHome() != null) {
                configFile = getLMFHome() + File.separator + "system-config.properties";
                File f = new File(configFile);
                if (!f.exists()) {
                    log.info("creating system configuration in configuration file {}", f.getAbsolutePath());
                } else {
                    log.info("reading system configuration from existing configuration file {}", f.getAbsolutePath());
                }
                saveConfiguration = new PropertiesConfiguration(f);
            } else {
                log.error("error while initialising configuration: no marmotta.home property given; creating memory-only configuration");
                saveConfiguration = new MapConfiguration(new HashMap<String, Object>());
            }
        } catch (Exception e) {
            log.error("error while initialising configuration file {}: {}; creating memory-only configuration", configFile, e.getMessage());
            saveConfiguration = new MapConfiguration(new HashMap<String, Object>());
        }
        config = new FallbackConfiguration();
        config.addConfiguration(saveConfiguration,true);

        // load all default-config.properties

        try {
            Enumeration<URL> configs = this.getClass().getClassLoader().getResources("config-defaults.properties");
            while(configs.hasMoreElements()) {
                URL url = configs.nextElement();
                config.addConfiguration(new PropertiesConfiguration(url));
            }

        } catch (IOException e) {
            log.error("I/O error while loading default configurations",e);
        } catch (ConfigurationException e) {
            log.error("configuration error while loading default configurations",e);
        }

        // legacy support (to be removed)
        try {
            Enumeration<URL> configs = this.getClass().getClassLoader().getResources("default-config.properties");
            while(configs.hasMoreElements()) {
                URL url = configs.nextElement();
                config.addConfiguration(new PropertiesConfiguration(url));
                log.warn("found legacy configuration file {}; should be replaced with per-module configuration!",url);
            }

        } catch (IOException e) {
            log.error("I/O error while loading default configurations",e);
        } catch (ConfigurationException e) {
            log.error("configuration error while loading default configurations",e);
        }

        configDescriptions = new CompositeConfiguration();
        try {
            Enumeration<URL> configs = this.getClass().getClassLoader().getResources("config-descriptions.properties");
            while(configs.hasMoreElements()) {
                URL url = configs.nextElement();
                configDescriptions.addConfiguration(new PropertiesConfiguration(url));
            }

        } catch (IOException e) {
            log.error("I/O error while loading configuration descriptions",e);
        } catch (ConfigurationException e) {
            log.error("configuration error while loading configuration descriptions",e);
        }


        // setup KiWi home - if it is given as system property, the bootstrap configuration is
        // overwritten
        if (getLMFHome() != null) {
            config.setProperty("marmotta.home", getLMFHome());
            config.setProperty("solr.home", getLMFHome() + File.separator + "solr");
        }


        // in case override configuration is given, change all settings in the configuration accordingly
        if(override != null) {
            for (Iterator<String> it = override.getKeys(); it.hasNext();) {
                String key = it.next();

                config.setProperty(key, override.getProperty(key));
            }
        }

        save();

        // configuration service is now ready to use
        initialised = true;


        // this should maybe move to the KiWiPreStartupFilter ...
        initLoggingConfiguration();
        initDatabaseConfiguration();

        save();

        log.info("Apache Marmotta Configuration Service: initialisation completed");

        configurationInitEvent.fire(new ConfigurationServiceInitEvent());

        initialising = false;

    }

    /**
     * Initialise the Apache Marmotta Logging Configuration.
     * <ul>
     * <li>if the logback.xml file does not yet exist, create it based on the logback-template.xml resource</li>
     * <li>reiniaialise the logging framework using the logback.xml file from Apache Marmotta Home, overwriting the basic logging configured on startup</li>
     * <li>in case debug.enabled = true, set the root logger level to debug, otherwise set it to info</li>
     * </ul>
     */
    private void initLoggingConfiguration() {
        File log_configuration = new File(getWorkDir() + File.separator + "logback.xml");

        if(!log_configuration.exists()) {
            // create new logging configuration from template
            URL url_template = ConfigurationService.class.getResource(config.getString("logging.template", "/logback-template.xml"));

            if (url_template != null) {
                try {
                    Files.copy(Resources.newInputStreamSupplier(url_template), log_configuration);
                } catch (IOException e) {
                    log.error("could not create logging configuration; reverting to bootstrap logging", e);
                }
            } else {
                log.error("could not find logging template; reverting to bootstrap logging");
            }
        }

        log.warn("LOGGING: Switching to Apache Marmotta logging configuration; further output will be found in {}/log/*.log", getWorkDir());
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
        if(getBooleanConfiguration("debug.enabled",false)) {
            rootLogger.setLevel(Level.DEBUG);
        } else {
            rootLogger.setLevel(Level.INFO);
        }

    }

    private void initDatabaseConfiguration() {
        if (!config.getBoolean("kiwi.setup.database")) {
            log.info("SETUP: Setting up initial Apache Marmotta database configuration ...");
            String db_type = config.getString("database.type", "h2");
            config.setProperty("database.h2.url", "jdbc:h2:" + getWorkDir() + "/db/lmf;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10");
            if (db_type.equals("h2")) {
                config.setProperty("database.url", "jdbc:h2:" + getWorkDir() + "/db/lmf;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10");
                config.setProperty("database.user", "sa");
                config.setProperty("database.password", "sa");
                config.setProperty("database.mode", "create");
            }
            config.setProperty("kiwi.setup.database", true);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("shutting down configuration service");

        //        if (SystemTray.isSupported() && icon != null) {
        //
        //            SystemTray tray = SystemTray.getSystemTray();
        //            tray.remove(icon);
        //        }
    }

    /**
     * Pass the servlet context over to the configuration service to provide runtime information about
     * the environment to the rest of the system.
     *
     * @param context
     */
    @Override
    public void setServletContext(ServletContext context) {
        this.servletContext = context;
    }


    /**
     * Get the servlet context used when initialising the system
     *
     * @return
     */
    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Get the base URI out of the current request. The base URI
     * is used e.g. to generate URIs of internal content items
     * 
     * @see org.apache.marmotta.platform.core.api.config.ConfigurationService#getBaseUri()
     */

    @Override
    public String getBaseUri() {
        return getStringConfiguration("kiwi.context");
    }

    /**
     * Get the base path of the system, which is the relative path from the server host.
     * For example, in the case of http://localhost:8080/LMF, /LMF would be returned as the path.
     * 
     * @return a String representing the path
     */
    @Override
    public String getPath() {
        return getStringConfiguration("kiwi.path");
    }

    /**
     * Get the server uri of the system, i.e. a uri that when entered in the browser accesses the
     * server that runs the KiWi (and SOLR) applications. Can be used to compute the paths of
     * other applications relative to the current application. Computed like the base uri.
     * 
     * @see ConfigurationService#getServerUri()
     */
    @Override
    public String getServerUri() {
        String serverUrl = getStringConfiguration("kiwi.host");

        if (serverUrl.endsWith("/"))
            return serverUrl;
        else
            return serverUrl + "/";
    }

    /**
     * List all configuration keys defined for the system configuration of KiWi.
     * 
     * @return
     */
    @Override
    public List<String> listConfigurationKeys() {
        List<String> keys = new LinkedList<String>();
        for (Iterator<String> it = config.getKeys(); it.hasNext();) {
            keys.add(it.next());
        }
        return keys;
    }

    /**
     * List all configuration keys defined for the system configuration of KiWi having prefix.
     * 
     * @param prefix the prefix of keys that should be returned
     * @return
     */
    @Override
    public List<String> listConfigurationKeys(String prefix) {
        List<String> keys = new LinkedList<String>();
        for (Iterator<String> it = config.getKeys(prefix); it.hasNext();) {
            keys.add(it.next());
        }
        return keys;
    }

    /*
     * (non-Javadoc)
     * 
     * @see kiwi.api.config.ConfigurationService#isConfigurationSet(java.lang.String)
     */
    @Override
    public boolean isConfigurationSet(String key) {
        return config.containsKey(key);
    }

    /**
     * Get the configuration for the given key. If there is no such configuration, a new one is
     * created with empty value (returns null).
     * 
     * @param key unique configuration key for lookup
     * @return a configuration object with either the configured value or null as value
     */
    @Override
    public Object getConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        return config.getProperty(key);
    }

    /**
     * Return the comment for the configuration with the given key as string. If there is no such
     * configuration, null is returned
     * 
     * @param key unique configuration key for lookup
     * @return a string describing the configuration option or null if no comment was given
     */
    @Override
    public String getComment(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        return configDescriptions.getString(key + ".description");

    }

    /**
     * Return the type for the configuration with the given key as string. If there is no such
     * configuration, null is returned
     * @param key unique configuration key for lookup
     * @return  a string describing the type for key or DEFAULT_TYPE (String) if no type was given
     */
    @Override
    public String getType(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        String s = configDescriptions.getString(key+".type");
        return s!=null?s: String.class.getName();
    }

    /**
     * Set the configuration "key" to the string value "value".
     * 
     * @param key
     * @param value
     */
    @Override
    public void setConfiguration(String key, Object value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        config.setProperty(key, value);
        save();

        if (!initialising) {
            configurationEvent.fire(new ConfigurationChangedEvent(key));
        }
    }

    /**
     * Set type for a configuration key
     *
     * @param key key for configuration fields
     * @param type type for configuratino field
     */
    @Override
    public void setType(String key, String type) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        configDescriptions.setProperty(key+".type",type);
        save();
    }

    /**
     * Set type for a configuration key
     *
     * @param key key for configuration fields
     * @param description type for configuratino field
     */
    @Override
    public void setComment(String key, String description) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        configDescriptions.setProperty(key+".description",description);
        save();
    }

    /**
     *
     *
     */
    @Override
    public String getStringConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        if (config instanceof AbstractConfiguration) {
            ((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
        }
        String result = config.getString(key);
        if (config instanceof AbstractConfiguration) {
            ((AbstractConfiguration) config).setDelimiterParsingDisabled(false);
        }
        return result;
    }

    @Override
    public String getStringConfiguration(String key, String defaultValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        if (config instanceof AbstractConfiguration) {
            ((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
        }
        String result = config.getString(key, defaultValue);
        if (config instanceof AbstractConfiguration) {
            ((AbstractConfiguration) config).setDelimiterParsingDisabled(false);
        }
        return result;
    }

    @Override
    public double getDoubleConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        return config.getDouble(key, 0.0);
    }

    @Override
    public double getDoubleConfiguration(String key, double defaultValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        return config.getDouble(key, defaultValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see kiwi.api.config.ConfigurationService#setDoubleConfiguration(java.lang.String, double)
     */
    @Override
    public void setDoubleConfiguration(String key, double value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        config.setProperty(key, value);
        save();

        if (!initialising) {
            configurationEvent.fire(new ConfigurationChangedEvent(key));
        }
    }

    @Override
    public int getIntConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised, "ConfigurationService not yet initialised; call initialise() manually");

        return config.getInt(key, 0);
    }

    @Override
    public int getIntConfiguration(String key, int defaultValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        return config.getInt(key, defaultValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see kiwi.api.config.ConfigurationService#setIntConfiguration(java.lang.String, int)
     */
    @Override
    public void setIntConfiguration(String key, int value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        config.setProperty(key, value);
        save();

        if (!initialising) {
            configurationEvent.fire(new ConfigurationChangedEvent(key));
        }
    }


    /**
     * Get the configuration for the given key. If there is no such configuration, 0 is returned
     *
     * @param key unique configuration key for lookup
     * @return a int value with either the configured value or 0
     */
    @Override
    public long getLongConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised, "ConfigurationService not yet initialised; call initialise() manually");

        return config.getLong(key, 0);
    }

    /**
     * Get the configuration for the given key. If there is no such configuration, a new one is
     * created using the provided defaultValue as double value.
     *
     * @param key          unique configuration key for lookup
     * @param defaultValue default value if configuration not found
     * @return a configuration object with either the configured value or defaultValue
     */
    @Override
    public long getLongConfiguration(String key, long defaultValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        return config.getLong(key, defaultValue);
    }

    /**
     * Set the system configuration with the given key to the given int value.
     *
     * @param key
     * @param value
     */
    @Override
    public void setLongConfiguration(String key, long value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        config.setProperty(key, value);
        save();

        if (!initialising) {
            configurationEvent.fire(new ConfigurationChangedEvent(key));
        }
    }

    @Override
    public boolean getBooleanConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        return config.getBoolean(key, false);
    }

    @Override
    public boolean getBooleanConfiguration(String key, boolean defaultValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        return config.getBoolean(key, defaultValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see kiwi.api.config.ConfigurationService#setIntConfiguration(java.lang.String, int)
     */
    @Override
    public void setBooleanConfiguration(String key, boolean value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        config.setProperty(key, value);
        save();

        if (!initialising) {
            configurationEvent.fire(new ConfigurationChangedEvent(key));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see kiwi.api.config.ConfigurationService#getListConfiguration(java.lang.String)
     */
    @Override
    public Properties getPropertiesConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        return config.getProperties(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see kiwi.api.config.ConfigurationService#getListConfiguration(java.lang.String)
     */
    @Override
    public List<String> getListConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        String[] result = config.getStringArray(key);

        if(result.length == 1 && "".equals(result[0].trim())) return Collections.emptyList();

        return Lists.newArrayList(result);
    }

    /*
     * (non-Javadoc)
     * 
     * @see kiwi.api.config.ConfigurationService#getListConfiguration(java.lang.String,
     * java.util.List)
     */
    @Override
    public List<String> getListConfiguration(String key, List<String> defaultValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        if (config.containsKey(key)) {
            String[] values = config.getStringArray(key);
            if(values.length > 0) return Lists.newArrayList(values);
            else
                return Lists.newArrayList();
        } else
            return defaultValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see kiwi.api.config.ConfigurationService#setListConfiguration(java.lang.String,
     * java.util.List)
     */
    @Override
    public void setListConfiguration(String key, List<String> value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        config.setProperty(key, value);
        save();

        if (!initialising) {
            configurationEvent.fire(new ConfigurationChangedEvent(key));
        }
    }

    @Override
    public void removeConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        config.clearProperty(key);
    }

    @Override
    public void setConfiguration(String key, String value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        if (config instanceof AbstractConfiguration) {
            ((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
        }
        config.setProperty(key, value);
        if (config instanceof AbstractConfiguration) {
            ((AbstractConfiguration) config).setDelimiterParsingDisabled(false);
        }
        save();

        if (!initialising) {
            configurationEvent.fire(new ConfigurationChangedEvent(key));
        }
    }

    /**
     * Set a configuration value without firing an event. This is in rare cases needed to avoid
     * propagation of events.
     *
     * @param key
     * @param value
     */
    @Override
    public void setConfigurationWithoutEvent(String key, Object value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        if (config instanceof AbstractConfiguration) {
            ((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
        }
        config.setProperty(key, value);
        if (config instanceof AbstractConfiguration) {
            ((AbstractConfiguration) config).setDelimiterParsingDisabled(false);
        }
        save();
    }


    @Override
    public void setConfigurations(Map<String, ?> values) {
        Preconditions.checkNotNull(values);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        if (config instanceof AbstractConfiguration) {
            ((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
        }
        for (Map.Entry<String,?> entry : values.entrySet()) {
            config.setProperty(entry.getKey(), entry.getValue());
        }
        if (config instanceof AbstractConfiguration) {
            ((AbstractConfiguration) config).setDelimiterParsingDisabled(false);
        }
        save();

        if (!initialising) {
            configurationEvent.fire(new ConfigurationChangedEvent(values.keySet()));
        }
    }

    @Override
    public void setConfiguration(String key, List<String> values) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        config.setProperty(key, values);
        save();

        if (!initialising) {
            configurationEvent.fire(new ConfigurationChangedEvent(key));
        }
    }

    /**
     * The work directory for marmotta, where all applications will create their own subdirectories
     */

    @Override
    public String getWorkDir() {
        String value = getStringConfiguration("marmotta.home");
        if (StringUtils.isBlank(value)) {
        	log.warn("property 'marmotta.home' not given, trying with the old 'lmf.home'...");
        	value = getStringConfiguration("lmf.home");
        }
        if (StringUtils.isBlank(value)) {
        	log.warn("property 'lmf.home' not given neither, trying with the pretty old 'kiwi.home'...");
        	value = getStringConfiguration("kiwi.home");
        }
        if (StringUtils.isBlank(value)) {
        	value =  new File(System.getProperty("java.io.tmpdir", "/tmp"), "marmotta").getAbsolutePath();
        	log.warn("no property found pointing a home, so creating it in the temporal one: " + value);
        }
        return value;
    }

    public void save() {
        if(saveConfiguration instanceof PropertiesConfiguration) {
            try {
                ((PropertiesConfiguration)saveConfiguration).save();
            } catch (ConfigurationException e) {
                log.error("could not save system configuration: #0", e.getMessage());
            }
        }
    }

    /**
     * Return the value of the runtime flag passed as argument.
     * 
     * @param flag
     * @return
     */
    @Override
    public boolean getRuntimeFlag(String flag) {
        if (runtimeFlags.get(flag) != null)
            return runtimeFlags.get(flag);
        else
            return false;
    }

    /**
     * Set a flag at runtime that is discarded on system shutdown; used e.g. to indicate that
     * certain
     * processes have already been carried out.
     * 
     * @param value
     */
    @Override
    public void setRuntimeFlag(String flag, boolean value) {
        runtimeFlags.put(flag, value);
    }

    /**
     * Set the LMF_HOME value to the correct path. Used during the initialisation process.
     * 
     * @param home
     */
    @Override
    public void setLMFHome(String home) {
        this.lmfHome = home;
    }

    /**
     * Return the value of the LMF_HOME setting. Used during the initialisation process.
     * 
     * @return
     */
    @Override
    public String getLMFHome() {
        return lmfHome;
    }

    /**
     * Get the base context URI
     * 
     * @return base context
     */
    @Override
    public String getBaseContext() {
        return getBaseUri() + CONTEXT_PATH + "/";
    }

    /**
     * Return the context used for storing system information.
     *
     * @return a KiWiUriResource representing the system knowledge space
     */
    @Override
    public String getSystemContext() {
        return getBaseUri() + CONTEXT_PATH + CONTEXT_SYSTEM;
    }

    /**
     * Get the uri of the inferred context
     *
     * @return uri of this inferred context
     */
    @Override
    public String getInferredContext() {
        return getBaseUri() + CONTEXT_PATH + "/" + CONTEXT_INFERRED;
    }

    /**
     * Get the uri of the default context
     *
     * @return
     */
    @Override
    public String getDefaultContext() {
        return getBaseUri() + CONTEXT_PATH + "/" + CONTEXT_DEFAULT;
    }

    /**
     * Get the uri of the context used for caching linked data
     *
     * @return
     */
    @Override
    public String getCacheContext() {
        return getBaseUri() + CONTEXT_PATH + "/" + CONTEXT_CACHE;
    }

    @Override
    public String getEnhancerContex() {
        return getBaseUri() + CONTEXT_PATH + "/" + CONTEXT_ENHANCEMENT;
    }

    /**
     * Get a string describing the type and version of the application server running the LMF.
     *
     * @return
     */
    @Override
    public String getServerInfo() {
        if(isTomcat7())
            return "Apache Tomcat 7.x";
        else if(isTomcat6())
            return "Apache Tomcat <= 6.x";
        else if(isJetty7())
            return "Jetty 7.x";
        else if(isJetty6()) return "Jetty 6.x";
        else
            return "Unknown Servlet Container";

    }


    /**
     * Try figuring out on which port the server is running ...
     */
    @Override
    public int getServerPort() {

        if(serverPort == 0) {

            if(isTomcat6()) {
                // tomcat <= 6.x
                try {
                    Object server = Class.forName("org.apache.catalina.ServerFactory").getMethod("getServer").invoke(null);
                    Object service = Array.get(server.getClass().getMethod("findServices").invoke(server),0);
                    Object connector = Array.get(service.getClass().getMethod("findConnectors").invoke(service),0);

                    int port = (Integer)connector.getClass().getMethod("getPort").invoke(connector);
                    log.info("Tomcat <= 6.x detected, server port: {}",port);
                    serverPort = port;
                } catch (Exception e) {
                }
            } else if(isTomcat7()) {
                // tomcat 7.x
                try {
                    MBeanServer mBeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
                    ObjectName name = new ObjectName("Catalina", "type", "Server");
                    Object server = mBeanServer.getAttribute(name, "managedResource");

                    Object service = Array.get(server.getClass().getMethod("findServices").invoke(server),0);
                    Object connector = Array.get(service.getClass().getMethod("findConnectors").invoke(service),0);

                    int port = (Integer)connector.getClass().getMethod("getPort").invoke(connector);
                    log.info("Tomcat 7.x detected, server port: {}",port);
                    serverPort = port;
                } catch (Exception e) {
                }
            } else {

                log.warn("not running on Tomcat, could not determine server port, returning default of 8080");
                serverPort = 8080;
            }
        }

        return serverPort;

    }

    /**
     * Try figuring out the local name of the server
     * @return
     */
    @Override
    public String getServerName() {
        if(serverName == null) {
            try {
                serverName = java.net.InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                serverName = "localhost";
            }
        }
        return serverName;

    }

    /**
     * Return the context path of this application
     * @return
     */
    @Override
    public String getServerContext() {
        return servletContext.getContextPath();
    }


    /**
     * Shutdown the application server running this web application; tries to determine the kind of server we are
     * running under and send the proper shutdown signal before exiting with System.exit
     */
    @Override
    public void performServerShutdown() {
        try{
            MBeanServer server = (MBeanServer)Class.forName("org.apache.catalina.mbeans.MBeanUtils").getMethod("createServer").invoke(null);
            ObjectName name;
            if(isTomcat6()) {
                // Tomcat 6.x
                name = new ObjectName("Catalina:type=Service,serviceName=Catalina");
                server.invoke(name, "stop", new Object[0], new String[0]);
                log.warn("shutting down Apache Tomcat server on user request");
            } else if(isTomcat7()) {
                // Tomcat 7.x
                name = new ObjectName("Catalina", "type", "Service");
                server.invoke(name, "stop", new Object[0], new String[0]);
                log.warn("shutting down Apache Tomcat server on user request");
            }
        } catch (Exception ex) {
            log.error("shutting down other servers than Apache Tomcat is not supported",ex);
        }

        // ensure complete shutdown
        System.exit(0);

    }

    /**
     * Return true if Jetty 6.x is detected; tests for presence of class org.mortbay.jetty.Server
     * @return
     */
    @Override
    public boolean isJetty6() {
        try {
            Class.forName("org.mortbay.jetty.Server");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    /**
     * Return true if Jetty 7.x is detected; tests for presence of class org.eclipse.jetty.server.Server
     * @return
     */
    @Override
    public boolean isJetty7() {
        try {
            Class.forName("org.eclipse.jetty.server.Server");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    /**
     * Return true if Tomcat 6.x is detected; tests for presence of class org.apache.catalina.ServerFactory
     * @return
     */
    @Override
    public boolean isTomcat6() {
        try {
            Class.forName("org.apache.catalina.ServerFactory");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    /**
     * Return true if Tomcat 7.x is detected; tests for presence of class org.apache.catalina.CatalinaFactory
     * @return
     */
    @Override
    public boolean isTomcat7() {
        try {
            Class.forName("org.apache.catalina.CatalinaFactory");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
