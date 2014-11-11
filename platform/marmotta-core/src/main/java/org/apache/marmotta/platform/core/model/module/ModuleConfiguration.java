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
package org.apache.marmotta.platform.core.model.module;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.apache.commons.configuration.Configuration;
import org.apache.marmotta.commons.util.DateUtils;

import java.util.Date;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ModuleConfiguration {
    
    private Configuration config;

    public ModuleConfiguration(Configuration config) {
        Preconditions.checkNotNull(config, "configuration must not be null");

        this.config = config;
    }


    public boolean hasBuildInfo() {
        return config.containsKey("build.module");
    }


    /**
     * Get the module name as configured in kiwi-module.properties
     * @return the human-readable name of the module, or the string "Unknown Module"
     */
    public String getModuleName() {
        return config.getString("name","Unknown Module");
    }

    /**
     * Get the module identifier as used during build time.
     * @return the module identifier, or "unknown" if no build info is present
     */
    public String getModuleId() {
        return config.getString("build.module","unknown");
    }

    /**
     * Get the build version of the module as configured at build time.
     * @return the module version, or the string "RUNTIME" to indicate that the module is not part of a build
     */
    public String getModuleVersion() {
        return config.getString("build.version","RUNTIME");
    }

    /**
     * The user who assembled the module (was running the build), or the currently active system user
     * @return   the user who assembled the module (was running the build), or the currently active system user
     */
    public String getBuildUser() {
        return config.getString("build.user",System.getProperty("user.name"));
    }

    /**
     * The host on which the module was assembled
     * @return   the host on which the module was assembled
     */
    public String getBuildHost() {
        return config.getString("build.host","unknown");
    }

    /**
     * The host on which the module was assembled
     * @return   the host on which the module was assembled
     */
    public String getBuildOS() {
        return config.getString("build.os",System.getProperty("os.name") + " " + System.getProperty("os.version") + "/" + System.getProperty("os.arch"));
    }

    /**
     * The version management revision of the module as determined at build time, or "0". Returns the
     * integer version increment of the local repository used for building. For unique identifiers,
     * consider using getBuildRevisionHash
     *
     * @return the version management revision of the module as determined at build time, or "0"
     */
    public String getBuildRevisionNumber() {
        return config.getString("build.revision","0");
    }


    /**
     * Return the unique Mercurial hash identifier for the revision used to build the system. Use this instead
     * of getBuildRevisionNumber to ensure unique identifiers over several repositories.
     * @return   he version management revision of the module as determined at build time, or ""
     */
    public String getBuildRevisionHash() {
        if(config.getString("build.revhash") != null) {
            return "hg:" + config.getString("build.revhash");
        } else if(config.getString("git.revision") != null) {
            return "git:"+config.getString("git.revision").substring(0,6);
        } else {
            return "";
        }
    }
    
    
    /**
     * Return the build time of the module in ISO time (GMT)
     * @return the build time of the module in ISO time (GMT)
     */
    public String getBuildTimestamp() {
        String timestamp = Joiner.on(", ").join(config.getStringArray("build.timestamp"));
        if("".equals(timestamp)) {
            return DateUtils.GMTFORMAT.format(new Date());
        } else {
            return timestamp;
        }

    }


    /**
     * Return the Apache Commons Configuration object behind this module.
     *
     * @return
     */
    public Configuration getConfiguration() {
        return config;
    }
}
