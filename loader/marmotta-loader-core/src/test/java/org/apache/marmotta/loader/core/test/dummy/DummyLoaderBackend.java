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
package org.apache.marmotta.loader.core.test.dummy;

import com.google.common.collect.Sets;
import org.apache.commons.cli.Option;
import org.apache.commons.configuration.Configuration;
import org.apache.marmotta.loader.api.LoaderBackend;
import org.apache.marmotta.loader.api.LoaderHandler;

import java.util.Collection;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class DummyLoaderBackend implements LoaderBackend {
    public static final String METHOD_SLEEP_MILLIS = "loader.dummy.method_sleep";

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
        return "dummy";
    }

    /**
     * Create the RDFHandler to be used for bulk-loading, optionally using the configuration passed as argument.
     *
     * @param configuration
     * @return a newly created RDFHandler instance
     */
    @Override
    public LoaderHandler createLoader(Configuration configuration) {
        return new DummyLoaderHandler(configuration.getLong(METHOD_SLEEP_MILLIS, 0l));
    }

    /**
     * Return any additional options that this backend offers (e.g. for connecting to a database etc).
     * If there are no additional options, return an empty collection.
     *
     * @return
     */
    @Override
    public Collection<Option> getOptions() {
        return Sets.newHashSet(new Option("U", "user", true, "dummy user"), new Option("E", "enabled", false, "dummy enabled"));
    }
}
