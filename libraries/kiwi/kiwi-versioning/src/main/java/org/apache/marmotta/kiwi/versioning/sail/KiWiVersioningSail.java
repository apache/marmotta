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
package org.apache.marmotta.kiwi.versioning.sail;

import org.apache.marmotta.commons.sesame.filter.AlwaysTrueFilter;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.commons.sesame.transactions.api.TransactionListener;
import org.apache.marmotta.commons.sesame.transactions.api.TransactionalSail;
import org.apache.marmotta.commons.sesame.transactions.model.TransactionData;
import org.apache.marmotta.commons.sesame.transactions.wrapper.TransactionalSailWrapper;
import org.apache.marmotta.kiwi.model.rdf.KiWiResource;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.versioning.api.VersioningSail;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.kiwi.versioning.persistence.KiWiVersioningConnection;
import org.apache.marmotta.kiwi.versioning.persistence.KiWiVersioningPersistence;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A KiWi Stackable Sail offering versioning support for transactional sails. The versioning sail create a new
 * version entry in the database whenever a transaction commits successfully (implemented through transaction listener).
 * <p/>
 * This SAIL offers three additional methods (need to be accessed directly):
 * <ul>
 *     <li>getSnapshot(Date) returns a read-only SailConnection operating on a historic snapshot of the repository,
 *         i.e. it is possible to query the triple store with its state at any given point in history</li>
 *     <li>getVersions() returns a list of all versions that have been tracked in the triple store</li>
 *     <li>getVersions(Date from, Date to) returns a list of all versions that have been tracked between the given dates</li>
 * </ul>
 * Further functionalities like removing old versions or undoing certain versions are planned but currently not yet
 * implemented.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiVersioningSail extends TransactionalSailWrapper implements TransactionListener, VersioningSail {

    private static Logger log = LoggerFactory.getLogger(KiWiVersioningSail.class);

    protected final static long DEFAULT_CONNECTION_TIMEOUT = 20000L;

    private KiWiVersioningPersistence persistence;

    private final Set<KiWiSnapshotConnection> activeSnapshots;

    private SesameFilter<Statement> filter;

    /**
     * Build a new {@link KiWiVersioningSail} based on the provided parent
     * {@link TransactionalSail}.
     *
     * @param parent
     *            the {@link TransactionalSail} to base the
     *            {@link KiWiVersioningSail} on.
     */
    public KiWiVersioningSail(TransactionalSail parent) {
        this(parent,new AlwaysTrueFilter<Statement>());
    }

    /**
     * Build a new selective {@link KiWiVersioningSail} based on the provided
     * parent {@link TransactionalSail}. Only {@link Statement}s that are
     * accepted by the filter are included in versioning.
     *
     * @param parent
     *            the {@link TransactionalSail} to base the
     *            {@link KiWiVersioningSail} on.
     * @param filter
     *            a {@link SesameFilter} to filter out {@link Statement}s that
     *            should not be versioned. Only a {@link Statement} that is
     *            accepted by this filter will be versioned.
     *
     * @see SesameFilter#accept(Object)
     */
    public KiWiVersioningSail(TransactionalSail parent, SesameFilter<Statement> filter) {
        super(parent);
        this.persistence = new KiWiVersioningPersistence(getBaseStore().getPersistence());
        this.activeSnapshots = new HashSet<>();
        this.filter = filter;

        parent.addTransactionListener(this);
    }


    /**
     * Ensure that the versioning tables are properly initialised in the database.
     *
     * @throws SailException
     */
    @Override
    public void initialize() throws SailException {
        super.initialize();

        try {
            persistence.initDatabase();
        } catch (SQLException e) {
            throw new SailException("error while initialising versioning tables in database",e);
        }
    }

    /**
     * Return the KiWi store that is at the base of the SAIL stack. Throws an IllegalArgumentException in case the base
     * store is not a KiWi store.
     *
     * @return
     */
    public KiWiStore getBaseStore() {
        StackableSail current = this;
        while(current != null && current.getBaseSail() instanceof StackableSail) {
            current = (StackableSail) current.getBaseSail();
        }
        if(current != null && current.getBaseSail() instanceof KiWiStore) {
            return (KiWiStore) current.getBaseSail();
        } else {
            throw new IllegalStateException("the base store is not a KiWiStore (type: "+current.getBaseSail().getClass().getCanonicalName()+")!");
        }
    }

    /**
     * Return the versioning persistence object underlying this SAIL.
     *
     * @return
     */
    public KiWiVersioningPersistence getPersistence() {
        return persistence;
    }

    /**
     * Called after a transaction has committed. This method will take the transaction data and convert it into a
     * Version object, which will then be persisted in the KiWiVersioningPersistence.
     * @param data
     */
    @Override
    public void afterCommit(TransactionData data) {
        if(data.getAddedTriples().size() > 0 || data.getRemovedTriples().size() > 0) {
            Version version = new Version();

            version.setCommitTime(data.getCommitTime());
            for(Statement stmt : data.getAddedTriples()) {
                if(filter.accept(stmt)) {
                    version.getAddedTriples().add(stmt);
                }
            }
            for(Statement stmt : data.getRemovedTriples()) {
                if(filter.accept(stmt)) {
                    version.getRemovedTriples().add(stmt);
                }
            }

            if(version.getAddedTriples().size() > 0 || version.getRemovedTriples().size() > 0) {

                try {
                    final KiWiVersioningConnection connection = persistence.getConnection();
                    try {
                        connection.storeVersion(version);
                        connection.commit();
                    } catch (SQLException ex) {
                        log.warn("could not store versioning information (error: {}); rolling back...", ex.getMessage());
                        connection.rollback();
                    } finally {
                        connection.close();
                    }
                } catch(SQLException ex) {
                    log.warn("could not store versioning information (error: {})", ex.getMessage());
                }
            }
        }
    }

    /**
     * Called before a transaction commits. The transaction data will contain all changes done in the transaction since
     * the last commit. This method should be used in case the transaction listener aims to perform additional activities
     * in the same transaction, like inserting or updating database tables.
     *
     * @param data
     */
    @Override
    public void beforeCommit(TransactionData data) {
        // do nothing, versioning is carried out after commit
    }

    /**
     * Called when a transaction rolls back.
     */
    @Override
    public void rollback(TransactionData data) {
        // do nothing, don't create a version
    }

    /**
     * Get a read-only snapshot of the repository at the given date. Returns a sail connection that
     * can be used to access the triple data. Any attempts to modify the underlying data will throw
     * a SailReadOnlyException.
     *
     * @param snapshotDate the date of which to take the snapshot; the snapshot will consist of all
     *                     triples that have been created before or at the date and deleted after that date
     *                     (or not deleted at all).
     * @return a read-only sail connection to access the data of the triple store at the given date
     */
    @Override
    public KiWiSnapshotConnection getSnapshot(Date snapshotDate)  throws SailException {
        KiWiSnapshotConnection con = new KiWiSnapshotConnection(this,snapshotDate);
        activeSnapshots.add(con);
        return con;
    }

    /**
     * Unregister the snapshot connection, it has been cleaned up properly.
     * @param con
     */
    protected void closeSnapshotConnection(KiWiSnapshotConnection con) {
        synchronized (activeSnapshots) {
            activeSnapshots.remove(con);

            if(activeSnapshots.isEmpty()) {
                activeSnapshots.notifyAll();
            }
        }
    }

    /**
     * List all versions of this repository.
     *
     * @return
     */
    @Override
    public RepositoryResult<Version> listVersions()  throws SailException {
        try {
            final KiWiVersioningConnection connection = persistence.getConnection();
            return new RepositoryResult<Version>(connection.listVersions()) {
                @Override
                protected void handleClose() throws RepositoryException {
                    super.handleClose();

                    try {
                        connection.commit();
                        connection.close();
                    } catch (SQLException ex) {
                        throw new RepositoryException("database error while committing/closing connection");
                    }
                }
            };
        } catch(SQLException ex) {
            throw new SailException("database error while listing versions",ex);
        }
    }

    /**
     * List all versions of this repository between a start and end date.
     *
     * @return
     */
    @Override
    public RepositoryResult<Version> listVersions(Date from, Date to) throws SailException {
        try {
            final KiWiVersioningConnection connection = persistence.getConnection();
            return new RepositoryResult<Version>(connection.listVersions(from,to)) {
                @Override
                protected void handleClose() throws RepositoryException {
                    super.handleClose();

                    try {
                        connection.commit();
                        connection.close();
                    } catch (SQLException ex) {
                        throw new RepositoryException("database error while committing/closing connection");
                    }
                }
            };
        } catch(SQLException ex) {
            throw new SailException("database error while listing versions",ex);
        }
    }



    public Version getVersion(Long id) throws SailException {
        try {
            final KiWiVersioningConnection connection = persistence.getConnection();
            try {
                return connection.getVersion(id);
            } finally {
                connection.commit();
                connection.close();
            }

        } catch(SQLException ex) {
            throw new SailException("database error while listing versions",ex);
        }
    }


    /**
     * Remove the version with the id passed as argument, including all references to added and removed triples. The
     * triples themselves are not deleted immediately, we let the garbage collector carry this out periodically.
     * @param id  the database ID of the version (see {@link Version#getId()})
     * @throws SailException
     */
    public void removeVersion(Long id) throws SailException {
        try {
            try (KiWiVersioningConnection connection = persistence.getConnection()) {
                connection.removeVersion(id);
                connection.commit();
            }

        } catch(SQLException ex) {
            throw new SailException("database error while listing versions",ex);
        }
    }

    /**
     * Remove all versions until the date given as argument. Iterates over all versions and deletes them individually.
     * Entries in join tables (added/removed triples) are also deleted, the triples themselves not. Deleted triples
     * without version will later be cleaned up by the garbage collector
     * @param until date until when to delete versions
     * @throws SailException
     */
    public void removeVersions(Date until) throws SailException {
        try {
            try (KiWiVersioningConnection connection = persistence.getConnection()) {
                connection.removeVersions(until);
                connection.commit();
            }

        } catch(SQLException ex) {
            throw new SailException("database error while listing versions",ex);
        }
    }


    /**
     * Remove all versions in the given time interval. Iterates over all versions and deletes them individually.
     * Entries in join tables (added/removed triples) are also deleted, the triples themselves not. Deleted triples
     * without version will later be cleaned up by the garbage collector
     * @param from date after which versions will be deleted
     * @param to   date before which versions will be deleted
     * @throws SailException
     */
    public void removeVersions(Date from, Date to) throws SailException {
        try {
            try (KiWiVersioningConnection connection = persistence.getConnection()) {
                connection.removeVersions(from, to);
                connection.commit();
            }

        } catch(SQLException ex) {
            throw new SailException("database error while listing versions",ex);
        }
    }

    /**
     * Return the version that is the most recent version for a resource given a reference date. The method will either
     * return the version that was current for the resource at the given date or return null in case such a version
     * does not exist (e.g. before the resource was created).
     *
     * @param r         the resource for which to find a version
     * @param date      the reference date
     * @return the latest version of the resource at the given date, or null if such a version does not exist
     * @throws SQLException
     */
    public Version getLatestVersion(Resource r, Date date) throws SailException {
        try {
            final KiWiVersioningConnection connection = persistence.getConnection();

            KiWiResource kr = (KiWiResource) ((r instanceof URI) ? getValueFactory().createURI(r.stringValue()) : getValueFactory().createBNode(r.stringValue()));

            try {
                return connection.getLatestVersion(kr,date);
            } finally {
                connection.commit();
                connection.close();
            }

        } catch(SQLException ex) {
            throw new SailException("database error while listing versions",ex);
        }
    }


    /**
     * List all versions of this repository affecting the given resource as subject.
     *
     * @return
     */
    @Override
    public RepositoryResult<Version> listVersions(Resource r)  throws SailException {
        try {
            final KiWiVersioningConnection connection = persistence.getConnection();

            KiWiResource kr = (KiWiResource) ((r instanceof URI) ? getValueFactory().createURI(r.stringValue()) : getValueFactory().createBNode(r.stringValue()));

            return new RepositoryResult<Version>(connection.listVersions(kr)) {
                @Override
                protected void handleClose() throws RepositoryException {
                    super.handleClose();

                    try {
                        connection.commit();
                        connection.close();
                    } catch (SQLException ex) {
                        throw new RepositoryException("database error while committing/closing connection");
                    }
                }
            };
        } catch(SQLException ex) {
            throw new SailException("database error while listing versions",ex);
        }
    }

    /**
     * List all versions of this repository affecting the given resource as subject between a start and end date.
     *
     * @return
     */
    @Override
    public RepositoryResult<Version> listVersions(Resource r, Date from, Date to) throws SailException {
        try {
            final KiWiVersioningConnection connection = persistence.getConnection();

            KiWiResource kr = (KiWiResource) ((r instanceof URI) ? getValueFactory().createURI(r.stringValue()) : getValueFactory().createBNode(r.stringValue()));

            return new RepositoryResult<Version>(connection.listVersions(kr,from,to)) {
                @Override
                protected void handleClose() throws RepositoryException {
                    super.handleClose();

                    try {
                        connection.commit();
                        connection.close();
                    } catch (SQLException ex) {
                        throw new RepositoryException("database error while committing/closing connection");
                    }
                }
            };
        } catch(SQLException ex) {
            throw new SailException("database error while listing versions",ex);
        }
    }

    /**
     * Revert (undo) the version given as argument. This method creates a new transaction, adds all triples
     * that were deleted in the old version, removes all triples that were added in the old version, and commits
     * the transaction, effectively creating a new (reverted) version.
     *
     * @param version    the version to revert
     * @throws SailException in case reverting the version failed
     */
    public void revertVersion(Version version) throws SailException {
        SailConnection con = getConnection();
        try {
            con.begin();

            for(Statement stmt : version.getAddedTriples()) {
                con.removeStatements(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext());
            }

            for(Statement stmt : version.getRemovedTriples()) {
                con.addStatement(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext());
            }

            con.commit();
        } finally {
            con.close();
        }
    }


    @Override
    public void shutDown() throws SailException {
        // close all open connections after a grace period
        synchronized (activeSnapshots) {
            if(!activeSnapshots.isEmpty()) {
                // wait for a certain grace period for other threads still holding connections to finish
                log.warn("waiting for open connections ({}) to finish ...", activeSnapshots.size());
                try {
                    activeSnapshots.wait(DEFAULT_CONNECTION_TIMEOUT);
                } catch (InterruptedException e) {
                }

                // create a copy of the set and rollback-close all active connections
                HashSet<KiWiSnapshotConnection> connectionCopy = new HashSet<>(activeSnapshots);
                for(KiWiSnapshotConnection con : connectionCopy) {
                    if(con.isActive()) {
                        con.rollback();
                    }
                    if(con.isOpen()) {
                        con.close();
                    }
                }
            }
        }

        // call parent
        super.shutDown();
    }
}
