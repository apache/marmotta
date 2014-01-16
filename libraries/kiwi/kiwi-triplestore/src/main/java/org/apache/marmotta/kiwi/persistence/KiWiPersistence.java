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

import org.apache.marmotta.kiwi.caching.*;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.generator.IDGenerator;
import org.apache.marmotta.kiwi.generator.SnowflakeIDGenerator;
import org.apache.marmotta.kiwi.persistence.util.ScriptRunner;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

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
     * A reference to the value factory used to access this store. Used for notifications when to flush batches.
     */
    private KiWiValueFactory      valueFactory;


    private IDGenerator    idGenerator;


    /**
     * This lock allows setting the backend into maintenance mode (by locking the write lock), which essentially
     * grants an exclusive access to the database. This is currently used by the garbage collector, but can also
     * be used in other situations-
     */
    private boolean         maintenance;

    private boolean         initialized = false;

    // in case the cache manager comes from outside, it is passed over here
    private EmbeddedCacheManager infinispan;

    @Deprecated
    public KiWiPersistence(String name, String jdbcUrl, String db_user, String db_password, KiWiDialect dialect) {
        this(new KiWiConfiguration(name,jdbcUrl,db_user,db_password,dialect));
    }

    public KiWiPersistence(KiWiConfiguration configuration) {
        this.configuration = configuration;
        this.maintenance = false;
    }

    public KiWiPersistence(KiWiConfiguration configuration, EmbeddedCacheManager infinispan) {
        this.configuration = configuration;
        this.maintenance = false;
        this.infinispan = infinispan;
    }


    public void initialise() {
        // init JDBC connection pool
        initConnectionPool();

        // init Infinispan caches
        initCachePool();

        // init garbage collector thread
        initGarbageCollector();

        try {
            logPoolInfo();
        } catch (SQLException e) {

        }

        idGenerator = new SnowflakeIDGenerator(configuration.getDatacenterId());

        log.info("database key generation strategy: Twitter Snowflake");

        //garbageCollector.start();

        initialized = true;
    }


    public KiWiDialect getDialect() {
        return configuration.getDialect();
    }

    public KiWiCacheManager getCacheManager() {
        return cacheManager;
    }


    private void initCachePool() {
        AdvancedExternalizer[] externalizers =  new AdvancedExternalizer[] {
                new TripleExternalizer(this),
                new UriExternalizer(),
                new BNodeExternalizer(),
                new StringLiteralExternalizer(),
                new DateLiteralExternalizer(),
                new BooleanLiteralExternalizer(),
                new IntLiteralExternalizer(),
                new DoubleLiteralExternalizer()
        };

        if(infinispan != null) {
            cacheManager = new KiWiCacheManager(infinispan,configuration, externalizers);
        } else {
            cacheManager = new KiWiCacheManager(configuration, externalizers);
        }
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

    public void logPoolInfo() throws SQLException {
        if(connectionPool != null) {
            log.debug("num_busy_connections:    {}", connectionPool.getNumActive());
            log.debug("num_idle_connections:    {}", connectionPool.getNumIdle());
        } else {
            log.debug("connection pool not initialized");
        }

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
        if(!initialized) {
            throw new SQLException("persistence backend not initialized; call initialise before acquiring a connection");
        }

        if(connectionPool != null) {
            KiWiConnection con = new KiWiConnection(this,configuration.getDialect(),cacheManager);
            if(getDialect().isBatchSupported()) {
                con.setBatchCommit(configuration.isTripleBatchCommit());
                con.setBatchSize(configuration.getTripleBatchSize());
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

        if(initialized && connectionPool != null) {
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


    public void shutdown() {
        log.info("shutting down KiWi persistence ...");
        initialized = false;

        idGenerator.shutdown(this);
        garbageCollector.shutdown();
        cacheManager.shutdown();
        connectionPool.close();

        connectionPool = null;
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


    public void garbageCollect() throws SQLException {
        this.garbageCollector.garbageCollect();
    }

    public boolean checkConsistency() throws SQLException {
        return garbageCollector.checkConsistency();
    }

    public IDGenerator getIdGenerator() {
        return idGenerator;
    }


}
