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

package org.apache.marmotta.ldcache.backend.file;

import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.backend.file.util.FileBackendUtils;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * File-based implementation of the next generation LDCaching Backend API
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCachingFileBackend implements LDCachingBackend {

    private static Logger log = LoggerFactory.getLogger(LDCachingFileBackend.class);

    private final File storageDir;

    private Repository cacheRepository;

    public LDCachingFileBackend(File storageDir) throws RepositoryException {
        if (storageDir == null) throw new NullPointerException();
        this.storageDir = storageDir;

    }


    /**
     * Return the cache entry for the given resource, or null if this entry does not exist.
     *
     *
     * @param resource the resource to retrieve the cache entry for
     * @return
     */
    @Override
    public CacheEntry getEntry(URI resource) {
        try {
            // load metadata from disk
            final File dataFile = FileBackendUtils.getMetaFile(resource, storageDir);
            if (!(dataFile.exists())) return null;
            final CacheEntry ce = FileBackendUtils.readCacheEntry(dataFile, getValueFactory());
            if (FileBackendUtils.isExpired(ce)) return null;

            // read triples for this entry from cache repository
            RepositoryConnection con = cacheRepository.getConnection();
            try {
                con.begin();

                Model triples = new TreeModel();
                ModelCommons.add(triples, con.getStatements(resource,null,null,true));
                ce.setTriples(triples);

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } finally {
                con.close();
            }

            return ce;
        } catch (IOException | RepositoryException e) {
            log.error("error while loading cache entry from file system:",e);

            return null;
        }
    }


    /**
     * Update the cache entry for the given resource with the given entry.
     *
     * @param resource the resource to update
     * @param entry    the entry for the resource
     */
    @Override
    public void putEntry(URI resource, CacheEntry entry) {
        try {
            FileBackendUtils.writeCacheEntry(entry, storageDir);

            // update the repository with the triples from the entry
            RepositoryConnection con = cacheRepository.getConnection();
            try {
                con.begin();

                con.remove(resource,null,null);
                con.add(entry.getTriples());

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } finally {
                con.close();
            }
        } catch (IOException | RepositoryException e) {
            log.error("could not store cache entry for {}: {}", resource.stringValue(), e.getMessage());
        }

    }

    /**
     * Remove the cache entry for the given resource if it exists. Does nothing otherwise.
     *
     * @param resource the resource to remove the entry for
     */
    @Override
    public void removeEntry(URI resource) {
        try {
            final File metaFile = FileBackendUtils.getMetaFile(resource, storageDir);
            if (metaFile.exists()) metaFile.delete();

            // update the repository with the triples from the entry
            RepositoryConnection con = cacheRepository.getConnection();
            try {
                con.begin();

                con.remove(resource, null, null);

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } finally {
                con.close();
            }
        } catch (RepositoryException e) {
            log.error("could not remove cache entry for {}: {}", resource.stringValue(), e.getMessage());
        }
    }

    /**
     * Clear all entries in the cache backend.
     */
    @Override
    public void clear() {
        for(File metaFile : FileBackendUtils.listMetaFiles(storageDir)) {
            metaFile.delete();
        }

        try {
            RepositoryConnection con = cacheRepository.getConnection();
            try {
                con.begin();

                con.clear();

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } finally {
                con.close();
            }
        } catch(RepositoryException ex) {
            log.error("could not clear cache: {}", ex.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldcache.api.LDCachingBackend#initialize()
     */
    @Override
    public void initialize() {
        if (!storageDir.exists() && !storageDir.mkdirs()){
            log.error("Could not create storage directory: " + storageDir.getPath());
        } else if (!storageDir.isDirectory()) {
            log.error(storageDir.getPath() + " is not a directory");
        }

        File tripleDir = new File(storageDir,"triples");

        try {
            cacheRepository = new SailRepository(new NativeStore(tripleDir, "spoc"));
            cacheRepository.initialize();
        } catch (RepositoryException ex) {
            log.error("could not initialize cache directory",ex);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.ldcache.api.LDCachingBackend#shutdown()
     */
    @Override
    public void shutdown() {
        try {
            cacheRepository.shutDown();
        } catch (RepositoryException e) {
            log.error("error while shutting down cache repository", e);
        }

    }


    private ValueFactory getValueFactory() {
        return ValueFactoryImpl.getInstance();
    }

}
