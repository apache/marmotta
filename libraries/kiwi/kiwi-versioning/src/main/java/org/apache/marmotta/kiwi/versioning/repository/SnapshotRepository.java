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

import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.kiwi.versioning.sail.KiWiVersioningSail;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.SailException;

import java.util.Date;

/**
 * A wrapper around a KiWiVersioningSail that allows accessing snapshots using the Repository API instead of the
 * SAIL API.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SnapshotRepository extends SailRepository {

    KiWiVersioningSail sail;

    public SnapshotRepository(KiWiVersioningSail sail) {
        super(sail);

        this.sail = sail;
    }

    /**
     * Get a read-only snapshot of the repository at the given date. Returns a repository connection that
     * can be used to access the triple data. Any attempts to modify the underlying data will throw
     * a SailReadOnlyException.
     *
     * @param snapshotDate the date of which to take the snapshot; the snapshot will consist of all
     *                     triples that have been created before or at the date and deleted after that date
     *                     (or not deleted at all).
     * @return  a read-only sail connection to access the data of the triple store at the given date
     */
    public SailRepositoryConnection getSnapshot(Date snapshotDate) throws RepositoryException {
        try {
            return new SnapshotRepositoryConnection(this, sail.getSnapshot(snapshotDate));
        }
        catch (SailException e) {
            throw new RepositoryException(e);
        }

    }

    /**
     * List all versions of this repository.
     *
     * @return
     */
    public RepositoryResult<Version> listVersions() throws SailException {
        return sail.listVersions();
    }

    /**
     * List all versions of this repository between a start and end date.
     *
     * @return
     */
    public RepositoryResult<Version> listVersions(Date from, Date to) throws SailException {
        return sail.listVersions(from, to);
    }
}
