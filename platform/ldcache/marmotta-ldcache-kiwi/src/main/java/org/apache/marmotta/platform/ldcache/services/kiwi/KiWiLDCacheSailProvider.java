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
package org.apache.marmotta.platform.ldcache.services.kiwi;

import com.google.common.collect.Lists;
import org.apache.marmotta.commons.sesame.filter.NotFilter;
import org.apache.marmotta.commons.sesame.filter.OneOfFilter;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.ldcache.sail.KiWiLinkedDataSail;
import org.apache.marmotta.ldcache.services.LDCache;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.platform.core.model.filter.MarmottaLocalFilter;
import org.apache.marmotta.platform.ldcache.api.ldcache.LDCacheSailProvider;
import org.openrdf.model.Resource;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.helpers.NotifyingSailWrapper;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Set;

/**
 * A sail provider service that allows wrapping a transparent Linked Data caching component around the
 * main SAIL. This service builds on the KiWi triple store and represents cache entries in a separate table
 * of the database.
 *
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class KiWiLDCacheSailProvider extends LDCacheSailProvider {


    private KiWiLinkedDataSail sail;

    /**
     * Return the name of the provider. Used e.g. for displaying status information or logging.
     *
     * @return
     */
    @Override
    public String getName() {
        return "Linked Data Caching (KiWi Backend)";
    }


    /**
     * Create the sail wrapper provided by this SailProvider
     *
     * @param parent the parent sail to wrap by the provider
     * @return the wrapped sail
     */
    @Override
    public NotifyingSailWrapper createSail(NotifyingSail parent) {
        Set<SesameFilter<Resource>> filters = new HashSet<SesameFilter<Resource>>();
        filters.add(MarmottaLocalFilter.getInstance());
        filters.addAll(Lists.newArrayList(ignoreFilters));

        SesameFilter<Resource> cacheFilters = new OneOfFilter<Resource>(filters);

        String cache_context = configurationService.getCacheContext();
        sail = new KiWiLinkedDataSail(parent, new NotFilter<Resource>(cacheFilters), cache_context, ldclientConfig);
        return sail;
    }


    /**
     * Return the Linked Data Client used by the caching system (e.g. for debugging).
     * @return
     */
    public LDClientService getLDClient() {
        if(sail != null) {
            return sail.getLDCache().getClient();
        } else {
            return null;
        }
    }

    /**
     * Return the caching backend used by the caching system (e.g. for debugging)
     * @return
     */
    public LDCache getLDCache() {
        if(sail != null) {
            return sail.getLDCache();
        } else {
            return null;
        }
    }

    /**
     * Clear the currently configured Linked Data Sail.
     */
    @Override
    public void clearSail() {
        sail = null;
    }
}
