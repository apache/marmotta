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

package org.apache.marmotta.platform.sparql.services.sail;

import org.apache.marmotta.kiwi.sparql.sail.KiWiSparqlSail;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.NotifyingSailProvider;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.helpers.NotifyingSailWrapper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * This service wraps enhanced KiWi SPARQL support around the KiWi Store to improve the performance of typical
 * SPARQL queries.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class SPARQLSailProvider implements NotifyingSailProvider {

    public static final String SPARQL_STRATEGY = "sparql.strategy";


    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    @Override
    public NotifyingSailWrapper createSail(NotifyingSail parent) {
        KiWiSparqlSail sail = new KiWiSparqlSail(parent);

        return sail;
    }

    public void configurationChanged(@Observes ConfigurationChangedEvent e) {
        if(e.containsChangedKey(SPARQL_STRATEGY)) {
            sesameService.shutdown();
            sesameService.initialise();
        }
    }


    @Override
    public String getName() {
        return "SPARQL Optimizer";
    }

    @Override
    public boolean isEnabled() {
        if("native".equalsIgnoreCase(configurationService.getStringConfiguration(SPARQL_STRATEGY))) {
            return true;
        } else {
            return false;
        }
    }
}
