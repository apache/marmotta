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
package org.apache.marmotta.platform.core.webservices;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * CoreApplication
 *
 * TODO: manage this by reading kiwi-module.properties file in each of the KiWi modules and looking for a property
 * "webservices"
 *
 * @author Sebastian Schaffert
 *
 */
public class CoreApplication extends Application {

    private Logger log = LoggerFactory.getLogger(CoreApplication.class);


    private static Set<Class<?>> classes = null;

    /**
     * 
     */
    public CoreApplication() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.ws.rs.core.Application#getClasses()
     */
    @Override
    public synchronized Set<Class<?>> getClasses() {


        if(classes == null) {
            classes = new HashSet<Class<?>>();

            try {
                Enumeration<URL> modulePropertiesEnum = this.getClass().getClassLoader().getResources("kiwi-module.properties");

                while(modulePropertiesEnum.hasMoreElements()) {
                    URL moduleUrl = modulePropertiesEnum.nextElement();

                    Configuration moduleProperties = null;
                    try {
                        moduleProperties = new PropertiesConfiguration(moduleUrl);

                        for(Object clsName : moduleProperties.getList("webservices")) {

                            if(!"".equals(clsName)) {
                                try {
                                    Class<?> cls = Class.forName(clsName.toString());

                                    classes.add(cls);

                                    log.debug("module {}: registered webservice {}", moduleProperties.getString("name"), cls.getCanonicalName());
                                } catch (ClassNotFoundException e) {
                                    log.error("could not load class {}, it was not found",clsName.toString());
                                }
                            }
                        }

                    } catch (ConfigurationException e) {
                        log.error("configuration exception: {}",e.getMessage());
                    }

                }


            } catch (IOException e) {
                log.error("I/O error while trying to load kiwi-module.properties file: {}",e.getMessage());
            }
        }

        return classes;
    }



}
