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
package org.apache.marmotta.platform.backend.accumulograph;

import com.tinkerpop.blueprints.oupls.sail.GraphSail;
import edu.jhuapl.tinkerpop.AccumuloGraph;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.api.triplestore.StoreProvider;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.Sail;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

/**
 * Blueprints Implementation by Accumulo Graph
 * @author Raffaele Palmieri
 * @see https://github.com/JHUAPL/AccumuloGraph
 */
@ApplicationScoped
public class AccumuloGraphProvider implements StoreProvider {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    /**
     * Create the store provided by this SailProvider
     *
     * @return a new instance of the store
     */
    @Override
    public NotifyingSail createStore() {
        log.info("Initializing Backend: AccumuloGraph Store");
        final AccumuloGraph graph = createAccumuloGraph();
        return new GraphSail(graph);
    }

    /**
     * Observes configuration change
     */
    public void configurationChanged(@Observes ConfigurationChangedEvent e) {
        if(e.containsChangedKeyWithPrefix("accumulograph")) {
            sesameService.restart();
        }
    }

    /**
     * Create the repository using the sail given as argument.
     * This method is needed because some backends
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
     * Create Accumulograph from the configuration
     *
     * @return AccumuloGraph
     */
    public AccumuloGraph createAccumuloGraph() {
        Configuration conf = new BaseConfiguration();
        List<String> accumuloGraphConf = configurationService.listConfigurationKeys("accumulograph");
        for (String key : accumuloGraphConf) {
            String accumuloGraphKey = key.replaceFirst("^accumulograph\\.", "");
            conf.setProperty(accumuloGraphKey, configurationService.getStringConfiguration(key));
        }
        return new AccumuloGraph(conf);
    }

    /**
     * Return the name of the provider. Used e.g. for displaying status information or logging.
     *
     * @return
     */
    @Override
    public String getName() {
        return "AccumuloGraph";
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
}
