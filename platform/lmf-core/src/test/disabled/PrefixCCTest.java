package kiwi.core.services.prefix;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PrefixCCTest {

    private PrefixCC prefixcc;
    private final String PREFIX = "foaf";
    private final String NS = "http://xmlns.com/foaf/0.1/";

    @Before
    public void setup() {
        prefixcc = new PrefixCC();
    }

    @After
    public void shutdown() {
        prefixcc = null;
    }

    @Test
    public void prefixTest() {
        assertEquals(NS, prefixcc.getNamespace(PREFIX));
    }

    @Test
    public void prefixFailTest() {
        assertNull(prefixcc.getPrefix("http://ewmknnakjfdajas.com/fsjaff#"));
    }

    @Test
    public void reverseTest() {
        assertEquals(PREFIX, prefixcc.getPrefix(NS));
    }

    @Test
    public void reverseFailTest() {
        assertNull(prefixcc.getPrefix("ewmknnakjfdajas"));
    }

}
