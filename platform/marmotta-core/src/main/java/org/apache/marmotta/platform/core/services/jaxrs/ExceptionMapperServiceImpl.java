package org.apache.marmotta.platform.core.services.jaxrs;

import org.apache.marmotta.platform.core.api.content.ContentReader;
import org.apache.marmotta.platform.core.api.jaxrs.ExceptionMapperService;
import org.apache.marmotta.platform.core.events.ConfigurationServiceInitEvent;
import org.apache.marmotta.platform.core.events.SystemStartupEvent;
import org.apache.marmotta.platform.core.jaxrs.CDIExceptionMapper;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * This service auto-registers JAX-RS exception mappers implementing the CDIExceptionMapper interface and
 * registers them with RESTEasy. This allows applications based on Marmotta to easily implement and register their
 * own ExceptionMapppers without needing to go into RESTEasy.
 * <p/>
 * Note that ExceptionMappers that are injected via CDI need to be annotated with @Dependent, or otherwise
 * they will be proxied by the CDI implementation and then the generic type cannot be determined.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class ExceptionMapperServiceImpl implements ExceptionMapperService {

    @Inject
    private Logger log;

    @Inject
    private Instance<CDIExceptionMapper<?>> mappers;

    /**
     * Register Exception Mappers
     */
    @PostConstruct
    public void initialise() {
        log.info("initialising JAX-RS exception mappers");

        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();

        for(CDIExceptionMapper<?> mapper : mappers) {
            log.debug("registering exception mapper: {}", mapper.getClass().getName());

            factory.registerProviderInstance(mapper);
        }
    }

    // trigger startup once configuration service is finished with initialisation
    public void initEvent(@Observes SystemStartupEvent e) {

    }


}
