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
package at.newmedialab.lmf.client;

import org.apache.http.conn.ClientConnectionManager;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ClientConfiguration {

	/**
	 * The URI at which the LMF installation can be located
	 */
	private String lmfUri;

	/**
	 * (Optional) user to authenticate with the LMF system
	 */
	private String lmfUser;

	/**
	 * (Optional) password to authenticate with the LMF system
	 */
	private String lmfPassword;

	/**
	 * (Optional) context in the LMF system
	 */
	private String lmfContext;

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
	
	private ClientConnectionManager conectionManager;

	public ClientConfiguration(String lmfUri) {
		this.lmfUri = lmfUri;
	}

	public ClientConfiguration(String lmfUri, String lmfUser, String lmfPassword) {
		this.lmfUri = lmfUri;
		this.lmfUser = lmfUser;
		this.lmfPassword = lmfPassword;
	}

	public String getLmfUri() {
		return lmfUri;
	}

	public void setLmfUri(String lmfUri) {
		this.lmfUri = lmfUri;
	}

	public String getLmfUser() {
		return lmfUser;
	}

	public void setLmfUser(String lmfUser) {
		this.lmfUser = lmfUser;
	}

	public String getLmfPassword() {
		return lmfPassword;
	}

	public void setLmfPassword(String lmfPassword) {
		this.lmfPassword = lmfPassword;
	}

	public String getLmfContext() {
		return lmfContext;
	}

	public void setLmfContext(String lmfContext) {
		this.lmfContext = lmfContext;
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

    public ClientConnectionManager getConectionManager() {
        return conectionManager;
    }

    public void setConectionManager(ClientConnectionManager conectionManager) {
        this.conectionManager = conectionManager;
    }
}
