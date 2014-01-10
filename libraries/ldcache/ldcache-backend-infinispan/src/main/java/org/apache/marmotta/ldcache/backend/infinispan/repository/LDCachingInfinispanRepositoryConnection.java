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

package org.apache.marmotta.ldcache.backend.infinispan.repository;

import org.apache.marmotta.ldcache.api.LDCachingConnection;
import org.apache.marmotta.ldcache.backend.infinispan.LDCachingInfinispanBackend;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.util.concurrent.TimeUnit;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCachingInfinispanRepositoryConnection extends RepositoryConnectionWrapper implements LDCachingConnection {

    private LDCachingInfinispanBackend backend;

    private String resource;

    public LDCachingInfinispanRepositoryConnection(LDCachingInfinispanBackend backend, String resource) throws RepositoryException {
        super(getModelRepository(backend.getTripleCache().get(resource)));
        setDelegate(getRepository().getConnection());
        this.backend = backend;
        this.resource = resource;
    }


    private static Repository getModelRepository(Model model) throws RepositoryException {
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();

            if(model != null) {
                con.add(model);
            }

            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }

        return repository;
    }


    /**
     * Get the cache entry for the passed resource, if any. Returns null in case there is no cache entry.
     *
     * @param resource the resource to look for
     * @return the cache entry for the resource, or null if the resource has never been cached or is expired
     */
    @Override
    public CacheEntry getCacheEntry(URI resource) throws RepositoryException {
        return backend.getEntryCache().get(resource.stringValue());
    }

    /**
     * Store a cache entry for the passed resource in the backend. Depending on the backend, this can be a
     * persistent storage or an in-memory storage.
     *
     * @param resource
     * @param entry
     */
    @Override
    public void addCacheEntry(URI resource, CacheEntry entry) throws RepositoryException {
        backend.getEntryCache().put(resource.stringValue(),entry,entry.getExpiryDate().getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        Model model = new TreeModel();

        RepositoryResult<Statement> triples = getStatements(resource,null,null,true);
        try {
            while(triples.hasNext()) {
                model.add(triples.next());
            }
        } finally {
            triples.close();
        }

        backend.getTripleCache().put(resource.stringValue(),model,entry.getExpiryDate().getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Remove the currently stored cache entry for the passed resource from the backend.
     *
     * @param resource
     */
    @Override
    public void removeCacheEntry(URI resource) throws RepositoryException {
        backend.getEntryCache().remove(resource.stringValue());
        backend.getTripleCache().remove(resource.stringValue());
    }

    @Override
    public void close() throws RepositoryException {
        super.close();
    }
}
