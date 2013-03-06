package org.apache.marmotta.ldcache.backend.file.repository;

import info.aduna.iteration.UnionIteration;

import java.io.File;
import java.io.IOException;

import org.apache.marmotta.ldcache.api.LDCachingConnection;
import org.apache.marmotta.ldcache.backend.file.util.FileBackendUtils;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.SailConnection;

public class LDCachingFileRepositoryConnection extends SailRepositoryConnection implements LDCachingConnection {

	private final File baseDir;

	public LDCachingFileRepositoryConnection(SailRepository repository,
			SailConnection sailConnection, File baseDir) {
		super(repository, sailConnection);
		this.baseDir = baseDir;
		// TODO Auto-generated constructor stub
	}

	@Override
	public RepositoryResult<Statement> getStatements(Resource subject, URI predicate,
			Value object, boolean inferred, Resource... contexts)
			throws RepositoryException {
		// TODO: add cached triples here...
		return new RepositoryResult<Statement>(new UnionIteration<Statement, RepositoryException>(super.getStatements(subject, predicate, object, inferred, contexts)));
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
