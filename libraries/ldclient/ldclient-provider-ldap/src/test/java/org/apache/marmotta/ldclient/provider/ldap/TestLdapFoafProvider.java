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
package org.apache.marmotta.ldclient.provider.ldap;

import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.test.helper.TestLDClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;

/**
 * Tests the LdapFoafProvider
 * 
 * @author Sergio Fernández
 *
 */
public class TestLdapFoafProvider {
	
	//list at http://www.emailman.com/ldap/public.html
	
    @Test
    @Ignore("wip")
    public void testBostonUniversity() throws Exception {   
    	genericTestImplementation(new LdapEndpoint("Boston University (LDAP)", "ldap.bu.edu", 389), 
				  				  "cn=Foo,o=Boston University,c=us");
    }
    
    @Test
    @Ignore("unreacheable")
    public void testSalzburgResearch() throws Exception {    	
    	genericTestImplementation(new LdapEndpoint("Salzburg Research (LDAP)", "ldap.bu.edu", 389), 
    							  "cn=Sergio Fernandez,ou=USERS,ou=SRFG,dc=salzburgresearch,dc=at");
    }
    
    private void genericTestImplementation(Endpoint endpoint, String resource) throws Exception {    	
        ClientConfiguration config = new ClientConfiguration();
        config.addEndpoint(endpoint); 
        LDClientService ldclient = new TestLDClient(new LDClient(config));
        ClientResponse response = ldclient.retrieveResource(resource);
        RepositoryConnection connection = ModelCommons.asRepository(response.getData()).getConnection();
        connection.begin();
        Assert.assertTrue(connection.size() > 0);
        connection.commit();
        connection.close();
    }

}
