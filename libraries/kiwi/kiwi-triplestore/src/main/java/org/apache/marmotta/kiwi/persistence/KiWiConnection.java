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

import com.google.common.base.Preconditions;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.common.primitives.Longs;
import info.aduna.iteration.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.marmotta.commons.sesame.model.LiteralCommons;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.sesame.tripletable.TripleTable;
import org.apache.marmotta.commons.util.DateUtils;
import org.apache.marmotta.kiwi.caching.CacheManager;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.exception.ResultInterruptedException;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.apache.marmotta.kiwi.persistence.util.ResultSetIteration;
import org.apache.marmotta.kiwi.persistence.util.ResultTransformerFunction;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A KiWiConnection offers methods for storing and retrieving KiWiTriples, KiWiNodes, and KiWiNamespaces in the
 * database. It wraps a JDBC connection which will be committed on commit(), rolled back on rollback() and
 * closed on close();
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiConnection implements AutoCloseable {

    private static Logger log = LoggerFactory.getLogger(KiWiConnection.class);


    protected KiWiDialect dialect;

    protected Connection connection;

    protected KiWiPersistence  persistence;

    protected CacheManager cacheManager;

    protected TripleTable<KiWiTriple> tripleBatch;

    /**
     * Cache nodes by database ID
     */
    private Map<Long,KiWiNode> nodeCache;

    /**
     * Cache triples by database ID
     */
    private Map<Long,KiWiTriple> tripleCache;


    /**
     * Cache URI resources by uri
     */
    private Map<String,KiWiUriResource> uriCache;


    /**
     * Cache BNodes by BNode ID
     */
    private Map<String,KiWiAnonResource> bnodeCache;

    /**
     * Cache literals by literal cache key (LiteralCommons#createCacheKey(String,Locale,URI))
     */
    private Map<String,KiWiLiteral> literalCache;


    /**
     * Look up namespaces by URI
     */
    private Map<String,KiWiNamespace> namespaceUriCache;

    /**
     * Look up namespaces by prefix
     */
    private Map<String,KiWiNamespace> namespacePrefixCache;

    /**
     * Cache instances of locales for language tags
     */
    private static Map<String,Locale> localeMap = new HashMap<String, Locale>();


    private Map<String,PreparedStatement> statementCache;

    private boolean autoCommit = false;

    private boolean batchCommit = true;

    private boolean closed = false;

    private int batchSize = 1000;

    private ReentrantLock commitLock;

    private ReentrantLock literalLock;
    private ReentrantLock uriLock;
    private ReentrantLock bnodeLock;


    // this set keeps track of all statements that have been deleted in the active transaction of this connection
    // this is needed to be able to determine if adding the triple again will merely undo a deletion or is a
    // completely new addition to the triple store
    private BloomFilter<Long> deletedStatementsLog;

    private static long numberOfCommits = 0;

    private long transactionId;

    private int QUERY_BATCH_SIZE = 1024;

    public KiWiConnection(KiWiPersistence persistence, KiWiDialect dialect, CacheManager cacheManager) throws SQLException {
        this.cacheManager = cacheManager;
        this.dialect      = dialect;
        this.persistence  = persistence;
        this.commitLock   = new ReentrantLock();
        this.literalLock   = new ReentrantLock();
        this.uriLock   = new ReentrantLock();
        this.bnodeLock   = new ReentrantLock();
        this.batchCommit  = dialect.isBatchSupported();
        this.deletedStatementsLog = BloomFilter.create(Funnels.longFunnel(), 100000);
        this.transactionId = getNextSequence("seq.tx");

        initCachePool();
        initStatementCache();
    }

    private void initCachePool() {
        nodeCache    = cacheManager.getNodeCache();
        tripleCache  = cacheManager.getTripleCache();
        uriCache     = cacheManager.getUriCache();
        bnodeCache   = cacheManager.getBNodeCache();
        literalCache = cacheManager.getLiteralCache();

        namespacePrefixCache = cacheManager.getNamespacePrefixCache();
        namespaceUriCache    = cacheManager.getNamespaceUriCache();
    }

    /**
     * Load all prepared statements of the dialect into the statement cache
     * @throws SQLException
     */
    private void initStatementCache() throws SQLException {
        statementCache = new HashMap<String, PreparedStatement>();

        /*
        for(String key : dialect.getStatementIdentifiers()) {
            statementCache.put(key,connection.prepareStatement(dialect.getStatement(key)));
        }
        */
    }

    /**
     * This method must be called by all methods as soon as they actually require a JDBC connection. This allows
     * more efficient implementations in case the queries can be answered directly from the cache.
     */
    protected void requireJDBCConnection() throws SQLException {
        if(connection == null) {
            connection = persistence.getJDBCConnection();
            connection.setAutoCommit(autoCommit);
        }
        if(tripleBatch == null) {
            tripleBatch = new TripleTable<KiWiTriple>();
        }
    }

    /**
     * Get direct access to the JDBC connection used by this KiWiConnection.
     *
     * @return
     */
    public Connection getJDBCConnection() throws SQLException {
        requireJDBCConnection();

        return connection;
    }

    /**
     * Return the cache manager used by this connection
     * @return
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public KiWiDialect getDialect() {
        return dialect;
    }

    public KiWiConfiguration getConfiguration() {
        return persistence.getConfiguration();
    }

    /**
     * Load a KiWiNamespace with the given prefix, or null if the namespace does not exist. The method will first
     * look in the node cache for cached nodes. If no cache entry is found, it will run a database query
     * ("load.namespace_prefix").
     *
     * @param prefix  the prefix to look for
     * @return the KiWiNamespace with this prefix or null if it does not exist
     * @throws SQLException
     */
    public KiWiNamespace loadNamespaceByPrefix(String prefix) throws SQLException {
        KiWiNamespace element = namespacePrefixCache.get(prefix);
        if(element != null) {
            return element;
        }

        requireJDBCConnection();

        // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
        PreparedStatement query = getPreparedStatement("load.namespace_prefix");
        query.setString(1, prefix);
        query.setMaxRows(1);

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return constructNamespaceFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Load a KiWiNamespace with the given uri, or null if the namespace does not exist. The method will first
     * look in the node cache for cached nodes. If no cache entry is found, it will run a database query
     * ("load.namespace_prefix").
     *
     * @param uri  the uri to look for
     * @return the KiWiNamespace with this uri or null if it does not exist
     * @throws SQLException
     */
    public KiWiNamespace loadNamespaceByUri(String uri) throws SQLException {
        KiWiNamespace element = namespaceUriCache.get(uri);
        if(element != null) {
            return element;
        }

        requireJDBCConnection();

        // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
        PreparedStatement query = getPreparedStatement("load.namespace_uri");
        query.setString(1, uri);
        query.setMaxRows(1);

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return constructNamespaceFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Store the namespace passed as argument in the database. The database might enfore unique constraints and
     * thus throw an exception in case the prefix or URI is already used.
     *
     * @param namespace the namespace to store
     * @throws SQLException the prefix or URI is already used, or a database error occurred
     */
    public void storeNamespace(KiWiNamespace namespace) throws SQLException {
        // TODO: add unique constraints to table
        if(namespace.getId() >= 0) {
            log.warn("trying to store namespace which is already persisted: {}",namespace);
            return;
        }

        requireJDBCConnection();

        namespace.setId(getNextSequence("seq.namespaces"));

        PreparedStatement insertNamespace = getPreparedStatement("store.namespace");
        insertNamespace.setLong(1,namespace.getId());
        insertNamespace.setString(2,namespace.getPrefix());
        insertNamespace.setString(3,namespace.getUri());
        insertNamespace.setTimestamp(4,new Timestamp(namespace.getCreated().getTime()));

        insertNamespace.executeUpdate();

        namespacePrefixCache.put(namespace.getPrefix(), namespace);
        namespaceUriCache.put(namespace.getUri(),namespace);
    }

    /**
     * Delete the namespace passed as argument from the database and from the caches.
     * @param namespace the namespace to delete
     * @throws SQLException in case a database error occurred
     */
    public void deleteNamespace(KiWiNamespace namespace) throws SQLException {
        if(namespace.getId() < 0) {
            log.warn("trying to remove namespace which is not persisted: {}",namespace);
            return;
        }

        requireJDBCConnection();

        PreparedStatement deleteNamespace = getPreparedStatement("delete.namespace");
        deleteNamespace.setLong(1, namespace.getId());
        deleteNamespace.executeUpdate();

        namespacePrefixCache.remove(namespace.getPrefix());
        namespaceUriCache.remove(namespace.getUri());
    }

    /**
     * Count all non-deleted triples in the triple store
     * @return
     * @throws SQLException
     */
    public long getSize() throws SQLException {
        requireJDBCConnection();

        PreparedStatement querySize = getPreparedStatement("query.size");
        ResultSet result = querySize.executeQuery();
        try {
            if(result.next()) {
                return result.getLong(1) + (tripleBatch != null ? tripleBatch.size() : 0);
            } else {
                return 0  + (tripleBatch != null ? tripleBatch.size() : 0);
            }
        } finally {
            result.close();
        }
    }

    /**
     * Count all non-deleted triples in the triple store
     * @return
     * @throws SQLException
     */
    public long getSize(KiWiResource context) throws SQLException {
        if(context.getId() < 0) {
            return 0;
        };

        requireJDBCConnection();

        PreparedStatement querySize = getPreparedStatement("query.size_ctx");
        querySize.setLong(1,context.getId());

        ResultSet result = querySize.executeQuery();
        try {
            if(result.next()) {
                return result.getLong(1) + (tripleBatch != null ? tripleBatch.listTriples(null,null,null,context, false).size() : 0);
            } else {
                return 0 + (tripleBatch != null ? tripleBatch.listTriples(null,null,null,context, false).size() : 0);
            }
        } finally {
            result.close();
        }
    }

    /**
     * Load a KiWiNode by database ID. The method will first look in the node cache for cached nodes. If
     * no cache entry is found, it will run a database query ('load.node_by_id') on the NODES table and
     * construct an appropriate subclass instance of KiWiNode with the obtained values. The result will be
     * constructed based on the value of the NTYPE column as follows:
     * <ul>
     *     <li>'uri' - KiWiUriResource using the id and svalue (as URI) columns</li>
     *     <li>'bnode' - KiWiAnonResource using the id and svalue (as AnonId) columns</li>
     *     <li>'string' - KiWiStringLiteral using the id, svalue (literal value), lang (literal
     *         language) and ltype (literal type) columns</li>
     *     <li>'int' - KiWiIntLiteral using the id, svalue (string value), ivalue (integer value)
     *         and ltype (literal type) columns</li>
     *     <li>'double' - KiWiDoubleLiteral using the id, svalue (string value), dvalue (double
     *         value) and ltype (literal type) columns</li>
     *     <li>'boolean' - KiWiBooleanLiteral using the id, svalue (string value), bvalue (boolean
     *         value) and ltype (literal type) columns</li>
     *     <li>'date' - KiWiDateLiteral using the id, svalue (string value), tvalue (time value)
     *         and ltype (literal type) columns</li>
     * </ul>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param id the database id of the node to load
     * @return an instance of a KiWiNode subclass representing the node with the given database id;
     *     type depends on value of the ntype column
     */
    public KiWiNode loadNodeById(long id) throws SQLException {

        // look in cache
        KiWiNode element = nodeCache.get(id);
        if(element != null) {
            return element;
        }

        requireJDBCConnection();

        // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
        PreparedStatement query = getPreparedStatement("load.node_by_id");
        synchronized (query) {
            query.setLong(1,id);
            query.setMaxRows(1);

            // run the database query and if it yields a result, construct a new node; the method call will take care of
            // caching the constructed node for future calls
            ResultSet result = query.executeQuery();
            try {
                if(result.next()) {
                    return constructNodeFromDatabase(result);
                } else {
                    return null;
                }
            } finally {
                result.close();
            }
        }

    }

    /**
     * Batch load the nodes with the given ids. This method aims to offer performance improvements by reducing
     * database roundtrips.
     * @param ids array of ids to retrieve
     * @return array of nodes corresponding to these ids (in the same order)
     * @throws SQLException
     */
    public KiWiNode[] loadNodesByIds(long... ids) throws SQLException {
        requireJDBCConnection();

        KiWiNode[] result = new KiWiNode[ids.length];

        // first look in the cache for any ids that have already been loaded
        ArrayList<Long> toFetch = new ArrayList<>(ids.length);
        for(int i=0; i < ids.length; i++) {
            if(ids[i] != 0) {
                result[i] = nodeCache.get(ids[i]);
                if(result[i] == null) {
                    toFetch.add(ids[i]);
                }
            }
        }

        if(toFetch.size() > 0) {
            // declare variables before to optimize stack allocation
            int position = 0;
            int nextBatchSize;
            PreparedStatement query;
            KiWiNode node;

            while(position < toFetch.size()) {
                nextBatchSize = computeBatchSize(position, toFetch.size());

                query = getPreparedStatement("load.nodes_by_ids", nextBatchSize);
                synchronized (query) {

                    for(int i=0; i<nextBatchSize; i++) {
                        query.setLong(i+1, toFetch.get(position + i));
                    }
                    query.setMaxRows(nextBatchSize);

                    // run the database query and if it yields a result, construct a new node; the method call will take care of
                    // caching the constructed node for future calls
                    ResultSet rows = query.executeQuery();
                    try {
                        while(rows.next()) {
                            node = constructNodeFromDatabase(rows);
                            for(int i=0; i<ids.length; i++) {
                                if(ids[i] == node.getId()) {
                                    result[i] = node;
                                }
                            }
                        }
                    } finally {
                        rows.close();
                    }

                    position += nextBatchSize;
                }
            }


        }
        return result;
    }

    private int computeBatchSize(int position, int length) {
        int batchSize = QUERY_BATCH_SIZE;
        while(length - position < batchSize) {
            batchSize = batchSize >> 1;
        }
        return batchSize;
    }

    public KiWiTriple loadTripleById(long id) throws SQLException {

        // look in cache
        KiWiTriple element = tripleCache.get(id);
        if(element != null) {
            return element;
        }

        requireJDBCConnection();

        // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
        PreparedStatement query = getPreparedStatement("load.triple_by_id");
        query.setLong(1,id);
        query.setMaxRows(1);

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return constructTripleFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }

    }

    /**
     * Load a KiWiUriResource by URI. The method will first look in the node cache for cached nodes. If
     * no cache entry is found, it will run a database query ('load.uri_by_uri') on the NODES table and
     * construct a new KiWiUriResource using the values of the id and svalue columns.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param uri the URI of the resource to load
     * @return the KiWiUriResource identified by the given URI  or null if it does not exist
     */
    public KiWiUriResource loadUriResource(String uri) throws SQLException {
        Preconditions.checkNotNull(uri);

        // look in cache
        KiWiUriResource element = uriCache.get(uri);
        if(element != null) {
            return element;
        }

        requireJDBCConnection();

        uriLock.lock();
        try {
            // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
            PreparedStatement query = getPreparedStatement("load.uri_by_uri");
            query.setString(1, uri);
            query.setMaxRows(1);

            // run the database query and if it yields a result, construct a new node; the method call will take care of
            // caching the constructed node for future calls
            ResultSet result = query.executeQuery();
            try {
                if(result.next()) {
                    return (KiWiUriResource)constructNodeFromDatabase(result);
                } else {
                    return null;
                }
            } finally {
                result.close();
            }
        } finally {
            uriLock.unlock();
        }
    }


    /**
     * Load a KiWiAnonResource by anonymous ID. The method will first look in the node cache for
     * cached nodes. If no cache entry is found, it will run a database query ('load.bnode_by_anonid')
     * on the NODES table and construct a new KiWiAnonResource using the values of the id and
     * svalue columns.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param id the anonymous ID of the resource to load
     * @return the KiWiAnonResource identified by the given internal ID or null if it does not exist
     */
    public KiWiAnonResource loadAnonResource(String id) throws SQLException {
        // look in cache
        KiWiAnonResource element = bnodeCache.get(id);
        if(element != null) {
            return element;
        }

        requireJDBCConnection();


        bnodeLock.lock();

        try {
            // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
            PreparedStatement query = getPreparedStatement("load.bnode_by_anonid");
            query.setString(1,id);
            query.setMaxRows(1);

            // run the database query and if it yields a result, construct a new node; the method call will take care of
            // caching the constructed node for future calls
            ResultSet result = query.executeQuery();
            try {
                if(result.next()) {
                    return (KiWiAnonResource)constructNodeFromDatabase(result);
                } else {
                    return null;
                }
            } finally {
                result.close();
            }
        } finally {
            bnodeLock.unlock();
        }
    }

    /**
     * Load a literal based on the value, language and type passed as argument. The method will first look in the node cache for
     * cached nodes. If no cache entry is found, it will run a database query ("load.literal_by_v")
     * on the NODES table and construct a new KiWiLiteral using the values of the literal columns (svalue, ivalue, ...). The
     * type of literal returned depends on the value of the ntype column.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param value string value of the literal to load
     * @param lang  language of the literal to load (optional, 2-letter language code with optional country)
     * @param ltype the type of the literal to load (optional)
     * @return the literal matching the given arguments or null if it does not exist
     * @throws SQLException
     */
    public KiWiLiteral loadLiteral(String value, String lang, KiWiUriResource ltype) throws SQLException {
        // look in cache
        final KiWiLiteral element = literalCache.get(LiteralCommons.createCacheKey(value,getLocale(lang), ltype));
        if(element != null) {
            return element;
        }

        requireJDBCConnection();

        // ltype not persisted
        if(ltype != null && ltype.getId() < 0) {
            return null;
        }

        literalLock.lock();

        try {
            // otherwise prepare a query, depending on the parameters given
            final PreparedStatement query;
            if(lang == null && ltype == null) {
                query = getPreparedStatement("load.literal_by_v");
                query.setString(1,value);
            } else if(lang != null) {
                query = getPreparedStatement("load.literal_by_vl");
                query.setString(1,value);
                query.setString(2, lang);
            } else if(ltype != null) {
                query = getPreparedStatement("load.literal_by_vt");
                query.setString(1,value);
                query.setLong(2,ltype.getId());
            } else {
                // This cannot happen...
                throw new IllegalArgumentException("Impossible combination of lang/type in loadLiteral!");
            }

            // run the database query and if it yields a result, construct a new node; the method call will take care of
            // caching the constructed node for future calls
            ResultSet result = query.executeQuery();
            try {
                if(result.next()) {
                    return (KiWiLiteral)constructNodeFromDatabase(result);
                } else {
                    return null;
                }
            } finally {
                result.close();
            }
        } finally {
            literalLock.unlock();
        }
    }

    /**
     * Load a literal with the date value given as argument if it exists. The method will first look in
     * the node cache for cached nodes. If no cache entry is found, it will run a database query ("load.literal_by_tv")
     * on the NODES table and construct a new KiWiLiteral using the values of the literal columns
     * (svalue, ivalue, ...). The type of literal returned depends on the value of the ntype column.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param date the date of the date literal to load
     * @return a KiWiDateLiteral with the correct date, or null if it does not exist
     * @throws SQLException
     */
    public KiWiDateLiteral loadLiteral(Date date) throws SQLException {
        // look in cache
        KiWiLiteral element = literalCache.get(LiteralCommons.createCacheKey(DateUtils.getDateWithoutFraction(date),Namespaces.NS_XSD + "dateTime"));
        if(element != null) {
            return (KiWiDateLiteral)element;
        }

        requireJDBCConnection();

        KiWiUriResource ltype = loadUriResource(Namespaces.NS_XSD + "dateTime");

        if(ltype == null || ltype.getId() < 0) {
            return null;
        }

        literalLock.lock();
        try {

            // otherwise prepare a query, depending on the parameters given
            PreparedStatement query = getPreparedStatement("load.literal_by_tv");
            query.setTimestamp(1, new Timestamp(DateUtils.getDateWithoutFraction(date).getTime()));
            query.setLong(2,ltype.getId());

            // run the database query and if it yields a result, construct a new node; the method call will take care of
            // caching the constructed node for future calls
            ResultSet result = query.executeQuery();
            try {
                if(result.next()) {
                    return (KiWiDateLiteral)constructNodeFromDatabase(result);
                } else {
                    return null;
                }
            } finally {
                result.close();
            }
        } finally {
            literalLock.unlock();
        }
    }


    /**
     * Load a integer literal with the long value given as argument if it exists. The method will first look in
     * the node cache for cached nodes. If no cache entry is found, it will run a database query ("load.literal_by_iv")
     * on the NODES table and construct a new KiWiLiteral using the values of the literal columns
     * (svalue, ivalue, ...). The type of literal returned depends on the value of the ntype column.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param value the value of the integer literal to load
     * @return a KiWiIntLiteral with the correct value, or null if it does not exist
     * @throws SQLException
     */
    public KiWiIntLiteral loadLiteral(long value) throws SQLException {
        // look in cache
        KiWiLiteral element = literalCache.get(LiteralCommons.createCacheKey(Long.toString(value),null,Namespaces.NS_XSD + "integer"));
        if(element != null) {
            return (KiWiIntLiteral)element;
        }

        requireJDBCConnection();

        KiWiUriResource ltype = loadUriResource(Namespaces.NS_XSD + "integer");

        // ltype not persisted
        if(ltype == null || ltype.getId() < 0) {
            return null;
        }

        literalLock.lock();

        try {

            // otherwise prepare a query, depending on the parameters given
            PreparedStatement query = getPreparedStatement("load.literal_by_iv");
            query.setLong(1,value);
            query.setLong(2,ltype.getId());

            // run the database query and if it yields a result, construct a new node; the method call will take care of
            // caching the constructed node for future calls
            ResultSet result = query.executeQuery();
            try {
                if(result.next()) {
                    return (KiWiIntLiteral)constructNodeFromDatabase(result);
                } else {
                    return null;
                }
            } finally {
                result.close();
            }
        } finally {
            literalLock.unlock();
        }
    }

    /**
     * Load a double literal with the double value given as argument if it exists. The method will first look in
     * the node cache for cached nodes. If no cache entry is found, it will run a database query ("load.literal_by_dv")
     * on the NODES table and construct a new KiWiLiteral using the values of the literal columns
     * (svalue, ivalue, ...). The type of literal returned depends on the value of the ntype column.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param value the value of the integer literal to load
     * @return a KiWiDoubleLiteral with the correct value, or null if it does not exist
     * @throws SQLException
     */
    public KiWiDoubleLiteral loadLiteral(double value) throws SQLException {
        // look in cache
        KiWiLiteral element = literalCache.get(LiteralCommons.createCacheKey(Double.toString(value),null,Namespaces.NS_XSD + "double"));
        if(element != null) {
            return (KiWiDoubleLiteral)element;
        }

        requireJDBCConnection();

        KiWiUriResource ltype = loadUriResource(Namespaces.NS_XSD + "double");

        // ltype not persisted
        if(ltype == null || ltype.getId() < 0) {
            return null;
        }

        literalLock.lock();

        try {
            // otherwise prepare a query, depending on the parameters given
            PreparedStatement query = getPreparedStatement("load.literal_by_dv");
            query.setDouble(1, value);
            query.setLong(2,ltype.getId());

            // run the database query and if it yields a result, construct a new node; the method call will take care of
            // caching the constructed node for future calls
            ResultSet result = query.executeQuery();
            try {
                if(result.next()) {
                    return (KiWiDoubleLiteral)constructNodeFromDatabase(result);
                } else {
                    return null;
                }
            } finally {
                result.close();
            }
        } finally {
            literalLock.unlock();
        }
    }

    /**
     * Load a boolean literal with the boolean value given as argument if it exists. The method will first look in
     * the node cache for cached nodes. If no cache entry is found, it will run a database query ("load.literal_by_bv")
     * on the NODES table and construct a new KiWiLiteral using the values of the literal columns
     * (svalue, ivalue, ...). The type of literal returned depends on the value of the ntype column.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param value the value of the integer literal to load
     * @return a KiWiBooleanLiteral with the correct value, or null if it does not exist
     * @throws SQLException
     */
    public KiWiBooleanLiteral loadLiteral(boolean value) throws SQLException {
        // look in cache
        KiWiLiteral element = literalCache.get(LiteralCommons.createCacheKey(Boolean.toString(value),null,Namespaces.NS_XSD + "boolean"));
        if(element != null) {
            return (KiWiBooleanLiteral)element;
        }


        requireJDBCConnection();

        KiWiUriResource ltype = loadUriResource(Namespaces.NS_XSD + "boolean");

        // ltype not persisted
        if(ltype == null || ltype.getId() < 0) {
            return null;
        }

        literalLock.lock();

        try {

            // otherwise prepare a query, depending on the parameters given
            PreparedStatement query = getPreparedStatement("load.literal_by_bv");
            query.setBoolean(1, value);
            query.setLong(2,ltype.getId());

            // run the database query and if it yields a result, construct a new node; the method call will take care of
            // caching the constructed node for future calls
            ResultSet result = query.executeQuery();
            try {
                if(result.next()) {
                    return (KiWiBooleanLiteral)constructNodeFromDatabase(result);
                } else {
                    return null;
                }
            } finally {
                result.close();
            }
        } finally {
            literalLock.unlock();
        }
    }

    /**
     * Store a new node in the database. The method will retrieve a new database id for the node and update the
     * passed object. Afterwards, the node data will be inserted into the database using appropriate INSERT
     * statements. The caller must make sure the connection is committed and closed properly.
     * <p/>
     * If the node already has an ID, the method will do nothing (assuming that it is already persistent)
     *
     *
     * @param node
     * @throws SQLException
     */
    public synchronized void storeNode(KiWiNode node) throws SQLException {

        // ensure the data type of a literal is persisted first
        if(node instanceof KiWiLiteral) {
            KiWiLiteral literal = (KiWiLiteral)node;
            if(literal.getType() != null && literal.getType().getId() < 0) {
                storeNode(literal.getType());
            }
        }

        requireJDBCConnection();

        // retrieve a new node id and set it in the node object
        if(node.getId() < 0) {
            node.setId(getNextSequence("seq.nodes"));
        }

        // distinguish the different node types and run the appropriate updates
        if(node instanceof KiWiUriResource) {
            KiWiUriResource uriResource = (KiWiUriResource)node;

            PreparedStatement insertNode = getPreparedStatement("store.uri");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2,uriResource.stringValue());
            insertNode.setTimestamp(3, new Timestamp(uriResource.getCreated().getTime()));

            insertNode.executeUpdate();

        } else if(node instanceof KiWiAnonResource) {
            KiWiAnonResource anonResource = (KiWiAnonResource)node;

            PreparedStatement insertNode = getPreparedStatement("store.bnode");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2,anonResource.stringValue());
            insertNode.setTimestamp(3, new Timestamp(anonResource.getCreated().getTime()));

            insertNode.executeUpdate();
        } else if(node instanceof KiWiDateLiteral) {
            KiWiDateLiteral dateLiteral = (KiWiDateLiteral)node;

            PreparedStatement insertNode = getPreparedStatement("store.tliteral");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2, dateLiteral.stringValue());
            insertNode.setTimestamp(3, new Timestamp(dateLiteral.getDateContent().getTime()));
            if(dateLiteral.getType() != null)
                insertNode.setLong(4,dateLiteral.getType().getId());
            else
                throw new IllegalStateException("a date literal must have a datatype");
            insertNode.setTimestamp(5, new Timestamp(dateLiteral.getCreated().getTime()));

            insertNode.executeUpdate();
        } else if(node instanceof KiWiIntLiteral) {
            KiWiIntLiteral intLiteral = (KiWiIntLiteral)node;

            PreparedStatement insertNode = getPreparedStatement("store.iliteral");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2, intLiteral.getContent());
            insertNode.setDouble(3, intLiteral.getDoubleContent());
            insertNode.setLong(4, intLiteral.getIntContent());
            if(intLiteral.getType() != null)
                insertNode.setLong(5,intLiteral.getType().getId());
            else
                throw new IllegalStateException("an integer literal must have a datatype");
            insertNode.setTimestamp(6, new Timestamp(intLiteral.getCreated().getTime()));

            insertNode.executeUpdate();
        } else if(node instanceof KiWiDoubleLiteral) {
            KiWiDoubleLiteral doubleLiteral = (KiWiDoubleLiteral)node;

            PreparedStatement insertNode = getPreparedStatement("store.dliteral");
            insertNode.setLong(1, node.getId());
            insertNode.setString(2, doubleLiteral.getContent());
            insertNode.setDouble(3, doubleLiteral.getDoubleContent());
            if(doubleLiteral.getType() != null)
                insertNode.setLong(4,doubleLiteral.getType().getId());
            else
                throw new IllegalStateException("a double literal must have a datatype");
            insertNode.setTimestamp(5, new Timestamp(doubleLiteral.getCreated().getTime()));

            insertNode.executeUpdate();
        } else if(node instanceof KiWiBooleanLiteral) {
            KiWiBooleanLiteral booleanLiteral = (KiWiBooleanLiteral)node;

            PreparedStatement insertNode = getPreparedStatement("store.bliteral");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2, booleanLiteral.getContent());
            insertNode.setBoolean(3, booleanLiteral.booleanValue());
            if(booleanLiteral.getType() != null)
                insertNode.setLong(4,booleanLiteral.getType().getId());
            else
                throw new IllegalStateException("a boolean literal must have a datatype");
            insertNode.setTimestamp(5, new Timestamp(booleanLiteral.getCreated().getTime()));

            insertNode.executeUpdate();
        } else if(node instanceof KiWiStringLiteral) {
            KiWiStringLiteral stringLiteral = (KiWiStringLiteral)node;


            Double dbl_value = null;
            Long   lng_value = null;
            if(stringLiteral.getContent().length() < 64 && NumberUtils.isNumber(stringLiteral.getContent()))
                try {
                    dbl_value = Double.parseDouble(stringLiteral.getContent());
                    lng_value = Long.parseLong(stringLiteral.getContent());
                } catch (NumberFormatException ex) {
                    // ignore, keep NaN
                }


            PreparedStatement insertNode = getPreparedStatement("store.sliteral");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2, stringLiteral.getContent());
            if(dbl_value != null) {
                insertNode.setDouble(3, dbl_value);
            } else {
                insertNode.setObject(3, null);
            }
            if(lng_value != null) {
                insertNode.setLong(4, lng_value);
            } else {
                insertNode.setObject(4, null);
            }

            if(stringLiteral.getLocale() != null) {
                insertNode.setString(5, stringLiteral.getLocale().getLanguage());
            } else {
                insertNode.setObject(5, null);
            }
            if(stringLiteral.getType() != null) {
                insertNode.setLong(6,stringLiteral.getType().getId());
            } else {
                insertNode.setObject(6, null);
            }
            insertNode.setTimestamp(7, new Timestamp(stringLiteral.getCreated().getTime()));

            insertNode.executeUpdate();
        } else {
            log.warn("unrecognized node type: {}", node.getClass().getCanonicalName());
        }

        cacheNode(node);
    }

    /**
     * Store a triple in the database. This method assumes that all nodes used by the triple are already persisted.
     *
     * @param triple     the triple to store
     * @throws SQLException
     * @throws NullPointerException in case the subject, predicate, object or context have not been persisted
     * @return true in case the update added a new triple to the database, false in case the triple already existed
     */
    public synchronized void storeTriple(final KiWiTriple triple) throws SQLException {
        // mutual exclusion: prevent parallel adding and removing of the same triple
        synchronized (triple) {

            requireJDBCConnection();

            if(triple.getId() < 0) {
                triple.setId(getNextSequence("seq.triples"));
            }

            if(deletedStatementsLog.mightContain(triple.getId())) {
                // this is a hack for a concurrency problem that may occur in case the triple is removed in the
                // transaction and then added again; in these cases the createStatement method might return
                // an expired state of the triple because it uses its own database connection

                //deletedStatementsLog.remove(triple.getId());
                undeleteTriple(triple);

            } else {

                if(batchCommit) {
                    commitLock.lock();
                    try {
                        cacheTriple(triple);
                        tripleBatch.add(triple);
                        if(tripleBatch.size() >= batchSize) {
                            flushBatch();
                        }
                    } finally {
                        commitLock.unlock();
                    }
                }  else {
                    Preconditions.checkNotNull(triple.getSubject().getId());
                    Preconditions.checkNotNull(triple.getPredicate().getId());
                    Preconditions.checkNotNull(triple.getObject().getId());


                    try {
                        RetryExecution<Boolean> execution = new RetryExecution<>("STORE");
                        execution.setUseSavepoint(true);
                        execution.execute(connection, new RetryCommand<Boolean>() {
                            @Override
                            public Boolean run() throws SQLException {
                                PreparedStatement insertTriple = getPreparedStatement("store.triple");
                                insertTriple.setLong(1,triple.getId());
                                insertTriple.setLong(2,triple.getSubject().getId());
                                insertTriple.setLong(3,triple.getPredicate().getId());
                                insertTriple.setLong(4,triple.getObject().getId());
                                if(triple.getContext() != null) {
                                    insertTriple.setLong(5,triple.getContext().getId());
                                } else {
                                    insertTriple.setNull(5, Types.BIGINT);
                                }
                                insertTriple.setBoolean(6,triple.isInferred());
                                insertTriple.setTimestamp(7, new Timestamp(triple.getCreated().getTime()));
                                int count = insertTriple.executeUpdate();

                                cacheTriple(triple);

                                return count > 0;
                            }
                        });

                    } catch(SQLException ex) {
                        if("HYT00".equals(ex.getSQLState())) { // H2 table locking timeout
                            throw new ConcurrentModificationException("the same triple was modified in concurrent transactions (triple="+triple+")");
                        } else {
                            throw ex;
                        }
                    }
                }
            }
        }
    }


    /**
     * Return the identifier of the triple with the given subject, predicate, object and context, or null if this
     * triple does not exist. Used for quick existance checks of triples.
     *
     * @param subject
     * @param predicate
     * @param object
     * @param context
     * @param inferred
     * @return
     */
    public synchronized long getTripleId(final KiWiResource subject, final KiWiUriResource predicate, final KiWiNode object, final KiWiResource context, final boolean inferred) throws SQLException {
        if(tripleBatch != null && tripleBatch.size() > 0) {
            Collection<KiWiTriple> batched = tripleBatch.listTriples(subject,predicate,object,context, false);
            if(batched.size() > 0) {
                return batched.iterator().next().getId();
            }
        }

        requireJDBCConnection();
        PreparedStatement loadTripleId = getPreparedStatement("load.triple");
        loadTripleId.setLong(1, subject.getId());
        loadTripleId.setLong(2, predicate.getId());
        loadTripleId.setLong(3, object.getId());
        if(context != null) {
            loadTripleId.setLong(4, context.getId());
        } else {
            loadTripleId.setNull(4, Types.BIGINT);
        }

        ResultSet result = loadTripleId.executeQuery();
        try {
            if(result.next()) {
                return result.getLong(1);
            } else {
                return -1L;
            }

        } finally {
            result.close();
        }
    }

    /**
     * Mark the triple passed as argument as deleted, setting the "deleted" flag to true and
     * updating the timestamp value of "deletedAt".
     * <p/>
     * The triple remains in the database, because other entities might still reference it (e.g. a version).
     * Use the method cleanupTriples() to fully remove all deleted triples without references.
     *
     * @param triple
     */
    public void deleteTriple(final KiWiTriple triple) throws SQLException {
        requireJDBCConnection();

        RetryExecution<Void> execution = new RetryExecution<>("DELETE");
        execution.setUseSavepoint(true);
        execution.execute(connection, new RetryCommand<Void>() {
            @Override
            public Void run() throws SQLException {
                // mutual exclusion: prevent parallel adding and removing of the same triple
                synchronized (triple) {

                    // make sure the triple is marked as deleted in case some service still holds a reference
                    triple.setDeleted(true);
                    triple.setDeletedAt(new Date());

                    if (triple.getId() < 0) {
                        log.warn("attempting to remove non-persistent triple: {}", triple);
                    } else {
                        if (batchCommit) {
                            // need to remove from triple batch and from database
                            commitLock.lock();
                            try {
                                if (tripleBatch == null || !tripleBatch.remove(triple)) {

                                    PreparedStatement deleteTriple = getPreparedStatement("delete.triple");
                                    synchronized (deleteTriple) {
                                        deleteTriple.setLong(1, triple.getId());
                                        deleteTriple.executeUpdate();
                                    }
                                    deletedStatementsLog.put(triple.getId());
                                }
                            } finally {
                                commitLock.unlock();
                            }
                        } else {
                            requireJDBCConnection();

                            PreparedStatement deleteTriple = getPreparedStatement("delete.triple");
                            synchronized (deleteTriple) {
                                deleteTriple.setLong(1, triple.getId());
                                deleteTriple.executeUpdate();
                            }
                            deletedStatementsLog.put(triple.getId());


                        }
                    }
                    removeCachedTriple(triple);
                }

                return null;
            }
        });


    }

    /**
     * Mark the triple passed as argument as not deleted, setting the "deleted" flag to false and
     * clearing the timestamp value of "deletedAt".
     * <p/>
     * Note that this operation should only be called if the triple was deleted before in the same
     * transaction!
     *
     * @param triple
     */
    public void undeleteTriple(KiWiTriple triple) throws SQLException {
        if(triple.getId() < 0) {
            log.warn("attempting to undelete non-persistent triple: {}",triple);
            return;
        }

        requireJDBCConnection();

        // make sure the triple is not marked as deleted in case some service still holds a reference
        triple.setDeleted(false);
        triple.setDeletedAt(null);


        synchronized (triple) {
            if(!triple.isDeleted()) {
                log.warn("attemting to undelete triple that was not deleted: {}",triple);
            }

            PreparedStatement undeleteTriple = getPreparedStatement("undelete.triple");
            undeleteTriple.setLong(1, triple.getId());
            undeleteTriple.executeUpdate();

            if(!persistence.getConfiguration().isClustered()) {
                cacheTriple(triple);
            }
        }

    }


    /**
     * List all contexts used in this triple store. See query.contexts .
     * @return
     * @throws SQLException
     */
    public CloseableIteration<KiWiResource, SQLException> listContexts() throws SQLException {
        requireJDBCConnection();

        PreparedStatement queryContexts = getPreparedStatement("query.contexts");

        final ResultSet result = queryContexts.executeQuery();

        if(tripleBatch != null && tripleBatch.size() > 0) {
            return new DistinctIteration<KiWiResource, SQLException>(
                    new UnionIteration<KiWiResource, SQLException>(
                            new ConvertingIteration<Resource,KiWiResource,SQLException>(new IteratorIteration<Resource, SQLException>(tripleBatch.listContextIDs().iterator())) {
                                @Override
                                protected KiWiResource convert(Resource sourceObject) throws SQLException {
                                    return (KiWiResource)sourceObject;
                                }
                            },
                            new ResultSetIteration<KiWiResource>(result, new ResultTransformerFunction<KiWiResource>() {
                                @Override
                                public KiWiResource apply(ResultSet row) throws SQLException {
                                    return (KiWiResource)loadNodeById(result.getLong("context"));
                                }
                            })
                    )
            );


        } else {
            return new ResultSetIteration<KiWiResource>(result, new ResultTransformerFunction<KiWiResource>() {
                @Override
                public KiWiResource apply(ResultSet row) throws SQLException {
                    return (KiWiResource)loadNodeById(result.getLong("context"));
                }
            });
        }

    }

    /**
     * List all contexts used in this triple store. See query.contexts .
     * @return
     * @throws SQLException
     */
    public CloseableIteration<KiWiResource, SQLException> listResources() throws SQLException {
        requireJDBCConnection();

        PreparedStatement queryContexts = getPreparedStatement("query.resources");

        final ResultSet result = queryContexts.executeQuery();

        return new ResultSetIteration<KiWiResource>(result, new ResultTransformerFunction<KiWiResource>() {
            @Override
            public KiWiResource apply(ResultSet row) throws SQLException {
                return (KiWiResource)constructNodeFromDatabase(row);
            }
        });

    }

    /**
     * List all contexts used in this triple store. See query.contexts .
     * @return
     * @throws SQLException
     */
    public CloseableIteration<KiWiUriResource, SQLException> listResources(String prefix) throws SQLException {
        requireJDBCConnection();

        PreparedStatement queryContexts = getPreparedStatement("query.resources_prefix");
        queryContexts.setString(1, prefix + "%");

        final ResultSet result = queryContexts.executeQuery();

        return new ResultSetIteration<KiWiUriResource>(result, new ResultTransformerFunction<KiWiUriResource>() {
            @Override
            public KiWiUriResource apply(ResultSet row) throws SQLException {
                return (KiWiUriResource)constructNodeFromDatabase(row);
            }
        });

    }


    public CloseableIteration<KiWiNamespace, SQLException> listNamespaces() throws SQLException {
        requireJDBCConnection();

        PreparedStatement queryContexts = getPreparedStatement("query.namespaces");

        final ResultSet result = queryContexts.executeQuery();

        return new ResultSetIteration<KiWiNamespace>(result, new ResultTransformerFunction<KiWiNamespace>() {
            @Override
            public KiWiNamespace apply(ResultSet input) throws SQLException {
                return constructNamespaceFromDatabase(result);
            }
        });
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
     *
     *
     * @param subject    the subject to query for, or null for a wildcard query
     * @param predicate  the predicate to query for, or null for a wildcard query
     * @param object     the object to query for, or null for a wildcard query
     * @param context    the context to query for, or null for a wildcard query
     * @param inferred   if true, the result will also contain triples inferred by the reasoner, if false not
     * @param wildcardContext if true, a null context will be interpreted as a wildcard, if false, a null context will be interpreted as "no context"
     * @return a new RepositoryResult with a direct connection to the database; the result should be properly closed
     *         by the caller
     */
    public RepositoryResult<Statement> listTriples(final KiWiResource subject, final KiWiUriResource predicate, final KiWiNode object, final KiWiResource context, final boolean inferred, final boolean wildcardContext) throws SQLException {


        if(tripleBatch != null && tripleBatch.size() > 0) {
            synchronized (tripleBatch) {
                return new RepositoryResult<Statement>(
                        new ExceptionConvertingIteration<Statement, RepositoryException>(
                                new UnionIteration<Statement, SQLException>(
                                        new IteratorIteration<Statement, SQLException>(tripleBatch.listTriples(subject,predicate,object,context, wildcardContext).iterator()),
                                        new DelayedIteration<Statement, SQLException>() {
                                            @Override
                                            protected Iteration<? extends Statement, ? extends SQLException> createIteration() throws SQLException {
                                                return listTriplesInternal(subject,predicate,object,context,inferred, wildcardContext);
                                            }
                                        }

                                )
                        ) {
                            @Override
                            protected RepositoryException convert(Exception e) {
                                return new RepositoryException("database error while iterating over result set",e);
                            }
                        }

                );
            }
        }  else {
            return new RepositoryResult<Statement>(
                    new ExceptionConvertingIteration<Statement, RepositoryException>(listTriplesInternal(subject,predicate,object,context,inferred, wildcardContext)) {
                        @Override
                        protected RepositoryException convert(Exception e) {
                            return new RepositoryException("database error while iterating over result set",e);
                        }
                    }

            );
        }
    }

    /**
     * Internal implementation for actually carrying out the query. Returns a closable iteration that can be used
     * in a repository result. The iteration is forward-only and does not allow removing result rows.
     *
     * @param subject    the subject to query for, or null for a wildcard query
     * @param predicate  the predicate to query for, or null for a wildcard query
     * @param object     the object to query for, or null for a wildcard query
     * @param context    the context to query for, or null for a wildcard query
     * @param inferred   if true, the result will also contain triples inferred by the reasoner, if false not
     * @param wildcardContext if true, a null context will be interpreted as a wildcard, if false, a null context will be interpreted as "no context"
     * @return a ClosableIteration that wraps the database ResultSet; needs to be closed explicitly by the caller
     * @throws SQLException
     */
    private CloseableIteration<Statement, SQLException> listTriplesInternal(KiWiResource subject, KiWiUriResource predicate, KiWiNode object, KiWiResource context, boolean inferred, final boolean wildcardContext) throws SQLException {
        // if one of the database ids is null, there will not be any database results, so we can return an empty result
        if(subject != null && subject.getId() < 0) {
            return new EmptyIteration<Statement, SQLException>();
        }
        if(predicate != null && predicate.getId() < 0) {
            return new EmptyIteration<Statement, SQLException>();
        }
        if(object != null && object.getId() < 0) {
            return new EmptyIteration<Statement, SQLException>();
        }
        if(context != null && context.getId() < 0) {
            return new EmptyIteration<Statement, SQLException>();
        }

        requireJDBCConnection();

        // otherwise we need to create an appropriate SQL query and execute it, the repository result will be read-only
        // and only allow forward iteration, so we can limit the query using the respective flags
        PreparedStatement query = connection.prepareStatement(
                constructTripleQuery(subject,predicate,object,context,inferred, wildcardContext),
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY
        );
        query.clearParameters();

        if(persistence.getDialect().isCursorSupported()) {
            query.setFetchSize(persistence.getConfiguration().getCursorSize());
        }

        // set query parameters
        int position = 1;
        if(subject != null) {
            query.setLong(position++, subject.getId());
        }
        if(predicate != null) {
            query.setLong(position++, predicate.getId());
        }
        if(object != null) {
            query.setLong(position++, object.getId());
        }
        if(context != null) {
            query.setLong(position++, context.getId());
        }

        final ResultSet result = query.executeQuery();


        return new CloseableIteration<Statement, SQLException>() {

            List<KiWiTriple> batch = null;
            int batchPosition = 0;

            @Override
            public void close() throws SQLException {
                result.close();
            }

            @Override
            public boolean hasNext() throws SQLException {
                fetchBatch();

                return batch.size() > batchPosition;
            }

            @Override
            public Statement next() throws SQLException {
                fetchBatch();

                if(batch.size() > batchPosition) {
                    return batch.get(batchPosition++);
                }  else {
                    return null;
                }
            }

            private void fetchBatch() throws SQLException {
                if(batch == null || batch.size() <= batchPosition) {
                    batch = constructTriplesFromDatabase(result, QUERY_BATCH_SIZE);
                    batchPosition = 0;
                }
            }

            @Override
            public void remove() throws SQLException {
                throw new UnsupportedOperationException("removing results not supported");
            }
        };
    }

    /**
     * Construct the SQL query string from the query pattern passed as arguments
     *
     * @param subject    the subject to query for, or null for a wildcard query
     * @param predicate  the predicate to query for, or null for a wildcard query
     * @param object     the object to query for, or null for a wildcard query
     * @param context    the context to query for, or null for a wildcard query
     * @param inferred   if true, the result will also contain triples inferred by the reasoner, if false not
     * @return an SQL query string representing the triple pattern
     */
    protected String constructTripleQuery(KiWiResource subject, KiWiUriResource predicate, KiWiNode object, KiWiResource context, boolean inferred, boolean wildcardContext) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT id,subject,predicate,object,context,deleted,inferred,creator,createdAt,deletedAt FROM triples WHERE deleted = false");
        if(subject != null) {
            builder.append(" AND subject = ?");
        }
        if(predicate != null) {
            builder.append(" AND predicate = ?");
        }
        if(object != null) {
            builder.append(" AND object = ?");
        }
        if(context != null) {
            builder.append(" AND context = ?");
        } else if(!wildcardContext) {
            builder.append(" AND context IS NULL");
        }
        if(!inferred) {
            builder.append(" AND inferred = false");
        }
        return builder.toString();

    }

    protected KiWiNamespace constructNamespaceFromDatabase(ResultSet row) throws SQLException {
        KiWiNamespace result = new KiWiNamespace(row.getString("prefix"),row.getString("uri"));
        result.setId(row.getLong("id"));
        result.setCreated(new Date(row.getTimestamp("createdAt").getTime()));

        namespacePrefixCache.put(result.getPrefix(),result);
        namespaceUriCache.put(result.getUri(),result);

        return result;
    }

    /**
     * Construct an appropriate KiWiNode from the result of an SQL query. The method will not change the
     * ResultSet iterator, only read its values, so it needs to be executed for each row separately.
     * @param row
     * @return
     */
    protected KiWiNode constructNodeFromDatabase(ResultSet row) throws SQLException {
        // column order; id,ntype,svalue,ivalue,dvalue,tvalue,bvalue,lang,ltype,createdAt
        //               1 ,2    ,3     ,4     ,5     ,6     ,7     ,8   ,9    ,10

        long id = row.getLong(1);

        KiWiNode cached = nodeCache.get(id);

        // lookup element in cache first, so we can avoid reconstructing it if it is already there
        if(cached != null) {
            return cached;
        }

        String ntype = row.getString(2);
        if("uri".equals(ntype)) {
            KiWiUriResource result = new KiWiUriResource(row.getString(3),new Date(row.getTimestamp(10).getTime()));
            result.setId(id);

            cacheNode(result);
            return result;
        } else if("bnode".equals(ntype)) {
            KiWiAnonResource result = new KiWiAnonResource(row.getString(3), new Date(row.getTimestamp(10).getTime()));
            result.setId(id);

            cacheNode(result);
            return result;
        } else if("string".equals(ntype)) {
            final KiWiStringLiteral result = new KiWiStringLiteral(row.getString(3), new Date(row.getTimestamp(10).getTime()));
            result.setId(id);

            if(row.getString(8) != null) {
                result.setLocale(getLocale(row.getString(8)));
            }
            if(row.getLong(9) != 0) {
                result.setType((KiWiUriResource) loadNodeById(row.getLong(9)));
            }

            cacheNode(result);
            return result;
        } else if("int".equals(ntype)) {
            KiWiIntLiteral result = new KiWiIntLiteral(row.getLong(4), null, new Date(row.getTimestamp(10).getTime()));
            result.setId(id);
            if(row.getLong(9) != 0) {
                result.setType((KiWiUriResource) loadNodeById(row.getLong(9)));
            }

            cacheNode(result);
            return result;
        } else if("double".equals(ntype)) {
            KiWiDoubleLiteral result = new KiWiDoubleLiteral(row.getDouble(5), null, new Date(row.getTimestamp(10).getTime()));
            result.setId(id);
            if(row.getLong(9) != 0) {
                result.setType((KiWiUriResource) loadNodeById(row.getLong(9)));
            }

            cacheNode(result);
            return result;
        } else if("boolean".equals(ntype)) {
            KiWiBooleanLiteral result = new KiWiBooleanLiteral(row.getBoolean(7),null,new Date(row.getTimestamp(10).getTime()));
            result.setId(id);

            if(row.getLong(9) != 0) {
                result.setType((KiWiUriResource) loadNodeById(row.getLong(9)));
            }

            cacheNode(result);
            return result;
        } else if("date".equals(ntype)) {
            KiWiDateLiteral result = new KiWiDateLiteral(new Date(row.getTimestamp(6).getTime()), null, new Date(row.getTimestamp(10).getTime()));
            result.setId(id);

            if(row.getLong(9) != 0) {
                result.setType((KiWiUriResource) loadNodeById(row.getLong(9)));
            }

            cacheNode(result);
            return result;
        } else {
            throw new IllegalArgumentException("unknown node type in database result: "+ntype);
        }
    }

    /**
     * Construct a KiWiTriple from the result of an SQL query. The query result is expected to contain the
     * following columns:
     * <ul>
     *     <li>id: the database id of the triple (long value)</li>
     *     <li>subject: the database id of the subject (long value); the node will be loaded using the loadNodeById method</li>
     *     <li>predicate: the database id of the predicate (long value); the node will be loaded using the loadNodeById method</li>
     *     <li>object: the database id of the object (long value); the node will be loaded using the loadNodeById method</li>
     *     <li>context: the database id of the context (long value); the node will be loaded using the loadNodeById method</li>
     *     <li>creator: the database id of the creator (long value); the node will be loaded using the loadNodeById method; may be null</li>
     *     <li>deleted: a flag (boolean) indicating whether this triple has been deleted</li>
     *     <li>inferred: a flag (boolean) indicating whether this triple has been inferred by the KiWi reasoner</li>
     *     <li>createdAt: a timestamp representing the creation date of the triple</li>
     *     <li>createdAt: a timestamp representing the deletion date of the triple (null in case triple is not deleted)</li>
     * </ul>
     * The method will not change the ResultSet iterator, only read its values, so it needs to be executed for each row separately.
     *
     * @param row a database result containing the columns described above
     * @return a KiWiTriple representation of the database result
     */
    protected KiWiTriple constructTripleFromDatabase(ResultSet row) throws SQLException {
        if(row.isClosed()) {
            throw new ResultInterruptedException("retrieving results has been interrupted");
        }

        // columns: id,subject,predicate,object,context,deleted,inferred,creator,createdAt,deletedAt
        //          1 ,2      ,3        ,4     ,5      ,6      ,7       ,8      ,9        ,10

        Long id = row.getLong(1);

        KiWiTriple cached = tripleCache.get(id);

        // lookup element in cache first, so we can avoid reconstructing it if it is already there
        if(cached != null) {
            return cached;
        }

        KiWiTriple result = new KiWiTriple();
        result.setId(id);

        KiWiNode[] batch = loadNodesByIds(row.getLong(2), row.getLong(3), row.getLong(4), row.getLong(5));
        result.setSubject((KiWiResource) batch[0]);
        result.setPredicate((KiWiUriResource) batch[1]);
        result.setObject(batch[2]);
        result.setContext((KiWiResource) batch[3]);

//        result.setSubject((KiWiResource)loadNodeById(row.getLong(2)));
//        result.setPredicate((KiWiUriResource) loadNodeById(row.getLong(3)));
//        result.setObject(loadNodeById(row.getLong(4)));
//        result.setContext((KiWiResource) loadNodeById(row.getLong(5)));
        if(row.getLong(8) != 0) {
            result.setCreator((KiWiResource)loadNodeById(row.getLong(8)));
        }
        result.setDeleted(row.getBoolean(6));
        result.setInferred(row.getBoolean(7));
        result.setCreated(new Date(row.getTimestamp(9).getTime()));
        try {
            if(row.getDate(10) != null) {
                result.setDeletedAt(new Date(row.getTimestamp(10).getTime()));
            }
        } catch (SQLException ex) {
            // work around a MySQL problem with null dates
            // (see http://stackoverflow.com/questions/782823/handling-datetime-values-0000-00-00-000000-in-jdbc)
        }

        cacheTriple(result);

        return result;
    }


    /**
     * Construct a batch of KiWiTriples from the result of an SQL query. This query differs from constructTripleFromDatabase
     * in that it does a batch-prefetching for optimized performance
     *
     * @param row a database result containing the columns described above
     * @return a KiWiTriple representation of the database result
     */
    protected List<KiWiTriple> constructTriplesFromDatabase(ResultSet row, int maxPrefetch) throws SQLException {
        int count = 0;

        // declare variables to optimize stack allocation
        KiWiTriple triple;
        long id;

        List<KiWiTriple> result = new ArrayList<>();
        Map<Long,Long[]> tripleIds  = new HashMap<>();
        Set<Long> nodeIds   = new HashSet<>();
        while(count < maxPrefetch && row.next()) {
            count++;

            if(row.isClosed()) {
                throw new ResultInterruptedException("retrieving results has been interrupted");
            }

            // columns: id,subject,predicate,object,context,deleted,inferred,creator,createdAt,deletedAt
            //          1 ,2      ,3        ,4     ,5      ,6      ,7       ,8      ,9        ,10

            id = row.getLong(1);

            triple = tripleCache.get(id);

            // lookup element in cache first, so we can avoid reconstructing it if it is already there
            if(triple != null) {
                result.add(triple);
            } else {

                triple = new KiWiTriple();
                triple.setId(id);

                // collect node ids for batch retrieval
                nodeIds.add(row.getLong(2));
                nodeIds.add(row.getLong(3));
                nodeIds.add(row.getLong(4));

                if(row.getLong(5) != 0) {
                    nodeIds.add(row.getLong(5));
                }

                if(row.getLong(8) != 0) {
                    nodeIds.add(row.getLong(8));
                }

                // remember which node ids where relevant for the triple
                tripleIds.put(id,new Long[] { row.getLong(2),row.getLong(3),row.getLong(4),row.getLong(5),row.getLong(8) });

                triple.setDeleted(row.getBoolean(6));
                triple.setInferred(row.getBoolean(7));
                triple.setCreated(new Date(row.getTimestamp(9).getTime()));
                try {
                    if(row.getDate(10) != null) {
                        triple.setDeletedAt(new Date(row.getTimestamp(10).getTime()));
                    }
                } catch (SQLException ex) {
                    // work around a MySQL problem with null dates
                    // (see http://stackoverflow.com/questions/782823/handling-datetime-values-0000-00-00-000000-in-jdbc)
                }

                result.add(triple);
            }
        }

        KiWiNode[] nodes = loadNodesByIds(Longs.toArray(nodeIds));
        Map<Long,KiWiNode> nodeMap = new HashMap<>();
        for(int i=0; i<nodes.length; i++) {
            nodeMap.put(nodes[i].getId(), nodes[i]);
        }

        for(KiWiTriple t : result) {
            if(tripleIds.containsKey(t.getId())) {
                // need to set subject, predicate, object, context and creator
                Long[] ids = tripleIds.get(t.getId());
                t.setSubject((KiWiResource) nodeMap.get(ids[0]));
                t.setPredicate((KiWiUriResource) nodeMap.get(ids[1]));
                t.setObject(nodeMap.get(ids[2]));

                if(ids[3] != 0) {
                    t.setContext((KiWiResource) nodeMap.get(ids[3]));
                }

                if(ids[4] != 0) {
                    t.setCreator((KiWiResource) nodeMap.get(ids[4]));
                }

            }

            cacheTriple(t);
        }



        return result;
    }


    protected static Locale getLocale(String language) {
        Locale locale = localeMap.get(language);
        if(locale == null && language != null && !language.isEmpty()) {
            try {
                Locale.Builder builder = new Locale.Builder();
                builder.setLanguageTag(language);
                locale = builder.build();
                localeMap.put(language, locale);
            } catch (IllformedLocaleException ex) {
                throw new IllegalArgumentException("Language was not a valid BCP47 language: " + language, ex);
            }
        }
        return locale;
    }

    /**
     * Return the prepared statement with the given identifier; first looks in the statement cache and if it does
     * not exist there create a new statement.
     *
     * @param key the id of the statement in statements.properties
     * @return
     * @throws SQLException
     */
    public PreparedStatement getPreparedStatement(String key) throws SQLException {
        requireJDBCConnection();

        PreparedStatement statement = statementCache.get(key);
        if(statement == null || statement.isClosed()) {
            statement = connection.prepareStatement(dialect.getStatement(key), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statementCache.put(key,statement);
        }
        statement.clearParameters();
        if(persistence.getDialect().isCursorSupported()) {
            statement.setFetchSize(persistence.getConfiguration().getCursorSize());
        }
        return statement;
    }

    /**
     * Return the prepared statement with the given identifier; first looks in the statement cache and if it does
     * not exist there create a new statement. This method is used for building statements with variable argument
     * numbers (e.g. in an IN).
     *
     * @param key the id of the statement in statements.properties
     * @return
     * @throws SQLException
     */
    public PreparedStatement getPreparedStatement(String key, int numberOfArguments) throws SQLException {
        requireJDBCConnection();

        PreparedStatement statement = statementCache.get(key+numberOfArguments);
        if(statement == null || statement.isClosed()) {
            StringBuilder s = new StringBuilder();
            for(int i=0; i<numberOfArguments; i++) {
                if(i != 0) {
                    s.append(',');
                }
                s.append('?');
            }

            statement = connection.prepareStatement(String.format(dialect.getStatement(key),s.toString(), numberOfArguments), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statementCache.put(key+numberOfArguments,statement);
        }
        statement.clearParameters();
        if(persistence.getDialect().isCursorSupported()) {
            statement.setFetchSize(persistence.getConfiguration().getCursorSize());
        }
        return statement;
    }


    /**
     * Get next number in a sequence; for databases without sequence support (e.g. MySQL), this method will first update a
     * sequence table and then return the value.
     *
     * @param sequenceName the identifier in statements.properties for querying the sequence
     * @return a new sequence ID
     * @throws SQLException
     */
    public long getNextSequence(String sequenceName) throws SQLException {
        return persistence.getIdGenerator().getId();
    }

    public long getDatabaseSequence(String sequenceName) throws SQLException {
        requireJDBCConnection();

        // retrieve a new node id and set it in the node object

        // if there is a preparation needed to update the transaction, run it first
        if(dialect.hasStatement(sequenceName+".prep")) {
            PreparedStatement prepNodeId = getPreparedStatement(sequenceName+".prep");
            prepNodeId.executeUpdate();
        }

        PreparedStatement queryNodeId = getPreparedStatement(sequenceName);
        ResultSet resultNodeId = queryNodeId.executeQuery();
        try {
            if(resultNodeId.next()) {
                return resultNodeId.getLong(1);
            } else {
                throw new SQLException("the sequence did not return a new value");
            }
        } finally {
            resultNodeId.close();
        }
    }


    private void cacheNode(KiWiNode node) {
        if(node.getId() >= 0) {
            nodeCache.put(node.getId(), node);
        }
        if(node instanceof KiWiUriResource) {
            uriCache.put(node.stringValue(), (KiWiUriResource) node);
        } else if(node instanceof KiWiAnonResource) {
            bnodeCache.put(node.stringValue(), (KiWiAnonResource) node);
        } else if(node instanceof KiWiLiteral) {
            literalCache.put(LiteralCommons.createCacheKey((Literal) node), (KiWiLiteral) node);
        }
    }

    private void cacheTriple(KiWiTriple triple) {
        if(triple.getId() >= 0) {
            tripleCache.put(triple.getId(), triple);
        }
    }

    private void removeCachedTriple(KiWiTriple triple) {
        if(triple.getId() >= 0) {
            tripleCache.remove(triple.getId());
        }
    }

    /**
     * Return a collection of database tables contained in the database. This query is used for checking whether
     * the database needs to be created when initialising the system.
     *
     *
     *
     * @return
     * @throws SQLException
     */
    public Set<String> getDatabaseTables() throws SQLException {
        requireJDBCConnection();

        PreparedStatement statement = getPreparedStatement("meta.tables");
        ResultSet result = statement.executeQuery();
        try {
            Set<String> tables = new HashSet<String>();
            while(result.next()) {
                tables.add(result.getString(1).toLowerCase());
            }
            return tables;
        } finally {
            result.close();
        }
    }

    /**
     * Return the metadata value with the given key; can be used by KiWi modules to retrieve module-specific metadata.
     *
     * @param key
     * @return
     * @throws SQLException
     */
    public String getMetadata(String key) throws SQLException {
        requireJDBCConnection();

        PreparedStatement statement = getPreparedStatement("meta.get");
        statement.setString(1,key);
        ResultSet result = statement.executeQuery();
        try {
            if(result.next()) {
                return result.getString(1);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }


    /**
     * Update the metadata value for the given key; can be used by KiWi modules to set module-specific metadata.
     *
     * @param key
     * @return
     * @throws SQLException
     */
    public void setMetadata(String key, String value) throws SQLException {
        requireJDBCConnection();

        PreparedStatement statement = getPreparedStatement("meta.get");
        ResultSet result = statement.executeQuery();
        try {
            if(result.next()) {
                PreparedStatement update = getPreparedStatement("meta.update");
                update.clearParameters();
                update.setString(1, value);
                update.setString(2, key);
                update.executeUpdate();
            } else {
                PreparedStatement insert = getPreparedStatement("meta.insert");
                insert.clearParameters();
                insert.setString(1, key);
                insert.setString(2, value);
                insert.executeUpdate();
            }
        } finally {
            result.close();
        }
    }


    /**
     * Return the KiWi version of the database this connection is operating on. This query is necessary for
     * checking proper state of a database when initialising the system.
     *
     * @return
     */
    public int getDatabaseVersion() throws SQLException {
        requireJDBCConnection();

        PreparedStatement statement = getPreparedStatement("meta.version");
        ResultSet result = statement.executeQuery();
        try {
            if(result.next()) {
                return Integer.parseInt(result.getString(1));
            } else {
                throw new SQLException("no version information available");
            }
        } finally {
            result.close();
        }
    }


    /**
     * Sets this connection's auto-commit mode to the given state.
     * If a connection is in auto-commit mode, then all its SQL
     * statements will be executed and committed as individual
     * transactions.  Otherwise, its SQL statements are grouped into
     * transactions that are terminated by a call to either
     * the method <code>commit</code> or the method <code>rollback</code>.
     * By default, new connections are in auto-commit
     * mode.
     * <P>
     * The commit occurs when the statement completes. The time when the statement
     * completes depends on the type of SQL Statement:
     * <ul>
     * <li>For DML statements, such as Insert, Update or Delete, and DDL statements,
     * the statement is complete as soon as it has finished executing.
     * <li>For Select statements, the statement is complete when the associated result
     * set is closed.
     * <li>For <code>CallableStatement</code> objects or for statements that return
     * multiple results, the statement is complete
     * when all of the associated result sets have been closed, and all update
     * counts and output parameters have been retrieved.
     *</ul>
     * <P>
     * <B>NOTE:</B>  If this method is called during a transaction and the
     * auto-commit mode is changed, the transaction is committed.  If
     * <code>setAutoCommit</code> is called and the auto-commit mode is
     * not changed, the call is a no-op.
     *
     * @param autoCommit <code>true</code> to enable auto-commit mode;
     *         <code>false</code> to disable it
     * @exception java.sql.SQLException if a database access error occurs,
     *  setAutoCommit(true) is called while participating in a distributed transaction,
     * or this method is called on a closed connection
     * @see #getAutoCommit
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        if(connection != null) {
            connection.setAutoCommit(autoCommit);
        }
    }


    /**
     * Retrieves the current auto-commit mode for this <code>Connection</code>
     * object.
     *
     * @return the current state of this <code>Connection</code> object's
     *         auto-commit mode
     * @exception java.sql.SQLException if a database access error occurs
     * or this method is called on a closed connection
     * @see #setAutoCommit
     */
    public boolean getAutoCommit() throws SQLException {
        return autoCommit;
    }

    /**
     * Return true if batched commits are enabled. Batched commits will try to group database operations and
     * keep a memory log while storing triples. This can considerably improve the database performance.
     * @return
     */
    public boolean isBatchCommit() {
        return batchCommit;
    }

    /**
     * Enabled batched commits. Batched commits will try to group database operations and
     * keep a memory log while storing triples. This can considerably improve the database performance.
     * @return
     */
    public void setBatchCommit(boolean batchCommit) {
        if(dialect.isBatchSupported()) {
            this.batchCommit = batchCommit;
        } else {
            log.warn("batch commits are not supported by this database dialect");
        }
    }


    /**
     * Return the size of a batch for batched commits. Batched commits will try to group database operations and
     * keep a memory log while storing triples. This can considerably improve the database performance.
     * @return
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Set the size of a batch for batched commits. Batched commits will try to group database operations and
     * keep a memory log while storing triples. This can considerably improve the database performance.
     * @param batchSize
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Makes all changes made since the previous
     * commit/rollback permanent and releases any database locks
     * currently held by this <code>Connection</code> object.
     * This method should be
     * used only when auto-commit mode has been disabled.
     *
     * @exception java.sql.SQLException if a database access error occurs,
     * this method is called while participating in a distributed transaction,
     * if this method is called on a closed conection or this
     *            <code>Connection</code> object is in auto-commit mode
     * @see #setAutoCommit
     */
    public synchronized void commit() throws SQLException {
        numberOfCommits++;

        RetryExecution execution = new RetryExecution("COMMIT");
        execution.execute(connection, new RetryCommand<Void>() {
            @Override
            public Void run() throws SQLException {
                if(tripleBatch != null && tripleBatch.size() > 0) {
                    flushBatch();
                }


                deletedStatementsLog = BloomFilter.create(Funnels.longFunnel(), 100000);

                if(connection != null) {
                    connection.commit();
                }

                return null;
            }
        });

        this.transactionId = getNextSequence("seq.tx");
    }

    /**
     * Undoes all changes made in the current transaction
     * and releases any database locks currently held
     * by this <code>Connection</code> object. This method should be
     * used only when auto-commit mode has been disabled.
     *
     * @exception java.sql.SQLException if a database access error occurs,
     * this method is called while participating in a distributed transaction,
     * this method is called on a closed connection or this
     *            <code>Connection</code> object is in auto-commit mode
     * @see #setAutoCommit
     */
    public void rollback() throws SQLException {
        if(tripleBatch != null && tripleBatch.size() > 0) {
            synchronized (tripleBatch) {
                for(KiWiTriple triple : tripleBatch) {
                    triple.setId(-1L);
                }
                tripleBatch.clear();
            }
        }
        deletedStatementsLog = BloomFilter.create(Funnels.longFunnel(), 100000);
        if(connection != null && !connection.isClosed()) {
            connection.rollback();
        }

        this.transactionId = getNextSequence("seq.tx");
    }

    /**
     * Retrieves whether this <code>Connection</code> object has been
     * closed.  A connection is closed if the method <code>close</code>
     * has been called on it or if certain fatal errors have occurred.
     * This method is guaranteed to return <code>true</code> only when
     * it is called after the method <code>Connection.close</code> has
     * been called.
     * <P>
     * This method generally cannot be called to determine whether a
     * connection to a database is valid or invalid.  A typical client
     * can determine that a connection is invalid by catching any
     * exceptions that might be thrown when an operation is attempted.
     *
     * @return <code>true</code> if this <code>Connection</code> object
     *         is closed; <code>false</code> if it is still open
     * @exception java.sql.SQLException if a database access error occurs
     */
    public boolean isClosed() throws SQLException {
        if(connection != null) {
            return connection.isClosed();
        } else {
            return false;
        }
    }


    /**
     * Releases this <code>Connection</code> object's database and JDBC resources
     * immediately instead of waiting for them to be automatically released.
     * <P>
     * Calling the method <code>close</code> on a <code>Connection</code>
     * object that is already closed is a no-op.
     * <P>
     * It is <b>strongly recommended</b> that an application explicitly
     * commits or rolls back an active transaction prior to calling the
     * <code>close</code> method.  If the <code>close</code> method is called
     * and there is an active transaction, the results are implementation-defined.
     * <P>
     *
     * @exception java.sql.SQLException SQLException if a database access error occurs
     */
    public void close() throws SQLException {
        closed = true;

        if(connection != null) {
            // close all prepared statements
            try {
                for(Map.Entry<String,PreparedStatement> entry : statementCache.entrySet()) {
                    try {
                        entry.getValue().close();
                    } catch (SQLException ex) {}
                }
            } catch(AbstractMethodError ex) {
                log.debug("database system does not allow closing statements");
            }

            persistence.releaseJDBCConnection(connection);
        }
    }


    int retry = 0;

    public synchronized void flushBatch() throws SQLException {
        if(batchCommit && tripleBatch != null) {
            requireJDBCConnection();

            commitLock.lock();
            try {
                RetryExecution execution = new RetryExecution("FLUSH BATCH");
                execution.setUseSavepoint(true);
                execution.execute(connection, new RetryCommand<Void>() {
                    @Override
                    public Void run() throws SQLException {
                        PreparedStatement insertTriple = getPreparedStatement("store.triple");
                        insertTriple.clearParameters();
                        insertTriple.clearBatch();

                        synchronized (tripleBatch) {
                            for(KiWiTriple triple : tripleBatch) {
                                // retrieve a new triple ID and set it in the object
                                if(triple.getId() < 0) {
                                    triple.setId(getNextSequence("seq.triples"));
                                }

                                insertTriple.setLong(1,triple.getId());
                                insertTriple.setLong(2,triple.getSubject().getId());
                                insertTriple.setLong(3,triple.getPredicate().getId());
                                insertTriple.setLong(4,triple.getObject().getId());
                                if(triple.getContext() != null) {
                                    insertTriple.setLong(5,triple.getContext().getId());
                                } else {
                                    insertTriple.setNull(5, Types.BIGINT);
                                }
                                insertTriple.setBoolean(6,triple.isInferred());
                                insertTriple.setTimestamp(7, new Timestamp(triple.getCreated().getTime()));

                                insertTriple.addBatch();
                            }
                        }
                        insertTriple.executeBatch();

                        tripleBatch.clear();

                        return null;
                    }
                });

            }  finally {
                commitLock.unlock();
            }

        }

    }

    /**
     * Return the current transaction ID
     * @return
     */
    public long getTransactionId() {
        return transactionId;
    }

    protected interface RetryCommand<T> {

        public T run() throws SQLException;
    }

    /**
     * A generic implementation of an SQL command that might fail (e.g. because of a timeout or concurrency situation)
     * and should be retried several times before giving up completely.
     *
     */
    protected class RetryExecution<T>  {

        // counter for current number of retries
        private int retries = 0;

        // how often to reattempt the operation
        private int maxRetries = 10;

        // how long to wait before retrying
        private long retryInterval = 1000;

        // use an SQL savepoint and roll back in case a retry is needed?
        private boolean useSavepoint = false;

        private String name;

        // if non-empty: only retry on the SQL states contained in this set
        private Set<String> sqlStates;

        public RetryExecution(String name) {
            this.name = name;
            this.sqlStates = new HashSet<>();
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public long getRetryInterval() {
            return retryInterval;
        }

        public void setRetryInterval(long retryInterval) {
            this.retryInterval = retryInterval;
        }

        public boolean isUseSavepoint() {
            return useSavepoint;
        }

        public void setUseSavepoint(boolean useSavepoint) {
            this.useSavepoint = useSavepoint;
        }

        public Set<String> getSqlStates() {
            return sqlStates;
        }

        public T execute(Connection connection, RetryCommand<T> command) throws SQLException {
            if(!closed) {
                Savepoint savepoint = null;
                if(useSavepoint) {
                    savepoint = connection.setSavepoint();
                }
                try {
                    T result = command.run();

                    if(useSavepoint && savepoint != null) {
                        connection.releaseSavepoint(savepoint);
                    }

                    return result;
                } catch (SQLException ex) {
                    if(retries < maxRetries && (sqlStates.size() == 0 || sqlStates.contains(ex.getSQLState()))) {
                        if(useSavepoint && savepoint != null) {
                            connection.rollback(savepoint);
                        }
                        Random rnd = new Random();
                        long sleep = retryInterval - 250 + rnd.nextInt(500);
                        log.warn("{}: temporary conflict, retrying in {} ms ... (thread={}, retry={})", name, sleep, Thread.currentThread().getName(), retries);
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {}
                        retries++;
                        T result = execute(connection, command);
                        retries--;

                        return result;
                    } else {
                        log.error("{}: temporary conflict could not be solved! (error: {})", name, ex.getMessage());

                        log.debug("main exception:",ex);
                        log.debug("next exception:",ex.getNextException());
                        throw ex;
                    }
                }
            } else {
                return null;
            }

        }

    }


}
