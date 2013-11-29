package org.apache.marmotta.platform.core.webservices.logging;

import ch.qos.logback.classic.Level;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.logging.LoggingModule;
import org.apache.marmotta.platform.core.api.logging.LoggingService;
import org.apache.marmotta.platform.core.model.logging.ConsoleOutput;
import org.apache.marmotta.platform.core.model.logging.LogFileOutput;
import org.apache.marmotta.platform.core.model.logging.LoggingOutput;
import org.apache.marmotta.platform.core.model.logging.SyslogOutput;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listing and modifying logging configuration for appenders and logging modules. JSON format for each appender is:
 * <pre>
 *     {
 *         "type":    ("console" | "logfile" | "syslog" ),
 *         "id":      unique identifier
 *         "name":    human-readable name
 *         "pattern": pattern for logging output
 *         "level":   ("ERROR" | "WARN" | "INFO" | "DEBUG" | "TRACE" | "OFF" )
 *     }
 * </pre>
 *
 * Depending on the type of appender, the following additional fields need to be provided:
 * <ul>
 *     <li><strong>logfile</strong>
 *     <pre>
 *         "file":    name of the logfile
 *         "keep":    how many days to keep old logfiles
 *     </pre>
 *     </li>
 *     <li><strong>syslog</strong>
 *     <pre>
 *         "host":      name of server where the syslog daemon is running
 *         "facility":  facility to use for logging
 *     </pre>
 *     </li>
 * </ul>
 *
 * JSON format for logging modules is:
 * <pre>
 *     {
 *         "id":        unique identifier
 *         "name":      human-readable name
 *         "level":     ("ERROR" | "WARN" | "INFO" | "DEBUG" | "TRACE" | "OFF" )
 *         "appenders": list of appender ids to send logging to
 *         "packages":  list of package ids managed by module
 *     }
 * </pre>
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
@Path("/" + ConfigurationService.LOGGING_PATH)
public class LoggingWebService {

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private LoggingService loggingService;

    /**
     * Get a JSON list of all log appenders currently configured in the system using the JSON format described in the
     * header of the class
     *
     * @return JSON list
     */
    @GET
    @Path("/appenders")
    @Produces("application/json")
    public Response listAppenders() {
        return Response.ok(Lists.transform(loggingService.listOutputConfigurations(), new Function<LoggingOutput, Object>() {
            @Override
            public Object apply(LoggingOutput input) {
                return appenderToJSON(input);
            }
        })).build();
    }

    /**
     * Update all log appenders passed in the JSON list given in the body of the POST service request.
     *
     * @HTTP 200 appenders updated successfully
     * @HTTP 400 appender configuration invalid (e.g. not proper JSON)
     *
     * @return HTTP status 200 in case of success
     */
    @POST
    @Path("/appenders")
    @Consumes("application/json")
    public Response updateAppenders(@Context HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            //log.info(getContentData(request.getReader()));
            List<Map<String,Object>> values = mapper.readValue(request.getInputStream(), new TypeReference<ArrayList<HashMap<String,Object>>>(){});

            for(Map<String,Object> module : values) {
                if(module.get("id") != null) {
                    updateAppenderJSON((String) module.get("id"), module);
                }
            }
            return Response.ok("modules updated successfully").build();

        } catch (JsonMappingException | JsonParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("invalid JSON format: "+e.getMessage()).build();
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("could not read stream: "+e.getMessage()).build();
        }
    }


    /**
     * Get the configuration of the log appender with the given ID using the JSON format described in the header of
     * the class
     *
     * @HTTP 200 appender configuration returned successfully
     * @HTTP 404 appender not found
     *
     * @param id unique identifier of appender
     * @return JSON formatted representation of configuration
     */
    @GET
    @Path("/appenders/{id}")
    @Produces("application/json")
    public Response getAppender(@PathParam("id") String id) {
        LoggingOutput appender = loggingService.getOutputConfiguration(id);
        if(appender != null) {
            return Response.ok(appenderToJSON(appender)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


    /**
     * Create or update the appender with the given id, using the JSON description sent in the body of the request
     *
     * @HTTP 200 appender updated successfully
     * @HTTP 400 appender configuration invalid (e.g. not proper JSON)
     * @HTTP 404 appender not found
     *
     * @param id unique identifier of appender
     * @return HTTP status 200 in case of success
     */
    @POST
    @Path("/appenders/{id}")
    @Consumes("application/json")
    public Response updateAppender(@PathParam("id") String id, @Context HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            //log.info(getContentData(request.getReader()));
            Map<String,Object> values = mapper.readValue(request.getInputStream(), new TypeReference<HashMap<String,Object>>(){});

            updateAppenderJSON(id,values);

            return Response.ok().build();
        } catch (IllegalArgumentException | UnsupportedOperationException ex) {
            // thrown by Preconditions.checkArgument
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (JsonMappingException | JsonParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("invalid JSON format: "+e.getMessage()).build();
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("could not read stream: "+e.getMessage()).build();
        }
    }


    /**
     * List all modules currently available in the system as a JSON list using the JSON format described in the
     * header of this class
     *
     * @HTTP 200 in case the modules are listed properly
     *
     * @return JSON list of module descriptions
     */
    @GET
    @Path("/modules")
    @Produces("application/json")
    public Response listModules() {
        return Response.ok(Lists.transform(loggingService.listModules(), new Function<LoggingModule, Object>() {
            @Override
            public Object apply(LoggingModule input) {
                return moduleToJSON(input);
            }
        })).build();
    }

    /**
     * Update all modules passed as JSON list argument to the POST body of the service call. Only the fields
     * "level" and "appenders" can be updated for modules.
     *
     * @HTTP 200 modules updated successfully
     * @HTTP 400 module configuration invalid (e.g. not proper JSON)
     * @HTTP 404 module not found
     *
     * @return 200 OK in case modules have been updated successfully
     */
    @POST
    @Path("/modules")
    @Consumes("application/json")
    public Response updateModules(@Context HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            //log.info(getContentData(request.getReader()));
            List<Map<String,Object>> values = mapper.readValue(request.getInputStream(), new TypeReference<ArrayList<HashMap<String,Object>>>(){});

            boolean updated = false;
            for(Map<String,Object> module : values) {
                if(module.get("id") != null) {
                    updated = updateModuleJSON((String) module.get("id"), module) || updated;
                }
            }
            if(updated) {
                return Response.ok("modules updated successfully").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("one or more modules where not found").build();
            }

        } catch (JsonMappingException | JsonParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("invalid JSON format: "+e.getMessage()).build();
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("could not read stream: "+e.getMessage()).build();
        }
    }


    /**
     * Get the configuration of the logging module with the given id, using the JSON format described in the
     * header of this class.
     *
     * @HTTP 200 module found
     * @HTTP 404 module not found
     *
     * @param id unique logging module identifier
     * @return HTTP status 200 in case of success
     */
    @GET
    @Path("/modules/{id}")
    @Produces("application/json")
    public Response getModule(@PathParam("id") String id) {
        for(LoggingModule module : loggingService.listModules()) {
            if(StringUtils.equals(module.getId(), id)) {
                return Response.ok(moduleToJSON(module)).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }



    /**
     * Update the module with the given id, using the JSON description sent in the body of the request. Only the fields
     * "level" and "appenders" can be updated for modules.
     *
     * @HTTP 200 module updated successfully
     * @HTTP 400 module configuration invalid (e.g. not proper JSON)
     * @HTTP 404 module not found
     *
     * @param id unique logging module identifier
     * @return HTTP status 200 in case of success
     */
    @POST
    @Path("/modules/{id}")
    @Consumes("application/json")
    public Response updateModule(@PathParam("id") String id, @Context HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            //log.info(getContentData(request.getReader()));
            Map<String,Object> values = mapper.readValue(request.getInputStream(), new TypeReference<HashMap<String,Object>>(){});


            if(updateModuleJSON(id, values)) {
                return Response.ok("module updated").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (JsonMappingException | JsonParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("invalid JSON format: "+e.getMessage()).build();
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("could not read stream: "+e.getMessage()).build();
        }
    }


    /**
     * Update a module following the JSON specification given as argument.
     * @param spec
     * @return
     */
    private boolean updateModuleJSON(String id, Map<String,Object> spec) {
        String level     = (String) spec.get("level");
        List   appenders = (List)   spec.get("appenders");
        for(LoggingModule module : loggingService.listModules()) {
            if(StringUtils.equals(module.getId(), id)) {
                if(level != null) {
                    module.setCurrentLevel(Level.toLevel(level));
                }
                if(appenders != null) {
                    module.setLoggingOutputIds(appenders);
                }

                return true;
            }
        }
        return false;
    }


    private void updateAppenderJSON(String id, Map<String,Object> values) {
        String type  = (String) values.get("type");
        String name  = (String) values.get("name");
        String level   = (String) values.get("level");
        String pattern = (String) values.get("pattern");

        LoggingOutput appender = loggingService.getOutputConfiguration(id);
        if(appender == null) {
            // type information required
            Preconditions.checkArgument(type != null, "appender type was not given");
            Preconditions.checkArgument(name != null, "appender name was not given");

            if("logfile".equals(type)) {
                String file = (String) values.get("file");

                Preconditions.checkArgument(file != null, "logfile name was not given");

                appender = loggingService.createLogFileOutput(id,name,file);
            } else if("syslog".equals(type)) {
                String host = (String) values.get("host");

                Preconditions.checkArgument(host != null, "syslog host was not given");

                appender = loggingService.createSyslogOutput(id,name);
            } else {
                throw new UnsupportedOperationException("new appenders of type "+type+" not supported");
            }
        }

        appender.setName(name);

        if(level != null) {
            appender.setMaxLevel(Level.toLevel(level));
        }
        if(pattern != null) {
            appender.setPattern(pattern);
        }
        if(values.get("file") != null && appender instanceof LogFileOutput) {
            ((LogFileOutput) appender).setFileName((String) values.get("file"));
        }
        if(values.get("keep") != null && appender instanceof LogFileOutput) {
            ((LogFileOutput) appender).setKeepDays(Integer.parseInt(values.get("keep").toString()));
        }
        if(values.get("host") != null && appender instanceof SyslogOutput) {
            ((SyslogOutput) appender).setHostName((String) values.get("host"));
        }
        if(values.get("facility") != null && appender instanceof SyslogOutput) {
            ((SyslogOutput) appender).setFacility((String) values.get("facility"));
        }

    }


    private static Map<String,Object> appenderToJSON(LoggingOutput out) {
        Map<String,Object> result = new HashMap<>();

        if(out instanceof SyslogOutput) {
            result.put("type", "syslog");
            result.put("host", ((SyslogOutput) out).getHostName());
            result.put("facility", ((SyslogOutput) out).getFacility());
        } else if(out instanceof ConsoleOutput) {
            result.put("type", "console");
        } else if(out instanceof LogFileOutput) {
            result.put("type", "logfile");
            result.put("file", ((LogFileOutput) out).getFileName());
            result.put("keep", ((LogFileOutput) out).getKeepDays());
        }

        result.put("level", out.getMaxLevel().toString());
        result.put("pattern", out.getPattern());
        result.put("name", out.getName());
        result.put("id", out.getId());

        return result;
    }


    private static Map<String,Object> moduleToJSON(LoggingModule module) {
        Map<String,Object> result = new HashMap<>();

        result.put("id", module.getId());
        result.put("name", module.getName());
        result.put("level", module.getCurrentLevel().toString());
        result.put("appenders", module.getLoggingOutputIds());
        result.put("packages", module.getPackages());

        return result;
    }
}
