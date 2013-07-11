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
package org.apache.marmotta.kiwi.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class implements a garbage collector for the database that cleans up deleted triples and nodes when they
 * are no longer referenced. It runs from time to time (default: on startup and every hour) and executes
 * a complex SQL query, checking for all tables referencing rows in the triples and nodes tables. References need
 * to be registered by other components via the addTripleTableDependency and addNodeTableDependency methods.
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiGarbageCollector extends Thread {

    private static Logger log = LoggerFactory.getLogger(KiWiGarbageCollector.class);

    private Set<TableDependency> tripleTableDependencies;
    private Set<TableDependency>  nodeTableDependencies;

    private long interval = 60 * 60 * 1000;

    private long round = 0;

    private KiWiPersistence persistence;

    private boolean shutdown = false;

    public KiWiGarbageCollector(KiWiPersistence persistence) {
        super("KiWi Garbage Collector");

        this.persistence = persistence;

        this.tripleTableDependencies = new HashSet<TableDependency>();
        this.nodeTableDependencies   = new HashSet<TableDependency>();
    }

    /**
     * Get the interval to wait between garbage collections (milliseconds)
     */
    public long getInterval() {
        return interval;
    }

    /**
     * Set the interval to wait between garbage collections (milliseconds)
     * @param interval
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * Add information about a dependency of a column in some table to the "triples" table; this information
     * is used when cleaning up unreferenced deleted entries in the triples table. In theory, we could
     * get this information from the database, but each database has a very different way of doing this, so
     * it is easier to simply let dependent modules register this information.
     * @param tableName
     * @param columnName
     */
    public void addTripleTableDependency(String tableName, String columnName) {
        tripleTableDependencies.add(new TableDependency(tableName,columnName));
    }

    /**
     * Add information about a dependency of a column in some table to the "nodes" table; this information
     * is used when cleaning up unreferenced deleted entries in the nodes table. In theory, we could
     * get this information from the database, but each database has a very different way of doing this, so
     * it is easier to simply let dependent modules register this information.
     * @param tableName
     * @param columnName
     */
    public void addNodeTableDependency(String tableName, String columnName) {
        nodeTableDependencies.add(new TableDependency(tableName,columnName));
    }


    protected int garbageCollect() throws SQLException {
        round++;

        long start = System.currentTimeMillis();

        Connection con = persistence.getJDBCConnection(true);
        try {
            int count = 0;

            // garbage collect triples
            try {
                String gcTripleQuery = buildGCTriplesQuery();
                PreparedStatement stmtGcTriples = con.prepareStatement(gcTripleQuery);
                count += stmtGcTriples.executeUpdate();
                stmtGcTriples.close();
                con.commit();
            } catch (SQLException ex) {
                con.rollback();

                log.warn("SQL error while executing garbage collection on triples table: {}", ex.getMessage());
            }

            // garbage collect nodes (only every 10th garbage collection, only makes sense when we previously deleted triples ...)
            if(count > 0 && round % 10 == 1) {
                // flush all nodes from the value factory first
                if(persistence.getValueFactory() != null) {
                    KiWiConnection vfConnection = persistence.getConnection();
                    try {
                        persistence.getValueFactory().flushBatch(vfConnection);
                    } finally {
                        vfConnection.close();
                    }
                }


                // then delete all unconnected nodes
                try {
                    String gcNodesQuery = buildGCNodesQuery();
                    PreparedStatement stmtGcNodes = con.prepareStatement(gcNodesQuery);
                    count += stmtGcNodes.executeUpdate();
                    stmtGcNodes.close();
                    con.commit();
                } catch (SQLException ex) {
                    con.rollback();

                    log.warn("SQL error while executing garbage collection on nodes table: {}", ex.getMessage());
                }
            }
            log.info("... cleaned up {} entries (duration: {} ms)", count, (System.currentTimeMillis()-start));

            return count;
        } finally {
            persistence.releaseJDBCConnection(con);
        }
    }


    /**
     * Run the garbage collector thread. The thread will run garbage collection on startup, and then in a loop wait
     * for one hour before running again.
     *
     * @see #start()
     * @see #stop()
     */
    @Override
    public void run() {
        synchronized (this) {

            boolean started = false;

            while(!shutdown) {
                // don't run immediately on startup
                if(started) {
                    log.info("running garbage collection ...");
                    try {
                        int count = garbageCollect();
                    } catch (SQLException e) {
                        log.error("error while executing garbage collection: {}",e.getMessage());
                    }
                }
                started = true;
                try {
                    this.wait(interval);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void shutdown() {
        synchronized (this) {
            shutdown = true;
            this.notifyAll();
        }
    }

    private String buildGCTriplesQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM triples WHERE deleted = true");

        if(tripleTableDependencies.size() > 0) {
            builder.append(" AND NOT EXISTS (");

            Iterator<TableDependency> iterator = tripleTableDependencies.iterator();
            while (iterator.hasNext()) {
                TableDependency next = iterator.next();

                builder.append("(");
                builder.append("SELECT ");
                builder.append(next.column);
                builder.append(" FROM ");
                builder.append(next.table);
                builder.append(" WHERE ");
                builder.append(next.column);
                builder.append(" = triples.id");

                builder.append(")");

                if(iterator.hasNext()) {
                    builder.append(" UNION ");
                }
            }

            builder.append(")");
        }
        return builder.toString();
    }


    private String buildGCNodesQuery() {
        StringBuilder builder = new StringBuilder();

        if(nodeTableDependencies.size() > 0) {
            builder.append("DELETE FROM nodes T1 WHERE NOT EXISTS (");

            Iterator<TableDependency> iterator = nodeTableDependencies.iterator();
            while (iterator.hasNext()) {
                TableDependency next = iterator.next();

                builder.append("(");
                builder.append("SELECT ");
                builder.append(next.column);
                builder.append(" FROM ");
                builder.append(next.table);
                builder.append(" WHERE ");
                builder.append(next.column);
                builder.append(" = T1.id");

                builder.append(")");

                if(iterator.hasNext()) {
                    builder.append(" UNION ");
                }
            }

            builder.append(")");
        }
        return builder.toString();
    }


    private static class TableDependency {
        String table;
        String column;

        private TableDependency(String table, String column) {
            this.column = column;
            this.table = table;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TableDependency that = (TableDependency) o;

            if (!column.equals(that.column)) return false;
            if (!table.equals(that.table)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = table.hashCode();
            result = 31 * result + column.hashCode();
            return result;
        }
    }

}
