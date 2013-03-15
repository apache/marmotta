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
