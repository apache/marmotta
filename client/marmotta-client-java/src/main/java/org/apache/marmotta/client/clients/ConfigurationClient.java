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
package org.apache.marmotta.client.clients;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.marmotta.client.ClientConfiguration;
import org.apache.marmotta.client.exception.MarmottaClientException;
import org.apache.marmotta.client.exception.NotFoundException;
import org.apache.marmotta.client.model.config.Configuration;
import org.apache.marmotta.client.util.HTTPUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A client that supports accessing the configuration webservice of the Apache Marmotta. May be used for
 * retrieving as well as changing properties.
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ConfigurationClient {
    private static Logger log = LoggerFactory.getLogger(ConfigurationClient.class);

    private static final String URL_CONFIG_SERVICE = "/config";


    private ClientConfiguration config;

    public ConfigurationClient(ClientConfiguration config) {
        this.config = config;
    }

    /**
     * Return a list of all configuration keys that are currently set in the Marmotta configuration.
     * @return
     * @throws IOException
     * @throws MarmottaClientException
     */
    public Set<String> listConfigurationKeys() throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        HttpGet get = new HttpGet(config.getMarmottaUri() + URL_CONFIG_SERVICE + "/list");
        get.setHeader("Accept", "application/json");

        try {
            
            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("configurations listed successfully");
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String,Map<String,Object>> result =
                            mapper.readValue(response.getEntity().getContent(),new TypeReference<Map<String,Map<String,Object>>>(){});
                    return result.keySet();
                default:
                    log.error("error retrieving list of configuration keys: {} {}",new Object[] {response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error retrieving list of configuration keys: "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Return a list of all configurations (keys and values) that are currently set in the Marmotta configuration.
     * @return
     * @throws IOException
     * @throws MarmottaClientException
     */
    public Set<Configuration> listConfigurations(String prefix) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_CONFIG_SERVICE + "/list" + (prefix != null? "?prefix="+ URLEncoder.encode(prefix,"utf-8") : "");

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader("Accept", "application/json");
        
        try {

            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("configurations listed successfully");
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String,Map<String,Object>> resultMap =
                            mapper.readValue(response.getEntity().getContent(),new TypeReference<Map<String,Map<String,Object>>>(){});
                    
                    Set<Configuration> result = new HashSet<Configuration>();
                    for(Map.Entry<String,Map<String,Object>> entry : resultMap.entrySet()) {
                        result.add(new Configuration(entry.getKey(),entry.getValue().get("value")));
                    }
                    return result;
                default:
                    log.error("error retrieving list of configuration keys: {} {}",new Object[] {response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error retrieving list of configuration keys: "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }


    /**
     * Return the configuration with the given key, or null if it does not exist
     * @param key
     * @return
     * @throws IOException
     * @throws MarmottaClientException
     */
    public Configuration getConfiguration(String key) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_CONFIG_SERVICE + "/data/" + URLEncoder.encode(key,"utf-8");

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader("Accept", "application/json");
        
        try {

            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("configuration {} retrieved successfully",key);
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String,Object> resultMap =
                            mapper.readValue(response.getEntity().getContent(),new TypeReference<Map<String,Object>>(){});

                    if(resultMap.isEmpty()) {
                        return null;
                    } else {
                        return new Configuration(key,resultMap.get(key));
                    }
                case 404:
                    log.info("configuration with key {} does not exist", key);
                    return null;
                default:
                    log.error("error retrieving configuration {}: {} {}",new Object[] {key,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error retrieving configuration "+key+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Update the configuration "key" with the given value. Value can be either a list of values or one of the
     * primitive types String, Boolean, Integer, Double
     * @param key
     * @param value
     * @throws IOException
     * @throws MarmottaClientException
     */
    public void setConfiguration(String key, final Object value) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_CONFIG_SERVICE + "/data/" + URLEncoder.encode(key,"utf-8");

        HttpPost post = new HttpPost(serviceUrl);
        post.setHeader("Content-Type", "application/json");
        ContentProducer cp = new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                ObjectMapper mapper = new ObjectMapper();
                if(value instanceof Collection) {
                    mapper.writeValue(outstream,value);
                } else {
                    mapper.writeValue(outstream, Collections.singletonList(value.toString()));
                }
            }
        };
        post.setEntity(new EntityTemplate(cp));
        
        try {

            HttpResponse response = httpClient.execute(post);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("configuration {} updated successfully",key);
                    break;
                case 404:
                    log.error("configuration with key {} does not exist",key);
                    throw new NotFoundException("configuration with key "+key+" does not exist");
                default:
                    log.error("error updating configuration {}: {} {}",new Object[] {key,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error updating configuration "+key+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            post.releaseConnection();
        }        
    }

    /**
     * Remove the configuration with the given key.
     *
     * @param key
     * @throws IOException
     * @throws MarmottaClientException
     */
    public void deleteConfiguration(String key) throws IOException, MarmottaClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_CONFIG_SERVICE + "/data/" + URLEncoder.encode(key,"utf-8");

        HttpDelete delete = new HttpDelete(serviceUrl);
            
        try {
            HttpResponse response = httpClient.execute(delete);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("configuration {} deleted successfully",key);
                    break;
                case 404:
                    log.error("configuration with key {} does not exist",key);
                    throw new NotFoundException("configuration with key "+key+" does not exist");
                default:
                    log.error("error deleting configuration {}: {} {}",new Object[] {key,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error deleting configuration "+key+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            delete.releaseConnection();
        }
    }

}
