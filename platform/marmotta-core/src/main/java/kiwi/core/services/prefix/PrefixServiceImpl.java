/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kiwi.core.services.prefix;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import kiwi.core.api.config.ConfigurationService;
import kiwi.core.api.prefix.PrefixService;
import kiwi.core.events.ConfigurationChangedEvent;
import kiwi.core.events.SesameStartupEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.marmotta.commons.http.UriUtil;
import org.slf4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Prefix Manager default implementation
 * 
 * @author Sergio Fernández
 * 
 */
@ApplicationScoped
public class PrefixServiceImpl implements PrefixService {

    private static final String CONFIGURATION_PREFIX = "prefix";

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService  configurationService;

    private BiMap<String, String> cache;

    public PrefixServiceImpl() {
        super();
        cache = HashBiMap.create();
    }

    protected void initialize(@Observes SesameStartupEvent event) {
        for (String key : configurationService.listConfigurationKeys(CONFIGURATION_PREFIX)) {
            String prefix = key.substring(CONFIGURATION_PREFIX.length() + 1);
            String namespace = configurationService.getStringConfiguration(key);
            cache.put(prefix, namespace);
        }
        addLocalMappings();
    }

    protected void onConfigurationChange(@Observes ConfigurationChangedEvent event) {
        for (String changedKey : event.getKeys()) {
            if (changedKey.startsWith(CONFIGURATION_PREFIX + ".")) {
                String prefix = changedKey.substring(CONFIGURATION_PREFIX.length() + 1);
                String namespace = configurationService.getStringConfiguration(changedKey);
                cache.put(prefix, namespace);
            }
        }
    }

    protected void addLocalMappings() {
        String base = configurationService.getBaseUri();
        cache.put("local", base + ConfigurationService.RESOURCE_PATH + "/");
        cache.put("context", base + ConfigurationService.CONTEXT_PATH + "/");
    }

    @Override
    public String getNamespace(String prefix) {
        return cache.get(prefix);
    }

    @Override
    public String getPrefix(String namespace) {
        return cache.inverse().get(namespace);
    }

    @Override
    public synchronized void add(String prefix, String namespace) throws IllegalArgumentException, URISyntaxException {
        if (cache.containsKey(prefix)) {
            log.error("prefix " + prefix + " already managed");
            throw new IllegalArgumentException("prefix " + prefix + " already managed, use forceAdd() if you'd like to force its rewrite");
        } else {
            String validatedNamespace = validateNamespace(namespace);
            if (validatedNamespace != null) {
                try {
                    cache.put(prefix, validatedNamespace);
                    configurationService.setConfiguration(CONFIGURATION_PREFIX + "." + prefix, validatedNamespace);
                } catch (IllegalArgumentException e) {
                    log.error("namespace " + validatedNamespace + " is already bound to '" + getPrefix(validatedNamespace) + "' prefix, use forceAdd() if you'd like to force its rewrite");
                    throw new IllegalArgumentException("namespace " + validatedNamespace + " is already bound to '" + getPrefix(validatedNamespace) + "' prefix");
                }
            } else {
                log.error("Namespace <" + namespace + "> is not valid");
                throw new URISyntaxException(namespace, "Namespace <" + namespace + "> is not valid");
            }
        }
    }

    @Override
    public void forceAdd(String prefix, String namespace) {
        cache.forcePut(prefix, namespace);
        configurationService.setConfiguration(CONFIGURATION_PREFIX + prefix, namespace);
    }

    @Override
    public Map<String, String> getMappings() {
        return Collections.unmodifiableMap(cache);
    }

    @Override
    public boolean containsPrefix(String prefix) {
        return cache.containsKey(prefix);
    }

    @Override
    public boolean containsNamespace(String namespace) {
        return cache.containsValue(namespace);
    }

    private String validateNamespace(String namespace) {
        String last = namespace.substring(namespace.length() - 1);
        if (last.compareTo("/") != 0) {
            if (last.compareTo("#") != 0) {
                namespace += "#";
            }
        }
        if (UriUtil.validate(namespace)) {
            return namespace;
        } else {
            return null;
        }
    }

    @Override
    public String getCurie(String uri) {
        if (UriUtil.validate(uri)) {
            String ns = UriUtil.getNamespace(uri);
            String ref = UriUtil.getReference(uri);
            if (StringUtils.isNotBlank(ns) && StringUtils.isNotBlank(ref) && containsNamespace(ns))
                return getPrefix(ns) + ":" + ref;
            else
                return null;
        } else
            return null;
    }

    @Override
    public String serializePrefixMapping() {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> mapping : cache.entrySet()) {
            sb.append("\n").append(mapping.getKey()).append(": ").append(mapping.getValue()).append(" ");
        }
        return sb.toString();
    }

    @Override
    public String serializePrefixesSparqlDeclaration() {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> mapping : cache.entrySet()) {
            sb.append("PREFIX ").append(mapping.getKey()).append(": <").append(mapping.getValue()).append("> \n");
        }
        return sb.toString();
    }

}
