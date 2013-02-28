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
package org.apache.marmotta.ldclient.dummy;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;

public class DummyProvider implements DataProvider {

	@Override
	public String getName() {
		return "Dummy";
	}

	@Override
	public String[] listMimeTypes() {
		return new String[] {"application/dummy"};
	}

	@Override
	public ClientResponse retrieveResource(String resource,
			LDClientService client, Endpoint endpoint)
			throws DataRetrievalException {

		try {
			final HttpGet request = new HttpGet(resource);
			client.getClient().execute(request, new ResponseHandler<String>() {

				@Override
				public String handleResponse(HttpResponse response)
						throws ClientProtocolException, IOException {
					StatusLine sL = response.getStatusLine();
					return sL.getReasonPhrase();
				}
			});
			return null;
		} catch (Exception e) {
			throw new DataRetrievalException(e);
		}
	}


}
