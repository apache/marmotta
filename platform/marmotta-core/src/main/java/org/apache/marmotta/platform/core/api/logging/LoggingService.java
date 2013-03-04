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

import org.slf4j.Logger;

import javax.enterprise.inject.spi.InjectionPoint;

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
	
}
