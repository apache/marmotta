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
package org.apache.marmotta.platform.core.api.task;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class TaskInfo {

    protected final long                started;
    protected final String              uuid;
    protected long                      lastUpdate;
    protected String                    name;
    protected String                    group;
    protected String                    message;
    protected long                      progress   = 0;
    protected long                      totalSteps = 0;
    protected final Map<String, String> detailMessages;

    public Date getStarted() {
        return new Date(started);
    }

    public Date getLastUpdate() {
        return new Date(lastUpdate);
    }

    public Date getETA() {
        if (progress > 0 && totalSteps > 0) {
            long now = System.currentTimeMillis();
            long end = now + totalSteps * (now - started) / progress;
            return new Date(end);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getMessage() {
        return message;
    }

    public long getProgress() {
        return progress;
    }

    public long getTotalSteps() {
        return totalSteps;
    }

    public Map<String, String> getDetailMessages() {
        return Collections.unmodifiableMap(detailMessages);
    }

    protected TaskInfo(String uuid) {
        this.uuid = uuid;
        this.detailMessages = new LinkedHashMap<String, String>();
        this.lastUpdate = this.started = System.currentTimeMillis();
    }

    @Deprecated
    @JsonIgnore
    public String printStatus() {
        if (totalSteps > 0) return String.format("%s: %s [%d/%d] (%tF %<tT)", name, message, progress, totalSteps, lastUpdate);
        if (progress > 0) return String.format("%s: %s [%d] (%tF %<tT)", name, message, progress, lastUpdate);
        return String.format("%s: %s (%tF %<tT)", name, message, lastUpdate);
    }

}
