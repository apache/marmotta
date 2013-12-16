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

package org.apache.marmotta.kiwi.persistence.registry;

import org.apache.marmotta.commons.sesame.tripletable.IntArray;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is used for keeping triples that are used by several transactions in parallel in sync. It allows
 * a transaction that creates a triple to register this triple and make the triple ID available to other transactions
 * to avoid duplicates. When a transaction commits, it then releases all its triple registrations.
 * <p/>
 * The implementation is based on a very simple database table (REGISTRY). When a transaction creates a triple
 * with a new ID, it temporarily inserts a row mapping the (subject,predicate,object,context) -> triple ID. Other
 * transactions trying to create the same triple can then first lookup this ID. If they do so successfully, they will
 * also insert a row to the registry.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class DBTripleRegistry implements KiWiTripleRegistry {

    private static Logger log = LoggerFactory.getLogger(DBTripleRegistry.class);

    private KiWiStore store;


    public DBTripleRegistry(KiWiStore store) {
        this.store = store;
    }

    @Override
    public void registerKey(IntArray key, long transactionId, long tripleId) {
        KiWiConnection con = aqcuireConnection();
        try {
            PreparedStatement stmt = con.getPreparedStatement("registry.register");
            synchronized (stmt) {
                stmt.setLong(1, key.longHashCode());
                stmt.setLong(2, tripleId);
                stmt.setLong(3, transactionId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("error registering key in temporary database table",e);
        } finally {
            releaseConnection(con);
        }
    }



    @Override
    public long lookupKey(IntArray key) {
        KiWiConnection con = aqcuireConnection();
        try {
            PreparedStatement stmt = con.getPreparedStatement("registry.lookup");
            synchronized (stmt) {
                stmt.setLong(1, key.longHashCode());

                try(ResultSet r = stmt.executeQuery()) {
                    if(r.next()) {
                        return r.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("error looking up key in temporary database table",e);
        } finally {
            releaseConnection(con);
        }
        return -1;
    }


    @Override
    public void releaseTransaction(long transactionId) {
        KiWiConnection con = aqcuireConnection();
        try {
            PreparedStatement stmt = con.getPreparedStatement("registry.release");
            synchronized (stmt) {
                stmt.setLong(1, transactionId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("error releasing key in temporary database table",e);
        } finally {
            releaseConnection(con);
        }

    }


    @Override
    public void deleteKey(IntArray key) {
        KiWiConnection con = aqcuireConnection();
        try {
            PreparedStatement stmt = con.getPreparedStatement("registry.delete");
            synchronized (stmt) {
                stmt.setLong(1, key.longHashCode());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("error deleting key in temporary database table",e);
        } finally {
            releaseConnection(con);
        }

    }


    protected KiWiConnection aqcuireConnection() {
        try {
            return store.getPersistence().getConnection();
        } catch(SQLException ex) {
            log.error("could not acquire database connection", ex);
            throw new RuntimeException(ex);
        }
    }

    protected void releaseConnection(KiWiConnection con) {
        try {
            con.getJDBCConnection().commit();
            con.close();
        } catch (SQLException ex) {
            log.error("could not release database connection", ex);
            throw new RuntimeException(ex);
        }
    }


}
