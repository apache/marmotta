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

import org.apache.marmotta.commons.sesame.filter.AlwaysTrueFilter;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.ldcache.backend.file.LDCachingFileBackendNG;
import org.apache.marmotta.ldcache.sail.GenericLinkedDataSail;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldpath.backend.sesame.SesameRepositoryBackend;
import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * A Linked Data backend with persistent caching of the retrieved data. All data is read and stored in the directory
 * passed as constructor argument.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LDPersistentBackend extends SesameRepositoryBackend {
    private static final Logger log = LoggerFactory.getLogger(LDPersistentBackend.class);

    private LDCachingFileBackendNG backend;

    private GenericLinkedDataSail sail;

    private SesameFilter<Resource> cacheFilter;


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

        cacheFilter = new AlwaysTrueFilter<Resource>();

        try {
            ClientConfiguration config = new ClientConfiguration();

            backend = new LDCachingFileBackendNG(dataDirectory);
            sail = new GenericLinkedDataSail(new MemoryStore(),backend, cacheFilter, config);
            Repository repository = new SailRepository(sail);
            repository.initialize();
            setRepository(repository);

        } catch (RepositoryException e) {
            log.error("error initialising connection to Sesame in-memory repository",e);
        }
    }


    public void shutdown() {
        try {
            getRepository().shutDown();
        } catch (RepositoryException e) {
            log.error("error shutting down repository for resource cache");
        }
    }



}
