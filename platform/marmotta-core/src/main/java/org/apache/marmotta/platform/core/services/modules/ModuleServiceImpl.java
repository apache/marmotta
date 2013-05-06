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
package org.apache.marmotta.platform.core.services.modules;

import com.google.common.collect.ImmutableList;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.modules.ModuleService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.model.module.ModuleConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
@ApplicationScoped
public class ModuleServiceImpl implements ModuleService {

    private String default_container_name = "Others";
    private int default_container_number = 100;

    private Logger log = LoggerFactory.getLogger(ModuleServiceImpl.class);

    private Set<String> modules;

    private List<AdminPageContainer> containers;

    private Map<String,Configuration> configurationMap;
    private Map<String, Configuration> jarURLs;

    @Inject
    ConfigurationService configurationService;

    @PostConstruct
    public void initialize() {

        default_container_name = configurationService.getStringConfiguration("kiwi.pages.default_container.name",default_container_name);
        default_container_number = configurationService.getIntConfiguration("kiwi.pages.default_container.number",default_container_number);

        modules = new HashSet<String>();
        containers = new ArrayList<AdminPageContainer>();
        configurationMap = new HashMap<String, Configuration>();
        jarURLs = new HashMap<String, Configuration>();

        try {
            Enumeration<URL> modulePropertiesEnum = this.getClass().getClassLoader().getResources("kiwi-module.properties");

            while(modulePropertiesEnum.hasMoreElements()) {
                URL moduleUrl = modulePropertiesEnum.nextElement();

                Configuration moduleProperties = null;
                try {
                    Set<Configuration> configurations = new HashSet<Configuration>();

                    // get basic module configuration
                    moduleProperties = new PropertiesConfiguration(moduleUrl);
                    configurations.add(moduleProperties);

                    String moduleName = moduleProperties.getString("name");
                    modules.add(moduleName);



                    if(moduleProperties.getString("container") != null) {
                        addContainerModule(moduleProperties.getString("container"),moduleName,moduleProperties.getInt("container.weight",-1));
                    } else {
                        addContainerModule(default_container_name,moduleName,default_container_number);
                    }


                    URLConnection urlConnection = moduleUrl.openConnection();
                    URL jarUrl;
                    if(urlConnection instanceof JarURLConnection) {
                        JarURLConnection conn = (JarURLConnection)urlConnection;
                        jarUrl = conn.getJarFileURL();
                    } else {
                        String fileUrl = moduleUrl.toString();
                        jarUrl = new URL(fileUrl.substring(0,fileUrl.lastIndexOf("/")));
                    }



                    // get the build information
                    try {
                        PropertiesConfiguration buildInfo = new PropertiesConfiguration(new URL("jar:"+jarUrl.toString()+"!/buildinfo.properties"));
                        buildInfo.setDelimiterParsingDisabled(true);
                        configurations.add(buildInfo);
                    } catch(ConfigurationException ex) {
                    }

                    // alternative: maven buildinfo plugin
                    try {
                        PropertiesConfiguration buildInfo = new PropertiesConfiguration(new URL("jar:"+jarUrl.toString()+"!/build.info"));
                        buildInfo.setDelimiterParsingDisabled(true);
                        configurations.add(buildInfo);
                    } catch(ConfigurationException ex) {
                    }



                    // create runtime configuration
                    MapConfiguration runtimeConfiguration = new MapConfiguration(new HashMap<String, Object>());
                    runtimeConfiguration.setProperty("runtime.jarfile", jarUrl.toString());
                    configurations.add(runtimeConfiguration);


                    CompositeConfiguration moduleConfiguration = new CompositeConfiguration(configurations);
                    configurationMap.put(moduleName, moduleConfiguration);
                    jarURLs.put(jarUrl.toString(), moduleConfiguration);

                } catch (ConfigurationException e) {
                    log.error("error parsing kiwi-module.properties file at {}",moduleUrl,e);
                }


            }
        } catch (IOException ex) {
            log.error("I/O error while trying to retrieve kiwi-module.properties file",ex);
        }
    }

    private void addContainerModule(String container, String module, int number) {
        AdminPageContainer c;
        int i = containers.indexOf(container);
        if(i>=0) {
            c = containers.get(i);
            c.addModule(module);
        }
        else {
            c = new AdminPageContainer(container);
            containers.add(c);
            c.addModule(module);
        }
        if(number>0) c.setNumber(number);
    }

    /**
     * Provide the current module configuration to the service injecting it
     *
     */
    @Override
    @Produces
    public ModuleConfiguration getModuleConfiguration(InjectionPoint injectionPoint) {
        URL jarUrl = injectionPoint.getMember().getDeclaringClass().getProtectionDomain().getCodeSource().getLocation();
        Configuration cfg = jarURLs.get(jarUrl.toString());
        if(cfg != null) {
            return new ModuleConfiguration(cfg);
        } else {
            return new ModuleConfiguration(new MapConfiguration(new HashMap<String, Object>()));
        }
    }

    /**
     * Provide the current module configuration for the given class, i.e. the configuration of the
     * module containing the class.
     */
    @Override
    public ModuleConfiguration getModuleConfiguration(Class<?> cls) {
        URL jarUrl = cls.getProtectionDomain().getCodeSource().getLocation();
        Configuration cfg = jarURLs.get(jarUrl.toString());
        if(cfg != null) {
            return new ModuleConfiguration(cfg);
        } else {
            return new ModuleConfiguration(new MapConfiguration(new HashMap<String, Object>()));
        }
    }

    /**
     * Return a list of entities used by this module. Each entry is a fully-qualified classname of
     * an entity class that this module registers.
     *
     * @param moduleName
     * @return
     */
    @Deprecated
    @Override
    public Collection<String> getEntities(String moduleName) {
        Configuration config = getModuleConfiguration(moduleName).getConfiguration();
        if(config != null) return ImmutableList.copyOf(config.getStringArray("entities"));
        else
            return null;
    }

    /**
     * List the names of all currently active modules
     *
     * @return
     */
    @Override
    public Collection<String> listModules() {
        return modules;
    }

    /**
     * Lists containers and underlying modules
     * @return
     */
    public List<AdminPageContainer> listContainers() {
        return containers;
    }

    /**
     * Return the configuration for the module identified by the name given as argument. Returns an
     * immutable Apache Commons Configuration object, or null if the module is not found.
     *
     * @param moduleName
     * @return
     */
    @Override
    public ModuleConfiguration getModuleConfiguration(String moduleName) {

        Configuration cfg = configurationMap.get(moduleName);
        if(cfg != null) {
            return new ModuleConfiguration(cfg);
        } else {
            return new ModuleConfiguration(new MapConfiguration(new HashMap<String, Object>()));
        }
    }

    /**
     * Get the URL of the JAR file of the module whose name is given as argument.
     *
     * @param moduleName
     * @return
     */
    @Override
    public URL getModuleJar(String moduleName) {
        try {
            return new URL(configurationMap.get(moduleName).getString("runtime.jarfile"));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Get the path relative to the Apache Marmotta base URL where the web contents of this URL can be found
     *
     * @param moduleName
     * @return
     */
    @Override
    public String getModuleWeb(String moduleName) {
        Configuration config = getModuleConfiguration(moduleName).getConfiguration();
        if(config != null) return config.getString("baseurl", "/" + moduleName);
        else
            return null;
    }

    /**
     * Return a list of webservices used by this module. Each entry is a fully-qualified classname
     * of a webservice class that this module registers.
     *
     * @param moduleName
     * @return
     */
    @Override
    public Collection<String> getWebservices(String moduleName) {
        Configuration config = getModuleConfiguration(moduleName).getConfiguration();
        if(config != null) return ImmutableList.copyOf(config.getStringArray("webservices"));
        else
            return null;
    }

    /**
     * Return a list of filters used by this module. Each entry is a fully-qualified classname
     * of a filter class implementing LMFHttpFilter that this module registers.
     *
     * @param moduleName
     * @return
     */
    @Override
    public Collection<String> getFilters(String moduleName) {
        Configuration config = getModuleConfiguration(moduleName).getConfiguration();
        if(config != null) return ImmutableList.copyOf(config.getStringArray("filters"));
        else
            return null;
    }

    /**
     * returns the icon (if set), null otherwise
     * @param moduleName
     * @return
     */
    @Override
    public String getIcon(String moduleName) {
        Configuration config = getModuleConfiguration(moduleName).getConfiguration();
        if(config != null) return config.getString("icon");
        else
            return null;
    }

    /**
     * Return a list of admin pages (paths)
     * @param moduleName
     * @return
     */
    @Override
    @Deprecated
    public List<String> getAdminPages(String moduleName) {
        Configuration config = getModuleConfiguration(moduleName).getConfiguration();
        if(config != null) return ImmutableList.copyOf(config.getStringArray("adminpages"));
        else
            return null;
    }

    /**
     * Returns a list of AdminPage Objects
     * @param moduleName
     * @return
     */
    @Override
    public List<AdminPage> getAdminPageList(String moduleName) {
        ArrayList<AdminPage> l = new ArrayList<AdminPage>();
        Configuration config = getModuleConfiguration(moduleName).getConfiguration();

        if(config==null) return null;

        //test if there are names and order for admin pages
        if(config.getString("adminpages.0") != null) {
            int i=0;
            while(config.getString("adminpages."+i) != null) {
                try {
                    l.add(new AdminPage(
                            i,
                            config.getString("adminpages." + i + ".path", null),
                            config.getString("adminpages."+i+".title",null),
                            config.getBoolean("adminpages."+i+".important",false)
                    ));
                } catch (MarmottaException e) {
                    log.error("admin page cannot be added for module "+moduleName);
                }
            }
        } else {
            for(String path : getAdminPages(moduleName)) {
                l.add(new AdminPage(path));
            }
        }
        Collections.sort(l);
        return l;
    }

    @Override
    public int getWeight(String moduleName) {
        Configuration config = getModuleConfiguration(moduleName).getConfiguration();
        if (config != null)
            return config.getInt("weight", 50);
        else
            return 50;
    }

}
