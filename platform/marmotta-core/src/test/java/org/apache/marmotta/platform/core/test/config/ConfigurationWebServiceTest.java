package org.apache.marmotta.platform.core.test.config;

import com.google.common.collect.Lists;
import com.jayway.restassured.RestAssured;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.test.base.JettyLMF;
import org.apache.marmotta.platform.core.webservices.config.ConfigurationWebService;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ConfigurationWebServiceTest {

    private static JettyLMF lmf;
    private static ConfigurationService configurationService;

    private static ObjectMapper mapper = new ObjectMapper();


    @BeforeClass
    public static void setUp() {
        lmf = new JettyLMF("/LMF",8080, ConfigurationWebService.class);
        configurationService = lmf.getService(ConfigurationService.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/LMF";

    }

    @AfterClass
    public static void tearDown() {
        lmf.shutdown();
    }

    @Test
    public void testSetConfiguration() throws IOException {
        // set my.key to the values "value1" and "value2"
        given().
        header("Content-Type","application/json").
        content(mapper.writeValueAsString(Lists.newArrayList("value1", "value2"))).
        expect().
        statusCode(200).
        when().
        post("/config/data/mykey");


        // test whether configuration service has the key
        Assert.assertThat(configurationService.getListConfiguration("mykey"), hasItem("value1"));

        // test whether values appear when retrieving the key
        expect().
        statusCode(200).
        body("mykey",hasItems("value1","value2")).
        when().
        get("/config/data/mykey");

        // test whether values appear in full list
        expect().
        statusCode(200).
        body("mykey.value",hasItems("value1","value2")).
        when().
        get("/config/list");


        // test whether deleting returns OK
        expect().
        statusCode(200).
        when().
        delete("/config/data/mykey");


        // test whether values appear when retrieving the key
        expect().
        statusCode(404).
        when().
        get("/config/data/mykey");
    }

}
