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
