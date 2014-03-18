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
package org.apache.marmotta.loader.berkeley;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.marmotta.loader.api.LoaderBackend;
import org.apache.marmotta.loader.api.LoaderHandler;
import org.apache.marmotta.loader.titan.TitanLoaderHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class BerkeleyDBLoaderBackend implements LoaderBackend {


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
        return "berkeley";
    }

    /**
     * Create the RDFHandler to be used for bulk-loading, optionally using the configuration passed as argument.
     *
     * @param configuration
     * @return a newly created RDFHandler instance
     */
    @Override
    public LoaderHandler createLoader(Configuration configuration) {

        Configuration titanCfg = new MapConfiguration(new HashMap<String,Object>());
        titanCfg.setProperty("storage.backend", "berkeleyje");
        //titanCfg.setProperty("storage.batch-loading", true);

        if(configuration.containsKey("backend.berkeley.storage-directory")) {
            titanCfg.setProperty("storage.directory", configuration.getString("backend.berkeley.storage-directory"));
        }

        titanCfg.setProperty("storage.buffer-size", 100000);

        return new TitanLoaderHandler(titanCfg);
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

        Option directory =
                OptionBuilder.withArgName("storage-directory")
                                .hasArgs(1)
                                .withDescription("directory where to store the berkeley database")
                                .withLongOpt("storage-directory")
                                .create('S');
        options.add(directory);


        return options;
    }
}
