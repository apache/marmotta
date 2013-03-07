package org.apache.marmotta.ldcache.sail;

import org.apache.marmotta.commons.sesame.filter.AlwaysTrueFilter;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.model.CacheConfiguration;
import org.apache.marmotta.ldcache.services.LDCache;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.openrdf.model.Resource;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailWrapper;
import org.openrdf.sail.memory.MemoryStore;

public class GenericCachingSail extends NotifyingSailWrapper {

	private final LDCachingBackend cachingBackend;
	private CacheConfiguration config;
	private LDCache ldcache;
	private SesameFilter<Resource> acceptForCaching;

	public GenericCachingSail(LDCachingBackend cachingBackend) {
		this(cachingBackend, new AlwaysTrueFilter<Resource>(), createCacheConfiguration(null));
	}

	public GenericCachingSail(LDCachingBackend cachingBackend, ClientConfiguration clientConfig) {
		this(cachingBackend, new AlwaysTrueFilter<Resource>(), clientConfig);
	}

	public GenericCachingSail(LDCachingBackend cachingBackend, CacheConfiguration cacheConfig) {
		this(cachingBackend, new AlwaysTrueFilter<Resource>(), cacheConfig);
	}
	
	public GenericCachingSail(LDCachingBackend cachingBackend, SesameFilter<Resource> acceptForCaching) {
		this(cachingBackend, acceptForCaching, createCacheConfiguration(null));
	}

	public GenericCachingSail(LDCachingBackend cachingBackend, SesameFilter<Resource> acceptForCaching, ClientConfiguration clientConfig) {
		this(cachingBackend, acceptForCaching, createCacheConfiguration(clientConfig));
	}

	public GenericCachingSail(LDCachingBackend cachingBackend, SesameFilter<Resource> acceptForCaching, CacheConfiguration cacheConfig) {
		super(new MemoryStore());
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
		return new GenericCachingSailConnection(super.getConnection(), ldcache, acceptForCaching);
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
}
