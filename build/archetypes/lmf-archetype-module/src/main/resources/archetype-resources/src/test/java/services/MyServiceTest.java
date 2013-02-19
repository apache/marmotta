package ${package}.services;

import junit.framework.Assert;
import kiwi.core.test.base.EmbeddedLMF;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ${package}.api.MyService;

public class MyServiceTest {

    private static EmbeddedLMF lmf;
    private static MyService myService;

    @BeforeClass
    public static void setUp() {
        lmf = new EmbeddedLMF();
        myService = lmf.getService(MyService.class);
    }

    @Test
    public void testDoThis() {

    }

    @Test
    public void testDoThat() {

    }

    @Test
    public void testHelloWorld() {
        Assert.assertEquals("Hello You", myService.helloWorld("You"));
        Assert.assertEquals("Hello Steve", myService.helloWorld("Steve"));
        Assert.assertEquals("Hello Tom", myService.helloWorld("Tom"));
        Assert.assertEquals("Hello Ron", myService.helloWorld("Ron"));
        Assert.assertEquals("Hello Wüterich", myService.helloWorld("Wüterich"));
    }

    @AfterClass
    public static void tearDown() {
        lmf.shutdown();
    }

}
