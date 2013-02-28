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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.marmotta.client.ClientConfiguration;
import org.apache.marmotta.client.util.HTTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Context Client 
 * 	
 * @author Sergio Fernández
 *
 */
public class ContextClient {
	
    private static Logger log = LoggerFactory.getLogger(ContextClient.class);

    private ClientConfiguration config;

    public ContextClient(ClientConfiguration config) {
        this.config = config;
    }
    
    public boolean delete(String uri) {
    	boolean result = false;
        HttpClient httpClient = HTTPUtil.createClient(config);
       
        HttpDelete delete = new HttpDelete(uri);
        
        try {
                
            HttpResponse response = httpClient.execute(delete);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug(uri + "cleanned");
                    result = true;
                    break;
                case 404:
                    log.error(uri + " is not a suitable context");
                    result = false;
                default:
                    log.error("error cleanning context: {} {}",new Object[] {response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});                
            }
        
        } catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
			result = false;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			result = false;
		} finally {
		    delete.releaseConnection();
        }
        return result;

    }

}
