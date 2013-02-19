package kiwi.core.test.base.jetty;

import org.jboss.resteasy.cdi.CdiConstructorInjector;
import org.jboss.resteasy.cdi.CdiPropertyInjector;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.core.ValueInjector;
import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.spi.ConstructorInjector;
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.MethodInjector;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

/**
 * Custom injector component for the test environment
 * <p/>
 * Author: Sebastian Schaffert
 */
@SuppressWarnings("rawtypes")
public class TestInjectorFactory implements InjectorFactory {

    private static final Logger log = Logger.getLogger(TestInjectorFactory.class);

    private InjectorFactory delegate;
    private static BeanManager manager;
    private ResteasyCdiExtension extension;

    public TestInjectorFactory() {
        this.delegate = ResteasyProviderFactory.getInstance().getInjectorFactory();
        this.extension = lookupResteasyCdiExtension();

        log.info("creating new RestEasy Injector Factory for LMF Test Environment");
    }

    public static void setManager(BeanManager manager) {
        TestInjectorFactory.manager = manager;
    }

    @Override
    public ConstructorInjector createConstructor(Constructor constructor) {
        Class<?> clazz = constructor.getDeclaringClass();

        if (!manager.getBeans(clazz).isEmpty())
        {
            log.debug("Using CdiConstructorInjector for class {0}.", clazz);
            return new CdiConstructorInjector(clazz, manager);
        }

        log.debug("No CDI beans found for {0}. Using default ConstructorInjector.", clazz);
        return delegate.createConstructor(constructor);

    }

    @Override
    public PropertyInjector createPropertyInjector(Class resourceClass) {
        return new CdiPropertyInjector(delegate.createPropertyInjector(resourceClass), resourceClass, Collections.<Class<?>, Type>emptyMap(), manager);
    }

    @Override
    public MethodInjector createMethodInjector(Class root, Method method) {
        return delegate.createMethodInjector(root, method);
    }

    @Override
    public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget, Class type, Type genericType, Annotation[] annotations) {
        return delegate.createParameterExtractor(injectTargetClass, injectTarget, type, genericType, annotations);
    }

    @Override
    public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget, Class type, Type genericType, Annotation[] annotations, boolean useDefault) {
        return delegate.createParameterExtractor(injectTargetClass, injectTarget, type, genericType, annotations, useDefault);
    }


    /**
     * Lookup ResteasyCdiExtension instance that was instantiated during CDI bootstrap
     *
     * @return ResteasyCdiExtension instance
     */
    private ResteasyCdiExtension lookupResteasyCdiExtension()
    {
        Set<Bean<?>> beans = manager.getBeans(ResteasyCdiExtension.class);
        Bean<?> bean = manager.resolve(beans);
        if (bean == null) throw new IllegalStateException("Unable to obtain ResteasyCdiExtension instance.");
        CreationalContext<?> context = manager.createCreationalContext(bean);
        return (ResteasyCdiExtension) manager.getReference(bean, ResteasyCdiExtension.class, context);
    }

}
