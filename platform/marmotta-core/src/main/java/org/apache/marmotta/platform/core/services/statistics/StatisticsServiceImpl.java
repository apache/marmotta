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
package org.apache.marmotta.platform.core.services.statistics;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.statistics.StatisticsModule;
import org.apache.marmotta.platform.core.api.statistics.StatisticsService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * StatisticsServiceImpl
 *
 * @author Sebastian Schaffert
 *
 */
@ApplicationScoped
@Named("kiwi.core.statisticsService")
public class StatisticsServiceImpl implements StatisticsService {

    private LinkedHashMap<String,StatisticsModule> modules;

    private boolean enabled = false;

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    /**
     * Initialise all statistics modules that are found in the system
     * @param modules
     */
    @Inject
    protected void initModules(@Any Instance<StatisticsModule> modules) {
        log.info("Apache Marmotta StatisticsService starting up ...");

        this.modules = new LinkedHashMap<String,StatisticsModule>();

        for(StatisticsModule module : modules) {
            registerModule(module.getName(),module);
        }

        if(configurationService.getBooleanConfiguration("statistics.enabled",false)) {
            enableAll();
        } else {
            disableAll();
        }
    }

    public void configurationChangedEvent(@Observes ConfigurationChangedEvent event) {
        if (event.containsChangedKey("statistics.enabled")) {
            if(configurationService.getBooleanConfiguration("statistics.enabled",false)) {
                enableAll();
            } else {
                disableAll();
            }
        }
    }



    /* (non-Javadoc)
     * @see kiwi.api.statistics.StatisticsService#enableAll()
     */
    @Override
    public void enableAll() {
        for(StatisticsModule mod : modules.values()) {
            mod.enable();
        }

        enabled = true;
    }

    /* (non-Javadoc)
     * @see kiwi.api.statistics.StatisticsService#disableAll()
     */
    @Override
    public void disableAll() {
        for(StatisticsModule mod : modules.values()) {
            mod.disable();
        }

        enabled = false;
    }



    /* (non-Javadoc)
     * @see kiwi.api.statistics.StatisticsService#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /* (non-Javadoc)
     * @see kiwi.api.statistics.StatisticsService#enableModule(java.lang.String)
     */
    @Override
    public void enableModule(String modName) {
        if(modules.get(modName) != null) {
            modules.get(modName).enable();
        }
    }

    /* (non-Javadoc)
     * @see kiwi.api.statistics.StatisticsService#disableModule(java.lang.String)
     */
    @Override
    public void disableModule(String modName) {
        if(modules.get(modName) != null) {
            modules.get(modName).disable();
        }
    }

    /* (non-Javadoc)
     * @see kiwi.api.statistics.StatisticsService#registerModule(java.lang.String, kiwi.api.statistics.StatisticsModule)
     */
    @Override
    public void registerModule(String modName, StatisticsModule mod) {
        log.info("registering statistics module \"{}\"", modName);
        modules.put(modName,mod);
    }

    /* (non-Javadoc)
     * @see kiwi.api.statistics.StatisticsService#unregisterModule(kiwi.api.statistics.StatisticsModule)
     */
    @Override
    public void unregisterModule(StatisticsModule mod) {
        unregisterModule(mod.getName());
    }

    /* (non-Javadoc)
     * @see kiwi.api.statistics.StatisticsService#unregisterModule(java.lang.String)
     */
    @Override
    public void unregisterModule(String modName) {
        log.info("unregistering statistics module \"{}\"", modName);
        modules.remove(modName);
    }


    /* (non-Javadoc)
     * @see kiwi.api.statistics.StatisticsService#getModule(java.lang.String)
     */
    @Override
    public StatisticsModule getModule(String modName) {
        return modules.get(modName);
    }

    /* (non-Javadoc)
     * @see kiwi.api.statistics.StatisticsService#listModules()
     */
    @Override
    public List<String> listModules() {
        return new LinkedList<String>(modules.keySet());
    }



}
