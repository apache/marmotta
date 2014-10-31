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
package org.apache.marmotta.client;

import org.apache.http.conn.HttpClientConnectionManager;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ClientConfiguration {

	/**
	 * The URI at which the Marmotta installation can be located
	 */
	private String marmottaUri;

	/**
	 * (Optional) user to authenticate with the Marmotta system
	 */
	private String marmottaUser;

	/**
	 * (Optional) password to authenticate with the Marmotta system
	 */
	private String marmottaPassword;

	/**
	 * (Optional) context in the Marmotta system
	 */
	private String marmottaContext;

	/**
	 * Socket timeout for established HTTP connections. Connection will be
	 * closed afterwards. Default: 60 seconds.
	 */
	private int soTimeout = 60000;

	/**
	 * Connection timeout for opening HTTP connections. If idle for this time,
	 * will be closed. Default: 10 seconds.
	 */
	private int connectionTimeout = 10000;
	
	private HttpClientConnectionManager conectionManager;

	public ClientConfiguration(String marmottaUri) {
        if (marmottaUri.endsWith("/")) {
		    this.marmottaUri = marmottaUri.substring(0, marmottaUri.length() - 1);
        } else {
		    this.marmottaUri = marmottaUri;
        }
	}

	public ClientConfiguration(String marmottaUri, String marmottaUser, String marmottaPassword) {
		this.marmottaUri = marmottaUri;
		this.marmottaUser = marmottaUser;
		this.marmottaPassword = marmottaPassword;
	}

	public String getMarmottaUri() {
		return marmottaUri;
	}

	public void setMarmottaUri(String marmottaUri) {
		this.marmottaUri = marmottaUri;
	}

	public String getMarmottaUser() {
		return marmottaUser;
	}

	public void setMarmottaUser(String marmottaUser) {
		this.marmottaUser = marmottaUser;
	}

	public String getMarmottaPassword() {
		return marmottaPassword;
	}

	public void setMarmottaPassword(String marmottaPassword) {
		this.marmottaPassword = marmottaPassword;
	}

	public String getMarmottaContext() {
		return marmottaContext;
	}

	public void setMarmottaContext(String marmottaContext) {
		this.marmottaContext = marmottaContext;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

    public HttpClientConnectionManager getConectionManager() {
        return conectionManager;
    }

    public void setConectionManager(HttpClientConnectionManager conectionManager) {
        this.conectionManager = conectionManager;
    }
}
