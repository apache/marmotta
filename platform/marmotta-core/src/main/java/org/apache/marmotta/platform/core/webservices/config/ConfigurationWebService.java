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
package org.apache.marmotta.platform.core.webservices.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.ConversionException;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *  Manage the system configuration of the Apache Marmotta Server. Provides methods for displaying and updating the configuration
 *  values.
 */
@ApplicationScoped
@Path("/config")
public class ConfigurationWebService {

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private Logger log;

    /**
     * Retrieve all entries in the system configuration as key-value pairs.
     *
     *
     * @return a map mapping all configuration keys to the respective string or list values
     * @HTTP 200 when the list of settings is retrieved successfully
     */
    @GET
    @Path("/list")
    @Produces("application/json")
    public Map<String,Map<String,Object>> listConfiguration(@QueryParam("prefix")String prefix) {
        HashMap<String,Map<String,Object>> result = new HashMap<String,Map<String,Object>>();
        if(prefix==null) {
            for(String key : configurationService.listConfigurationKeys()) {
                result.put(key, buildConfigurationMap(key));
            }
        } else {
            for(String key : configurationService.listConfigurationKeys(prefix)) {
                result.put(key, buildConfigurationMap(key));
            }
        }
        return result;
    }

    public Map<String,Object> buildConfigurationMap(String key) {
        Map<String,Object> config = new HashMap<String, Object>();
        config.put("comment",configurationService.getComment(key));
        config.put("type",configurationService.getType(key));
        config.put("value",configurationService.getConfiguration(key));
        if (config.get("type") != null) {
            try {
                if (((String) config.get("type")).startsWith("java.lang.Integer")) {
                    config.put("value", configurationService.getIntConfiguration(key));
                } else if (((String) config.get("type")).startsWith("java.lang.Boolean")) {
                    config.put("value", configurationService.getBooleanConfiguration(key));
                } else if (((String) config.get("type")).startsWith("java.lang.Double")) {
                    config.put("value", configurationService.getDoubleConfiguration(key));
                } else if (((String) config.get("type")).startsWith("java.lang.Long")) {
                    config.put("value", configurationService.getLongConfiguration(key));
                }
            } catch (ConversionException e) {
                log.warn("key {} cannot be converted to given type {}", key, config.get("type"));
            }
        }
        return config;
    }

    /**
     * stores a list of configuration
     * @HTTP 200 if the configuration was set
     * @HTTP 400 if the input sent in the body could not be parsed
     * @param request
     * @return HTTP 200 or 400
     */
    @POST
    @Path("/list")
    @Produces("application/json")
    public Response setListConfiguration(@Context HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            //log.info(getContentData(request.getReader()));
            Map<String,Object> values = mapper.readValue(request.getInputStream(), new TypeReference<HashMap<String,Object>>(){});
            configurationService.setConfigurations(values);
        } catch (IOException e) {
            return Response.serverError().build();
        }
        return Response.ok().build();
    }

    /**
     * Return the string or list value for the configuration key passed as argument.
     *
     * @param key the configuration key for which to return the value
     * @return the value of the requested configuration
     * @HTTP 200 if the configuration key exists
     * @HTTP 404 if the configuration key does not exist
     */
    @GET
    @Path("/data/{key}")
    @Produces("application/json")
    public Response getConfiguration(@PathParam("key") String key) {
        Object value = configurationService.getConfiguration(key);
        if(value != null) {
            HashMap<String,Object> result = new HashMap<String,Object>();
            result.put(key, value);
            return Response.status(200).entity(result).build();
        } else
            return Response.status(404).build();
    }

    /**
     * Return the description for the configuration key passed as argument.
     *
     * @param key the configuration key for which to return the value
     * @return the description of the requested configuration
     * @HTTP 200 if the configuration key exists
     * @HTTP 404 if the configuration key does not exist
     */
    @GET
    @Path("/comment/{key}")
    @Produces("application/json")
    public Response getConfigurationComment(@PathParam("key") String key) {
        String value = configurationService.getComment(key);
        if(value != null) return Response.status(200).entity(Collections.singletonList(value)).build();
        else
            return Response.status(404).build();
    }


    /**
     * Return the data type for the configuration key passed as argument.
     *
     * @param key the configuration key for which to return the value
     * @return the data type of the requested configuration
     * @HTTP 200 if the configuration key exists
     * @HTTP 404 if the configuration key does not exist
     */
    @GET
    @Path("/type/{key}")
    @Produces("application/json")
    public Response getConfigurationType(@PathParam("key") String key) {
        String value = configurationService.getType(key);
        if(value != null) return Response.status(200).entity(Collections.singletonList(value)).build();
        else
            return Response.status(404).build();
    }

    /**
     * Set the configuration with the key passed in the path argument. The input has to be a
     * JSON list of String values sent in the body of the request
     *
     * @param key the configuration key to set
     * @param request the request body as JSON list
     * @return OK if the value was set correctly, ERROR if parsing failed
     * @HTTP 200 if the configuration was set
     * @HTTP 400 if the input sent in the body could not be parsed
     */
    @POST
    @Path("/data/{key}")
    @Consumes("application/json")
    public Response setConfiguration(@PathParam("key") String key, @QueryParam("type") String type, @QueryParam("comment") String comment, @Context HttpServletRequest request) {
        try {
            //log.info(getContentData(request.getReader()));
            ObjectMapper mapper = new ObjectMapper();
            List<String> values = mapper.readValue(request.getInputStream(), new TypeReference<ArrayList<String>>(){});
            configurationService.setConfiguration(key,values);
            if(type!=null) configurationService.setType(key,type);
            if(comment!=null) configurationService.setComment(key,comment);
            return Response.status(200).build();
        } catch (JsonMappingException e) {
            log.error("cannot parse input into json",e);
        } catch (JsonParseException e) {
            log.error("cannot parse input into json",e);
        } catch (IOException e) {
            log.error("cannot parse input into json",e);
        }
        return Response.status(400).build(); // bad request
    }

    /**
     * Delete the configuration with the key passed in the path argument.
     * @param key the configuration to remove
     * @return 200 (OK) if the configuration was removed successfully, 500 (server error) otherwise
     * @HTTP 200 if the configuration was removed successfully
     * @HTTP 500 if there was an error while removing the configuration
     */
    @DELETE
    @Path("/data/{key}")
    public Response deleteConfiguration(@PathParam("key") String key) {
        if(configurationService.getConfiguration(key) != null) {
            try {
                configurationService.removeConfiguration(key);
                return Response.status(200).build();
            } catch (Exception e) {
                log.error("cannot delete configuration",e);
                return Response.status(500).build();
            }
        } else
            return Response.status(404).build();
    }

    public String getContent(BufferedReader r) {
        String s;StringBuffer b = new StringBuffer();
        try {
            while((s = r.readLine()) != null) {
                b.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return b.toString();
    }

}
