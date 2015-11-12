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
package org.apache.marmotta.platform.core.services.modules;

import com.google.common.collect.ImmutableList;
import org.apache.marmotta.platform.core.api.modules.ModuleService;
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

    private Logger log = LoggerFactory.getLogger(ModuleServiceImpl.class);

    private Set<String> modules;

    private HashMap<String,ArrayList<String>> containers;

    private HashMap<String,Integer> container_weight;

    private Map<String,Configuration> configurationMap;
    private Map<String, Configuration> jarURLs;

    @PostConstruct
    public void initialize() {

        //default_container_name = configurationService.getStringConfiguration("kiwi.pages.default_container.name",default_container_name);
        //default_container_number = configurationService.getIntConfiguration("kiwi.pages.default_container.number",default_container_number);

        modules = new HashSet<String>();
        containers = new HashMap<String,ArrayList<String>>();
        container_weight = new HashMap<String, Integer>();

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

                    String c_name = moduleProperties.getString("container") != null ? moduleProperties.getString("container") : default_container_name;

                    if(containers.get(c_name) == null) {
                        containers.put(c_name, new ArrayList<String>());
                    }
                    containers.get(c_name).add(moduleName);

                    if(container_weight.get(c_name) == null ) {
                        container_weight.put(c_name,-1);
                    }

                    if(moduleProperties.getString("container.weight") != null) {
                        container_weight.put(c_name,Math.max(container_weight.get(c_name),moduleProperties.getInt("container.weight",-1)));
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
            //TODO container should be sortable
        } catch (IOException ex) {
            log.error("I/O error while trying to retrieve kiwi-module.properties file",ex);
        }
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

    public List<String> listSortedModules() {
        return sortModules(modules);
    }

    /**
     * returns all modules within a container
     * @param container
     * @return
     */
    public Collection<String> listModules(String container) {
        if(containers.containsKey(container)) {
            return containers.get(container);
        } else return null;
    }

    @Override
    public List<String> listSortedModules(String container) {
        if(containers.containsKey(container)) {
            return sortModules(containers.get(container));
        } else return null;
    }

    /**
     * sort modules
     * @param m
     * @return
     */
    private List<String> sortModules(Collection<String> m) {
        List<String> sorted = new ArrayList<String>(m);
        Collections.sort(sorted,new Comparator<String>() {
            @Override
            public int compare(String o, String o2) {
                return ((Integer) getWeight(o)).compareTo(getWeight(o2));
            }
        });
        return sorted;
    }

    /**
     * Lists containers and underlying modules
     * @return
     */
    public Collection<String> listContainers() {
        return containers.keySet();
    }

    @Override
    public List<String> listSortedContainers() {
        List sorted = new ArrayList(containers.keySet());
        Collections.sort(sorted,new Comparator<String>() {
            @Override
            public int compare(String o, String o2) {
                return container_weight.get(o2).compareTo(container_weight.get(o));
            }
        });
        return sorted;
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
     * Return a list of admin pages (links)
     * @param moduleName
     * @return
     */
    @Override
    public List<String> getAdminPages(String moduleName) {
        Configuration config = getModuleConfiguration(moduleName).getConfiguration();
        if(config != null) {
            if(!config.subset("adminpage.").isEmpty()) {
                ArrayList<String> l = new ArrayList<String>();
                while(config.getString("adminpage."+l.size()+".link") != null) {
                    l.add(config.getString("adminpage."+l.size()+".link"));
                }
                return l;
            } else return ImmutableList.copyOf(config.getStringArray("adminpages"));
        } else
            return null;
    }

    /**
     * returns  more complex admin page description
     * @param moduleName
     * @return
     */
    @Override
    public List<HashMap<String,String>> getAdminPageObjects(String moduleName) {
        Configuration config = getModuleConfiguration(moduleName).getConfiguration();
        if(config != null) {
            ArrayList<HashMap<String,String>> l = new ArrayList<HashMap<String,String>>();
            if(!config.subset("adminpage").isEmpty()) {
                while(config.getString("adminpage."+l.size()+".link") != null) {
                    HashMap<String,String> map = new HashMap<String, String>();
                    map.put("link",config.getString("baseurl")+config.getString("adminpage."+l.size()+".link"));
                    map.put("title",config.getString("adminpage."+l.size()+".title"));
                    l.add(map);
                }
            } else {
                for(String path : config.getStringArray("adminpages")) {
                    HashMap<String,String> map = new HashMap<String, String>();
                    map.put("link",config.getString("baseurl")+path);
                    String title;
                    if(path.lastIndexOf(".") > path.lastIndexOf("/")+1)
                        title = path.substring(path.lastIndexOf("/")+1,path.lastIndexOf(".")).replaceAll("_"," ");
                    else
                        title = path.substring(path.lastIndexOf("/")+1);
                    map.put("title",title);
                    l.add(map);
                }
            }
            return l;
        } else
            return null;
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
