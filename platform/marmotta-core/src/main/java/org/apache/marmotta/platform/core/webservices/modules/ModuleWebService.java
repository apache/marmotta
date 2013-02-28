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
package org.apache.marmotta.platform.core.webservices.modules;

import org.apache.marmotta.platform.core.api.modules.ModuleService;
import org.apache.marmotta.platform.core.model.module.ModuleConfiguration;
import org.apache.commons.configuration.Configuration;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Give information about modules registered in the system.
 * <p/>
 * User: sschaffe
 */
@Path("/modules")
public class ModuleWebService {

    @Inject
    private ModuleService moduleService;


    /**
     * Return a list of names of the LMF modules that are currently enabled in the LMF installation.
     *
     * @return  a JSON list of strings
     */
    @Path("/list")
    @GET
    @Produces("application/json")
    public Collection<String> listModules() {
        return moduleService.listModules();
    }


    /**
     * Return the configuration of the module identified by the name passed as query argument. The module will be
     * a map containing the values specified in the kiwi-module.properties file of the module.
     *
     * @param moduleName the name of the module for which to return the configuration
     * @return a map with key/value pairs representing the module configuration as contained in kiwi-module.properties
     */
    @Path("/module")
    @GET
    @Produces("application/json")
    public Map<String,Object> getConfiguration(@QueryParam("name") String moduleName) {
        Configuration cfg = null;
        try {
            cfg = moduleService.getModuleConfiguration(URLDecoder.decode(moduleName, "UTF-8")).getConfiguration();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        if(cfg != null) {
            Map<String,Object> result = new HashMap<String, Object>();
            for(Iterator<String> it = cfg.getKeys() ; it.hasNext(); ) {
                String key = it.next();
                result.put(key,cfg.getProperty(key));
            }
            return result;
        } else
            return null;
    }

    /**
     * Return the configuration of the module identified by the name passed as query argument. The
     * module will be
     * a map containing the values specified in the kiwi-module.properties file of the module.
     * 
     * @return a map with key/value pairs representing the module configuration as contained in
     *         kiwi-module.properties
     */
    @Path("/buildinfo")
    @GET
    @Produces("application/json")
    public Map<String, Map<String, String>> getBuildInfo() {
        HashMap<String, Map<String, String>> mods = new HashMap<String, Map<String, String>>();

        for (String moduleName : moduleService.listModules()) {
            Configuration cfg = moduleService.getModuleConfiguration(moduleName).getConfiguration();
            if (cfg != null) {
                ModuleConfiguration mCfg = new ModuleConfiguration(cfg);
                if (mCfg.hasBuildInfo()) {
                    Map<String, String> result = new LinkedHashMap<String, String>();

                    result.put("id", mCfg.getModuleId());
                    result.put("version", mCfg.getModuleVersion());
                    result.put("timestamp", mCfg.getBuildTimestamp());
                    result.put("revNumber", mCfg.getBuildRevisionNumber());
                    result.put("revHash", mCfg.getBuildRevisionHash());
                    result.put("user", mCfg.getBuildUser());
                    result.put("host", mCfg.getBuildHost());
                    result.put("os", mCfg.getBuildOS());

                    final List<String> adminPages = moduleService.getAdminPages(moduleName);
                    if (adminPages != null && adminPages.size() > 0 && adminPages.get(0).trim().length() > 0) {
                        result.put("admin",
                                moduleService.getModuleWeb(moduleName) +
                                adminPages.get(0));
                    }

                    mods.put(moduleName, result);
                } else {
                    mods.put(moduleName, null);
                }
            } else {
                mods.put(moduleName, null);
            }
        }
        return mods;
    }

}
