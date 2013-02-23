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
package at.newmedialab.lmf.client.clients;

import at.newmedialab.lmf.client.ClientConfiguration;
import at.newmedialab.lmf.client.exception.LMFClientException;
import at.newmedialab.lmf.client.exception.NotFoundException;
import at.newmedialab.lmf.client.util.HTTPUtil;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * A client allowing to retrieve and configure the SOLR cores that are registered in the LMF server.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class CoresClient {

    private static Logger log = LoggerFactory.getLogger(CoresClient.class);

    private static final String URL_CORES_SERVICE  = "/solr/cores";

    private ClientConfiguration config;

    public CoresClient(ClientConfiguration config) {
        this.config = config;
    }


    /**
     * Retrieve a list of all core names registered and activated in the LMF server.
     * 
     * @return
     * @throws IOException
     * @throws LMFClientException
     */
    public List<String> listCores() throws IOException, LMFClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getLmfUri() + URL_CORES_SERVICE;

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader("Accept", "application/json");
        
        try {

            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("active cores retrieved successfully");
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                    List<String> result =
                            mapper.readValue(response.getEntity().getContent(),new TypeReference<List<String>>(){});

                    return result;

                default:
                    log.error("error retrieving active cores: {} {}",new Object[] {response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new LMFClientException("error retrieving active cores: "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Return the LDPath program configured for the core with the name passed as argument. 
     * 
     * Note that this library provides no further functionality for evaluating LDPath programs. You may use the
     * separate LDPath libraries at http://code.google.com/p/ldpath/.
     * 
     * @param coreName name of the core for which to retrieve the LDPath program
     * @return
     * @throws IOException
     * @throws LMFClientException
     */
    public String getCoreConfiguration(String coreName) throws IOException, LMFClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getLmfUri() + URL_CORES_SERVICE + "/" + URLEncoder.encode(coreName,"utf-8");

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader("Accept", "text/plain");
        
        try {

            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("core {} retrieved successfully",coreName);
                    return CharStreams.toString(new InputStreamReader(response.getEntity().getContent(),"utf-8"));
                default:
                    log.error("error retrieving core {}: {} {}",new Object[] {coreName,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new LMFClientException("error retrieving core "+coreName+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Create the core configuration for the given core using the LDPath program passed  as argument.
     *
     * Note that this library provides no further functionality for evaluating LDPath programs. You may use the
     * separate LDPath libraries at http://code.google.com/p/ldpath/.
     *
     * @param coreName     the name of the core to update
     * @param coreProgram  the LDPath program to use as core configuration
     * @throws IOException
     * @throws LMFClientException
     */
    public void createCoreConfiguration(String coreName, final String coreProgram) throws IOException, LMFClientException {
        createCoreConfiguration(coreName, new ByteArrayInputStream(coreProgram.getBytes("utf-8")));
    }


    /**
     * Update the core configuration for the given core using the LDPath program passed  as argument.
     *
     * Note that this library provides no further functionality for evaluating LDPath programs. You may use the
     * separate LDPath libraries at http://code.google.com/p/ldpath/.
     *
     * @param coreName     the name of the core to update
     * @param coreProgram  InputStream providing the LDPath program to use as core configuration
     * @throws IOException
     * @throws LMFClientException
     */
    public void createCoreConfiguration(String coreName, final InputStream coreProgram) throws IOException, LMFClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getLmfUri() + URL_CORES_SERVICE + "/" + URLEncoder.encode(coreName,"utf-8");

        HttpPost post = new HttpPost(serviceUrl);
        post.setHeader("Content-Type", "text/plain");
        ContentProducer cp = new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                ByteStreams.copy(coreProgram,outstream);
            }
        };
        post.setEntity(new EntityTemplate(cp));
        
        try {
                
            HttpResponse response = httpClient.execute(post);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("core {} updated successfully",coreName);
                    break;
                default:
                    log.error("error updating core {}: {} {}",new Object[] {coreName,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new LMFClientException("error updating core "+coreName+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            post.releaseConnection();
        }
    }


    /**
     * Set/update the core configuration for the given core using the LDPath program passed  as argument.
     *
     * Note that this library provides no further functionality for evaluating LDPath programs. You may use the
     * separate LDPath libraries at http://code.google.com/p/ldpath/.
     *
     * @param coreName     the name of the core to update
     * @param coreProgram  the LDPath program to use as core configuration
     * @throws IOException
     * @throws LMFClientException
     */
    public void updateCoreConfiguration(String coreName, final String coreProgram) throws IOException, LMFClientException {
        updateCoreConfiguration(coreName,new ByteArrayInputStream(coreProgram.getBytes("utf-8")));
    }

    /**
     * Update the core configuration for the given core using the LDPath program passed  as argument.
     *
     * Note that this library provides no further functionality for evaluating LDPath programs. You may use the
     * separate LDPath libraries at http://code.google.com/p/ldpath/.
     *
     * @param coreName     the name of the core to update
     * @param coreProgram  InputStream providing the LDPath program to use as core configuration
     * @throws IOException
     * @throws LMFClientException
     */
    public void updateCoreConfiguration(String coreName, final InputStream coreProgram) throws IOException, LMFClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getLmfUri() + URL_CORES_SERVICE + "/" + URLEncoder.encode(coreName,"utf-8");

        HttpPut put = new HttpPut(serviceUrl);
        put.setHeader("Content-Type", "text/plain");
        ContentProducer cp = new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                ByteStreams.copy(coreProgram,outstream);
            }
        };
        put.setEntity(new EntityTemplate(cp));
        
        try {
                
            HttpResponse response = httpClient.execute(put);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("core {} updated successfully",coreName);
                    break;
                default:
                    log.error("error updating core {}: {} {}",new Object[] {coreName,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new LMFClientException("error updating core "+coreName+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            put.releaseConnection();
        }
    }


    /**
     * Remove the core with the name passed as argument.
     *
     * @param coreName   name of the core to delete
     * @throws IOException
     * @throws NotFoundException  in case the core with this name does not exist
     * @throws LMFClientException
     */
    public void deleteCore(String coreName) throws IOException, LMFClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getLmfUri() + URL_CORES_SERVICE + "/" + URLEncoder.encode(coreName,"utf-8");

        HttpDelete delete = new HttpDelete(serviceUrl);
        
        try {
                
            HttpResponse response = httpClient.execute(delete);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("core {} deleted successfully",coreName);
                    break;
                case 404:
                    log.error("core {} does not exist and could not be deleted", coreName);
                    throw new NotFoundException("core "+coreName+" does not exist and could not be deleted");
                default:
                    log.error("error deleting core {}: {} {}",new Object[] {coreName,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new LMFClientException("error updating core "+coreName+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            delete.releaseConnection();
        }
    }
    
}
