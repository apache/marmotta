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

import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.backend.file.LDCachingFileBackend;
import org.apache.marmotta.ldcache.services.test.ng.BaseLDCacheTest;
import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCacheFileTest extends BaseLDCacheTest {

    private static Logger log = LoggerFactory.getLogger(LDCacheFileTest.class);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Needs to be implemented by tests to provide the correct backend. Backend needs to be properly initialised.
     *
     * @return an LDCachingBackend
     */
    @Override
    protected LDCachingBackend createBackend() {
        try {
            final File storageDir = temporaryFolder.newFolder();
            LDCachingBackend backend = new LDCachingFileBackend(storageDir);
            backend.initialize();

            return backend;
        } catch (IOException e) {
            throw new AssumptionViolatedException("could not create storage-dir for file backend", e);
        }
    }



}
