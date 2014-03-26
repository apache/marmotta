/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.platform.backend.kiwi;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.jaxrs.exceptionmappers.ErrorResponse;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@Path("/storage-kiwi")
@ApplicationScoped
public class DatabaseWebService {


    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;


    @POST
    @Path("/ping")
    public Response pingDatabase(@QueryParam("type")String type,@QueryParam("url")String url,@QueryParam("user")String user,@QueryParam("pwd")String pwd) {
        if(type==null||url==null||user==null||pwd==null) {
            return Response.status(400).entity("one or more values are not defined").build();
        }

        //get driver
        String db_driver = configurationService.getStringConfiguration("database."+type+".driver");
        if(db_driver==null) {
            return ErrorResponse.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "driver for " + type + " not defined");
        }

        //try if type matches url
        if(!url.startsWith("jdbc:"+type)) {
            return ErrorResponse.errorResponse(Response.Status.INTERNAL_SERVER_ERROR,"database and url do not match properly");
        }

        //try to connect
        try {
            Class.forName(db_driver);
            Connection conn = DriverManager.getConnection(url, user, pwd);
            conn.close();
            return Response.ok().build();
        } catch (ClassNotFoundException e) {
            return ErrorResponse.errorResponse(Response.Status.INTERNAL_SERVER_ERROR,"Can't load driver ",e);
        } catch (SQLException e) {
            return ErrorResponse.errorResponse(Response.Status.INTERNAL_SERVER_ERROR,"Database access failed ",e);
        }
    }

    /**
     * Manually trigger the garbage collector.
     *
     * @return ok if successful, 500 if not
     * @HTTP 200 if garbage collection completed successfully
     * @HTTP 500 if there was an error while running garbage collection (see log)
     */
    @POST
    @Path("/gc")
    public Response garbageCollector() {
        log.info("Running triple store garbage collection after admin user request ...");
        try {
            sesameService.garbageCollect();

            return Response.ok().entity("garbage collection completed successfully").build();
        } catch(Exception ex) {
            log.error("Error while running garbage collection ...",ex);
            return ErrorResponse.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }



}
