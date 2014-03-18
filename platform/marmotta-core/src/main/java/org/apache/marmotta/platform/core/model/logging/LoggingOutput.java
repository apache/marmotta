/*
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
package org.apache.marmotta.platform.core.model.logging;

import ch.qos.logback.classic.Level;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class LoggingOutput {
    protected String id;


    protected ConfigurationService configurationService;

    protected static String DEFAULT_PATTERN = "%d{HH:mm:ss.SSS} %-5level %logger{36} - %msg%n";

    protected static String CONFIG_PATTERN  = "logging.%s.%s.%s";


    public LoggingOutput(String id, ConfigurationService configurationService) {
        this.id = id;
        this.configurationService = configurationService;
    }

    public String getName() {
        return configurationService.getStringConfiguration(getConfigKey("name"));
    }

    public void setName(String name) {
        configurationService.setConfiguration(getConfigKey("name"), name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPattern() {
        return configurationService.getStringConfiguration(getConfigKey("pattern"), DEFAULT_PATTERN);
    }

    public void setPattern(String pattern) {
        configurationService.setConfiguration(getConfigKey("pattern"), pattern);
    }

    /**
     * Return the maximum logging level this output accepts. All messages above this level will be ignored.
     *
     * @return
     */
    public Level getMaxLevel() {
        return Level.toLevel(configurationService.getStringConfiguration(getConfigKey("level")), Level.INFO);
    }

    /**
     * Set the maximum logging level this output accepts. All messages above this level will be ignored.
     *
     * @return
     */
    public void setMaxLevel(Level level) {
        configurationService.setConfiguration(getConfigKey("level"), level.toString());
    }


    /**
     * Internal method: return the configuration key for this logging output and the given key suffix.
     * @param key
     * @return
     */
    protected String getConfigKey(String key) {
        return String.format(CONFIG_PATTERN, getTypeIdentifier(), getId(), key);
    }

    /**
     * Get the type identifier for this kind of logging output (e.g. "file", "console", "syslog"). Used for
     * properly resolving the configuration keys.
     *
     * @return
     */
    protected abstract String getTypeIdentifier();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoggingOutput that = (LoggingOutput) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
