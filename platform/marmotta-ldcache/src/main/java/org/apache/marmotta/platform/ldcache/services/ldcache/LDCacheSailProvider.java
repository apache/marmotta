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
package org.apache.marmotta.platform.ldcache.services.ldcache;

import org.apache.marmotta.platform.core.model.filter.MarmottaLocalFilter;
import org.apache.marmotta.platform.ldcache.api.endpoint.LinkedDataEndpointService;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.http.HttpClientService;
import org.apache.marmotta.platform.core.api.triplestore.NotifyingSailProvider;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;

import org.apache.marmotta.commons.sesame.filter.NotFilter;
import org.apache.marmotta.ldcache.sail.KiWiLinkedDataSail;
import org.apache.marmotta.ldcache.services.LDCache;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.openrdf.model.Resource;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.helpers.NotifyingSailWrapper;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * A sail provider service that allows wrapping a transparent Linked Data caching component around the
 * main SAIL.
 *
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class LDCacheSailProvider implements NotifyingSailProvider {

    public static final String LDCACHE_ENABLED = "ldcache.enabled";
    @Inject
    private Logger log;

    @Inject
    private LinkedDataEndpointService endpointService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    @Inject
    private HttpClientService         httpClientService;


    private Set<Endpoint> volatileEndpoints;

    private ClientConfiguration ldclientConfig;

    private KiWiLinkedDataSail  sail;

    /**
     * Return the name of the provider. Used e.g. for displaying status information or logging.
     *
     * @return
     */
    @Override
    public String getName() {
        return "Linked Data Caching";
    }


    /**
     * Return true if this sail provider is enabled in the configuration.
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return configurationService.getBooleanConfiguration(LDCACHE_ENABLED,true);
    }

    public void configurationChanged(@Observes ConfigurationChangedEvent e) {
        if(e.containsChangedKey(LDCACHE_ENABLED)) {
            sesameService.restart();

            if(!isEnabled()) {
                sail = null;
            }
        }
    }


    @PostConstruct
    public void initialize() {
        volatileEndpoints = new HashSet<Endpoint>();
        ldclientConfig = new ClientConfiguration();
        updateConfig();
    }

    public void updateEndpoints() {
        HashSet<Endpoint> endpoints = new HashSet<Endpoint>();
        endpoints.addAll(endpointService.listEndpoints());
        endpoints.addAll(volatileEndpoints);

        ldclientConfig.setEndpoints(endpoints);

        if(sail != null &&  sail.getLDCache() != null) {
            sail.getLDCache().reload();
        }
    }


    public void updateConfig() {
        ldclientConfig.setDefaultExpiry(configurationService.getLongConfiguration("ldcache.expiry", 86400L));
        ldclientConfig.setMinimumExpiry(configurationService.getLongConfiguration("ldcache.minexpiry", 3600L));
        ldclientConfig.setSocketTimeout(configurationService.getIntConfiguration("ldcache.so_timeout", 60000));
        ldclientConfig.setConnectionTimeout(configurationService.getIntConfiguration("ldcache.connection_timeout", 10000));
        ldclientConfig.setMaxParallelRequests(configurationService.getIntConfiguration("ldcache.max_parallel_requests",10));

        HashSet<Endpoint> endpoints = new HashSet<Endpoint>();
        endpoints.addAll(endpointService.listEndpoints());
        endpoints.addAll(volatileEndpoints);
        ldclientConfig.setEndpoints(endpoints);

        ldclientConfig.setHttpClient(httpClientService.getHttpClient());

        if(sail != null &&  sail.getLDCache() != null) {
            sail.getLDCache().reload();
        }
    }

    /**
     * Create the sail wrapper provided by this SailProvider
     *
     * @param parent the parent sail to wrap by the provider
     * @return the wrapped sail
     */
    @Override
    public NotifyingSailWrapper createSail(NotifyingSail parent) {
        String cache_context = configurationService.getCacheContext();
        sail = new KiWiLinkedDataSail(parent, new NotFilter<Resource>(MarmottaLocalFilter.getInstance()), cache_context, ldclientConfig);
        return sail;
    }


    /**
     * Return the Linked Data Client used by the caching system (e.g. for debugging).
     * @return
     */
    public LDClientService getLDClient() {
        if(sail != null) {
            return sail.getLDCache().getLDClient();
        } else {
            return null;
        }
    }

    /**
     * Return the caching backend used by the caching system (e.g. for debugging)
     * @return
     */
    public LDCache getLDCache() {
        if(sail != null) {
            return sail.getLDCache();
        } else {
            return null;
        }
    }


    /**
     * Add a volatile (in-memory) endpoint to the LDClient configuration. Can be used by other services for auto-registering
     * LDClient endpoints for special endpoints.
     *
     * @param endpoint
     */
    public void addVolatileEndpoint(Endpoint endpoint) {
        if(!volatileEndpoints.contains(endpoint)) {
            volatileEndpoints.add(endpoint);
            updateEndpoints();
        }
    }


    /**
     * Remove a volatile (in-memory) endpoint from the LDClient configuration.
     * @param endpoint
     */
    public void removeVolatileEndpoint(Endpoint endpoint) {
        if(volatileEndpoints.contains(endpoint)) {
            volatileEndpoints.remove(endpoint);
            updateEndpoints();
        }
    }

    /**
     * Return all configured volatile endpoints.
     * @return
     */
    public Set<Endpoint> getVolatileEndpoints() {
        return new HashSet<Endpoint>(volatileEndpoints);
    }
}
