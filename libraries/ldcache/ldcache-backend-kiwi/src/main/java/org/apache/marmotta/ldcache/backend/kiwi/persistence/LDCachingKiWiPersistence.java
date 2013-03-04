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
package org.apache.marmotta.ldcache.backend.kiwi.persistence;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;

import java.sql.SQLException;

/**
 * A KiWi persistence wrapper for storing caching information in the database used by the KiWi triple store
 * wrapped by the persistence.
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCachingKiWiPersistence {

    /**
     * Get the parent persistence service to access the database
     */
    private KiWiPersistence persistence;


    public LDCachingKiWiPersistence(KiWiPersistence persistence) {
        this.persistence = persistence;

        persistence.addNodeTableDependency("ldcache_entries","resource_id");
    }

    /**
     * Initialise the database, creating or upgrading tables if they do not exist or are of the wrong version.
     * This method must only be called after the initDatabase of the wrapped KiWiPersistence has been evaluated.
     */
    public void initDatabase() throws SQLException {
        persistence.initDatabase("ldcache", new String[] {"ldcache_entries"});
    }

    /**
     * Drop the versioning tables; this method must be called before the dropDatabase method of the underlying
     * KiWiPersistence is called.
     *
     * @throws SQLException
     */
    public void dropDatabase() throws SQLException {
        persistence.dropDatabase("ldcache");
    }

    /**
     * Return a connection from the connection pool which already has the auto-commit disabled.
     *
     * @return a fresh JDBC connection from the connection pool
     * @throws java.sql.SQLException in case a new connection could not be established
     */
    public LDCachingKiWiPersistenceConnection getConnection() throws SQLException {
        return new LDCachingKiWiPersistenceConnection(persistence.getConnection());
    }


    public KiWiDialect getDialect() {
        return persistence.getDialect();
    }

}
