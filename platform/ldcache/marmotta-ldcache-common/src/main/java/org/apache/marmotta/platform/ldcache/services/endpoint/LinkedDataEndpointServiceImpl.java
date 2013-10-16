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
package org.apache.marmotta.platform.ldcache.services.endpoint;

import com.google.common.base.Joiner;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.ldcache.api.endpoint.LinkedDataEndpointService;
import org.openrdf.model.URI;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
@ApplicationScoped
public class LinkedDataEndpointServiceImpl implements LinkedDataEndpointService {

    @Inject
    private Logger log;


    @Inject
    private ConfigurationService configurationService;


    private static Endpoint[] blacklist = new Endpoint[] {
        new Endpoint("HolyGoat", "NONE","^http://www\\.holygoat\\.co\\.uk","","",86400l),
        new Endpoint("KiWi Project","NONE","^http://www\\.kiwi-project\\.eu/","","",86400l)
    };

    @PostConstruct
    public void initialize() {
        log.trace("LDEndpointService starting up...");
    }


    /**
     * Add a new endpoint to the system. The endpoint will be persisted in the database.
     *
     * @param endpoint
     */
    @Override
    public void addEndpoint(Endpoint endpoint) {
        storeEndpoint(endpoint);
    }

    /**
     * `
     * Update the endpoint passed as argument in the database.
     *
     * @param endpoint
     */
    @Override
    public void updateEndpoint(Endpoint endpoint) {
        storeEndpoint(endpoint);
    }

    /**
     * List all endpoints registered in the system.
     *
     * @return a list of endpoints in the order they were added to the database.
     */
    @Override
    public List<Endpoint> listEndpoints() {
        List<Endpoint> result = new ArrayList<Endpoint>();
        for(String label : listEndpointLabels()) {
            result.add(getEndpointByLabel(label));
        }
        for(Endpoint e : blacklist) {
            if(!result.contains(e)) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Remove the endpoint given as argument. The endpoint will be deleted in the database.
     *
     * @param endpoint
     */
    @Override
    public void removeEndpoint(Endpoint endpoint) {
        String label = endpoint.getName().replaceAll("[^A-Za-z0-9 ]", "").toLowerCase();

        List<String> keys = configurationService.listConfigurationKeys("ldcache.endpoint."+label);
        for(String key : keys) {
            configurationService.removeConfiguration(key);
        }
    }

    /**
     * Return the endpoint with the given ID.
     *
     * @param id ID of the endpoint to return.
     * @return
     */
    @Override
    public Endpoint getEndpoint(String id) {
        for(Endpoint endpoint : listEndpoints()) {
            if(endpoint.getName().replaceAll("[^A-Za-z0-9 ]", "").toLowerCase().equals(id)) return endpoint;
        }
        return null;
    }

    /**
     * Retrieve the endpoint matching the KiWiUriResource passed as argument. The endpoint is determined by
     * matching the endpoint's URI prefix with the resource URI. If no matching endpoint exists, returns null.
     * The LinkedDataClientService can then decide (based on configuration) whether to try with a standard
     * LinkedDataRequest or ignore the request.
     *
     * @param resource the KiWiUriResource to check.
     */
    @Override
    public Endpoint getEndpoint(URI resource) {
        for(Endpoint endpoint : listEndpoints()) {
            if (endpoint.handles(resource.stringValue())) return endpoint;
        }

        return null;
    }

    /**
     * Test whether an endpoint definition for the given url pattern already exists.
     *
     * @param urlPattern
     * @return
     */
    @Override
    public boolean hasEndpoint(String urlPattern) {
        for(Endpoint endpoint : listEndpoints()) {
            if(endpoint.getUriPattern() != null && endpoint.getUriPattern().equals(urlPattern)) return true;
        }
        return false;
    }


    private void storeEndpoint(Endpoint endpoint) {

        String label = endpoint.getName().replaceAll("[^A-Za-z0-9 ]", "").toLowerCase();
        configurationService.setConfiguration("ldcache.endpoint."+label+".name", endpoint.getName());
        configurationService.setIntConfiguration("ldcache.endpoint." + label + ".prio", endpoint.getPriority());
        configurationService.setConfiguration("ldcache.endpoint." + label + ".provider", endpoint.getType());
        configurationService.setConfiguration("ldcache.endpoint." + label + ".pattern", endpoint.getUriPattern());
        configurationService.setConfiguration("ldcache.endpoint." + label + ".service", endpoint.getEndpointUrl());
        configurationService.setLongConfiguration("ldcache.endpoint." + label + ".expiry", endpoint.getDefaultExpiry());
        configurationService.setBooleanConfiguration("ldcache.endpoint." + label + ".active", endpoint.isActive());
        configurationService.setConfiguration(
                "ldcache.endpoint." + label + ".contenttype",
                Joiner.on(",").join(endpoint.getContentTypes())
        );
    }


    private Endpoint getEndpointByLabel(String label) {
        Endpoint endpoint = new Endpoint();

        endpoint.setName(configurationService.getStringConfiguration("ldcache.endpoint."+label+".name"));
        endpoint.setPriority(configurationService.getIntConfiguration("ldcache.endpoint." + label + ".prio"));
        endpoint.setType(configurationService.getStringConfiguration("ldcache.endpoint." + label + ".provider"));
        endpoint.setUriPattern(configurationService.getStringConfiguration("ldcache.endpoint." + label + ".pattern"));
        endpoint.setEndpointUrl(configurationService.getStringConfiguration("ldcache.endpoint." + label + ".service"));
        endpoint.setDefaultExpiry(configurationService.getLongConfiguration("ldcache.endpoint." + label + ".expiry"));
        endpoint.setActive(configurationService.getBooleanConfiguration("ldcache.endpoint." + label + ".active"));
        endpoint.setContentTypes(new HashSet<ContentType>(
                MarmottaHttpUtils.parseAcceptHeader(
                        configurationService.getStringConfiguration("ldcache.endpoint." + label + ".contenttype",""))
        ));

        return endpoint;
    }


    private Collection<String> listEndpointLabels() {
        Set<String> labels = new HashSet<String>();
        for(String key : configurationService.listConfigurationKeys("ldcache.endpoint")) {
            String[] components = key.split("\\.");
            labels.add(components[2]);
        }
        return labels;
    }

}
