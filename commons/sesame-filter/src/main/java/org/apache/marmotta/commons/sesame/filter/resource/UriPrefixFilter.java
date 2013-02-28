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
package org.apache.marmotta.commons.sesame.filter.resource;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A filter only accepting resources starting with a given prefix.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class UriPrefixFilter implements ResourceFilter {


    private Set<String> prefixes;

    public UriPrefixFilter(String... prefixes) {
        this(new HashSet<String>(Arrays.asList(prefixes)));
    }

    public UriPrefixFilter(Set<String> prefixes) {
        this.prefixes = prefixes;
    }

    public Set<String> getPrefixes() {
        return prefixes;
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
        if(! (resource instanceof URI)) {
            return false;
        }

        URI uri = (URI) resource;

        for(String prefix : prefixes) {
            if(uri.stringValue().startsWith(prefix)) {
                return true;
            }
        }


        return false;
    }

}
