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

public abstract class Task extends TaskInfo {

    protected Task(String uuid) {
        super(uuid);
    }

    public abstract long endTask();

    public void updateMessage(String message) {
        this.message = message;
        updateLastModified();
    }

    public void updateProgress(long steps) {
        this.progress = steps;
        updateLastModified();
    }

    public void updateTotalSteps(long totalSteps) {
        this.totalSteps = totalSteps;
        updateLastModified();
    }

    public void updateDetailMessage(String detail, String message) {
        if (message != null) {
            detailMessages.put(detail, message);
            updateLastModified();
        } else {
            removeDetailMessage(detail);
        }
    }

    public void removeDetailMessage(String detail) {
        detailMessages.remove(detail);
        updateLastModified();
    }

    protected void updateLastModified() {
        this.lastUpdate = System.currentTimeMillis();
    }

    public void updateName(String name) {
        this.name = name;
        updateLastModified();
    }

    public void updateGroup(String group) {
        this.group = group;
        updateLastModified();
    }

    public void resetProgress() {
        this.totalSteps = this.progress = 0;
        updateLastModified();
    }

}
