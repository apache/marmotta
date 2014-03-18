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
package org.apache.marmotta.platform.core.model.logging;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SyslogOutput extends LoggingOutput {


    public SyslogOutput(String id, ConfigurationService configurationService) {
        super(id, configurationService);
    }

    public String getFacility() {
        return configurationService.getStringConfiguration(getConfigKey("facility"), "LOCAL0");
    }

    public void setFacility(String facility) {
        configurationService.setConfiguration(getConfigKey("facility"), facility);
    }

    public String getHostName() {
        return configurationService.getStringConfiguration(getConfigKey("host"),"localhost");
    }

    public void setHostName(String hostName) {
        configurationService.setConfiguration(getConfigKey("host"), hostName);
    }

    /**
     * Get the type identifier for this kind of logging output (e.g. "file", "console", "syslog"). Used for
     * properly resolving the configuration keys.
     *
     * @return
     */
    @Override
    protected String getTypeIdentifier() {
        return "syslog";
    }
}
