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
package org.apache.marmotta.platform.core.api.modules;

import org.apache.commons.configuration.Configuration;
import org.apache.marmotta.platform.core.model.module.ModuleConfiguration;

import javax.enterprise.inject.spi.InjectionPoint;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * A service for managing the LMF modules that are active in the installation.
 * <p/>
 * User: sschaffe
 */
public interface ModuleService {

    /**
     * List the names of all currently active modules
     * @return
     */
    public Collection<String> listModules();

    /**
     * Return the configuration for the module identified by the name given as argument. Returns an
     * immutable Apache Commons Configuration object, or null if the module is not found.
     *
     * @param moduleName
     * @return
     */
    public ModuleConfiguration getModuleConfiguration(String moduleName);


    /**
     * Provide the current module configuration for the service injecting it, i.e. the configuration
     * of the module containing the service.
     *
     */
    public ModuleConfiguration getModuleConfiguration(InjectionPoint injectionPoint);

    /**
     * Provide the current module configuration for the given class, i.e. the configuration of the
     * module containing the class.
     *
     */
    public ModuleConfiguration getModuleConfiguration(Class<?> cls);

    /**
     * Get the URL of the JAR file of the module whose name is given as argument.
     *
     * @param moduleName
     * @return
     */
    public URL getModuleJar(String moduleName);

    /**
     * Get the path relative to the LMF base URL where the web contents of this URL can be found
     *
     * @param moduleName
     * @return
     */
    public String getModuleWeb(String moduleName);


    /**
     * Return a list of entities used by this module. Each entry is a fully-qualified classname of
     * an entity class that this module registers.
     *
     * @param moduleName
     * @return
     */
    public Collection<String> getEntities(String moduleName);

    /**
     * Return a list of webservices used by this module. Each entry is a fully-qualified classname
     * of a webservice class that this module registers.
     *
     * @param moduleName
     * @return
     */
    public Collection<String> getWebservices(String moduleName);


    /**
     * Return a list of filters used by this module. Each entry is a fully-qualified classname
     * of a filter class implementing LMFHttpFilter that this module registers.
     *
     * @param moduleName
     * @return
     */
    public Collection<String> getFilters(String moduleName);

    /**
     * Return a list of admin pages (paths)
     * @param moduleName
     * @return
     */
    public List<String> getAdminPages(String moduleName);

    /**
     * Weight is used to sort modules in UI.
     * typical weights &amp; recommendations:
     * <table>
     * <tr><th>weight</th><th>modules</th></tr>
     * <tr><td>5</td><td>scenario-specific module</td></tr>
     * <tr><td>10</td><td>lmf-core</td></tr>
     * <tr><td>20</td><td>lmf-demo-*</td></tr>
     * <tr><td>50</td><td>default</td></tr>
     * </table>
     * 
     * @param moduleName
     * @return the weight (default == 50)
     */
    public int getWeight(String moduleName);

    /**
     * returns  more complex admin page description
     * @param moduleName
     * @return
     */
    public List<HashMap<String,String>> getAdminPageObjects(String moduleName);

    /**
     * returns the icon (if set), null otherwise
     * @param moduleName
     * @return
     */
    public String getIcon(String moduleName);

    /**
     * list modules for container sorted on weight
     * @param container
     * @return
     */
    public List<String> listSortedModules(String container);

    /**
     * list containers sorted on weight
     * @return
     */
    public List<String> listSortedContainers();

}
