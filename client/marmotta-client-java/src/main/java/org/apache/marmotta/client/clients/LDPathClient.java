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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.marmotta.client.ClientConfiguration;
import org.apache.marmotta.client.exception.ContentFormatException;
import org.apache.marmotta.client.exception.MarmottaClientException;
import org.apache.marmotta.client.exception.NotFoundException;
import org.apache.marmotta.client.model.rdf.RDFNode;
import org.apache.marmotta.client.util.HTTPUtil;
import org.apache.marmotta.client.util.RDFJSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.net.HttpHeaders.ACCEPT;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LDPathClient {

    private static Logger log = LoggerFactory.getLogger(LDPathClient.class);

    private static final String URL_PATH_SERVICE  = "/ldpath/path";
    private static final String URL_PROGRAM_SERVICE = "/ldpath/program";


    private ClientConfiguration config;

    public LDPathClient(ClientConfiguration config) {
        this.config = config;
    }


    /**
     * Evaluate the path query passed as second argument, starting at the resource with the uri given as first argument.
     * Returns a List of RDFNode objects.
     *
     *
     * @param uri  the uri of the resource where to start the path evaluation
     * @param path the path to evaluate
     * @return a list of RDFNodes representing the result of the path evaluation as returned by the server
     * @throws MarmottaClientException
     * @throws IOException
     */
    public List<RDFNode> evaluatePath(String uri, String path) throws MarmottaClientException, IOException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_PATH_SERVICE + "?path=" + URLEncoder.encode(path, "utf-8")
                                                                  + "&uri="  + URLEncoder.encode(uri, "utf-8");

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader(ACCEPT, "application/json");
        
        try {

            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("LDPath Path Query {} evaluated successfully",path);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                    List<Map<String,String>> serverResult =
                            mapper.readValue(response.getEntity().getContent(),new TypeReference<List<Map<String,String>>>(){});

                    
                    List<RDFNode> result = new ArrayList<RDFNode>();
                    for(Map<String,String> value : serverResult) {
                        result.add(RDFJSONParser.parseRDFJSONNode(value));
                    }
                    return result;
                case 400:
                    log.error("the server did not accept the uri ({}) or path ({}) arguments",uri,path);
                    throw new ContentFormatException("the server did not accept the uri ("+uri+") or path ("+path+") arguments");
                case 404:
                    log.error("the resource with URI {} does not exist on the server",uri);
                    throw new NotFoundException("the resource with URI "+uri+" does not exist on the server");
                default:
                    log.error("error evaluating LDPath Path Query {}: {} {}",new Object[] {path,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error evaluating LDPath Path Query "+path+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }

    }
    
    
    public Map<String,List<RDFNode>> evaluateProgram(String uri, String program) throws MarmottaClientException, IOException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getMarmottaUri() + URL_PROGRAM_SERVICE + "?program=" + URLEncoder.encode(program, "utf-8")
                                                                                   + "&uri="  + URLEncoder.encode(uri, "utf-8");

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader(ACCEPT, "application/json");
        
        try {

            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("LDPath Program Query evaluated successfully:\n{}",program);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                    Map<String,List<Map<String,String>>> serverResult =
                            mapper.readValue(response.getEntity().getContent(),new TypeReference<Map<String,List<Map<String,String>>>>(){});


                    Map<String,List<RDFNode>> result = new HashMap<String, List<RDFNode>>();
                    for(Map.Entry<String,List<Map<String,String>>> field : serverResult.entrySet()) {
                        List<RDFNode> row = new ArrayList<RDFNode>();
                        for(Map<String,String> node : field.getValue()) {
                            row.add(RDFJSONParser.parseRDFJSONNode(node));
                        }
                        result.put(field.getKey(),row);
                    }
                    return result;
                case 400:
                    log.error("the server did not accept the uri ({}) or program ({}) arguments",uri,program);
                    throw new ContentFormatException("the server did not accept the uri ("+uri+") or program ("+program+") arguments");
                case 404:
                    log.error("the resource with URI {} does not exist on the server",uri);
                    throw new NotFoundException("the resource with URI "+uri+" does not exist on the server");
                default:
                    log.error("error evaluating LDPath Program Query: {} {}",new Object[] {response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new MarmottaClientException("error evaluating LDPath Program Query: "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }

}
