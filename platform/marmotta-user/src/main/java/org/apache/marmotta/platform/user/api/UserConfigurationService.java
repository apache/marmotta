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
package org.apache.marmotta.platform.user.api;

import org.apache.marmotta.platform.user.model.UserAccount;

import java.util.List;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public interface UserConfigurationService {


    /**
     * Check whether the given configuration is set for the given user.
     *
     * @param user
     * @param key
     * @return
     */
    boolean isUserConfigurationSet(UserAccount user, String key);

    /**
     * Get the configuration for the given user and key. If there is no such configuration, a new one is
     * created with empty value (returns null).
     *
     * @param user  the user for whom to get the configuration
     * @param key  unique configuration key for lookup
     * @return a configuration object with either the configured value or null as value
     */
    String getUserConfiguration(UserAccount user, String key);

    /**
     * Get the configuration for the given user and key. If there is no such configuration, a new one is
     * created using the provided defaultValue as string value.
     *
     * @param user  the user for whom to get the configuration
     * @param key unique configuration key for lookup
     * @param defaultValue default value if configuration not found
     * @return a configuration object with either the configured value or defaultValue
     */
    String getUserConfiguration(UserAccount user, String key, String defaultValue);


    /**
     * Set the configuration "key" to the string value "value".
     * @param key
     * @param value
     */
    void setUserConfiguration(UserAccount user, String key, String value);

    /**
     * Set the configuration "key" to the string value "value".
     * @param key
     * @param values
     */
    void setUserListConfiguration(UserAccount user, String key, List<String> values);


    /**
     * Return the list configuration value of the given key for the given user. If there is
     * no value for the key, returns the empty list.
     *
     * @param user
     */
    List<Object> getUserListConfiguration(UserAccount user, String key);

    /**
     * Return the list configuration value of the given key for the given user. Returns the
     * given defaultValue if no configuration is found for the given key.
     *
     * @param user
     */
    List<Object> getUserListConfiguration(UserAccount user, String key, List<Object> defaultValue);

    /**
     * Remove the user configuration identified by "key" from the database.
     * @param key
     */
    void removeUserConfiguration(UserAccount user, String key);


}
