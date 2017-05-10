package org.apache.marmotta.platform.user.webservices;

import com.jayway.restassured.RestAssured;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.jayway.restassured.RestAssured.expect;

/**
 * UserWebService Test
 *
 * @author Sergio Fernández
 */
public class UserWebServiceTest {

    private static JettyMarmotta marmotta;

    @BeforeClass
    public static void setUp() throws MarmottaImportException, URISyntaxException {
        marmotta = new JettyMarmotta("/marmotta", UserWebService.class);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();
    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    @Ignore("internal jboss issue")
    public void testLogin() throws IOException, InterruptedException {
        expect().
            log().ifError().
            statusCode(200).
        given().
            auth(). preemptive().basic("admin", "pass123").
        when().
            get("/user/login");
    }

}
