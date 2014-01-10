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

package org.apache.marmotta.ldcache.backend.file.test;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.marmotta.ldcache.api.LDCachingBackendNG;
import org.apache.marmotta.ldcache.backend.file.LDCachingFileBackendNG;
import org.apache.marmotta.ldcache.services.test.ng.BaseLDCacheNGTest;
import org.junit.internal.AssumptionViolatedException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCacheFileNGTest extends BaseLDCacheNGTest {

    private static Logger log = LoggerFactory.getLogger(LDCacheFileNGTest.class);

    /**
     * Needs to be implemented by tests to provide the correct backend. Backend needs to be properly initialised.
     *
     * @return
     */
    @Override
    protected LDCachingBackendNG createBackend() {
        final File storageDir = Files.createTempDir();

        LDCachingBackendNG backend = null;
        try {
            backend = new LDCachingFileBackendNG(storageDir) {
                @Override
                public void shutdown() {
                    super.shutdown();

                    try {
                        FileUtils.deleteDirectory(storageDir);
                    } catch (IOException e) {
                    }
                }
            };
            backend.initialize();

            return backend;
        } catch (RepositoryException e) {
            throw new AssumptionViolatedException("could not initialise backend",e);
        }
    }



}
