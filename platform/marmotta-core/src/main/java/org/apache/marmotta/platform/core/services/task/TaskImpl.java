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
package org.apache.marmotta.platform.core.services.task;

import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;

class TaskImpl extends Task {

    private static final String WAITING_DETAIL = "Waiting in status";

    private final TaskManagerService manager;

    TaskImpl(TaskManagerService manager, String uuid, String name, String group) {
        super(uuid);
        this.manager = manager;
        this.name = name;
        this.group = group;
    }

    @Override
    public long endTask() {
        long dur = System.currentTimeMillis() - started;
        manager.endTask(this);
        return dur;
    }

    public void subTastStarting(Task subTask) {
        detailMessages.put(WAITING_DETAIL, getMessage());
        updateMessage("waiting for Task " + subTask.getName() + " to complete");
    }

    public void subTaskEnded() {
        updateMessage(detailMessages.remove(WAITING_DETAIL));
    }
    
}
