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
package org.apache.marmotta.kiwi.versioning.repository;

import org.apache.marmotta.kiwi.versioning.sail.KiWiSnapshotConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;

/**
 * A wrapper class for snapshot repository connections.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SnapshotRepositoryConnection extends SailRepositoryConnection {

    public SnapshotRepositoryConnection(SailRepository repository, KiWiSnapshotConnection sailConnection) {
        super(repository, sailConnection);
    }
}
