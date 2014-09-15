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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.*;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.core.events.ConfigurationServiceInitEvent;
import org.apache.marmotta.platform.core.events.LoggingStartEvent;
import org.apache.marmotta.platform.core.model.config.CoreOptions;
import org.apache.marmotta.platform.core.util.FallbackConfiguration;
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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private String home;

    private static Logger log = LoggerFactory.getLogger(ConfigurationService.class);

    private String configFileName, metaFileName;

    /**
     * The configuration wrapper, loads first the stored configuration and then all stored configuration options
     */
    private CompositeConfiguration config;

    /**
     * The backend for storing configuration values, usually ${marmotta.home}/system-config.properties
     */
    private Configuration saveConfiguration;

    /**
     * The backend for storing metadata about configuration values, usually ${marmotta.home}/system-meta.properties
     */
    private Configuration saveMetadata;

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

    @Inject
    @Any
    private Event<LoggingStartEvent> loggingStartEvent;

    private int serverPort        = 0;
    //private String serverContext;
    private String serverName     = null;

    private boolean                          initialising;
    private boolean                          initialised = false;

    private ServletContext                   servletContext;


    /**
     * A lock to ensure proper concurrent access to the configuration. The system requests a write lock in case a
     * setXXX() method is called and a read lock in case a getXXX() method is called.
     */
    private ReadWriteLock lock;

    /**
     * Backlog for delayed event collection; only fires a configuration changed event if there has not been a further
     * update in a specified amount of time (default 250ms);
     */
    private Set<String> eventBacklog;

    private long EVENT_DELAY = 250L;


    /*
     * Timer and task for delayed execution of configuration changed events
     */
    private Timer          eventTimer;
    private ReentrantLock  eventLock;


    public ConfigurationServiceImpl() {
        runtimeFlags = new HashMap<String, Boolean>();
        lock = new ReentrantReadWriteLock();

        eventTimer = new Timer("Configuration Event Timer", true);
        eventLock  = new ReentrantLock();
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
    public void initialize(String home, Configuration override) {
        lock.writeLock().lock();
        try {
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

            setHome(home);

            if (getHome() != null) {
                File f1 = new File(getHome());
                if (!f1.exists()) {
                    f1.mkdirs();
                }
                // ensure directory for user configuration files
                File f2 = new File(getHome() + File.separator + DIR_CONFIG);
                if(!f2.exists()) {
                    f2.mkdirs();
                }

                // ensure directory for logging messages
                File f3 = new File(getHome() + File.separator + DIR_LOG);
                if(!f3.exists()) {
                    f3.mkdirs();
                }

                // ensure directory for importing data
                File f4 = new File(getHome() + File.separator + DIR_IMPORT);
                if(!f4.exists()) {
                    f4.mkdirs();
                }

            }

            // the save configuration will be in the  home directory
            try {
                if (getHome() != null) {
                    configFileName = getHome() + File.separator + "system-config.properties";
                    metaFileName = getHome() + File.separator + "system-meta.properties";

                    File configFile = new File(configFileName);
                    if (!configFile.exists()) {
                        log.info("creating system configuration in configuration file {}", configFile.getAbsolutePath());
                    } else {
                        log.info("reading system configuration from existing configuration file {}", configFile.getAbsolutePath());
                    }
                    saveConfiguration = new PropertiesConfiguration(configFile);


                    File metaFile = new File(metaFileName);
                    if (!metaFile.exists()) {
                        log.info("creating system configuration metadata in configuration file {}", metaFile.getAbsolutePath());
                    } else {
                        log.info("reading system configuration metadata from existing configuration file {}", metaFile.getAbsolutePath());
                    }
                    saveMetadata = new PropertiesConfiguration(metaFile);

                } else {
                    log.error("error while initialising configuration: no marmotta.home property given; creating memory-only configuration");
                    saveConfiguration = new MapConfiguration(new HashMap<String, Object>());
                    saveMetadata      = new MapConfiguration(new HashMap<String, Object>());
                }
            } catch (Exception e) {
                log.error("error while initialising configuration file {}: {}; creating memory-only configuration", configFileName, e.getMessage());
                saveConfiguration = new MapConfiguration(new HashMap<String, Object>());
                saveMetadata      = new MapConfiguration(new HashMap<String, Object>());
            }
            config = new FallbackConfiguration();
            final ConfigurationInterpolator _int = config.getInterpolator();
            _int.registerLookup("pattern.quote", new StrLookup() {
                @Override
                public String lookup(String key) {
                    return Pattern.quote(_int.getDefaultLookup().lookup(key));
                }
            });
            _int.registerLookup("urlencode", new StrLookup() {
                @Override
                public String lookup(String key) {
                    try {
                        return URLEncoder.encode(_int.getDefaultLookup().lookup(key), "utf8");
                    } catch (UnsupportedEncodingException e) {
                        return _int.getDefaultLookup().lookup(key);
                    }
                }
            });
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


            // create the configuration that is responsible for getting metadata about configuration keys in the main
            // configuration; since the keys will be different, we store changes also to the main save configuration
            configDescriptions = new FallbackConfiguration();
            configDescriptions.addConfiguration(saveMetadata,true);
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

            // setup home if it is given as system property,
            // the bootstrap configuration is overwritten
            if (getHome() != null) {
                config.setProperty("marmotta.home", getHome());
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


            loggingStartEvent.fire(new LoggingStartEvent());

            // this should maybe move to the KiWiPreStartupFilter ...
            initDatabaseConfiguration();

            save();

            log.info("Apache Marmotta Configuration Service: initialisation completed");

            configurationInitEvent.fire(new ConfigurationServiceInitEvent());

        } finally {
            lock.writeLock().unlock();
        }

    }

    @Override
    public boolean isInitialising() {
        return initialising;
    }

    /**
     * Signal that initialisation of the system has completed and configuration events are now enabled.
     *
     * @param initialising
     */
    @Override
    public void setInitialising(boolean initialising) {
        this.initialising = initialising;

        log.info("Initialisation completed, enabling configuration events");
    }

    private void initDatabaseConfiguration() {
        if (!config.getBoolean("kiwi.setup.database")) {
            log.info("SETUP: Setting up initial Apache Marmotta database configuration ...");
            String db_type = config.getString("database.type", "h2");
            config.setProperty("database.h2.url", "jdbc:h2:" + getHome() + "/db/marmotta;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10");
            if (db_type.equals("h2")) {
                config.setProperty("database.url", "jdbc:h2:" + getHome() + "/db/marmotta;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10");
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
        return getStringConfiguration(CoreOptions.BASE_URI);
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
        String serverUrl = getStringConfiguration(CoreOptions.SERVER_URI);

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
        lock.readLock().lock();
        try {
            List<String> keys = new LinkedList<String>();
            for (Iterator<String> it = config.getKeys(); it.hasNext();) {
                keys.add(it.next());
            }
            return keys;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * List all configuration keys defined for the system configuration of KiWi having prefix.
     *
     * @param prefix the prefix of keys that should be returned
     * @return
     */
    @Override
    public List<String> listConfigurationKeys(String prefix) {
        lock.readLock().lock();
        try {
            List<String> keys = new LinkedList<String>();
            for (Iterator<String> it = config.getKeys(prefix); it.hasNext();) {
                keys.add(it.next());
            }
            return keys;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * List all configuration keys matching a certain regular expression pattern. Returns a matcher object for all
     * matching keys that can be used to access capturing groups
     *
     * @param pattern
     * @return
     */
    @Override
    public List<Matcher> listConfigurationKeys(Pattern pattern) {
        lock.readLock().lock();
        try {
            List<Matcher> keys = new LinkedList<Matcher>();
            for (Iterator<String> it = config.getKeys(); it.hasNext();) {
                Matcher m = pattern.matcher(it.next());
                if(m.matches()) {
                    keys.add(m);
                }
            }
            return keys;
        } finally {
            lock.readLock().unlock();
        }
    }

    /*
         * (non-Javadoc)
         *
         * @see kiwi.api.config.ConfigurationService#isConfigurationSet(java.lang.String)
         */
    @Override
    public boolean isConfigurationSet(String key) {
        lock.readLock().lock();
        try {
            return config.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
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

        lock.readLock().lock();
        try {
            return config.getProperty(key);
        } finally {
            lock.readLock().unlock();
        }
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

        lock.readLock().lock();
        try {
            return configDescriptions.getString(key + ".description");
        } finally {
            lock.readLock().unlock();
        }

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

        lock.readLock().lock();
        try {
            String s = configDescriptions.getString(key+".type");
            return s!=null?s: String.class.getName();
        } finally {
            lock.readLock().unlock();
        }
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

        if(!config.containsKey(key) || !ObjectUtils.equals(value,config.getProperty(key))) {
            lock.writeLock().lock();
            try {
                config.setProperty(key, value);
                save();
            } finally {
                lock.writeLock().unlock();
            }

            if (!initialising) {
                raiseDelayedConfigurationEvent(Collections.singleton(key));
            }
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

        lock.writeLock().lock();
        try {
            configDescriptions.setProperty(key+".type",type);
            save();
        } finally {
            lock.writeLock().unlock();
        }
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

        lock.writeLock().lock();
        try {
            configDescriptions.setProperty(key+".description",description);
            save();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     *
     *
     */
    @Override
    public String getStringConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        lock.readLock().lock();
        try {
            if (config instanceof AbstractConfiguration) {
                ((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
            }
            String result = config.getString(key);
            if (config instanceof AbstractConfiguration) {
                ((AbstractConfiguration) config).setDelimiterParsingDisabled(false);
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String getStringConfiguration(String key, String defaultValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        lock.readLock().lock();
        try {
            if (config instanceof AbstractConfiguration) {
                ((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
            }
            String result = config.getString(key, defaultValue);
            if (config instanceof AbstractConfiguration) {
                ((AbstractConfiguration) config).setDelimiterParsingDisabled(false);
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public double getDoubleConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        lock.readLock().lock();
        try {
            return config.getDouble(key, 0.0);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public double getDoubleConfiguration(String key, double defaultValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        lock.readLock().lock();
        try {
            return config.getDouble(key, defaultValue);
        } finally {
            lock.readLock().unlock();
        }
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

        if(!config.containsKey(key) || !ObjectUtils.equals(value,config.getDouble(key))) {
            lock.writeLock().lock();
            try {
                config.setProperty(key, value);
                save();
            } finally {
                lock.writeLock().unlock();
            }

            if (!initialising) {
                raiseDelayedConfigurationEvent(Collections.singleton(key));
            }
        }
    }

    @Override
    public int getIntConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised, "ConfigurationService not yet initialised; call initialise() manually");

        lock.readLock().lock();
        try {
            return config.getInt(key, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int getIntConfiguration(String key, int defaultValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        lock.readLock().lock();
        try {
            return config.getInt(key, defaultValue);
        } finally {
            lock.readLock().unlock();
        }
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

        if(!config.containsKey(key) || !ObjectUtils.equals(value,config.getInt(key))) {
            lock.writeLock().lock();
            try {
                config.setProperty(key, value);
                save();
            } finally {
                lock.writeLock().unlock();
            }

            if (!initialising) {
                raiseDelayedConfigurationEvent(Collections.singleton(key));
            }
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

        lock.readLock().lock();
        try {
            return config.getLong(key, 0);
        } finally {
            lock.readLock().unlock();
        }
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

        lock.readLock().lock();
        try {
            return config.getLong(key, defaultValue);
        } finally {
            lock.readLock().unlock();
        }
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

        if(!config.containsKey(key) || !ObjectUtils.equals(value,config.getLong(key))) {
            lock.writeLock().lock();
            try {
                config.setProperty(key, value);
                save();
            } finally {
                lock.writeLock().unlock();
            }

            if (!initialising) {
                raiseDelayedConfigurationEvent(Collections.singleton(key));
            }
        }
    }

    @Override
    public boolean getBooleanConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        lock.readLock().lock();
        try {
            return config.getBoolean(key, false);
        } finally {
            lock.readLock().unlock();
        }

    }

    @Override
    public boolean getBooleanConfiguration(String key, boolean defaultValue) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        lock.readLock().lock();
        try {
            return config.getBoolean(key, defaultValue);
        } finally {
            lock.readLock().unlock();
        }

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

        if(!config.containsKey(key) || value != config.getBoolean(key)) {

            lock.writeLock().lock();
            try {
                config.setProperty(key, value);
                save();
            } finally {
                lock.writeLock().unlock();
            }


            if (!initialising) {
                raiseDelayedConfigurationEvent(Collections.singleton(key));
            }
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

        lock.readLock().lock();
        try {
            return config.getProperties(key);
        } finally {
            lock.readLock().unlock();
        }

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

        lock.readLock().lock();
        try {
            String[] result = config.getStringArray(key);

            if (result.length == 1 && "".equals(result[0].trim())) return Collections.emptyList();

            return Lists.newArrayList(result);
        } finally {
            lock.readLock().unlock();
        }

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

        lock.readLock().lock();
        try {
            if (config.containsKey(key)) {
                String[] values = config.getStringArray(key);
                if (values.length > 0) return Lists.newArrayList(values);
                else
                    return Lists.newArrayList();
            } else
                return defaultValue;
        } finally {
            lock.readLock().unlock();
        }

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

        if(!config.containsKey(key) || !ObjectUtils.equals(value,config.getList(key))) {
            lock.writeLock().lock();
            try {
                config.setProperty(key, value);
                save();
            } finally {
                lock.writeLock().unlock();
            }


            if (!initialising) {
                log.debug("firing configuration changed event for key {}", key);
                raiseDelayedConfigurationEvent(Collections.singleton(key));
            }
        }
    }

    @Override
    public void removeConfiguration(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        lock.writeLock().lock();
        try {
            config.clearProperty(key);
            configDescriptions.clearProperty(key+".type");
            configDescriptions.clearProperty(key+".description");
            save();
        } finally {
            lock.writeLock().unlock();
        }

    }

    @Override
    public void setConfiguration(String key, String value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        log.debug("setting configuration {} = {}", key, value);

        if(!config.containsKey(key) || !ObjectUtils.equals(value,config.getString(key))) {

            lock.writeLock().lock();
            try {
                if (config instanceof AbstractConfiguration) {
                    ((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
                }
                config.setProperty(key, value);
                if (config instanceof AbstractConfiguration) {
                    ((AbstractConfiguration) config).setDelimiterParsingDisabled(false);
                }
                save();
            } finally {
                lock.writeLock().unlock();
            }


            if (!initialising) {
                log.debug("firing configuration changed event for key {}", key);
                raiseDelayedConfigurationEvent(Collections.singleton(key));
            }
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

        lock.writeLock().lock();
        try {
            if (config instanceof AbstractConfiguration) {
                ((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
            }
            config.setProperty(key, value);
            if (config instanceof AbstractConfiguration) {
                ((AbstractConfiguration) config).setDelimiterParsingDisabled(false);
            }
            save();
        } finally {
            lock.writeLock().unlock();
        }

    }


    @Override
    public void setConfigurations(Map<String, ?> values) {
        Preconditions.checkNotNull(values);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        lock.writeLock().lock();
        try {
            if (config instanceof AbstractConfiguration) {
                ((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
            }
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                config.setProperty(entry.getKey(), entry.getValue());
            }
            if (config instanceof AbstractConfiguration) {
                ((AbstractConfiguration) config).setDelimiterParsingDisabled(false);
            }
            save();
        } finally {
            lock.writeLock().unlock();
        }


        if (!initialising) {
            raiseDelayedConfigurationEvent((values.keySet()));
        }
    }

    @Override
    public void setConfiguration(String key, List<String> values) {
        Preconditions.checkNotNull(key);
        Preconditions.checkState(initialised,"ConfigurationService not yet initialised; call initialise() manually");

        if(!config.containsKey(key) || !ObjectUtils.equals(values,config.getList(key))) {
            lock.writeLock().lock();
            try {
                config.setProperty(key, values);
                save();
            } finally {
                lock.writeLock().unlock();
            }


            if (!initialising) {
                raiseDelayedConfigurationEvent(Collections.singleton(key));
            }
        }
    }

    /**
     * The work directory for marmotta, where all applications will create their own subdirectories
     * @deprecated This name is misleading, use {@link #getHome()} instead.
     */

    @Override
    @Deprecated
    public String getWorkDir() {
        return getHome();
    }

    protected void save() {
        if(saveConfiguration instanceof PropertiesConfiguration) {
            try {
                log.debug("Saving configuration values");
                final PropertiesConfiguration conf = (PropertiesConfiguration)saveConfiguration;
                saveSecure(conf);
            } catch (ConfigurationException e) {
                log.error("could not save system configuration: {}", e.getMessage());
            }
        }

        if(saveMetadata instanceof PropertiesConfiguration) {
            try {
                log.debug("Saving configuration description");
                final PropertiesConfiguration conf = (PropertiesConfiguration)saveMetadata;
                saveSecure(conf);
            } catch (ConfigurationException e) {
                log.error("could not save system metadata: {}", e.getMessage());
            }
        }
    }

    protected void saveSecure(final PropertiesConfiguration conf) throws ConfigurationException {
        final File file = conf.getFile();
        try {
            if (file == null) {
                throw new ConfigurationException("No file name has been set!");
            } else if ((file.createNewFile() || true) && !file.canWrite()) {
                throw new IOException("Cannot write to file " + file.getAbsolutePath() + ". Is it read-only?");
            }
        } catch (IOException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
        log.debug("Saving {}", file.getAbsolutePath());
        
        final String fName = file.getName();
        try {
            int lastDot = fName.lastIndexOf('.');
            lastDot = lastDot > 0 ? lastDot : fName.length();

            final Path configPath = file.toPath();
            // Create a tmp file next to the original
            final Path tmp = Files.createTempFile(configPath.getParent(), fName.substring(0, lastDot)+".", fName.substring(lastDot));
            try {
                Files.copy(configPath, tmp, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException iox) {
                log.error("Could not create temp-file {}: {}", tmp, iox.getMessage());
                throw iox;
            }
            log.trace("using temporary file: {}", tmp);
            // Save the config to the tmp file
            conf.save(tmp.toFile());

            log.trace("tmp saved, now replacing the original file: {}", configPath);
            // Replace the original with the tmp file
            try {
                try {
                    Files.move(tmp, configPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException amnx) {
                    log.trace("atomic move not available: {}, trying without", amnx.getMessage());
                    Files.move(tmp, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException iox) {
                log.error("Could not write to {}, a backup was created in {}", configPath, tmp);
                throw iox;
            }
            log.info("configuration successfully saved to {}", configPath);
        } catch (final Throwable t) {
            throw new ConfigurationException("Unable to save the configuration to the file " + fName, t);
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
     * Set the LMF_HOME value to the correct path. Used during the initialization process.
     * @deprecated LMF_HOME is deprecated, use {@link #setHome(String)} instead!
     */
    @Override
    @Deprecated
    public void setLMFHome(String home) {
        log.warn("ConfigurationService.setLMFHome() is deprecated, consider call directly ConfigurationService.setHome()");
        this.setHome(home);
    }

    /**
     * Set the home value to the correct path. Used during the initialization process.
     *
     * @param home
     */
    @Override
    public void setHome(String home) {
        this.home = home;
    }

    /**
     * Return the value of the LMF_HOME setting. Used during the initialization process.
     * @deprecated use {@link #getHome()} instead
     */
    @Override
    @Deprecated
    public String getLMFHome() {
        log.warn("ConfigurationService.getLMFHome() is deprecated, consider call directly ConfigurationService.getHome()");
        return getHome();
    }

    /**
     * Return the value of the home setting. Used during the initialization process.
     *
     * @return
     */
    @Override
    public String getHome() {
        return home;
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
        if(StringUtils.isBlank(config.getString("contexts.inferred",null))) {
            return null;
        } else {
            return config.getString("contexts.inferred",null);
        }
    }

    /**
     * Get the uri of the default context
     *
     * @return
     */
    @Override
    public String getDefaultContext() {
        if(StringUtils.isBlank(config.getString("contexts.default",null))) {
            return null;
        } else {
            return config.getString("contexts.default",null);
        }
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

    /**
     * Return the context parameter, servlet init parameter, or system property with the given key. This method
     * provides unified access to properties configured in either the web.xml, the context.xml or via a system property
     * passed on startup. It can be used by services that cannot read their configuration from the main
     * system-config.properties.
     *
     * @param key
     * @return
     */
    @Override
    public String getContextParam(String key) {
        String value = System.getProperty(key);
        if(value == null) {
            if(servletContext != null) {
                value = servletContext.getInitParameter(key);

                if(value == null) {
                    value = servletContext.getAttribute(key) != null ? servletContext.getAttribute(key).toString() : null;
                }

            }
        }
        return value;
    }


    /**
     * Start a delayed execution of raising an event
     * @param keys
     */
    private void raiseDelayedConfigurationEvent(Set<String> keys) {
        eventLock.lock();
        try {
            if(eventBacklog == null) {
                eventBacklog = new HashSet<>();
            }
            eventBacklog.addAll(keys);

            if(eventTimer != null) {
                eventTimer.cancel();
            }
            eventTimer = new Timer("Configuration Event Timer", true);
            eventTimer.schedule(new EventTimerTask(), EVENT_DELAY);
        } finally {
            eventLock.unlock();
        }

        if(log.isDebugEnabled()) {
            log.debug("updated configuration keys [{}]", StringUtils.join(keys,", "));
        }

    }

    /**
     * Delayed event firing task
     */
    private class EventTimerTask extends TimerTask {

        @Override
        public void run() {
            eventLock.lock();
            try {
                Set<String> keys = eventBacklog;
                eventBacklog = null;

                if(log.isDebugEnabled()) {
                    log.debug("firing delayed ({}ms) configuration changed event with keys [{}]",EVENT_DELAY, StringUtils.join(keys,", "));
                }

                configurationEvent.fire(new ConfigurationChangedEvent(keys));
            } finally {
                eventLock.unlock();
            }
        }
    }
}
