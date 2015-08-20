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
package org.apache.marmotta.kiwi.versioning.persistence;

import com.google.common.base.Preconditions;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import org.apache.marmotta.kiwi.caching.CacheManager;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.persistence.util.ResultSetIteration;
import org.apache.marmotta.kiwi.persistence.util.ResultTransformerFunction;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiVersioningConnection extends KiWiConnection {

    private static Logger log = LoggerFactory.getLogger(KiWiVersioningConnection.class);

    public KiWiVersioningConnection(KiWiPersistence persistence, KiWiDialect dialect, CacheManager cacheManager) throws SQLException {
        super(persistence, dialect, cacheManager);
    }


    /**
     * Store a new version in the database. Will query for a new sequence ID for versions, insert a new row in the
     * versions table, and then add join entries to the versions_added and versions_removed tables for each added
     * and removed triple.
     * <p/>
     * Note that this method assumes the transaction data to be already consistent, i.e. a triple is not added and
     * removed at the same time. No check for this situation is performed
     *
     * @param data
     * @return
     * @throws SQLException
     */
    public void storeVersion(Version data) throws SQLException {
        if(data.getId() >= 0) {
            log.warn("version {} already had a version ID, not persisting", data);
            return;
        }

        requireJDBCConnection();

        // first create a new entry in the version table
        data.setId(getNextSequence());

        PreparedStatement insertVersion = getPreparedStatement("store.version");
        synchronized (insertVersion) {
            insertVersion.setLong(1,data.getId());
            if(data.getCreator() != null) {
                insertVersion.setLong(2,data.getCreator().getId());
            } else {
                insertVersion.setObject(2, null);
            }
            insertVersion.setTimestamp(3, new Timestamp(data.getCommitTime().getTime()));
            insertVersion.executeUpdate();
        }

        // then add join table entries for every added triple
        PreparedStatement insertAdded = getPreparedStatement("store.version_added");
        synchronized (insertAdded) {
            insertAdded.clearParameters();
            for(Statement added : data.getAddedTriples()) {
                if(added instanceof KiWiTriple) {
                    insertAdded.setLong(1,data.getId());
                    insertAdded.setLong(2,((KiWiTriple) added).getId());
                    insertAdded.addBatch();
                } else {
                    // maybe we should even throw an exception here
                    log.warn("cannot create version with statements that are not instances of KiWiTriple!");
                }
            }
            insertAdded.executeBatch(); // if this is a performance issue, we could also run it every 1000 inserts or so
        }

        // then add join table entries for every removed triple
        PreparedStatement insertRemoved = getPreparedStatement("store.version_removed");
        synchronized (insertRemoved) {
            insertRemoved.clearParameters();
            for(Statement added : data.getRemovedTriples()) {
                if(added instanceof KiWiTriple) {
                    insertRemoved.setLong(1,data.getId());
                    insertRemoved.setLong(2,((KiWiTriple) added).getId());
                    insertRemoved.addBatch();
                } else {
                    // maybe we should even throw an exception here
                    log.warn("cannot create version with statements that are not instances of KiWiTriple!");
                }
            }
            insertRemoved.executeBatch(); // if this is a performance issue, we could also run it every 1000 inserts or so
        }

    }

    /**
     * Remove the version with the id passed as argument, including all references to added and removed triples. The
     * triples themselves are not deleted immediately, we let the garbage collector carry this out periodically.
     * @param id
     * @throws SQLException
     */
    public void removeVersion(Long id) throws SQLException {
        Preconditions.checkNotNull(id);
        Preconditions.checkArgument(id > 0);

        requireJDBCConnection();

        PreparedStatement removeAdded = getPreparedStatement("delete.version_added");
        removeAdded.clearParameters();
        removeAdded.setLong(1, id);
        removeAdded.executeUpdate();

        PreparedStatement removeRemoved = getPreparedStatement("delete.version_removed");
        removeRemoved.clearParameters();
        removeRemoved.setLong(1, id);
        removeRemoved.executeUpdate();

        PreparedStatement removeVersion = getPreparedStatement("delete.version");
        removeVersion.clearParameters();
        removeVersion.setLong(1, id);
        removeVersion.executeUpdate();

    }

    /**
     * Remove all versions until the date given as argument. Iterates over all versions and deletes them individually.
     * Entries in join tables (added/removed triples) are also deleted, the triples themselves not. Deleted triples
     * without version will later be cleaned up by the garbage collector
     * @param until date until when to delete versions
     * @throws SQLException
     */
    public void removeVersions(Date until) throws SQLException {
        removeVersions(new Date(0), until);
    }


    /**
     * Remove all versions in the given time interval. Iterates over all versions and deletes them individually.
     * Entries in join tables (added/removed triples) are also deleted, the triples themselves not. Deleted triples
     * without version will later be cleaned up by the garbage collector
     * @param from date after which versions will be deleted
     * @param to   date before which versions will be deleted
     * @throws SQLException
     */
    public void removeVersions(Date from, Date to) throws SQLException {
        CloseableIteration<Version, SQLException> it = listVersionsInternal(from,to);
        while(it.hasNext()) {
            Version next = it.next();
            removeVersion(next.getId());
        }
    }



    /**
     * Retrieve a version by its id. If the version does not exist, returns null
     *
     * @param id
     * @return
     * @throws SQLException
     */
    public Version getVersion(Long id) throws SQLException {
        requireJDBCConnection();

        PreparedStatement queryVersions = getPreparedStatement("load.version_by_id");
        queryVersions.setLong(1,id);
        queryVersions.setMaxRows(1);

        ResultSet r = queryVersions.executeQuery();
        try {
            if(r.next()) {
                return constructVersionFromDatabase(r);
            } else {
                return null;
            }
        } finally {
            r.close();
        }
    }

    /**
     * Return the version that is the most recent version for a resource given a reference date. The method will either
     * return the version that was current for the resource at the given date or return null in case such a version
     * does not exist (e.g. before the resource was created).
     *
     * @param resource  the resource for which to find a version
     * @param date      the reference date
     * @return the latest version of the resource at the given date, or null if such a version does not exist
     * @throws SQLException
     */
    public Version getLatestVersion(KiWiResource resource, Date date) throws SQLException {
        requireJDBCConnection();

        PreparedStatement queryVersions = getPreparedStatement("load.versions_by_resource_latest");
        synchronized (queryVersions) {
            queryVersions.setLong(1, resource.getId());
            queryVersions.setTimestamp(2, new Timestamp(date.getTime()));
            queryVersions.setMaxRows(1);

            ResultSet r = queryVersions.executeQuery();
            try {
                if(r.next()) {
                    return constructVersionFromDatabase(r);
                } else {
                    return null;
                }
            } finally {
                r.close();
            }
        }
    }


    /**
     * List all versions in the database; operates directly on the result set, i.e. the iteration is carried out
     * lazily and needs to be closed when iteration is completed.
     *
     * @return
     * @throws SQLException
     */
    public RepositoryResult<Version> listVersions() throws SQLException {

        return new RepositoryResult<Version>(
                new ExceptionConvertingIteration<Version, RepositoryException>(listVersionsInternal()) {
                    @Override
                    protected RepositoryException convert(Exception e) {
                        return new RepositoryException("database error while iterating over result set",e);
                    }
                }

        );
    }


    /**
     * List all versions in the database; operates directly on the result set, i.e. the iteration is carried out
     * lazily and needs to be closed when iteration is completed.
     *
     * @return
     * @throws SQLException
     */
    private CloseableIteration<Version, SQLException> listVersionsInternal() throws SQLException {
        requireJDBCConnection();

        PreparedStatement queryVersions = getPreparedStatement("load.versions");

        final ResultSet result = queryVersions.executeQuery();
        return new ResultSetIteration<Version>(result, new ResultTransformerFunction<Version>() {
            @Override
            public Version apply(ResultSet row) throws SQLException {
                return constructVersionFromDatabase(result);
            }
        });
    }


    /**
     * List all versions in the database affecting the given resource as subject; operates directly on the result set,
     * i.e. the iteration is carried out lazily and needs to be closed when iteration is completed.
     *
     * @return
     * @throws SQLException
     */
    public RepositoryResult<Version> listVersions(KiWiResource r) throws SQLException {

        return new RepositoryResult<Version>(
                new ExceptionConvertingIteration<Version, RepositoryException>(listVersionsInternal(r)) {
                    @Override
                    protected RepositoryException convert(Exception e) {
                        return new RepositoryException("database error while iterating over result set",e);
                    }
                }

        );
    }

    /**
     * List all versions in the database affecting the given resource as subject; operates directly on the result set,
     * i.e. the iteration is carried out lazily and needs to be closed when iteration is completed.
     *
     * @return
     * @throws SQLException
     */
    private CloseableIteration<Version, SQLException> listVersionsInternal(KiWiResource r) throws SQLException {
        if(r.getId() < 0) {
            return new EmptyIteration<Version, SQLException>();
        } else {
            requireJDBCConnection();

            PreparedStatement queryVersions = getPreparedStatement("load.versions_by_resource");
            queryVersions.setLong(1,r.getId());

            final ResultSet result = queryVersions.executeQuery();
            return new ResultSetIteration<Version>(result, new ResultTransformerFunction<Version>() {
                @Override
                public Version apply(ResultSet row) throws SQLException {
                    return constructVersionFromDatabase(result);
                }
            });
        }

    }



    /**
     * List all versions in the database; operates directly on the result set, i.e. the iteration is carried out
     * lazily and needs to be closed when iteration is completed.
     *
     * @return
     * @throws SQLException
     */
    public RepositoryResult<Version> listVersions(final Date from, final Date to) throws SQLException {

        return new RepositoryResult<Version>(
                new ExceptionConvertingIteration<Version, RepositoryException>(listVersionsInternal(from, to)) {
                    @Override
                    protected RepositoryException convert(Exception e) {
                        return new RepositoryException("database error while iterating over result set",e);
                    }
                }

        );
    }

    /**
     * List all versions in the database; operates directly on the result set, i.e. the iteration is carried out
     * lazily and needs to be closed when iteration is completed.
     *
     * @return
     * @throws SQLException
     */
    private CloseableIteration<Version, SQLException> listVersionsInternal(Date from, Date to) throws SQLException {
        requireJDBCConnection();

        PreparedStatement queryVersions = getPreparedStatement("load.version_between");
        synchronized (queryVersions) {
            queryVersions.clearParameters();
            queryVersions.setTimestamp(1, new Timestamp(from.getTime()));
            queryVersions.setTimestamp(2, new Timestamp(to.getTime()));

            final ResultSet result = queryVersions.executeQuery();
            return new ResultSetIteration<Version>(result, new ResultTransformerFunction<Version>() {
                @Override
                public Version apply(ResultSet row) throws SQLException {
                    return constructVersionFromDatabase(result);
                }
            });
        }
    }



    /**
     * List all versions in the database; operates directly on the result set, i.e. the iteration is carried out
     * lazily and needs to be closed when iteration is completed.
     *
     * @return
     * @throws SQLException
     */
    public RepositoryResult<Version> listVersions(final KiWiResource r, final Date from, final Date to) throws SQLException {

        return new RepositoryResult<Version>(
                new ExceptionConvertingIteration<Version, RepositoryException>(listVersionsInternal(r, from, to)) {
                    @Override
                    protected RepositoryException convert(Exception e) {
                        return new RepositoryException("database error while iterating over result set",e);
                    }
                }

        );
    }

    /**
     * List all versions in the database; operates directly on the result set, i.e. the iteration is carried out
     * lazily and needs to be closed when iteration is completed.
     *
     * @return
     * @throws SQLException
     */
    private CloseableIteration<Version, SQLException> listVersionsInternal(KiWiResource r, Date from, Date to) throws SQLException {
        requireJDBCConnection();

        PreparedStatement queryVersions = getPreparedStatement("load.versions_by_resource_between");
        synchronized (queryVersions) {
            queryVersions.clearParameters();
            if(r.getId() < 0) {
                return new EmptyIteration<Version, SQLException>();
            } else {
                queryVersions.setLong(1, r.getId());
                queryVersions.setTimestamp(2, new Timestamp(from.getTime()));
                queryVersions.setTimestamp(3, new Timestamp(to.getTime()));

                final ResultSet result = queryVersions.executeQuery();
                return new ResultSetIteration<Version>(result, new ResultTransformerFunction<Version>() {
                    @Override
                    public Version apply(ResultSet row) throws SQLException {
                        return constructVersionFromDatabase(result);
                    }
                });
            }
        }
    }


    /**
     * Construct a version from the database using the data contained in the result set row passed as argument. The method
     * will load all added and removed triples in subsequent SQL queries.
     * <p/>
     * The method will not change the ResultSet iterator, only read its values, so it needs to be executed for each row separately.
     *
     * @param row
     * @return
     * @throws SQLException
     */
    protected Version constructVersionFromDatabase(ResultSet row) throws SQLException {
        Version result = new Version(row.getLong("id"));
        result.setCommitTime(new Date(row.getTimestamp("createdAt").getTime()));

        if(row.getObject("creator") != null) {
            result.setCreator((KiWiResource) loadNodeById(row.getLong("creator")));
        }

        // query the versions_added and versions_removed join tables to reconstruct the triple sets
        PreparedStatement queryAdded = getPreparedStatement("load.versions_added");
        synchronized (queryAdded) {
            queryAdded.clearParameters();
            queryAdded.setLong(1,result.getId());
            ResultSet addedRow = queryAdded.executeQuery();
            try {
                while(addedRow.next()) {
                    result.addTriple(loadTripleById(addedRow.getLong("triple_id")));
                }
            } finally {
                addedRow.close();
            }
        }

        PreparedStatement queryRemoved = getPreparedStatement("load.versions_removed");
        synchronized (queryRemoved) {
            queryRemoved.clearParameters();
            queryRemoved.setLong(1,result.getId());
            ResultSet removedRow = queryRemoved.executeQuery();
            try {
                while(removedRow.next()) {
                    result.removeTriple(loadTripleById(removedRow.getLong("triple_id")));
                }
            } finally {
                removedRow.close();
            }
        }

        return result;
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
     * @return a new RepositoryResult with a direct connection to the database; the result should be properly closed
     *         by the caller
     */
    public RepositoryResult<Statement> listTriplesSnapshot(KiWiResource subject, KiWiUriResource predicate, KiWiNode object, KiWiResource context, boolean inferred, Date snapshotDate) throws SQLException {

        return new RepositoryResult<Statement>(
                new ExceptionConvertingIteration<Statement, RepositoryException>(listTriplesInternalSnapshot(subject, predicate, object, context, inferred, snapshotDate)) {
                    @Override
                    protected RepositoryException convert(Exception e) {
                        return new RepositoryException("database error while iterating over result set",e);
                    }
                }

        );
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
     * @return a ClosableIteration that wraps the database ResultSet; needs to be closed explicitly by the caller
     * @throws SQLException
     */
    private CloseableIteration<Statement, SQLException> listTriplesInternalSnapshot(KiWiResource subject, KiWiUriResource predicate, KiWiNode object, KiWiResource context, boolean inferred, Date snapshotDate) throws SQLException {
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
                constructTripleQuerySnapshot(subject, predicate, object, context, inferred, snapshotDate),
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY
        );
        query.clearParameters();

        // set query parameters
        query.setTimestamp(1, new Timestamp(snapshotDate.getTime()));
        query.setTimestamp(2, new Timestamp(snapshotDate.getTime()));

        int position = 3;
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


        return new ResultSetIteration<Statement>(result, true, new ResultTransformerFunction<Statement>() {
            @Override
            public Statement apply(ResultSet row) throws SQLException {
                return constructTripleFromDatabase(result);
            }
        });
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
    protected String constructTripleQuerySnapshot(KiWiResource subject, KiWiUriResource predicate, KiWiNode object, KiWiResource context, boolean inferred, Date snapshotDate) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT id,subject,predicate,object,context,deleted,inferred,creator,createdAt,deletedAt FROM triples");
        builder.append(" WHERE  createdAt <= ? AND (deleted = false OR deletedAt > ?)");
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
        }
        if(!inferred) {
            builder.append(" AND inferred = false");
        }
        return builder.toString();

    }

    /**
     * Count all non-deleted triples in the triple store at the given snapshot date
     * @return
     * @throws SQLException
     */
    public long getSnapshotSize(Date snapshotDate) throws SQLException {
        PreparedStatement querySize = getPreparedStatement("query.snapshot_size");
        querySize.setTimestamp(1, new Timestamp(snapshotDate.getTime()));
        querySize.setTimestamp(2, new Timestamp(snapshotDate.getTime()));
        ResultSet result = querySize.executeQuery();
        try {
            if(result.next()) {
                return result.getLong(1);
            } else {
                return 0;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Count all non-deleted triples in the triple store at the given snapshot date
     * @return
     * @throws SQLException
     */
    public long getSnapshotSize(KiWiResource context, Date snapshotDate) throws SQLException {
        if(context.getId() < 0) {
            return 0;
        };

        requireJDBCConnection();

        PreparedStatement querySize = getPreparedStatement("query.snapshot_size_ctx");
        querySize.setLong(1,context.getId());
        querySize.setTimestamp(2, new Timestamp(snapshotDate.getTime()));
        querySize.setTimestamp(3, new Timestamp(snapshotDate.getTime()));

        ResultSet result = querySize.executeQuery();
        try {
            if(result.next()) {
                return result.getLong(1);
            } else {
                return 0;
            }
        } finally {
            result.close();
        }
    }

}
