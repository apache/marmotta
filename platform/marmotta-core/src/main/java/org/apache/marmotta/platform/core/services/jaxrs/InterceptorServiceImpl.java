/*
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
package org.apache.marmotta.platform.core.services.jaxrs;

import org.apache.marmotta.platform.core.api.jaxrs.InterceptorService;
import org.apache.marmotta.platform.core.events.SystemStartupEvent;
import org.apache.marmotta.platform.core.jaxrs.interceptors.CDIInterceptor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * This service auto-registers JAX-RS interceptors implementing the CDIInterceptor interface and
 * registers them with RESTEasy. This allows applications based on Marmotta to easily implement and register their
 * own Interceptors without needing to go into RESTEasy.
 * <p/>
 * Note that Interceptors that are injected via CDI need to be annotated with @Dependent, or otherwise
 * they will be proxied by the CDI implementation and then the generic type cannot be determined.
 *
 * @author Jakob Frank (jakob@apache.org)
 */
@ApplicationScoped
public class InterceptorServiceImpl implements InterceptorService {

    private Logger log = LoggerFactory.getLogger(InterceptorServiceImpl.class);

    @Inject
    private Instance<CDIInterceptor> interceptors;

    @PostConstruct
    protected void initialize() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();

        register(factory);
    }

    public void register(ResteasyProviderFactory factory) {
        log.info("initialising JAX-RS interceptors");

        for(CDIInterceptor interceptor : interceptors) {
            log.debug("registering interceptor: {}", interceptor.getClass().getName());

            factory.registerProviderInstance(interceptor);
        }
    }

    // trigger startup once configuration service is finished with initialisation
    protected void initEvent(@Observes SystemStartupEvent e) {

    }

}
