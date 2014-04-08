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
package ${package}.webservices;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ${package}.api.MyService;
import ${package}.exceptions.DoThisException;

@Path("/${moduleKey}")
@ApplicationScoped
public class MyWebService {

    @Inject
    private Logger log;

    @Inject
    private MyService myService;

    @GET
    @Produces("text/plain; charset=utf8")
    public Response hello(@QueryParam("name") String name) {
        if (StringUtils.isEmpty(name)) {
            log.warn("No name given");
            // No name given? Invalid request.
            return Response.status(Status.BAD_REQUEST).entity("Missing Parameter 'name'").build();
        }

        log.debug("Sending regards to {}", name);
        // Return the greeting.
        return Response.ok(myService.helloWorld(name)).build();
    }

    @POST
    public Response doThis(@FormParam("turns") @DefaultValue("2") int turns) throws DoThisException {
        log.debug("Request to doThis {} times", turns);
        if (turns > 100) throw new DoThisException("At max, 100 turns are allowed");
        if (turns < 0) throw new DoThisException("Can't undo 'This'");

        myService.doThis(turns);
        return Response.noContent().build();
    }

}
