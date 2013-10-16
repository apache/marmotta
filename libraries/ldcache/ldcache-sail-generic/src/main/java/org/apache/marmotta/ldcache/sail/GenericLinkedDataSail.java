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

package org.apache.marmotta.ldcache.sail;

import org.apache.marmotta.commons.sesame.filter.AlwaysTrueFilter;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.model.CacheConfiguration;
import org.apache.marmotta.ldcache.services.LDCache;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.openrdf.model.Resource;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailWrapper;

/**
 * A generic caching sail that can be used with any Sesame SAIL to integrate LDCache transparent Linked Data Caching.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 * @author Jakob Frank (jakob@apache.org)
 */
public class GenericLinkedDataSail extends NotifyingSailWrapper {

	private final LDCachingBackend cachingBackend;
	private CacheConfiguration config;
	private LDCache ldcache;
	private SesameFilter<Resource> acceptForCaching;

	public GenericLinkedDataSail(NotifyingSail base, LDCachingBackend cachingBackend) {
		this(base, cachingBackend, new AlwaysTrueFilter<Resource>(), createCacheConfiguration(null));
	}

	public GenericLinkedDataSail(NotifyingSail base, LDCachingBackend cachingBackend, ClientConfiguration clientConfig) {
		this(base, cachingBackend, new AlwaysTrueFilter<Resource>(), clientConfig);
	}

	public GenericLinkedDataSail(NotifyingSail base, LDCachingBackend cachingBackend, CacheConfiguration cacheConfig) {
		this(base, cachingBackend, new AlwaysTrueFilter<Resource>(), cacheConfig);
	}
	
	public GenericLinkedDataSail(NotifyingSail base, LDCachingBackend cachingBackend, SesameFilter<Resource> acceptForCaching) {
		this(base, cachingBackend, acceptForCaching, createCacheConfiguration(null));
	}

	public GenericLinkedDataSail(NotifyingSail base, LDCachingBackend cachingBackend, SesameFilter<Resource> acceptForCaching, ClientConfiguration clientConfig) {
		this(base, cachingBackend, acceptForCaching, createCacheConfiguration(clientConfig));
	}

	public GenericLinkedDataSail(NotifyingSail base, LDCachingBackend cachingBackend, SesameFilter<Resource> acceptForCaching, CacheConfiguration cacheConfig) {
		super(base);
		this.cachingBackend = cachingBackend;
		this.acceptForCaching = acceptForCaching;
		this.config = cacheConfig;
	}

	private static CacheConfiguration createCacheConfiguration(ClientConfiguration cc) {
		CacheConfiguration config = new CacheConfiguration();
		if (cc != null) {
			config.setClientConfiguration(cc);
		}
		return config;
	}
	
	@Override
	public NotifyingSailConnection getConnection() throws SailException {
		return new GenericLinkedDataSailConnection(super.getConnection(), ldcache, acceptForCaching);
	}
	
	@Override
	public void initialize() throws SailException {
		super.initialize();
		
		cachingBackend.initialize();
		
		ldcache = new LDCache(this.config,cachingBackend);
	}
	
	@Override
	public void shutDown() throws SailException {
		super.shutDown();
		
		cachingBackend.shutdown();
		
		ldcache.shutdown();
	}
	
	public void reinit() {
		ldcache.shutdown();
		ldcache = new LDCache(this.config,cachingBackend);
	}
	
	public CacheConfiguration getCacheConfiguration() {
		return config;
	}

    public LDCache getLDCache() {
        return ldcache;
    }
}
