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
package org.apache.marmotta.platform.core.util;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;

import java.util.Collection;
import java.util.List;

public class FallbackConfiguration extends CompositeConfiguration {

    public FallbackConfiguration() {
        super();
    }

    public FallbackConfiguration(Collection<? extends Configuration> configurations) {
        super(configurations);
    }

    public FallbackConfiguration(Configuration inMemoryConfiguration, Collection<? extends Configuration> configurations) {
        super(inMemoryConfiguration, configurations);
    }

    public FallbackConfiguration(Configuration inMemoryConfiguration) {
        super(inMemoryConfiguration);
    }

    @Override
    public List<Object> getList(String key, List<?> defaultValue) {
        final Configuration mem = getInMemoryConfiguration();
        if (mem.containsKey(key))
            return mem.getList(key, defaultValue);
        else
            return super.getList(key, defaultValue);
    }

    @Override
    public List<Object> getList(String key) {
        final Configuration mem = getInMemoryConfiguration();
        if (mem.containsKey(key))
            return mem.getList(key);
        else
            return super.getList(key);
    }
}
