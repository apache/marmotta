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
package org.apache.marmotta.kiwi.versioning.persistence;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * This class handles all database access of the versioning component of the KiWi triple store. It provides
 * methods for creating versions, deleting versions, listing versions, and accessing snapshots (following
 * the Memento specification, http://mementoweb.org/)
 * <p/>
 * The KiWiVersioningPersistence makes use of a wrapped KiWiPersistence object passed as constructor argument.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiVersioningPersistence {

    private static Logger log = LoggerFactory.getLogger(KiWiVersioningPersistence.class);


    /**
     * Get the parent persistence service to access the database
     */
    private KiWiPersistence persistence;


    public KiWiVersioningPersistence(KiWiPersistence persistence) {
        this.persistence = persistence;
    }

    /**
     * Initialise the database, creating or upgrading tables if they do not exist or are of the wrong version.
     * This method must only be called after the initDatabase of the wrapped KiWiPersistence has been evaluated.
     */
    public void initDatabase() throws SQLException {
        persistence.initDatabase("versioning", new String[] {"versions", "versions_added", "versions_removed"});

        persistence.addNodeTableDependency("versions", "creator");
        persistence.addTripleTableDependency("versions_added","triple_id");
        persistence.addTripleTableDependency("versions_removed","triple_id");
    }

    /**
     * Drop the versioning tables; this method must be called before the dropDatabase method of the underlying
     * KiWiPersistence is called.
     *
     * @throws SQLException
     */
    public void dropDatabase() throws SQLException {
        persistence.dropDatabase("versioning");
    }

    /**
     * Return a connection from the connection pool which already has the auto-commit disabled.
     *
     * @return a fresh JDBC connection from the connection pool
     * @throws java.sql.SQLException in case a new connection could not be established
     */
    public KiWiVersioningConnection getConnection() throws SQLException {
        return new KiWiVersioningConnection(persistence, persistence.getDialect(), persistence.getCacheManager());
    }


    public KiWiDialect getDialect() {
        return persistence.getDialect();
    }
}
