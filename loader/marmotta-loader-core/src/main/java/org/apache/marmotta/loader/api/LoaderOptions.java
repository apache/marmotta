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
     * Paths to archives to import
     */
    public static final String ARCHIVES    = "loader.archives";


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
