/*
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
package org.apache.marmotta.ldcache.backend.kiwi.persistence;

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiResource;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.util.ResultSetIteration;
import org.apache.marmotta.kiwi.persistence.util.ResultTransformerFunction;
import org.apache.marmotta.ldcache.backend.kiwi.model.KiWiCacheEntry;
import org.apache.marmotta.ldcache.model.CacheEntry;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCachingKiWiPersistenceConnection implements Closeable {

    private static Logger log = LoggerFactory.getLogger(LDCachingKiWiPersistenceConnection.class);


    private KiWiConnection connection;

    /**
     * Cache entries by resource
     */
    private Map<String,KiWiCacheEntry> entryResourceCache;


    /**
     * Cache entries by ID
     */
    private Map<Long,KiWiCacheEntry> entryIdCache;


    public LDCachingKiWiPersistenceConnection(KiWiConnection connection) throws SQLException {
        this.connection    = connection;

        entryResourceCache = connection.getCacheManager().getCacheByName("ldcache-entry-uri");
        entryIdCache       = connection.getCacheManager().getCacheByName("ldcache-entry-id");
    }

    public KiWiCacheEntry constructCacheEntry(ResultSet row) throws SQLException {
        Long id = row.getLong("id");

        KiWiCacheEntry cached = entryIdCache.get(id);

        // lookup element in cache first, so we can avoid reconstructing it if it is already there
        if(cached != null) {
            return cached;
        }

        KiWiCacheEntry entry = new KiWiCacheEntry();
        entry.setId(id);
        entry.setLastRetrieved(new Date(row.getTimestamp("retrieved_at").getTime()));
        entry.setExpiryDate(new Date(row.getTimestamp("expires_at").getTime()));
        entry.setUpdateCount(row.getInt("update_count"));
        entry.setResource((URI) connection.loadNodeById(row.getLong("resource_id")));
        entry.setTripleCount(row.getInt("triple_count"));

        entryIdCache.put(id,entry);
        entryResourceCache.put(entry.getResource().stringValue(),entry);

        return entry;
    }

    /**
     * Load the cache entry for the given URI from the database.
     *
     * @param uri the URI of the cached resource for which to return the cache entry
     * @return an instance of KiWiCacheEntry representing the caching metadata for the given resource, or null in case there
     *         is no entry for this resource
     * @throws SQLException
     */
    public KiWiCacheEntry getCacheEntry(String uri) throws SQLException {

        KiWiCacheEntry cached = entryResourceCache.get(uri);

        // lookup element in cache first, so we can avoid reconstructing it if it is already there
        if(cached != null) {
            return cached;
        }

        PreparedStatement query = connection.getPreparedStatement("load.entry_by_uri");
        query.setString(1, uri);
        query.setMaxRows(1);

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return constructCacheEntry(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Store the cache entry passed as argument in the database. In case the passed argument is not an instance of
     * KiWiCacheEntry, it will first be converted into a KiWiCacheEntry by copying the fields. In this case, the
     * stored object will not be the same instance as the object passed as argument.
     *
     * @param entry the cache entry to store
     * @throws SQLException
     */
    public void storeCacheEntry(CacheEntry entry) throws SQLException {
        KiWiCacheEntry kEntry;
        if(entry instanceof KiWiCacheEntry) {
            kEntry = (KiWiCacheEntry) entry;
        } else {
            kEntry = new KiWiCacheEntry();
            kEntry.setExpiryDate(entry.getExpiryDate());
            kEntry.setLastRetrieved(entry.getLastRetrieved());
            kEntry.setUpdateCount(entry.getUpdateCount());
            kEntry.setResource(entry.getResource());
            kEntry.setTripleCount(entry.getTripleCount());
        }

        if(! (entry.getResource() instanceof KiWiResource) || ((KiWiResource) entry.getResource()).getId() < 0) {
            throw new IllegalStateException("the resource contained in the cache entry is not a KiWiResource!");
        }

        // needed before the entry can be inserted
        connection.flushBatch();

        kEntry.setId(connection.getNextSequence());

        PreparedStatement insertEntry = connection.getPreparedStatement("store.entry");
        insertEntry.setLong(1, kEntry.getId());
        insertEntry.setTimestamp(2, new Timestamp(kEntry.getLastRetrieved().getTime()));
        insertEntry.setTimestamp(3,new Timestamp(kEntry.getExpiryDate().getTime()));
        insertEntry.setLong(4,((KiWiNode)kEntry.getResource()).getId());
        insertEntry.setInt(5, kEntry.getUpdateCount());
        insertEntry.setInt(6, kEntry.getTripleCount());
        insertEntry.executeUpdate();

        log.debug("persisted ld-cache entry with id {}", kEntry.getId());
        
        entryIdCache.put(kEntry.getId(),kEntry);
        entryResourceCache.put(kEntry.getResource().stringValue(),kEntry);

    }

    /**
     * Remove the given cache entry from the database. The cache entry passed as argument must be a persistent instance
     * of KiWiCacheEntry.
     * @param entry
     * @throws SQLException
     */
    public void removeCacheEntry(CacheEntry entry) throws SQLException {
        if(! (entry instanceof KiWiCacheEntry) || ((KiWiCacheEntry) entry).getId() == null) {
            throw new IllegalStateException("the passed cache entry is not managed by this connection");
        }

        PreparedStatement deleteEntry = connection.getPreparedStatement("delete.entry");
        deleteEntry.setLong(1,((KiWiCacheEntry) entry).getId());
        deleteEntry.executeUpdate();

        entryIdCache.remove(((KiWiCacheEntry) entry).getId());
        entryResourceCache.remove(entry.getResource().stringValue());
    }

    /**
     * Remove the given cache entry from the database. The cache entry passed as argument must be a persistent instance
     * of KiWiCacheEntry.
     * @param uri URI of the entry to delete
     * @throws SQLException
     */
    public void removeCacheEntry(String uri) throws SQLException {

        PreparedStatement deleteEntry = connection.getPreparedStatement("delete.entry_by_uri");
        deleteEntry.setString(1,uri);
        deleteEntry.executeUpdate();

        KiWiCacheEntry cached = entryResourceCache.get(uri);

        if(cached != null) {
            entryResourceCache.remove(uri);
            entryIdCache.remove(cached.getId());
        }
    }


    /**
     * List all cache entries with an expiry date older than the current time.
     *
     * @return a closeable iteration with KiWiCacheEntries; needs to be released by the caller
     * @throws SQLException
     */
    public CloseableIteration<KiWiCacheEntry,SQLException> listExpired() throws SQLException {
        PreparedStatement queryExpired = connection.getPreparedStatement("query.entries_expired");
        final ResultSet result = queryExpired.executeQuery();

        return new ResultSetIteration<KiWiCacheEntry>(result, new ResultTransformerFunction<KiWiCacheEntry>() {
            @Override
            public KiWiCacheEntry apply(ResultSet input) throws SQLException {
                return constructCacheEntry(result);
            }
        });
    }

    /**
     * List all cache entries in the database, regardless of expiry date.
     *
     * @return a closeable iteration with KiWiCacheEntries; needs to be released by the caller
     * @throws SQLException
     */
    public CloseableIteration<KiWiCacheEntry,SQLException> listAll() throws SQLException {
        PreparedStatement queryExpired = connection.getPreparedStatement("query.entries_all");
        final ResultSet result = queryExpired.executeQuery();

        return new ResultSetIteration<KiWiCacheEntry>(result, new ResultTransformerFunction<KiWiCacheEntry>() {
            @Override
            public KiWiCacheEntry apply(ResultSet input) throws SQLException {
                return constructCacheEntry(result);
            }
        });
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
     */
    public void commit() throws SQLException {
        connection.commit();
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
    public void close()  {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("error closing connection",e);
        }
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
        return connection.isClosed();
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
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /**
     * Store a new node in the database. The method will retrieve a new database id for the node and update the
     * passed object. Afterwards, the node data will be inserted into the database using appropriate INSERT
     * statements. The caller must make sure the connection is committed and closed properly.
     * <p/>
     * If the node already has an ID, the method will do nothing (assuming that it is already persistent)
     *
     * @param node
     * @throws java.sql.SQLException
     */
    public void storeNode(KiWiNode node) throws SQLException {
        connection.storeNode(node);
    }

    /**
     * Return a collection of database tables contained in the database. This query is used for checking whether
     * the database needs to be created when initialising the system.
     *
     *
     *
     * @return
     * @throws java.sql.SQLException
     */
    public Set<String> getDatabaseTables() throws SQLException {
        return connection.getDatabaseTables();
    }

    /**
     * Return the KiWi version of the database this connection is operating on. This query is necessary for
     * checking proper state of a database when initialising the system.
     *
     * @return
     */
    public int getDatabaseVersion() throws SQLException {
        return connection.getDatabaseVersion();
    }
}
