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
package org.apache.marmotta.platform.core.api.statistics;

import java.util.List;
import java.util.Map;

/**
 * StatisticsModule - collects statistical information for a specific part of the system.
 *
 * @author Sebastian Schaffert
 *
 */
public interface StatisticsModule {

	/**
	 * Enable this module. Depending on the type of information, this may involve additional runtime overhead.
	 */
	public void enable();
	
	/**
	 * Disable this module.
	 */
	public void disable();
	
	/**
	 * Return true if the module is enabled.
	 * @return
	 */
	public boolean isEnabled();
	
	
	/**
	 * Return all names of properties supported by this module.
	 * @return
	 */
	public List<String> getPropertyNames();
	
	/**
	 * Return the statistics as a map from key to value
	 * @return
	 */
	public Map<String,String> getStatistics();


    /**
     * Return the display name of the statistics module.
     * @return
     */
    public String getName();
}
