/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.platform.ldcache.api.ldcache;

import org.apache.marmotta.ldcache.services.LDCacheNG;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.http.HttpClientService;
import org.apache.marmotta.platform.core.api.triplestore.NotifyingSailProvider;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.ldcache.api.endpoint.LinkedDataEndpointService;
import org.apache.marmotta.platform.ldcache.model.filter.LDCacheIgnoreFilter;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * A sail provider service that allows wrapping a transparent Linked Data caching component around the
 * main SAIL. This is the generic superclass for all LDCache backends.
 *
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class LDCacheSailProvider implements NotifyingSailProvider {


    public static final String LDCACHE_ENABLED = "ldcache.enabled";

    @Inject
    protected ConfigurationService configurationService;

    @Inject
    protected Instance<LDCacheIgnoreFilter> ignoreFilters;

    protected ClientConfiguration ldclientConfig;

    @Inject
    private Logger log;

    @Inject
    private LinkedDataEndpointService endpointService;

    @Inject
    private SesameService sesameService;

    @Inject
    private HttpClientService httpClientService;

    private Set<Endpoint> volatileEndpoints;

    /**
     * Return true in case the URI resource passed as argument is cached.
     * @param resource
     * @return
     */
    public boolean isCached(URI resource) throws RepositoryException {
        return getLDCache().contains(resource);
    }

    /**
     * Clear the currently configured Linked Data Sail.
     */
    public abstract void clearSail();

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
                clearSail();
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

        if(getLDCache() != null) {
            getLDCache().reload();
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

        if(getLDCache() != null) {
            getLDCache().reload();
        }
    }

    /**
     * Return the Linked Data Client used by the caching system (e.g. for debugging).
     * @return
     */
    public abstract LDClientService getLDClient();

    /**
     * Return the caching backend used by the caching system (e.g. for debugging)
     * @return
     */
    public abstract LDCacheNG getLDCache();

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
