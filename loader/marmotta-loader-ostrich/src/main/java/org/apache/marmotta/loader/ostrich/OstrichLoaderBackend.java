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

package org.apache.marmotta.loader.ostrich;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.marmotta.loader.api.LoaderBackend;
import org.apache.marmotta.loader.api.LoaderHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Ostrich loader backend. Provides configuration for the OstrichLoaderHandler.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class OstrichLoaderBackend implements LoaderBackend {

    /**
     * Create the RDFHandler to be used for bulk-loading, optionally using the configuration passed as argument.
     *
     * @param configuration
     * @return a newly created RDFHandler instance
     */
    @Override
    public LoaderHandler createLoader(Configuration configuration) {
        return new OstrichLoaderHandler(
                configuration.getString("backend.ostrich.host", "localhost"),
                configuration.getInt("backend.ostrich.port", 10000),
                configuration.getLong("backend.ostrich.batchsize", 1000000));
    }

    /**
     * Return a unique identifier for the loader; used for identifying the loader to choose on the command line
     * in case more than one loader implementation is available.
     * <p/>
     * Should match with the regular expression [a-z][a-z0-9]*
     *
     * @return
     */
    @Override
    public String getIdentifier() {
        return "ostrich";
    }

    /**
     * Return any additional options that this backend offers (e.g. for connecting to a database etc).
     * If there are no additional options, return an empty collection.
     *
     * @return
     */
    @Override
    public Collection<Option> getOptions() {
        Set<Option> options = new HashSet<>();

        Option host =
                OptionBuilder.withArgName("host")
                        .hasArgs(1)
                        .withDescription("hostname or IP address of Ostrich/LevelDB server")
                        .withLongOpt("host")
                        .create('H');
        options.add(host);

        Option port =
                OptionBuilder.withArgName("port")
                        .hasArgs(1)
                        .withDescription("port used by Ostrich/LevelDB server")
                        .withLongOpt("port")
                        .create('P');
        options.add(port);

        Option batchSize =
                OptionBuilder.withArgName("batchsize")
                        .hasArgs(1)
                        .withDescription("maximum number of statements to commit in one batch (default 1M)")
                        .withLongOpt("batchsize")
                        .create('B');
        options.add(batchSize);

        return options;
    }
}
