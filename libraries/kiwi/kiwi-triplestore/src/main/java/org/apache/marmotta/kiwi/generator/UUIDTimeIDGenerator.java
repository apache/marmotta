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

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.ext.FileBasedTimestampSynchronizer;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Generate a long id using the most significant bits of a time-based UUID.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class UUIDTimeIDGenerator implements IDGenerator {

    private static Logger log = LoggerFactory.getLogger(UUIDTimeIDGenerator.class);

    TimeBasedGenerator generator;
    FileBasedTimestampSynchronizer synchronizer;

    File uuid1, uuid2;

    public UUIDTimeIDGenerator() {
        try {
            uuid1 = new File(System.getProperty("java.io.tmpdir") + File.separator + "uuid1.lck");
            uuid2 = new File(System.getProperty("java.io.tmpdir") + File.separator + "uuid2.lck");

            synchronizer = new FileBasedTimestampSynchronizer(uuid1, uuid2);

            generator = Generators.timeBasedGenerator(EthernetAddress.fromInterface(), synchronizer);
        } catch (IOException e) {
            log.error("error initialising time-based UUID generator",e);
        }
    }


    /**
     * Initialise the generator for the given persistence and module
     */
    @Override
    public void init(KiWiPersistence persistence, String scriptName) {
    }

    /**
     * Commit the current state of memory sequences to the database using the connection passed as second argument.
     *
     * @param persistence
     * @param con
     * @throws java.sql.SQLException
     */
    @Override
    public void commit(KiWiPersistence persistence, Connection con) throws SQLException {
    }


    /**
     * Shut down this id generator, performing any cleanups that might be necessary.
     *
     * @param persistence
     */
    @Override
    public void shutdown(KiWiPersistence persistence) {
        try {
            synchronizer.deactivate();

            uuid1.delete();
            uuid2.delete();
        } catch (IOException e) {
            log.error("error deactivating file synchronizer ...");
        }
    }

    /**
     * Return the next unique id for the type with the given name using the generator's id generation strategy.
     *
     * @param name
     * @return
     */
    @Override
    public synchronized long getId(String name, KiWiConnection connection) {
        return generator.generate().getMostSignificantBits();
    }
}
