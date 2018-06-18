/*
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
package org.apache.marmotta.ldpath.backend.sesame;


import org.openrdf.repository.Repository;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A threading version of the sesame backend.
 * <p/>
 * Author: Sebastian Schaffert
 * @deprecated threading must be handled explicitly.
 */
@Deprecated
public class ThreadingSesameBackend extends SesameRepositoryBackend {

    private ThreadPoolExecutor workers;

    /**
     * Initialise a new sesame backend. Repository needs to be set using setRepository.
     */
    protected ThreadingSesameBackend() {
        workers = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    /**
     * Initialise a new sesame backend using the repository passed as argument.
     *
     * @param repository
     */
    public ThreadingSesameBackend(Repository repository) {
        super(repository);
        workers = (ThreadPoolExecutor)Executors.newFixedThreadPool(4);
    }

    /**
     * Return true if the underlying backend supports the parallel execution of queries.
     *
     * @return
     */
    public boolean supportsThreading() {
        return true;
    }

    /**
     * In case the backend supports threading, this method should return the ExecutorService representing the
     * thread pool. LDPath lets the backend manage the thread pool to avoid excessive threading.
     *
     * @return
     */
    public ThreadPoolExecutor getThreadPool() {
        return workers;
    }

    /**
     * Shut down the thread pool
     */
    public void shutdown() {
        workers.shutdownNow();
    }

}
