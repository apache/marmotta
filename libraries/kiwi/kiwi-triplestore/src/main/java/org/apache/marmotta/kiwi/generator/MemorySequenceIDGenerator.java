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

package org.apache.marmotta.kiwi.generator;

import com.google.common.util.concurrent.AtomicLongMap;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Generate unique IDs by incrementing an in-memory atomic long value for each sequence. This method is much faster
 * than database sequences and backwards compatible (because values are written back to the database on each commit),
 * but it does not work reliably if several applications access the same database.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MemorySequenceIDGenerator implements IDGenerator {
    private static Logger log = LoggerFactory.getLogger(MemorySequenceIDGenerator.class);

    /**
     * A map holding in-memory sequences to be used for sequence caching in case the appropriate configuration option
     * is configued and batched commits are enabled.
     */
    private AtomicLongMap<String> memorySequences;


    // keep track which memory sequences have been updated and need to be written back
    private Set<String> sequencesUpdated;

    private ReentrantLock sequencesLock;



    public MemorySequenceIDGenerator() {
        this.sequencesLock = new ReentrantLock();
        this.sequencesUpdated = new HashSet<>();
        memorySequences = AtomicLongMap.create();
    }

    /**
     * Initialise in-memory sequences if the feature is enabled.
     */
    @Override
    public void init(KiWiPersistence persistence, String scriptName) {
        sequencesLock.lock();
        try {
            try {
                Connection con = persistence.getJDBCConnection(true);
                try {
                    for(String sequenceName : persistence.getDialect().listSequences(scriptName)) {

                        // load sequence value from database
                        // if there is a preparation needed to update the transaction, run it first
                        if(persistence.getDialect().hasStatement(sequenceName+".prep")) {
                            PreparedStatement prepNodeId = con.prepareStatement(persistence.getDialect().getStatement(sequenceName+".prep"));
                            prepNodeId.executeUpdate();
                            prepNodeId.close();
                        }

                        PreparedStatement queryNodeId = con.prepareStatement(persistence.getDialect().getStatement(sequenceName));
                        ResultSet resultNodeId = queryNodeId.executeQuery();
                        try {
                            if(resultNodeId.next()) {
                                memorySequences.put(sequenceName,resultNodeId.getLong(1)-1);
                            } else {
                                throw new SQLException("the sequence did not return a new value");
                            }
                        } finally {
                            resultNodeId.close();
                        }

                        con.commit();
                    }
                } finally {
                    persistence.releaseJDBCConnection(con);
                }
            } catch(SQLException ex) {
                log.warn("database error: could not initialise in-memory sequences",ex);
            }
        } finally {
            sequencesLock.unlock();
        }

    }

    /**
     * Commit the current state of memory sequences to the database using the connection passed as second argument.
     *
     * @param persistence
     * @param con
     * @throws SQLException
     */
    @Override
    public void commit(KiWiPersistence persistence, Connection con) throws SQLException {
        sequencesLock.lock();
        try {
            // clear existing list of updated sequences
            Set<String> updated = this.sequencesUpdated;
            this.sequencesUpdated = new HashSet<>();

            try {
                for(Map.Entry<String,Long> entry : memorySequences.asMap().entrySet()) {
                    if( updated.contains(entry.getKey()) && entry.getValue() > 0) {
                        PreparedStatement updateSequence = con.prepareStatement(persistence.getDialect().getStatement(entry.getKey()+".set"));
                        updateSequence.setLong(1, entry.getValue());
                        if(updateSequence.execute()) {
                            updateSequence.getResultSet().close();
                        } else {
                            updateSequence.getUpdateCount();
                        }
                    }
                }
            } catch(SQLException ex) {
                // MySQL deadlock state, in this case we retry anyways
                if(!"40001".equals(ex.getSQLState())) {
                    log.error("SQL exception:",ex);
                }
                throw ex;
            }

        } finally {
            sequencesLock.unlock();
        }
    }

    /**
     * Shut down this id generator, performing any cleanups that might be necessary.
     *
     * @param persistence
     */
    @Override
    public void shutdown(KiWiPersistence persistence) {

    }

    /**
     * Return the next unique id for the type with the given name using the generator's id generation strategy.
     *
     * @param name
     * @return
     */
    @Override
    public long getId(String name, KiWiConnection connection) throws SQLException {
        sequencesUpdated.add(name);

        if(memorySequences != null) {
            return memorySequences.incrementAndGet(name);
        } else {
            return 0;
        }

    }
}
