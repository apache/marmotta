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
 * Representation of the configuration of a Marmotta logfile. Mapped to a Logback RollingFileAppender configuration by
 * the LoggingService.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LogFileOutput extends LoggingOutput {


    public LogFileOutput(String id, ConfigurationService configurationService) {
        super(id, configurationService);
    }

    /**
     * Return the filename for logging; all logfiles will be created beneath MARMOTTA_HOME/log.
     * @return
     */
    public String getFileName() {
        return configurationService.getStringConfiguration(getConfigKey("file"));
    }

    public void setFileName(String fileName) {
        configurationService.setConfiguration(getConfigKey("file"), fileName);
    }

    public int getKeepDays() {
        return configurationService.getIntConfiguration(getConfigKey("keep"),30);
    }

    public void setKeepDays(int keepDays) {
        configurationService.setIntConfiguration(getConfigKey("keep"), keepDays);
    }

    /**
     * Get the type identifier for this kind of logging output (e.g. "file", "console", "syslog"). Used for
     * properly resolving the configuration keys.
     *
     * @return
     */
    @Override
    protected String getTypeIdentifier() {
        return "file";
    }


}
