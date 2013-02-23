/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
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
 * Perform various system actions like restarting database access or rebuilding indexes.
 */
@ApplicationScoped
@Path("/system")
public class SystemWebService {

    @Inject
    private Logger log;

	@Inject
    private ConfigurationService configurationService;

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
            return Response.status(500).entity("error while reinitialising database").build();
        }
    }

	@POST
    @Path("/database/ping")
	public Response pingDatabase(@QueryParam("type")String type,@QueryParam("url")String url,@QueryParam("user")String user,@QueryParam("pwd")String pwd) {
		if(type==null||url==null||user==null||pwd==null) {
			return Response.status(400).entity("one or more values are not defined").build();
		}

		//get driver
		String db_driver = configurationService.getStringConfiguration("database."+type+".driver");
		if(db_driver==null) {
			return Response.serverError().entity("driver for "+type+" not defined").build();
		}

		//try if type matches url
		if(!url.startsWith("jdbc:"+type)) {
			return Response.serverError().entity("database and url do not match properly").build();
		}

		//try to connect
		try {
			Class.forName(db_driver);
			Connection conn = DriverManager.getConnection(url,user,pwd);
			conn.close();
			return Response.ok().build();
		} catch (ClassNotFoundException e) {
			return Response.serverError().entity("Can't load driver " + e).build();
		} catch (SQLException e) {
      		return Response.serverError().entity("Database access failed " + e).build();
    	}
	}



}
