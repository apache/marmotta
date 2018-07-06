package org.apache.marmotta.platform.versioning;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.versioning.utils.MementoUtils;
import org.apache.marmotta.platform.versioning.webservices.MementoWebService;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.core.IsEqual;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 13.10.16.
 */
public class MementoWebServiceTest {

    private static Logger log = LoggerFactory.getLogger(MementoWebServiceTest.class);

    private static JettyMarmotta marmotta;

    private static Date date1,date2,date3;

    @BeforeClass
    public static void setUp() throws MarmottaImportException, URISyntaxException {
        marmotta = new JettyMarmotta("/marmotta", MementoWebService.class);

        ImportService importService = marmotta.getService(ImportService.class);
        UserService userService = marmotta.getService(UserService.class);
        ContextService contextService = marmotta.getService(ContextService.class);

        date1 = new Date();

        //import some data
        InputStream is_v1 = Thread.currentThread().getContextClassLoader().getResourceAsStream("data_v1.ttl");
        int n_v1 = importService.importData(is_v1, "text/turtle", userService.getAnonymousUser(), contextService.getDefaultContext());
        log.info("Imported RDF data_v1 with {} triples", n_v1);

        date2 = new Date();

        //import some data including updates
        InputStream is_v2 = Thread.currentThread().getContextClassLoader().getResourceAsStream("data_v2.ttl");
        int n_v2 = importService.importData(is_v2, "text/turtle", userService.getAnonymousUser(), contextService.getDefaultContext());
        log.info("Imported RDF data_v2 with {} triples", n_v2);

        date3 = new Date();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();
    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testNegotiationResponse() {
        expect().
                log().ifError().
                statusCode(302).
                when().request().redirects().follow(false).
                get(MementoUtils.MEMENTO_WEBSERVICE + "/" + MementoUtils.MEMENTO_TIMEGATE + "/http://example.org/resource1");
    }

    @Test
    public void testTimemapWithoutMementoDatetimeHeader() {
        expect().
                log().ifError().header("Memento-Datetime", isEmptyOrNullString()).
                when().request().redirects().follow(false).
                get(MementoUtils.MEMENTO_WEBSERVICE + "/" + MementoUtils.MEMENTO_TIMEGATE + "/http://example.org/resource1");
    }

    @Test
    public void testWithDatetimeHeader() {
        expect().
                log().ifError().
                when().request().redirects().follow(false).
                header("Accept-Datetime","Mon, 19 Sep 2016 23:47:12 GMT").
                get(MementoUtils.MEMENTO_WEBSERVICE + "/" + MementoUtils.MEMENTO_TIMEGATE + "/http://example.org/resource1");
    }

    @Test
    public void testDateFormatForTimemap() {
        when().
                get(MementoUtils.MEMENTO_WEBSERVICE + "/" + MementoUtils.MEMENTO_TIMEMAP + "/http://example.org/resource1").
                then().body(containsValidMementoDatetimeFormats("datetime=\"([^\"]+)\""));
    }

    @Test
    public void testDateFormatForLinkHeaders() {
        //timemap
        expect().
                log().ifError().
                when().request().redirects().follow(false).
                get(MementoUtils.MEMENTO_WEBSERVICE + "/" + MementoUtils.MEMENTO_TIMEMAP + "/http://example.org/resource1").
                then().header("date",containsValidMementoDatetimeFormats("(.+)"));

        //timegate
        String location = given().request().redirects().follow(false).
                when().
                get(MementoUtils.MEMENTO_WEBSERVICE + "/" + MementoUtils.MEMENTO_TIMEGATE + "/http://example.org/resource1").
                then().
                header("date",containsValidMementoDatetimeFormats("(.+)")).
                header("link", containsValidMementoDatetimeFormats("datetime=\"([^\"]+)\"")).extract().header("location");

        //memento resource
        when().
                get(location).
                then().
                header("date", containsValidMementoDatetimeFormats("(.+)")).
                header("memento-datetime",containsValidMementoDatetimeFormats("(.+)")).
                header("link", containsValidMementoDatetimeFormats("datetime=\"([^\"]+)\"")).extract().header("location");
    }

    @Test
    public void testTimemapSerialization() {
        expect()
                .log().ifError()
                .when()
                .request().header("Accept", "text / html, application / xhtml + xml, application/xml;q=0.9,image/webp,*/*;q=0.8")
                .get(MementoUtils.MEMENTO_WEBSERVICE + "/" + MementoUtils.MEMENTO_TIMEMAP + "/http://example.org/resource1")
                .then()
                .contentType(is("text / html"));
    }

    //matcher tests if all matching groups are in correct memento datetime serialization
    private org.hamcrest.Matcher containsValidMementoDatetimeFormats(final String pattern) {
        return new BaseMatcher() {

            private String lastGroup;

            @Override
            public boolean matches(final Object item) {

                Pattern p = Pattern.compile(pattern);

                Matcher m = p.matcher((String)item);

                while(m.find()) {
                    lastGroup = m.group(1);
                    try {
                        Date date = MementoUtils.MEMENTO_DATE_FORMAT.parse(lastGroup);
                        if(!lastGroup.equals(MementoUtils.MEMENTO_DATE_FORMAT.format(date))) return false;
                    } catch (ParseException ex) {
                        return false;
                    }
                }

                return true;
            }
            @Override
            public void describeTo(final Description description) {
                description.appendText("datetime does not match MementoDatetime: ").appendText(lastGroup);
            }
        };
    }

}
