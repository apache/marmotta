/**
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
package org.apache.marmotta.platform.core.services.modules;

import com.google.common.io.ByteStreams;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.modules.MarmottaResourceService;
import org.apache.marmotta.platform.core.api.modules.ModuleService;
import org.apache.marmotta.platform.core.api.modules.ResourceEntry;
import org.apache.marmotta.platform.core.events.SystemStartupEvent;
import org.apache.marmotta.platform.core.model.module.ModuleConfiguration;
import org.apache.marmotta.platform.core.qualifiers.cache.MarmottaCache;
import org.apache.tika.Tika;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * A service for resolving and accessing resources contained in the Apache Marmotta modules. The resource service takes care
 * of retrieving, caching and refreshing resources from the appropriate locations.
 * <p/>
 * Note that the resource service is not to be confused with the RDF resources maintained by the server. It is
 * purely meant to retrieve static non-Java resources contained in the modules and web application.
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
@ApplicationScoped
public class MarmottaResourceServiceImpl implements MarmottaResourceService {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ModuleService moduleService;

    @Inject @MarmottaCache("resource-cache")
    private ConcurrentMap resourceCache;

    private Map<String,String> resourceMap;

    private Tika tika;

    @PostConstruct
    public void initialise() {
        // find all kiwi-module.properties and check whether they contain a baseurl property to map module web
        // resources to a certain path prefix; if yes, store the prefix and jar URL in the map for lookup and
        // resource resolving by the filter
        this.resourceMap = new HashMap<>();

        for(String module : moduleService.listModules()) {
            ModuleConfiguration config = moduleService.getModuleConfiguration(module);
            if(config.getConfiguration().containsKey("baseurl")) {
                String path = config.getConfiguration().getString("baseurl");
                resourceMap.put(path.startsWith("/")?path:"/"+path,moduleService.getModuleJar(module).toString());
            }
        }

        this.tika = new Tika();
    }

    /**
     * Makes sure the service is initialised on system startup
     *
     * @param event
     */
    public void systemStartup(@Observes SystemStartupEvent event) {

    }

    /**
     * Return the resource identified by the relative URL passed as argument. The passed argument is relative
     * to the web application root of this web application.
     *
     * @param relativeURL a URL relative to the web application root of this web application
     * @return the resource identified by the relative URL, or null if it does not exist
     */
    @Override
    public ResourceEntry getResource(String relativeURL) {
        ResourceEntry data = null;

        // check the request path prefix whether it matches with one of the prefixes in the prefix mapping; if yes,
        // lookup the resource as follows:
        // 1. look in the cache using the path of the request url as cache key
        // 2. if not found: look in the jar file by mapping the resource to the correct jar file and retrieving it
        //    from the "web" folder contained therein
        // 3. if not found: proceed with the chain by calling chain.doFilter()
        // TODO: (FIXME) data_cache.get(path) might return null some times even though path is in cache
        if(isCached(relativeURL)) {
            data = getFromCache(relativeURL);
        } else {
            try {
                URL jarUrl = resolveResource(relativeURL);
                if(jarUrl != null) {
                    try {
                        byte[] bytes = ByteStreams.toByteArray(jarUrl.openStream());
                        data = new ResourceEntry(jarUrl, bytes, bytes.length, getMimeType(relativeURL));
                        log.debug("retrieved resource {} (mime type {}, length {} bytes)", jarUrl.toString(), data.getContentType(), data.getLength());
                    } catch (NullPointerException e) {
                        // This happens if a directory is accessed in the jar-file.
                        data = null;
                    }
                    putInCache(relativeURL,data);
                } else {
                    putInCache(relativeURL,null);
                    log.debug("resource {} not found in any module", relativeURL);
                }
            } catch(IOException ex) {
                log.debug("error while trying to retrieve resource {}: {}", relativeURL, ex.getMessage());
            }
        }
        return data;

    }

    /**
     * Return the file system URL of the resource identified by the relative HTTP URL passed as argument.
     * he passed argument is relative to the web application root of this web application.
     *
     * @param relativeURL a URL relative to the web application root of this web application
     * @return the file system URL of the resource, regardless whether it actually exists or not
     */
    @Override
    public URL resolveResource(String relativeURL) {

        // we take the first match from the resource map ...
        for(String key : resourceMap.keySet()) {
            if(relativeURL.startsWith(key)) {

                // the name of the resource inside the jar file
                String entryPath    = relativeURL.substring(key.length());
                // map "/" to /index.html
                if(entryPath.endsWith("/") || entryPath.equals("")) {
                    entryPath += "index.html";
                }

                // the base URL of the jar file in the file system
                String jarUrlBase = resourceMap.get(key);

                // the JAR URL of the resource inside the jar file
                String jarUrlEntry;

                if(jarUrlBase.endsWith(".jar")) {
                    jarUrlEntry = "jar:" + jarUrlBase + "!/web" + ( entryPath.startsWith("/") ? entryPath : "/" + entryPath);
                } else {
                    jarUrlEntry = jarUrlBase + (jarUrlBase.endsWith("/")?"":"/") + "web" + ( entryPath.startsWith("/") ? entryPath : "/" + entryPath);
                }

                try {
                    return new URL(jarUrlEntry);
                } catch(IOException ex) {
                    log.debug("error while trying to retrieve resource {}: {}", jarUrlEntry, ex.getMessage());
                }
            }
        }
        return null;
    }

    private boolean isCacheEnabled() {
        return configurationService.getBooleanConfiguration("resources.servercache.enabled", false);
    }

    private boolean isCached(String key) {
        return isCacheEnabled() && resourceCache.containsKey(key) && resourceCache.get(key) != null;
    }

    private ResourceEntry getFromCache(String key) {
        if (isCacheEnabled())
            return (ResourceEntry) resourceCache.get(key);
        else
            return null;
    }

    /**
     * Store in the cache
     */
    private void putInCache(String key, ResourceEntry data) {
        if(isCacheEnabled()) {
            resourceCache.put(key, data);
        }
    }

    private String getMimeType(URL resource) {
        return getMimeType(resource.toString());
    }

    private String getMimeType(String resource) {
        return tika.detect(resource);
    }

}
