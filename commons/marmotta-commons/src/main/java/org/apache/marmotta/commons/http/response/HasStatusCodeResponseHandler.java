package org.apache.marmotta.commons.http.response;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;


public class HasStatusCodeResponseHandler implements ResponseHandler<Boolean> {

    private final int statusCode;

    public HasStatusCodeResponseHandler(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        try {
            return statusCode == response.getStatusLine().getStatusCode();
        } finally {
            EntityUtils.consumeQuietly(response.getEntity());
        }
    }

}
