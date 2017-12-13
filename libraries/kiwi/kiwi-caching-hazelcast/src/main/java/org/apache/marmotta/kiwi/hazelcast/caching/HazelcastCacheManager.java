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

package org.apache.marmotta.kiwi.hazelcast.caching;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.marmotta.kiwi.caching.CacheManager;
import org.apache.marmotta.kiwi.config.CacheMode;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.hazelcast.serializer.*;
import org.apache.marmotta.kiwi.hazelcast.util.AsyncMap;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class HazelcastCacheManager implements CacheManager {

    private static Logger log = LoggerFactory.getLogger(HazelcastCacheManager.class);

    private KiWiConfiguration configuration;

    private Config hcConfiguration;

    private HazelcastInstance hazelcast;

    private AsyncMap<Long,KiWiNode> nodeCache;
    private AsyncMap<Long,KiWiTriple> tripleCache;
    private AsyncMap<String,KiWiUriResource> uriCache;
    private AsyncMap<String,KiWiAnonResource> bnodeCache;
    private AsyncMap<String,KiWiLiteral> literalCache;
    private AsyncMap<String,KiWiNamespace> nsPrefixCache;
    private AsyncMap<String,KiWiNamespace> nsUriCache;

    private Map<Long,Long> registryCache;

    public HazelcastCacheManager(KiWiConfiguration configuration) {
        this.configuration = configuration;

        hcConfiguration = new Config();
        if(configuration.isClustered()) {
            hcConfiguration.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
            hcConfiguration.getNetworkConfig().getJoin().getMulticastConfig().setMulticastPort(configuration.getClusterPort());
            hcConfiguration.getNetworkConfig().getJoin().getMulticastConfig().setMulticastGroup(configuration.getClusterAddress());
        } else {
            hcConfiguration.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        }
        hcConfiguration.getGroupConfig().setName(configuration.getClusterName());

        hcConfiguration.setClassLoader(HazelcastCacheManager.class.getClassLoader());
        hcConfiguration.setProperty("hazelcast.logging.type", "slf4j");

        setupSerializers();
        setupCaches();

        hazelcast = Hazelcast.newHazelcastInstance(hcConfiguration);

        if(!configuration.isClustered()) {
            log.info("initialised Hazelcast local cache manager");
        } else {
            log.info("initialised Hazelcast distributed cache manager (cluster name: {})",  configuration.getClusterName());

            if(configuration.getCacheMode() != CacheMode.DISTRIBUTED) {
                log.warn("Hazelcast only supports distributed cache mode (mode configuration was {})", configuration.getCacheMode());
            }
        }

    }

    private void setupSerializers() {
        SerializerConfig scBNode = new SerializerConfig().setImplementation(new BNodeSerializer()).setTypeClass(KiWiAnonResource.class);
        hcConfiguration.getSerializationConfig().addSerializerConfig(scBNode);

        SerializerConfig scBoolean = new SerializerConfig().setImplementation(new BooleanLiteralSerializer()).setTypeClass(KiWiBooleanLiteral.class);
        hcConfiguration.getSerializationConfig().addSerializerConfig(scBoolean);

        SerializerConfig scDate = new SerializerConfig().setImplementation(new DateLiteralSerializer()).setTypeClass(KiWiDateLiteral.class);
        hcConfiguration.getSerializationConfig().addSerializerConfig(scDate);

        SerializerConfig scDouble = new SerializerConfig().setImplementation(new DoubleLiteralSerializer()).setTypeClass(KiWiDoubleLiteral.class);
        hcConfiguration.getSerializationConfig().addSerializerConfig(scDouble);

        SerializerConfig scInt = new SerializerConfig().setImplementation(new IntLiteralSerializer()).setTypeClass(KiWiIntLiteral.class);
        hcConfiguration.getSerializationConfig().addSerializerConfig(scInt);

        SerializerConfig scString = new SerializerConfig().setImplementation(new StringLiteralSerializer()).setTypeClass(KiWiStringLiteral.class);
        hcConfiguration.getSerializationConfig().addSerializerConfig(scString);

        SerializerConfig scTriple = new SerializerConfig().setImplementation(new TripleSerializer()).setTypeClass(KiWiTriple.class);
        hcConfiguration.getSerializationConfig().addSerializerConfig(scTriple);

        SerializerConfig scUri = new SerializerConfig().setImplementation(new UriSerializer()).setTypeClass(KiWiUriResource.class);
        hcConfiguration.getSerializationConfig().addSerializerConfig(scUri);
    }

    private void setupCaches() {
        setupMapConfig(NODE_CACHE, configuration.getNodeCacheSize());
        setupMapConfig(TRIPLE_CACHE, configuration.getTripleCacheSize());
        setupMapConfig(URI_CACHE, configuration.getUriCacheSize());
        setupMapConfig(BNODE_CACHE, configuration.getBNodeCacheSize());
        setupMapConfig(LITERAL_CACHE, configuration.getLiteralCacheSize());
        setupMapConfig(NS_PREFIX_CACHE, configuration.getNamespaceCacheSize());
        setupMapConfig(NS_URI_CACHE, configuration.getNamespaceCacheSize());

    }


    private void setupMapConfig(String name, int size) {
        MapConfig cfg = new MapConfig(NODE_CACHE);
        cfg.setMaxSizeConfig(new MaxSizeConfig(size, MaxSizeConfig.MaxSizePolicy.PER_PARTITION));
        cfg.setAsyncBackupCount(1);
        cfg.setBackupCount(0);
        cfg.setEvictionPolicy(MapConfig.EvictionPolicy.LRU);
        cfg.setMaxIdleSeconds(600);     // 10 minutes
        cfg.setTimeToLiveSeconds(3600); // 1 hour

        hcConfiguration.addMapConfig(cfg);
    }

    /**
     * Return the node id -> node cache from the cache manager. This cache is heavily used to lookup
     * nodes when querying or loading triples and should therefore have a decent size (default 500.000 elements).
     *
     * @return an EHCache Cache instance containing the node id -> node mappings
     */
    @Override
    public Map<Long, KiWiNode> getNodeCache() {
        if(nodeCache == null) {
            nodeCache = new AsyncMap<>(hazelcast.<Long,KiWiNode>getMap(NODE_CACHE));
        }

        return nodeCache;
    }

    /**
     * Return the triple id -> triple cache from the cache manager. This cache is used for speeding up the
     * construction of query results.
     *
     * @return
     */
    @Override
    public Map<Long, KiWiTriple> getTripleCache() {
        if(tripleCache == null) {
            tripleCache = new AsyncMap<>(hazelcast.<Long,KiWiTriple>getMap(TRIPLE_CACHE));
        }

        return tripleCache;
    }

    /**
     * Return the uri -> KiWiUriResource cache from the cache manager. This cache is used when constructing new
     * KiWiUriResources to avoid a database lookup.
     *
     * @return
     */
    @Override
    public Map<String, KiWiUriResource> getUriCache() {
        if(uriCache == null) {
            uriCache = new AsyncMap<>(hazelcast.<String,KiWiUriResource>getMap(URI_CACHE));
        }

        return uriCache;
    }

    /**
     * Return the anonId -> KiWiAnonResource cache from the cache manager. This cache is used when constructing new
     * KiWiAnonResources to avoid a database lookup.
     *
     * @return
     */
    @Override
    public Map<String, KiWiAnonResource> getBNodeCache() {
        if(bnodeCache == null) {
            bnodeCache = new AsyncMap<>(hazelcast.<String,KiWiAnonResource>getMap(BNODE_CACHE));
        }

        return bnodeCache;
    }

    /**
     * Return the literal cache key -> KiWiLiteral cache from the cache manager. This cache is used when constructing new
     * KiWiLiterals to avoid a database lookup.
     *
     * @return
     * @see org.apache.marmotta.commons.sesame.model.LiteralCommons#createCacheKey(String, java.util.Locale, String)
     */
    @Override
    public Map<String, KiWiLiteral> getLiteralCache() {
        if(literalCache == null) {
            literalCache = new AsyncMap<>(hazelcast.<String,KiWiLiteral>getMap(LITERAL_CACHE));
        }

        return literalCache;
    }

    /**
     * Return the URI -> namespace cache from the cache manager. Used for looking up namespaces
     *
     * @return
     */
    @Override
    public Map<String, KiWiNamespace> getNamespaceUriCache() {
        if(nsUriCache == null) {
            nsUriCache = new AsyncMap<>(hazelcast.<String,KiWiNamespace>getMap(NS_URI_CACHE));
        }

        return nsUriCache;
    }

    /**
     * Return the prefix -> namespace cache from the cache manager. Used for looking up namespaces
     *
     * @return
     */
    @Override
    public Map<String, KiWiNamespace> getNamespacePrefixCache() {
        if(nsPrefixCache == null) {
            nsPrefixCache = new AsyncMap<>(hazelcast.<String,KiWiNamespace>getMap(NS_PREFIX_CACHE));
        }

        return nsPrefixCache;
    }

    /**
     * Create and return the cache used by the CacheTripleRegistry. This is an unlimited synchronous replicated
     * cache and should be used with care.
     *
     * @return
     */
    @Override
    public Map<Long, Long> getRegistryCache() {
        if(registryCache == null) {
            registryCache = hazelcast.getMap(REGISTRY_CACHE);
        }

        return registryCache;
    }

    /**
     * Get the cache with the given name from the cache manager. Can be used to request additional
     * caches from the cache manager that are not covered by explicit methods.
     *
     * @param name
     * @return
     */
    @Override
    public Map getCacheByName(String name) {
        return hazelcast.getMap(name);
    }


    /**
     * Return the backend instance for further access to the cluster (in case modules need it)
     * @return
     */
    public HazelcastInstance getBackend() {
        return hazelcast;
    }

    /**
     * Clear all caches managed by this cache manager.
     */
    @Override
    public void clear() {
        for(Map m : new Map[] { nodeCache, tripleCache, uriCache, bnodeCache, literalCache, nsPrefixCache, nsUriCache, registryCache}) {
            if(m != null) {
                m.clear();
            }
        }
    }

    /**
     * Shutdown this cache manager instance. Will shutdown the underlying EHCache cache manager.
     */
    @Override
    public void shutdown() {
        hazelcast.shutdown();
    }
}
