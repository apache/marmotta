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
package kiwi.core.model.filter;

import kiwi.core.api.config.ConfigurationService;
import kiwi.core.util.KiWiContext;

import org.apache.marmotta.commons.sesame.filter.resource.ResourceFilter;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

/**
 * Accept only resources that are considered "local", i.e. either have the same URI prefix as the system,
 * start with file:, start with urn:, or are blank nodes.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LMFLocalFilter implements ResourceFilter {

    private ConfigurationService configurationService;

    public LMFLocalFilter() {
        configurationService = KiWiContext.getInstance(ConfigurationService.class);
    }


    private static LMFLocalFilter instance = null;

    public static LMFLocalFilter getInstance() {
        if(instance == null) {
            instance = new LMFLocalFilter();
        }
        return instance;
    }


    /**
     * Return false in case the filter does not accept the resource passed as argument, true otherwise.
     *
     *
     * @param resource
     * @return
     */
    @Override
    public boolean accept(Resource resource) {
        if(resource instanceof BNode) {
            return true;
        }

        URI uri = (URI)resource;

        if(uri.stringValue().startsWith("file:") || uri.stringValue().startsWith("urn:")) {
            return true;
        }

        return uri.stringValue().startsWith(configurationService.getBaseUri());


    }

}
