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
package org.apache.marmotta.platform.core.webservices.statistics;

import org.apache.marmotta.platform.core.api.statistics.StatisticsModule;
import org.apache.marmotta.platform.core.api.statistics.StatisticsService;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
@ApplicationScoped
@Path("/statistics")
public class StatisticsWebService {

    @Inject
    private Logger log;

    @Inject
    private StatisticsService statisticsService;


    /**
     * Enable or disable the statistics gathering. If enabled, might cause additional overhead in execution.
     *
     * @param enabled if true, statistics gathering will be enabled, if false, it will be disabled
     * @HTTP 200 when the statistics gathering has been enabled or disabled successfully
     * @return OK when changing the statistics setting was successful
     */
    @PUT
    @Path("/enabled")
    public Response setEnabled(@QueryParam("value") boolean enabled) {
        if(enabled) {
            log.info("enabling statistics gathering ...");
            statisticsService.enableAll();
        } else {
            log.info("disabling statistics gathering ...");
            statisticsService.disableAll();
        }
        return Response.ok().entity("statistics gathering "+(enabled?"enabled":"disabled")).build();
    }

    /**
     * Return the status of statistics gathering.
     *
     * @return Returns true if statistics gathering is enabled, false if it is disabled.
     */
    @GET
    @Path("/enabled")
    public boolean isEnabled() {
        return statisticsService.isEnabled();
    }

    /**
     * Retrieve the statistics information of all statistics modules.
     * @return a JSON-formatted map with an entry for each statistics module, where the value is a map of
     *         (key,value) entries for the statistics properties that are collected by the module
     */
    @GET
    @Produces("application/json")
    @Path("/list")
    public Map<String,Map<String,String>> getStatistics() {
        Map<String,Map<String,String>> result = new HashMap<String, Map<String, String>>();

        for(String module : statisticsService.listModules()) {
            result.put(module, statisticsService.getModule(module).getStatistics());
        }

        return result;
    }

    /**
     * Retrieve the statistics information of the statistics module with the name passed as path argument. The
     * result format is identical to the result returned by /list
     *
     * @param module the module for which to return the statistics
     * @return a JSON-formatted map with an entry for each statistics module, where the value is a map of
     *         (key,value) entries for the statistics properties that are collected by the module
     * @HTTP 200 if the statistics module exists
     * @HTTP 404 if the statistics module does not exist
     */
    @GET
    @Produces("application/json")
    @Path("/{module}")
    public Response getStatistics(@PathParam("module") String module) {
        if(statisticsService.getModule(module) != null) {
            Map<String,Map<String,String>> result = new HashMap<String, Map<String, String>>();

            result.put(module, statisticsService.getModule(module).getStatistics());

            return Response.ok().entity(result).build();
        } else
            return Response.status(404).entity("module with name "+module+" does not exist; available modules: "+statisticsService.listModules()).build();
    }

    @PUT
    @Path("/{module}/enabled")
    public Response setEnabled(@PathParam("module") String module, @QueryParam("value") boolean enabled) {
        final StatisticsModule mod = statisticsService.getModule(module);
        if (mod != null) {
            if (enabled) {
                mod.enable();
            } else {
                mod.disable();
            }
            return Response.ok().entity(enabled).build();
        }
        else
            return Response.status(404)
                    .entity("module with name " + module + " does not exist; available modules: " + statisticsService.listModules()).build();
    }

    @GET
    @Path("/{module}/enabled")
    public boolean isEnabled(@PathParam("module") String module) {
        final StatisticsModule mod = statisticsService.getModule(module);
        if (mod != null)
            return mod.isEnabled();
        else
            return false;
    }

    /**
     * Return a JSON-formatted list of all statistics modules that are available in the system.
     *
     * @return a JSON-formatted list of strings, each representing the name of a statistics module
     */
    @GET
    @Produces("application/json")
    @Path("/modules")
    public List<String> getModules() {
        return statisticsService.listModules();
    }
}
