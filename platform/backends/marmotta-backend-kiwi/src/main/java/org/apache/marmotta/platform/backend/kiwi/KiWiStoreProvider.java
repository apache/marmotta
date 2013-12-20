/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.platform.backend.kiwi;

import com.google.common.collect.ImmutableList;
import org.apache.marmotta.kiwi.caching.config.KiWiQueryCacheConfiguration;
import org.apache.marmotta.kiwi.caching.sail.KiWiCachingSail;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.exception.DriverNotFoundException;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.sparql.sail.KiWiSparqlSail;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.api.triplestore.StoreProvider;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.infinispan.manager.EmbeddedCacheManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.Sail;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * A store implementation providing an instance of a KiWi triple store as backend for the Marmotta platform. Uses the
 * following configuration properties from the configuration service:
 * <ul>
 *     <li>database.type: which database dialect to use (currently "h2", "mysql", "postgres")</li>
 *     <li>database.url:  the JDBC url of the database to connect to</li>
 *     <li>database.user: the user to use for connecting to the database</li>
 *     <li>database.password: the password to use for connecting to the database</li>
 *     <li>database.batchcommit: commit triples in batches instead of individual inserts - faster but no recovery if
 *         system stops during transaction</li>
 *     <li>database.batchsize: how many triples to keep in one batch (default 10000)</li>
 *     <li>database.generator: which strategy to use for generating database ids (options are "snowflake", "database",
 *         "memory", "uuid-time", and "uuid-random", "snowflake" is very fast and reliable and therefore preferred)</li>
 * </ul>
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class KiWiStoreProvider implements StoreProvider {

    public static final String SPARQL_STRATEGY    = "sparql.strategy";
    public static final String DATACENTER_ID      = "database.datacenter.id";
    public static final String FULLTEXT_ENABLED   = "database.fulltext.enabled";
    public static final String FULLTEXT_LANGUAGES = "database.fulltext.languages";
    public static final String DEBUG_SLOWQUERIES = "database.debug.slowqueries";
    public static final String CLUSTERING_ENABLED = "clustering.enabled";
    public static final String CACHING_LITERAL_SIZE = "caching.literal.size";
    public static final String CACHING_BNODE_SIZE = "caching.bnode.size";
    public static final String CACHING_URI_SIZE = "caching.uri.size";
    public static final String CACHING_TRIPLE_SIZE = "caching.triple.size";
    public static final String CLUSTERING_NAME = "clustering.name";
    public static final String CACHING_QUERY_ENABLED = "caching.query.enabled";
    public static final String CACHING_QUERY_SIZE = "caching.query.size";
    public static final String CACHING_QUERY_LIMIT = "caching.query.limit";

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;


    @Inject
    private EmbeddedCacheManager cacheManager;

    /**
     * Create the store provided by this SailProvider
     *
     * @return a new instance of the store
     */
    @Override
    public NotifyingSail createStore() {
        log.info("Initializing Backend: KiWi Store");

        String database = configurationService.getStringConfiguration("database.type");
        KiWiDialect dialect;
        try {
            if("h2".equalsIgnoreCase(database)) {
                dialect = new H2Dialect();
            } else if("mysql".equalsIgnoreCase(database)) {
                dialect = new MySQLDialect();
            } else if("postgres".equalsIgnoreCase(database)) {
                dialect = new PostgreSQLDialect();
            } else
                throw new IllegalStateException("database type "+database+" currently not supported!");
        } catch (DriverNotFoundException dnf) {
            log.error("{}, can't build KiwiStore.", dnf.getMessage());
            throw dnf;
        }
        
        String jdbcUrl = configurationService.getStringConfiguration("database.url");
        String dbUser  = configurationService.getStringConfiguration("database.user");
        String dbPass  = configurationService.getStringConfiguration("database.password");

        KiWiConfiguration configuration = new KiWiConfiguration(configurationService.getStringConfiguration(CLUSTERING_NAME, "Marmotta") + " KiWi", jdbcUrl, dbUser, dbPass, dialect, configurationService.getDefaultContext(), configurationService.getInferredContext());
        configuration.setQueryLoggingEnabled(configurationService.getBooleanConfiguration(DEBUG_SLOWQUERIES, false));
        configuration.setTripleBatchCommit(configurationService.getBooleanConfiguration("database.triples.batchcommit", true));
        configuration.setTripleBatchSize(configurationService.getIntConfiguration("database.triples.batchsize", 10000));

        configuration.setDatacenterId(configurationService.getIntConfiguration(DATACENTER_ID,0));
        configuration.setFulltextEnabled(configurationService.getBooleanConfiguration(FULLTEXT_ENABLED, true));
        configuration.setFulltextLanguages(configurationService.getListConfiguration(FULLTEXT_LANGUAGES, ImmutableList.of("en")));

        configuration.setClustered(configurationService.getBooleanConfiguration(CLUSTERING_ENABLED, false));
        configuration.setClusterName(configurationService.getStringConfiguration(CLUSTERING_NAME, "Marmotta"));

        configuration.setLiteralCacheSize(configurationService.getIntConfiguration(CACHING_LITERAL_SIZE, 100000));
        configuration.setBNodeCacheSize(configurationService.getIntConfiguration(CACHING_BNODE_SIZE, 10000));
        configuration.setUriCacheSize(configurationService.getIntConfiguration(CACHING_URI_SIZE, 500000));
        configuration.setTripleCacheSize(configurationService.getIntConfiguration(CACHING_TRIPLE_SIZE, 100000));

        NotifyingSail base = new KiWiStore(configuration, cacheManager);

        if(configurationService.getBooleanConfiguration(CACHING_QUERY_ENABLED,true)) {
            KiWiQueryCacheConfiguration qcfg = new KiWiQueryCacheConfiguration();
            qcfg.setMaxCacheSize(configurationService.getIntConfiguration(CACHING_QUERY_SIZE, 100000));
            qcfg.setMaxEntrySize(configurationService.getIntConfiguration(CACHING_QUERY_LIMIT, 150));
            base = new KiWiCachingSail(base, qcfg);
        }


        if("native".equalsIgnoreCase(configurationService.getStringConfiguration(SPARQL_STRATEGY))) {
            base = new KiWiSparqlSail(base);
        }

        return base;
    }

    /**
     * Create the repository using the sail given as argument. This method is needed because some backends
     * use custom implementations of SailRepository.
     *
     * @param sail
     * @return
     */
    @Override
    public SailRepository createRepository(Sail sail) {
        return new SailRepository(sail);
    }

    /**
     * Return the name of the provider. Used e.g. for displaying status information or logging.
     *
     * @return
     */
    @Override
    public String getName() {
        return "KiWi Store";
    }

    /**
     * Return true if this sail provider is enabled in the configuration.
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    public void configurationChanged(@Observes ConfigurationChangedEvent e) {
        log.info("configuration changed: {}", e.getKeys());
        if(e.containsChangedKey(SPARQL_STRATEGY) ||
                e.containsChangedKey(DATACENTER_ID) ||
                e.containsChangedKey(FULLTEXT_ENABLED) ||
                e.containsChangedKey(FULLTEXT_LANGUAGES) ||
                e.containsChangedKey(DEBUG_SLOWQUERIES) ||
                e.containsChangedKey(CLUSTERING_ENABLED) ||
                e.containsChangedKey(CACHING_LITERAL_SIZE) ||
                e.containsChangedKey(CACHING_TRIPLE_SIZE) ||
                e.containsChangedKey(CACHING_URI_SIZE) ||
                e.containsChangedKey(CACHING_BNODE_SIZE) ||
                e.containsChangedKey(CACHING_QUERY_ENABLED)
                ) {
            log.info("KiWi backend configuration changed, re-initialising triple store");

            sesameService.restart();
        }
    }

}
