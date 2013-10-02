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
package org.apache.marmotta.platform.reasoner.webservices;

import com.google.common.base.Joiner;
import info.aduna.iteration.Iterations;
import org.apache.marmotta.kiwi.reasoner.model.program.Program;
import org.apache.marmotta.platform.reasoner.services.ReasoningSailProvider;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
@ApplicationScoped
@Path("/reasoner/program")
@Produces({"application/json"})
public class ProgramWebService {

    @Inject
    private Logger log;

    @Inject
    private ReasoningSailProvider provider;

    @POST
    @Path("/{name}")
    @Produces("text/plain")
    @Consumes("text/plain")
    public Response uploadProgram(@PathParam("name") String name, @Context HttpServletRequest request) {
        try {
            provider.addProgram(name,request.getInputStream());

            return Response.ok("program created").build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{name}")
    @Produces("text/plain")
    @Consumes("text/plain")
    public Response updateProgram(@PathParam("name") String name, @Context HttpServletRequest request) {
        try {
            provider.updateProgram(name, request.getInputStream());

            return Response.ok("program updated").build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{name}")
    @Produces("text/plain")
    public Response getProgram(@PathParam("name") String name) {
        try {
            Program program = provider.getProgram(name);
            if (program == null) return Response.status(404).entity("Could not find program with name '" + name + "'").build();
            return Response.ok(program.toString()).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{name}")
    public Response deleteProgram(@PathParam("name") String name) {
        log.debug("Deleting reasoning program '{}'", name);
        try {
            provider.deleteProgram(name);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(404).entity("program with name "+name+" not found").build();
        }
    }


    @GET
    @Path("/list")
    public Response getProgramList() {
        try {
            List<Program> programs = Iterations.asList(provider.listPrograms());
            List<POJOProgram> pgrs = new ArrayList<POJOProgram>();
            log.debug("Listing {} reasoning programs", programs.size());
            for(Program p : programs) {
                POJOProgram pr = new POJOProgram();
                pr.setName(p.getName());
                pr.setRules(Joiner.on("\n").join(p.getRules()));
                pgrs.add(pr);
            }
            return Response.ok().entity(pgrs).build();
        } catch (Exception ex) {
            return Response.serverError().build();
        }
    }

    private static class POJOProgram {
        String name;
        String rules;

        public POJOProgram() {
        }

        public POJOProgram(String name, String rules) {
            this.name = name;
            this.rules = rules;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRules() {
            return rules;
        }

        public void setRules(String rules) {
            this.rules = rules;
        }
    }

}
