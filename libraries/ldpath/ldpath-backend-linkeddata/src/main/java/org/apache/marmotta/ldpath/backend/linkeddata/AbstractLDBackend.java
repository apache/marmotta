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
package org.apache.marmotta.ldpath.backend.linkeddata;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.api.LDCacheProvider;
import org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.model.Endpoint;
import org.apache.marmotta.ldpath.backend.linkeddata.legacy.ldclient.service.LDCache;
import org.apache.marmotta.ldpath.backend.sesame.SesameRepositoryBackend;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Abstract superclass for Linked Data backends. Implements functionality common to all implementations.
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class AbstractLDBackend extends SesameRepositoryBackend implements LDCacheProvider {

    private Logger log = LoggerFactory.getLogger(AbstractLDBackend.class);

    private LDCache ldCache;

    /**
     * Initialise a new sesame backend. Repository needs to be set using setRepository.
     */
    protected AbstractLDBackend() {
        ldCache = new LDCache(this);

        try {
            Configuration config = new PropertiesConfiguration("endpoints.properties");

            HashSet<String> endpointNames = new HashSet<String>();
            for(Iterator<String> it = config.getKeys(); it.hasNext(); ) {
                String key = it.next();
                String[] components = key.split("\\.");
                if(components.length > 1) {
                    endpointNames.add(components[0]);
                }
            }
            for(String endpointName : endpointNames) {
                String prefix      = config.getString(endpointName+".prefix","");
                String kind        = config.getString(endpointName+".kind","");
                String endpointUrl = config.getString(endpointName+".endpoint","");
                String mimetype    = config.getString(endpointName+".mimetype","");
                long expiry        = config.getLong(endpointName+".expiry",(long)86400);

                Endpoint.EndpointType type;
                try {
                    type = Endpoint.EndpointType.valueOf(kind.toUpperCase());
                } catch (Exception e) {
                    type = Endpoint.EndpointType.LINKEDDATA;
                }

                if (prefix != null && prefix.startsWith(Endpoint.REGEX_INDICATOR)) {
                    // Check for valid Regex
                    try {
                        Pattern.compile(prefix.substring(Endpoint.REGEX_INDICATOR.length()));
                    } catch (PatternSyntaxException pse) {
                        log.error("invalid regexp pattern in endpoint '{}' prefix definition: {}",endpointName,prefix);
                    }
                }
                if (endpointUrl != null) {
                    endpointUrl = endpointUrl.replace('<', '{').replace('>', '}');
                } else {
                    endpointUrl = "";
                }
                Endpoint endpoint = new Endpoint(endpointName, type, prefix, endpointUrl, mimetype, expiry);
                log.info("Registering LD Cache Endpoint \"{}\"",endpointName);
                registerEndpoint(endpoint);
            }

        } catch (ConfigurationException e) {
            log.warn("could not load configuration file endpoints.properties from current directory, home directory, or classpath");
        }


    }

    /**
     * Return the sesame repository used for storing the triples that are retrieved from the Linked Data Cloud.
     * Triples will always be added to the context http://www.newmedialab.at/ldclient/cache to be able to distinguish
     * them from other triples.
     *
     * @return an initialised Sesame repository that can be used for caching triples
     */
    @Override
    public Repository getTripleRepository() {
        return getRepository();
    }


    /**
     * List the objects of triples in the triple store underlying this backend that have the subject and
     * property given as argument.
     *
     * @param subject  the subject of the triples to look for
     * @param property the property of the triples to look for
     * @return all objects of triples with matching subject and property
     */
    @Override
    public Collection<Value> listObjects(Value subject, Value property) {
        if(isURI(subject)) {
            ldCache.refreshResource((URI)subject);
        }
        return super.listObjects(subject, property);
    }

    /**
     * List the subjects of triples in the triple store underlying this backend that have the object and
     * property given as argument.
     *
     * @param object   the object of the triples to look for
     * @param property the property of the triples to look for
     * @return all dubjects of triples with matching object and property
     * @throws UnsupportedOperationException in case reverse selection is not supported (e.g. when querying Linked Data)
     */
    @Override
    public Collection<Value> listSubjects(Value property, Value object) {
        throw new IllegalArgumentException("reverse navigation not supported by Linked Data backend");
    }


    /**
     * Register a new Linked Data endpoint with this cache provider.
     *
     * @param endpoint
     */
    @Override
    public void registerEndpoint(Endpoint endpoint) {
        ldCache.getEndpointService().addEndpoint(endpoint);
    }

    /**
     * List all endpoints currently registered with the Linked Data cache provider.
     *
     * @return a collection of endpoints
     */
    @Override
    public Collection<Endpoint> listEndpoints() {
        return ldCache.getEndpointService().listEndpoints();
    }

    /**
     * Unregister the Linked Data endpoint given as argument.
     *
     * @param endpoint the endpoint to unregister
     */
    @Override
    public void unregisterEndpoint(Endpoint endpoint) {
        ldCache.getEndpointService().removeEndpoint(endpoint);
    }
}
