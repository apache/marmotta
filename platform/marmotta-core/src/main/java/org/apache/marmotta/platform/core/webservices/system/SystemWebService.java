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
package org.apache.marmotta.platform.core.webservices.system;

import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.jaxrs.exceptionmappers.ErrorResponse;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Perform various system actions like restarting database access or rebuilding indexes.
 */
@ApplicationScoped
@Path("/system")
public class SystemWebService {

    @Inject
    private Logger log;

    @Inject
    private SesameService sesameService;

    /**
     * Reinitialise the database configuration. Will close the database connection and
     * reopen it, possibly with new settings.
     *
     * @return ok if successful, 500 if not
     * @HTTP 200 if database was reinitialised successfully
     * @HTTP 500 if there was an error while reinitialising database (see log)
     */
    @POST
    @Path("/database/reinit")
    public Response reinitDatabase() {
        log.info("Reinitialising database after admin user request ...");
        try {
            sesameService.shutdown();
            sesameService.initialise();

            return Response.ok().entity("database reinitialised successfully").build();
        } catch(Exception ex) {
            log.error("Error while reinitalising database ...",ex);
            return ErrorResponse.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }
}
