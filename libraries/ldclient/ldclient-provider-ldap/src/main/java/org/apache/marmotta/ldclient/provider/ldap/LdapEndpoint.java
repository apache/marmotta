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

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;

/**
 * Create an endpoint configuration for LDAP using the {@link LdapFoafProvider}.
 * 
 * @author Sergio Fernández
 */
public class LdapEndpoint extends Endpoint {
	
    /**
     * Create an endpoint configuration for LDAP
     *
     * @param name a name for the endpoint definition
     * @param host
     */
    public LdapEndpoint(String name, String host) {
        this(name, host, 389);
    }

    /**
     * Create an endpoint configuration for LDAP
     *
     * @param name a name for the endpoint definition
     * @param host
     * @param port
     */
    public LdapEndpoint(String name, String host, int port) {
        super(name, LdapFoafProvider.PROVIDER_NAME, buildPattern(host, port), buildPattern(host, port), 86400L);
    }

	private static String buildPattern(String host, int port) {
		return "ldap://" + host + ":" + port;
	}
    
}
