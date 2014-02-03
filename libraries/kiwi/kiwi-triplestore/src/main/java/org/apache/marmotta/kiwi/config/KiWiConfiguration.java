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

import java.util.ArrayList;
import java.util.List;

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
     * Enable batched commit for triples (if supported by the database dialect). If this is enabled,
     * the KiWiConnection will use an in-memory buffer for stored triples that are committed in a batch
     * once the limit is reached or the connection committed. Enabling this can significantly improve the
     * performance, and is usually quite safe for triples.
     */
    private boolean tripleBatchCommit;


    private int tripleBatchSize = 10000;

    /**
     * Size of the database cursor for pre-fetching rows on database supporting this feature. If the size is set to 0,
     * no cursor is used and all rows are retrieved in one batch.
     *
     * @see java.sql.PreparedStatement#setFetchSize(int)
     */
    private int cursorSize = 1000;

    private boolean fulltextEnabled     = false;
    private String[] fulltextLanguages;


    private int uriCacheSize = 500000;

    private int bNodeCacheSize = 10000;

    private int literalCacheSize = 100000;

    private int tripleCacheSize = 100000;

    private int namespaceCacheSize = 500;

    private int datacenterId = 0;


    private RegistryStrategy registryStrategy = RegistryStrategy.CACHE;

    /**
     * Enable to turn on cluster mode (e.g. for cache replication)
     */
    private boolean clustered = false;

    /**
     * Name of the cache cluster this triplestore belongs to.
     */
    private String clusterName = "Marmotta";

    /**
     * Multicast port of the cache cluster is listening on and distributing cache updates.
     */
    private int clusterPort = 46655;

    /**
     * Multicast address of the cache cluster is listening on and distributing cache updates.
     */
    private String clusterAddress = "228.6.7.8";

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

        tripleBatchCommit = dialect.isBatchSupported();
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
     * Enable batched commit for triples (if supported by the database dialect). If this is enabled,
     * the KiWiConnection will use an in-memory buffer for stored triples that are committed in a batch
     * once the limit is reached or the connection committed. Enabling this can significantly improve the
     * performance, and is usually quite safe for triples.
     */
    public boolean isTripleBatchCommit() {
        return tripleBatchCommit;
    }

    /**
     * Enable batched commit for triples (if supported by the database dialect). If this is enabled,
     * the KiWiConnection will use an in-memory buffer for stored triples that are committed in a batch
     * once the limit is reached or the connection committed. Enabling this can significantly improve the
     * performance, and is usually quite safe for triples.
     */
    public void setTripleBatchCommit(boolean tripleBatchCommit) {
        if(dialect.isBatchSupported()) {
            this.tripleBatchCommit = tripleBatchCommit;
        }
    }

    public int getTripleBatchSize() {
        return tripleBatchSize;
    }

    public void setTripleBatchSize(int tripleBatchSize) {
        this.tripleBatchSize = tripleBatchSize;
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

    /**
     * Return true in case fulltext support is enabled in this configuration. If this is the case, the SPARQL module
     * will prepare the database with appropriate fulltext index support. Since this adds additional overhead, it is
     * not enabled by default.
     *
     * @return
     */
    public boolean isFulltextEnabled() {
        return fulltextEnabled;
    }

    /**
     * Set to true in case fulltext support is enabled in this configuration. If this is the case, the SPARQL module
     * will prepare the database with appropriate fulltext index support. Since this adds additional overhead, it is
     * not enabled by default.
     */
    public void setFulltextEnabled(boolean fulltextEnabled) {
        this.fulltextEnabled = fulltextEnabled;
    }

    /**
     * Return the languages (ISO codes) for which to add specific fulltext search support. The SPARQL module will add a
     * separate fulltext index for each supported language, adding additional overhead. In case you only want generic
     * fulltext support, use the empty array.
     */
    public String[] getFulltextLanguages() {
        return fulltextLanguages;
    }

    /**
     * Set the languages (ISO codes) for which to add specific fulltext search support. The SPARQL module will add a
     * separate fulltext index for each supported language, adding additional overhead. In case you only want generic
     * fulltext support, use the empty array.
     */
    public void setFulltextLanguages(String[] fulltextLanguages) {
        this.fulltextLanguages = fulltextLanguages;
    }

    /**
     * Set the languages (ISO codes) for which to add specific fulltext search support. The SPARQL module will add a
     * separate fulltext index for each supported language, adding additional overhead. In case you only want generic
     * fulltext support, use the empty array.
     */
    public void setFulltextLanguages(List<String> fulltextLanguages) {
        this.fulltextLanguages = new ArrayList<String>(fulltextLanguages).toArray(new String[fulltextLanguages.size()]);
    }


    /**
     * The maximum size of the literal cache used by the KiWiValueFactory (default: 100000)
     * @return
     */
    public int getLiteralCacheSize() {
        return literalCacheSize;
    }

    /**
     * The maximum size of the literal cache used by the KiWiValueFactory (default: 100000)
     */
    public void setLiteralCacheSize(int literalCacheSize) {
        this.literalCacheSize = literalCacheSize;
    }

    /**
     * The maximum size of the BNode cache used by the KiWiValueFactory (default: 10000)
     * @return
     */
    public int getBNodeCacheSize() {
        return bNodeCacheSize;
    }

    /**
     * The maximum size of the BNode cache used by the KiWiValueFactory (default: 10000)
     */
    public void setBNodeCacheSize(int bNodeCacheSize) {
        this.bNodeCacheSize = bNodeCacheSize;
    }

    /**
     * The maximum size of the URI cache used by the KiWiValueFactory (default: 500000)
     * @return
     */
    public int getUriCacheSize() {
        return uriCacheSize;
    }

    /**
     * The maximum size of the URI cache used by the KiWiValueFactory (default: 500000)
     */
    public void setUriCacheSize(int uriCacheSize) {
        this.uriCacheSize = uriCacheSize;
    }


    /**
     * The maximum size of the triple cache used for looking up triples by id
     * @return
     */
    public int getTripleCacheSize() {
        return tripleCacheSize;
    }

    /**
     * The maximum size of the triple cache used for looking up triples by id
     */
    public void setTripleCacheSize(int tripleCacheSize) {
        this.tripleCacheSize = tripleCacheSize;
    }

    /**
     * The maximum size of the namespace cache used for looking up namespaces by prefix and uri
     */
    public int getNamespaceCacheSize() {
        return namespaceCacheSize;
    }

    /**
     * The maximum size of the namespace cache used for looking up namespaces by prefix and uri
     */
    public void setNamespaceCacheSize(int namespaceCacheSize) {
        this.namespaceCacheSize = namespaceCacheSize;
    }

    /**
     * The datacenter ID of this server for generating unique database IDs. If not given, a random value will
     * be generated.
     *
     * @return
     */
    public int getDatacenterId() {
        return datacenterId;
    }

    /**
     * The datacenter ID of this server for generating unique database IDs.
     * @return
     */
    public void setDatacenterId(int datacenterId) {
        this.datacenterId = datacenterId;
    }


    /**
     * Return the strategy to use for synchronizing between transactions. Currently either "database" or "cache".
     * @return
     */
    public RegistryStrategy getRegistryStrategy() {
        return registryStrategy;
    }

    /**
     * Change the strategy to use for synchronizing between transactions. Currently either "database" or "cache".
     * @return
     */
    public void setRegistryStrategy(RegistryStrategy registryStrategy) {
        this.registryStrategy = registryStrategy;
    }

    /**
     * If true, support for special cluster features is enabled (e.g. replicated caches).
     *
     * @return
     */
    public boolean isClustered() {
        return clustered;
    }

    /**
     * If true, support for special cluster features is enabled (e.g. replicated caches).
     */
    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }


    /**
     * Return the name of the cluster. This name is e.g. used by the Infinispan cache to identify other cache members
     * in the same cluster,
     *
     * @return
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * Change the name of the cluster. This name is e.g. used by the Infinispan cache to identify other cache members
     * in the same cluster,
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * Return the multicast port used by the cache cluster this triplestore belongs to. This port number is
     * used by JGroups to distribute and receive cache synchronization updates. Triplestores with different
     * data should use different ports or addresses,
     * <p/>
     * Only used in case isClustered() is true
     *
     * @return
     */
    public int getClusterPort() {
        return clusterPort;
    }

    /**
     * Change the multicast port used by the cache cluster this triplestore belongs to. This port number is
     * used by JGroups to distribute and receive cache synchronization updates. Triplestores with different
     * data should use different ports or addresses.
     * <p/>
     * Only used in case isClustered() is true
     *
     * @return
     */
    public void setClusterPort(int clusterPort) {
        this.clusterPort = clusterPort;
    }

    /**
     * Return the multicast address used by the cache cluster this triplestore belongs to. This address is
     * used by JGroups to distribute and receive cache synchronization updates. Triplestores with different
     * data should use different ports or addresses,
     * <p/>
     * Only used in case isClustered() is true
     *
     * @return
     */
    public String getClusterAddress() {
        return clusterAddress;
    }

    /**
     * Change the multicast address used by the cache cluster this triplestore belongs to. This address is
     * used by JGroups to distribute and receive cache synchronization updates. Triplestores with different
     * data should use different ports or addresses,
     * <p/>
     * Only used in case isClustered() is true
     *
     * @return
     */
    public void setClusterAddress(String clusterAddress) {
        this.clusterAddress = clusterAddress;
    }
}
