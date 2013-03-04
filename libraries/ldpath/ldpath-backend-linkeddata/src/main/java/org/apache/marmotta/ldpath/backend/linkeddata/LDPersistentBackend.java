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

import at.newmedialab.ldclient.model.CacheEntry;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * A Linked Data backend with persistent caching of the retrieved data. All data is read and stored in the directory
 * passed as constructor argument.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LDPersistentBackend extends AbstractLDBackend {
    private static final Logger log = LoggerFactory.getLogger(LDMemoryBackend.class);

    private Map<String,CacheEntry> cacheEntries;


    private RecordManager recordManager;

    /**
     * Create a persistent linked data backend storing the cache data in the directory passed as argument.
     * The directory will be created if it does not exist.
     * @param dataDirectory
     */
    public LDPersistentBackend(File dataDirectory) throws IOException {
        super();

        if(!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }

        File tripleStore = new File(dataDirectory.getAbsolutePath()+File.separator+"triples");
        if(!tripleStore.exists()) {
            tripleStore.mkdirs();
        }


        recordManager = RecordManagerFactory.createRecordManager(dataDirectory.getAbsolutePath()+File.separator+"resource_cache.cache");

        cacheEntries = recordManager.treeMap("resources");

        try {
            Repository repository = new SailRepository(new NativeStore(tripleStore));
            repository.initialize();
            setRepository(repository);

        } catch (RepositoryException e) {
            log.error("error initialising connection to Sesame in-memory repository",e);
        }
    }


    public void shutdown() {
        try {
            recordManager.close();
            getRepository().shutDown();
        } catch (IOException e) {
            log.error("error shutting down record manager for resource cache");
        } catch (RepositoryException e) {
            log.error("error shutting down repository for resource cache");
        }
    }


    /**
    /**
     * Return a map that can be used to store caching metadata about resources. The LDCacheProvider should take care
     * of persisting the metadata if desired.
     *
     * @return a map for storing caching metadata
     */
    @Override
    public Map<String,CacheEntry> getMetadataRepository() {
        return cacheEntries;
    }

}
