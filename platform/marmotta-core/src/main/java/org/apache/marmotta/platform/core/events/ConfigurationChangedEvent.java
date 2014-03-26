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
package org.apache.marmotta.platform.core.events;

import java.util.Collections;
import java.util.Set;

/**
 * An event indicating that the configuration has been updated. Takes the configuration key as argument
 *
 * <p/>
 * KiWiUser: sschaffe
 */
public class ConfigurationChangedEvent {

    private final Set<String> keys;

    public ConfigurationChangedEvent(String key) {
        this.keys = Collections.singleton(key);
    }

    public ConfigurationChangedEvent(Set<String> keys) {
        this.keys = Collections.unmodifiableSet(keys);
    }

    public Set<String> getKeys() {
        return keys;
    }

    public boolean containsChangedKey(String key) {
        return keys.contains(key);
    }

    public boolean containsChangedKeyWithPrefix(String prefix) {
        for (String key : keys) {
            if (key.startsWith(prefix)) return true;
        }
        return false;
    }
}
