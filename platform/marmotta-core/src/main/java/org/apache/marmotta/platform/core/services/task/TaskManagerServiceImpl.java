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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.marmotta.commons.util.HashUtils;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskInfo;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.slf4j.Logger;

import com.google.common.collect.MapMaker;

@ApplicationScoped
public class TaskManagerServiceImpl implements TaskManagerService {

    private static final String  DEFAULT_GROUP = "default";

    private final ConcurrentMap<Thread, Stack<TaskImpl>> tasks;

    private final ThreadWatchdog watchdog;

    @Inject
    private Logger log;

    @Inject
    public TaskManagerServiceImpl() {
        tasks = new MapMaker().makeMap();
        watchdog = new ThreadWatchdog(15000);
    }

    @PostConstruct
    public void startup() {
        watchdog.start();
    }

    /* (non-Javadoc)
     * @see xx.test.taks.TaskManagerService#endTask(xx.test.taks.TaskInfo)
     */
    @Override
    public void endTask(TaskInfo task) {
        final Stack<TaskImpl> stack = getStack();
        if (stack.contains(task)) {
            while (!stack.isEmpty()) {
                Task t = stack.pop();
                if (t.equals(task)) {
                    log.debug("Ending task {}.{}", t.getGroup(), t.getName());
                    break;
                } else {
                    log.debug("Ending task {}.{} because it's a sub-task", t.getGroup(), t.getName());
                    t.endTask();
                }
            }
            if (!stack.isEmpty()) {
                stack.peek().subTaskEnded();
            }
        } else {
            log.debug("Not ending task {} because thread {} is not the owner", task.getName(), Thread.currentThread().getName());
        }
        if (stack.isEmpty()) {
            tasks.remove(Thread.currentThread());
        }
    }

    /* (non-Javadoc)
     * @see xx.test.taks.TaskManagerService#getTasks()
     */
    @Override
    public List<TaskInfo> getTasks() {
        LinkedList<TaskInfo> ts = new LinkedList<TaskInfo>();
        for (Stack<TaskImpl> stack : tasks.values()) {
            ts.addAll(stack);
        }
        return Collections.unmodifiableList(ts);
    }

    /* (non-Javadoc)
     * @see xx.test.taks.TaskManagerService#getTask()
     */
    @Override
    public Task getTask() {
        return createTask(Thread.currentThread().getName());
    }

    /* (non-Javadoc)
     * @see xx.test.taks.TaskManagerService#createTask(java.lang.String)
     */
    @Override
    public Task createTask(String name, String group) {
        final Stack<TaskImpl> stack = getStack();
        final Task task;
        if (stack.isEmpty())
            return createSubTask(name, group);
        else {
            task = stack.peek();
            task.updateName(name);
            if (group != null) {
                task.updateGroup(group);
            }
        }
        return task;
    }

    @Override
    public Task createTask(String string) {
        return createTask(string, null);
    }

    /* (non-Javadoc)
     * @see xx.test.taks.TaskManagerService#createSubTask(java.lang.String)
     */
    @Override
    public Task createSubTask(String name) {
        return createSubTask(name, null);
    }

    @Override
    public Task createSubTask(String name, String group) {
        final Stack<TaskImpl> stack = getStack();
        final TaskImpl task;
        synchronized (stack) {
            task = new TaskImpl(this, createUUID(), name, group != null ? group : DEFAULT_GROUP);
            if (!stack.isEmpty()) {
                stack.peek().subTastStarting(task);
            }
            stack.push(task);
        }
        return task;
    }

    private String createUUID() {
        final Thread t = Thread.currentThread();
        String string = String.format("%d: %s %tR", t.getId(), t.getName(), new Date());
        return HashUtils.md5sum(string);
    }

    private Stack<TaskImpl> getStack() {
        final Thread key = Thread.currentThread();
        Stack<TaskImpl> stack = tasks.get(key);
        if (stack == null) {
            stack = new Stack<TaskImpl>();
            tasks.put(key, stack);
        }
        return stack;
    }

    /* (non-Javadoc)
     * @see xx.test.taks.TaskManagerService#getTasksByGroup()
     */
    @Override
    public Map<String, List<TaskInfo>> getTasksByGroup() {
        Map<String, List<TaskInfo>> result = new LinkedHashMap<String, List<TaskInfo>>();

        for (TaskInfo task : getTasks()) {
            final String group = task.getGroup();
            List<TaskInfo> list = result.get(group);
            if (list == null) {
                list = new LinkedList<TaskInfo>();
                result.put(group, list);
            }
            list.add(task);
        }

        return result;
    }

    /* (non-Javadoc)
     * @see xx.test.taks.TaskManagerService#getTasksByThread()
     */
    @Override
    public Map<WeakReference<Thread>, Stack<TaskInfo>> getTasksByThread() {
        Map<WeakReference<Thread>, Stack<TaskInfo>> result = new LinkedHashMap<WeakReference<Thread>, Stack<TaskInfo>>();

        for (Map.Entry<Thread, Stack<TaskImpl>> e : tasks.entrySet()) {
            Stack<TaskInfo> list = new Stack<TaskInfo>();
            list.addAll(e.getValue());
            result.put(new WeakReference<Thread>(e.getKey()), list);
        }

        return result;
    }

    private class ThreadWatchdog extends Thread {

        private final long millis;
        private boolean running;

        public ThreadWatchdog(long millis) {
            super("TaskThreadWatchdog");
            this.millis = millis;
        }

        @Override
        public void run() {
            running = true;
            log.trace("Watchdog: starting up");
            final Task task = getTask();
            int count = 0;
            while (running) {
                task.updateMessage("cleaning up");
                task.updateProgress(++count);
                try {
                    HashSet<Thread> dead = new HashSet<Thread>();
                    for (Thread t : tasks.keySet()) {
                        if (!t.isAlive()) {
                            dead.add(t);
                        }
                    }
                    for (Thread t : dead) {
                        log.debug("Watchdog: cleaning up dead thread " + t.getName());
                        tasks.remove(t);
                        t = null;
                    }
                    dead.clear();
                    dead = null;
                    task.updateMessage("sleeping");
                    synchronized (ThreadWatchdog.this) {
                        this.wait(millis);
                    }
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            task.endTask();
            log.trace("Watchdog: shutdown");
        }

        public void shutdown() {
            running = false;
            synchronized (this) {
                this.notify();
            }
        }

    }

    @PreDestroy
    public void shutdown() {
        watchdog.shutdown();
    }

}
