package org.apache.marmotta.platform.sparql.webservices;

import java.io.IOException;
import java.io.InputStream;

import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

/**
 * Tests for testing the SPAQL endpoint
 * 
 * @author Sergio Fernández
 *
 */
public class SparqlWebServiceTest {

    private static JettyMarmotta marmotta;

    @BeforeClass
    public static void setUp() throws MarmottaImportException {
        marmotta = new JettyMarmotta("/marmotta");
        ImportService importService = marmotta.getService(ImportService.class);
        UserService userService = marmotta.getService(UserService.class);
        ContextService contextService = marmotta.getService(ContextService.class);
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("foaf.rdf"); 
        importService.importData(is, "application/rdf+xml", userService.getAnonymousUser(), contextService.getDefaultContext());

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();

    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testSet() throws IOException, InterruptedException {
        

    }

}
