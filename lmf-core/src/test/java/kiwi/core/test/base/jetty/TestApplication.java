package kiwi.core.test.base.jetty;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class TestApplication extends Application {

    // this is a hack, since there is no other way to inject a service class into a JAX-RS application
    private static Set<Class<?>> testedWebService;


    @Override
    public Set<Class<?>> getClasses() {
        return testedWebService;
    }


    public static void setTestedWebServices(Set<Class<?>> testedWebService) {
        TestApplication.testedWebService = testedWebService;
    }
}
