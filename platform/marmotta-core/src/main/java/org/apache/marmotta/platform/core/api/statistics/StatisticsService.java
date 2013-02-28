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
package org.apache.marmotta.platform.core.api.statistics;

import java.util.List;

/**
 * StatisticsService - gathers statistics about the system runtime behaviour, e.g.
 * database/hibernate, caching, etc.
 * 
 * 
 * 
 * @author Sebastian Schaffert
 * 
 */
public interface StatisticsService {

    /**
     * Turn on collection of statistical information for all modules. May introduce additional
     * overhead.
     */
    public void enableAll();

    /**
     * Turn off collection of statistical information for all modules.
     */
    public void disableAll();

    /**
     * Return true if statistics gathering is enabled.
     * 
     * @return
     */
    public boolean isEnabled();

    /**
     * Enable collection of statistical information for the specified module;
     * 
     * @param modName
     */
    public void enableModule(String modName);

    /**
     * Disable collection of statistical information for the specified module;
     * 
     * @param modName
     */
    public void disableModule(String modName);

    /**
     * Register the statistics module given as argument with the statistics service
     * 
     * @param mod
     */
    public void registerModule(String modName, StatisticsModule mod);

    /**
     * Unregister the statistics module given as argument.
     * 
     * @param mod
     */
    public void unregisterModule(StatisticsModule mod);

    /**
     * Unregister the statistics module with the moduleName given as argument.
     * 
     * @param modName
     */
    public void unregisterModule(String modName);

    /**
     * Return the statistics module identified by the name passed as parameter.
     * 
     * @param modName
     * @return
     */
    public StatisticsModule getModule(String modName);

    /**
     * Return all statistics modules associated with the statistics service.
     * 
     * @return
     */
    public List<String> listModules();

}
