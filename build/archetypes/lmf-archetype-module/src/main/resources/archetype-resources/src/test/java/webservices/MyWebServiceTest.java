package ${package}.webservices;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.http.ContentType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import kiwi.core.test.base.JettyLMF;

public class MyWebServiceTest {
    private static JettyLMF lmf;

    @BeforeClass
    public static void beforeClass() {
        lmf = new JettyLMF("/${moduleKey}-test", 9090, MyWebService.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 9090;
        RestAssured.basePath = "/${moduleKey}-test";
        RestAssured.config = RestAssuredConfig.newConfig().decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"));
    }

    @AfterClass
    public static void afterClass() {
        if (lmf != null) {
            lmf.shutdown();
        }
    }

    @Test
    public void testHello() {
        /*
         * GET ?name=<xxx>
         */
        given()
        .param("name", "Steve")
        .expect()
        .content(containsString("Hello Steve"))
        .when()
        .get("/${moduleKey}");

        given()
        .contentType(ContentType.HTML)
        .param("name", "Jürgen")
        .expect()
        .content(containsString("Hello Jürgen"))
        .when()
        .get("/${moduleKey}");

        expect()
        .statusCode(400)
        .when()
        .get("/${moduleKey}");
    }

    @Test
    public void testDoThis() {
        /*
         * POST ?turns=i default 2
         */
        given()
        .param("turns", 1)
        .expect().statusCode(200)
        .when()
        .post("/${moduleKey}");

        given()
        .param("turns", 10)
        .expect().statusCode(200)
        .when()
        .post("/${moduleKey}");

        expect().statusCode(200)
        .when()
        .post("/${moduleKey}");
    }

}
