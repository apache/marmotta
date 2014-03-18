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
package org.apache.marmotta.platform.backend.bigdata;

import com.bigdata.Banner;
import com.bigdata.rdf.sail.BigdataSail;
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
import java.io.File;
import java.util.Properties;

/**
 * A store provider implementation for Apache Marmotta providing instances of "BigData" stores. This provider uses
 * the BigData library developed by SysTap (http://www.systap.com/bigdata.htm).
 *
 *
 * NOTE: the BigData library is published under GPL license, and therefore requires special consideration when distributing packages.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class BigDataStoreProvider implements StoreProvider {

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
        log.info("Initializing Backend: BigData Store");

        File journal = new File(configurationService.getHome() + File.separator + "bigdata" + File.separator + "bigdata.jnl");

        Properties properties = new Properties();
        properties.setProperty( BigdataSail.Options.FILE, journal.getAbsolutePath());
        properties.setProperty( BigdataSail.Options.QUADS, "true");
        properties.setProperty( BigdataSail.Options.TRUTH_MAINTENANCE, "false");
        properties.setProperty( BigdataSail.Options.STATEMENT_IDENTIFIERS, "false");
        properties.setProperty( BigdataSail.Options.AXIOMS_CLASS, "com.bigdata.rdf.axioms.NoAxioms");
        properties.setProperty( BigdataSail.Options.TEXT_INDEX, ""+configurationService.getBooleanConfiguration("bigdata.textIndex",true));
        properties.setProperty( Banner.Options.LOG4J_MBEANS_DISABLE, "true");
        properties.setProperty( BigdataSail.Options.ISOLATABLE_INDICES, "true");
        properties.setProperty( BigdataSail.Options.VOCABULARY_CLASS, "com.bigdata.rdf.vocab.NoVocabulary");
        properties.setProperty( BigdataSail.Options.JUSTIFY, "false");

        return new BigDataSesame27Sail(properties);
    }


    public void configurationChanged(@Observes ConfigurationChangedEvent e) {
        if(e.containsChangedKeyWithPrefix("bigdata")) {
            sesameService.restart();
        }
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
        return new BigDataSesame27Repository(sail);
    }


    /**
     * Return the name of the provider. Used e.g. for displaying status information or logging.
     *
     * @return
     */
    @Override
    public String getName() {
        return "BigData Store";
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
