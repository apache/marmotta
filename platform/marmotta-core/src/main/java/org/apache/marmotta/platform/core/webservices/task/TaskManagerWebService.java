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
package org.apache.marmotta.platform.core.webservices.task;

import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskInfo;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * A webservice for listing and managing currently active background tasks of services that support the task manager
 * interface.
 *
 * <p/>
 * User: sschaffe
 */
@ApplicationScoped
@Path("/tasks")
public class TaskManagerWebService {

    private static Logger log = LoggerFactory.getLogger(TaskManagerWebService.class);

    @Inject
    private TaskManagerService taskManagerService;

    /**
     * List all tasks in all groups currently running in the system. The result
     * is a map from group to list of tasks, each task formatted as a key-value
     * map.
     * 
     * @return List of {@link TaskInfo}s boxed in {@link JSONObject}s
     */
    @GET
    @Path("/")
    @Produces("application/json")
    public Map<String, List<TaskInfo>> list() {
        log.debug("Listing all running tasks.");

        return taskManagerService.getTasksByGroup();
    }

    @GET
    @Path("/byThread")
    @Produces("application/json")
    public Map<String, List<TaskInfo>> listByThread() {
        HashMap<String, List<TaskInfo>> result = new HashMap<>();
        final Map<WeakReference<Thread>, Stack<TaskInfo>> tasksByThread = taskManagerService.getTasksByThread();
        for (Map.Entry<WeakReference<Thread>, Stack<TaskInfo>> e : tasksByThread.entrySet()) {
            Thread t = e.getKey().get();
            if (t != null) {
                result.put(t.getName(), e.getValue());
            }
        }

        return result;
    }

    /**
     * List all tasks in the group given as argument. The result is a map from
     * group to list of tasks, each task formatted as a key-value map.
     * 
     * @param group
     *            the group to list the running tasks
     * @return List of {@link TaskInfo}s boxed in {@link JSONObject}s
     */
    @GET
    @Path("/{group}")
    @Produces("application/json")
    public Map<String, List<TaskInfo>> list(@PathParam("group") String group) {
        log.debug("Listing all tasks of group '{}'", group);

        Map<String, List<TaskInfo>> result = new HashMap<>();

        Map<String, List<TaskInfo>> allTasks = taskManagerService.getTasksByGroup();
        if (allTasks.containsKey(group)) {
            result.put(group, allTasks.get(group));
        }

        return result;
    }


    /**
     * Return the task identified by the id given as argument.
     * 
     * @param id
     *            the id of the task to return (long value)
     * @return The {@link Task} boxed in a {@link JSONObject}
     */
    @GET
    @Path("/{group}/{name}")
    @Produces("application/json")
    public Response get(@PathParam("group") String group, @PathParam("name") String name) {
        Map<String, List<TaskInfo>> gList = list(group);
        if (gList.containsKey(group)) return Response.status(404).entity("Group " + group + " not found").build();

        for (TaskInfo t : gList.get(group)) {
            if (t != null && t.getName().equals(name)) return Response.ok().entity(t).build();
        }

        return Response.status(404).entity("Task " + name + " in Group " + group + " not found").build();
    }

}
