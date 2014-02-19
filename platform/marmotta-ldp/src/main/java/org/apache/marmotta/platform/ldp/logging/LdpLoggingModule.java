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
package org.apache.marmotta.platform.ldp.logging;

import ch.qos.logback.classic.Level;
import org.apache.marmotta.platform.core.logging.BaseLoggingModule;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.Collections;

/**
 * Logging module for LDP
 */
@ApplicationScoped
public class LdpLoggingModule extends BaseLoggingModule {

    @Override
    public String getId() {
        return "ldp";
    }

    @Override
    public String getName() {
        return "LDP";
    }

    @Override
    public Collection<String> getPackages() {
        return Collections.singleton("org.apache.marmotta.platform.ldp");
    }

    @Override
    public Level getDefaultLevel() {
        return Level.WARN;
    }
}
