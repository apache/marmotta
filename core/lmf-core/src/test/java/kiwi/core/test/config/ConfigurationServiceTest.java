package kiwi.core.test.config;

import junit.framework.Assert;
import kiwi.core.api.config.ConfigurationService;
import kiwi.core.test.base.EmbeddedLMF;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test the functionality of the configuration service
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ConfigurationServiceTest {

    private static EmbeddedLMF lmf;
    private static ConfigurationService configurationService;

    @BeforeClass
    public static void setUp() {
        lmf = new EmbeddedLMF();
        configurationService = lmf.getService(ConfigurationService.class);
    }

    @Test
    public void testSetString() {
        String key = "foo.key";
        String value = "Foo Value";

        // test without / with default value
        Assert.assertNull(configurationService.getStringConfiguration(key));
        Assert.assertEquals("default",configurationService.getStringConfiguration(key,"default"));


        // set value to a new value
        configurationService.setConfiguration(key, value);
        Assert.assertEquals(value,configurationService.getConfiguration(key));

        // remove value and check it is unset afterwards
        configurationService.removeConfiguration(key);
        Assert.assertNull(configurationService.getStringConfiguration(key));

    }

    @Test
    public void testSetList() {
        String key = "foo.listkey";
        List<String> values = new ArrayList<String>();
        values.add("foo");
        values.add("bar");

        // test without / with default value
        Assert.assertNull(configurationService.getStringConfiguration(key));
        Assert.assertTrue(configurationService.getListConfiguration(key).size() == 0);
        Assert.assertEquals(values,configurationService.getListConfiguration(key, values));


        // set value to a new value
        configurationService.setConfiguration(key, values);
        Assert.assertEquals(values,configurationService.getListConfiguration(key));

        // remove value and check it is unset afterwards
        configurationService.removeConfiguration(key);
        Assert.assertTrue(configurationService.getListConfiguration(key).size() == 0);
    }

    @Test
    public void testSetBoolean() {
        String key = "foo.booleankey";
        boolean value = true;


        // test without / with default value
        Assert.assertNull(configurationService.getStringConfiguration(key));
        Assert.assertFalse(configurationService.getBooleanConfiguration(key));
        Assert.assertTrue(configurationService.getBooleanConfiguration(key, true));


        // set value to a new value
        configurationService.setBooleanConfiguration(key, value);
        Assert.assertTrue(configurationService.getBooleanConfiguration(key));

        // remove value and check it is unset afterwards
        configurationService.removeConfiguration(key);
        Assert.assertFalse(configurationService.getBooleanConfiguration(key));

    }


    @Test
    public void testSetDouble() {
        String key = "foo.dblkey";
        double value = new Random().nextDouble();

        // test without / with default value
        Assert.assertNull(configurationService.getStringConfiguration(key));
        Assert.assertEquals(value,configurationService.getDoubleConfiguration(key,value),0.1);


        // set value to a new value
        configurationService.setDoubleConfiguration(key, value);
        Assert.assertEquals(value,configurationService.getDoubleConfiguration(key),0.1);

        // remove value and check it is unset afterwards
        configurationService.removeConfiguration(key);
        Assert.assertNull(configurationService.getStringConfiguration(key));

    }

    @Test
    public void testSetInt() {
        String key = "foo.intkey";
        int value = new Random().nextInt();

        // test without / with default value
        Assert.assertNull(configurationService.getStringConfiguration(key));
        Assert.assertEquals(value, configurationService.getIntConfiguration(key, value));
        Assert.assertEquals((double)value,configurationService.getDoubleConfiguration(key, value));


        // set value to a new value
        configurationService.setIntConfiguration(key, value);
        Assert.assertEquals(value,configurationService.getIntConfiguration(key));

        // remove value and check it is unset afterwards
        configurationService.removeConfiguration(key);
        Assert.assertNull(configurationService.getStringConfiguration(key));

    }


    @AfterClass
    public static void tearDown() {
        lmf.shutdown();
    }

}
