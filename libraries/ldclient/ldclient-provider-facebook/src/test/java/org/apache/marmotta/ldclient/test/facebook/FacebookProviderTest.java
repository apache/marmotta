package org.apache.marmotta.ldclient.test.facebook;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.test.helper.TestLDClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;

/**
 * Test the Facebook Provider for some sample resources
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class FacebookProviderTest {

    private LDClientService ldclient;

    private static Logger log = LoggerFactory.getLogger(FacebookProviderTest.class);

    @Before
    public void setupClient() {
        ldclient = new TestLDClient(new LDClient());
    }

    @After
    public void shutdownClient() {
        ldclient.shutdown();
    }


    @Test
    public void testMovie() throws Exception {

        String uriMovie = "http://graph.facebook.com/160617097307237";
        ClientResponse respMovie = ldclient.retrieveResource(uriMovie);

        RepositoryConnection con = respMovie.getTriples().getConnection();
        con.begin();
        Assert.assertTrue(con.size() > 0);

        logData(con);

        // run a SPARQL test to see if the returned data is correct
        InputStream sparql = this.getClass().getResourceAsStream("movie.sparql");
        BooleanQuery testLabel = con.prepareBooleanQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        Assert.assertTrue("SPARQL test query failed", testLabel.evaluate());


        con.commit();
        con.close();


    }

    @Test
    public void testInterest() throws Exception {
        String uriInterest = "http://graph.facebook.com/106515832719603";
        ClientResponse respInterest = ldclient.retrieveResource(uriInterest);

        RepositoryConnection con = respInterest.getTriples().getConnection();
        con.begin();
        Assert.assertTrue(con.size() > 0);

        logData(con);

        // run a SPARQL test to see if the returned data is correct
        InputStream sparql = this.getClass().getResourceAsStream("interest.sparql");
        BooleanQuery testLabel = con.prepareBooleanQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        Assert.assertTrue("SPARQL test query failed", testLabel.evaluate());


        con.commit();
        con.close();

    }

    @Test
    public void testRestauraunt() throws Exception {
        String uriRestaurant = "http://graph.facebook.com/285699076901";
        ClientResponse respInterest = ldclient.retrieveResource(uriRestaurant);

        RepositoryConnection con = respInterest.getTriples().getConnection();
        con.begin();
        Assert.assertTrue(con.size() > 0);

        logData(con);

        // run a SPARQL test to see if the returned data is correct
        InputStream sparql = this.getClass().getResourceAsStream("restaurant.sparql");
        BooleanQuery testLabel = con.prepareBooleanQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        Assert.assertTrue("SPARQL test query failed", testLabel.evaluate());


        con.commit();
        con.close();

    }


    private void logData(RepositoryConnection con) throws Exception {
        if(log.isDebugEnabled()) {
            StringWriter out = new StringWriter();
            con.export(Rio.createWriter(RDFFormat.TURTLE, out));
            log.debug("DATA:");
            log.debug(out.toString());
        }

    }

}
