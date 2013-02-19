package org.apache.marmotta.ldclient.test;

import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ldclient.test.helper.TestLDClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestLDClientTest {

    private TestLDClient client;

    @Before
    public void setUp() {
        TestLDClient testLDClient = new TestLDClient(new LDClient());
        client = testLDClient;
    }

    @After
    public void cleanUp() {
        client.shutdown();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testConnectionRefused() throws Exception {
        client.retrieveResource("http://no.host.for/this/url");
        Assert.fail();
    }

    @Test(expected = DataRetrievalException.class)
    public void testLocalhostInvalidPort() throws Exception {
        client.retrieveResource("http://127.1.2.3:-1/");
        Assert.fail();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMissingProvider() throws Exception {
        client.retrieveResource("ftp://no.provider.for/this/url");
        Assert.fail();
    }

}
