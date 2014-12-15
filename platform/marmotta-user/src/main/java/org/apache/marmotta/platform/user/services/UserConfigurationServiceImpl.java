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
package org.apache.marmotta.platform.user.services;

import org.apache.marmotta.platform.user.api.UserConfigurationService;
import org.apache.marmotta.platform.user.model.UserAccount;
import com.google.common.base.Preconditions;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class UserConfigurationServiceImpl implements UserConfigurationService {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    private HashMap<String, Configuration>   userConfigurations;


    @PostConstruct
    public void initialise() {
        userConfigurations = new HashMap<String, Configuration>();
    }

    /*
    * (non-Javadoc)
    *
    * @see kiwi.api.config.ConfigurationService#isUserConfigurationSet(kiwi.model.user.UserAccount,
    * java.lang.String)
    */
    @Override
    public boolean isUserConfigurationSet(UserAccount user, String key) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(key);

        return getUserConfiguration(user).containsKey(key);
    }

    @Override
    public String getUserConfiguration(UserAccount user, String key, String defaultValue) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(key);

        return getUserConfiguration(user).getString(key, defaultValue);
    }

    @Override
    public String getUserConfiguration(UserAccount user, String key) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(key);

        return getUserConfiguration(user).getString(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * kiwi.api.config.ConfigurationService#getUserListConfiguration(kiwi.model.user.UserAccount,
     * java.lang.String)
     */
    @Override
    public List<Object> getUserListConfiguration(UserAccount user, String key) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(key);

        return getUserListConfiguration(user, key, Collections.emptyList());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * kiwi.api.config.ConfigurationService#getUserListConfiguration(kiwi.model.user.UserAccount,
     * java.lang.String, java.util.List)
     */
    @Override
    public List<Object> getUserListConfiguration(UserAccount user, String key, List<Object> defaultValue) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(key);

        return getUserConfiguration(user).getList(key, defaultValue);
    }

    @Override
    public void removeUserConfiguration(UserAccount user, String key) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(key);

        getUserConfiguration(user).clearProperty(key);
    }

    @Override
    public void setUserListConfiguration(UserAccount user, String key, List<String> values) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(key);

        getUserConfiguration(user).setProperty(key, values);
    }

    @Override
    public void setUserConfiguration(UserAccount user, String key, String value) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(key);

        getUserConfiguration(user).setProperty(key, value);
    }


    public Configuration getUserConfiguration(UserAccount user) {
        Configuration userConfig = userConfigurations.get(user.getLogin());
        if (userConfig == null) {

            String userConfigFile = configurationService.getConfiguration("kiwi.work.dir") + File.separator + "config" + File.separator + user.getLogin() + ".conf";

            try {
                File f = new File(userConfigFile);
                if (f.exists()) {
                    f.createNewFile();
                }
                userConfig = new PropertiesConfiguration(f);
            } catch (Exception ex) {
                log.error("could not create user configuration in file #0: #1", userConfigFile, ex.getMessage());
                userConfig = new MapConfiguration(new HashMap<String, Object>());
            }
            userConfigurations.put(user.getLogin(), userConfig);
        }
        return userConfig;
    }


    public void save(UserAccount user) {
        Configuration userConfig = getUserConfiguration(user);

        if (userConfig instanceof PropertiesConfiguration) {
            try {
                ((PropertiesConfiguration) userConfig).save();
            } catch (ConfigurationException e) {
                log.error("could not save user configuration for user #0: #1", user.getLogin(), e.getMessage());
            }
        }
    }


}
