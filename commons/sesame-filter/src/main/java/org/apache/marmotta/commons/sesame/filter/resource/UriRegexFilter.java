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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A filter only accepting URI resources where the URI matches one of the configured patterns.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class UriRegexFilter implements ResourceFilter {

    private Set<Pattern> patterns;


    public UriRegexFilter(Collection<String> regexps) {
        patterns = new HashSet<Pattern>();

        for(String s : regexps) {
            Pattern p = Pattern.compile(s);
            patterns.add(p);
        }

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

        for(Pattern p : patterns) {
            if(p.matcher(uri.stringValue()).matches()) {
                return true;
            }
        }


        return false;
    }
}
