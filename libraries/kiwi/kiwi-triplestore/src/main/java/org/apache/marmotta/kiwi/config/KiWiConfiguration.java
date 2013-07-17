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
package org.apache.marmotta.kiwi.config;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;

/**
 * An object to hold a KiWi persistence configuration. The configuration consists of:
 * <ul>
 *     <li>a name to identify the persistence configuration throughout the system</li>
 *     <li>a JDBC URL for connecting to the respective relational database</li>
 *     <li>a database user to access the database</li>
 *     <li>a database password for that user</li>
 *     <li>a KiWi dialect for representing the different SQL dialects</li>
 * </ul>
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiConfiguration {

    /**
     * A unique name for identifying this instance of KiWiPersistence. Can be used in case there are several
     * instances running in the same environment.
     */
    private String name;
    private String jdbcUrl;
    private String dbUser;
    private String dbPassword;

    /**
     * The default context to use when no explicit context is given in createStatement. The KiWi triple store
     * does not support null values for the context of a triple, so this URL must be set to an appropriate value
     */
    private String defaultContext;

    /**
     * The context to use for storing all inferred triples. The value set here will override all contexts
     * given to addInferredTriple, because KiWi always stores all inferred triples in the same context.
     */
    private String inferredContext;


    /**
     * The SQL dialect to use
     */
    private KiWiDialect dialect;

    /**
     * A flag indicating if the query logging (Tomcat JDBC SlowQueryReport) is enabled or not.
     */
    private boolean queryLoggingEnabled = false;

    /**
     * Enable batched commit (if supported by the database dialect). If this is enabled, the KiWiConnection will
     * use an in-memory buffer for stored triples and nodes that are committed in a batch once the limit is reached
     * or the connection committed. Enabling this can significantly improve the performance (EXPERIMENTAL).
     */
    private boolean batchCommit;

    private int batchSize = 10000;

    /**
     * Size of the database cursor for pre-fetching rows on database supporting this feature. If the size is set to 0,
     * no cursor is used and all rows are retrieved in one batch.
     *
     * @see java.sql.PreparedStatement#setFetchSize(int)
     */
    private int cursorSize = 1000;

    /**
     * If enabled, and batchCommit is also true, load sequence values into static memory fields once and increment
     * values purely in-memory. The last value is then written back on batch commits.
     */
    private boolean memorySequences = true;

    public KiWiConfiguration(String name, String jdbcUrl, String dbUser, String dbPassword, KiWiDialect dialect) {
        this(name, jdbcUrl, dbUser, dbPassword, dialect, null, null);
    }

    public KiWiConfiguration(String name, String jdbcUrl, String dbUser, String dbPassword, KiWiDialect dialect, String defaultContext, String inferredContext) {
        this.dbPassword = dbPassword;
        this.dbUser = dbUser;
        this.dialect = dialect;
        this.jdbcUrl = jdbcUrl;
        this.name = name;
        this.defaultContext = defaultContext;
        this.inferredContext = inferredContext;

        batchCommit = dialect.isBatchSupported();
    }


    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbUser() {
        return dbUser;
    }

    public KiWiDialect getDialect() {
        return dialect;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getName() {
        return name;
    }

    public boolean isQueryLoggingEnabled() {
        return queryLoggingEnabled;
    }

    public void setQueryLoggingEnabled(boolean queryLoggingEnabled) {
        this.queryLoggingEnabled = queryLoggingEnabled;
    }

    public String getDefaultContext() {
        return defaultContext;
    }

    public void setDefaultContext(String defaultContext) {
        this.defaultContext = defaultContext;
    }

    public String getInferredContext() {
        return inferredContext;
    }

    public void setInferredContext(String inferredContext) {
        this.inferredContext = inferredContext;
    }

    /**
     * Return true if batched commit is enabled. If this is enabled, the KiWiConnection will
     * use an in-memory buffer for stored triples and nodes that are committed in a batch once the limit is reached
     * or the connection committed. Enabling this can significantly improve the performance (EXPERIMENTAL).
     */
    public boolean isBatchCommit() {
        return batchCommit;
    }

    /**
     * Enable batched commit (if supported by the database dialect). If this is enabled, the KiWiConnection will
     * use an in-memory buffer for stored triples and nodes that are committed in a batch once the limit is reached
     * or the connection committed. Enabling this can significantly improve the performance (EXPERIMENTAL).
     */
    public void setBatchCommit(boolean batchCommit) {
        if(dialect.isBatchSupported()) {
            this.batchCommit = batchCommit;
        }
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Size of the database cursor for pre-fetching rows on database supporting this feature. If the size is set to 0,
     * no cursor is used and all rows are retrieved in one batch.
     *
     * @see java.sql.PreparedStatement#setFetchSize(int)
     */
    public int getCursorSize() {
        return cursorSize;
    }

    /**
     * Size of the database cursor for pre-fetching rows on database supporting this feature. If the size is set to 0,
     * no cursor is used and all rows are retrieved in one batch.
     *
     * @see java.sql.PreparedStatement#setFetchSize(int)
     */
    public void setCursorSize(int cursorSize) {
        this.cursorSize = cursorSize;
    }

    public boolean isMemorySequences() {
        return memorySequences;
    }

    /**
     * Enable in-memory sequences. If enabled, and batchCommit is also true, load sequence values into static memory
     * fields once and increment values purely in-memory. The last value is then written back on batch commits. This
     * feature can avoid many database accesses and connections and therefore give significant performance improvements.
     * (EXPERIMENTAL).
     */
    public void setMemorySequences(boolean memorySequences) {
        this.memorySequences = memorySequences;
    }
}
