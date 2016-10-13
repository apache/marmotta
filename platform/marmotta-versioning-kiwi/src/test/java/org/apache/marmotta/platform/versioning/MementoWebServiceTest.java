package org.apache.marmotta.platform.versioning;

import com.jayway.restassured.RestAssured;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.versioning.utils.MementoUtils;
import org.apache.marmotta.platform.versioning.webservices.MementoWebService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Date;

import static com.jayway.restassured.RestAssured.expect;

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

}
