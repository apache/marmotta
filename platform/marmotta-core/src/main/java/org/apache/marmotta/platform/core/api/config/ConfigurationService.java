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
package org.apache.marmotta.platform.core.api.config;

import org.apache.commons.configuration.Configuration;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manage the system configuration.
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 *
 */
public interface ConfigurationService {

    static final String RESOURCE_PATH = "resource";

    static final String ANONYMOUS_PATH = "anon";

    static final String META_PATH = "meta";

    static final String CONTENT_PATH = "content";

    static final String INSPECT_PATH = "inspect";
    
    static final String LDP_PATH = "ldp"; //to be removed
    static final String CONTAINER_PATH = "container";
    
    @Deprecated
    static final String KNOWLEDGESPACE_PATH = "knowledgedspace";

    static final String CONTEXT_PATH = "context";

    static final String CONTEXT_DEFAULT = "default";

    static final String CONTEXT_CACHE = "cache";

    static final String CONTEXT_ENHANCEMENT = "enhanced";

    static final String CONTEXT_INFERRED = "inferred";

    static final String CONTEXT_SYSTEM = "system";
    
    static final String DIR_CONFIG = "config";

    static final String DIR_LOG = "log";

    static final String DIR_IMPORT = "import";

    /**
     * Get the base URI of the system.
     * The base URI is used by the LMF to create local resource URIs. In this way, all Apache Marmotta resources
     * are "Linked Open Data" compatible.
     * 
     * The base URI is calculated based on the request URI given by the user.
     * In most cases it is the same as {@link #getServerUri()}, but this is not guaranteed.
     * 
     * @return the base URI
     */
    String getBaseUri();

    /**
     * Get the base path of the system, which is the relative path from the server host.
     * For example, in the case of http://localhost:8080/LMF, /LMF would be returned as the path.
     * @return a String representing the path
     */
    String getPath();

    /**
     * Get the server URI of the system.
     * The server URI is used to accesses the
     * server that runs the KiWi (and SOLR) applications.
     * 
     * Can be used to compute the paths of the web services or
     * other applications relative to the current application.
     * 
     * Computed like the base URI.
     * 
     * @return the server URI
     */
    String getServerUri();

    /**
     * List all configuration keys defined for the system configuration of KiWi.
     * @return
     */
    List<String> listConfigurationKeys();

    /**
     * List all configuration keys defined for the system configuration of KiWi having prefix.
     * @param prefix the prefix of keys that should be returned
     * @return
     */
    List<String> listConfigurationKeys(String prefix);


    /**
     * List all configuration keys matching a certain regular expression pattern. Returns a matcher object for all
     * matching keys that can be used to access capturing groups
     *
     * @param pattern
     * @return
     */
    List<Matcher> listConfigurationKeys(Pattern pattern);

    /**
     * Check whether a certain configuration property is set.
     * @param key
     * @return
     */
    boolean isConfigurationSet(String key);

    /**
     * Get the configuration for the given key. If there is no such configuration, a new one is
     * created with empty value (returns null).
     *
     * @param key  unique configuration key for lookup
     * @return a configuration object with either the configured value or null as value
     */
    Object getConfiguration(String key);

    /**
     * Return the comment for the configuration with the given key as string. If there is no such
     * configuration, null is returned
     *
     * @param key unique configuration key for lookup
     * @return a string describing the configuration option or null if no comment was given
     */
    String getComment(String key);

    /**
     * Return the comment for the configuration with the given key as string. If there is no such
     * configuration, null is returned
     * @param key unique configuration key for lookup
     * @return  a string describing the type for key or DEFAULT_TYPE (String) if no type was given
     */
    String getType(String key);

    /**
     * Get the configuration for the given key. If there is no such configuration, a new one is
     * created with empty value (returns null).
     *
     * @param key  unique configuration key for lookup
     * @return a configuration object with either the configured value or null as value
     */
    String getStringConfiguration(String key);

    /**
     * Get the configuration for the given key. If there is no such configuration, a new one is
     * created using the provided defaultValue as string value.
     *
     * @param key unique configuration key for lookup
     * @param defaultValue default value if configuration not found
     * @return a configuration object with either the configured value or defaultValue
     */
    String getStringConfiguration(String key, String defaultValue);

    /**
     * Get the configuration for the given key. If there is no such configuration, 0.0 is returned
     *
     * @param key unique configuration key for lookup
     * @return a double value with either the configured value or 0.0
     */

    double getDoubleConfiguration(String key);

    /**
     * Get the configuration for the given key. If there is no such configuration, a new one is
     * created using the provided defaultValue as double value.
     *
     * @param key unique configuration key for lookup
     * @param defaultValue default value if configuration not found
     * @return a configuration object with either the configured value or defaultValue
     */
    double getDoubleConfiguration(String key, double defaultValue);

    /**
     * Set the system configuration with the given key to the given double value.
     * 
     * @param key
     * @param value
     */
    void setDoubleConfiguration(String key, double value);

    /**
     * Get the configuration for the given key. If there is no such configuration, 0 is returned
     *
     * @param key unique configuration key for lookup
     * @return a int value with either the configured value or 0
     */
    int getIntConfiguration(String key);

    /**
     * Get the configuration for the given key. If there is no such configuration, a new one is
     * created using the provided defaultValue as double value.
     *
     * @param key unique configuration key for lookup
     * @param defaultValue default value if configuration not found
     * @return a configuration object with either the configured value or defaultValue
     */
    int getIntConfiguration(String key, int defaultValue);

    /**
     * Set the system configuration with the given key to the given int value.
     * 
     * @param key
     * @param value
     */
    void setIntConfiguration(String key, int value);



    /**
     * Get the configuration for the given key. If there is no such configuration, 0 is returned
     *
     * @param key unique configuration key for lookup
     * @return a int value with either the configured value or 0
     */
    long getLongConfiguration(String key);

    /**
     * Get the configuration for the given key. If there is no such configuration, a new one is
     * created using the provided defaultValue as double value.
     *
     * @param key unique configuration key for lookup
     * @param defaultValue default value if configuration not found
     * @return a configuration object with either the configured value or defaultValue
     */
    long getLongConfiguration(String key, long defaultValue);

    /**
     * Set the system configuration with the given key to the given int value.
     *
     * @param key
     * @param value
     */
    void setLongConfiguration(String key, long value);


    /**
     * Get the configuration for the given key. If there is no such configuration, true is returned
     *
     * @param key unique configuration key for lookup
     * @return a int value with either the configured value or true
     */
    boolean getBooleanConfiguration(String key);

    /**
     * Get the configuration for the given key. If there is no such configuration, a new one is
     * created using the provided defaultValue as boolean value.
     *
     * @param key unique configuration key for lookup
     * @param defaultValue default value if configuration not found
     * @return a configuration object with either the configured value or defaultValue
     */
    boolean getBooleanConfiguration(String key, boolean defaultValue);

    /**
     * Set the system configuration with the given key to the given boolean value.
     * 
     * @param key
     * @param value
     */
    void setBooleanConfiguration(String key, boolean value);

    /**
     * Get the configuration for the given key as properties. The configuration is persisted in the following form:
     * key = k=v,k=v,k=v
     *
     * @param key unique configuration key for lookup
     * @return properties with the configured key value pairs
     */
    Properties getPropertiesConfiguration(String key);

    /**
     * Get the configuration for the given key. If there is no such configuration, an empty list is returned
     *
     * @param key unique configuration key for lookup
     * @return a list with either the configured value or empty list
     */

    List<String> getListConfiguration(String key);

    /**
     * Get the configuration for the given key. If there is no such configuration, a new one is
     * created using the provided defaultValue as double value.
     *
     * @param key unique configuration key for lookup
     * @param defaultValue default value if configuration not found
     * @return a configuration object with either the configured value or defaultValue
     */
    List<String> getListConfiguration(String key, List<String> defaultValue);

    /**
     * Set the system configuration with the given key to the given int value.
     * 
     * @param key
     * @param value
     */
    void setListConfiguration(String key, List<String> value);

    /**
     * Set a configuration value without firing an event. This is in rare cases needed to avoid
     * propagation of events.
     *
     * @param key
     * @param value
     */
    void setConfigurationWithoutEvent(String key, Object value);


    /**
     * Set the configuration "key" to the string value "value".
     * @param key
     * @param value
     */
    void setConfiguration(String key, Object value);

    /**
     * Set the configuration "key" to the string value "value".
     * @param key
     * @param value
     */
    void setConfiguration(String key, String value);

    /**
     * Set the configuration "key" to the string value "value".
     * @param key
     * @param values
     */
    void setConfiguration(String key, List<String> values);

    /**
     * Remove the configuration identified by "key" from the database.
     * @param key
     */
    void removeConfiguration(String key);

    /**
     * @return a string representation of work direction
     */
    @Deprecated
    String getWorkDir();

    /**
     * Initialise the configuration service using the given home directory, and optionally a configuration override
     * @param home
     * @param override
     */
    void initialize(String home, Configuration override);


    /**
     * Set a flag at runtime that is discarded on system shutdown; used e.g. to indicate that certain
     * processes have already been carried out.
     * @param value
     */
    void setRuntimeFlag(String flag, boolean value);

    /**
     * Return the value of the runtime flag passed as argument.
     * @param flag
     * @return
     */
    boolean getRuntimeFlag(String flag);

    /**
     * Set the LMF_HOME value to the correct path. Used during the initialisation process.
     *
     * @param home
     */
    @Deprecated
    void setLMFHome(String home);
    
    /**
     * Set the home value to the correct path. Used during the initialization process.
     * 
     * @param home
     */
    public void setHome(String home);

    /**
     * Return the value of the LMF_HOME setting. Used during the initialisation process.
     * @return
     */
    @Deprecated
    String getLMFHome();
    
    /**
     * Return the value of the home setting. Used during the initialization process.
     * 
     * @return
     */
    public String getHome();

    /**
     * Get the base URI for contexts
     * @return
     */
    String getBaseContext();

    /**
     * Return the context used for storing system information.
     *
     * @return a URI representing the system context
     */
    String getSystemContext();

    /**
     * Get the uri of the inferred context
     *
     * @return uri of this inferred context
     */
    String getInferredContext();

    /**
     * Get the uri of the default context
     *
     * @return
     */
    String getDefaultContext();

    /**
     * Get the uri of the context used for caching linked data
     * @return
     */
    String getCacheContext();

    /**
     * Get the uri of the context used for enhancements.
     * 
     * @return
     */
    String getEnhancerContex();

    /**
     * Batch update.
     * 
     * @param values
     * @see #setConfiguration(String, String)
     */
    void setConfigurations(Map<String, ?> values);

    /**
     * Set type for a configuration key
     *
     * @param key key for configuration fields
     * @param type type for configuratino field
     */
    void setType(String key, String type);

    /**
     * Set type for a configuration key
     *
     * @param key key for configuration fields
     * @param comment type for configuratino field
     */
    void setComment(String key, String comment);

    /**
     * Pass the servlet context over to the configuration service to provide runtime information about
     * the environment to the rest of the system.
     * @param context
     */
    void setServletContext(ServletContext context);

    /**
     * Get the servlet context used when initialising the system
     * @return
     */
    ServletContext getServletContext();

    /**
     * Get a string describing the type and version of the application server running the Apache Marmotta.
     *
     * @return
     */
    String getServerInfo();

    /**
     * Try figuring out on which port the server is running ...
     */
    int getServerPort();

    /**
     * Try figuring out the local name of the server
     * @return
     */
    String getServerName();

    /**
     * Return the context path of this application
     * @return
     */
    String getServerContext();

    /**
     * Return true if Jetty 6.x is detected; tests for presence of class org.mortbay.jetty.Server
     * @return
     */
    boolean isJetty6();

    /**
     * Return true if Jetty 7.x is detected; tests for presence of class org.eclipse.jetty.server.Server
     * @return
     */
    boolean isJetty7();

    /**
     * Return true if Tomcat 6.x is detected; tests for presence of class org.apache.catalina.ServerFactory
     * @return
     */
    boolean isTomcat6();

    /**
     * Return true if Tomcat 7.x is detected; tests for presence of class org.apache.catalina.CatalinaFactory
     * @return
     */
    boolean isTomcat7();

    /**
     * Shutdown the application server running this web application; tries to determine the kind of server we are
     * running under and send the proper shutdown signal before exiting with System.exit
     */
    void performServerShutdown();


    boolean isInitialising();

    void setInitialising(boolean initialising);


    /**
     * Return the context parameter, servlet init parameter, or system property with the given key. This method
     * provides unified access to properties configured in either the web.xml, the context.xml or via a system property
     * passed on startup. It can be used by services that cannot read their configuration from the main
     * system-config.properties.
     *
     * @param key
     * @return
     */
    String getContextParam(String key);
}
