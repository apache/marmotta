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
package org.apache.marmotta.platform.versioning.services;

import org.apache.marmotta.commons.sesame.filter.AllOfFilter;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.commons.sesame.filter.statement.StatementFilter;
import org.apache.marmotta.kiwi.transactions.api.TransactionalSail;
import org.apache.marmotta.kiwi.transactions.wrapper.TransactionalSailWrapper;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.kiwi.versioning.repository.SnapshotRepositoryConnection;
import org.apache.marmotta.kiwi.versioning.sail.KiWiVersioningSail;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.api.triplestore.TransactionalSailProvider;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A SAIL provider wrapping a versioning component around the repository
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class VersioningSailProvider implements TransactionalSailProvider {

    public static final String VERSIONING_ENABLED = "versioning.enabled";
    @Inject
    private Logger                    log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    @Inject
    @Named("versioning")
    private Instance<StatementFilter> filters;


    private KiWiVersioningSail sail;

    /**
     * Return the name of the provider. Used e.g. for displaying status information or logging.
     *
     * @return
     */
    @Override
    public String getName() {
        return "Versioning";
    }

    /**
     * Return true if this sail provider is enabled in the configuration.
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return configurationService.getBooleanConfiguration(VERSIONING_ENABLED,true);
    }


    public void configurationChanged(@Observes ConfigurationChangedEvent e) {
        if(e.containsChangedKey(VERSIONING_ENABLED)) {
            sesameService.restart();
        }
    }

    /**
     * Create the sail wrapper provided by this SailProvider
     *
     * @param parent the parent sail to wrap by the provider
     * @return the wrapped sail
     */
    @Override
    public TransactionalSailWrapper createSail(TransactionalSail parent) {
        Set<SesameFilter<Statement>> sFilters = new HashSet<SesameFilter<Statement>>();

        StatementFilter filterCached = new StatementFilter() {
            @Override
            public boolean accept(Statement object) {
                if(object.getContext() != null && configurationService.getCacheContext().equals(object.getContext().stringValue())) return false;
                else
                    return true;
            }
        };
        sFilters.add(filterCached);

        StatementFilter filterInferred = new StatementFilter() {
            @Override
            public boolean accept(Statement object) {
                if(object.getContext() != null && configurationService.getInferredContext().equals(object.getContext().stringValue())) return false;
                else
                    return true;
            }
        };
        sFilters.add(filterInferred);

        StatementFilter filterEnhancing = new StatementFilter() {
            @Override
            public boolean accept(Statement object) {
                if(object.getContext() != null && configurationService.getEnhancerContex().equals(object.getContext().stringValue())) return false;
                else
                    return true;
            }
        };
        sFilters.add(filterEnhancing);

        Iterator<StatementFilter> fIt = filters.iterator();
        while (fIt.hasNext()) {
            StatementFilter f = fIt.next();
            log.info("Auto-Registering filter {}", f.getClass().getSimpleName());
            sFilters.add(f);
        }

        sail = new KiWiVersioningSail(parent, new AllOfFilter<Statement>(sFilters));

        return sail;
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
    public SnapshotRepositoryConnection getSnapshot(Date snapshotDate) throws RepositoryException {
        try {
            return new SnapshotRepositoryConnection(sesameService.getRepository(),sail.getSnapshot(snapshotDate));
        } catch (SailException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * List all versions of this repository.
     *
     * @return
     */
    public RepositoryResult<Version> listVersions() throws SailException {
        return sail.listVersions();
    }

    /**
     * List all versions of this repository between a start and end date.
     *
     * @return
     */
    public RepositoryResult<Version> listVersions(Date from, Date to) throws SailException {
        return sail.listVersions(from, to);
    }

    /**
     * List all versions of this repository affecting the given resource as subject.
     *
     * @return
     */
    public RepositoryResult<Version> listVersions(Resource r) throws SailException {
        return sail.listVersions(r);
    }

    /**
     * List all versions of this repository affecting the given resource as subject between a start and end date.
     *
     * @return
     */
    public RepositoryResult<Version> listVersions(Resource r, Date from, Date to) throws SailException {
        return sail.listVersions(r, from, to);
    }

    public Version getVersion(Long id) throws SailException {
        return sail.getVersion(id);
    }

    /**
     * Return the version that is the most recent version for a resource given a reference date. The method will either
     * return the version that was current for the resource at the given date or return null in case such a version
     * does not exist (e.g. before the resource was created).
     *
     * @param resource  the resource for which to find a version
     * @param date      the reference date
     * @return the latest version of the resource at the given date, or null if such a version does not exist
     * @throws java.sql.SQLException
     */
    public Version getLatestVersion(Resource r, Date date) throws SailException {
        return sail.getLatestVersion(r, date);
    }

    /**
     * Remove the version with the id passed as argument, including all references to added and removed triples. The
     * triples themselves are not deleted immediately, we let the garbage collector carry this out periodically.
     * @param id  the database ID of the version (see {@link org.apache.marmotta.kiwi.versioning.model.Version#getId()})
     * @throws org.openrdf.sail.SailException
     */
    public void removeVersion(Long id) throws SailException {
        sail.removeVersion(id);
    }

    /**
     * Remove all versions in the given time interval. Iterates over all versions and deletes them individually.
     * Entries in join tables (added/removed triples) are also deleted, the triples themselves not. Deleted triples
     * without version will later be cleaned up by the garbage collector
     * @param from date after which versions will be deleted
     * @param to   date before which versions will be deleted
     * @throws org.openrdf.sail.SailException
     */
    public void removeVersions(Date from, Date to) throws SailException {
        sail.removeVersions(from, to);
    }

    /**
     * Remove all versions until the date given as argument. Iterates over all versions and deletes them individually.
     * Entries in join tables (added/removed triples) are also deleted, the triples themselves not. Deleted triples
     * without version will later be cleaned up by the garbage collector
     * @param until date until when to delete versions
     * @throws org.openrdf.sail.SailException
     */
    public void removeVersions(Date until) throws SailException {
        sail.removeVersions(until);
    }

    /**
     * Revert (undo) the version given as argument. This method creates a new transaction, adds all triples
     * that were deleted in the old version, removes all triples that were added in the old version, and commits
     * the transaction, effectively creating a new (reverted) version.
     *
     * @param version    the version to revert
     * @throws org.openrdf.sail.SailException in case reverting the version failed
     */
    public void revertVersion(Version version) throws SailException {
        sail.revertVersion(version);
    }
}
