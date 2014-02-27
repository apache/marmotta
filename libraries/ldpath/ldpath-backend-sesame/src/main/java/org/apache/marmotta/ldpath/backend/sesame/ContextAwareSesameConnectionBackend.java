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

import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;

/**
 * {@link SesameConnectionBackend} that considers only data from the provided contexts.
 * @author Jakob Frank <jakob@apache.org>
 *
 */
public class ContextAwareSesameConnectionBackend extends SesameConnectionBackend {

    /**
     * Create a new {@link ContextAwareSesameConnectionBackend} that considers only data from the provided contexts.
     * @param connection the {@link RepositoryConnection} to use.
     * @param contexts the contexts to look for, providing an empty array here will revert the functionality to {@link SesameConnectionBackend}.
     */
    public ContextAwareSesameConnectionBackend(RepositoryConnection connection, Resource... contexts) {
        super(connection, true, contexts);
    }

    /**
     * Create a new {@link ContextAwareSesameConnectionBackend} that considers only data from the provided contexts.
     * @param connection the {@link RepositoryConnection} to use.
     * @param includeInferred
     * @param contexts the contexts to look for, providing an empty array here will revert the functionality to {@link SesameConnectionBackend}.
     */
    public ContextAwareSesameConnectionBackend(RepositoryConnection connection, boolean includeInferred, Resource... contexts) {
        super(connection, includeInferred, contexts);
    }

    /**
     * Create a new {@link ContextAwareSesameConnectionBackend} that considers only data from the provided contexts.
     * @param connection the {@link RepositoryConnection} to use.
     * @param contexts the contexts to look for, providing an empty array here will revert the functionality to {@link SesameConnectionBackend}.
     */
    public static ContextAwareSesameConnectionBackend withConnection(RepositoryConnection connection, Resource... contexts) {
        return new ContextAwareSesameConnectionBackend(connection, contexts);
    }


    /**
     * Create a new {@link ContextAwareSesameConnectionBackend} that considers only data from the provided contexts.
     * @param connection the {@link RepositoryConnection} to use.
     * @param includeInferred
     * @param contexts the contexts to look for, providing an empty array here will revert the functionality to {@link SesameConnectionBackend}.
     */
    public static ContextAwareSesameConnectionBackend withConnection(RepositoryConnection connection, boolean includeInferred, Resource... contexts) {
        return new ContextAwareSesameConnectionBackend(connection, includeInferred, contexts);
    }
}
