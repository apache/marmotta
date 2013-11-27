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
package org.apache.marmotta.platform.core.api.logging;

import org.apache.marmotta.platform.core.model.logging.ConsoleOutput;
import org.apache.marmotta.platform.core.model.logging.LogFileOutput;
import org.apache.marmotta.platform.core.model.logging.LoggingOutput;
import org.apache.marmotta.platform.core.model.logging.SyslogOutput;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.InjectionPoint;
import java.util.List;

/**
 * LoggingService - a service for providing a SLF4J logger to other components in the 
 * KiWi system.
 *
 * @author Sebastian Schaffert
 *
 */
public interface LoggingService {

	/**
	 * Provide a logger to the injection point given as argument.
	 * 
	 * @param injectionPoint
	 * @return
	 */
	public Logger createLogger(InjectionPoint injectionPoint);


    /**
     * Return a list of all output configurations, reading directly from the configuration service.
     *
     * @return
     */
    public List<LoggingOutput> listOutputConfigurations();


    /**
     * Return the output configuration with the given ID.
     * @param id
     * @return
     */
    public LoggingOutput getOutputConfiguration(String id);


    /**
     * Return the console output configuration used by Marmotta. There is only a single console output for any
     * Marmotta instance.
     *
     * @return
     */
    public ConsoleOutput getConsoleOutput();

    /**
     * Create a new syslog output configuration using the given parameters; further options can be set on the object
     * itself. If not set, the default hostname is "localhost" and the default facility is "LOCAL0".
     *
     * @param id   unique identifier for the log output configuration
     * @param name human-readable name for configuration (displayed in UI)
     * @return
     */
    public SyslogOutput createSyslogOutput(String id, String name);

    /**
     * Create a new logfile output configuration using the given parameters; further options can be set on the object
     * itself.
     *
     * @param id   unique identifier for the log output configuration
     * @param name human-readable name for configuration (displayed in UI)
     * @param file filename under MARMOTTA_HOME/log
     * @return
     */
    public LogFileOutput createLogFileOutput(String id, String name, String file);


    /**
     * Return a list of all modules found on the classpath. This list is assembled via CDI injection. Since modules are
     * managed beans, calling setters on the LoggingModule implementations will directly change the configuration.
     *
     * @return
     */
    public List<LoggingModule> listModules();

}
