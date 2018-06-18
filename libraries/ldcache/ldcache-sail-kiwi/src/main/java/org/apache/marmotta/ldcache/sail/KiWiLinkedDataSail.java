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
package org.apache.marmotta.ldcache.sail;

import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.ldcache.backend.kiwi.LDCachingKiWiBackend;
import org.apache.marmotta.ldcache.model.CacheConfiguration;
import org.apache.marmotta.ldcache.services.LDCache;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.openrdf.model.Resource;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;
import org.openrdf.sail.helpers.NotifyingSailWrapper;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiLinkedDataSail extends NotifyingSailWrapper {

    private KiWiStore store;

    private LDCachingKiWiBackend backend;

    private LDCache ldcache;

    private SesameFilter<Resource> acceptor;

    private String cache_context;

    private CacheConfiguration config;

    /**
     * Creates a new SAIL with transparent Linked Data access. Caching information is stored in the underlying
     * KiWi triple store.
     *
     * @param baseSail the wrapped SAIL; at the parent level must be a KiWiStore
     * @param acceptor a filter that selects which resources are considered as external Linked Data resources,
     *                 e.g. all resources not matching http://localhost and file:
     * @param cacheContextUri the URI of the context to use for storing cached triples
     */
    public KiWiLinkedDataSail(NotifyingSail baseSail, SesameFilter<Resource> acceptor, String cacheContextUri) {
        this(baseSail, acceptor, cacheContextUri,null);
    }

    /**
     * Creates a new SAIL with transparent Linked Data access. Caching information is stored in the underlying
     * KiWi triple store.
     *
     * @param baseSail the wrapped SAIL; at the parent level must be a KiWiStore
     * @param acceptor a filter that selects which resources are considered as external Linked Data resources,
     *                 e.g. all resources not matching http://localhost and file:
     * @param cacheContextUri the URI of the context to use for storing cached triples
     */
    public KiWiLinkedDataSail(NotifyingSail baseSail, SesameFilter<Resource> acceptor, String cacheContextUri, ClientConfiguration clientConfiguration) {
        super(baseSail);

        this.store = getBaseStore();
        this.cache_context = cacheContextUri;
        this.acceptor = acceptor;
        this.config = new CacheConfiguration();
        if(clientConfiguration != null) {
            config.setClientConfiguration(clientConfiguration);
        }
    }


    @Override
    public void initialize() throws SailException {
        super.initialize();

        backend = new LDCachingKiWiBackend(store, cache_context);
        backend.initialize();

        ldcache = new LDCache(config,backend);

    }

    @Override
    public void shutDown() throws SailException {
        ldcache.shutdown();
        backend.shutdown();

        super.shutDown();
    }

    @Override
    public NotifyingSailConnection getConnection() throws SailException {
        return new KiWiLinkedDataSailConnection(super.getConnection(),ldcache,acceptor);
    }

    /**
     * Return the KiWi store that is at the base of the SAIL stack. Throws an IllegalArgumentException in case the base
     * store is not a KiWi store.
     *
     * @return
     */
    public KiWiStore getBaseStore() {
        StackableSail current = this;
        while(current != null && current.getBaseSail() instanceof StackableSail) {
            current = (StackableSail) current.getBaseSail();
        }
        if(current != null && current.getBaseSail() instanceof KiWiStore) {
            return (KiWiStore) current.getBaseSail();
        } else {
            throw new IllegalStateException("the base store is not a KiWiStore (type: "+current.getBaseSail().getClass().getCanonicalName()+")!");
        }
    }

    public LDCachingKiWiBackend getBackend() {
        return backend;
    }

    public LDCache getLDCache() {
        return ldcache;
    }
}
