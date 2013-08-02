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

import com.google.common.util.concurrent.AtomicLongMap;
import org.apache.marmotta.kiwi.caching.KiWiCacheManager;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.persistence.util.ScriptRunner;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiPersistence {

    private static Logger log = LoggerFactory.getLogger(KiWiPersistence.class);

    // internal KiWi persistence ID (used for pool name)
    private static int KIWI_ID = 0;

    /**
     * The connection pool for managing JDBC connections
     */
    private DataSource connectionPool;

    private PoolProperties        poolConfig;

    private KiWiCacheManager      cacheManager;

    private KiWiGarbageCollector  garbageCollector;

    /**
     * The KiWi configuration for this persistence.
     */
    private KiWiConfiguration     configuration;

    /**
     * A map holding in-memory sequences to be used for sequence caching in case the appropriate configuration option
     * is configued and batched commits are enabled.
     */
    private AtomicLongMap<String> memorySequences;

    private ReentrantLock          sequencesLock;


    /**
     * A reference to the value factory used to access this store. Used for notifications when to flush batches.
     */
    private KiWiValueFactory      valueFactory;


    /**
     * This lock allows setting the backend into maintenance mode (by locking the write lock), which essentially
     * grants an exclusive access to the database. This is currently used by the garbage collector, but can also
     * be used in other situations-
     */
    private boolean         maintenance;


    @Deprecated
    public KiWiPersistence(String name, String jdbcUrl, String db_user, String db_password, KiWiDialect dialect) {
        this(new KiWiConfiguration(name,jdbcUrl,db_user,db_password,dialect));
    }

    public KiWiPersistence(KiWiConfiguration configuration) {
        this.configuration = configuration;
        this.maintenance = false;
        this.sequencesLock = new ReentrantLock();

        // init JDBC connection pool
        initConnectionPool();

        // init EHCache caches
        initCachePool();

        // init garbage collector thread
        initGarbageCollector();

        try {
            logPoolInfo();
        } catch (SQLException e) {

        }

    }

    public KiWiDialect getDialect() {
        return configuration.getDialect();
    }

    public KiWiCacheManager getCacheManager() {
        return cacheManager;
    }


    private void initCachePool() {
        cacheManager = new KiWiCacheManager(configuration.getName());
    }


    private void initConnectionPool() {
        poolConfig = new PoolProperties();
        poolConfig.setName("kiwi-" + (++KIWI_ID));
        poolConfig.setUrl(configuration.getJdbcUrl());
        poolConfig.setDriverClassName(configuration.getDialect().getDriverClass());
        poolConfig.setUsername(configuration.getDbUser());
        poolConfig.setPassword(configuration.getDbPassword());
        poolConfig.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        poolConfig.setCommitOnReturn(true);
        poolConfig.setValidationQuery(configuration.getDialect().getValidationQuery());
        poolConfig.setLogValidationErrors(true);
        /*
        poolConfig.setLogAbandoned(true);
        poolConfig.setRemoveAbandoned(true);
        */

        // interceptors
        if(configuration.isQueryLoggingEnabled()) {
            poolConfig.setJdbcInterceptors(
                    "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"   +
                            "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;" +
                            "org.apache.tomcat.jdbc.pool.interceptor.SlowQueryReport"
            );
        } else {
            poolConfig.setJdbcInterceptors(
                    "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"   +
                            "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer"
            );
        }

        if(log.isDebugEnabled()) {
            poolConfig.setSuspectTimeout(30);
            poolConfig.setLogAbandoned(true);
        }


        connectionPool = new DataSource(poolConfig);

    }

    private void initGarbageCollector() {
        this.garbageCollector = new KiWiGarbageCollector(this);

        garbageCollector.addNodeTableDependency("triples","subject");
        garbageCollector.addNodeTableDependency("triples","predicate");
        garbageCollector.addNodeTableDependency("triples","object");
        garbageCollector.addNodeTableDependency("triples","context");
        garbageCollector.addNodeTableDependency("triples","creator");
        garbageCollector.addNodeTableDependency("nodes","ltype");

    }

    /**
     * Initialise in-memory sequences if the feature is enabled.
     */
    public void initSequences(String scriptName) {
        if(configuration.isBatchCommit() && configuration.isMemorySequences()) {
            sequencesLock.lock();
            try {
                if(memorySequences == null) {
                    memorySequences = AtomicLongMap.create();
                }

                try {
                    Connection con = getJDBCConnection(true);
                    try {
                        for(String sequenceName : getDialect().listSequences(scriptName)) {

                            // load sequence value from database
                            // if there is a preparation needed to update the transaction, run it first
                            if(getDialect().hasStatement(sequenceName+".prep")) {
                                PreparedStatement prepNodeId = con.prepareStatement(getDialect().getStatement(sequenceName+".prep"));
                                prepNodeId.executeUpdate();
                                prepNodeId.close();
                            }

                            PreparedStatement queryNodeId = con.prepareStatement(getDialect().getStatement(sequenceName));
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
                        releaseJDBCConnection(con);
                    }
                } catch(SQLException ex) {
                    log.warn("database error: could not initialise in-memory sequences",ex);
                }
            } finally {
                sequencesLock.unlock();
            }
        }
    }

    public void logPoolInfo() throws SQLException {
        log.debug("num_busy_connections:    {}", connectionPool.getNumActive());
        log.debug("num_idle_connections:    {}", connectionPool.getNumIdle());

    }


    public void initDatabase() throws SQLException {
        initDatabase("base", new String[] {"nodes", "triples", "namespaces","metadata"});
    }


    /**
     * Initialise the database, creating or upgrading tables if they do not exist or are of the wrong version.
     *
     * @param scriptName the name of the script to use for create or update (e.g. "base" or "versioning")
     */
    public void initDatabase(String scriptName, String[] checkTables) throws SQLException {
        // get a database connection and check which version the database is (if it exists)
        KiWiConnection connection = getConnection();
        try {
            Set<String> tables = connection.getDatabaseTables();

            if(log.isDebugEnabled()) {
                log.debug("database tables:");
                for(String table : tables) {
                    log.debug("- found table: {}",table);
                }
            }

            // check existence of all tables; if the necessary tables are not there, they need to be created
            boolean createNeeded = false;
            for(String tableName : checkTables) {
                createNeeded = createNeeded || !tables.contains(tableName);
            }
            if(createNeeded) {
                log.info("creating new KiWi database ...");

                ScriptRunner runner = new ScriptRunner(connection.getJDBCConnection(), false, false);
                runner.runScript(new StringReader(configuration.getDialect().getCreateScript(scriptName)));

            } else {
                int version = connection.getDatabaseVersion();

                String updateScript = configuration.getDialect().getMigrationScript(version,scriptName);
                if(updateScript != null && updateScript.length() > 0) {
                    log.info("upgrading existing KiWi database from version {} to version {}", version, configuration.getDialect().getVersion());

                    ScriptRunner runner = new ScriptRunner(connection.getJDBCConnection(), false, false);
                    runner.runScript(new StringReader(updateScript));

                } else {
                    log.info("connecting to existing KiWi database (version: {})",version);
                }
            }
            connection.getJDBCConnection().commit();
        } catch (SQLException ex) {
            log.error("SQL exception while initialising database, rolling back");
            connection.rollback();
            throw ex;
        } catch (IOException ex) {
            log.error("I/O exception while initialising database, rolling back");
            connection.rollback();
        } finally {
            connection.close();
        }

        // init the in-memory sequences
        initSequences(scriptName);
    }

    /**
     * Remove all KiWi base tables from the SQL database. This method will run the drop script of the respective dialect and
     * return.
     *
     * @throws SQLException
     */
    public void dropDatabase() throws SQLException {
        dropDatabase("base");
    }

    /**
     * Remove all KiWi tables from the SQL database. This method will run the drop script of the respective dialect and
     * return.
     *
     *
     * @param scriptName the name of the script to use for drop (e.g. "base" or "versioning")
     * @throws SQLException
     */
    public void dropDatabase(final String scriptName) throws SQLException {
        // log connection pool information
        logPoolInfo();

        // we start this in a separate thread because there might still be a lock on the database tables
        forceCloseConnections();

        try {
            // get a database connection and check which version the database is (if it exists)
            KiWiConnection connection = getConnection();
            try {
                Set<String> tables = connection.getDatabaseTables();

                if(log.isDebugEnabled()) {
                    log.debug("BEFORE DROP: database tables");
                    for(String table : tables) {
                        log.debug("- found table: {}",table);
                    }
                }

                ScriptRunner runner = new ScriptRunner(connection.getJDBCConnection(), false, false);
                runner.runScript(new StringReader(configuration.getDialect().getDropScript(scriptName)));


                if(log.isDebugEnabled()) {
                    tables = connection.getDatabaseTables();
                    log.debug("AFTER DROP: database tables");
                    for(String table : tables) {
                        log.debug("- found table: {}",table);
                    }
                }
                connection.getJDBCConnection().commit();
            } catch (SQLException ex) {
                log.error("SQL exception while dropping database, rolling back");
                connection.rollback();
                throw ex;
            } catch (IOException ex) {
                log.error("I/O exception while dropping database, rolling back");
                connection.rollback();
            } finally {
                connection.close();
            }
        } catch(SQLException ex) {
            log.error("SQL exception while acquiring database connection");
        }
    }

    /**
     * Return a connection from the connection pool which already has the auto-commit disabled.
     *
     * @return a fresh JDBC connection from the connection pool
     * @throws SQLException in case a new connection could not be established
     */
    public KiWiConnection getConnection() throws SQLException {
        if(connectionPool != null) {
            KiWiConnection con = new KiWiConnection(this,configuration.getDialect(),cacheManager);
            if(getDialect().isBatchSupported()) {
                con.setBatchCommit(configuration.isBatchCommit());
                con.setBatchSize(configuration.getBatchSize());
            }
            return con;
        } else {
            throw new SQLException("connection pool is closed, database connections not available");
        }
    }

    /**
     * Return a raw JDBC connection from the connection pool, which already has the auto-commit disabled.
     * @return
     * @throws SQLException
     */
    public Connection getJDBCConnection() throws SQLException {
        return getJDBCConnection(false);
    }

    /**
     * Return a raw JDBC connection from the connection pool, which already has the auto-commit disabled.
     * @return
     * @throws SQLException
     */
    public Connection getJDBCConnection(boolean maintenance) throws SQLException {
        synchronized (this) {
            if(this.maintenance) {
                try {
                    this.wait();
                } catch (InterruptedException e) { }
            }
            if(maintenance) {
                this.maintenance = true;
            }
        }

        if(connectionPool != null) {
            Connection conn = connectionPool.getConnection();
            conn.setAutoCommit(false);

            return conn;
        } else {
            throw new SQLException("connection pool is closed, database connections not available");
        }
    }


    /**
     * Release the JDBC connection passed as argument. This method will close the connection and release
     * any locks that might be held by the caller.
     * @param con
     * @throws SQLException
     */
    public void releaseJDBCConnection(Connection con) throws SQLException {
        try {
            con.close();
        } finally {
            synchronized (this) {
                if(this.maintenance) {
                    this.maintenance = false;
                    this.notifyAll();
                }
            }
        }
    }

    private void forceCloseConnections() {
        if(connectionPool != null) {
            connectionPool.close(true);
        }

        connectionPool = new DataSource(poolConfig);
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
        garbageCollector.addNodeTableDependency(tableName, columnName);
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
        garbageCollector.addTripleTableDependency(tableName, columnName);
    }

    /**
     * Return a Sesame RepositoryResult of statements according to the query pattern given in the arguments. Each of
     * the parameters subject, predicate, object and context may be null, indicating a wildcard query. If the boolean
     * parameter "inferred" is set to true, the result will also include inferred triples, if it is set to false only
     * base triples.
     * <p/>
     * The RepositoryResult holds a direct connection to the database and needs to be closed properly, or otherwise
     * the system might run out of resources. The returned RepositoryResult will try its best to clean up when the
     * iteration has completed or the garbage collector calls the finalize() method, but this can take longer than
     * necessary.
     * <p/>
     * This method will create a new database connection for running the query which is only released when the
     * result is closed.
     *
     *
     * @param subject    the subject to query for, or null for a wildcard query
     * @param predicate  the predicate to query for, or null for a wildcard query
     * @param object     the object to query for, or null for a wildcard query
     * @param context    the context to query for, or null for a wildcard query
     * @param inferred   if true, the result will also contain triples inferred by the reasoner, if false not
     * @return a new RepositoryResult with a direct connection to the database; the result should be properly closed
     *         by the caller
     */
    public RepositoryResult<Statement> listTriples(KiWiResource subject, KiWiUriResource predicate, KiWiNode object, KiWiResource context, boolean inferred) throws SQLException {
        final KiWiConnection conn = getConnection();

        return new RepositoryResult<Statement>(conn.listTriples(subject,predicate,object,context,inferred)) {
            @Override
            protected void handleClose() throws RepositoryException {
                super.handleClose();
                try {
                    if(!conn.isClosed()) {
                        conn.commit();
                        conn.close();
                    }
                } catch (SQLException ex) {
                    throw new RepositoryException("SQL error when closing database connection",ex);
                }
            }

            @Override
            protected void finalize() throws Throwable {
                handleClose();
                super.finalize();
            }
        };
    }


    public void initialise() {
        garbageCollector.start();
    }

    public void shutdown() {
        if(!configuration.isCommitSequencesOnCommit()) {
            log.info("storing in-memory sequences in database ...");
            try {
                KiWiConnection connection = getConnection();
                try {
                    connection.commitMemorySequences();
                    connection.commit();
                } finally {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("could not store back values of in-memory sequences", e);
            }
        }


        garbageCollector.shutdown();
        cacheManager.shutdown();
        connectionPool.close();

        connectionPool = null;
        memorySequences = null;
    }

    /**
     * Remove all elements from the cache
     */
    public void clearCache() {
        cacheManager.clear();
    }


    public void setValueFactory(KiWiValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    public KiWiValueFactory getValueFactory() {
        return valueFactory;
    }

    public KiWiConfiguration getConfiguration() {
        return configuration;
    }

    public AtomicLongMap<String> getMemorySequences() {
        return memorySequences;
    }

    public long incrementAndGetMemorySequence(String name) {
        if(memorySequences != null) {
            return memorySequences.incrementAndGet(name);
        } else {
            return 0;
        }
    }


    public void garbageCollect() throws SQLException {
        this.garbageCollector.garbageCollect();
    }

    public boolean checkConsistency() throws SQLException {
        return garbageCollector.checkConsistency();
    }
}
