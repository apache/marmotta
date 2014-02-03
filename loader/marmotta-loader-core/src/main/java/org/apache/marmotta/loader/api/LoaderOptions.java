package org.apache.marmotta.loader.api;

/**
 * Contains configuration keys that can be used in a loader configuration.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LoaderOptions {

    /**
     * Base URI to use. Configuration values need to be proper URI Strings.
     */
    public static final String BASE_URI = "loader.base";

    /**
     * Compression used by input. Either gzip, bzip2, or auto/null.
     */
    public static final String COMPRESSION = "loader.compression";

    /**
     * Backend to use by loader (in case it cannot be auto-detected)
     */
    public static final String BACKEND     = "loader.backend";


    /**
     * Optional URI of context to import data into.
     */
    public static final String CONTEXT     = "loader.context";


    /**
     * MIME type of format to import (auto-guessing if not given)
     */
    public static final String FORMAT      = "loader.format";


    /**
     * Paths to files to import.
     */
    public static final String FILES       = "loader.files";


    /**
     * Paths to directories to import
     */
    public static final String DIRS        = "loader.dirs";

    /**
     * Enable statistics collection. Configuration value needs to be a boolean.
     */
    public static final String STATISTICS_ENABLED = "loader.statistics.enabled";

    /**
     * Write statistics graph into this file. Configuration option must be the path to a writeable file.
     */
    public static final String STATISTICS_GRAPH = "loader.statistics.graph";

    /**
     * Interval at which to write out statistics information.
     */
    public static final String STATISTICS_INTERVAL = "loader.statistics.interval";

}
