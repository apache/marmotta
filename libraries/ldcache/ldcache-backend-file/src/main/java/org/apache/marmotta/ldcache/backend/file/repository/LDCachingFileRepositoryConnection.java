package org.apache.marmotta.ldcache.backend.file.repository;

import org.apache.marmotta.ldcache.api.LDCachingConnection;
import org.apache.marmotta.ldcache.backend.file.util.FileBackendUtils;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryConnectionWrapper;

import java.io.File;
import java.io.IOException;

public class LDCachingFileRepositoryConnection extends RepositoryConnectionWrapper implements LDCachingConnection {

	private final File baseDir;

	public LDCachingFileRepositoryConnection(Repository repository, RepositoryConnection repositoryConnection, File baseDir) {
		super(repository, repositoryConnection);
		this.baseDir = baseDir;
	}

	@Override
	public CacheEntry getCacheEntry(URI resource) throws RepositoryException {
		try {
			final File dataFile = FileBackendUtils.getMetaFile(resource, baseDir);
			if (!(dataFile.exists())) return null;
			final CacheEntry ce = FileBackendUtils.readCacheEntry(dataFile, getValueFactory());
			if (FileBackendUtils.isExpired(ce)) return null;
			return ce;
		} catch (IOException e) {
			throw new RepositoryException("could not read cache entry for " + resource.stringValue(), e);
		}
	}

	@Override
	public void addCacheEntry(URI resource, CacheEntry entry)
			throws RepositoryException {
		try {
			FileBackendUtils.writeCacheEntry(entry, baseDir);
		} catch (IOException e) {
			throw new RepositoryException("could not store cache entry for " + resource.stringValue(), e);
		}
	}

	@Override
	public void removeCacheEntry(URI resource) throws RepositoryException {
		final File metaFile = FileBackendUtils.getMetaFile(resource, baseDir);
		if (metaFile.exists()) metaFile.delete();
	}

}
