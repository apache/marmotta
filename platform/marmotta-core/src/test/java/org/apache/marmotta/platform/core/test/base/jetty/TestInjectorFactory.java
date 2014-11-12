/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.test.base.jetty;

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
import org.jboss.resteasy.spi.metadata.Parameter;
import org.jboss.resteasy.spi.metadata.ResourceClass;
import org.jboss.resteasy.spi.metadata.ResourceConstructor;
import org.jboss.resteasy.spi.metadata.ResourceLocator;

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

        log.info("creating new RestEasy Injector Factory for Marmotta Test Environment");
    }

    public static void setManager(BeanManager manager) {
        TestInjectorFactory.manager = manager;
    }

    @Override
    public ConstructorInjector createConstructor(Constructor constructor, ResteasyProviderFactory factory) {
        Class<?> clazz = constructor.getDeclaringClass();

        if (!manager.getBeans(clazz).isEmpty())
        {
            log.debug("Using CdiConstructorInjector for class {0}.", clazz);
            return new CdiConstructorInjector(clazz, manager);
        }

        log.debug("No CDI beans found for {0}. Using default ConstructorInjector.", clazz);
        return delegate.createConstructor(constructor, factory);

    }

    @Override
    public PropertyInjector createPropertyInjector(Class resourceClass, ResteasyProviderFactory factory) {
        return new CdiPropertyInjector(delegate.createPropertyInjector(resourceClass, factory), resourceClass, Collections.<Class<?>, Type>emptyMap(), manager);
    }

    @Override
    public MethodInjector createMethodInjector(ResourceLocator method, ResteasyProviderFactory factory) {
        return delegate.createMethodInjector(method, factory);
    }

    @Override
    public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget, Class type, Type genericType, Annotation[] annotations, ResteasyProviderFactory factory) {
        return delegate.createParameterExtractor(injectTargetClass, injectTarget, type, genericType, annotations, factory);
    }

    @Override
    public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget, Class type, Type genericType, Annotation[] annotations, boolean useDefault, ResteasyProviderFactory factory) {
        return delegate.createParameterExtractor(injectTargetClass, injectTarget, type, genericType, annotations, useDefault, factory);
    }

    @Override
    public ValueInjector createParameterExtractor(Parameter parameter, ResteasyProviderFactory providerFactory) {
        return delegate.createParameterExtractor(parameter, providerFactory);
    }

    @Override
    public PropertyInjector createPropertyInjector(ResourceClass resourceClass, ResteasyProviderFactory providerFactory) {
        return delegate.createPropertyInjector(resourceClass, providerFactory);
    }

    @Override
    public ConstructorInjector createConstructor(ResourceConstructor constructor, ResteasyProviderFactory providerFactory) {
        return createConstructor(constructor.getConstructor(), providerFactory);
    }

    /**
     * Lookup ResteasyCdiExtension instance that was instantiated during CDI bootstrap
     *
     * @return ResteasyCdiExtension instance
     */
    private ResteasyCdiExtension lookupResteasyCdiExtension() {
        Set<Bean<?>> beans = manager.getBeans(ResteasyCdiExtension.class);
        Bean<?> bean = manager.resolve(beans);
        if (bean == null) throw new IllegalStateException("Unable to obtain ResteasyCdiExtension instance.");
        CreationalContext<?> context = manager.createCreationalContext(bean);
        return (ResteasyCdiExtension) manager.getReference(bean, ResteasyCdiExtension.class, context);
    }

}
