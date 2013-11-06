package org.apache.marmotta.kiwi.loader;

/**
 * Configuration options for the KiWiLoader
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiLoaderConfiguration {

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

    public KiWiLoaderConfiguration() {
    }

    public int getCommitBatchSize() {
        return commitBatchSize;
    }

    public void setCommitBatchSize(int commitBatchSize) {
        this.commitBatchSize = commitBatchSize;
    }

    public int getStatementBatchSize() {
        return statementBatchSize;
    }

    public void setStatementBatchSize(int statementBatchSize) {
        this.statementBatchSize = statementBatchSize;
    }

    public boolean isStatementExistanceCheck() {
        return statementExistanceCheck;
    }

    public void setStatementExistanceCheck(boolean statementExistanceCheck) {
        this.statementExistanceCheck = statementExistanceCheck;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
