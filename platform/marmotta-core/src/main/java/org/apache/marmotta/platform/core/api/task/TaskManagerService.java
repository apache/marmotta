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

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public interface TaskManagerService {

    public Task createTask(String name);

    public Task createTask(String name, String group);

    public Task createSubTask(String name);

    public Task createSubTask(String name, String group);

    public Task getTask();

    public void endTask(TaskInfo task);

    public List<TaskInfo> getTasks();

    public Map<String, List<TaskInfo>> getTasksByGroup();

    public Map<WeakReference<Thread>, Stack<TaskInfo>> getTasksByThread();

}
