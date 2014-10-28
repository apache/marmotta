package org.apache.marmotta.platform.ldf.webservices;

import com.jayway.restassured.RestAssured;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.prefix.PrefixService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.core.webservices.io.ImportWebService;
import org.apache.marmotta.platform.core.webservices.prefix.PrefixWebService;
import org.apache.marmotta.platform.core.webservices.triplestore.ContextWebService;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static com.jayway.restassured.RestAssured.expect;

/**
 * LDF Webservice test
 *
 * @author Sergio Fernández
 */
public class LdfWebServiceTest {

    private static JettyMarmotta marmotta;

    @BeforeClass
    public static void setUp() {
        marmotta = new JettyMarmotta("/marmotta",  LdfWebService.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();

    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testEmptyContext() throws IOException, InterruptedException {
        expect().
            statusCode(400).
        when().
            get(LdfWebService.PATH + "/empty");
    }

    @Test
    public void testFragment() throws IOException, InterruptedException, URISyntaxException, MarmottaImportException {
        final String ctx = RandomStringUtils.random(8, true, false);
        final String uri = "http://www.wikier.org/foaf#wikier";

        // 1. check is empty
        expect().
            statusCode(400).
        when().
            get(LdfWebService.PATH + "/" + ctx);

        // 2. import some data
        final ConfigurationService configurationService = marmotta.getService(ConfigurationService.class);
        final ImportService importService = marmotta.getService(ImportService.class);
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream("wikier.rdf");
        final ValueFactoryImpl vf = new ValueFactoryImpl();
        importService.importData(is, "application/rdf+xml", null, vf.createURI(configurationService.getBaseContext() + ctx));

        // 3. request a fragment
        expect().
            statusCode(200).
        given().
            queryParam("subject", uri).
        when().
            get(LdfWebService.PATH + "/" + ctx);

        // 4. test wrong page
        expect().
            statusCode(400).
        given().
            queryParam("subject", uri).
            queryParam("page", 100).
        when().
            get(LdfWebService.PATH + "/" + ctx);

    }

}
