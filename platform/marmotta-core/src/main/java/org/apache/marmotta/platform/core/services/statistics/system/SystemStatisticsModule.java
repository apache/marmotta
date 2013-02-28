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
package org.apache.marmotta.platform.core.services.statistics.system;

import org.apache.marmotta.platform.core.api.statistics.StatisticsModule;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SystemStatisticsModule
 *
 * @author Sebastian Schaffert
 *
 */
public class SystemStatisticsModule implements StatisticsModule {


    /**
     * Return the display name of the statistics module.
     *
     * @return
     */
    @Override
    public String getName() {
        return "System Statistics";
    }

    /* (non-Javadoc)
      * @see kiwi.api.statistics.StatisticsModule#enable()
      */
	@Override
	public void enable() {
		// do nothing, system statistics always enabled
	}

	/* (non-Javadoc)
	 * @see kiwi.api.statistics.StatisticsModule#disable()
	 */
	@Override
	public void disable() {
		// do nothing, system statistics always enabled
	}

	
	
	/* (non-Javadoc)
	 * @see kiwi.api.statistics.StatisticsModule#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}

	
	
	/* (non-Javadoc)
	 * @see kiwi.api.statistics.StatisticsModule#getPropertyNames()
	 */
	@Override
	public List<String> getPropertyNames() {
		List<String> result = new LinkedList<String>();
		
		result.add("java version");
		result.add("java vendor");
		result.add("operating system");
		
		result.add("free memory");
		result.add("total memory");
		result.add("max memory");
		
		return result;
	}

	/* (non-Javadoc)
	 * @see kiwi.api.statistics.StatisticsModule#getStatistics()
	 */
	@Override
	public Map<String, String> getStatistics() {

		
		
		LinkedHashMap<String,String> result = new LinkedHashMap<String, String>();

		result.put("java version", System.getProperty("java.version"));
		result.put("java vendor", System.getProperty("java.vendor"));
		result.put("operating system", System.getProperty("os.name")+" ("+System.getProperty("os.version")+")");
		
		Runtime rt = Runtime.getRuntime();
		result.put("free memory",""+rt.freeMemory()/(1024*1024)+"MB");
		result.put("total memory",""+rt.totalMemory()/(1024*1024)+"MB");
		result.put("max memory",""+rt.maxMemory()/(1024*1024)+"MB");
		
		return result;
	}

}
