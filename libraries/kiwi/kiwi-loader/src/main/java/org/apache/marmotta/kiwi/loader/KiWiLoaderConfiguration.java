package org.apache.marmotta.kiwi.loader;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

import java.util.HashMap;

/**
 * Configuration options for the KiWiLoader
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiLoaderConfiguration {

    public static final String LOADER_COMMIT_BATCH_SIZE = "loader.commitBatchSize";
    public static final String LOADER_STATEMENT_BATCH_SIZE = "loader.statementBatchSize";
    public static final String LOADER_STATEMENT_EXISTENCE_CHECK = "loader.statementExistenceCheck";
    public static final String LOADER_CONTEXT = "loader.context";
    public static final String LOADER_DROP_INDEXES = "loader.dropIndexes";
    public static final String LOADER_STATISTICS_ENABLED = "loader.statistics.enabled";
    public static final String LOADER_STATISTICS_GRAPH = "loader.statistics.graph";

    /**
     * the size of a batch insert into the database; only when this number of statements has been processed will
     * an insert statement to the database be issued.
     */

    int statementBatchSize = 1000;

    /**
     * the size of a database transaction; the database transaction will commit after this number of statements
     */
    int commitBatchSize = 10000;

    /**
     * If true, the importer will check if a statement already exists; this check is necessary to ensure consistency
     * of the database, but it is also very expensive, because every triple needs to be checked. Set this option to
     * false in case you are sure that every imported triple does not yet exist in the database.
     */
    boolean statementExistanceCheck = false;

    /**
     * Import into this context, ignoring context provided by the statements
     */
    String context;

    private Configuration config;

    public KiWiLoaderConfiguration() {
        this(new MapConfiguration(new HashMap<String,Object>()));
    }

    public KiWiLoaderConfiguration(Configuration config) {
        this.config = config;
    }

    public int getCommitBatchSize() {
        return config.getInt(LOADER_COMMIT_BATCH_SIZE,commitBatchSize);
    }

    public void setCommitBatchSize(int commitBatchSize) {
        config.setProperty(LOADER_COMMIT_BATCH_SIZE, commitBatchSize);
    }

    public int getStatementBatchSize() {
        return config.getInt(LOADER_STATEMENT_BATCH_SIZE,statementBatchSize);
    }

    public void setStatementBatchSize(int statementBatchSize) {
        config.setProperty(LOADER_STATEMENT_BATCH_SIZE, statementBatchSize);
    }

    public boolean isStatementExistanceCheck() {
        return config.getBoolean(LOADER_STATEMENT_EXISTENCE_CHECK,statementExistanceCheck);
    }

    public void setStatementExistanceCheck(boolean statementExistanceCheck) {
        config.setProperty(LOADER_STATEMENT_EXISTENCE_CHECK, statementExistanceCheck);
    }

    public String getContext() {
        return config.getString(LOADER_CONTEXT, context);
    }

    public void setContext(String context) {
        config.setProperty(LOADER_CONTEXT, context);
    }


    public boolean isDropIndexes() {
        return config.getBoolean(LOADER_DROP_INDEXES, true);
    }

    public void setDropIndexes(boolean v) {
        config.setProperty(LOADER_DROP_INDEXES,v);
    }


    /**
     * Statistics collection (using rrd) enabled? Will generate performance graphs at certain time intervals.
     *
     * @return
     */
    public boolean isStatistics() {
        return config.getBoolean(LOADER_STATISTICS_ENABLED, false);
    }

    public void setStatistics(boolean v) {
        config.setProperty(LOADER_STATISTICS_ENABLED,v);
    }


    public String getStatisticsGraph() {
        return config.getString(LOADER_STATISTICS_GRAPH, "kiwiloader.png");
    }

    public void setStatisticsGraph(String path) {
        config.setProperty(LOADER_STATISTICS_GRAPH, path);
    }

}
