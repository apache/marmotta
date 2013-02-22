/**
 *  Copyright (c) 2012 Salzburg Research.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.marmotta.platform.core.test.base;

import com.jayway.restassured.RestAssured;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;

import java.io.IOException;
import java.util.UUID;

/**
 * LMF Base Integration Test
 * 
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class LMFBaseTest {

    protected final String HOST = "localhost";
    protected final int PORT = 8080;
    protected final String CTX = "LMF";
    protected final String RSC = "resource";
    protected final String PROP = "marmotta.home";
    protected String baseUrl;
    protected String baseResource;
    protected String basePath;

    protected ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        RestAssured.baseURI = "http://" + HOST;
        RestAssured.port = PORT;
        RestAssured.basePath = "/" + CTX;
        baseUrl = getBaseUrl();
        baseResource = getBaseResource();
        try {
            basePath = getBasePath();
        } catch (Exception e) {
            basePath = "/tmp/lmf"; // FIXME
        }
    }

    private String getBaseUrl() {
        return "http://" + HOST + ":" + PORT + "/" + CTX;
    }

    private String getBaseResource() {
        return getBaseUrl() + "/" + RSC + "/";
    }

    private String getBasePath() throws ClientProtocolException, IOException, ParseException, JSONException {
        String resource = baseUrl + "/config/data/" + PROP;
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(resource);
        get.setHeader("Accept", "application/json");
        HttpResponse response = client.execute(get);
        if (200 == response.getStatusLine().getStatusCode()) {
            HttpEntity entity = response.getEntity();
            JSONObject json = new JSONObject(EntityUtils.toString(entity));
            if (json.has(PROP))
                return json.getString(PROP);
            else
                throw new JSONException("Missing " + PROP + " entry");
        } else
            throw new IOException(resource + " not accessed");
    }

    protected String generateRandomUri() {
        UUID uuid = UUID.randomUUID();
        return getBaseResource() + uuid.toString();
    }

    protected String generateRandomContent(int count) {
        return RandomStringUtils.randomAlphanumeric(count);
    }

}
