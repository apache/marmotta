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
package org.apache.marmotta.platform.core.webservices.io;

import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskInfo;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A webservice offering functionality to import data from the KiWi knowledge base.
 * <p/>
 * User: sschaffe
 */
@ApplicationScoped
@Path("/import")
public class ImportWebService {

    @Inject
    private Logger log;

    @Inject
    private ImportService importService;

    @Inject
    private ContextService contextService;

    @Inject
    private TaskManagerService taskManagerService;

    @Inject
    private UserService userService;

    private static final ThreadGroup IMPORTER_THREADS = new ThreadGroup("asynchronous imports");
    private static final String TASK_GROUP_NAME = "Imports";

    /**
     * Return a set of all mime types that are acceptable by the importer.
     * @return a set of all mime types that are acceptable by the importer.
     */
    @GET
    @Path("/types")
    @Produces("application/json")
    public List<String> getTypes(@QueryParam("filename") String filename) {
        if(filename == null)
            return new ArrayList<String>(importService.getAcceptTypes());
        else {
            List<String> result = new ArrayList<String>();
            RDFFormat format = Rio.getParserFormatForFileName(filename);
            if(format != null) {
                result.addAll(format.getMIMETypes());
            }
            return result;
        }
    }

    /**
     * Upload content and import it into the LMF system. The importer is selected based on the Content-Type header
     * of the HTTP request. Calling the service spawns a separate asynchronous thread. Its status can be queried by
     * calling the /status webservice.
     *
     * @param type the content type of the uploaded content
     * @param request the request data of the uploaded file
     * @return OK after starting a thread for importing the data, or error if the import cannot be started
     * @HTTP 200 if the import was successfully started
     * @HTTP 412 if the content-type header is not present or contains unsupported mime types
     */
    @POST
    @Path("/upload")
    public Response uploadData(@HeaderParam("Content-Type") String type, @Context HttpServletRequest request, @QueryParam("context") String context_string) throws IOException, MarmottaImportException {
        if(type != null && type.lastIndexOf(';') >= 0) {
            type = type.substring(0,type.lastIndexOf(';'));
        }
        if (type == null || !importService.getAcceptTypes().contains(type)) return Response.status(412).entity("define a valid content-type (types: "+importService.getAcceptTypes()+")").build();
        final String finalType = type;
        final InputStream in = request.getInputStream();

        Task t = taskManagerService.createTask(String.format("Upload-Import from %s (%s)", request.getRemoteHost(), finalType), TASK_GROUP_NAME);
        t.updateMessage("preparing import...");
        t.updateDetailMessage("type", finalType);
        try {
            //create context
            URI context = getContext(context_string);
            if (context != null) {
                t.updateDetailMessage("context", context.toString());
            }

            t.updateMessage("importing data...");
            importService.importData(in,finalType, userService.getCurrentUser(), context);
            t.updateMessage("import complete");

            return Response.ok().entity("import of content successful\n").build();
        } catch(Exception ex) {
            log.error("error while importing", ex);
            return Response.status(500).entity("error while importing: " + ex.getMessage()).build();
        } finally {
            taskManagerService.endTask(t);
        }
    }

    /**
     * Fetch content from an external resource and import it into the LMF system. The importer is selected based on
     * the Content-Type header of the HTTP request. Calling the service spawns a separate asynchronous thread. Its
     * status can be queried by calling the /status webservice.
     *
     * @param type the content type of the uploaded content
     * @param url an optional URL of a remote resource to import
     * @return OK if the import was successfully started
     * @HTTP 200 if the import was successfully started
     * @HTTP 400 if the URL argument is not valid
     * @HTTP 412 if the content-type header is not present or contains unsupported mime types
     * @HTTP 502 if a connection to the URL of the external source cannot be established
     */
    @POST
    @Path("/external")
    public Response externalData(@HeaderParam("Content-Type") String type, @QueryParam("url") String url, @QueryParam("context") String context_string) throws IOException, MarmottaImportException {
        try {
            log.debug("Received 'external' request for {} with {}%n", type, url);
            if(type != null && type.lastIndexOf(';') >= 0) {
                type = type.substring(0,type.lastIndexOf(';'));
            }
            if(type==null || !importService.getAcceptTypes().contains(type)) return Response.status(412).entity("define a valid content-type (types: "+importService.getAcceptTypes()+")").build();
            final URL finalUrl = new URL(url);
            final URI context = getContext(context_string);

            try {
                URLConnection con = finalUrl.openConnection();
                con.connect();
            } catch(IOException ex) {
                return Response.status(502).entity("the URL passed as argument cannot be retrieved").build();
            }

            final String finalType = type;
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    Task task = taskManagerService.createTask("Import from external source", TASK_GROUP_NAME);
                    task.updateDetailMessage("source", finalUrl.toExternalForm());
                    task.updateDetailMessage("type", finalType);
                    if (context != null) {
                        task.updateDetailMessage("context", context.toString());
                    }
                    try {
                        importService.importData(finalUrl,finalType,userService.getCurrentUser(),context);
                    } catch(Exception e) {
                        log.error("exception while asynchronously importing data",e);
                    } finally {
                        taskManagerService.endTask(task);
                    }
                }
            };

            Thread t = new Thread(IMPORTER_THREADS, r);
            t.setName("Import(start:" + new Date() + ",url:" + url + ")");
            t.setDaemon(true);
            t.start();

            return Response.ok().entity(String.format("{\"tname\":\"%s\"}", t.getName())).build();
        } catch(MalformedURLException ex) {
            return Response.status(400).entity("the URL passed as argument is not valid").build();
        }
    }

    /**
     * Stop the importer thread with the ID passed as query argument.
     *
     * @param tname the thread ID of the importer thread to stop
     * @return OK if thread has been stopped or no longer exists
     * @throws UnsupportedEncodingException
     */
    @DELETE
    @Path("/cancel")
    public Response stopArticleXMLImport(@QueryParam("tname")String tname) throws UnsupportedEncodingException {
        tname = URLDecoder.decode(tname, "utf-8");
        Thread[] threads = new Thread[IMPORTER_THREADS.activeCount()];
        IMPORTER_THREADS.enumerate(threads);
        for(Thread t : threads) {
            if(t!=null&&t.getName().equals(tname)) {
                //TODO
                t.interrupt();
                return Response.ok().build();
            }
        }
        return Response.ok().entity("thread does not exist or is already stopped").build();
    }

    /**
     * Get the status of the importer thread with the ID passed as query argument.
     *
     * @param tname the thread ID of the importer thread to query
     * @return the status of the importer thread as a JSON object
     * @throws UnsupportedEncodingException
     */
    @GET
    @Path("/status")
    @Produces("application/json")
    public Status isActiveImport(@QueryParam("tname")String tname) throws UnsupportedEncodingException {
        tname = URLDecoder.decode(tname,"utf-8");
        Thread[] threads = new Thread[IMPORTER_THREADS.activeCount()];
        IMPORTER_THREADS.enumerate(threads);
        for(Thread t : threads) {
            if(t!=null&&t.getName().equals(tname)) {
                if(t.isAlive())
                    return new Status(tname,true,"success","import is running");
                else if(t.isInterrupted()) return new Status(tname,false,"error","import was not successful");
                return new Status(tname,false,"success","import was successful");
            }
        }
        return new Status(tname,false,"undefined","thread does not exist or is already stopped");
    }

    @GET
    @Path("/list")
    @Produces("application/json")
    public List<TaskInfo> listRunningImport() throws UnsupportedEncodingException {
        return taskManagerService.getTasksByGroup().get(TASK_GROUP_NAME);
        // List<String> running = new LinkedList<String>();
        // for (Task t : taskManagerService.listTasks(TASK_GROUP_NAME)) {
        // running.add(String.format("[%d] %s (%s) %.2f%% ETA: %tF %<tT",
        // t.getId(), t.getName(), t.getStatus(), 100 * t.getRelProgress(),
        // t.getETA()));
        // }
        // return running;
    }

    private URI getContext(String context_string) {
        if(context_string != null)
            return contextService.createContext(context_string);
        else
            return contextService.getDefaultContext();
    }

    protected static class Status {
        boolean isRunning;
        String status;
        String message;
        String tname;

        Status(String tname, boolean running, String status, String message) {
            this.tname = tname;
            isRunning = running;
            this.message = message;
            this.status = status;
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

}
