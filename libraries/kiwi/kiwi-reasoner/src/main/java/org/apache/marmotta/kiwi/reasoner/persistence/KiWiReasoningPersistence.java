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
package org.apache.marmotta.kiwi.reasoner.persistence;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * This class handles all database access of the reasoning component of the KiWi triple store. It provides
 * methods for creating programs, rules, and justifications, deleting programs, rules and justifications,
 * and listing programs, rules and justifications.
 * <p/>
 * The KiWiReasoningPersistence makes use of a wrapped KiWiPersistence object passed as constructor argument.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiReasoningPersistence {


    private static Logger log = LoggerFactory.getLogger(KiWiReasoningPersistence.class);


    /**
     * Get the parent persistence service to access the database
     */
    private KiWiPersistence persistence;


    /**
     * The value factory to use for parsing RDF values in KWRL programs
     */
    private ValueFactory valueFactory;

    public KiWiReasoningPersistence(KiWiPersistence persistence, ValueFactory valueFactory) {
        this.persistence = persistence;
        this.valueFactory = valueFactory;

        persistence.addTripleTableDependency("reasoner_justifications","triple_id");
        persistence.addTripleTableDependency("reasoner_just_supp_triples","triple_id");
    }

    /**
     * Initialise the database, creating or upgrading tables if they do not exist or are of the wrong version.
     * This method must only be called after the initDatabase of the wrapped KiWiPersistence has been evaluated.
     */
    public void initDatabase() throws SQLException {
        persistence.initDatabase(
                "reasoner",
                new String[] {
                        "reasoner_programs", "reasoner_program_namespaces", "reasoner_program_rules", "reasoner_rules",
                        "reasoner_justifications", "reasoner_just_supp_triples", "reasoner_just_supp_rules"
                }
        );
    }

    /**
     * Drop the versioning tables; this method must be called before the dropDatabase method of the underlying
     * KiWiPersistence is called.
     *
     * @throws SQLException
     */
    public void dropDatabase() throws SQLException {
        persistence.dropDatabase("reasoner");
    }

    /**
     * Return a connection from the connection pool which already has the auto-commit disabled.
     *
     * @return a fresh JDBC connection from the connection pool
     * @throws java.sql.SQLException in case a new connection could not be established
     */
    public KiWiReasoningConnection getConnection() throws SQLException {
        return new KiWiReasoningConnection(persistence, persistence.getDialect(), persistence.getCacheManager(), valueFactory );
    }


    public KiWiDialect getDialect() {
        return persistence.getDialect();
    }


}
